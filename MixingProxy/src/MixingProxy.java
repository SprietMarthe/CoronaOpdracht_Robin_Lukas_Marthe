import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MixingProxy extends Remote {
    String sayHello() throws RemoteException;

    void register(Visitor visitor) throws RemoteException;
}
