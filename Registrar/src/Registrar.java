import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;

/*
de registrar maakt één master key aan die het dan telkens gebruikt voor die andere keys per barowner te maken
 */


public class Registrar {
    private SecretKey masterSecretKey;

    private void startRegistrar() {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("Enrollment", new EnrollmentImpl());

            try {
                masterSecretKey = generateMasterKey(256);
                System.out.println("masterSecretKey: " + masterSecretKey);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("system is ready");
    }

    private static SecretKey generateMasterKey(int n) throws NoSuchAlgorithmException {
        //AES key with the size of n (128, 192, and 256) bits
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        return keyGenerator.generateKey();
    }

    public static void main(String[] args) {
        Registrar registrar = new Registrar();
        registrar.startRegistrar();
    }
}
