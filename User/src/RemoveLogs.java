import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class RemoveLogs extends TimerTask {
    VisitorImpl visitor;

    RemoveLogs(VisitorImpl visitor){
        this.visitor = visitor;
    }

    @Override
    public void run() {
        List<Location> toremove = new ArrayList<>();
        for(Location l : visitor.locationlogs){
            if(l.date.plusDays(2).isBefore(LocalDateTime.now())){
                toremove.add(l);
            }
        }
        visitor.locationlogs.removeAll(toremove);
    }
}
