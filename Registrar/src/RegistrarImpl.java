import at.favre.lib.crypto.HKDF;
import com.google.zxing.WriterException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/*
de registrar maakt één master key aan die het dan telkens gebruikt voor die andere keys per barowner te maken
 */


public class RegistrarImpl extends UnicastRemoteObject implements Registrar {
    private SecretKey masterSecretKey;
    private SecretKey s_CF_Day;
    //map met alle geregistreerde caterers met key hun bedrijfsnummer
    private Map<Integer, Catering> caterers;
    //key derivation functie om secretkey te genereren
    HKDF hkdf = HKDF.fromHmacSha256();
    //hashing functie om pseudoniem te genereren
    MessageDigest md = MessageDigest.getInstance("SHA-256");

    protected RegistrarImpl() throws RemoteException, NoSuchAlgorithmException {
        caterers = new HashMap<>();
    }

    private void startRegistrar() {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("Registrar", this);

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

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, WriterException {
        Scanner sc = new Scanner(System.in);
        RegistrarImpl registrar = new RegistrarImpl();
        registrar.startRegistrar();
        while(true){
            System.out.println("1: Print Caterers");
            System.out.println("2: Generate secret keys + pseudonym");
            System.out.println("Enter your choice.");
            int i = sc.nextInt();
            switch (i){
                case 1:
                    registrar.printCaterers();
                    break;
                case 2:
                    registrar.genSecretKeysAndPseudonym();
            }
        }
    }

    public void printCaterers() throws RemoteException {
        for(Catering c : caterers.values()){
            System.out.println(c.getName());
        }
    }

    //elke dag voor elke caterer een nieuwe secret key generaten //TODO dagelijks oproepen
    public void genSecretKeysAndPseudonym() throws IOException, WriterException {
        for(Catering caterer : caterers.values()){
            String CF = caterer.getCF();
            String location = caterer.getLocation();
            byte[] expandedAesKey = hkdf.expand(masterSecretKey, CF.getBytes(StandardCharsets.UTF_8), 16);
            caterer.setSecretKey(expandedAesKey);

            md.update(location.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            caterer.setPseudonym(digest);
        }
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
    public void register(Catering caterer) throws RemoteException {
        caterers.put(caterer.getBusinessNumber(),caterer);
    }
}
