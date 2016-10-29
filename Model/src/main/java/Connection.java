import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class Connection {

    private SocketChannel channel;
    private Connection otherSide; //TODO: rename variable
    private boolean isServer;
    private ByteBuffer outputByteBuffer;
    private ByteBuffer inputByteBuffer;
    private ConnectionStatus connectionStatus;
    private String username;
    private String password;
    private int interestOps;
    private String request;


    Connection(SocketChannel channel, Connection otherSide, boolean isServer, ByteBuffer inputByteBuffer
            , ByteBuffer outputByteBuffer) {
        this.channel = channel;
        this.otherSide = otherSide;
        this.isServer = isServer;
        this.inputByteBuffer = inputByteBuffer;
        this.outputByteBuffer = outputByteBuffer;
        this.connectionStatus = ConnectionStatus.WAITING_CONNECTION;
        this.interestOps = 0;
        this.request = "";
    }

    Connection(SocketChannel channel, boolean isServer, ByteBuffer inputByteBuffer
            , ByteBuffer outputByteBuffer) {
        this.channel = channel;
        this.isServer = isServer;
        this.outputByteBuffer = outputByteBuffer;
        this.inputByteBuffer = inputByteBuffer;
        this.connectionStatus = ConnectionStatus.WAITING_CONNECTION;
    }

    ByteBuffer getOutputByteBuffer() {
        return outputByteBuffer;
    }

    ByteBuffer getInputByteBuffer() {
        return inputByteBuffer;
    }

    ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    String getUsername() {
        return username;
    }

    void setUsername(String username) {
        this.username = username;
    }

    String getPassword() {
        return password;
    }

    void setPassword(String password) {
        this.password = password;
    }

    public Connection getOtherSide() {
        return otherSide;
    }

    void setOtherSide(Connection otherSide) {
        this.otherSide = otherSide;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    boolean isServer() {
        return isServer;
    }

    public int getInterestOps() {
        return interestOps;
    }

    public void setInterestOps(int interestOps) {
        this.interestOps = interestOps;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

}
