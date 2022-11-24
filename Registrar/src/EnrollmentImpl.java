import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;

public class EnrollmentImpl extends UnicastRemoteObject implements Enrollment{
    private SecretKey s_CF_Day;


    protected EnrollmentImpl() throws RemoteException {

    }

    @Override
    public String helloTo(String name, int businessNumber, String address) throws RemoteException, NoSuchAlgorithmException {
        System.err.println(name + " is trying to contact!");
        s_CF_Day = generateBarOwnerKey(name, businessNumber, address);
        return "Server says hello to " + name;
    }

    private SecretKey generateBarOwnerKey(String name, int businessNumber, String address) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    @Override
    public void getSecretKey() throws RemoteException {

    }

    @Override
    public void getPseudonym() throws RemoteException {

    }
}
