import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.Timer;

//klasse om gescande QR code te loggen
class Location {
    int random;
    String CF;
    byte[] hash;
    LocalDateTime date = LocalDateTime.now();
    Token token;

    Location(int random, String CF, byte[] hash, Token token){
        this.random = random;
        this.CF = CF;
        this.hash = hash;
        this.token = token;
    }
}

public class VisitorImpl extends UnicastRemoteObject implements Visitor {
    Registrar registrar;
    Practitioner practitioner;
    String number;
    String name;
    List<Token> tokens = new ArrayList<>();
    //data verkregen uit QR code
    List<Location> locationlogs = new ArrayList<>();
    byte[] locationhash;
    MixingProxy mixer;
    //bool om logout button te tonen op ui?
    boolean onlocation = false;
    //timer om updatecapsules te sturen naar mixer eenmaal op locatie
    Timer t;
    //lijst met uitgegeven tokens
    List<Token> spent = new ArrayList<>();

    JFrame frame = new JFrame("Visitor");
    JTextField NameTextField = new JTextField();
    JTextField PhoneTextField = new JTextField();
    JTextField QRTextField = new JTextField();
    JButton logInButton = new JButton("Log in");
    JButton scanQRCodeButton = new JButton("Scan QR code");
    JLabel PhoneLabel = new JLabel("Unique phone number");
    JLabel NameLabel = new JLabel("Name");
    JLabel QRLabel = new JLabel("QR code");

