import com.google.zxing.WriterException;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public interface Registrar extends Remote {
    public String helloTo(String name) throws RemoteException, NoSuchAlgorithmException;
    public void register(Catering caterer) throws IOException, WriterException;
    public void register(Visitor visitor) throws RemoteException, SignatureException, InvalidKeyException;
    public void register(MatchingService matcher) throws RemoteException;
    public boolean checkTokenValidity(Token token) throws RemoteException, InvalidKeyException, SignatureException;
}
