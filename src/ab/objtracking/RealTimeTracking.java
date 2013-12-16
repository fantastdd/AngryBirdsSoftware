package ab.objtracking;

import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import ab.demo.other.ActionRobot;
import ab.objtracking.tracker.*;
import ab.utils.ImageSegFrame;
import ab.vision.VisionUtils;
import ab.vision.real.MyVisionUtils;

public class RealTimeTracking implements Runnable{
	
	public static boolean askForIniScenario = false;
	public static void flipAskForInitialScenario()
	{
		askForIniScenario = !askForIniScenario;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RealTimeTracking dt = new RealTimeTracking();
		dt.run();
	}

	@Override
	public void run() {
		//the display frame;
		 ImageSegFrame frame = null;
		//initialize the tracker
		 
		 Tracker tracker = new KnowledgeTrackerBaseLine_8(400);
		// initialize the new vision moudle
		 //MyVision myVision = new MyVision();
		 
		 
		 
		//long screenshot_time = System.nanoTime();
		//long timeGap = 0l;
		//long vision_process_time = 0l;
		while (true) {	
			//long current_time = System.nanoTime();
			// capture an image
			BufferedImage screenshot = ActionRobot.doScreenShot();
			//timeGap = System.nanoTime() - screenshot_time;
			//screenshot_time = System.nanoTime();
			// analyze and show image
			//screenshot_time = System.nanoTime() - current_time;
			
			screenshot = MyVisionUtils.constructImageSegWithTracking(screenshot, tracker);
			screenshot = VisionUtils.resizeImage(screenshot, 800, 1200);
			//vision_process_time = System.nanoTime() - screenshot_time - current_time;
			if (frame == null) {
				frame = new ImageSegFrame("Object Tracking", screenshot,
						null);
				
				 frame.getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			} else {
				frame.refresh(screenshot, null);
			}
			//System.out.println(" screenshot time : " + screenshot_time + " vision process time " + vision_process_time);
			
			//System.out.println(" time gap " + timeGap);
		}
	}

}
