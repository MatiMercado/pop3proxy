import java.io.IOException;
import java.util.Map;

//TODO: Rename class
public class Main {

    private static int PROXY_LISTENING_PORT = 9090;
    private static int CONFIG_SERVER_PORT = 9091;
    private static int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {

        ProxyConfigLoader loader = new ProxyConfigLoader();
        loader.loadProxyConfig();
        Map<String, User> users = loader.getUsersMap();
        String defaultServer = loader.getDefaultServer();
        Integer defaultPort = loader.getDefaultPort();

        try {
            ProxyServer proxyServer = new ProxyServer(PROXY_LISTENING_PORT, defaultPort, defaultServer, CONFIG_SERVER_PORT,
                    THREAD_POOL_SIZE, users);
            proxyServer.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
