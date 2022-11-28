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
    int random;
    int day;
    byte[] hash;
    LocalDateTime date = LocalDateTime.now();

    Capsule(int random, int day, byte[] hash){
        this.random = random;
        this.day = day;
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "Capsule{" +
                "random=" + random +
                ", day=" + day +
                ", hash=" + Arrays.toString(hash) +
                ", date=" + date +
                '}';
    }
}

public class MixingProxyImpl extends UnicastRemoteObject implements MixingProxy{
    private MatchingService matcher;
    private Map<String, Visitor> visitors;
    private Queue<Capsule> queueCapsules;

    protected MixingProxyImpl() throws RemoteException {
        visitors = new HashMap<>();
        queueCapsules = new LinkedList<>();
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
        queueCapsules.add(c);
        checkCapsule();
    }

    private void checkCapsule() {
        for(Capsule c : queueCapsules){
            boolean isNotOkay = false;
            // TODO check the validity of the user token

            // check if token is a token for that particular 𝑑𝑎𝑦𝑖
            if (LocalDateTime.now().getDayOfYear() != c.day){
                System.out.println(LocalDateTime.now().getDayOfYear());
                System.out.println(c.day);
                isNotOkay = true;
            }
            // TODO check if token has not been spent before

            System.out.println("isNotOkay: " + isNotOkay);
        }

    }

}
