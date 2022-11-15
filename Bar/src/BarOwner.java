import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class BarOwner {
    Enrollment enrollment = null;
    int businessNumber;
    String name, address;
    Scanner sc;
    public BarOwner(){
        System.out.println("Give business number, name, address");
        sc = new Scanner(System.in);
        this.businessNumber = Integer.parseInt(sc.next());
        this.name = sc.next();
        this.address = sc.next();
        try {
            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            enrollment = (Enrollment) myRegistry.lookup("EnrollmentService");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        BarOwner barOwner = new BarOwner();
    }

}
