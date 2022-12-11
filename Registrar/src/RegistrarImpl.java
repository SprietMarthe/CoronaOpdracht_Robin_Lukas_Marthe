import at.favre.lib.crypto.HKDF;
import com.google.zxing.WriterException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.Timer;

/*
de registrar maakt één master key aan die het dan telkens gebruikt voor die andere keys per barowner te maken
 */


public class RegistrarImpl extends UnicastRemoteObject implements Registrar {
    private SecretKey masterSecretKey;
    //map met alle geregistreerde caterers met key hun CF
    private Map<String, Catering> caterers;
    //map met alle geregistreerde visitors met key hun telefoonnummer
    private Map<String, Visitor> visitors;
    //map met pseudoniemen met CF als key in map met dag als key
    private Map<Integer, Map<String, byte[]>> pseudonyms;
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
    JFrame frame = new JFrame("Registrar");
    JLabel catererLabel = new JLabel("Caterers: ");
    JLabel visitorLabel = new JLabel("Visitors");
    TextArea dayText = new TextArea();
    JList catererList = new JList<String>();
    JList visitorList = new JList<String>();
    JButton genKeys = new JButton("generate secret keys + pseudonym");
    JButton genTokens = new JButton("generate tokens");
    DefaultListModel defaultCatererList = new DefaultListModel();
    DefaultListModel defaultVisitorList = new DefaultListModel();

    protected RegistrarImpl() throws RemoteException, NoSuchAlgorithmException {
        setFrame();
        caterers = new HashMap<>();
        visitors = new HashMap<>();
        visitortokenmap = new HashMap<>();
        pseudonyms = new HashMap<>();
        this.keyPairGenerator.initialize(1024);
        this.pair = this.keyPairGenerator.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, WriterException, SignatureException, InvalidKeyException {
        Scanner sc = new Scanner(System.in);
        RegistrarImpl registrar = new RegistrarImpl();
        registrar.startRegistrar();
    }

    private void startRegistrar() {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("Registrar", this);

            try {
                masterSecretKey = generateMasterKey(256);
//                System.out.println("masterSecretKey: " + masterSecretKey);
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
        System.out.println("System is ready...");
    }

    private static SecretKey generateMasterKey(int n) throws NoSuchAlgorithmException {
        //AES key with the size of n (128, 192, and 256) bits
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        return keyGenerator.generateKey();
    }


    //voor elke caterer een nieuwe secret key en pseudoniem generaten
    public void genSecretKeysAndPseudonym() throws IOException, WriterException {
        for(Catering caterer : caterers.values()){
            genSecretKeyAndPseudonym(caterer);
        }
    }

    public void genSecretKeyAndPseudonym(Catering caterer) throws IOException, WriterException {
        String CF = caterer.getCF();
        byte[] expandedAesKey = hkdf.expand(masterSecretKey, CF.getBytes(StandardCharsets.UTF_8), 16);
        caterer.setSecretKey(expandedAesKey);

        String data = CF + day;
        md.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        caterer.setPseudonym(digest);

        Map<String, byte[]> catererpseudonymmap = new HashMap<>();
        if(pseudonyms.get(day) != null){
            catererpseudonymmap = pseudonyms.get(day);
        }
        catererpseudonymmap.put(CF,digest);
        //System.out.println(CF + " " + Base64.getEncoder().encodeToString(digest));
        //System.out.println(digest);
        //System.out.println(Base64.getDecoder().decode(Base64.getEncoder().encodeToString(digest)));
        pseudonyms.put(day,  catererpseudonymmap);
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

    private void setFrame(){
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        visitorList.setVisible(true);
        catererList.setVisible(true);
        catererLabel.setVisible(true);
        visitorLabel.setVisible(true);
        dayText.setEditable(false);
        frame.getContentPane().add(catererLabel);
        frame.getContentPane().add(catererList);
        frame.getContentPane().add(visitorLabel);
        frame.getContentPane().add(visitorList);
        frame.getContentPane().add(genKeys);
        frame.getContentPane().add(genTokens);
        frame.getContentPane().add(dayText);

        frame.setLayout(new GridLayout(4,2));
        frame.setSize(700,250);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        dayText.setText(Integer.toString(day));
        genKeys.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    genSecretKeysAndPseudonym();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        genTokens.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sendTokens();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }


    @Override
    public String helloTo(String name) throws RemoteException, NoSuchAlgorithmException {
        System.err.println(name + " is trying to contact!");
        return "Server says hello to " + name;
    }

    @Override
    public void register(Catering caterer) throws RemoteException, IOException, WriterException {
        caterers.put(caterer.getCF(),caterer);
        genSecretKeyAndPseudonym(caterer);
        defaultCatererList.addElement(caterer.getName());
        catererList.setModel(defaultCatererList);
    }

    @Override
    public void register(Visitor visitor) throws RemoteException, SignatureException, InvalidKeyException {
        visitors.put(visitor.getNumber(), visitor);
        visitortokenmap.put(visitor.getNumber(), new ArrayList<>());
        sendTokenToNewVisitor(visitor);
        defaultVisitorList.addElement(visitor.getName());
        visitorList.setModel(defaultVisitorList);
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
    public Map<String, byte[]> downloadPseudonyms(int date) {
        return pseudonyms.get(date);
    }

    public void nextDay(){
        day += 1;
    }
}

