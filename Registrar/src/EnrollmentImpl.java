import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class EnrollmentImpl extends UnicastRemoteObject implements Enrollment{


    protected EnrollmentImpl() throws RemoteException {

    }

    @Override
    public String helloTo(String name) throws RemoteException {
        System.err.println(name + " is trying to contact!");
        return "Server says hello to " + name;
    }

    @Override
    public void getSecretKey() throws RemoteException {

    }

    @Override
    public void getPseudonym() throws RemoteException {

    }
}
