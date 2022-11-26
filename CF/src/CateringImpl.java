import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class CateringImpl extends UnicastRemoteObject implements Catering {

    Registrar registrar;
    int businessNumber;
    String name, address;
    Scanner sc;
    byte[] secretKey;
    byte[] pseudonym;
    String CF;
    //hashing functie om hash in qrcode te genereren
    MessageDigest md = MessageDigest.getInstance("SHA-256");

    public CateringImpl() throws RemoteException, NoSuchAlgorithmException {

        sc = new Scanner(System.in);
        System.out.println("Enter unique name");
        this.name = sc.nextLine();
        System.out.println("Enter unique business number");
        this.businessNumber = Integer.parseInt(sc.next());
        System.out.println("Enter unique address");
        this.address = sc.next();
        System.out.println("Done!");
        this.CF = businessNumber + name + address;

        try {
            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registrar = (Registrar) myRegistry.lookup("Registrar");

            String response = registrar.helloTo(name);
            System.out.println(response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //stuur een referentie naar onze eigen interface door naar de server zodat deze ons ook kan contacteren
    public void register() throws RemoteException {
        registrar.register(this);
    }

    public static void main(String[] args) throws RemoteException, NoSuchAlgorithmException {
        printMenu();
    }

    private static void printMenu() throws RemoteException, NoSuchAlgorithmException {
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

    //qr code generaten van random nummer, identifier en hash van pseudonym met randon nummer
    public void genQRCode() throws IOException, WriterException {
        Random rand = new Random();
        int random = rand.nextInt(1000);
        String tbhash = String.valueOf(random) + Arrays.toString(pseudonym);
        md.update(tbhash.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        String data = String.valueOf(random) + "/" + CF + "/" + Arrays.toString(digest);
        BitMatrix matrix = new MultiFormatWriter().encode(
                new String(data.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8),
                BarcodeFormat.QR_CODE, 200, 200);

        String path = "code" + businessNumber;
        path += ".png";
        MatrixToImageWriter.writeToFile(
                matrix,
                path.substring(path.lastIndexOf('.') + 1),
                new File(path));
        System.out.println("qr code output: " + data);
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
    public String getCF() throws RemoteException {
        return CF;
    }

    @Override
    public String getLocation() throws RemoteException {
        return address;
    }

    @Override
    public void setPseudonym(byte[] pseudonym) throws IOException, WriterException {
        this.pseudonym = pseudonym;
        this.genQRCode();
    }
}
