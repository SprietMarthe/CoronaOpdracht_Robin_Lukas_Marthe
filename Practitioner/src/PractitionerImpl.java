import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class PractitionerImpl extends UnicastRemoteObject implements Practitioner {
    MatchingService matcher;

    protected PractitionerImpl() throws RemoteException {
        try {
            // fire to localhost port 1099
            Registry registry = LocateRegistry.getRegistry(1099);
            registry.bind("Practitioner", this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, RemoteException  {
        PractitionerImpl practitioner = new PractitionerImpl();
    }

    @Override
    public void register(MatchingService matchingService) throws RemoteException{
        this.matcher = matchingService;
    }
}
