import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.util.concurrent.LinkedBlockingQueue;

public class WorkerThread implements Runnable {

    private LinkedBlockingQueue<SelectionKey> outQueue;
    private LinkedBlockingQueue<SelectionKey> inQueue;
    private Authenticator authenticator;
    private SubjectToL33t transformer;
    private ConfigServer configServer;

    public WorkerThread(LinkedBlockingQueue<SelectionKey> inQueue, LinkedBlockingQueue<SelectionKey> outQueue,
                        Authenticator authenticator, SubjectToL33t transformer, ConfigServer configServer) {
        this.outQueue = outQueue;
        this.inQueue = inQueue;
        this.authenticator = authenticator;
        this.transformer = transformer;
        this.configServer = configServer;
        System.out.println("Despierto un worker");
    }

    public void run() {

        while (true) {
            System.out.println("Hay un poll, bloqueante espero");
            SelectionKey key = null;

            try {
                key = inQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Desbloqueado: " + Thread.currentThread().getName());


            Connection connection = (Connection) key.attachment();

            try {
                if (connection.isServer()) {
                    ServerInputParser.parseInput(connection, key, transformer);
                } else {
                    ClientInputParser.parseInput(connection, authenticator, key, transformer);
                }
            } catch (ClosedChannelException e) {
                System.out.println("TODO: Elegantly handle closed channel =/");
            } catch (Exception e) {
                System.out.println("WHAT? ");
                e.printStackTrace();
            }
            outQueue.offer(key);
        }
    }
}
