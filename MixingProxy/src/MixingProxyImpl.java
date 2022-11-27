import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

public class MixingProxyImpl extends UnicastRemoteObject implements MixingProxy{
//    MatchingService matcher;
    private Map<String, Visitor> visitors;



    protected MixingProxyImpl() throws RemoteException {
        visitors = new HashMap<>();
    }

    private void startMixingProxy(){
        try {
//            System.setProperty("java.security.policy","file:permissionPolicies.policy");

//            // fire to localhost port 1099
//            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
//            matcher = (MatchingService) myRegistry.lookup("MatchingService");
//            matcher.register(this);


//        // Create and install a security manager
//        if (System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }

            // Create SSL-based registry
            Registry registry = LocateRegistry.createRegistry(2019,
                    new SslRMIClientSocketFactory(),
                    new SslRMIServerSocketFactory());
            registry.bind("MixingProxy", this);
            System.out.println("MixingProxy bound in registry");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        System.setProperty("javax.net.ssl.keyStore","keystore");
        System.setProperty("javax.net.ssl.keyStorePassword","password");
        MixingProxyImpl mixingProxy = new MixingProxyImpl();
        mixingProxy.startMixingProxy();
    }

    @Override
    public String sayHello() throws RemoteException {
        return "Hello World!";
    }

    @Override
    public void register(Visitor visitor)throws RemoteException {
        visitors.put(visitor.getNumber(), visitor);
    }
}
