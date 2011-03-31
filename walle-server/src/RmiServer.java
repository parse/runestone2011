/*
 * Robot server manager
 * @author: Anders Hassis
 */

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RmiServer extends java.rmi.server.UnicastRemoteObject implements RmiInterface {

	private static final long serialVersionUID = -701950875090680095L;
	Registry registry;
	int port;
	String address;
	RobotCommand WallE;
	CamGrabber grabber = null;
	
	/*
	 * Robot modes
	 */
	AwesomePilot ap;
	Thread apThread;
	
	public static String AUTOMODE = "auto"; 
	public static String MANUALMODE = "manual";
	String robotMode = MANUALMODE; // Defaults to manual mode
	
	/*
	 * Setup RMI server
	 */
	public RmiServer() throws RemoteException {
		try {
			address = (InetAddress.getLocalHost()).toString();
		} 
		catch (Exception e) {
			throw new RemoteException("No addresss");
		}
		
		port = 3232;
		
		System.out.println("Listening on " + address + ":" + port + ")");
		try {
			registry = LocateRegistry.createRegistry(port);
			registry.rebind("rmiServer", this);
		}
		catch (RemoteException e) {
			throw e;
		}
		
		WallE = new RobotCommand(this);
		
		try {
			grabber = new CamGrabber();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			grabber = null;
		} 
	}
	
	/*
	 * Static RMI server handle
	 */
	static public void main(String args[]) throws RemoteException {
		new RmiServer();
	}

	/*
	 * Robot commands
	 */
	public boolean setAutoMode() throws RemoteException {
		if ( WallE.startMode("auto") ) {
			robotMode = AUTOMODE;
			
			/*
			 * Start thread for handling AutoPilot
			 */
			ap = new AwesomePilot(this);
			apThread = new Thread(ap);
			apThread.start();
			
			System.out.println("Switched to autoMode");
		}
		
		return true;
	}
	
	public boolean setManualMode() throws RemoteException {
		if ( WallE.startMode("manual") ) {
			robotMode = MANUALMODE;
			System.out.println("Switched to manualMode");
			/*
			 * Disconnect autopilot
			 */
			ap.active = false;
			//ap.shutdownPilot();
			//apThread.destroy();
			
			
			
		}
		
		return true;
	}
	
	public void moveForward(int x) throws RemoteException {
		WallE.moveForward(x);
	}

	public void rotate(int x) throws RemoteException {
		WallE.rotate(x);
	}

	public int getDistance() throws RemoteException {
		int x = WallE.getDistance();

		return x;
	}

	public void sensorRotate(int x) throws RemoteException {
		WallE.sensorRotate(x);
	}

	public boolean connectBT() throws RemoteException {
		if (!WallE.isConnected()) {
			WallE.connectBT();
			return WallE.connected;
		}
		
		return false;
	}

	public void disconnectBT() throws RemoteException {
		if ( WallE.isConnected() ) {
			if (WallE.getMode() == AUTOMODE) {
				ap.shutdownPilot();
				
				/* Wait for robot to finish it run */
				/*try {
					ap.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				robotMode = MANUALMODE;
			}
			
			WallE.disconnectBT();
		}
	}

	public int strengthBT() throws RemoteException {
		int x = WallE.strengthBT();

		return x;
	}

	public int getBattery() throws RemoteException {
		int x = WallE.getBattery();

		return x;
	}

	public void kill() throws RemoteException {
		WallE.kill();
	}

	public void play(int a) throws RemoteException {
		WallE.play(a);
	}

	public byte[] getFrame() throws RemoteException {
		if(grabber == null)
			return null;
		
		return grabber.getFrame();
	}
}
