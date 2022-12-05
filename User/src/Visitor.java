import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Visitor extends Remote {
    public String getNumber() throws RemoteException;
    public String getName() throws RemoteException;
    public void setTokens(List<Token> t) throws RemoteException;
    public void setSignedHash(byte[] signedHash) throws RemoteException;
}
