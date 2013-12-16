package ab.vision;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.ExampleTrajectoryPlanner;

public class ABUtil {
	
	public static int gap = 5;
	private static ExampleTrajectoryPlanner tp = new ExampleTrajectoryPlanner();
    public static ABState getState()
    {
    	//Do screenshot
        BufferedImage image = ActionRobot.doScreenShot();
    	return new ABState(image);
    }

/*	
	*//**by
	 * Sort the ABObjects according their X coordinate (top-left corner)
	 * *//*
	public static List<ABObject> sortByX(List<ABObject> objects)
	{
		Collections.sort(objects, new Comparator<Rectangle>(){

			@Override
			public int compare(Rectangle o1, Rectangle o2) {
				
				return ((Integer)(o1.x)).compareTo((Integer)(o2.x));
			}
			
			
		});
		return objects;
	}
	
	*//**
	 * Sort the ABObjects according their Y coordinate (top-left corner)
	 * *//*
	public static List<ABObject> sortByY(List<ABObject> objects)
	{
		Collections.sort(objects, new Comparator<Rectangle>(){

			@Override
			public int compare(Rectangle o1, Rectangle o2) {
				
				return ((Integer)(o1.y)).compareTo((Integer)(o2.y));
			}
			
			
		});
		return objects;
		
	}*/
	public void process(Rectangle rec){};
	

	// If o1 supports o2, return true
	public static boolean isSupport(ABObject o2, ABObject o1)
	{
		int ex_o1 = o1.x + o1.width;
		int ex_o2 = o2.x + o2.width;
		
		int ey_o2 = o2.y + o2.height;
		if(
			(Math.abs(ey_o2 - o1.y) < gap)
			&& 
 			!( o2.x - ex_o1  > gap || o1.x - ex_o2 > gap )
		  )
	        return true;	
		
		return false;
		
	}
	//Return a link list of ABObjects that support o1 (test by isSupport function ). 
	//objs refers to a list of potential supporters.
	//Empty list will be returned if no such supporters. 
	public static ABList getSupporters(ABObject o2, List<ABObject> objs)
			{
				ABList result = ABList.newList();
				//Loop through the potential supporters
		        for(ABObject o1: objs)
		        {
		        	if(isSupport(o2,o1))
		        		result.add(o1);
		        }
		        return result;
			}

	//Return true if the target can be+ hit by releasing the bird at the specified release point
	public static boolean isReachable(ABState state, Point target, Shot shot)
	{
		boolean result = true;
		Point releasePoint = new Point(shot.getX() + shot.getDx(), shot.getY() + shot.getDy()); 
		Rectangle sling = state.findSlingshot();
		List<Point> points = tp.predictTrajectory(sling, releasePoint);
		int counter = 10;
		for(Point point: points)
		{
		  if((counter++)%10 == 0 && point.x < 840 && point.y < 480)
			for(ABObject ab: state.findBlocks())
			{
				/*System.out.println(point.x + "  " + point.y + "  " + state.vision._scene.length + "  "
						+ state.vision._scene[1].length);*/
				if( ((ab.contains(point) && !ab.contains(target))||Math.abs(state.vision._scene[point.y][point.x] - 72 ) < 10) 
						&& point.x < target.x
						)
					return false;
				
				
			}
		  
		}
		return result;
	}
	

	public static void main(String[] args) {


	}

}
