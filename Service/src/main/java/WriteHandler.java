import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class WriteHandler {

    public void handle(SelectionKey key) throws IOException {
        Connection connection = (Connection) key.attachment();
        ByteBuffer outBuf = connection.getOutputByteBuffer();
        SocketChannel socketChannel = (SocketChannel) key.channel();

        outBuf.flip();
        int remain = outBuf.remaining();
        socketChannel.write(outBuf);
        outBuf.compact();

        System.out.println("Write a " + (connection.isServer() ? "Server" : "Client") + " de " + remain);

        if (outBuf.hasRemaining()) {

            key.interestOps(SelectionKey.OP_READ);

            if (connection.getConnectionStatus() == ConnectionStatus.PROXY_STATE) {
                //TODO: Este if no deberia ir, deberia quedar tanto para cliente como para server
                if (connection.isServer() == false) {
                    connection.getOtherSide().getChannel().register(key.selector(), SelectionKey.OP_READ, connection.getOtherSide());
                    //Como el channel ya esta registrado en el selector, lo que va a hacer es setearle las options a la key
                }
            }
        }
    }
}
