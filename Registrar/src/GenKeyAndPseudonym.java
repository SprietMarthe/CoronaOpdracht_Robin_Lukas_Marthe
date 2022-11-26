import com.google.zxing.WriterException;

import java.io.IOException;
import java.util.TimerTask;

public class GenKeyAndPseudonym extends TimerTask {
    RegistrarImpl registrar;

    GenKeyAndPseudonym(RegistrarImpl registrar){
        this.registrar = registrar;
    }

    @Override
    public void run() {
        try {
            registrar.genSecretKeysAndPseudonym();
        } catch (IOException | WriterException e) {
            e.printStackTrace();
        }
        registrar.nextDay();
    }
}
