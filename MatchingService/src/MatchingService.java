import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MatchingService extends Remote {
    public void register(MixingProxy mixer) throws RemoteException;
}
