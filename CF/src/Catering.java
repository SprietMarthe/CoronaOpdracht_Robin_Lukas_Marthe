import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Catering extends Remote {
    public void printName() throws RemoteException;
}
