import java.rmi.RemoteException;
import java.util.TimerTask;

public class FlushQueue extends TimerTask {
    MixingProxyImpl mixer;

    FlushQueue(MixingProxyImpl mixer){
        this.mixer = mixer;
    }

    @Override
    public void run() {
        try {
            mixer.flushQueue();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
