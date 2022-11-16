import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Enrollment extends Remote {

    public String helloTo(String name) throws RemoteException;
    public void getSecretKey()throws RemoteException;
    public void getPseudonym()throws RemoteException;
}
