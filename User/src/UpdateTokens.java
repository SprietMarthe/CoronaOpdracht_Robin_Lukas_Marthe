import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.TimerTask;

public class UpdateTokens extends TimerTask {
    VisitorImpl visitor;

    UpdateTokens(VisitorImpl visitor){
        this.visitor = visitor;
    }

    @Override
    public void run() {
        try {
            visitor.sendUpdateCapsule();
        } catch (SignatureException | RemoteException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
