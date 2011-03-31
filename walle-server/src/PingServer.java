/*
 * Ping server for keeping the Bluetooth connection going
 * @author: Anders Hassis
 */
import java.rmi.RemoteException;

public class PingServer extends Thread {
	BTConnection conn;
	
	public PingServer(BTConnection connection) {
		conn = connection;
	}
	
	public void interrupt() {
		super.interrupt();
	}
	
	public synchronized void run() {
		/*
		 * Only run the ping-command when Bluetooth connection exists
		 */
		while ( conn.isConnected() ) {
			//try {
				//RobotCommand.ping(conn);
				//Thread.sleep(3000);
			//} catch (RemoteException e) {
			//	System.out.println(e);
			//} catch (InterruptedException e) {
			//	System.out.println(e);
			//}
		}
	}
}
