import java.io.IOException;
import java.nio.channels.SelectionKey;

public class ConnectHandler {

    public void handle(SelectionKey key) throws IOException {

        Connection connection = (Connection) key.attachment();
        connection.setConnectionStatus(ConnectionStatus.WAITING_FOR_BANNER);
        if (connection.getChannel().finishConnect())
            key.interestOps(SelectionKey.OP_READ);
    }
}
