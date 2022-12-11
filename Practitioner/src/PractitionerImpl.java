import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

class Tuple implements Serializable {
    Location l;
    byte[] pracSignature;

    Tuple(Location l, byte[] pracsig){
        this.l = l;
        this.pracSignature = pracsig;
    }
    Location getLocation(){
        return l;
    }
}

public class PractitionerImpl extends UnicastRemoteObject implements Practitioner {
    MatchingService matcher;
    private final Signature ecdsaSignature = Signature.getInstance("SHA256withRSA");
    private final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    private KeyPair pair;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    JFrame frame = new JFrame("Practicioner");
    JLabel textLabel = new JLabel("Logs van visitors");
    JTextArea logArea = new JTextArea();

    protected PractitionerImpl() throws RemoteException, NoSuchAlgorithmException {
        this.keyPairGenerator.initialize(1024);
        this.pair = this.keyPairGenerator.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
        setFrame();
        try {
            // fire to localhost port 1099
            Registry registry = LocateRegistry.getRegistry(1099);
            registry.bind("Practitioner", this);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("System is ready...");
    }



    public static void main(String[] args) throws IOException, RemoteException, NoSuchAlgorithmException {
        PractitionerImpl practitioner = new PractitionerImpl();
    }

    private void setFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(300, 400));
        frame.setLayout(new BorderLayout());

        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setOpaque(false);
        logArea.setEditable(false);
        frame.getContentPane().add(textLabel, BorderLayout.PAGE_START);
        frame.add(logArea);
        frame.setSize(400,400);
        frame.pack();
        frame.setLocationRelativeTo(null); // center
        frame.setVisible(true);
        frame.pack();
    }

    private byte[] signLocation(byte[] hash) throws InvalidKeyException, SignatureException {
        ecdsaSignature.initSign(privateKey);
        ecdsaSignature.update(hash);
        return ecdsaSignature.sign();
    }

    @Override
    public void register(MatchingService matchingService) throws RemoteException{
        this.matcher = matchingService;
    }

    //maak tuples met log data en signature van practitioner voor matching server
    @Override
    public void getLogs(List<Location> locationlogs, String name) throws SignatureException, InvalidKeyException, RemoteException {
        List<Tuple> tuples = new ArrayList<>();
        for (Location l : locationlogs) {
            tuples.add(new Tuple(l,signLocation(l.hash)));
        }
        // TODO shuffeling with tupples from other users
        StringBuilder logs = new StringBuilder("Visitor " + name + ":\n");
        for (Tuple t: tuples) {
            logs.append(t.l).append("\n");
        }
        System.out.println(String.valueOf(logs));
        logArea.setText(String.valueOf(logs));
        matcher.getTuples(tuples);
    }
}
