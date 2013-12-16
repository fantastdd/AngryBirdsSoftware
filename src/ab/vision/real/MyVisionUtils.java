package ab.vision.real;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import ab.objtracking.RealTimeTracking;
import ab.objtracking.Tracker;
import ab.vision.ABList;

public class MyVisionUtils {
	
	public static BufferedImage constructImageSegWithTracking(BufferedImage screenshot, Tracker tracker) {

		// process imaged
		MyVision vision = new MyVision(screenshot);
		
		ABList allInterestObjs = ABList.newList();
		allInterestObjs.addAll(vision.findObjects());
		if(RealTimeTracking.askForIniScenario)
		{
			
			tracker.startTracking(allInterestObjs);
		
			//System.out.println(" Initial objects size: " + allInterestObjs.size());
			RealTimeTracking.flipAskForInitialScenario();
			
		}
		else
		{
			if(tracker != null && tracker.isTrackingStart())
			{
				long time = System.nanoTime();
				boolean matched = tracker.matchObjs(allInterestObjs);
				System.out.println("Processing time: " + (System.nanoTime() - time));
				if(!matched)
				{
					vision.drawObjects(screenshot, false);
					return screenshot;
				}
				//tracker.setInitialObjects(allInterestObjs);
				//System.out.println(" match completed");
			}
		}
		// draw objects
		vision.drawObjectsWithID(screenshot, true);
		
	
		return screenshot;
	}
	public static BufferedImage deepCopy(BufferedImage bi) {
		 ColorModel cm = bi.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bi.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
		/*BufferedImage copyOfImage = 
				   new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics g = copyOfImage.createGraphics();
				g.drawImage(originalImage, 0, 0, null);*/
		//return copyOfImage;
		}

}
