import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentImpl extends UnicastRemoteObject implements Enrollment{
    private SecretKey s_CF_Day;
    List<Catering> barowners = new ArrayList<>();


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

    @Override
    public void register(Catering barowner) throws RemoteException{
        barowners.add(barowner);
        barowners.forEach(b-> {
            try {
                b.printName();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }
}
