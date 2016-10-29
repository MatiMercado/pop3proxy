import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;


public class Authenticator {

    private static final String NOT_ACCEPTED = "-ERR Proxy not handling codified authentication";
    private static final int BUFSIZE = 256;

    Map<String, User> users;
    int defaultPort;
    String defaultIP;

    public Authenticator(Map<String, User> users, String defaultIP, int defaultPort) {
        this.users = users;
        this.defaultPort = defaultPort;
        this.defaultIP = defaultIP;
    }

    public boolean authenticatePlain(Connection connection, SelectionKey key) throws IOException {

        SocketChannel serverChannel = SocketChannel.open();
        serverChannel.configureBlocking(false);

        User user = users.get(connection.getUsername());
        String host = defaultIP;
        if (user != null) {
            host = user.getServer();
        }


        serverChannel.connect(new InetSocketAddress(host, defaultPort));
        Connection serverConnection = new Connection(serverChannel, connection, true, ByteBuffer.allocate(BUFSIZE),
                ByteBuffer.allocate(BUFSIZE));
        serverConnection.setUsername(connection.getUsername());
        serverConnection.setPassword(connection.getPassword());
        connection.setOtherSide(serverConnection);


        System.out.println("Antes del register: Host " + host + " and port " + defaultPort);
        connection.setRequest("REGISTER");

        return false;
    }
}
