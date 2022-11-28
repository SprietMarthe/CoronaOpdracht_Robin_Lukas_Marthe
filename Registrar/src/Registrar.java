import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public interface Registrar extends Remote {
    public String helloTo(String name) throws RemoteException, NoSuchAlgorithmException;
    public void register(Catering caterer) throws RemoteException;
    public void register(Visitor visitor) throws RemoteException;
    public void register(MatchingService matcher) throws RemoteException;
}
