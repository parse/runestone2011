import java.awt.Component;
import java.net.InetAddress;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.media.control.BufferControl;
import javax.media.protocol.DataSource;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPControl;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SessionListener;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.NewParticipantEvent;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.RemotePayloadChangeEvent;
import javax.media.rtp.event.SessionEvent;
import javax.media.rtp.event.StreamMappedEvent;
import javax.swing.JPanel;

public class AVReceiver implements ReceiveStreamListener, SessionListener,
		ControllerListener {

	String IPAddress = null;
	int port;
	int ttl;

	RTPManager mgr = null;
	JPanel rootPanel = null;
	Component visualComponent = null;
	Player player = null;

	public AVReceiver(String IPAddress, int port, int ttl, JPanel rootPanel) {
		this.IPAddress = IPAddress;
		this.port = port;
		this.ttl = ttl;
		this.rootPanel = rootPanel;
	}

	public boolean initialize() {
		System.err.println("  - Open RTP session for: addr: " + IPAddress
				+ " port: " + port + " ttl: " + ttl);

		mgr = RTPManager.newInstance();
		mgr.addSessionListener(this);
		mgr.addReceiveStreamListener(this);

		try {
			mgr.initialize(new RTPSocketAdapter(InetAddress
					.getByName(IPAddress), port, ttl));

			// You can try out some other buffer size to see
			// if you can get better smoothness.
			BufferControl bc = (BufferControl) mgr
					.getControl("javax.media.control.BufferControl");
			if (bc != null)
				bc.setBufferLength(350);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Close the players and the session managers.
	 */
	protected void close() {
		if(player != null){
			player.stop();
			player = null;
		}
		
		if (visualComponent != null) {
			rootPanel.remove(visualComponent);
			rootPanel.validate();
			visualComponent = null;
		}

		if (mgr != null) {
			mgr.removeTargets("Closing session from AVReceiver");
			mgr.dispose();
			mgr = null;
		}
	}

	/**
	 * SessionListener.
	 */
	public synchronized void update(SessionEvent evt) {
		if (evt instanceof NewParticipantEvent) {
			Participant p = ((NewParticipantEvent) evt).getParticipant();
			System.err.println("  - A new participant had just joined: "
					+ p.getCNAME());
		}
	}

	/**
	 * ReceiveStreamListener
	 */
	public synchronized void update(ReceiveStreamEvent evt) {
		Participant participant = evt.getParticipant(); // could be null.
		ReceiveStream stream = evt.getReceiveStream(); // could be null.

		if (evt instanceof RemotePayloadChangeEvent) {

			System.err.println("  - Received an RTP PayloadChangeEvent.");
			System.err.println("Sorry, cannot handle payload change.");
			System.exit(0);

		} else if (evt instanceof NewReceiveStreamEvent) {
			try {
				stream = ((NewReceiveStreamEvent) evt).getReceiveStream();
				DataSource ds = stream.getDataSource();

				// Find out the formats.
				RTPControl ctl = (RTPControl) ds
						.getControl("javax.media.rtp.RTPControl");
				if (ctl != null) {
					System.err.println("  - Recevied new RTP stream: "
							+ ctl.getFormat());
				} else
					System.err.println("  - Recevied new RTP stream");

				if (participant == null)
					System.err
							.println("The sender of this stream had yet to be identified.");
				else {
					System.err.println("The stream comes from: "
							+ participant.getCNAME());
				}

				// create a player by passing datasource to the Media Manager
				Player p = javax.media.Manager.createPlayer(ds);
				if (p == null)
					return;

				p.addControllerListener(this);
				p.realize();

			} catch (Exception e) {
				System.err.println("NewReceiveStreamEvent exception "
						+ e.getMessage());
				return;
			}
		} else if (evt instanceof StreamMappedEvent) {

			if (stream != null && stream.getDataSource() != null) {
				DataSource ds = stream.getDataSource();
				// Find out the formats.
				RTPControl ctl = (RTPControl) ds
						.getControl("javax.media.rtp.RTPControl");
				System.err.println("  - The previously unidentified stream ");
				if (ctl != null)
					System.err.println("      " + ctl.getFormat());
				System.err.println("      had now been identified as sent by: "
						+ participant.getCNAME());
			}
		} else if (evt instanceof ByeEvent) {
			System.err.println("  - Got \"bye\" from: "
					+ participant.getCNAME());
			
			if(player != null){
				player.stop();
				player = null;
			}
			
			if(visualComponent != null){
				rootPanel.remove(visualComponent);
				rootPanel.validate();
				visualComponent = null;
			}
			
		}
	}

	/**
	 * ControllerListener for the Players.
	 */
	public synchronized void controllerUpdate(ControllerEvent ce) {
		player = (Player) ce.getSourceController();
		
		if (player == null)
			return;

		// Get this when the internal players are realized.
		if (ce instanceof RealizeCompleteEvent) {
			if ((visualComponent = player.getVisualComponent()) != null) {
				visualComponent.setSize(rootPanel.getSize());
				visualComponent.setLocation(0, 0);
				rootPanel.add(visualComponent);
			}

			rootPanel.validate();
			player.start();
		}
	}
}
