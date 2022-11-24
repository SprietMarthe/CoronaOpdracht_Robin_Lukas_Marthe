import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Catering extends Remote {
    public int getBusinessNumber() throws RemoteException;
    public String getName() throws RemoteException;
}
