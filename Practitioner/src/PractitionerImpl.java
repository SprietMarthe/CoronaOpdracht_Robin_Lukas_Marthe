import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.List;


public class PractitionerImpl extends UnicastRemoteObject implements Practitioner {
    MatchingService matcher;
    private final Signature ecdsaSignature = Signature.getInstance("SHA256withRSA");
    private final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    private KeyPair pair;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    protected PractitionerImpl() throws RemoteException, NoSuchAlgorithmException {
        this.keyPairGenerator.initialize(1024);
        this.pair = this.keyPairGenerator.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
        try {
            // fire to localhost port 1099
            Registry registry = LocateRegistry.getRegistry(1099);
            registry.bind("Practitioner", this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, RemoteException, NoSuchAlgorithmException {
        PractitionerImpl practitioner = new PractitionerImpl();
    }

    private byte[] signHash(byte[] hash) throws InvalidKeyException, SignatureException {
        ecdsaSignature.initSign(privateKey);
        ecdsaSignature.update(hash);
        return ecdsaSignature.sign();
    }

    @Override
    public void register(MatchingService matchingService) throws RemoteException{
        this.matcher = matchingService;
    }

    @Override
    public void getLogs(List<Location> locationlogs) throws SignatureException, InvalidKeyException, RemoteException {
        for (Location l : locationlogs) {
            signHash(l.hash);
        }
        // TODO shuffeling with tupples from other users
        matcher.getLogs(locationlogs);
    }


}
