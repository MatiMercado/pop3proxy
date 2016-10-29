import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class ServerInputParser {

    private static final String OK = "+OK";
    private static final String GIVE_USER = "USER %s\n";
    private static final String GIVE_PASSWORD = "PASS %s\n";
    private static final int BUFSIZE = 256;

    public static void parseInput(Connection connection, SelectionKey key, SubjectToL33t transformer) throws IOException {

        ByteBuffer inBuffer = connection.getInputByteBuffer();

        inBuffer.flip();
        int remaining = Math.min(connection.getOtherSide().getOutputByteBuffer().remaining(), inBuffer.remaining());
        byte[] bytes = new byte[remaining];
        inBuffer.get(bytes);
        String received = new String(bytes);
        inBuffer.compact();

        System.out.println("#DEBUG: " + remaining + "!");

        switch (connection.getConnectionStatus()) {
            case WAITING_FOR_BANNER:
                System.out.println("El banner es " + received);
                if (received.startsWith(OK)) {
                    String giveUser = String.format(GIVE_USER, connection.getUsername());
                    System.out.println("DOY " + giveUser);
                    connection.getOutputByteBuffer().put(giveUser.getBytes());
                    key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ); //TODO: Por que quiero escribir en el server??
                    connection.setConnectionStatus(ConnectionStatus.USER_GIVEN);
                } else {
                    connection.getOtherSide().setConnectionStatus(ConnectionStatus.CONNECTED);
                    connection.getOtherSide().getOutputByteBuffer().put((received).getBytes());
                    connection.getOtherSide().getChannel().register(key.selector(), SelectionKey.OP_WRITE | SelectionKey.OP_READ
                            , connection.getOtherSide());
                }
                break;
            case USER_GIVEN:
                if (received.startsWith(OK)) {
                    String givePassword = String.format(GIVE_PASSWORD, connection.getPassword());
                    connection.getOutputByteBuffer().put(givePassword.getBytes());
                    key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                    connection.setConnectionStatus(ConnectionStatus.PASS_GIVEN);
                } else {
                    connection.getOtherSide().setConnectionStatus(ConnectionStatus.CONNECTED);
                    connection.getOtherSide().getOutputByteBuffer().put((received).getBytes());
                    connection.getOtherSide().getChannel().register(key.selector(), SelectionKey.OP_WRITE | SelectionKey.OP_READ
                            , connection.getOtherSide());
                }
                break;
            case PASS_GIVEN:
                if (received.startsWith(OK)) {
                    System.out.println("HANDSHAKE WITH SERVER DONE! ANSWER THAT OK AND GO TO PROXY STATUS");

                    connection.getOtherSide().setConnectionStatus(ConnectionStatus.PROXY_STATE);
                    connection.getOtherSide().getOutputByteBuffer().put((received).getBytes());
                    connection.getOtherSide().getChannel().register(key.selector(), SelectionKey.OP_WRITE | SelectionKey.OP_READ
                            , connection.getOtherSide());

                    connection.setConnectionStatus(ConnectionStatus.PROXY_STATE);
                } else {
                    connection.getOtherSide().setConnectionStatus(ConnectionStatus.CONNECTED);
                    connection.getOtherSide().getOutputByteBuffer().put((received).getBytes());
                    connection.getOtherSide().getChannel().register(key.selector(), SelectionKey.OP_WRITE | SelectionKey.OP_READ
                            , connection.getOtherSide());

                }
                break;
            case PROXY_STATE:
                System.out.println("Fowarding to client: " + received);

                transformer.parseConnection(key, bytes);

        }
        key.selector().wakeup();
    }
}
