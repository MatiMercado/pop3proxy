import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Arrays;

public class ClientInputParser {

    private static final String MINIMAL_CAPA = "+OK sending minimal capabilities\nUSER\nPIPELINING\n.\n";
    private static final String OK = "+OK \n";
    private static final String NOT_ACCEPTED = "-ERR command not accepted without authentication\n";
    private static final String CAPA = "CAPA";
    private static final String USER = "USER ";
    private static final String PASS = "PASS ";

    public static void parseInput(Connection connection, Authenticator authenticator, SelectionKey key, SubjectToL33t transformer) throws IOException {

        ByteBuffer inBuffer = connection.getInputByteBuffer();

        inBuffer.flip();
        int remaining;
        if (connection.getOtherSide() != null) {
            remaining = Math.min(connection.getOtherSide().getOutputByteBuffer().remaining(), inBuffer.remaining());
        } else {
            remaining = inBuffer.remaining();
        }
        byte[] bytes = new byte[remaining];
        inBuffer.get(bytes);
        String received = new String(bytes);
        inBuffer.compact();

        System.out.println("MUA me dice " + received);

        boolean validCommand = false;

        switch (connection.getConnectionStatus()) {
            case CONNECTED:
                if (received.toUpperCase().trim().startsWith(CAPA)) {
                    validCommand = true;
                    connection.getOutputByteBuffer().put(MINIMAL_CAPA.getBytes());

                    key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);


                } else if (received.toUpperCase().trim().startsWith(USER)) {
                    String[] args = received.split(" ");
                    if (args.length > 1) {
                        validCommand = true;
                        connection.setUsername(args[1].trim());
                        connection.setConnectionStatus(ConnectionStatus.WAITING_PASS);
                        connection.getOutputByteBuffer().put(OK.getBytes());
                        key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                    }
                }
                break;
            case WAITING_PASS:
                if (received.toUpperCase().trim().startsWith(PASS)) {
                    String[] args = received.split(" ");
                    if (args.length > 1) {
                        validCommand = true;
                        connection.setPassword(args[1].trim());
                        authenticator.authenticatePlain(connection, key);
                        //TODO: ADD key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                        key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                    }
                }
                break;
            case PROXY_STATE:
                validCommand = true;
                System.out.println("Fowarding to server: " + received);
                connection.getOtherSide().getOutputByteBuffer().put(Arrays.copyOfRange(bytes, 0, remaining));
                connection.getOtherSide().getChannel().register(key.selector(), SelectionKey.OP_WRITE | SelectionKey.OP_READ
                        , connection.getOtherSide());
                break;
        }

        if (!validCommand) {
            connection.getOutputByteBuffer().put(NOT_ACCEPTED.getBytes());
            key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            connection.setConnectionStatus(ConnectionStatus.CONNECTED);
        }

        //TODO: Hace falta un wake aca??
        // Si seteamos la queue si.
        // Si tocamos directamente las keys: Se despierta el selector?

        key.selector().wakeup();

    }
}
