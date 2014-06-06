package ab.planner;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.utils.ABUtil;
import ab.vision.Vision;

public class TestTrajectory {
	public static void main(String args[])
	{
		ActionRobot ar = new ActionRobot();
		Vision vision = new Vision(ActionRobot.doScreenShot());
		TrajectoryPlanner tp = new TrajectoryPlanner();
		Rectangle sling = vision.findSlingshotMBR();
		//List<Point> pts = (tp.estimateLaunchPoint(vision.findSlingshotMBR(), new Point(400,10)));
		Point target = vision.findPigsMBR().get(0).getCenter();
		//Point target = new Point(400, 10);
		List<Point> pts = (tp.estimateLaunchPoint(vision.findSlingshotMBR(), target));
		Point releasePoint = null;
		if(!pts.isEmpty())
		{
			releasePoint = pts.get(0);
		}
		else
		{
				System.out.println("No release point found for the target");
				System.out.println("Try a shot with 45 degree");
				releasePoint = tp.findReleasePoint(sling, Math.PI/4);
		}
		Point refPoint = tp.getReferencePoint(sling);
		int dx = (int)releasePoint.getX() - refPoint.x;
		int dy = (int)releasePoint.getY() - refPoint.y;
		System.out.println("release.x " + releasePoint.x + "  release.y " + releasePoint.y);
		System.out.println("dx " + dx + " dy " + dy);
		System.out.println("ref.x  " + refPoint.x + " ref.y " + refPoint.y);
		Shot shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, 0);
		System.out.println(pts);
		System.out.println(shot);
		System.out.println("Is reachable: " + ABUtil.isReachable(vision,  target, shot));
		ar.cshoot(shot);
	}
}
