import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.SignatureException;

public interface MixingProxy extends Remote {
    void register(Visitor visitor) throws RemoteException;
    void register(MatchingService matcher) throws RemoteException;

    void sendCapsule(Capsule c) throws RemoteException, SignatureException, InvalidKeyException;
}
