package ab.demo;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import ab.demo.other.ActionRobot;
import ab.server.proxy.message.ProxyMouseWheelMessage;
import ab.vision.Vision;

public class Test {
	public static void main(String args[])
	{
		
		ActionRobot actionRobot = new ActionRobot();
		for (int k = 0; k < 10; k++) {
			ActionRobot.proxy.send(new ProxyMouseWheelMessage(1));
		}
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (int k = 0; k < 10; k++) {
			ActionRobot.proxy.send(new ProxyMouseWheelMessage(-1));
		}
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BufferedImage image_1 = actionRobot.doScreenShot();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BufferedImage image_2 = actionRobot.doScreenShot();
		Vision vision = new Vision(image_1);
		Rectangle rec_1 =vision.findSlingshotMBR();
		vision = new Vision(image_2);
		Rectangle rec_2 =vision.findSlingshotMBR();
		System.out.println(rec_1.equals(rec_2));		
		System.exit(0);
	}
	static boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
	    if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
	        for (int x = 0; x < img1.getWidth(); x++) {
	            for (int y = 0; y < img1.getHeight(); y++) {
	                if (img1.getRGB(x, y) != img2.getRGB(x, y))
	                    return false;
	            }
	        }
	    } else {
	        return false;
	    }
	    return true;
	}
}
