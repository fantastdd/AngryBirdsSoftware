package ab.utils;

import java.awt.image.BufferedImage;

import ab.demo.other.ActionRobot;
import ab.vision.Vision;

public class SpeedTest {

	private static void log(String message)
	{
		System.out.println(message);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		new ActionRobot();
		while(true)
		{
			long time = System.nanoTime();
			BufferedImage image = ActionRobot.doScreenShot();
			Vision vision = new Vision(image);
			vision.findPigsRealshape();
			//Vision vision = new Vision(image);
			//vision = null;
			log((System.nanoTime() - time) + "");
		}
	}

}
