import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Practitioner extends Remote {
    void register(MatchingService matchingService) throws RemoteException;
}
