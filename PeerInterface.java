import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedHashMap;

public interface PeerInterface extends Remote {
	
	public ViewData join(String sourceIp, Point dest, HashMap<String, Integer> peerVisited, int newPort)
			throws RemoteException;
	
	public void setInfo(Point id, Region r, HashMap<String, Region> neighbors, int shape) 
			throws RemoteException;
	
	public void removeNeighbor(String key) throws RemoteException;	//change port to IP
	
	public void addNeighbor(String key, Region r) throws RemoteException;  //change port to IP
	
	public ViewData wantoConnect(Point p) throws RemoteException;
	
	public ViewData view(String ipString) throws RemoteException;
	
	public FileInfo insertFile(String name) throws RemoteException;
	
	public FileInfo searchOrInsert(String name, Point p, LinkedHashMap<String, Integer> peerVisited,
								int flag)
			throws RemoteException;
	
	public FileInfo search(String name) throws RemoteException;
	
	public void leave() throws RemoteException;
	
	public void takeover(HashMap<String,String> dht,  HashMap<String,Region> n, 
						 Region leaveRegion)
			throws RemoteException;
	
}
