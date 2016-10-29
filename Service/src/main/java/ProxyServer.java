import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ProxyServer {
    private static final int TIMEOUT = 3000;

    private WriteHandler writeHandler = new WriteHandler();
    private ReadHandler readHandler = new ReadHandler();

    private SubjectToL33t transformer;

    private Authenticator authenticator;
    private String defaultIP;
    private int defaultPort;
    private int poolSize;
    private AcceptHandler acceptHandler;
    private ConnectHandler connectHandler;
    private ConfigServer configServer;
    private Selector selector;
    private LinkedBlockingQueue<SelectionKey> inQueue;
    private LinkedBlockingQueue<SelectionKey> outQueue;
    private ExecutorService executorService;
    private ServerSocketChannel clientChannel;
    private ServerSocketChannel configChannel;

    public ProxyServer(int clientPort, int defaultPort, String defaultIP, int configPort, int poolSize, Map<String, User> users) throws IOException {
        this.defaultIP = defaultIP;
        this.defaultPort = defaultPort;

        this.transformer = new SubjectToL33t();

        this.inQueue = new LinkedBlockingQueue<SelectionKey>();
        this.outQueue = new LinkedBlockingQueue<SelectionKey>();

        this.acceptHandler = new AcceptHandler();
        this.connectHandler = new ConnectHandler();
        this.authenticator = new Authenticator(users, defaultIP, defaultPort);
        this.configServer = new ConfigServer();

        this.selector = Selector.open();
        this.executorService = Executors.newFixedThreadPool(poolSize);
        this.poolSize = poolSize;

        //Open clienside channel
        this.clientChannel = ServerSocketChannel.open();
        clientChannel.socket().bind(new InetSocketAddress(clientPort));
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_ACCEPT);

        //Open config channel
        this.configChannel = ServerSocketChannel.open();
        configChannel.socket().bind(new InetSocketAddress(configPort));
        configChannel.configureBlocking(false);
        configChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void run() throws IOException {


        for (int i = 0; i < poolSize; i++) {
            Runnable worker = new WorkerThread(inQueue, outQueue, authenticator, transformer, configServer);
            executorService.execute(worker);
        }


        while (true) {

            if (selector.select(TIMEOUT) == 0) {
                while (!outQueue.isEmpty()) {
                    SelectionKey key = outQueue.poll();
                    if (key.isValid()) {
                        Connection connection = (Connection) key.attachment();
                        if (connection != null && connection.getRequest() == "REGISTER") {
                            Connection otherSide = connection.getOtherSide();
                            otherSide.getChannel().register(key.selector(), SelectionKey.OP_CONNECT, otherSide);
                            connection.setRequest("");
                            System.out.println("Estoy registrando");
                            //NOTA: Por ahora la outQueue no se usa. Los workers modifican directamente las keys.
                            //key.interestOps(connection.getInterestOps()); //Preguuntar si es valida, me tiro un CancelledKeyException
                        }
                    } else {
                        System.out.println("DEBUG: En el while del proxy: Invalid Key");
                    }
                }
                continue;
            }

            Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
            while (keyIter.hasNext()) {
                SelectionKey key = keyIter.next();

                if (key.isAcceptable()) {
                    acceptHandler.handle(key);
                }

                if (key.isConnectable()) {
                    connectHandler.handle(key);
                }

                if (key.isReadable()) {
                    readHandler.handle(key, inQueue);
                }

                if (key.isValid() && key.isWritable()) {
                    writeHandler.handle(key);
                }

                keyIter.remove();
            }
        }
    }
}