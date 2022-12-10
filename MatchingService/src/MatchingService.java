import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MatchingService extends Remote {
    void register(MixingProxy mixer) throws RemoteException;
    void sendCapsules(List<Capsule> capsules) throws RemoteException;
    void getTuples(List<Tuple> tuples) throws RemoteException;
}
