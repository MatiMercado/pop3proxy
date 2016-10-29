import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AcceptHandler {

    private static final int BUFSIZE = 256;
    private static final String HANDSHAKE_BANNER = "+OK proxy running, authorization required\n";

    public void handle(SelectionKey key) throws IOException {
        SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
        clientChannel.configureBlocking(false);

        ByteBuffer outBuffer = ByteBuffer.allocate(BUFSIZE);
        outBuffer.put(HANDSHAKE_BANNER.getBytes());
        Connection clientConnection = new Connection(clientChannel, false, ByteBuffer.allocate(BUFSIZE), outBuffer);
        clientConnection.setConnectionStatus(ConnectionStatus.CONNECTED);

        clientChannel.register(key.selector(), SelectionKey.OP_WRITE | SelectionKey.OP_READ, clientConnection);
    }
}
