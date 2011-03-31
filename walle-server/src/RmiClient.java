/*
 * Example client using RMI for accessing the Robot server
 * @author: Anders Hassis
 */

import java.rmi.*;
import java.rmi.registry.*;

public class RmiClient {
	static public void main(String args[]) {
		RmiInterface rmiServer;
		Registry registry;
		
		/*
		 * Robot server options
		 */
		String serverAddress = "localhost";
		int serverPort = 3232;
		
		System.out.println("Connecting to robot server... ");
		try {
			registry = LocateRegistry.getRegistry(
					serverAddress,
					new Integer(serverPort).intValue()
			);
			rmiServer = (RmiInterface)(registry.lookup("rmiServer"));
			System.out.println("Connected to robot server!");
			
			System.out.println("Sending hej");
//			int a = rmiServer.receiveMessage("Hej");
//			System.out.println("Received: " + a);
			
			/*
			 * Below are commands that are passed directly to robot server
			 */
			/*System.out.println("Connect Robot Server to Wall-E robot device via Bluetooth");
			rmiServer.connectBT();
			
			int a = rmiServer.strengthBT();
			System.out.println("Strength: "+ a);
			
			System.out.println("Move Wall-E forward 10 'steps'");
			rmiServer.moveForward(1000);
			rmiServer.play(2000);
			//System.out.println("Battery: "+ c);
			
			//rmiServer.sensorRotate(180);
			
			int b = rmiServer.strengthBT();
			System.out.println("Strength: "+ b);
			
			System.out.println("Disconnect Robot Server from Wall-E robot device");
			//rmiServer.disconnectBT();*/
			
		} catch (RemoteException e) {
			System.out.println("Couldn't connect to server ("+serverAddress + ":"+serverPort+"). Make sure server is started");
		} catch (NotBoundException e) {
		}
	}
}
