import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Random;

/*
 * Class modeling the functionality of Peer 
 */
public class Peer implements PeerInterface {

	String bootstrapIp = "";
	final String bootstrapPort = "55000";
	final int registryPort = 60000;
	
	public static String localhost = "";
	String ip;
	int port, shape; // 0 - square, 1 - rectangle
	Point id;
	Region r;

	HashMap<String, Region> neighbors;
	HashMap<String,String> dht;
	
	Peer() throws RemoteException {
		super();
	}
	
	Peer(Point id, int port, Region r, HashMap<String, Region> neighbors, int shape)
	  throws RemoteException {
		super();
		dht = new HashMap<String,String>();
		try {
			localhost = InetAddress.getLocalHost().getHostAddress(); //TBR
			ip = InetAddress.getLocalHost().getHostAddress();
			this.port = port;
			this.id = id;
			this.r = r;
			this.neighbors = neighbors;	
			this.shape = shape;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return ip + ":" + port;
	}
	
	/*
	 * Starting rmi server, registry binding etc.
	 * remotely exposing self object
	 * 
	 * bindStr eg: 192.168.1.2:50001
	 */
	void init() {		
		try {
			String bindStr = InetAddress.getLocalHost().getHostAddress() + ":" + port;
			PeerInterface PeerStub = 
				(PeerInterface) UnicastRemoteObject.exportObject(this, port);
			Registry registry = LocateRegistry.getRegistry(registryPort);
	        registry.rebind(bindStr, PeerStub);
		} catch (Exception e) {
			System.out.println("Init exception: " + e);
		}
	}
	
	@Override
	/*
	 * Method to set region, id and neighbors of new node
	 */
	public void setInfo(Point id, Region r, HashMap<String, Region> neighbors, int shape)
			throws RemoteException {
		this.id = id;
		this.r = r;
		this.neighbors = neighbors;
		this.shape = shape; //how could i forget this!!
		
		HashMap<String,Integer> updateList = new HashMap();
		for (Entry<String, Region> entry : neighbors.entrySet()) {
			if ( neighbors.containsKey(entry.getKey()) )
				updateList.put(entry.getKey(), 1);
			else 
				updateList.put(entry.getKey(), 0);
		}
		
		propagateUpdate(updateList);
	}
	
	/*
	 * checks if dest point is present with self
	 */
	boolean isDestPresent(Point dest) {
		if ( (dest.x > r.p4.x) && (dest.y > r.p4.y) &&
			 (dest.x < r.p2.x) && (dest.y < r.p2.y))
			return true;
		
		return false;
	}
	
	/*
	 * The join method...
	 */
	public ViewData join(String sourceIp, Point dest, HashMap<String, Integer> peerVisited, int newPort) 
			throws RemoteException {
		if ( isDestPresent(dest) ) {
			return split(sourceIp, dest, newPort);			
		} else {
			double min = 11, dist; // coz min will never exceed 10
			String minNeighbor = null;	
			for (Entry<String, Region> entry : neighbors.entrySet()) {
				//this peer should not already be visited
				if ( peerVisited.isEmpty() || 
						  (peerVisited.get(neighbors.get(entry.getKey())) == null) ) {			
					double xDiff = Math.abs(dest.x-entry.getValue().id.x);					
					xDiff = xDiff*xDiff; 
					double yDiff = Math.abs(dest.y-entry.getValue().id.y);					
					yDiff = yDiff*yDiff;
					
					dist = Math.sqrt(xDiff + yDiff);
					
					if ( dist < min ) {
						min = dist;
						minNeighbor = entry.getKey();						
					}
				}
			}			
			String bindStr = minNeighbor;
			peerVisited.put(bindStr, 1); //change to IP later..
			String[] temp = bindStr.split(":");
			Registry registry = LocateRegistry.getRegistry(temp[0], registryPort);
			PeerInterface pf = null;
			try {
				pf = (PeerInterface) registry.lookup(bindStr);
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return pf.join(sourceIp, dest, peerVisited, newPort);
		}
	}
	
	/*
	 * method to get IP of primary node in CAN and call join on that
	 */
	public ViewData wantoConnect(Point p) {
		String primaryIp;
		String bindStr;		
		try {
			//get IP of primary node from Bootstrap server
			bindStr = bootstrapIp + ":" + bootstrapPort;
			Registry registry = LocateRegistry.getRegistry(bootstrapIp, registryPort);
			BootstrapInterface bs = null;
			bs = (BootstrapInterface) registry.lookup(bindStr);
			primaryIp = bs.connect(ip+":"+port); // ip - ip of peer
						
			if ( primaryIp.equals(ip+":"+port) ) { //join of first node and change port to IP!
				Point id = new Point(5,5);
				Region r = new Region(new Point(0,10), new Point(10,10), 
						              new Point(10,0), new Point(0,0), id, 0);				
				HashMap<String, Region> n = new HashMap<String, Region>();
				this.id = id;
				this.r = r;
				this.neighbors = n;
				this.shape = 0;
				
				ViewData viewDataObj = new ViewData(ip, r, this.port, id, n, null);//self IP 
				return viewDataObj;
				
			} else {
				//call join on primary CAN node
				bindStr = primaryIp;
				String[] temp = primaryIp.split(":");
				registry = LocateRegistry.getRegistry(temp[0], registryPort); //note: CHANGE TO primaryIP
				PeerInterface pf = null;
				pf = (PeerInterface) registry.lookup(bindStr);
				
				Random r = new Random();
				int x = r.nextInt(10) + 1;
				int y = r.nextInt(10) + 1;
				p = new Point(x,y);
				
				HashMap<String, Integer> peerVisited = new HashMap();
				return pf.join(ip, p, peerVisited, this.port);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * wrapper method to invoke any remote method
	 */
	void invokeRemoteMethod(String name, String sourceIp, String methodName,
							HashMap<String, Object> params) {				
		//TBD		
	}
	
	
	/*
	 * allocates and splits zone 
	 */
	ViewData split(String sourceIp, Point dest, int newPort) {		
		if ( shape == 0 ) //square
			return verticalSplit(sourceIp, dest, newPort);
		else 
			return horizontalSplit(sourceIp, dest, newPort);
	}
	
	ViewData verticalSplit(String sourceIp, Point dest, int newPort) {
		Point midUp, midDown, leftId, rightId, newId;
		Point newP1, newP2, newP3, newP4;
		Region newRegion;
		
		//calculate mids
		midDown = calculateMid(r.p3, r.p4);
		midUp = calculateMid(r.p1, r.p2);
		
		//calculate both id's
		leftId = calculateLeftId(midUp, midDown);
		rightId = calculateRightId(midUp, midDown);
		
		if ( dest.x > id.x ) {						
			//create new Region
			newP1 = new Point(midUp.x, midUp.y);
			newP2 = new Point(r.p2.x, r.p2.y);
			newP3 = new Point(r.p3.x, r.p3.y);
			newP4 = new Point(midDown.x, midDown.y);
			newId = rightId;
			newRegion = new Region(newP1, newP2, newP3, newP4, newId, 1);
			
			//modify self region
			r.p2.x = midUp.x;
			r.p2.y = midUp.y;
			r.p3.x = midDown.x;
			r.p3.y = midDown.y;			
			id = leftId;
		} else {
			//new Region
			newP1 = new Point(r.p1.x, r.p1.y);
			newP2 = new Point(midUp.x, midUp.y);
			newP3 = new Point(midDown.x, midDown.y);
			newP4 = new Point(r.p4.x, r.p4.y);
			newId = leftId;
			newRegion = new Region(newP1, newP2, newP3, newP4, newId, 1);
			
			r.p1.x = midUp.x;
			r.p1.y = midUp.y;
			r.p4.x = midDown.x;
			r.p4.y = midDown.y;
			id = rightId;
		}
				
		return updateNeighbors(sourceIp, newRegion, newId, 1, newPort);
	}
	
	ViewData horizontalSplit(String sourceIp, Point dest, int newPort) {
		Point midLeft, midRight, upperId, lowerId, newId;
		Point newP1, newP2, newP3, newP4;
		Region newRegion;
		
		//calculate mids
		midLeft = calculateMid(r.p1, r.p4);
		midRight = calculateMid(r.p2, r.p3);
		
		//calculate both id's
		upperId = calculateUpperId(midLeft, midRight);
		lowerId = calculateLowerId(midLeft, midRight);
		
		if ( dest.y > id.y ) {						
			//create new Region
			newP1 = new Point(r.p1.x, r.p1.y);
			newP2 = new Point(r.p2.x, r.p2.y);
			newP3 = new Point(midRight.x, midRight.y);
			newP4 = new Point(midLeft.x, midLeft.y);
			newId = upperId;
			newRegion = new Region(newP1, newP2, newP3, newP4, newId, 0);
			
			//modify self region
			r.p1.x = midLeft.x;
			r.p1.y = midLeft.y;			
			r.p2.x = midRight.x;
			r.p2.y = midRight.y;
			id = lowerId;			
		} else {
			//new Region
			newP1 = new Point(midLeft.x, midLeft.y);
			newP2 = new Point(midRight.x, midRight.y);
			newP3 = new Point(r.p3.x, r.p3.y);
			newP4 = new Point(r.p4.x, r.p4.y);
			newId = lowerId;
			newRegion = new Region(newP1, newP2, newP3, newP4, newId, 0);
			
			r.p3.x = midRight.x;
			r.p3.y = midRight.y;
			r.p4.x = midLeft.x;
			r.p4.y = midLeft.y;
			id = upperId;
		}		
		
		return updateNeighbors(sourceIp, newRegion, newId, 0, newPort);
	}
	
	/*
	 * update neighbors of self and return those of new node
	 */
	ViewData updateNeighbors(String sourceIp, Region newRegion, Point newId, int shape, int newPort) {
		HashMap<String, Region> selfNeighbors = new HashMap<String, Region>();
		//update self neighbors
		ArrayList<Point> points = new ArrayList<Point>();
		
		//1 - present , 0 - removed
		HashMap<String, Integer> removeNeighbors = new HashMap(); // change to <String, Region> as it should be IP not port 
		
		points.add(new Point(r.p1.x + 0.01, r.p1.y));
		points.add(new Point(r.p1.x, r.p1.y - 0.01));
		
		points.add(new Point(r.p2.x - 0.01, r.p2.y));
		points.add(new Point(r.p2.x, r.p2.y - 0.01));
		
		points.add(new Point(r.p3.x, r.p3.y + 0.01));
		points.add(new Point(r.p3.x - 0.01, r.p1.y));
		
		points.add(new Point(r.p4.x + 0.01, r.p4.y));
		points.add(new Point(r.p4.x, r.p4.y + 0.01));
		
		for (Entry<String, Region> entry : neighbors.entrySet()) {
			double[] yLines = new double[2];
			double[] xLines = new double[2];
			
			//of neighbors
			yLines[0] = entry.getValue().p1.y; //or p2
			yLines[1] = entry.getValue().p3.y; //or p4
			
			xLines[0] = entry.getValue().p1.x; //or p4
			xLines[1] = entry.getValue().p3.x; // or p2
			
			for ( int i = 0 ; i < points.size() ; i++ ) {				
				if ( yLines[0] == points.get(i).y ) {
					if ( (points.get(i).x > entry.getValue().p1.x) &&
						 (points.get(i).x < entry.getValue().p2.x)) {
						selfNeighbors.put(entry.getKey(), entry.getValue());
					}					
				} else if ( yLines[1] == points.get(i).y ) {
					if ( (points.get(i).x > entry.getValue().p4.x) && 
						 (points.get(i).x < entry.getValue().p3.x) ) {
						selfNeighbors.put(entry.getKey(), entry.getValue());
					}
				} else if (xLines[0] == points.get(i).x) {
					if ( (points.get(i).y < entry.getValue().p1.y) &&
						 (points.get(i).y > entry.getValue().p4.y) ) {
						selfNeighbors.put(entry.getKey(), entry.getValue());
					}
					
				} else if (xLines[1] == points.get(i).x) {
					if ( (points.get(i).y < entry.getValue().p2.y) &&
						 (points.get(i).y > entry.getValue().p3.y) ) {
						selfNeighbors.put(entry.getKey(), entry.getValue());	
					}
				}
			}			
		}
		
		//mark neighbors which are not in selfNeighbors
		for (Entry<String, Region> entry : neighbors.entrySet()) {
			if ( selfNeighbors.containsKey(entry.getKey()) )
				removeNeighbors.put(entry.getKey(), 1);
			else 
				removeNeighbors.put(entry.getKey(), 0);
		}
		
		//adding new node as a neighbor
		selfNeighbors.put(sourceIp+":"+newPort, newRegion); //change port to IP				
		
		//update new nodes neightbors
		HashMap<String, Region> newNeighbors = new HashMap();
		points.clear(); // clear list before adding 
				
		points.add(new Point(newRegion.p1.x + 0.01, newRegion.p1.y));
		points.add(new Point(newRegion.p1.x, newRegion.p1.y - 0.01));
		
		points.add(new Point(newRegion.p2.x - 0.01, newRegion.p2.y));
		points.add(new Point(newRegion.p2.x, newRegion.p2.y - 0.01));
		
		points.add(new Point(newRegion.p3.x, newRegion.p3.y + 0.01));
		points.add(new Point(newRegion.p3.x - 0.01, newRegion.p1.y));
		
		points.add(new Point(newRegion.p4.x + 0.01, newRegion.p4.y));
		points.add(new Point(newRegion.p4.x, newRegion.p4.y + 0.01));
		
		for (Entry<String, Region> entry : neighbors.entrySet()) {
			double[] yLines = new double[2];
			double[] xLines = new double[2];
			
			//of neighbors
			yLines[0] = entry.getValue().p1.y; //or p2
			yLines[1] = entry.getValue().p3.y; //or p4
			
			xLines[0] = entry.getValue().p1.x; //or p4
			xLines[1] = entry.getValue().p3.x; // or p2
			
			for ( int i = 0 ; i < points.size() ; i++ ) {				
				if ( yLines[0] == points.get(i).y ) {
					if ( (points.get(i).x > entry.getValue().p1.x) &&
						 (points.get(i).x < entry.getValue().p2.x)) {
						newNeighbors.put(entry.getKey(), entry.getValue());
					}					
				} else if ( yLines[1] == points.get(i).y ) {
					if ( (points.get(i).x > entry.getValue().p4.x) && 
						 (points.get(i).x < entry.getValue().p3.x) ) {
						newNeighbors.put(entry.getKey(), entry.getValue());
					}
				} else if (xLines[0] == points.get(i).x) {
					if ( (points.get(i).y < entry.getValue().p1.y) &&
						 (points.get(i).y > entry.getValue().p4.y) ) {
						newNeighbors.put(entry.getKey(), entry.getValue());
					}
					
				} else if (xLines[1] == points.get(i).x) {
					if ( (points.get(i).y < entry.getValue().p2.y) &&
						 (points.get(i).y > entry.getValue().p3.y) ) {
						newNeighbors.put(entry.getKey(), entry.getValue());	
					}
				}
			}			
		}
		//self node added as neighbor to new node
		newNeighbors.put(this.ip+":"+port, r);
		
		//overright neighbors HashMap now
		neighbors.clear();
		for (Entry<String, Region> entry : selfNeighbors.entrySet()) {
			neighbors.put(entry.getKey(), entry.getValue());
		}
		this.shape = shape; //update self shape too
		
		String bindStr = sourceIp + ":" + newPort;//CHANGE!!!		
		try {
			Registry registry = LocateRegistry.getRegistry(sourceIp, registryPort);
			PeerInterface pf = null;
			pf = (PeerInterface) registry.lookup(bindStr);
			pf.setInfo(newId, newRegion, newNeighbors, shape);
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}		
					
		propagateUpdate(removeNeighbors);
			
		ViewData returnObj = new ViewData(sourceIp, newRegion, newPort, newId, newNeighbors, null);
		return returnObj;
	}
	
	/*
	 * asks neighbors to update their neighbor lists
	 * 
	 * change firstparam to <String, Integer>
	 */
	void propagateUpdate(HashMap<String,Integer> updateList) {
		for (Entry<String, Integer> entry : updateList.entrySet()) {			
			String bindStr = entry.getKey();		
			try {
				String[] temp = bindStr.split(":");
				Registry registry = LocateRegistry.getRegistry(temp[0], registryPort);
				PeerInterface pf = null;
				pf = (PeerInterface) registry.lookup(bindStr);				
			
				if ( entry.getValue() == 1 ) {
					pf.addNeighbor(ip+":"+port, r); 						//Change port to iP
				} else {
					pf.removeNeighbor(ip+":"+port);			//Change port to iP
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
	
	/*
	 * method to return ViewData of given IP
	 */
	public ViewData view(String ipString) throws RemoteException {
		if ( ipString.equals(this.ip+":"+this.port)) {
			return (new ViewData(ip,r,port,id,neighbors, dht));
		}
		else {
			String bindStr = ipString;		
			try {
				String[] temp = bindStr.split(":");
				Registry registry = LocateRegistry.getRegistry(temp[0], registryPort);
				PeerInterface pf = null;
				pf = (PeerInterface) registry.lookup(bindStr);				
				return pf.view(ipString);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return null;			
	}
	
	/*
	 * adds neighbor
	 */
	public void addNeighbor(String key, Region r) {
		neighbors.put(key, r); //change port to IP		
	}
	
	/*
	 * removes from neighbor
	 */
	public void removeNeighbor(String key) {
	    neighbors.remove(key); //change port to IP		
	}
		
	/*
	 * Calculates id in upper half
	 */
	Point calculateUpperId(Point left, Point right) {
		Point x,y;
		
		x = calculateMid(left,right);
		y = calculateMid(r.p1,left);
		
		return (new Point(x.x, y.y));
	}
	
	/*
	 * Calcualtes id of lower half
	 */
	Point calculateLowerId(Point left, Point right) {
		Point x,y;
		
 		x = calculateMid(left,right);
		y = calculateMid(r.p4,left);
		
		return (new Point(x.x, y.y));
	}
	
	/*
	 * Calculate id in left half
	 */
	Point calculateLeftId(Point up, Point down) {
		Point x,y;
		
		x = calculateMid(down, r.p4);
		y = calculateMid(r.p1, r.p4);
		
		return (new Point(x.x , y.y)); 
	}
	
	/*
	 * Calculate id in left half
	 */
	Point calculateRightId(Point up, Point down) {
		Point x,y;
		
		x = calculateMid(down, r.p3);
		y = calculateMid(r.p2, r.p3);
		
		return (new Point(x.x , y.y));
	}
	
	/*
	 * Calculates and returns midpoint of 2 Points
	 */
	Point calculateMid( Point p1, Point p2) {		
		double x = (p1.x + p2.x)/2;
		double y = (p1.y + p2.y)/2;
		
		return (new Point(x,y));
	}
	
	/*
	 * displays neighbor lists
	 */
	void displayNeighbors() {		
		for (Entry<String, Region> entry : neighbors.entrySet() ) {
			System.out.println(entry.getKey());
		}
	}
	
	public void dispPeer() {
		System.out.println("Peer" + port);
		System.out.println("Peer id:" + id);
		System.out.println("Region: " + r);
		System.out.println("Neighbors: ");
		displayNeighbors();
		System.out.println("DHT: ");
		for ( Entry<String,String> entry : dht.entrySet() )
			System.out.println(entry.getKey());
	}
	
	/*
	 * Search file
	 */
	public FileInfo search(String name) {
		int flag = 0; // 0 - search
		double x, y;
		x = xHash(name) + 0.01;
		y = yHash(name) + 0.01;
				
		Point p = new Point(x,y);
		LinkedHashMap<String, Integer> peerVisited = new LinkedHashMap();
		peerVisited.put(ip+":"+port, 1);
		return searchOrInsert(name, p, peerVisited, flag);
	}
	
	/*
	 * to insert file, calls routeAndInsert
	 */
	public FileInfo insertFile(String name) {
		int flag = 1; // 1 - insert
		double x, y;
		x = xHash(name) + 0.01;
		y = yHash(name) + 0.01;
				
		Point p = new Point(x,y);		
		//order of pushed elements is important
		LinkedHashMap<String, Integer> peerVisited = new LinkedHashMap();
		peerVisited.put(ip+":"+port, 1);
		return searchOrInsert(name, p, peerVisited, flag);		
	}
	
	/*
	 *  routes and inserts file
	 */
	public FileInfo searchOrInsert(String name, Point p, LinkedHashMap<String, Integer> peerVisited,
							   int flag) {
		if ( isDestPresent(p) ) {
			if ( flag == 1 ) {
				return insertIntoDht(name, peerVisited);
			}				
			else {
				return returFromDht(name, peerVisited);
			}				
		} else {
			double min = 11, dist; // coz min will never exceed 10
			String minNeighbor = null;	
			for (Entry<String, Region> entry : neighbors.entrySet()) {
				//this peer should not already be visited
				String key = entry.getKey();
				boolean val1 = peerVisited.isEmpty(); 
				boolean val2 = (peerVisited.containsKey(key));				
				if ( val1 || !val2) {
					double xDiff = Math.abs(p.x-entry.getValue().id.x);					
					xDiff = xDiff*xDiff; 
					double yDiff = Math.abs(p.y-entry.getValue().id.y);					
					yDiff = yDiff*yDiff;
					
					dist = Math.sqrt(xDiff + yDiff);
					
					if ( dist < min ) {
						min = dist;
						minNeighbor = entry.getKey();				
					}
				}
			}
			
			String bindStr = minNeighbor;//change to minNeighbor + ":" + port where port is predefined const
			peerVisited.put(bindStr, 1); //change to IP later..
			
			PeerInterface pf = null;
			try {
				String[] temp = bindStr.split(":");
				Registry registry = LocateRegistry.getRegistry(temp[0], registryPort);
				pf = (PeerInterface) registry.lookup(bindStr);
				return pf.searchOrInsert(name, p, peerVisited, flag);
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		return null; //should change ??
	}
	
	/*
	 * Actually adds to the DHT and return appropriate info 
	 */
	FileInfo insertIntoDht(String name, LinkedHashMap<String, Integer> peerVisited) {
		FileInfo f;
		dht.put(name, name);
		peerVisited.put(ip+":"+port, 1);
		f = new FileInfo(true, peerVisited);
		return f;
	}
	
	/*
	 * Peer having file found, now return appropriate info
	 */
	FileInfo returFromDht(String name, LinkedHashMap<String, Integer> peerVisited) {
		FileInfo f;
		boolean found;
		
		if ( dht.get(name) != null ) {
			found = true;
		} else {
			found = false;
		}
		
		peerVisited.put(ip+":"+port, 1);
		f = new FileInfo(found, peerVisited);
		return f;
	}
	
	/*
	 * calculates x co-ordinate
	 */
	int xHash(String name) {
		int sum = 0, x;
		for ( int i = 1 ; i < name.length() ; i = i + 2 ) {
			sum = sum + (int)name.charAt(i);
		}
		return sum%10;
	}
	
	/*
	 * calculates y co-ordinate
	 */
	int yHash(String name) {
		int sum = 0;
		for ( int i = 0 ; i < name.length() ; i = i + 2 ) {
			sum = sum + (int)name.charAt(i);
		}
		return sum%10;
	}
	
	/*
	 * method for node leave
	 */
	public void leave() {
		//find a neighbor with same shape
		String zone = null;
		for ( Entry<String,Region> entry : neighbors.entrySet() ) {
			if ( entry.getValue().shape == this.shape) {
				zone = entry.getKey();
				break;
			}	
		}
		
		String bindStr = zone;
		PeerInterface pf = null;
		try {
			String[] temp = bindStr.split(":");
			Registry registry = LocateRegistry.getRegistry(temp[0], registryPort); //IP of remote machine
			pf = (PeerInterface) registry.lookup(bindStr);
			pf.takeover(this.dht, this.neighbors, this.r);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		updateSelfNeighborsOnLeave();
		
	}
	
	/*
	 * transfers all dht values during peer leave and since this is the 
	 * context of the peer which acts as a takeover node, it also updates its 
	 * neighbors list by performing union like operation.
	 * 
	 * This peer also updates its own region
	 */
	public void takeover(HashMap<String,String> dht, HashMap<String,Region> n, 
			             Region leaveRegion) {
		 // list of peers to be informed of neighbor addition
		HashMap<String, Region> diff = new HashMap();
		
		//copy dht
		for ( Entry<String,String> entry : dht.entrySet() ) {
			this.dht.put(entry.getKey(), entry.getValue());
		}
		
		//union of self and departing peers neighbors list
		for ( Entry<String,Region> entry : n.entrySet() ) {
			String key = entry.getKey(); 
			if ( (!key.equals(this.ip+":"+this.port)) &&
					!neighbors.containsKey(entry.getKey()) ) {
				diff.put(entry.getKey(), entry.getValue());			
				neighbors.put(entry.getKey(), entry.getValue());
			}
		}
		
		//infrom peers to update their neighbors
		for ( Entry<String,Region> entry : diff.entrySet() ) {
			String bindStr = entry.getKey();
			PeerInterface pf = null;
			try {
				String[] temp = bindStr.split(":");
				Registry registry = LocateRegistry.getRegistry(temp[0], registryPort); //IP of remote machine
				pf = (PeerInterface) registry.lookup(bindStr);
				pf.addNeighbor(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//update self region
		//CASE1: vertical departure
		if ( (leaveRegion.p1.equals(r.p2) ) && (leaveRegion.p4.equals(r.p3))) {
			r.p2 = leaveRegion.p2;
			r.p3 = leaveRegion.p3;
		} else if ( (leaveRegion.p2.isEqual(r.p1)) && (leaveRegion.p3.isEqual(r.p4)) ) {
			r.p1 = leaveRegion.p1;
			r.p4 = leaveRegion.p4;
		} else if ( (leaveRegion.p3.isEqual(r.p2)) && (leaveRegion.p4.isEqual(r.p1)) ) { //CASE2: horizontal leave
			r.p1 = leaveRegion.p1;
			r.p2 = leaveRegion.p2;
		} else if ( ( leaveRegion.p1.isEqual(r.p4)) && (leaveRegion.p2.isEqual(r.p3)) ) {
			r.p3 = leaveRegion.p3;
			r.p4 = leaveRegion.p4;
		}
		
		//recalculate self ID point
		Point x = calculateMid(r.p1,r.p2);
		Point y = calculateMid(r.p1,r.p4);
		
		id.x = x.x;
		id.y = y.y;
	}
	
	
	/*
	 * infroms neighbors of itself of its departure
	 */
	void updateSelfNeighborsOnLeave() {
		for ( Entry<String,Region> entry : neighbors.entrySet() ) {
			String bindStr = entry.getKey();
			PeerInterface pf = null;
			try {
				String[] temp = bindStr.split(":");
				Registry registry = LocateRegistry.getRegistry(temp[0], registryPort); //IP of remote machine
				pf = (PeerInterface) registry.lookup(bindStr);
				pf.removeNeighbor(this.ip+this.port);//remove itself from neighbors hashtable				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//clear self neightbors list and dht
		neighbors.clear();
		dht.clear();
	}
	
	/*
	 * main method.
	 */
	public static void main(String args[]) throws UnknownHostException {		
		try {
			if ( args.length != 1) {
				System.out.println("Enter bootstrap ip as arg");
				return;
			}
				
			Peer peer1 = new Peer(null, 50001, null, null, -1);
			peer1.bootstrapIp = args[0];
			peer1.init();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}