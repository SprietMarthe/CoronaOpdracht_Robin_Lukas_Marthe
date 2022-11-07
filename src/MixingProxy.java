import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MixingProxy extends Remote {

    void registerVisit() throws RemoteException;
    void submitCapsules() throws RemoteException;
    void acknowledge() throws RemoteException;
    void submitAcks() throws RemoteException;

}