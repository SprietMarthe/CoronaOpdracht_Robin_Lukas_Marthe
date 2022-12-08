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
import java.util.ArrayList;
import java.util.List;

public class MatchingServiceImpl extends UnicastRemoteObject implements MatchingService {
    MixingProxy mixer;
    Registrar registrar;
    Practitioner practitioner;
    List<Capsule> capsules = new ArrayList<>();
    private final HKDF hkdf = HKDF.fromHmacSha256();
    private final MessageDigest md = MessageDigest.getInstance("SHA-256");

    protected MatchingServiceImpl() throws RemoteException, NoSuchAlgorithmException {
        try {
            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registrar = (Registrar) myRegistry.lookup("Registrar");
            registrar.register(this);

            practitioner = (Practitioner) myRegistry.lookup("Practitioner");
            practitioner.register(this);

            Registry registryMixing = LocateRegistry.getRegistry("localhost", 2019,
                    new SslRMIClientSocketFactory());
            mixer = (MixingProxy) registryMixing.lookup("MixingProxy");
            mixer.register(this);

            //TODO timer schedulen die capsules verwijdert na x aantal dagen

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
        this.capsules = capsules;
    }

    @Override
    public void getLogs(List<Location> locationlogs) throws RemoteException {
        List<byte[]> pseudonyms = new ArrayList<>();
        for (Location l : locationlogs) {
            byte[] pseudonym = registrar.downloadPseudonyms(l.date.getDayOfYear());
            pseudonyms.add(pseudonym);
        }
        checkValidity(pseudonyms, locationlogs);
    }

    private void checkValidity(List<byte[]> pseudonyms, List<Location> locationlogs) {
        // TODO hash of random and CF (van registrar)
        for (Location l : locationlogs) {
//            byte[] hashRandomAndCF = hkdf.expand(l.random.getBytes(StandardCharsets.UTF_8), l.CF.getBytes(StandardCharsets.UTF_8), 16);
        }
    }
}
