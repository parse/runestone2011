import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

//draw image or text on canvas
class PhotoCanvas extends Canvas {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String text;
	private Image image;
	int x;
	int y;

	public PhotoCanvas() {
		text = new String();
	}

	// shows the text and set the image to null
	public void setText(String text, int theX, int theY) {
		this.text = text;
		image = null;
		x = theX;
		y = theY;
		repaint();
	}

	// shows the text and set the image to nulls
	public void setImage(Image img) {
		image = img;
		text = "";
		repaint();
	}

	public void paint(Graphics g) {
		if (text.length() > 0) {
			g.drawString(text, x, y);
		}
		if (image != null) {
			g.drawImage(image, 0, 0, this);
		}
	}
}

public class ClientGUI extends JApplet {
	public ClientGUI() {

	}
	// Business Object
	RmiClient client = new RmiClient("http://ironbreaker.no-ip.com", 3232, this);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static int MAX_BATTERY = 8200;
	private static int MIN_BATTERY = 6600;
	private static int MAX_BTSTRENGTH = 255;
	private static int WINDOW_WIDTH = 500;
	private static int WINDOW_HEIGHT = 650;
	private static int LEFTCANVAS_WIDTH = 28;
	private static int LEFTCANVAS_HEIGHT = 27;
	JPanel GameHandlePanel = new JPanel();
	JLabel statusLabel = new JLabel("label");
	JPanel StatusPanel = new JPanel();
	Panel VideoPanel = new Panel();

	PhotoCanvas BatteryCanvas = new PhotoCanvas();
	PhotoCanvas BTStrengthCanvas = new PhotoCanvas();
	PhotoCanvas AutoModeCanvas = new PhotoCanvas();
	PhotoCanvas ManualModeCanvas = new PhotoCanvas();
	PhotoCanvas BackCanvas = new PhotoCanvas();
	PhotoCanvas ForwardCanvas = new PhotoCanvas();
	PhotoCanvas RightCanvas = new PhotoCanvas();
	PhotoCanvas LeftCanvas = new PhotoCanvas();
	PhotoCanvas ConnectCanvas = new PhotoCanvas();
	PhotoCanvas DisconnectCanvas = new PhotoCanvas();
	PhotoCanvas Backgroundcanvas = new PhotoCanvas();
	PhotoCanvas Videocanvas = new PhotoCanvas();

	// Image
	Image imgAutomode;
	Image imgAutomodePressed;
	Image imgManualmode;
	Image imgManualmodePressed;
	Image imgBack;
	Image imgBackPressed;
	Image imgForward;
	Image imgForwardPressed;
	Image imgLeft;
	Image imgLeftPressed;
	Image imgRight;
	Image imgRightPressed;
	Image imgBatteries[];
	Image imgBTStrength[];
	Image imgConnect;
	Image imgConnectPressed;
	Image imgDisconnect;
	Image imgDisconnectPressed;
	Image imgBackground;
	Image imgTest;

	// Thread updateMapThread;
	Thread updateBatteryThread;
	Thread updateBTStrengthThread;
	Thread updateVideoThread;

	// AVReceiver receiver = new AVReceiver("224.123.111.101", 22224, 255,
	// VideoPanel);

