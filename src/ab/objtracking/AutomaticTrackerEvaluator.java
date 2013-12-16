package ab.objtracking;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Map;

import javax.imageio.ImageIO;

import ab.objtracking.representation.util.GlobalObjectsToolkit;
import ab.objtracking.tracker.KnowledgeTrackerBaseLine_8;
import ab.objtracking.tracker.SMETracker;
import ab.vision.ABList;
import ab.vision.ABObject;
import ab.vision.real.MyVision;
import ab.vision.real.MyVisionUtils;

public class AutomaticTrackerEvaluator {
	public static File matchReport;
	public static FileWriter writer;
	public static BufferedWriter bw;
	
	
	public static void runEvaluation(String dir) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		File directory = new File(dir);
		createMatchReport(dir + "\\" + "Report.txt");
		File samples[] = null;
		if (directory.isDirectory()) 
		{
				samples = directory.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File directory, String fileName) {
						return fileName.startsWith("L");
				}
				});
		}
		Tracker tracker;
		for (File sample : samples)
		{
			bw = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(matchReport, true)));
			//tracker = new KnowledgeTrackerBaseLine_8(200);
			tracker = new SMETracker(200);
			evaluate(tracker, sample.getAbsolutePath());
			bw.close();
		}
	
	}
	public static void createMatchReport(String filename) throws IOException
	{
		matchReport = new File(filename);
		if(!matchReport.exists())
		{
			//System.out.println("@@");
			matchReport.createNewFile();
		}
		
		
	}
	public static void createGroundTruth(String dir) throws IOException
	{
		File directory = new File(dir);

		File samples[] = null;
		if (directory.isDirectory()) 
		{
				samples = directory.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File directory, String fileName) {
						return fileName.startsWith("L");
				}
				});
		}
		Tracker tracker;
		for (File sample : samples)
		{
			
			tracker = new KnowledgeTrackerBaseLine_8(200);
			createGroundTruth(tracker, sample.getAbsolutePath());
		}
	}
	private static void createGroundTruth(Tracker tracker, String filename) throws IOException
	{
		int timegap = 200;
		if(filename.contains("_"))
			timegap = Integer.parseInt(filename.substring(filename.lastIndexOf("_") + 1));
		tracker.setTimeGap(timegap);
		File[] images = null;
		//System.out.println(filename + "  " + (new File(filename)).isDirectory());
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
		Map<ABObject, ABObject> match = tracker.getLastMatch();
		File groundTruth = new File(filename + "\\" + "groundtruth.obj");
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(groundTruth));
		oos.writeObject(match);
		oos.close();
	}
	public static int evaluate(Tracker tracker, String filename) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		int timegap = 200;
		if(filename.contains("_"))
			timegap = Integer.parseInt(filename.substring(filename.lastIndexOf("_") + 1));
		tracker.setTimeGap(timegap);
		File[] images = null;
		
		bw.append(filename.substring(filename.indexOf("L")) + ",");
		
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
		int step = 1;
		int length = (step == 1)? images.length: images.length/step + 1;
		File[] _images = new File[length];
		int index = -1;
		
		for (File image : images)
		{   
			index ++;
			if(index%step == 0)
			{	
				_images[index/step] = image;
				//System.out.println(images[index%step]);
			}
			
		}
		_images[0] = images[0];
		_images[_images.length - 1] = images[images.length - 1];
		//load ground truth
		ObjectInputStream ois = new ObjectInputStream( new FileInputStream( new File(filename + "\\" + "groundtruth.obj")));
		@SuppressWarnings("unchecked")
		Map<ABObject, ABObject> groundtruth = (Map<ABObject, ABObject>) ois.readObject();
		ois.close();
		return evaluate(tracker, _images, groundtruth);
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
		int error = 0;
		Map<ABObject, ABObject> match = tracker.getLastMatch();

		for (ABObject iniObj: groundTruth.keySet())
		{
			ABObject newObj = groundTruth.get(iniObj);
			if (newObj != null)
			{
				ABObject _newObj = match.get(iniObj);
				if(_newObj == null || _newObj.id != newObj.id)
				{
					error++;
				}
			}
		}
		double accuracy = 1 - (double)((double)error/ (double)(GlobalObjectsToolkit.allObjsNum - GlobalObjectsToolkit.uniqueObjsNum));	
		bw.append(error + "," + GlobalObjectsToolkit.allObjsNum + "," + GlobalObjectsToolkit.uniqueObjsNum + ","
		+ accuracy + "\n");
		System.out.println(" Mismatch : " + error);
		return error;
	}
	public static void getStaticObjs(String dir) throws IOException, ClassNotFoundException
	{
		File directory = new File(dir);
		createMatchReport(dir + "\\" + "StationaryObjs.txt");
		File samples[] = null;
		if (directory.isDirectory()) 
		{
				samples = directory.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File directory, String fileName) {
						return fileName.startsWith("L");
				}
				});
		}

		for (File sample : samples)
		{
			bw = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(matchReport, true)));
			ObjectInputStream ois = new ObjectInputStream( new FileInputStream( new File(sample.getAbsolutePath() + "\\" + "groundtruth.obj")));
			@SuppressWarnings("unchecked")
			Map<ABObject, ABObject> groundtruth = (Map<ABObject, ABObject>) ois.readObject();
			String filename = sample.getAbsolutePath();
			File[] images = null;
			
			bw.append(filename.substring(filename.indexOf("L")) + ",");
			
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
			MyVision vision = new MyVision(ImageIO.read(images[0]));
			ABList allInterestObjs = ABList.newList();
			allInterestObjs.addAll(vision.findObjects());
			int stationary = 0;
			for (ABObject abobject : allInterestObjs)
			{
				for (ABObject _obj : groundtruth.keySet())
				{   
					//ABObject iniObj = groundtruth.get(_object);
					if(_obj.id == abobject.id)
					{
						double distance = Math.sqrt(
								(_obj.getCenterX() - abobject.getCenterX()) * (_obj.getCenterX() - abobject.getCenterX())
						  + (_obj.getCenterY() - abobject.getCenterY()) * (_obj.getCenterY() - abobject.getCenterY()));
						if (distance < 5)
						{
							
							stationary++;
						}
					}
					 
				}
			}
			bw.append(stationary + "\n");
			//tracker = new KnowledgeTrackerBaseLine_8(200);
			//tracker = new SMETracker(200);
			//evaluate(tracker, sample.getAbsolutePath());
			
			bw.close();
		}
	}
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException 
	{
		//createGroundTruth("F:\\Samples");
		//runEvaluation("F:\\Samples");
		getStaticObjs("F:\\Samples");
	}

}
