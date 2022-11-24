import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public interface Enrollment extends Remote {

    public String helloTo(String name, int businessNumber, String address) throws RemoteException, NoSuchAlgorithmException;
    public void getSecretKey()throws RemoteException;
    public void getPseudonym()throws RemoteException;
    public void register(Catering barowner) throws RemoteException;
}
