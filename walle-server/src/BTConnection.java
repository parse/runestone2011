/*
 * Bluetooth connection manager
 * @author: Anders Hassis
 */
import java.io.*;

import lejos.nxt.remote.NXTCommand;
import lejos.pc.comm.*;

public class BTConnection implements Runnable {
	private NXTConnector connection;
	
	private boolean connected;
	private static Thread pingserver;
	private DataOutputStream dos;
	private DataInputStream dis;
	
	public BTConnection (RobotCommand rc) {
		connection = null;
		dos = null;
		dis = null;
		connected = false;
	}

	public void run() { }
	
	/*
	 * Is Bluetooth connection available?
	 */
	public synchronized boolean isConnected () {
		return connected;
	}
	
	public synchronized boolean connect() {
		/*
		 * Connect to Bluetooth Device
		 */
		connection = new NXTConnector();
		
		connected = connection.connectTo("btspp://TEAM07");

		System.out.println("Connected to NXT Bluetooth Device!");
		
		/*
		 * Create streams
		 */
		dos = connection.getDataOut();
		dis = connection.getDataIn();
		
		return connected;
	}

	public synchronized boolean disconnect() {
		
		if ( isConnected() ) {
			try {
				dis.close();
				dos.close();
				connection.close();
			} catch (IOException ioe) {
				System.out.println(ioe);
			}
			
			connected = false;
		}
		
		return connected;
	}
	
	public synchronized int[] sendCommand(int[] input) { 
		int command[] = new int[2];
		
		if ( isConnected() ) {
			if (dos == null) {
				System.out.println("ERROR: OutputStream is NULL");
			} else {
				for (int i=0; i<input.length; i++) {
		    		try {
			    		dos.writeInt(input[i]);
			    		dos.flush();
		    		} catch (IOException ioe) {
						System.out.println("IO Exception writing bytes:");
						System.out.println(ioe.getMessage());
						break;
					}
		    	}
			    
		    	int[] reply = new int[2];
		    	for (int i=0;i<2;i++) {
		    		try {
		    			reply[i] = dis.readInt();
		    			// System.out.println("Receieved from Walle: "+ reply[i]);
		    		} catch (IOException ioe) {
		    			break;
		    		}
		    	}
				
		    	return reply;
			}
		}
		
		return command;
	}
}
