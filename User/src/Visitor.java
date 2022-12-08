import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Visitor extends Remote {
    String getNumber() throws RemoteException;
    String getName() throws RemoteException;
    void setTokens(List<Token> t) throws RemoteException;
    void setSignedHash(byte[] signedHash) throws IOException;

}
