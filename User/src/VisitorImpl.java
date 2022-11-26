import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class VisitorImpl extends UnicastRemoteObject implements Visitor {
    Registrar registrar;
    String number;
    String name;
    Token token;

    public VisitorImpl() throws RemoteException {
        try {
            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registrar = (Registrar) myRegistry.lookup("Registrar");
            registrar.register(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws RemoteException {
        Scanner sc = new Scanner(System.in);
        VisitorImpl visitor = new VisitorImpl();
        System.out.println("Enter name:");
        visitor.name = sc.nextLine();
        System.out.println("Enter phone number:");
        visitor.number = sc.nextLine();
        System.out.println("succesfully signed up!");
    }

    @Override
    public String getNumber() throws RemoteException {
        return number;
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public void setToken(Token token) throws RemoteException {
        this.token = token;
    }
}
