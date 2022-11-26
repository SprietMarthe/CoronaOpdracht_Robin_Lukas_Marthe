public class Token {
    private final int random;
    private final int day;

    Token(int day, int random){
        this.day = day;
        this.random = random;
    }

    public String getData(){
        return Integer.toString(day) + random;
    }
}
