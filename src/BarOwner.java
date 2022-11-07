import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BarOwner {
    Registrar registrar = null;

    public BarOwner(){
        try {
            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);

            registrar = (Registrar) myRegistry.lookup("Registrar");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
