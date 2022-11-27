import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

public class VisitorLogInUI {
    private JButton logInButton;
    private JPanel rootPanel;
    private JTextField NameTextField;
    private JTextField PhoneTextField;
    private JButton scanQRCodeButton;
    private JLabel PhoneLabel;
    private JLabel NameLabel;

    public VisitorLogInUI(VisitorImpl visitor){
        rootPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        scanQRCodeButton.setVisible(false);

        logInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //                    VisitorImpl.tryLogIn(visitor, NameTextField.getText(), PhoneTextField.getText());
                PhoneTextField.setVisible(false);
                PhoneLabel.setVisible(false);
                NameLabel.setText("QR code");
                NameTextField.setText("");
                logInButton.setVisible(false);
                scanQRCodeButton.setVisible(true);
            }
        });

        scanQRCodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                VisitorImpl.scanQRCodeFromGUI();
            }
        });
        }


        public JPanel getRootPanel(){
            return rootPanel;
        }

}
