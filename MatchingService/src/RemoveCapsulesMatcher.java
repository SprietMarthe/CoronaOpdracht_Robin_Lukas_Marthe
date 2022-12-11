import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class RemoveCapsulesMatcher extends TimerTask {
    MatchingServiceImpl matcher;

    RemoveCapsulesMatcher(MatchingServiceImpl matcher){
        this.matcher = matcher;
    }

    @Override
    public void run() {
        List<Capsule> old = new ArrayList<>();
        matcher.capsules.forEach(c->{
            if(c.date.plusDays(1).isBefore(LocalDateTime.now())){
                old.add(c);
            }
        });
        matcher.capsules.removeAll(old);
    }
}
