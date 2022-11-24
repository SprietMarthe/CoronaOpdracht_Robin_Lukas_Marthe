import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class CateringImpl extends UnicastRemoteObject implements Catering {

    Enrollment enrollment = null;
    int businessNumber;
    String name, address;
    Scanner sc;

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
            enrollment = (Enrollment) myRegistry.lookup("Enrollment");

            String response = enrollment.helloTo(name, businessNumber, address);
            System.out.println(response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //stuur een referentie naar onze eigen interface door naar de server zodat deze ons ook kan contacteren
    public void register() throws RemoteException {
        enrollment.register(this);
    }



    public static void main(String[] args) throws RemoteException {
        printMenu();
    }

    private static void printMenu() throws RemoteException {
        Scanner s = new Scanner(System.in);
        int choice = 0;
        while (choice != -1) {
            System.out.println();
            System.out.println("1.Exit");
            System.out.println("2.Enroll");
            System.out.println("Enter your choice");
            choice = s.nextInt();
            switch (choice) {
                case 1:
                    choice = -1;
                    break;
                case 2:
                    System.out.println("-----Enroll BarOwner-----");
                    CateringImpl catering = new CateringImpl();
                    catering.register();
                    break;
            }
        }
        s.close();
    }

    @Override
    public void printName() throws RemoteException {
        System.out.println(name);
    }
}
