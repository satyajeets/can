import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

public class ViewData implements Serializable {
	String ip;
	Region r;
	int port;
	Point id;
	HashMap<String, Region> neighbors;
	HashMap<String,String> dht;
	
	ViewData(String ip, Region r, int port, Point id, 
			HashMap<String, Region> neighbors, HashMap<String,String> dht) {
		this.ip = ip;
		this.r = r;
		this.port = port;
		this.id = id;
		this.neighbors = neighbors;
		this.dht = dht;
	}
	
	ViewData() {}
	
	public void display() {
		System.out.println("IP: " + ip);
		System.out.println("Region Co-ordinates: " + r);
		System.out.println("Port: " + port);
		System.out.println("ID: " + id);
		System.out.println("Neighbors are: ");		
		for ( Entry<String,Region> entry : neighbors.entrySet()) {
			System.out.println(entry.getKey());
		}
		
		System.out.println();
		System.out.println("DHT: ");
		
		if ( dht != null ) {
			for ( Entry<String,String> entry : dht.entrySet()) {
				System.out.println(entry.getKey() + " : " + entry.getValue());
			}
		}
	}
}

class Point implements Serializable {
	double x,y;
	
	Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	void setX(double x) {
		this.x = x;
	}
	
	void setY(double y) {
		this.y = y;
	}
	
	double getX() {
		return this.x;
	}
	
	double getY() {
		return this.y;
	}
	
	boolean isEqual(Point p) {
		if ( (x == p.x) && (y == p.y) )
			return true;
		else 
			return false;
	}
	
	public String toString() {
		String s = "(" + x + "," + y + ")";
		return s;
	}
}

class Region implements Serializable {
	Point p1, p2, p3, p4, id;
	int shape;
	
	Region() {
		p1 = p2 = p3 = p4 = null;
	}
	
	Region(Point p1, Point p2, Point p3, Point p4, Point id, int shape) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		this.p4 = p4;
		this.id = id;
		this.shape = shape;
	}
	
	public String toString() {
		String s = "P1" + p1 + " P2" + p2 + " P3" + p3 + " P4" + p4;
		return s;
	}
}
