import java.io.Serializable;
import java.util.Arrays;

public class Token implements Serializable {
    private final int random;
    private final int day;
    private final byte[] signature;

    Token(int day, int random, byte[] signature){
        this.day = day;
        this.random = random;
        this.signature = signature;
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

    public byte[] getSignature(){
        return signature;
    }

//    @Override
//    public String toString() {
//        return "Token{" +
//                "random=" + random +
//                ", day=" + day +
//                ", signature=" + signature +
//                '}';
//    }

    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Token)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        Token t = (Token) o;

        // Compare the data members and return accordingly
        return Integer.compare(random, t.random) == 0
                && Integer.compare(day, t.day) == 0
                && Arrays.equals(signature, t.signature);
    }
}
