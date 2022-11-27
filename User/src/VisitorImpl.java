import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.net.SocketPermission;

//klasse om gescande QR code te loggen
class Location {
    int random;
    String CF;
    byte[] hash;
    LocalDateTime date = LocalDateTime.now();

    Location(int random, String CF, byte[] hash){
        this.random = random;
        this.CF = CF;
        this.hash = hash;
    }
}

public class VisitorImpl extends UnicastRemoteObject implements Visitor {
    Registrar registrar;
    String number;
    String name;
    Token token;
    //data verkregen uit QR code
    List<Location> logs = new ArrayList<>();

    public VisitorImpl() throws RemoteException {
        try {
            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registrar = (Registrar) myRegistry.lookup("Registrar");
            registrar.register(this);

            //TODO timer schedulen die logs verwijdert na x aantal dagen

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws RemoteException {
        Scanner sc = new Scanner(System.in);
        VisitorImpl visitor = new VisitorImpl();
        System.out.println("Enter name:");
        visitor.name = sc.nextLine();
        System.out.println("Enter unique phone number:");
        visitor.number = sc.nextLine();
        System.out.println("succesfully signed up!");
        int i = 0;
        while(true){
            System.out.println("1. Scan QR Code");
            System.out.println("Enter your choice");
            i = sc.nextInt();
            switch(i){
                case 1:
                    visitor.scanQRCode();
                    break;
            }
        }
    }

    public void scanQRCode(){
        String input;
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter input");
        input = sc.nextLine();
        if(!Objects.equals(input, "")){
            int random = Integer.parseInt(input.split("/")[0]);
            String CF = input.split("/")[1];
            byte[] hash = input.split("/")[2].getBytes(StandardCharsets.UTF_8);
            Location l = new Location(random, CF, hash);
            // location maar 1 keer toevoegen
            if (!logs.contains(l)){
                logs.add(l);
            }
            sendCapsule(hash);
        }
    }

    public void sendCapsule(byte[] hash){
        //TODO stuur capsule naar mixing server/proxy
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
    public void setToken(int day, int r) throws RemoteException {
        this.token = new Token(day,r);
    }
}
