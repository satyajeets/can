import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PeerClient {
	
	String localhost;
	final int port = 50001;
	final int registryPort = 60000;
	
	PeerClient() {
		try {
			localhost = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * for node joining
	 */
	void insertPeerIntoCan() {
		ViewData v;
		String bindStr = localhost + ":" + port;
		try { 			
			Registry registry = LocateRegistry.getRegistry(registryPort);
			PeerInterface pf = null;
			pf = (PeerInterface) registry.lookup(bindStr);
			v = pf.wantoConnect(new Point(7,8)); //override p inside wanto
			v.display();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	/*
	 * display info of that peer
	 */
	void viewInfo(String ip) {
		String bindStr = localhost + ":" + port;
		try {
			Registry registry = LocateRegistry.getRegistry(registryPort);
			PeerInterface pf = null;
			pf = (PeerInterface) registry.lookup(bindStr);
			ViewData obj = pf.view(ip);
			obj.display();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	/*
	 * Inserts a file in CAN from this peer
	 */
	void insert(String name) {
		FileInfo f;
		String bindStr = localhost + ":" + port;
		try { 			
			Registry registry = LocateRegistry.getRegistry(registryPort);
			PeerInterface pf = null;
			pf = (PeerInterface) registry.lookup(bindStr);
			f = pf.insertFile(name);
			f.displayInsert();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	/*
	 * Search for file in CAN from this peer
	 */
	void search(String name) {
		FileInfo f;
		String bindStr = localhost + ":" + port;
		try { 			
			Registry registry = LocateRegistry.getRegistry(registryPort);
			PeerInterface pf = null;
			pf = (PeerInterface) registry.lookup(bindStr);
			f = pf.search(name);
			f.displaySearch();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	/*
	 * Peer on whoes machine this is invoked leaves
	 */
	void leave() {
		String bindStr = localhost + ":" + port;
		try { 			
			Registry registry = LocateRegistry.getRegistry(registryPort);
			PeerInterface pf = null;
			pf = (PeerInterface) registry.lookup(bindStr);
			pf.leave();
		} catch (Exception e) {
			System.out.println(e);
		}	
	}
	
	public static void main(String args[]) {
		String input;
		
		input = args[0];
		PeerClient pc = new PeerClient();				
				
		switch (input) {
			case "join":
				pc.insertPeerIntoCan();
				break;
				
			case "view":
				if ( args.length == 2 )
					pc.viewInfo(args[1]);
				else 
					pc.viewInfo(pc.localhost+":"+pc.port);
				break;
				
			case "insert":
				if (args.length != 2) {
					System.out.println("Incorrect invocation. Refer README");
					return;
				}				
				pc.insert(args[1]);
				break;
				
			case "search":
				if (args.length != 2) {
					System.out.println("Incorrect invocation. Refer README");
					return;
				}
				pc.search(args[1]);
				break;
				
			case "leave":
				pc.leave();
				break;
				
			default:				
				System.out.println("Incorrect invocation. Refer README");
				return;
			
		}
	}
}