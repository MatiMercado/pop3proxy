import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ProxyConfigLoader {

    private ProxyConfigXML proxyConfigXML;

    public boolean loadProxyConfig() {

        try {
            JAXBContext context = JAXBContext.newInstance(ProxyConfigXML.class);
            proxyConfigXML = (ProxyConfigXML) context.createUnmarshaller().unmarshal(new File("proxyServerConfig.xml"));
            if (proxyConfigXML.getDefaultServer() == null) {
                System.out.println("Configuration error: Default server is missing.");
                return false;
            }
        } catch (JAXBException e) {
            System.out.println("Configuration error: ");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Map<String, User> getUsersMap() {
        Map<String, User> usersMap = new HashMap<String, User>();
        if (proxyConfigXML.getUsers() == null) {
            return null;
        }
        for (ProxyConfigXML.User user : proxyConfigXML.getUsers()) {
            if (user.getUsername() != null && user.getServer() != null && user.getPort() != null) {
                User newUser = new User(user.getUsername(), user.getServer(), user.getPort());
                usersMap.put(newUser.getUsername(), newUser);
            }
        }
        return usersMap;
    }

    public String getDefaultServer() {
        return this.proxyConfigXML.getDefaultServer();
    }

    public Integer getDefaultPort() {
        return this.proxyConfigXML.getDefaultPort();
    }

}