    public VisitorImpl() throws RemoteException {
        setFrame();

        try {
            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registrar = (Registrar) myRegistry.lookup("Registrar");
            registrar.register(this);

            practitioner = (Practitioner) myRegistry.lookup("Practitioner");

            Registry registryMixing = LocateRegistry.getRegistry("localhost", 2019,
                    new SslRMIClientSocketFactory());
            mixer = (MixingProxy) registryMixing.lookup("MixingProxy");
            mixer.register(this);

            //TODO timer schedulen die logs verwijdert na x aantal dagen

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(0,0,600,400);

        NameLabel.setBounds(0,0, 10, 10);
        PhoneLabel.setBounds(50,100, 10, 10);
        NameTextField.setVisible(true);
        PhoneTextField.setVisible(true);
        QRLabel.setVisible(false);
        QRTextField.setVisible(false);
        frame.getContentPane().add(NameLabel);
        frame.getContentPane().add(NameTextField);
        frame.getContentPane().add(PhoneLabel);
        frame.getContentPane().add(PhoneTextField);
        frame.getContentPane().add(logInButton);
        frame.getContentPane().add(QRLabel);
        frame.getContentPane().add(QRTextField);
        frame.getContentPane().add(scanQRCodeButton);


        frame.setLayout(new GridLayout(4,2));
        frame.setSize(600,400);
        scanQRCodeButton.setVisible(false);
        NameTextField.setEditable(true);
        scanQRCodeButton.setVisible(false);
        PhoneTextField.setEditable(true);
        frame.pack();
        frame.setLocationRelativeTo(null); // center

        frame.setVisible(true);
        frame.pack();

        logInButton.addActionListener(new ActionListener(){    //add an event and take action
            public void actionPerformed(ActionEvent e){
                try {
                    tryLogIn();
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });
        scanQRCodeButton.addActionListener(new ActionListener(){    //add an event and take action
            public void actionPerformed(ActionEvent e){
                try {
                    scanQRCodeFromGUI();
                } catch (RemoteException | SignatureException | InvalidKeyException ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    public static void main(String[] args) throws RemoteException {
        System.setProperty("javax.net.ssl.trustStore","truststore");
        System.setProperty("javax.net.ssl.trustStorePassword","trustword");

        Scanner sc = new Scanner(System.in);
        VisitorImpl visitor = new VisitorImpl();
        visitor.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        visitor.frame.setVisible(true);
//        System.out.println("Enter name:");
//        visitor.name = sc.nextLine();
//        System.out.println("Enter unique phone number:");
//        visitor.number = sc.nextLine();
//        System.out.println("succesfully signed up!");
//        int i = 0;
//        while(true){
//            System.out.println("1. Scan QR Code");
//            System.out.println("Enter your choice");
//            i = sc.nextInt();
//            switch(i){
//                case 1:
//                    visitor.scanQRCode();
//                    break;
//            }
//        }
    }

    private void tryLogIn() throws RemoteException {
        if(!Objects.equals(NameTextField, "") && !Objects.equals(PhoneTextField, "")){
//            System.out.println("name:" + NameTextField.getText());
//            System.out.println("phone:" + PhoneTextField.getText());
            this.name = NameTextField.getText();
            this.number = PhoneTextField.getText();
            frame.remove(PhoneTextField);
            frame.remove(PhoneLabel);
            frame.remove(NameLabel);
            frame.remove(NameTextField);
            frame.remove(logInButton);
            QRLabel.setVisible(true);
            QRTextField.setVisible(true);
            scanQRCodeButton.setVisible(true);
        }
    }

    public void scanQRCode() throws RemoteException, SignatureException, InvalidKeyException {
        //TODO niet up to date met volgende functie
        String input;
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter input");
        input = sc.nextLine();
        if(!Objects.equals(input, "")){
            int random = Integer.parseInt(input.split("/")[0]);
            String CF = input.split("/")[1];
            locationhash = input.split("/")[2].getBytes(StandardCharsets.UTF_8);
            Token token = tokens.remove(0);
            spent.add(token);
            Location l = new Location(random, CF, locationhash, token);
            // location maar 1 keer toevoegen
            if (!locationlogs.contains(l)){
                locationlogs.add(l);
            }
            System.out.println("logs:" + locationlogs);
            sendCapsule(locationhash, token, random, CF);
        }
    }

    public void scanQRCodeFromGUI() throws RemoteException, SignatureException, InvalidKeyException {
        if(!Objects.equals(QRTextField.getText(), "")){
            int random = Integer.parseInt(QRTextField.getText().split("/")[0]);
            String CF = QRTextField.getText().split("/")[1];
            locationhash = QRTextField.getText().split("/")[2].getBytes(StandardCharsets.UTF_8);
            Token token = tokens.remove(0);
            spent.add(token);
            Location l = new Location(random, CF, locationhash,token);
            // location maar 1 keer toevoegen
            if (!locationlogs.contains(l)){
                locationlogs.add(l);
            }
            sendCapsule(locationhash, token, random, CF);
            onlocation = true;
        }
        QRTextField.setText("");
    }

    public void sendCapsule(byte[] hash, Token token, int random, String CF) throws RemoteException, SignatureException, InvalidKeyException {
//        System.out.println("time: " + LocalDateTime.now());
//        System.out.println("token: " + token);
//        System.out.println("hash: " + Arrays.toString(hash));

        //TODO capsule wordt 2x doorgestuurd nog fixen
        Capsule c = new Capsule(token, hash);
        setSignedHash(mixer.sendCapsule(c));
        //elk halfuur nieuwe token sturen naar mixer
        t = new Timer();
        t.scheduleAtFixedRate(new UpdateTokens(this, random, CF), 0, 30*60*1000);
    }

    public void sendUpdateCapsule(int random, String CF) throws SignatureException, RemoteException, InvalidKeyException {
        Token token = tokens.remove(0);
        Location l = new Location(random, CF, locationhash,token);
        locationlogs.add(l);
        Capsule c = new Capsule(token, locationhash);
        mixer.sendCapsule(c);
    }

    //TODO deze funtie callen vanuit een logout button op ui
    //TODO log exit time
    public void leaveLocation(){
        onlocation = false;
        System.out.println("left location");
        t.cancel();
    }

    @Override
    public String getNumber() throws RemoteException {return number;}

    @Override
    public String getName() throws RemoteException {return name;}

    @Override
    public void setTokens(List<Token> t) throws RemoteException {
        this.tokens = t;
    }

    @Override
    public void setSignedHash(byte[] signedHash) throws RemoteException {
        IdenticonGenerator.saveImage(IdenticonGenerator.generateIdenticons(Arrays.toString(signedHash), 500,500),"identicon");
    }
}
