package ab.objtracking;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Map;

import javax.imageio.ImageIO;

import ab.objtracking.tracker.KnowledgeTrackerBaseLine_5;
import ab.utils.ImageTrackFrame;
import ab.vision.ABObject;
import ab.vision.VisionUtils;
import ab.vision.real.MyVisionUtils;

public class TrackingFrameComparison implements Runnable {
	
	String filename;
	public static volatile boolean goToNextFrame = false;
	public static volatile boolean goToPrevFrame = false;
	public static boolean continuous = false;
	public int index_0 = -1;
	public int index_1 = -1;
	public Tracker tracker;
	public int step = 1;
	
	
	
	public static void main(String[] args) {
		
		Tracker tracker = new KnowledgeTrackerBaseLine_5(200);
		TrackingFrameComparison tfc = new TrackingFrameComparison("t4_61", tracker);// t3,t9,t5,t13 Fixed: t11, t12, t6, t14, t15[not]
		
		TrackingFrameComparison.continuous = true;
		tfc.run();
	}
	
	public TrackingFrameComparison(String filename, Tracker tracker, int step)
	{
		this.filename = filename;
		this.tracker = tracker;
		this.step = step;
		
	}
	public TrackingFrameComparison(String filename, Tracker tracker)
	{
		this.filename = filename;
		this.tracker = tracker;
	}
	public TrackingFrameComparison(String filename, Tracker tracker, int index_0, int index_1)
	{
		this.filename = filename;
		this.tracker = tracker;
		this.index_0 = index_0;
		this.index_1 = index_1;
	}
	
/*	public BufferedImage loadImage(int Pointer)
	{
		return ImageIO.read(new File(filename + "image"))
	}*/
	@Override
	public void run() {
		
		    BufferedImage prevScreenshot, nextScreenshot = null;
			// get list of images to process
			File[] _images = null;
			//Buffered results
			BufferedImage[] processedImages;
			// check if argument is a directory or an image
			int pointer = 0;
			if ((new File(filename)).isDirectory()) 
			{
					_images = new File(filename).listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File directory, String fileName) {
							return fileName.endsWith(".png");
					}
					});
			
				// iterate through the images
				Arrays.sort(_images);
				int length = (step == 1)? _images.length: _images.length/step + 1;
				File[] images = new File[length];
				int index = -1;
				
				for (File image : _images)
				{   
					index ++;
					if(index%step == 0)
					{	
						images[index/step] = image;
						//System.out.println(images[index%step]);
					}
					
				}
			
				if(index_0 != -1)
				{
					//System.out.println("@@@");
					File[] bufferedImages = new File[2];
					bufferedImages[0] = images[index_0];
					bufferedImages[1] = images[index_1];
					images = null;
					images = bufferedImages;
				}
				processedImages = new BufferedImage[images.length];
				/*for (File file: images)
				{
					try {
						screenshots.add(ImageIO.read(file));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}*/
				assert(images.length > 1);
				try {
					
				prevScreenshot = ImageIO.read(images[pointer]);
			
				RealTimeTracking.flipAskForInitialScenario();
				prevScreenshot = MyVisionUtils.constructImageSegWithTracking(prevScreenshot, tracker);
				prevScreenshot = VisionUtils.resizeImage(prevScreenshot, 800, 1200);
				processedImages[0] = prevScreenshot;
			
				nextScreenshot = ImageIO.read(images[pointer + 1]);
				//long time = System.nanoTime();
				nextScreenshot = MyVisionUtils.constructImageSegWithTracking(nextScreenshot, tracker);
				//System.out.println("Processing time: " + (System.nanoTime() - time)/1000000);
				nextScreenshot = VisionUtils.resizeImage(nextScreenshot, 800, 1200);
				processedImages[1] = nextScreenshot;
				
				ImageTrackFrame prevFrame = new ImageTrackFrame(" Prev Frame " + images[pointer].getName(), prevScreenshot, null);
				prevFrame.setTracker(tracker);
				prevFrame.setInitialFrame(true);
				prevFrame.refresh(prevScreenshot);
				
				ImageTrackFrame nextFrame = new ImageTrackFrame(" Next Frame " + images[pointer + 1].getName(), nextScreenshot, null);
				nextFrame.setTracker(tracker);
				nextFrame.refresh(nextScreenshot);
				
				int lastProcessed = 1;// the index of the last processed image
				boolean saveGroundTruth = true;
				while(true)
				{
					
					if(goToNextFrame)
					{
						goToNextFrame = !goToNextFrame;
						pointer++;
						lastProcessed = (lastProcessed == images.length - 1)? lastProcessed : pointer;
						
						if (pointer == images.length - 1 && saveGroundTruth)
						{
							saveGroundTruth = false;
							//save ground truth file;
							Map<ABObject, ABObject> matchedObjs = tracker.getLastMatch();
							File groundTruth = new File(filename + "\\" + "groundtruth" + System.currentTimeMillis() + ".obj");
							ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream(groundTruth));
							oos.writeObject(matchedObjs);
							oos.close();
						}
						
						if(pointer == images.length - 1)
							pointer = 0;
													
						if(pointer <= lastProcessed  && continuous)
						{
							prevScreenshot = processedImages[pointer];
							
						}
						else
						{
							prevScreenshot = ImageIO.read(images[pointer]);
							if(!continuous)
								RealTimeTracking.flipAskForInitialScenario();
							prevScreenshot = MyVisionUtils.constructImageSegWithTracking(prevScreenshot, tracker);
							prevScreenshot = VisionUtils.resizeImage(prevScreenshot, 800, 1200);
							processedImages[pointer] = prevScreenshot;
						}
						prevFrame.setTitle(" Prev Frame " + images[pointer].getName());
						prevFrame.refresh(prevScreenshot);
						
						if(pointer + 1 <= lastProcessed  && continuous)
						{
							nextScreenshot = processedImages[pointer + 1];
							nextFrame.setTitle(" Next Frame " + images[pointer + 1].getName());
						}
						else
						{
							nextScreenshot = ImageIO.read(images[pointer + 1]);;
							//long time = System.nanoTime();
							nextScreenshot = MyVisionUtils.constructImageSegWithTracking(nextScreenshot, tracker);
							//System.out.println("Processing time: " + (System.nanoTime() - time)/1000000);
							nextScreenshot = VisionUtils.resizeImage(nextScreenshot, 800, 1200);
							processedImages[pointer + 1] = nextScreenshot;
						}
						
						nextFrame.setTitle(" Next Frame " + images[pointer + 1].getName());
						nextFrame.refresh(nextScreenshot);
						
						
					} else
						if(goToPrevFrame)
						{
							goToPrevFrame = !goToPrevFrame;
							pointer--;
							if(pointer < 0)
								pointer = images.length - 2;
							if(pointer <= lastProcessed  && continuous)
							{
								prevScreenshot = processedImages[pointer];
								
							}
							else
							{
								prevScreenshot = ImageIO.read(images[pointer]);
							   if(!continuous)
								RealTimeTracking.flipAskForInitialScenario();
								prevScreenshot = MyVisionUtils.constructImageSegWithTracking(prevScreenshot, tracker);
								prevScreenshot = VisionUtils.resizeImage(prevScreenshot, 800, 1200);
							}
							prevFrame.setTitle(" Prev Frame " + images[pointer].getName());
							prevFrame.refresh(prevScreenshot);
							
							if(pointer + 1 <= lastProcessed && continuous)
							{
								nextScreenshot = processedImages[pointer + 1];
								nextFrame.setTitle(" Next Frame " + images[pointer + 1].getName());
							}
							else
							{
								nextScreenshot =ImageIO.read(images[pointer + 1]);
								//long time = System.nanoTime();
								nextScreenshot = MyVisionUtils.constructImageSegWithTracking(nextScreenshot, tracker);
								//System.out.println("Processing time: " + (System.nanoTime() - time)/1000000);
								nextScreenshot = VisionUtils.resizeImage(nextScreenshot, 800, 1200);
							}
							nextFrame.setTitle(" Next Frame " + images[pointer + 1].getName());
							nextFrame.refresh(nextScreenshot);
						} 
				}
				
				
				
				
			
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		
	}

	}
	}
