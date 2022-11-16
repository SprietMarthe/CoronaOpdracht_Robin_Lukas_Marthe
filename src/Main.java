import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        printMenu();
    }

    private static void printMenu() {
        Scanner sc = new Scanner(System.in);
        int choice = 0;
        while (choice != -1) {
            System.out.println("1.Exit");
            System.out.println("2.Enroll");
            System.out.println("Enter your choice");
            choice = sc.nextInt();
            switch (choice) {
                case 1:
                    choice = -1;
                    break;

                case 2:
                    System.out.println("Enroll BarOwner");
                    BarOwner barOwner = new BarOwner();
                    break;
            }
        }
    }
}
