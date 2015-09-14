import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Bootstrap implements BootstrapInterface {

	final int port = 55000;
	final int registryPort = 60000;
	public String p1;
	boolean firstPresent = false;

	Bootstrap() {
		try {
			p1 = InetAddress.getLocalHost().getHostAddress(); // later take this from command line
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public String connect(String ipStr) throws RemoteException {
		if ( !firstPresent ) {
			p1 = ipStr;
			firstPresent = true;
			return ipStr; //change to return sourceIp
		} else {
			return p1;
		}
	}

	void init() {		
		try {
			String bindStr = InetAddress.getLocalHost().getHostAddress() + ":" + port;
			BootstrapInterface bsStub = 
				(BootstrapInterface) UnicastRemoteObject.exportObject(this, port);
			Registry registry = LocateRegistry.getRegistry(registryPort);
	        registry.rebind(bindStr, bsStub);
		} catch (Exception e) {
			System.out.println("Init exception: " + e);
		}
	}
	
	public static void main(String args[]) {
		Bootstrap bs = new Bootstrap(); //args[0] = p1 IP
		bs.init();
	}
}
