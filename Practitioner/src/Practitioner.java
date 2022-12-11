import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.List;

public interface Practitioner extends Remote {
    void register(MatchingService matchingService) throws RemoteException;
    void getLogs(List<Location> locationlogs, String visitor) throws SignatureException, InvalidKeyException, RemoteException;
}