	public void init() {

		try {

			this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
			getContentPane().setBackground(Color.WHITE);
			getContentPane().setLayout(null);
			getContentPane().setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

			GameHandlePanel.setBackground(Color.WHITE);
			GameHandlePanel.setBounds(29, 338, 450, 300);
			getContentPane().add(GameHandlePanel);
			GameHandlePanel.setLayout(null);

			VideoPanel.setBackground(Color.LIGHT_GRAY);
			VideoPanel.setBounds(29, 10, 450, 300);
			getContentPane().add(VideoPanel);
			VideoPanel.setLayout(null);

			Videocanvas.setBounds(0, 0, 450, 300);
			Videocanvas.setBackground(Color.LIGHT_GRAY);
			Videocanvas.setText("Video is not available!", 160, 20);
			VideoPanel.add(Videocanvas);

			AutoModeCanvas.setName("automode");
			AutoModeCanvas.setBackground(Color.LIGHT_GRAY);
			AutoModeCanvas.setBounds(160, 210, 128, 36);
			GameHandlePanel.add(AutoModeCanvas);

			ManualModeCanvas.setName("manualmode");
			ManualModeCanvas.setBackground(Color.LIGHT_GRAY);
			ManualModeCanvas.setBounds(160, 245, 128, 36);
			GameHandlePanel.add(ManualModeCanvas);

			BackCanvas.setName("back");
			BackCanvas.setBackground(Color.LIGHT_GRAY);
			BackCanvas.setBounds(100, 82, 27, 28);
			GameHandlePanel.add(BackCanvas);

			ForwardCanvas.setName("forward");
			ForwardCanvas.setBackground(Color.LIGHT_GRAY);
			ForwardCanvas.setBounds(100, 45, 27, 28);
			GameHandlePanel.add(ForwardCanvas);

			RightCanvas.setName("right");
			RightCanvas.setBackground(Color.LIGHT_GRAY);
			RightCanvas.setBounds(122, 65, LEFTCANVAS_WIDTH, LEFTCANVAS_HEIGHT);
			GameHandlePanel.add(RightCanvas);

			LeftCanvas.setName("Left");
			LeftCanvas.setBackground(Color.LIGHT_GRAY);
			LeftCanvas.setBounds(80, 65, 28, 27);
			GameHandlePanel.add(LeftCanvas);

			BatteryCanvas.setBackground(Color.LIGHT_GRAY);
			BatteryCanvas.setBounds(294, 42, 91, 80);
			GameHandlePanel.add(BatteryCanvas);

			BTStrengthCanvas.setBackground(Color.LIGHT_GRAY);
			BTStrengthCanvas.setBounds(256, 110, 73, 64);
			GameHandlePanel.add(BTStrengthCanvas);

			ConnectCanvas.setName("connect");
			ConnectCanvas.setBackground(Color.LIGHT_GRAY);
			ConnectCanvas.setBounds(176, 55, 95, 30);
			GameHandlePanel.add(ConnectCanvas);

			DisconnectCanvas.setName("disconnect");
			DisconnectCanvas.setBackground(Color.LIGHT_GRAY);
			DisconnectCanvas.setBounds(176, 86, 95, 30);
			GameHandlePanel.add(DisconnectCanvas);
			statusLabel.setText("hello world!");
			StatusPanel.setBackground(Color.WHITE);

			StatusPanel.setBounds(160, 0, 128, 19);
			GameHandlePanel.add(StatusPanel);

			StatusPanel.add(statusLabel);

			imgBackground = getImage(getCodeBase(),
					"../resources/background.jpg");
			imgConnect = getImage(getCodeBase(), "../resources/connect.png");
			imgConnectPressed = getImage(getCodeBase(),
					"../resources/connect(pressed).png");
			imgDisconnect = getImage(getCodeBase(),
					"../resources/disconnect.png");
			imgDisconnectPressed = getImage(getCodeBase(),
					"../resources/disconnect(pressed).png");
			imgAutomode = getImage(getCodeBase(), "../resources/automode.png");
			imgAutomodePressed = getImage(getCodeBase(),
					"../resources/automode(pressed).png");
			imgManualmode = getImage(getCodeBase(),
					"../resources/manualmode.png");
			imgManualmodePressed = getImage(getCodeBase(),
					"../resources/manualmode(pressed).png");
			imgBack = getImage(getCodeBase(), "../resources/back.png");
			imgBackPressed = getImage(getCodeBase(),
					"../resources/back(pressed).png");
			imgForward = getImage(getCodeBase(), "../resources/forward.png");
			imgForwardPressed = getImage(getCodeBase(),
					"../resources/forward(pressed).png");
			imgLeft = getImage(getCodeBase(), "../resources/left.png");
			imgLeftPressed = getImage(getCodeBase(),
					"../resourse/left(pressed).png");
			imgRight = getImage(getCodeBase(), "../resources/right.png");
			imgRightPressed = getImage(getCodeBase(),
					"../resources/right(pressed).png");
			imgTest = getImage(getCodeBase(), "../resources/test.png");
			imgBatteries = new Image[17];
			for (int j = 0; j < 17; j++) {
				int temp = j + 1;
				Image imgTemp;
				imgTemp = getImage(getCodeBase(),
						"../resources/battery/BatteryBG_" + temp + ".png");
				imgBatteries[j] = imgTemp.getScaledInstance(91, 80,
						Image.SCALE_SMOOTH);
			}

			imgBTStrength = new Image[6];
			for (int z = 0; z < 6; z++) {
				Image imgTemp;
				imgTemp = getImage(getCodeBase(), "../resources/BTStrength/BT"
						+ z + ".png");
				imgBTStrength[z] = imgTemp.getScaledInstance(73, 64,
						Image.SCALE_SMOOTH);
			}

			Backgroundcanvas.setBounds(0, -3, 450, 300);
			GameHandlePanel.add(Backgroundcanvas);
			Backgroundcanvas.setBackground(Color.WHITE);
			Backgroundcanvas.setImage(imgBackground);
			AutoModeCanvas.setImage(imgAutomode);
			ManualModeCanvas.setImage(imgManualmode);
			ConnectCanvas.setImage(imgConnect);
			DisconnectCanvas.setImage(imgDisconnect);
			ForwardCanvas.setImage(imgForward);
			BackCanvas.setImage(imgBack);
			LeftCanvas.setImage(imgLeft);
			RightCanvas.setImage(imgRight);
			BatteryCanvas.setImage(imgBatteries[0]);
			BTStrengthCanvas.setImage(imgBTStrength[0]);

			disableComponents();
			repaint();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	class ChangeStatusText extends Thread {
		public ChangeStatusText() {
			setDaemon(true);
			start();
		}

		public void run() {
			String str[] = { "Status: connecting", "Status: connecting.",
					"Status: connecting..", "Status: connecting..." };
			int i = 0;

			while (true) {
				statusLabel.setText(str[i]);
				i = (i + 1) % 4;
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					break;
				}
			}
		}
	}

	class ConnectThread extends Thread {
		ChangeStatusText change;

		public ConnectThread() {
			start();
		}

		public void run() {
			try {
				change = new ChangeStatusText();
				client.connect();
				boolean connected = client.connectBT();

				change.interrupt();
				if (connected) {
					// videoInfoLabel.setText("Waiting for video stream!");

					// if (!receiver.initialize())
					// videoInfoLabel.setText("Video is not available!");

					enableComponents();
				} else {
					JOptionPane.showMessageDialog(null,
							"Cannot connect to server!");
					disableComponents();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				change.interrupt();
				JOptionPane
						.showMessageDialog(null, "Cannot connect to server!");
				disableComponents();
				return;
			}

		}
	}

	class ExecuteOrderThread extends Thread {
		String order;

		ExecuteOrderThread(String theOrder) {
			order = theOrder;
			setDaemon(true);
			start();
		}

		public void run() {

			if (order.equals("Forward")) {
				ForwardCanvas.setImage(imgForwardPressed);
				client.moveForward(10);
			} else if (order.equals("Backward")) {
				BackCanvas.setImage(imgBackPressed);
				client.rotate(180);
				// client.moveForward(10);
			} else if (order.equals("Left")) {
				LeftCanvas.setImage(imgLeftPressed);
				client.rotate(90);
				// client.sensorRotate(10);
			} else if (order.equals("Right")) {
				RightCanvas.setImage(imgRightPressed);
				client.rotate(-90);
				// client.sensorRotate(-10);
			} else if (order.equals("automode")) {
				ManualModeCanvas.setImage(imgManualmode);
				AutoModeCanvas.setImage(imgAutomodePressed);
				inactivateControlPanel();
				client.setAutoMode();
			} else if (order.equals("manualmode")) {
				ManualModeCanvas.setImage(imgManualmodePressed);
				AutoModeCanvas.setImage(imgAutomode);
				activateControlPanel();
				client.setManualMode();
			} else if (order.equals("disconnect")) {
				ConnectCanvas.setImage(imgConnect);
				DisconnectCanvas.setImage(imgDisconnectPressed);
				client.disconnectBT();
				client.disconnect();
				disableComponents();
			} else if (order.equals("connect")) {
				ConnectCanvas.setImage(imgConnectPressed);
				ConnectCanvas.removeMouseListener(controlPanelListener);
				statusLabel.setText("Status: connecting");
				new ConnectThread();
			}
		}
	}

	class UpdateBatteryThread extends Thread {
		public UpdateBatteryThread() {
			setDaemon(true);
			start();
		}

		public void run() {
			int difference = MAX_BATTERY - MIN_BATTERY;
			while (true) {
				int battery = client.getBattery();
				int percentage = (battery - MIN_BATTERY) * 100 / difference;

				if (percentage > 94)
					BatteryCanvas.setImage(imgBatteries[16]);
				else if (percentage > 88)
					BatteryCanvas.setImage(imgBatteries[15]);
				else if (percentage > 82)
					BatteryCanvas.setImage(imgBatteries[14]);
				else if (percentage > 75)
					BatteryCanvas.setImage(imgBatteries[13]);
				else if (percentage > 69)
					BatteryCanvas.setImage(imgBatteries[12]);
				else if (percentage > 63)
					BatteryCanvas.setImage(imgBatteries[11]);
				else if (percentage > 56)
					BatteryCanvas.setImage(imgBatteries[10]);
				else if (percentage > 50)
					BatteryCanvas.setImage(imgBatteries[9]);
				else if (percentage > 44)
					BatteryCanvas.setImage(imgBatteries[8]);
				else if (percentage > 38)
					BatteryCanvas.setImage(imgBatteries[7]);
				else if (percentage > 32)
					BatteryCanvas.setImage(imgBatteries[6]);
				else if (percentage > 25)
					BatteryCanvas.setImage(imgBatteries[5]);
				else if (percentage > 18)
					BatteryCanvas.setImage(imgBatteries[4]);
				else if (percentage > 12)
					BatteryCanvas.setImage(imgBatteries[3]);
				else if (percentage > 6)
					BatteryCanvas.setImage(imgBatteries[2]);
				else if (percentage > 1)
					BatteryCanvas.setImage(imgBatteries[1]);
				else
					BatteryCanvas.setImage(imgBatteries[0]);
				try {
					sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					BatteryCanvas.setImage(imgBatteries[0]);
					break;
				}
			}
		}
	}

	class UpdateVideoThread extends Thread {
		public UpdateVideoThread() {
			setDaemon(true);
			start();
		}

		public void run() {
			byte[] byteImage = null;
			BufferedImage buffImg = null;
			Image image = null;
			int sleepTime = 0;

			while (true) {
				byteImage = client.getFrame();

				if (byteImage == null)
					sleepTime = 6000;
				else {
					InputStream in = new ByteArrayInputStream(byteImage);
					try {
						buffImg = ImageIO.read(in);
					} catch (IOException e) {
						buffImg = null;
						sleepTime = 6000;
					}

					if (buffImg != null) {
						buffImg = scale(buffImg, 2.0f);
						image = buffImg.getScaledInstance(450, 300,
								Image.SCALE_SMOOTH);
						Videocanvas.setImage(image);
						Videocanvas.repaint();
						sleepTime = 1000;
					}
				}

				try {
					sleep(sleepTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Videocanvas.setText("Video is not available!", 160, 20);
					break;
				}
			}
		}
	}

	class UpdateBTStrengthThread extends Thread {
		public UpdateBTStrengthThread() {
			setDaemon(true);
			start();
		}

		public void run() {
			while (true) {
				int btStrength = client.strengthBT();
				int percentage = btStrength * 100 / MAX_BTSTRENGTH;

				if (percentage > 80)
					BTStrengthCanvas.setImage(imgBTStrength[5]);
				else if (percentage > 60)
					BTStrengthCanvas.setImage(imgBTStrength[4]);
				else if (percentage > 40)
					BTStrengthCanvas.setImage(imgBTStrength[3]);
				else if (percentage > 20)
					BTStrengthCanvas.setImage(imgBTStrength[2]);
				else if (percentage > 1)
					BTStrengthCanvas.setImage(imgBTStrength[1]);
				else
					BTStrengthCanvas.setImage(imgBTStrength[0]);

				try {
					sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					BTStrengthCanvas.setImage(imgBTStrength[0]);
					break;
				}
			}
		}
	}

	MouseListener controlPanelListener = new MouseListener() {

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			LeftCanvas.setImage(imgLeft);
			RightCanvas.setImage(imgRight);
			ForwardCanvas.setImage(imgForward);
			BackCanvas.setImage(imgBack);

		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			String order = e.getComponent().getName();

			new ExecuteOrderThread(order);
		}
	};

	public void disableComponents() {
		statusLabel.setText("Status: disconnected");
		ConnectCanvas.setImage(imgConnect);
		ConnectCanvas.addMouseListener(controlPanelListener);
		DisconnectCanvas.setImage(imgDisconnectPressed);
		DisconnectCanvas.removeMouseListener(controlPanelListener);
		AutoModeCanvas.removeMouseListener(controlPanelListener);
		AutoModeCanvas.setImage(imgAutomode);
		ManualModeCanvas.removeMouseListener(controlPanelListener);
		ManualModeCanvas.setImage(imgManualmode);
		LeftCanvas.removeMouseListener(controlPanelListener);
		LeftCanvas.setImage(imgLeft);
		RightCanvas.removeMouseListener(controlPanelListener);
		RightCanvas.setImage(imgRight);
		ForwardCanvas.removeMouseListener(controlPanelListener);
		ForwardCanvas.setImage(imgForward);
		BackCanvas.removeMouseListener(controlPanelListener);
		BackCanvas.setImage(imgBack);

		// if (updateMapThread != null) {
		// updateMapThread.interrupt();
		// updateMapThread = null;
		// }

		if (updateBatteryThread != null) {
			updateBatteryThread.interrupt();
			updateBatteryThread = null;
		}

		if (updateBTStrengthThread != null) {
			updateBTStrengthThread.interrupt();
			updateBTStrengthThread = null;
		}

		if (updateVideoThread != null) {
			updateVideoThread.interrupt();
			updateVideoThread = null;
		}

		// receiver.close();

		inactivateControlPanel();
	}

	public void enableComponents() {
		ConnectCanvas.setImage(imgConnectPressed);
		ConnectCanvas.removeMouseListener(controlPanelListener);
		statusLabel.setText("Status: connected");
		DisconnectCanvas.setImage(imgDisconnect);
		DisconnectCanvas.addMouseListener(controlPanelListener);
		AutoModeCanvas.removeMouseListener(controlPanelListener);// manual mode
																	// is
																	// activated
																	// by
																	// default.
		AutoModeCanvas.setImage(imgAutomode);
		ManualModeCanvas.addMouseListener(controlPanelListener);
		ManualModeCanvas.setImage(imgManualmode);
		// updateMapThread = new UpdateMapThread();
		updateBatteryThread = new UpdateBatteryThread();
		updateBTStrengthThread = new UpdateBTStrengthThread();
		updateVideoThread = new UpdateVideoThread();

		activateControlPanel();
	}

	public void activateControlPanel() {

		LeftCanvas.addMouseListener(controlPanelListener);
		LeftCanvas.setImage(imgLeft);
		RightCanvas.addMouseListener(controlPanelListener);
		RightCanvas.setImage(imgRight);
		ForwardCanvas.addMouseListener(controlPanelListener);
		ForwardCanvas.setImage(imgForward);
		BackCanvas.addMouseListener(controlPanelListener);
		BackCanvas.setImage(imgBack);
	}

	public void inactivateControlPanel() {

		LeftCanvas.removeMouseListener(controlPanelListener);
		LeftCanvas.setImage(imgLeft);
		RightCanvas.removeMouseListener(controlPanelListener);
		RightCanvas.setImage(imgRight);
		ForwardCanvas.removeMouseListener(controlPanelListener);
		ForwardCanvas.setImage(imgForward);
		BackCanvas.removeMouseListener(controlPanelListener);
		BackCanvas.setImage(imgBack);
	}

	public void destroy() {
		if (client.isConnected) {
			client.disconnectBT();
			client.disconnect();
		}

		// if (receiver != null) {
		// receiver.close();
	}

	private BufferedImage scale(BufferedImage img, float scale) {
		int nw = (int) (img.getWidth() * scale);
		int nh = (int) (img.getHeight() * scale);
		BufferedImage res = new BufferedImage(nw, nh,
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = res.getGraphics();
		g.drawImage(img.getScaledInstance(nw, nh, Image.SCALE_AREA_AVERAGING),
				0, 0, null);
		img = res;

		return img;
	}
}

// }
