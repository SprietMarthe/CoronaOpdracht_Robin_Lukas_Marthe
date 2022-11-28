import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MixingProxy extends Remote {
    void register(Visitor visitor) throws RemoteException;
    void register(MatchingService matcher) throws RemoteException;

    void sendCapsule(Capsule c) throws RemoteException;
}
