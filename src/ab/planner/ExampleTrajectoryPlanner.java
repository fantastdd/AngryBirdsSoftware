package ab.planner;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ab.demo.other.Shot;
import ab.vision.ABUtil;
import ab.vision.ABState;

public class ExampleTrajectoryPlanner extends TrajectoryPlanner {

	public Shot shot = null;
	private Point releasePoint = null;
	private BufferedImage plot = null;

	public ExampleTrajectoryPlanner()
	{
		super();
		
	}

	public int getYellowBirdTapTime(Rectangle sling, Point release, Point target)
	{
		Point tapPoint = new Point();
		int distance = target.x - sling.x;
		
		Random randomGenerator = new Random();
		double r = (double)((randomGenerator.nextInt(25) + 65)) / 100;
		System.out.println(" tap at " + r + " of the distance");
		tapPoint.setLocation(new Point((int)(distance * r + sling.x) , target.y));
		return getTimeByDistance(sling, release, tapPoint);
		
	}
	public int getBlueBirdTapTime(Rectangle sling, Point release, Point target)
	{
		Point tapPoint = new Point();
		int distance = target.x - sling.x;
		
		Random randomGenerator = new Random();
		double r = (double)((randomGenerator.nextInt(20) + 65)) / 100;
		System.out.println(" tap at " + r + " of the distance");
		tapPoint.setLocation(new Point((int)(distance * r + sling.x) , target.y));
		return getTimeByDistance(sling, release, tapPoint);
	}
	public int getWhiteBirdTapTime(Rectangle sling, Point release, Point target)
	{
		Point tapPoint = new Point();
		int distance = target.x - sling.x;
		
		Random randomGenerator = new Random();
		double r = (double)((randomGenerator.nextInt(20) + 70)) / 100;
		System.out.println(" tap at " + r + " of the distance");
		tapPoint.setLocation(new Point((int)(distance * r + sling.x) , target.y));
		return getTimeByDistance(sling, release, tapPoint);
	}
	


		public BufferedImage plotTrajectory() {
			
			return plot;
		}
		public void adjustTrajectory(List<Point> traj, Rectangle sling) {
			
			adjustTrajectory(traj, sling, releasePoint);
			
		}
		public Point getReleasePoint(Point target, Rectangle sling, boolean useHighTraj)
		{
			Point releasePoint = null;
			ArrayList<Point> pts = estimateLaunchPoint(sling, target);
			releasePoint = pts.get(0);
			if (pts.size() > 1)
				if(useHighTraj)
					releasePoint = pts.get(1);
			
			
			return releasePoint;
			
		}
		public Shot getShot(ABState state, Point target, boolean useHighTraj, int tapInterval)
		{
	
			Rectangle sling = state.findSlingshot();
			float interval = ((float)tapInterval/100);
			
			//We can not make a shot without sling
			while(sling == null)
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
				System.out.println("no slingshot detected. Please remove pop up or zoom out");
				state = ABUtil.getState();
				sling = state.findSlingshot();
			}
			
			// estimate the trajectory
			ArrayList<Point> pts = estimateLaunchPoint(sling, target);

			if (useHighTraj && pts.size() > 1)
					releasePoint = pts.get(1);
				else
					releasePoint = pts.get(0);
			
			Point refPoint = getReferencePoint(sling);
		

			System.out.println("Release Point: " + releasePoint);

			//Calculate the tapping time
			if (releasePoint != null) {
				double releaseAngle = getReleaseAngle(sling,
						releasePoint);
				System.out.println("Release Angle : "
						+ Math.toDegrees(releaseAngle));
				int tap_time = 0;
				Point tapPoint = new Point();
				int distance = target.x - sling.x;
				if(interval == 0)
					System.out.println(" No tapping");
				else
				{	
					System.out.println(" Tap: " + interval + " of the distance");
				
					tapPoint.setLocation(new Point((int)(distance * interval + sling.x) , target.y));
					tap_time = getTimeByDistance(sling, releasePoint, tapPoint);
				}
					
				shot = new Shot( refPoint.x, refPoint.y, 
						(int) releasePoint
						.getX() - refPoint.x, (int) releasePoint.getY()
						-  refPoint.y,0,tap_time);
				
				plot = plotTrajectory(state.image, sling, releasePoint);
			
			}
			return shot;
			
		}



}
