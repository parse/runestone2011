import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.net.*;

import javax.naming.spi.DirStateFactory.Result;

public class MapServer extends UnicastRemoteObject implements
		RmiInterfaceMapServer {

	private static final long serialVersionUID = -701950875090680095L;
	Registry registry; // rmi registry for lookup the remote objects
	int port;
	String address;
	Connection conn;
	boolean connected = false;

	public MapServer() throws RemoteException {
		try {
			address = (InetAddress.getLocalHost()).toString();
		} catch (Exception e) {
			throw new RemoteException("No addresss mothafucka");
		}

		// port = 3233;
		port = 3333;

		System.out.println("Listening on " + address + ":" + port + ")");
		try {
			registry = LocateRegistry.createRegistry(port);
			registry.rebind("rmiServer", this);
		} catch (RemoteException e) {
			throw e;
		}
	}


	static public void main(String args[]) throws RemoteException {
		new MapServer();

	}

	boolean isConnectedMySQL() {
		return connected;
	}
	
	public boolean connectDB(int clientID) throws RemoteException {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			String str = "jdbc:mysql://ironbreaker.no-ip.com/mapserver";
			conn = DriverManager.getConnection(str, "root", "team14");
			System.out.println("Connected to MySQL at mapserver");
			connected = true;
			
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean disconnectDB(int clientID) throws RemoteException {
		try {
			conn.close();
			connected = false;
			System.out.println("Disconnected from MySQL at mapserver");
			
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public int[][] getMap(int clientID) throws RemoteException {
		System.out.println("I am running in the Server" + clientID
				+ " is getting map from me");
		
		//int[][] map = new int[100][100];
		//int i=0;
		//String[] color={"white"};
		int[][] map = new int[100][100];
		
		try {
			Statement stmt = (Statement) conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			String mapSearch = "SELECT * FROM map WHERE 1";
			ResultSet res = (ResultSet) stmt.executeQuery(mapSearch);
			
			int x,y = 0;
			
			while(res.next()) {
				x=res.getInt(1);
				y=res.getInt(2);
				map[x][y] = res.getInt(3);
				/*if(res.getInt(4) != clientID && map[x][y] == 1) {   //If tagged by the other team mark it as 6
                    map[x][y] = 6;
                }*/
			}
			System.out.println("Ran mysql : SELECT * FROM map WHERE 1");
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return map;
	}

	public int updateMap(int xCoord, int yCoord, int data,int clientID) throws RemoteException {
		System.out.println("I am running in the Server, clientID= " + clientID
				+ " is updating map to me");
		try {
			PreparedStatement updateMap=conn.prepareStatement("UPDATE map SET data=?, client=? WHERE x=? AND y=?");
			//for (int i=0;i<xCoord.length;i++){
				updateMap.setInt(1, data);
				updateMap.setInt(2, clientID);
				updateMap.setInt(3, xCoord);
				updateMap.setInt(4, yCoord);
			//}
			updateMap.executeUpdate();
			updateMap.close();
			System.out.println("Ran mysql : UPDATE map SET data="+data+" WHERE x="+xCoord+" AND y="+yCoord);
			System.out.println("Map server has updated map information from clientID= "+clientID+" now");
			
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}
	
	public int updateMapRectangle(int[] lL, int[] uR, int data,int clientID) throws RemoteException {
		System.out.println("I am running in the Server, clientID= " + clientID
				+ " is updating map to me");
		try {
			PreparedStatement updateMap=conn.prepareStatement("UPDATE map SET data=?, client=? WHERE x<=? AND x>=? AND y<=? AND y>=?");
			//for (int i=0;i<xCoord.length;i++){
				updateMap.setInt(1, data);
				updateMap.setInt(2, clientID);
				updateMap.setInt(3, uR[0]);
				updateMap.setInt(4, lL[0]);
				updateMap.setInt(5, uR[1]);
				updateMap.setInt(6, lL[1]);
			//}
			updateMap.executeUpdate();
			updateMap.close();
			System.out.println("Ran mysql : UPDATE map SET data="+data+" WHERE x<="+uR[0]+" AND x<="+lL[0] +"AND y<="+uR[1]+" AND y>="+uR[1]);
			System.out.println("Map server has updated map information from clientID= "+clientID+" now");
			
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

}
