import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Visitor extends Remote {
    public String getNumber() throws RemoteException;
    public String getName() throws RemoteException;
    public void setToken(Token t) throws RemoteException;
    public void setSignedHash(byte[] signedHash) throws RemoteException;
}
