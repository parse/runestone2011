import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*
 * Class to manage the mapserver connection
 */
class MapServerManagement {

	/*
	 * For usage with the map server
	 */
	RmiInterfaceMapServer rmiMapServer;
	Registry registryMapserver;

	/*
	 * Server options
	 */
	String mapServerAddress = "localhost";
	int mapServerPort = 3333;

	public MapServerManagement() {
		connect();
	}

	/*
	 * Connect to the mapserver
	 */
	public void connect() {
		try {
			registryMapserver = LocateRegistry.getRegistry(mapServerAddress,
					new Integer(mapServerPort).intValue());
			rmiMapServer = (RmiInterfaceMapServer) (registryMapserver
					.lookup("rmiServer"));

			rmiMapServer.connectDB(1);
		} catch (RemoteException e) {
			System.out.println("Couldn't connect to map server ("
					+ mapServerAddress + ":" + mapServerPort
					+ "). Make sure server is started");
		} catch (NotBoundException e) {
		}
	}
	
	public void disconnect() {
		try {
			System.out.println("Sending disconnect to mapserver");
			rmiMapServer.disconnectDB(1);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int receiveMessage(String str, int clientID) {
		try {	
			int a = rmiMapServer.receiveMessage(str, clientID);
			return a;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return -1;

	}

	public int updateMap(int x, int y, int data, int clientID) {
		try {
			int a = rmiMapServer.updateMap(x, y, data, clientID);
			return a;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return -1;
	}
	public int updateMapRectangle(int[] lL, int[] uR, int data, int clientID) {
		try {
			int a = rmiMapServer.updateMapRectangle(lL, uR, data, clientID);
			return a;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return -1;
	}
	public int[][] getMap(int clientID) {
		try {
			int res[][] = rmiMapServer.getMap(clientID);
			return res;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}
}