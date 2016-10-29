import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;


public class ReadHandler {


    public void handle(SelectionKey key, LinkedBlockingQueue<SelectionKey> inQueue) throws IOException {
        SocketChannel socketChan = (SocketChannel) key.channel();
        Connection connection = (Connection) key.attachment();

        long read;
        ByteBuffer inBuffer = connection.getInputByteBuffer();

        read = socketChan.read(inBuffer);

        if (read == -1) {
            key.interestOps(0);
            socketChan.close();
        }

        System.out.println("READ: " + read + " bytes, " + inBuffer.toString() + "from " + (connection.isServer() ? "Server" : "Client"));

        if (read > 0) {

            try {
                connection.setInterestOps(key.interestOps()); //TODO: Set this flag when the worker thread finishes instead
                key.interestOps(0);
                inQueue.put(key);
                System.out.println("ENCOLADO");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}