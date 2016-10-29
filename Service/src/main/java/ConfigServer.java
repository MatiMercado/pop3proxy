import java.nio.channels.SelectionKey;

public class ConfigServer {

    //El handleAccept:
    // - Hay que preguntar en el proxy o en el AcceptHandler si channel.equals(configChannel)
    //      2 Opciones:
    //      - Crear una clase nueva: ConfigConnection
    //      - Agregarle a la clase connection un boolean o un String o un Enum con el tipo.
    //

    public void handleAccept(SelectionKey key) {

    }

    //Si usamos el handler del proxy: Hay que ver si sirve que le setee el estado waiting for banner, eso deberia ser
    //manejado en una maquina de estados del metodo parseInput de esta clase.
    public void handleConnect(SelectionKey key) {

    }


    //Si usamos los mismos handlers que para el proxy: Hay que usar si o si connection o extenderla

    //Al final del write: connection.isServer() == false, hay que fijarse que cuando modificas la key, no estes modificando
    //la key del configChannel
    public void handleWrite(SelectionKey key) {

    }

    public void handleRead(SelectionKey key) {

    }

    //Hay que agregarle esto al WorkerThread, preguntar si la connection es para config llamar a este metodo
    public void parseInput() {

    }


}
