package ab.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import ab.demo.other.ActionRobot;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.Vision;

public class VisionPerformanceTest {

	private static void log(String message)
	{
		System.out.println(message);
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
	
		new ActionRobot();
		BufferedImage screenshot = ActionRobot.doScreenShot();
		Vision vision = new Vision(screenshot);
		List<ABObject> objs = vision.findBlocksRealShape();
		//File file = new File("F:\\sampleLevel2-13.png");
		//File file = new File("F:\\sampleLevel1-19.png");
		File file = new File("F:\\sampleLevel1-18.png");
		//BufferedImage savedImage = new BufferedImage(screenshot.getWidth(), screenshot.getHeight(), BufferedImage.TYPE_INT_RGB);
		/*Graphics2D g = savedImage.createGraphics();
		for (int x = 0; x < savedImage.getWidth(); x++)
			for (int y = 0; y < savedImage.getHeight(); y++)
				savedImage.setRGB(x, y, 0xffffff);*/
		Graphics2D g = screenshot.createGraphics();
		g.setColor(Color.red);
		for (ABObject obj : objs)
		{
			if(obj.type != ABType.Hill)
				g.draw(obj.getBounds());
		}
		
		ImageIO.write(screenshot, "png", file);
		System.exit(0);
		/*while(true)
		{
			long time = System.nanoTime();
			BufferedImage image = ActionRobot.doScreenShot();
			Vision vision = new Vision(image);
			vision.findBlocksMBR();
			log((System.nanoTime() - time) + "");
		}*/
	}

}
