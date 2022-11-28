import java.io.Serializable;

public class Token implements Serializable {
    private final int random;
    private final int day;

    Token(int day, int random){
        this.day = day;
        this.random = random;
    }

    public String getData(){
        return Integer.toString(day) + random;
    }

    public int getRandom() {
        return random;
    }

    public int getDay() {
        return day;
    }

    @Override
    public String toString() {
        return "Token{" +
                "random=" + random +
                ", day=" + day +
                '}';
    }
}
