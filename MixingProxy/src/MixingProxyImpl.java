import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.*;

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
    private Queue<Capsule> queueCapsules;
    private List<Token> spent;

    protected MixingProxyImpl() throws RemoteException {
        visitors = new HashMap<>();
        queueCapsules = new LinkedList<>();
        spent = new ArrayList<>();
    }

    private void startMixingProxy(){
        try {
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

    public static void main(String[] args) throws IOException {
        //http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html#CreateKeystore gevolgde tutorial
        System.setProperty("javax.net.ssl.keyStore","keystore");
        System.setProperty("javax.net.ssl.keyStorePassword","password");
        MixingProxyImpl mixingProxy = new MixingProxyImpl();
        mixingProxy.startMixingProxy();
        printMenu(mixingProxy);
    }

    private static void printMenu(MixingProxyImpl mixingProxy) {
        Scanner s = new Scanner(System.in);
        int choice = 0;
        System.out.println("-----Mixing Proxy Options-----");
        while (choice != -1) {
            System.out.println();
            System.out.println("1. Exit");
            System.out.println("2. Show Queue");
            System.out.println("3. Flush Queue");
            System.out.println("Enter your choice:");
            choice = s.nextInt();
            switch (choice) {
                case 1:
                    choice = -1;
                    break;
                case 2:
                    mixingProxy.printQueue();
                    break;
                case 3:
                    mixingProxy.flushQueue();
                    break;
            }
        }
        s.close();
    }

    private void flushQueue() {
        queueCapsules.clear();
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
    public void sendCapsule(Capsule c) throws RemoteException {
        //check incoming capsule en voeg toe aan queue als goedgekeurd
        if(checkCapsule(c)){
            queueCapsules.add(c);
            spent.add(c.token);
        }

    }

    private boolean checkCapsule(Capsule c) {
        boolean good = true;

        // TODO check the validity of the user token

        // check if token is a token for that particular day
        if(LocalDateTime.now().getDayOfYear() != c.token.getDay()){
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

}
