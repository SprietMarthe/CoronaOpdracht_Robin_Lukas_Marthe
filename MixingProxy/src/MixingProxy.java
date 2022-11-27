import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MixingProxy extends Remote {
    void register(Visitor visitor) throws RemoteException;
    public void register(MatchingService matcher) throws RemoteException;
}
