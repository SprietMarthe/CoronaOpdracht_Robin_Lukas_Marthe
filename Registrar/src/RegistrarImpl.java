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
    //map met alle geregistreerde caterers met key hun bedrijfsnummer
    private Map<Integer, Catering> caterers;
    //map met alle geregistreerde visitors met key hun telefoonnummer
    private Map<String, Visitor> visitors;
    //key derivation functie om secretkey te genereren
    private final HKDF hkdf = HKDF.fromHmacSha256();
    //hashing functie om pseudoniem te genereren
    private final MessageDigest md = MessageDigest.getInstance("SHA-256");
    private int day = 0;
    //map die visitor aan tokens linkt
    private Map<String, List<Token>> visitortokenmap;

    protected RegistrarImpl() throws RemoteException, NoSuchAlgorithmException {
        caterers = new HashMap<>();
        visitors = new HashMap<>();
        visitortokenmap = new HashMap<>();
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
            //stuur nieuwe tokens naar visitors elk halfuur
            new Timer().scheduleAtFixedRate(new SendToken(this), 0, 30*60*1000);
            //stuur nieuwe key en pseudoniem naar caterers elke dag
            new Timer().scheduleAtFixedRate(new GenKeyAndPseudonym(this), 0, 24*60*60*1000);
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
            System.out.println("2. Print Visitors");
            System.out.println("3: Generate secret keys + pseudonym");
            System.out.println("Enter your choice.");
            int i = sc.nextInt();
            switch (i){
                case 1:
                    registrar.printCaterers();
                    break;
                case 2:
                    registrar.printVisitors();
                    break;
                case 3:
                    registrar.genSecretKeysAndPseudonym();
                    break;
            }
        }
    }

    public void printCaterers() throws RemoteException {
        for(Catering c : caterers.values()){
            System.out.println(c.getName());
        }
    }
    public void printVisitors() throws RemoteException {
        for(Visitor v : visitors.values()){
            System.out.println(v.getName());
        }
    }

    //voor elke caterer een nieuwe secret key en pseudoniem generaten
    public void genSecretKeysAndPseudonym() throws IOException, WriterException {
        for(Catering caterer : caterers.values()){
            String CF = caterer.getCF();
            String location = caterer.getLocation();
            byte[] expandedAesKey = hkdf.expand(masterSecretKey, CF.getBytes(StandardCharsets.UTF_8), 16);
            caterer.setSecretKey(expandedAesKey);

            String data = location + day;
            md.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            caterer.setPseudonym(digest);
        }
    }

    //set voor elke visitor een nieuwe token en voeg deze toe aan de tokenmap
    public void sendTokens() throws RemoteException {
        Random rand = new Random();
        for(Map.Entry<String, Visitor> e : visitors.entrySet()){
            int r = rand.nextInt();
            Token t = new Token(day, r);
            e.getValue().setToken(t);
            visitortokenmap.get(e.getKey()).add(t);
        }
    }

    public void nextDay(){
        day += 1;
    }

    @Override
    public String helloTo(String name) throws RemoteException, NoSuchAlgorithmException {
        System.err.println(name + " is trying to contact!");
        return "Server says hello to " + name;
    }

    @Override
    public void register(Catering caterer) throws RemoteException {
        caterers.put(caterer.getBusinessNumber(),caterer);
        //TODO geef initiele key en pseudoniem door
    }

    @Override
    public void register(Visitor visitor) throws RemoteException {
        visitors.put(visitor.getNumber(), visitor);
        visitortokenmap.put(visitor.getNumber(), new ArrayList<>());
        //TODO geef initiele tokens door
    }
}
