import java.rmi.*;

/*
 * This interface needs to be identical to the one at the client:RmiInterfaceMapServer.java
 */
public interface RmiInterface extends Remote {
	
	/*
	 * Movement
	 */
	public void moveForward(int x) throws RemoteException;
	public void rotate(int x) throws RemoteException;
	public boolean setAutoMode() throws RemoteException;
	public boolean setManualMode() throws RemoteException;
	
	/*
	 * Sensors
	 */
	public int getDistance() throws RemoteException;
	public void sensorRotate(int x) throws RemoteException;
	
	/*
	 * Bluetooth
	 */
	public boolean connectBT() throws RemoteException;
	public void disconnectBT() throws RemoteException;
	public int strengthBT() throws RemoteException;
	
	/*
	 * Misc
	 */
	public int getBattery() throws RemoteException;
	public void kill() throws RemoteException;
	public void play(int a) throws RemoteException;
	
	//Transmit Video Picture
	public byte[] getFrame() throws RemoteException;
}
