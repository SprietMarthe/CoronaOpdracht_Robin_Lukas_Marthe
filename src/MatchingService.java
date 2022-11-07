import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MatchingService {

    void requestInfectedLogs() throws RemoteException;
    void unacknowledgedLogs() throws RemoteException;
    void acknowledge() throws RemoteException;
    void submitAcks() throws RemoteException;

}
