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
import java.security.*;
import java.time.LocalDateTime;
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
    private int day = LocalDateTime.now().getDayOfYear()-1;
    //map die visitor aan tokens linkt
    private Map<String, List<Token>> visitortokenmap;
    private MatchingService matcher;
    //signature om tokens te signen
    private final Signature ecdsaSignature = Signature.getInstance("SHA256withRSA");
    private final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    private KeyPair pair;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    protected RegistrarImpl() throws RemoteException, NoSuchAlgorithmException {
        caterers = new HashMap<>();
        visitors = new HashMap<>();
        visitortokenmap = new HashMap<>();
        this.keyPairGenerator.initialize(1024);
        this.pair = this.keyPairGenerator.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
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
            //stuur nieuwe tokens naar visitors elke dag
            new Timer().scheduleAtFixedRate(new SendToken(this), 0, 24*60*60*1000);
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

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, WriterException, SignatureException, InvalidKeyException {
        Scanner sc = new Scanner(System.in);
        RegistrarImpl registrar = new RegistrarImpl();
        registrar.startRegistrar();
        while(true){
            System.out.println("1: Print Caterers");
            System.out.println("2: Print Visitors");
            System.out.println("3: Generate secret keys + pseudonym");
            System.out.println("4: Generate tokens");
            System.out.println("5: Skip to next day");
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
                case 4:
                    registrar.sendTokens();
                    break;
                case 5:
                    registrar.nextDay();
                    break;
            }
        }
    }

    public void printCaterers() throws RemoteException {
        if(!caterers.isEmpty()){
            for(Catering c : caterers.values()){
                System.out.println(c.getName());
            }
        }else {
            System.out.println("No caterers");
        }
    }
    public void printVisitors() throws RemoteException {

        if(!visitors.isEmpty()){
            for(Visitor v : visitors.values()){
                System.out.println(v.getName());
            }
        }else {
            System.out.println("No visitors");
        }
    }

    //voor elke caterer een nieuwe secret key en pseudoniem generaten
    public void genSecretKeysAndPseudonym() throws IOException, WriterException {
        for(Catering caterer : caterers.values()){
            genSecretKeyAndPseudonym(caterer);
        }
    }

    public void genSecretKeyAndPseudonym(Catering caterer) throws IOException, WriterException {
        String CF = caterer.getCF();
        String location = caterer.getLocation();
        byte[] expandedAesKey = hkdf.expand(masterSecretKey, CF.getBytes(StandardCharsets.UTF_8), 16);
        caterer.setSecretKey(expandedAesKey);

        String data = location + day;
        md.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        caterer.setPseudonym(digest);
    }

    //set voor elke visitor een nieuwe token en voeg deze toe aan de tokenmap
    public void sendTokens() throws RemoteException, SignatureException, InvalidKeyException {
        Random rand = new Random();
        for(Map.Entry<String, Visitor> e : visitors.entrySet()){
            List<Token> tokens = new ArrayList<>();
            for(int i = 0; i < 48; i++){
                int r = rand.nextInt();

                ecdsaSignature.initSign(privateKey);
                ecdsaSignature.update((byte) r);
                byte[] signature = ecdsaSignature.sign();

                Token t = new Token(day, r, signature);
                tokens.add(t);
                visitortokenmap.get(e.getKey()).add(t);
            }
            e.getValue().setTokens(tokens);
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
    public void register(Catering caterer) throws RemoteException, IOException, WriterException {
        caterers.put(caterer.getBusinessNumber(),caterer);
        genSecretKeyAndPseudonym(caterer);
    }

    @Override
    public void register(Visitor visitor) throws RemoteException, SignatureException, InvalidKeyException {
        visitors.put(visitor.getNumber(), visitor);
        visitortokenmap.put(visitor.getNumber(), new ArrayList<>());
        sendTokenToNewVisitor(visitor);
    }

    public void sendTokenToNewVisitor(Visitor visitor) throws RemoteException, InvalidKeyException, SignatureException {
        Random rand = new Random();
        List<Token> tokens = new ArrayList<>();
        for(int i = 0; i < 48; i++){
            int r = rand.nextInt();

            ecdsaSignature.initSign(privateKey);
            ecdsaSignature.update((byte) r);
            byte[] signature = ecdsaSignature.sign();

            Token t = new Token(day, r, signature);
            tokens.add(t);
            visitortokenmap.get(visitor.getNumber()).add(t);
        }
        visitor.setTokens(tokens);
    }

    @Override
    public void register(MatchingService matcher) throws RemoteException {
        this.matcher = matcher;
    }

    @Override
    public boolean checkTokenValidity(Token token) throws RemoteException, InvalidKeyException, SignatureException {
        ecdsaSignature.initVerify(publicKey);
        ecdsaSignature.update((byte) token.getRandom());
        return ecdsaSignature.verify(token.getSignature());
    }

    @Override
    public int getDay() throws RemoteException {
        return day;
    }
}
