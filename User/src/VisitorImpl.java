import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.*;
import java.util.List;
import java.util.Timer;

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
    JPanel panel = new JPanel();
    JPanel panel2 = new JPanel();
    JTextField NameTextField = new JTextField(30);
    JTextField PhoneTextField = new JTextField(30);
    JTextField QRTextField = new JTextField(30);
    JButton logInButton = new JButton("Log in");
    JButton scanQRCodeButton = new JButton("Scan QR code");
    JButton releaseLogs = new JButton("Release Logs to Practioner");
    JButton logOutButton = new JButton("Log out");
    JLabel PhoneLabel = new JLabel("Unique phone number");
    JLabel NameLabel = new JLabel("Name");
    JLabel IntroLabel = new JLabel("Welkom new visitor");
    JLabel QRLabel = new JLabel("QR code");
    JLabel ImageLabel = new JLabel();

    public VisitorImpl() throws RemoteException {
        setFrame();

        try {
            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registrar = (Registrar) myRegistry.lookup("Registrar");


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
        frame.setPreferredSize(new Dimension(300, 500));
        frame.setLayout(new BorderLayout());

        IntroLabel.setVisible(true);
        frame.getContentPane().add(IntroLabel, BorderLayout.PAGE_START);
        panel.setLayout(new GridLayout(2,2));
        panel.add(NameLabel);
        panel.add(NameTextField);
        panel.add(PhoneLabel);
        panel.add(PhoneTextField);
        frame.add(panel);
        frame.setSize(600,400);
        NameTextField.setEditable(true);
        PhoneTextField.setEditable(true);
        frame.getContentPane().add(logInButton, BorderLayout.PAGE_END);

        frame.pack();
        frame.setLocationRelativeTo(null); // center

        frame.setVisible(true);
        frame.pack();

        logInButton.addActionListener(new ActionListener(){    //add an event and take action
            public void actionPerformed(ActionEvent e){
                try {
                    tryLogIn();
                } catch (RemoteException | SignatureException | InvalidKeyException ex) {
                    ex.printStackTrace();
                }
            }
        });
        scanQRCodeButton.addActionListener(new ActionListener(){    //add an event and take action
            public void actionPerformed(ActionEvent e){
                try {
                    scanQRCodeFromGUI();
                } catch (SignatureException | InvalidKeyException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        releaseLogs.addActionListener(new ActionListener(){    //add an event and take action
            public void actionPerformed(ActionEvent e){
                try {
                    releaseLogs(locationlogs);
                } catch (IOException | SignatureException | InvalidKeyException ex) {
                    ex.printStackTrace();
                }
            }
        });
        logOutButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                QRLabel.setVisible(false);
                QRTextField.setVisible(false);
                logOutButton.setVisible(false);
                scanQRCodeButton.setVisible(false);
                ImageLabel.setText("Close window");
                leaveLocation();
            }
        });

    }


    public static void main(String[] args) throws RemoteException {
        System.setProperty("javax.net.ssl.trustStore","truststore");
        System.setProperty("javax.net.ssl.trustStorePassword","trustword");

//        Scanner sc = new Scanner(System.in);
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

    private void tryLogIn() throws RemoteException, SignatureException, InvalidKeyException {
        if(!Objects.equals(NameTextField, "") && !Objects.equals(PhoneTextField, "")){
            this.name = NameTextField.getText();
            this.number = PhoneTextField.getText();
            registrar.register(this);
            panel.remove(NameLabel);
            panel.remove(NameTextField);
            panel.remove(PhoneTextField);
            panel.remove(PhoneLabel);
            frame.remove(NameLabel);
            frame.remove(NameTextField);
            frame.remove(PhoneTextField);
            frame.remove(PhoneLabel);
            frame.remove(logInButton);
            frame.pack();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            panel.add(QRLabel);
            JPanel p = new JPanel();
            p.setPreferredSize(new Dimension(300,30));
            p.setMaximumSize(new Dimension(300,30));
            p.add(QRTextField);
            panel.add(p);
            JPanel p2 = new JPanel();
            p2.setPreferredSize(new Dimension(300,300));
            p2.setMaximumSize(new Dimension(300,300));
            p2.add(ImageLabel);
            ImageLabel.setText("Hier komt bewijs!");
            panel.add(p2);
            panel2.setLayout(new GridLayout(2,1));
            panel2.add(scanQRCodeButton);
            panel2.add(releaseLogs);
            panel2.add(logOutButton);
            frame.add(panel2, BorderLayout.PAGE_END);
            frame.pack();
        }
    }

    public void scanQRCode() throws IOException, SignatureException, InvalidKeyException {
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

    public void scanQRCodeFromGUI() throws IOException, SignatureException, InvalidKeyException {
        if(!Objects.equals(QRTextField.getText(), "")){
            int random = Integer.parseInt(QRTextField.getText().split("\\|")[0]);
            String CF = QRTextField.getText().split("\\|")[1];
            locationhash = Base64.getDecoder().decode(QRTextField.getText().split("\\|")[2]);
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

    public void sendCapsule(byte[] hash, Token token, int random, String CF) throws IOException, SignatureException, InvalidKeyException {
//        System.out.println("time: " + LocalDateTime.now());
//        System.out.println("token: " + token);
//        System.out.println("hash: " + Arrays.toString(hash));

        Capsule c = new Capsule(token, hash);
        setSignedHash(mixer.sendCapsule(c));
        //elk halfuur nieuwe token sturen naar mixer
        t = new Timer();
        t.scheduleAtFixedRate(new UpdateTokens(this, random, CF),30*60*1000, 30*60*1000);
    }

    public void sendUpdateCapsule(int random, String CF) throws SignatureException, RemoteException, InvalidKeyException {
        Token token = tokens.remove(0);
        Location l = new Location(random, CF, locationhash,token);
        locationlogs.add(l);
        Capsule c = new Capsule(token, locationhash);
        mixer.sendCapsule(c);
    }

    public void releaseLogs(List<Location> locationlogs) throws IOException, SignatureException, InvalidKeyException {
        practitioner.getLogs(locationlogs);
    }

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
    public void setSignedHash(byte[] signedHash) throws IOException {
        IdenticonGenerator.saveImage(IdenticonGenerator.generateIdenticons(Arrays.toString(signedHash), 200,200),"identicon");
        ImageLabel.setIcon(new ImageIcon("identicon.png"));
        ImageLabel.setText("");
    }

}
