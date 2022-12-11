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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class VisitorImpl extends UnicastRemoteObject implements Visitor {
    Registrar registrar;
    Practitioner practitioner;
    MatchingService matcher;
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
    //lijst met uitgegeven tokens, mss niet nodig!
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
    JButton close = new JButton("Close");
    JLabel PhoneLabel = new JLabel("Unique phone number");
    JLabel NameLabel = new JLabel("Name");
    JLabel IntroLabel = new JLabel("Welkom new visitor");
    JLabel QRLabel = new JLabel("QR code");
    JLabel ImageLabel = new JLabel();
    JTextArea infectedText = new JTextArea();

    public VisitorImpl() throws RemoteException {
        setFrame();

        try {
            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registrar = (Registrar) myRegistry.lookup("Registrar");


            matcher = (MatchingService) myRegistry.lookup("Matcher");

            practitioner = (Practitioner) myRegistry.lookup("Practitioner");

            Registry registryMixing = LocateRegistry.getRegistry("localhost", 2019,
                    new SslRMIClientSocketFactory());
            mixer = (MixingProxy) registryMixing.lookup("MixingProxy");
            mixer.register(this);

            //timer die logs verwijdert na 2 dagen (checkt wel elke dag)
            new Timer().scheduleAtFixedRate(new RemoveLogs(this), 0, 24*60*60*1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws RemoteException {
        System.setProperty("javax.net.ssl.trustStore","truststore");
        System.setProperty("javax.net.ssl.trustStorePassword","trustword");

        Scanner sc = new Scanner(System.in);
        VisitorImpl visitor = new VisitorImpl();
        visitor.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        visitor.frame.setVisible(true);

        int i = 0;
        while(true){
            System.out.println("1. haal kritische capsules op");
            System.out.println("Enter your choice");
            i = sc.nextInt();
            switch(i){
                case 1:
                    visitor.fetchCriticalCapsules();
                    break;
            }
        }
    }

    private void setFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(300, 500));
        frame.setLayout(new BorderLayout());
        infectedText.setVisible(false);
        IntroLabel.setVisible(true);
        close.setVisible(false);
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
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                infectedText.setVisible(false);
                close.setVisible(false);
            }
        });
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
                releaseLogs.setVisible(false);
                ImageLabel.setText("Close window");
                try {
                    leaveLocation();
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

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

            panel2.add(infectedText);
            panel2.add(close);
            panel2.add(scanQRCodeButton);
            panel2.add(releaseLogs);
            panel2.add(logOutButton);
            frame.add(panel2, BorderLayout.PAGE_END);
            frame.pack();
        }
    }

    public void scanQRCode() throws IOException, SignatureException, InvalidKeyException {
        //TODO niet meer in gebruik normaal
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
        ImageLabel.setText("");
        ImageLabel.setIcon(null);
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
        practitioner.getLogs(locationlogs, this.name);
    }

    public void leaveLocation() throws RemoteException {
        onlocation = false;
        IntroLabel.setText("");
        ImageLabel.setIcon(null);
        System.out.println("left location");
        if (t != null)
            t.cancel();
        registrar.leaveLocation(this.number);
    }

    //haal critische tuples op en check ze tegen eigen logs
    public void fetchCriticalCapsules() throws RemoteException {
        List<Capsule> critical = matcher.getCritical();
        for(Capsule c : critical){
            for(Location l : locationlogs){
                if(Arrays.equals(c.hash, l.hash) && overlap(c.date, l.date)){
                    System.out.println("Persoon loopt risico!");
                    mixer.forwardConfirmedToken(l.token);
                    break;
                }
            }
        }
    }

    public boolean overlap(LocalDateTime d1, LocalDateTime d2){
        long overlap = Math.max(0, Math.min(d1.plusMinutes(30).toEpochSecond(ZoneOffset.MIN),d2.plusMinutes(30).toEpochSecond(ZoneOffset.MIN))-Math.max(d1.toEpochSecond(ZoneOffset.MIN),d2.toEpochSecond(ZoneOffset.MIN))+1);
        System.out.println(overlap);
        return overlap > 0;
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

    @Override
    //TODO op gui melding geven van at risk
    public void notifyAtRisk() throws RemoteException {
        System.out.println("registrar zegt dat deze visitor risico loopt!");
        infectedText.setVisible(true);
        close.setVisible(true);
        infectedText.setBackground(Color.RED);
        infectedText.setText("Je loopt risico op besmetting!");
    }

}
