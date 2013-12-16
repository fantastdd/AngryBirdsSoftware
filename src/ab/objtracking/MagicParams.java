package ab.objtracking;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import ab.demo.other.ActionRobot;
import ab.vision.ABObject;
import ab.vision.real.MyVision;
import ab.vision.real.shape.Body;
import ab.vision.real.shape.Rect;
import ab.vision.real.shape.RectType;

public  class MagicParams {
	public static final float AngleTolerance = 0.12f; // 5 degree

	public static final int DiffTolerance = Integer.MAX_VALUE;
    public static final int MovementTolearance = 5; // within this value considered as static
    public static final int StrongMovementDist = 50;
    public static final int NormalMovementDist = 30;
    public static final int WeakMovementDist = 10;
    public static final int DebrisRadius = 5; // All the circles of radius smaller than 5 are considered as Debris
	
	public static final double AreaRatio = 1.5;
   
	
	public static  int SlimRecWidth = 7;// sling: 65,58,59 [5,6], sling 50 [4,5] 
	public static  int NormalRecWidth = 13;
	public static  int FatRecWidth = 20;
	
	public static int maximumHeight = 60;
	public static final int VisionGap = 2;
	public static final int maxIncreaseRatio = 3;//get by limiting on the formula.

	
	
	//Test the scale
	public static void main(String args[])
	{
		new ActionRobot();
		BufferedImage image = ActionRobot.doScreenShot();
		MyVision vision = new MyVision(image);
		List<Body> objs = vision.findObjects();
		List<Rect> rects = new LinkedList<Rect>();
		for (ABObject obj : objs)
		{
			if (obj.rectType == RectType.rec8x1)
			{
				rects.add((Rect)obj);
			}
		}
		//calculate average
		double total = 0;
		for (Rect rect : rects)
		{
			double width = rect.getPreciseWidth();
			total += width;
			System.out.println(width);
		}
		double average = total/rects.size();
		System.out.println(" scene scale: " + (vision.findSling().height));
		System.out.println(" average width: " + average);
		System.out.println(" normalize: " + average/ ( vision.findSling().height));
		System.exit(0);
	}

}
