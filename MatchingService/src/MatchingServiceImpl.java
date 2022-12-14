import at.favre.lib.crypto.HKDF;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class MatchingServiceImpl extends UnicastRemoteObject implements MatchingService {
    MixingProxy mixer;
    Registrar registrar;
    Practitioner practitioner;
    List<Capsule> capsules = new ArrayList<>();
    private final HKDF hkdf = HKDF.fromHmacSha256();
    //om zelfde waarde als catering te hashen uit Ri en nym
    private final MessageDigest md = MessageDigest.getInstance("SHA-256");
    //critische waarden met key de dag, zodat we ze na x dagen kunnen verwijderen
    Map<Integer, List<Capsule>> criticalCaps = new HashMap<>();
    Map<Integer, List<Token>> criticalTokens = new HashMap<>();
    //UI
    JFrame frame = new JFrame("Matching Service");
    JLabel matchingProcess = new JLabel("Matching Process");
    JTextArea matchingText = new JTextArea();
    JLabel infected = new JLabel("Infected Capsules");
    JList infectedCapsules = new JList();
    DefaultListModel infectedList = new DefaultListModel();

    protected MatchingServiceImpl() throws RemoteException, NoSuchAlgorithmException {
        try {
            setFrame();
            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            myRegistry.bind("Matcher", this);

            registrar = (Registrar) myRegistry.lookup("Registrar");
            registrar.register(this);

            practitioner = (Practitioner) myRegistry.lookup("Practitioner");
            practitioner.register(this);

            Registry registryMixing = LocateRegistry.getRegistry("localhost", 2019,
                    new SslRMIClientSocketFactory());
            mixer = (MixingProxy) registryMixing.lookup("MixingProxy");
            mixer.register(this);

            //timer die oude capsules verwijdert elke dag 24*60*60*1000
            new Timer().scheduleAtFixedRate(new RemoveCapsulesMatcher(this), 0, 10000);
            //timer die overgebleven kritische waarden forward naar registrar na 1 dag
            new Timer().scheduleAtFixedRate(new RemoveCriticalValues(this,true), 0, 10000);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("System is ready...");
    }
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        System.setProperty("javax.net.ssl.trustStore","truststore");
        System.setProperty("javax.net.ssl.trustStorePassword","trustword");
        MatchingServiceImpl matcher = new MatchingServiceImpl();
    }

    @Override
    public void register(MixingProxy mixer) throws RemoteException {
        this.mixer = mixer;
    }

    @Override
    public void sendCapsules(List<Capsule> capsules) throws RemoteException {
        this.capsules.addAll(capsules);
    }

    @Override
    //krijg tuples van practitioner, zoek betreffende caterer op via registrar mbh CF in tuple, hash R uit tuple en nym die bij CF hoort,
    // check of deze hash overeen komt met hash die visitor al had
    public void getTuples(List<Tuple> tuples) throws RemoteException {
        for(Tuple t : tuples){
            if(t != null) {
                Map<String, byte[]> pseudonyms = registrar.downloadPseudonyms(t.getLocation().date.getDayOfYear());
                String tbhash = t.getLocation().random + Arrays.toString(pseudonyms.get(t.getLocation().CF));
                md.update(tbhash.getBytes(StandardCharsets.UTF_8));
                byte[] digest = md.digest();
                matchingText.setText("is " + Arrays.toString(digest) + "\n");
                matchingText.append("Equal to " + Arrays.toString(t.getLocation().hash) + "\n");
                frame.pack();
                if (Arrays.equals(digest, t.getLocation().hash)) {
                    matchingText.append("Correct!");
                    System.out.println("hash klopt, correcte informatie verschaft door visitor");
                    markCapsules(t);
                } else {
                    matchingText.append("Incorrect");
                    System.out.println("hash incorrect!");
                    System.out.println("random tuple: " + t.getLocation().random);
                    System.out.println("nym registrar: " + pseudonyms.get(t.getLocation().CF));
                    System.out.println("CF tuple: " + t.getLocation().CF);
                    System.out.println(digest);
                    System.out.println(t.getLocation().hash);
                }
            }else{
                System.out.println("Valse user dus geen tuples");
            }
            try {
                Thread.sleep(2000);
                matchingText.setText("");

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<Capsule> getCritical() throws RemoteException {
        List<Capsule> critcap = new ArrayList<>();
        criticalCaps.values().forEach(critcap::addAll);
        return critcap;
    }

    @Override
    public void forwardConfirmedToken(Token conftoken) throws RemoteException {
        criticalTokens.values().forEach((list) -> {
            int index = list.indexOf(conftoken);
                if(list.remove(conftoken)){
                    System.out.println("token removed");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    infectedList.removeElement((2*index)+1);
                    infectedList.removeElement(2*index);
                }
            });
    }
    public void markCapsules(Tuple t){
        List<Capsule> newcriticalcaps = new ArrayList<>();
        List<Token> newcriticaltokens = new ArrayList<>();
        for(Capsule c : capsules){
            if(Arrays.equals(c.hash, t.getLocation().hash) && overlap(c.date, t.getLocation().date)){
                infectedList.addElement("Capsule "+ c.toString());
                infectedList.addElement("    Token "+c.token.toString());
                newcriticalcaps.add(c);
                newcriticaltokens.add(c.token);
            }
        }
        infectedCapsules.setModel(infectedList);
        frame.pack();
        if(criticalCaps.get(LocalDateTime.now().getDayOfYear()) != null){
            newcriticalcaps.addAll(criticalCaps.get(LocalDateTime.now().getDayOfYear()));
        }
        if(criticalTokens.get(LocalDateTime.now().getDayOfYear()) != null){
            newcriticaltokens.addAll(criticalTokens.get(LocalDateTime.now().getDayOfYear()));
        }
        criticalCaps.put(LocalDateTime.now().getDayOfYear(), newcriticalcaps);
        criticalTokens.put(LocalDateTime.now().getDayOfYear(), newcriticaltokens);
    }

    public boolean overlap(LocalDateTime d1, LocalDateTime d2){
        long overlap = Math.max(0, Math.min(d1.plusMinutes(30).toEpochSecond(ZoneOffset.MIN),d2.plusMinutes(30).toEpochSecond(ZoneOffset.MIN))-Math.max(d1.toEpochSecond(ZoneOffset.MIN),d2.toEpochSecond(ZoneOffset.MIN))+1);
        System.out.println(overlap);
        return overlap > 0;
    }

    public void setFrame(){
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(400, 500));
        infected.setVisible(true);
        infectedCapsules.setVisible(true);
        matchingProcess.setVisible(true);
        matchingText.setVisible(true);
        matchingText.setEditable(false);
        frame.getContentPane().add(matchingProcess);
        frame.getContentPane().add(matchingText);
        frame.getContentPane().add(infected);
        frame.getContentPane().add(infectedCapsules);
        frame.setLayout(new GridLayout(4,1));
        frame.setSize(400,500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void forwardUninformed(Token t) throws RemoteException {
        System.out.println("forwarding an uninformed token to registrar");
        registrar.forwardUninformed(t);
    }
}
