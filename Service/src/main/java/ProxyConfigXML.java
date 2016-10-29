import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "proxyServerConfig")
public class ProxyConfigXML {

    @XmlElement(name = "defaultServer")
    private String defaultServer;

    @XmlElement(name = "defaultPort")
    private Integer defaultPort;


    @XmlElement(name = "user")
    private List<ProxyConfigXML.User> users;

    public String getDefaultServer() {
        return defaultServer;
    }

    public void setDefaultServer(String defaultServer) {
        this.defaultServer = defaultServer;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Integer getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(Integer defaultPort) {
        this.defaultPort = defaultPort;
    }

    @XmlRootElement(name = "user")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class User {
        @XmlElement
        private String username;
        @XmlElement
        private String server;
        @XmlElement
        private Integer port;

        public User() {
        }

        public User(String username, String server, Integer port) {
            this.username = username;
            this.server = server;
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }
    }

}



