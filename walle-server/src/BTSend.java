
import lejos.pc.comm.*;
import java.io.*;

public class BTSend {	
	public static void main(String[] args) {
		
		NXTConnector conn = new NXTConnector();
		
		// Connect to the NXT over Bluetooth
		boolean connected = conn.connectTo("btspp://TEAM07");
		
		if (!connected) {
			System.err.println("Failed to connect to any NXT");
			System.exit(1);
		}
		
		DataOutputStream dos = conn.getDataOut();
		final DataInputStream dis = conn.getDataIn();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	    String str = "";
		
	    int command[] = new int[2];
	    int i;
	    
	    while(true) {
		    try {
			    for(i=0; i<command.length; i++) {
			    	System.out.println("> ");
			    	str = in.readLine();
			    	command[i] = Integer.valueOf(str);
			    }
		    } catch (IOException ioe) {
				System.out.println("IO Exception reading bytes:");
				System.out.println(ioe.getMessage());
				break;
			}
		    
	    	for (i=0; i<command.length; i++) {
	    		try {
		    		dos.writeInt(command[i]);
		    		dos.flush();
	    		} catch (IOException ioe) {
					System.out.println("IO Exception writing bytes:");
					System.out.println(ioe.getMessage());
					break;
				}
	    	}
		    
	    	int[] reply = new int[2];
	    	
	    	for (i=0; i<reply.length; i++) {
	    		try {
	    			reply[i] = dis.readInt();
	    			System.out.println("Received: " + reply[i] );
	    		} catch (IOException ioe) {
	    			System.out.println("IO Exception reading bytes:");
	    			System.out.println(ioe.getMessage());
	    			break;
	    		}
	    	}
	    }
	    
	    try {
			dis.close();
			dos.close();
			conn.close();
		} catch (IOException ioe) {
			System.out.println("IOException closing connection:");
			System.out.println(ioe.getMessage());
		}
	}
}
