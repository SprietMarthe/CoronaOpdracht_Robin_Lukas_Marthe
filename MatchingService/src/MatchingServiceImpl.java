import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MatchingServiceImpl extends UnicastRemoteObject implements MatchingService {
    MixingProxy mixer;
    protected MatchingServiceImpl() throws RemoteException {
    }

    @Override
    public void register(MixingProxy mixer) throws RemoteException {
        this.mixer = mixer;
    }
}
