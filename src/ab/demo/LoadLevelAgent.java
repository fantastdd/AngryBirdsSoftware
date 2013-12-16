package ab.demo;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ab.demo.other.ActionRobot;
import ab.vision.ShowSeg;

public class LoadLevelAgent {
public static void main(String args[])
{
	ActionRobot ar = new ActionRobot();
	Thread thre = new Thread(new ShowSeg());
    thre.start();
    //Start Load Level;  
    for (int i = 1; i < 22; i++)
    {
    	System.out.println("load level " + i);
    	ar.loadLevel(i);
    	System.out.println("save screenshot ");
    	BufferedImage image = ActionRobot.doScreenShot();
    	try {
			ImageIO.write(image, "png", new File("level_" + i  + ".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	int[][] meta = ShowSeg.computeMetaInformation(image);
		image = ShowSeg.analyseScreenShot(image);
		try {
			ImageIO.write(image, "png", new File("level_seg_" + i  + ".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
		System.out.println("Finished");
		System.exit(0);

}
}
