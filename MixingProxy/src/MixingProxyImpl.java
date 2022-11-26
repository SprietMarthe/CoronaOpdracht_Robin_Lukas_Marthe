import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MixingProxyImpl extends UnicastRemoteObject implements MixingProxy{
    MatchingService matcher;

    public MixingProxyImpl() throws RemoteException {
        try {
            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            matcher = (MatchingService) myRegistry.lookup("MatchingService");
            matcher.register(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){

    }
}
