import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class CateringImpl extends UnicastRemoteObject implements Catering {

    Registrar registrar;
    int businessNumber;
    String name, address;
    Scanner sc;
    byte[] secretKey;
    byte[] pseudonym;
    String CF;

    JFrame frame = new JFrame("Caterer");
    JTextField NameTextField = new JTextField();
    JTextField BuisinessNumberTextField = new JTextField();
    JTextField AddressTextField = new JTextField();
    JTextField OutputTextField = new JTextField();
    JButton logInButton = new JButton("log in");
    JButton getSecretkey = new JButton("secret key");
    JButton getPseudonym = new JButton("pseudonym");
    JButton getQRCode = new JButton("QR code");
    JLabel NameLabel = new JLabel("Name");
    JLabel BuisinessNumberLabel = new JLabel("Buisiness Number");
    JLabel AddressLabel = new JLabel("Address");
    JLabel OutputLabel = new JLabel("output:");

    //hashing functie om hash in qrcode te genereren
    MessageDigest md = MessageDigest.getInstance("SHA-256");

    public CateringImpl() throws RemoteException, NoSuchAlgorithmException {
        setFrame();
        try {
            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registrar = (Registrar) myRegistry.lookup("Registrar");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws RemoteException, NoSuchAlgorithmException {
        CateringImpl catering = new CateringImpl();
        //catering.register();
    }

    public void setFrame(){
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(0,0,1000,400);
        NameLabel.setBounds(0,20,10,10);
        BuisinessNumberLabel.setBounds(0,0,10,10);
        AddressLabel.setBounds(0,0,10,10);
        NameTextField.setVisible(true);
        BuisinessNumberTextField.setVisible(true);
        AddressTextField.setVisible(true);
        OutputLabel.setVisible(false);
        OutputTextField.setVisible(false);
        frame.getContentPane().add(NameLabel);
        frame.getContentPane().add(BuisinessNumberLabel);
        frame.getContentPane().add(AddressLabel);
        frame.getContentPane().add(NameTextField);
        frame.getContentPane().add(BuisinessNumberTextField);
        frame.getContentPane().add(AddressTextField);
        frame.getContentPane().add(OutputLabel);
        frame.getContentPane().add(OutputTextField);
        frame.getContentPane().add(logInButton);
        frame.getContentPane().add(getQRCode);
        frame.getContentPane().add(getPseudonym);
        frame.getContentPane().add(getSecretkey);

        frame.setLayout(new GridLayout(5,2));
        frame.setSize(1000,400);
        getPseudonym.setVisible(false);
        getSecretkey.setVisible(false);
        getQRCode.setVisible(false);
        NameTextField.setEditable(true);
        BuisinessNumberTextField.setEditable(true);
        AddressTextField.setEditable(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.pack();

        getQRCode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    //TODO only generate QR each day
                    //TODO eigenlijk enkel updaten als genPseudonym is opgeroepen (registrar bepaald wanneer code wordt geupdate)
                    genQRCode();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        getSecretkey.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    OutputTextField.setText(Arrays.toString(secretKey));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        getPseudonym.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    OutputTextField.setText(Arrays.toString(pseudonym));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        logInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    tryLogIn();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }

    public void tryLogIn()throws  Exception{
        if(!Objects.equals(NameTextField,"") && !Objects.equals(BuisinessNumberTextField,"") && !Objects.equals(AddressTextField,"")){
            this.name = NameTextField.getText();
            this.businessNumber = Integer.parseInt(BuisinessNumberTextField.getText());
            this.address = AddressTextField.getText();
            this.CF = businessNumber + name + address;
            frame.remove(NameTextField);
            frame.remove(BuisinessNumberTextField);
            frame.remove(AddressTextField);
            frame.remove(NameLabel);
            frame.remove(BuisinessNumberLabel);
            frame.remove(AddressLabel);
            frame.remove(logInButton);
            OutputLabel.setVisible(true);
            OutputTextField.setVisible(true);
            getQRCode.setVisible(true);
            getSecretkey.setVisible(true);
            getPseudonym.setVisible(true);
            register();
            String response = registrar.helloTo(name);
            OutputTextField.setText(response);
        }
    }

    //stuur een referentie naar onze eigen interface door naar de server zodat deze ons ook kan contacteren
    public void register() throws IOException, WriterException {
        registrar.register(this);
    }



    private static void printMenu() throws RemoteException, NoSuchAlgorithmException {
        Scanner s = new Scanner(System.in);
        int choice = 0;
        System.out.println("-----Enroll BarOwner-----");
        //CateringImpl catering = new CateringImpl();
        //catering.register();

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
                    //System.out.println(Arrays.toString(catering.secretKey));
                    break;
                case 3:
                    //System.out.println(Arrays.toString(catering.pseudonym));
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
        String data = String.valueOf(random) + "|" + CF + "|" + Base64.getEncoder().encodeToString(digest);
        BitMatrix matrix = new MultiFormatWriter().encode(
                new String(data.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8),
                BarcodeFormat.QR_CODE, 200, 200);

        String path = "code" + businessNumber;              //TODO tonen in gui
        path += ".png";
        MatrixToImageWriter.writeToFile(
                matrix,
                path.substring(path.lastIndexOf('.') + 1),
                new File(path));
        OutputTextField.setText(data);
        System.out.println("random: " + random);
        System.out.println("nym: " + pseudonym);
        System.out.println("CF: " + CF);
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
        System.out.println("nym bij ontvangst: " + pseudonym);
        this.pseudonym = pseudonym;
        this.genQRCode();
    }
}
