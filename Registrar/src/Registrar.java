import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public interface Registrar extends Remote {
    public String helloTo(String name, int businessNumber, String address) throws RemoteException, NoSuchAlgorithmException;
    public void register(Catering caterer) throws RemoteException;
}
