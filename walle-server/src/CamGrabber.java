import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.CannotRealizeException;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

class DeviceNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7776095923807432580L;

}

public class CamGrabber {

	private CaptureDeviceInfo deviceInfo;
	private Player player;
	private FrameGrabbingControl grabber;

	public CamGrabber() throws DeviceNotFoundException, NoPlayerException, CannotRealizeException, IOException {
		VideoFormat format = new VideoFormat(VideoFormat.YUV);
		Vector<?> devices = CaptureDeviceManager.getDeviceList(format);

		if (devices.size() > 0)
			deviceInfo = (CaptureDeviceInfo) devices.firstElement();
		else {
			throw new DeviceNotFoundException();
		}
		
		player = Manager.createRealizedPlayer(deviceInfo.getLocator());
		grabber = (FrameGrabbingControl) player
				.getControl("javax.media.control.FrameGrabbingControl");
		player.start();
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

	public byte[] getFrame() {
		Buffer b = grabber.grabFrame();
		Image img = (new BufferToImage((VideoFormat) b.getFormat())
				.createImage(b));

		if (img == null) {
			return null;
		} else {
			// And convert to bytearray with JPG format
			BufferedImage buffImg = new BufferedImage(img.getWidth(null),
					img.getHeight(null), BufferedImage.TYPE_INT_RGB);
			buffImg.getGraphics().drawImage(img, 0, 0, null);

			// Scale it down to half the size (we can't send highres images..)
			buffImg = scale(buffImg, 0.5f);
			Graphics2D g2 = (Graphics2D) buffImg.getGraphics();
			g2.setColor(Color.BLACK);
			Date d = new Date();
			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
			g2.drawString(df.format(d), 1, 10);
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			try {
				ImageIO.write(buffImg, "jpg", bout);
				bout.flush();
			} catch (IOException e) {
				return null;
			}
			byte bytes[] = bout.toByteArray();
			return bytes;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
