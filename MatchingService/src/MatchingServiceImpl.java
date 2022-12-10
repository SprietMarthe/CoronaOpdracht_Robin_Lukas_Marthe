import at.favre.lib.crypto.HKDF;

import javax.rmi.ssl.SslRMIClientSocketFactory;
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

    protected MatchingServiceImpl() throws RemoteException, NoSuchAlgorithmException {
        try {
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

            //TODO timer schedulen die capsules verwijdert na x aantal dagen
            //TODO timer schedulen die critische waarden verwijderd na x dagen + tokens die nog over zijn inlichten via registrar

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        //TODO check of er geen null waarden worden gegeven uit de map (kan als info van visitor vals is)
        for(Tuple t : tuples){
            Map<String, byte[]> pseudonyms = registrar.downloadPseudonyms(t.getLocation().date.getDayOfYear());
            String tbhash = t.getLocation().random + Arrays.toString(pseudonyms.get(t.getLocation().CF));
            md.update(tbhash.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();

            if(Arrays.equals(digest, t.getLocation().hash)){
                System.out.println("hash klopt, correcte informatie verschaft door visitor");
                markCapsules(t);
            }
            else{
                System.out.println("hash incorrect!");
                System.out.println("random tuple: " + t.getLocation().random);
                System.out.println("nym registrar: " + pseudonyms.get(t.getLocation().CF));
                System.out.println("CF tuple: " + t.getLocation().CF);
                System.out.println(digest);
                System.out.println(t.getLocation().hash);
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
                if(list.remove(conftoken)){
                    System.out.println("token removed");
                }
            });
    }

    public void markCapsules(Tuple t){
        List<Capsule> newcriticalcaps = new ArrayList<>();
        List<Token> newcriticaltokens = new ArrayList<>();
        for(Capsule c : capsules){
            if(Arrays.equals(c.hash, t.getLocation().hash) && overlap(c.date, t.getLocation().date)){
                newcriticalcaps.add(c);
                newcriticaltokens.add(c.token);
                System.out.println("capsule gemarked");
            }
        }
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
}
