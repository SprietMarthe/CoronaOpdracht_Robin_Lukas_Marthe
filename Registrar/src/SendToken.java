import java.rmi.RemoteException;
import java.util.TimerTask;

public class SendToken extends TimerTask {
    RegistrarImpl registrar;

    SendToken(RegistrarImpl registrar){
        this.registrar = registrar;
    }

    @Override
    public void run() {
        try {
            registrar.sendTokens();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
