import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
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
        } catch (RemoteException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
