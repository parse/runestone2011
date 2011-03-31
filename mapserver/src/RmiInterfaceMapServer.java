import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * This interface needs to be identical to the one at walle-server:RmiInterfaceMapServer.java
 */
 public interface RmiInterfaceMapServer extends Remote {
	/* This is just a test method */
	boolean connectDB(int clientID) throws RemoteException;
	boolean disconnectDB(int clientID) throws RemoteException;
	int[][] getMap(int clientID)throws RemoteException;
	int updateMap(int x, int y,int data,int clientID) throws RemoteException;
	int updateMapRectangle(int[] lL, int[] uR, int data,int clientID) throws RemoteException;
}
