import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class FileInfo implements Serializable {
	boolean success;
	HashMap<String, Integer> peerVisited;
	
	FileInfo(boolean s, HashMap<String, Integer> p) {
		success = s;
		peerVisited = p;
	}
	
	void displayInsert() {
		
		String last = null;
		
		for ( Entry<String, Integer> entry : peerVisited.entrySet())
			last = entry.getKey();
					
		if (success)
			System.out.println("File successfully added to " + last);
		else {
			System.out.println("Failed to add");
			return;
		}
		
		for ( Entry<String, Integer> entry : peerVisited.entrySet() ) {			
			System.out.print(entry.getKey() + "->" );
		}
	}
	
	void displaySearch() {
		String last = null;
		ArrayList<String> peersReverse = new ArrayList<String>(peerVisited.keySet());
				
		for ( Entry<String, Integer> entry : peerVisited.entrySet())
			last = entry.getKey();
					
		if (success)
			System.out.println("File found at " + last);
		else {
			System.out.println("File not found in CAN!");
			return;
		}
		
		//print reverse order
		for ( int i = peersReverse.size()-1 ; i >= 0 ; i-- )
			System.out.println(peersReverse.get(i));
		
	}
}