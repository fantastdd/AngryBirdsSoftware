package ab.objtracking;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import ab.objtracking.tracker.KnowledgeTrackerBaseLine_7;
import ab.objtracking.tracker.KnowledgeTrackerBaseLine_8;
import ab.vision.ABObject;
import ab.vision.real.MyVisionUtils;

public class TrackerEvaluator {
	
	public static int evaluate(Tracker tracker, String filename) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		int timegap = 200;
		if(filename.contains("_"))
			timegap = Integer.parseInt(filename.substring(filename.indexOf("_") + 1));
		tracker.setTimeGap(timegap);
		File[] images = null;
		if ((new File(filename)).isDirectory()) 
		{
				images = new File(filename).listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File directory, String fileName) {
						return fileName.endsWith(".png");
				}
				});
		
			// iterate through the images
			Arrays.sort(images);
		}
		//load ground truth
		ObjectInputStream ois = new ObjectInputStream( new FileInputStream( new File(filename + "\\" + "groundtruth.obj")));
		@SuppressWarnings("unchecked")
		Map<ABObject, ABObject> groundtruth = (Map<ABObject, ABObject>) ois.readObject();
		ois.close();
		return evaluate(tracker, images, groundtruth);
	}
	private static int evaluate(Tracker tracker, File[] images, Map<ABObject, ABObject> groundTruth) throws IOException
	{
		RealTimeTracking.flipAskForInitialScenario();
		File image = images[0];
		BufferedImage screenshot = ImageIO.read(image);
		MyVisionUtils.constructImageSegWithTracking(screenshot, tracker);
		
		image = images[1];
		screenshot= ImageIO.read(image);
		MyVisionUtils.constructImageSegWithTracking(screenshot, tracker);
		
		for(int i = 1; i < images.length - 1; i++ )
		{
			image = images[i];
			screenshot = ImageIO.read(image);
			
			image = images[i + 1];
			screenshot = ImageIO.read(image);
			MyVisionUtils.constructImageSegWithTracking(screenshot, tracker);
		}
		
		//Compare the matching result;
		int Error = 0;
		Map<ABObject, ABObject> match = tracker.getLastMatch();
/*		System.out.println(" ===========  Print Match ============= ");
		for (ABObject newObj : match.keySet())
		{
			System.out.println(" newObj: " + newObj);
			System.out.println(" initial Obj" + match.get(newObj));
			System.out.println("==========");
		}*/
		for (ABObject iniObj: groundTruth.keySet())
		{
			ABObject newObj = groundTruth.get(iniObj);
			if (newObj != null)
			{
				ABObject _newObj = match.get(iniObj);
				if(_newObj == null || _newObj.id != newObj.id)
				{
					System.out.println(newObj + "  " + _newObj + "  " + iniObj);
					Error++;
				}
			}
		}
		System.out.println(" Mismatch : " + Error);
		return Error;
	}
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException 
	{
		Tracker tracker = new KnowledgeTrackerBaseLine_8(200);
		Map<String, Integer> errors = new HashMap<String, Integer>();
		errors.put("e2L6_56", evaluate(tracker, "e2L6_56"));
		
		tracker = new KnowledgeTrackerBaseLine_8(200);
		errors.put("t11", evaluate(tracker, "t11"));
		
		tracker = new KnowledgeTrackerBaseLine_8(200);
		errors.put("t12", evaluate(tracker, "t12"));
		
		tracker = new KnowledgeTrackerBaseLine_8(200);
		errors.put("t14", evaluate(tracker, "t14"));
		
		tracker = new KnowledgeTrackerBaseLine_8(200);
		errors.put("t6", evaluate(tracker, "t6"));
		
		
		tracker = new KnowledgeTrackerBaseLine_8(200);
		errors.put("e1L9_54", evaluate(tracker, "e1L9_54"));
		
		tracker = new KnowledgeTrackerBaseLine_8(200);
		errors.put("e1L9_62", evaluate(tracker, "e1L9_62"));
		
		tracker = new KnowledgeTrackerBaseLine_8(200);
		errors.put("e1L10t_54", evaluate(tracker, "e1L10t_54"));
		
		tracker = new KnowledgeTrackerBaseLine_8(200);
		errors.put( "e1L15_53", evaluate(tracker,  "e1L15_53"));
		
		tracker = new KnowledgeTrackerBaseLine_8(200);
		errors.put( "e1L17_58", evaluate(tracker,  "e1L17_58"));
		
		for (String file : errors.keySet())
		{
			System.out.println(file + "  Mismatch " + errors.get(file));
		}
	}

}
