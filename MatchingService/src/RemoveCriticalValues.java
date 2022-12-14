import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class RemoveCriticalValues extends TimerTask {
    MatchingServiceImpl matcher;
    Boolean demo;

    RemoveCriticalValues(MatchingServiceImpl matcher, Boolean demo) {
        this.matcher = matcher;
        this.demo = demo;
    }

    @Override
    public void run() {
        if (!demo) {
            List<Integer> oldcaps = new ArrayList<>();
            matcher.criticalCaps.forEach((key, value) -> {
                if (key + 1 < LocalDateTime.now().getDayOfYear()) {
                    oldcaps.add(key);
                }
            });
            oldcaps.forEach(key -> {
                matcher.criticalCaps.remove(key);
            });

            List<Integer> oldtokens = new ArrayList<>();
            matcher.criticalTokens.forEach((key, value) -> {
                if (key + 1 < LocalDateTime.now().getDayOfYear()) {
                    oldtokens.add(key);
                }
            });
            oldtokens.forEach(key -> {
                matcher.criticalTokens.get(key).forEach(token -> {
                    try {
                        System.out.println("notifying registrar of uninformed visitors!");
                        matcher.forwardUninformed(token);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });
            });
            oldtokens.forEach(key -> {
                matcher.criticalTokens.remove(key);
            });
        }else{
            List<Integer> oldcaps = new ArrayList<>();
            matcher.criticalCaps.forEach((key, value) -> {
                oldcaps.add(key);
            });
            oldcaps.forEach(key -> {
                matcher.criticalCaps.remove(key);
            });

            List<Integer> oldtokens = new ArrayList<>();
            matcher.criticalTokens.forEach((key, value) -> {
                oldtokens.add(key);
            });
            oldtokens.forEach(key -> {
                matcher.criticalTokens.get(key).forEach(token -> {
                    try {
                        System.out.println("notifying registrar of uninformed visitors!");
                        matcher.forwardUninformed(token);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });
            });
            oldtokens.forEach(key -> {
                matcher.criticalTokens.remove(key);
            });
        }
    }
}

