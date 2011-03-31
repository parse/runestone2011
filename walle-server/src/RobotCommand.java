/*
 * Robot command manager
 * @author: Anders Hassis
 */

import java.io.IOException;
import java.rmi.RemoteException;


public class RobotCommand {
	private BTConnection connection;
	RmiServer robotServer;
	boolean connected;
	Thread btThread;
	
	public RobotCommand(RmiServer rs) throws RemoteException { robotServer = rs; }
	
	public boolean isConnected() {
		return connected;
	}
	
	public void connectBT() throws RemoteException {
		//System.out.println("Initializing Bluetooth connection...");
		
		connection = new BTConnection(this);
		btThread = new Thread(connection);
		btThread.start();
		connected = connection.connect();
	}

	public void disconnectBT() throws RemoteException {
		//System.out.println("Disconnecting Bluetooth connection...");
		
		int[] cmd = {9, 0};
		int[] reply = connection.sendCommand(cmd);	
		
		connected = connection.disconnect();
	}
	
	public String getMode() {
		return robotServer.robotMode;
	}
	
	public boolean startMode(String mode) {
		if ( mode.equals("auto") ) {
			/*
			 * @TODO: Send command to server
			 */
			
			return true;
		} else if ( mode.equals("manual") ) {
			/*
			 * @TODO: Send command to server
			 */
			
			return true;
		}
		
		return false;
	}
	
	public void moveForward(int x) throws RemoteException {
		//System.out.println("Forward: " + x);
		
		int[] cmd = {2, x};
		int[] reply = connection.sendCommand(cmd);	
	}

	public void rotate(int x) throws RemoteException {
		//System.out.println("Rotate: " + x + " degrees");
		
		int[] cmd = {1, x};
		int[] reply = new int[2];
		reply = connection.sendCommand(cmd);	
	}

	public int getDistance() throws RemoteException {
		//System.out.println("Distance: ");
		
		int[] cmd = {4, 0};
		int[] reply = new int[2];
		reply = connection.sendCommand(cmd);	
		
		int[] cmd2 = {4, 0};
		int[] reply2 = new int[2];
		reply2 = connection.sendCommand(cmd2);	
		
		int[] cmd3 = {4, 0};
		int[] reply3 = new int[2];
		reply3 = connection.sendCommand(cmd3);	
		
		return reply3[1];
	}

	public void sensorRotate(int x) throws RemoteException {
		//System.out.println("Rotate sensor: " + x);
		
		int[] cmd = {3, x};
		int[] reply = new int[2];
		reply = connection.sendCommand(cmd);	
	}

	public int strengthBT() throws RemoteException {
		int[] cmd = {6, 0};
		int[] reply = new int[2];
		reply = connection.sendCommand(cmd);	
		
		//System.out.println("Fetched Bluetooth level: " + reply[1]);
		
		return reply[1];
	} 

	public int getBattery() throws RemoteException {
		int[] cmd = {5, 0};
		int[] reply = new int[2];
		reply = connection.sendCommand(cmd);	
		
		//System.out.println("Fetched battery level: " + reply[0] + " " + reply[1]);
		
		return reply[1];
	}

	public void kill() throws RemoteException {
		//System.out.println("Sent kill command");
		
		int[] cmd = {9, 0};
		int[] reply = new int[2];
		reply = connection.sendCommand(cmd);	
	}

	public void play(int a) throws RemoteException {
		//System.out.println("Sent play command");
		
		int[] cmd = {7, a};
		int[] reply = new int[2];
		reply = connection.sendCommand(cmd);	
	}
	
	/*
	 * @TODO: Implement response from WallE on a given input (=PING command)
	 */
	static public void ping(BTConnection connection) throws RemoteException {
		int[] cmd = {8, 0};
		int[] reply = new int[2];
		
		reply = connection.sendCommand(cmd);	
	}
}
