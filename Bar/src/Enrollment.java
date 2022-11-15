import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Enrollment extends Remote {
    public void getSecretKey()throws RemoteException;
    public void getPseudonym()throws RemoteException;
}