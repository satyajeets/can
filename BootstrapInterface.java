import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BootstrapInterface extends Remote {
	public String connect(String sourceIp) throws RemoteException;
}
