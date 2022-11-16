import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Registrar {

    private void startRegistrar() {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);

            registry.rebind("Enrollment", new EnrollmentImpl());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("system is ready");
    }

    public static void main(String[] args) {
        Registrar registrar = new Registrar();
        registrar.startRegistrar();
    }
}
