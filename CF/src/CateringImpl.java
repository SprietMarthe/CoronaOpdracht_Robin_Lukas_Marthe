import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Scanner;

public class CateringImpl extends UnicastRemoteObject implements Catering {

    Registrar registrar;
    int businessNumber;
    String name, address;
    Scanner sc;
    byte[] secretKey;
    byte[] pseudonym;

    public CateringImpl() throws RemoteException {

        sc = new Scanner(System.in);
        System.out.println("Enter unique name");
        this.name = sc.nextLine();
        System.out.println("Enter unique business number");
        this.businessNumber = Integer.parseInt(sc.next());
        System.out.println("Enter unique address");
        this.address = sc.next();
        System.out.println("Done!");

        try {
            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registrar = (Registrar) myRegistry.lookup("Registrar");

            String response = registrar.helloTo(name, businessNumber, address);
            System.out.println(response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //stuur een referentie naar onze eigen interface door naar de server zodat deze ons ook kan contacteren
    public void register() throws RemoteException {
        registrar.register(this);
    }



    public static void main(String[] args) throws RemoteException {
        printMenu();
    }

    private static void printMenu() throws RemoteException {
        Scanner s = new Scanner(System.in);
        int choice = 0;
        System.out.println("-----Enroll BarOwner-----");
        CateringImpl catering = new CateringImpl();
        catering.register();

        while (choice != -1) {
            System.out.println();
            System.out.println("1.Exit");
            System.out.println("2. Print secret key");
            System.out.println("3. Print pseudonym");
            System.out.println("Enter your choice");
            choice = s.nextInt();
            switch (choice) {
                case 1:
                    choice = -1;
                    break;
                case 2:
                    System.out.println(Arrays.toString(catering.secretKey));
                    break;
                case 3:
                    System.out.println(Arrays.toString(catering.pseudonym));
            }
        }
        s.close();
    }

    @Override
    public int getBusinessNumber() throws RemoteException {
        return businessNumber;
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public void setSecretKey(byte[] secretKey) throws RemoteException {
        this.secretKey = secretKey;
    }

    @Override
    public String getData() throws RemoteException {
        return businessNumber + name + address;
    }

    @Override
    public String getLocation() throws RemoteException {
        return address;
    }

    @Override
    public void setPseudonym(byte[] pseudonym) throws RemoteException {
        this.pseudonym = pseudonym;
    }
}
