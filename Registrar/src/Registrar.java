import com.google.zxing.WriterException;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Map;

public interface Registrar extends Remote {
    String helloTo(String name) throws RemoteException, NoSuchAlgorithmException;
    void register(Catering caterer) throws IOException, WriterException;
    void register(Visitor visitor) throws RemoteException, SignatureException, InvalidKeyException;
    void register(MatchingService matcher) throws RemoteException;
    boolean checkTokenValidity(Token token) throws RemoteException, InvalidKeyException, SignatureException;
    Map<String, byte[]> downloadPseudonyms(int date) throws RemoteException;

    void leaveLocation(String visitor) throws RemoteException;
}
