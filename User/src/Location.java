import java.io.Serializable;
import java.time.LocalDateTime;

//klasse om gescande QR code te loggen
public class Location implements Serializable  {
    int random;
    String CF;
    byte[] hash;
    LocalDateTime date = LocalDateTime.now();
    Token token;

    Location(int random, String CF, byte[] hash, Token token){
        this.random = random;
        this.CF = CF;
        this.hash = hash;
        this.token = token;
    }
}