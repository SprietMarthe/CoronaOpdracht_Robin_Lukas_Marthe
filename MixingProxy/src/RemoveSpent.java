import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class RemoveSpent extends TimerTask {
    MixingProxyImpl mixer;

    RemoveSpent(MixingProxyImpl mixer){
        this.mixer = mixer;
    }

    @Override
    public void run() {
        List<Token> toremove = new ArrayList<>();
        mixer.spent.forEach(t->{
            if(t.getDay()+1 < LocalDateTime.now().getDayOfYear()){
                toremove.add(t);
            }
        });
        mixer.spent.removeAll(toremove);
    }
}
