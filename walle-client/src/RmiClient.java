import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RmiClient {

	/**
	 * @param args
	 */

	boolean isConnected = false;
	RmiInterface rmiServer;
	String serverAddress;
	int serverPort;
	ClientGUI gui;

	// RmiClient constructor
	public RmiClient(String theServerAddress, int theServerPort,
			ClientGUI theGUI) {
		serverAddress = theServerAddress;
		serverPort = theServerPort;
		gui = theGUI;
	}

	// Try to connect to the robot server.
	// If the connection is set up, the client state will be changed to
	// connected;
	public void connect() throws RemoteException, NotBoundException {
		Registry registry;
		registry = LocateRegistry.getRegistry(serverAddress,
				new Integer(serverPort).intValue());

		rmiServer = (RmiInterface) (registry.lookup("rmiServer"));
		isConnected = true;
		System.out.println("Connected to robot server!");
	}

	public void disconnect() {
		isConnected = false;
		
	}

	public boolean connectBT() throws RemoteException {
		try {
			boolean connected = rmiServer.connectBT();
			
			if (!connected) {
				isConnected = false;
			}
			return connected;
			
		} catch (RemoteException e) {
			System.out
			.println("No \"connectBT\" function on the remote machine.\n");
		}
		
		return false;
	}
	
	public void disconnectBT(){
		try {
			rmiServer.disconnectBT();
		} catch (RemoteException e) {
			System.out
			.println("No \"disconnectBT\" function on the remote machine.\n");
		}
		
	}
	
	public int strengthBT(){
		try {
			return rmiServer.strengthBT();
			//return 255;
		} catch (RemoteException e) {
			System.out
			.println("No \"strengthBT\" function on the remote machine.\n");
			return 0;
		}
	}
	
	public void setAutoMode(){
		try {
			rmiServer.setAutoMode();
		} catch (RemoteException e) {
			System.out
			.println("No \"setAutoMode\" function on the remote machine.\n");
		}
	}
	
	public void setManualMode(){
		try {
			rmiServer.setManualMode();
		} catch (RemoteException e) {
			System.out
			.println("No \"setManualMode\" function on the remote machine.\n");
		}
	}
	
	public void moveForward(int x) {
		try {
			rmiServer.moveForward(x);
		} catch (RemoteException e) {
			System.out
					.println("No \"moveForward\" function on the remote machine.\n");
		}
	}

	public void rotate(int x) {
		try {
			rmiServer.rotate(x);
		} catch (RemoteException e) {
			System.out
					.println("No \"rotate\" function on the remote machine.\n");
		}
	}

	public void sensorRotate(int x) {
		try {
			rmiServer.sensorRotate(x);
		} catch (RemoteException e) {
			System.out
					.println("No \"sensorRotate\" function on the remote machine.\n");
		}
	}

	public int getBattery() {
		try {
			return rmiServer.getBattery();
		//	return 7000;
		} catch (RemoteException e) {
			System.out
					.println("No \"getBattery\" function on the remote machine.\n");
			return 0;
		}
	}

	// getters and setters
	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	
	public byte[] getFrame(){
		try {
			return rmiServer.getFrame();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
