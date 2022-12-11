import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.Timer;

class Capsule implements Serializable {
    Token token;
    byte[] hash;
    LocalDateTime date = LocalDateTime.now();

    Capsule(Token token, byte[] hash){
        this.token = token;
        this.hash = hash;
    }
   @Override
    public String toString() {
        return "Capsule{" +
               "Token=" + token +
                ", hash=" + Arrays.toString(hash) +
                ", date=" + date +
                '}';
    }
}

public class MixingProxyImpl extends UnicastRemoteObject implements MixingProxy{
    private MatchingService matcher;
    private Map<String, Visitor> visitors;
    private List<Capsule> queueCapsules;
    public List<Token> spent;
    private Registrar registrar;
    //signature om hashes te signen
    private final Signature ecdsaSignature = Signature.getInstance("SHA256withRSA");
    private final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    private KeyPair pair;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    JFrame frame = new JFrame("Mixing Proxy");
    JLabel capsulesLabel = new JLabel("Capsules: ");
    JList capsulesList = new JList();
    JButton flushQueue = new JButton("flush queue");
    DefaultListModel capsules = new DefaultListModel();
    protected MixingProxyImpl() throws RemoteException, NoSuchAlgorithmException {
        visitors = new HashMap<>();
        queueCapsules = new ArrayList<>();
        spent = new ArrayList<>();
        this.keyPairGenerator.initialize(1024);
        this.pair = this.keyPairGenerator.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    private void startMixingProxy(){
        try {
            setFrame();
            // Create SSL-based registry
            Registry registry = LocateRegistry.createRegistry(2019,
                    new SslRMIClientSocketFactory(),
                    new SslRMIServerSocketFactory());
            registry.bind("MixingProxy", this);

            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registrar = (Registrar) myRegistry.lookup("Registrar");

            //timer die queue flushed naar matcher elke dag
            new Timer().scheduleAtFixedRate(new FlushQueue(this), 60*1000, 24*60*60*1000);
            //timer die spent tokens verwijdert na 1 dag (minimum)
            new Timer().scheduleAtFixedRate(new RemoveSpent(this), 0, 24*60*60*1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("System is ready...");
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        //settings voor ssl connection
        System.setProperty("javax.net.ssl.keyStore","keystore");
        System.setProperty("javax.net.ssl.keyStorePassword","password");
        MixingProxyImpl mixingProxy = new MixingProxyImpl();
        mixingProxy.startMixingProxy();
        //printMenu(mixingProxy);
    }

    public void setFrame(){
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        capsulesLabel.setVisible(true);
        capsulesList.setVisible(true);
        flushQueue.setVisible(true);
        frame.getContentPane().add(capsulesLabel);
        frame.getContentPane().add(capsulesList);
        frame.getContentPane().add(flushQueue);
        frame.setLayout(new GridLayout(5,2));
        frame.setSize(500,200);
        frame.setVisible(true);

        flushQueue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    flushQueue();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }
    public void flushQueue() throws RemoteException {
        if (!queueCapsules.isEmpty()) {
            Collections.shuffle(queueCapsules);
            matcher.sendCapsules(queueCapsules);
            queueCapsules.clear();
            capsules.clear();
            capsulesList.clearSelection();
        }
    }

    private void printQueue() {
        System.out.println(queueCapsules);
    }

    @Override
    public void register(Visitor visitor)throws RemoteException {
        visitors.put(visitor.getNumber(), visitor);
    }

    @Override
    public void register(MatchingService matcher) throws RemoteException {
        this.matcher = matcher;
    }

    @Override
    public byte[] sendCapsule(Capsule c) throws RemoteException, SignatureException, InvalidKeyException {
        //check incoming capsule en voeg toe aan queue als goedgekeurd
        if(checkCapsule(c)){
            queueCapsules.add(c);
            capsules.addElement("Capsule "+c.toString());
            capsules.addElement("    Token "+c.token.toString());
            capsulesList.setModel(capsules);
            frame.pack();
            spent.add(c.token);
            return signHash(c.hash);
        }
        return new byte[0];
    }

    @Override
    public void forwardConfirmedToken(Token token) throws RemoteException {
        matcher.forwardConfirmedToken(token);
    }

    private boolean checkCapsule(Capsule c) throws RemoteException, SignatureException, InvalidKeyException {
        boolean good = true;

        //check met registrar of token valid is adhv signature
        if(!registrar.checkTokenValidity(c.token)){
            good = false;
        }
        // check if token is a token for that particular day
        else if(LocalDateTime.now().getDayOfYear() != c.token.getDay()){
            System.out.println(LocalDateTime.now().getDayOfYear());
            System.out.println(c.token.getDay());
            good = false;
        }
        else if(spent.contains(c.token)){
            good = false;
        }
        System.out.println("good: " + good);
        return good;
    }

    private byte[] signHash(byte[] hash) throws InvalidKeyException, SignatureException {
        ecdsaSignature.initSign(privateKey);
        ecdsaSignature.update(hash);
        return ecdsaSignature.sign();
    }

}
