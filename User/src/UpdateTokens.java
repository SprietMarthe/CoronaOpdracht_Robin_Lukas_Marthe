import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.TimerTask;

public class UpdateTokens extends TimerTask {
    VisitorImpl visitor;
    int random;
    String CF;

    public UpdateTokens(VisitorImpl visitor, int random, String cf) {
        this.visitor = visitor;
        this.random = random;
        this.CF = cf;
    }

    @Override
    public void run() {
        try {
            visitor.sendUpdateCapsule(random,CF);
        } catch (SignatureException | RemoteException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
