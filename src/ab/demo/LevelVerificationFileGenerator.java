package ab.demo;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;

import ab.demo.other.ActionRobot;
import ab.vision.VisionMBR;

public class LevelVerificationFileGenerator {
public static void main(String args[]) throws IOException
{
	String dir = args[0];
	File file = new File(dir + File.separator);
	if(!file.exists())
		file.mkdir();
	ActionRobot ar = new ActionRobot();
	
	file = new File(dir + File.separator + "levelVerificationR_" + Integer.parseInt(args[1]) + ".txt");
	if (file.exists())
		file.delete();
	try {
		file.createNewFile();
		
	}catch (IOException e) 
	{
		e.printStackTrace();
	}
	BufferedWriter fw = new BufferedWriter(new FileWriter(file));
    //Start Load Level;  
    for (int i = 1; i < 22; i++)
    {
    	System.out.println("load level " + i);
    	ar.loadLevel(i);
    	System.out.println("save screenshot ");
    	BufferedImage image = ActionRobot.doScreenShot();
    	try {
			ImageIO.write(image, "png", new File(dir + File.separator + "level_" + i  + ".png"));
			
		} catch (IOException e) 
    	{
			e.printStackTrace();
		}
    	VisionMBR vision = new VisionMBR(image);
    	List<Rectangle> pigs = vision.findPigsMBR();
    	sortByX(pigs);
    	Rectangle lmPig = pigs.get(0);
    	System.out.println(" The leftmost Pig: " + lmPig);
    	fw.write(i + "," + lmPig.x + "," + lmPig.y);
    	if (i == 21)
    		fw.write("\n");
    }
    	fw.close();
		System.out.println("Finished");
		System.exit(0);

}
private static void sortByX(List<Rectangle> pigs)
{
	Collections.sort(pigs, new Comparator<Rectangle>(){

		@Override
		public int compare(Rectangle o1, Rectangle o2) {
			
			return ((Integer)(o1.x)).compareTo((Integer)(o2.x));
		}
		
		
	});
}
}
