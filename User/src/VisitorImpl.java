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
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

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
    MixingProxy mixer;

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
                } catch (RemoteException ex) {
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

    public void scanQRCode() throws RemoteException {
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
            System.out.println("logs:" + logs);
            sendCapsule(hash);
        }
    }

    public void scanQRCodeFromGUI() throws RemoteException {
        if(!Objects.equals(QRTextField.getText(), "")){
            int random = Integer.parseInt(QRTextField.getText().split("/")[0]);
            String CF = QRTextField.getText().split("/")[1];
            byte[] hash = QRTextField.getText().split("/")[2].getBytes(StandardCharsets.UTF_8);
            Location l = new Location(random, CF, hash);
            // location maar 1 keer toevoegen
            if (!logs.contains(l)){
                logs.add(l);
            }
            sendCapsule(hash);
        }
        QRTextField.setText("");
    }

    public void sendCapsule(byte[] hash) throws RemoteException {
//        System.out.println("time: " + LocalDateTime.now());
//        System.out.println("token: " + token);
//        System.out.println("hash: " + Arrays.toString(hash));
        Capsule c = new Capsule(token.getRandom(), token.getDay(), hash);
        mixer.sendCapsule(c);
    }

    @Override
    public String getNumber() throws RemoteException {return number;}

    @Override
    public String getName() throws RemoteException {return name;}

    @Override
    public void setToken(int day, int r) throws RemoteException {
        this.token = new Token(day,r);
    }
}
