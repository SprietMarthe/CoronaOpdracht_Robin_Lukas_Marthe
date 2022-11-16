import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Visitor {
    Registrar registrar = null;

    public Visitor(){
        try {
            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);

            registrar = (Registrar) myRegistry.lookup("Enrollment");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Visitor visitor = new Visitor();
    }

}
