import com.google.zxing.WriterException;

import javax.management.modelmbean.RequiredModelMBean;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Catering extends Remote {
    public int getBusinessNumber() throws RemoteException;
    public String getName() throws RemoteException;
    public void setSecretKey(byte[] secretKey) throws RemoteException;
    public String getCF() throws RemoteException;
    public String getLocation() throws RemoteException;
    public void setPseudonym(byte[] pseudonym) throws IOException, WriterException;
}
