import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MatchingServiceImpl extends UnicastRemoteObject implements MatchingService {
    MixingProxy mixer;
    Registrar registrar;

    protected MatchingServiceImpl() throws RemoteException {
        try {
            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registrar = (Registrar) myRegistry.lookup("Registrar");
            registrar.register(this);

            Registry registryMixing = LocateRegistry.getRegistry("localhost", 2019,
                    new SslRMIClientSocketFactory());
            mixer = (MixingProxy) registryMixing.lookup("MixingProxy");
            mixer.register(this);

            //TODO timer schedulen die logs verwijdert na x aantal dagen

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("javax.net.ssl.trustStore","truststore");
        System.setProperty("javax.net.ssl.trustStorePassword","trustword");
        MatchingServiceImpl matcher = new MatchingServiceImpl();
    }

    @Override
    public void register(MixingProxy mixer) throws RemoteException {
        this.mixer = mixer;
    }
}
