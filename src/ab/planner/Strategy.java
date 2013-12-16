package ab.planner;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Random;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.vision.ABBlock;
import ab.vision.ABList;
import ab.vision.ABObject;
import ab.vision.ABPig;
import ab.vision.ABPoint;
import ab.vision.ABRunner;
import ab.vision.ABState;
import ab.vision.ABSupport;
import ab.vision.ABType;
import ab.vision.ABUtil;


public abstract class Strategy {
	private Random randomGenerator;
	public  ABState state;
	public ExampleTrajectoryPlanner trajectoryPlanner;
	public Strategy()
	{
		randomGenerator = new Random();
		trajectoryPlanner = new ExampleTrajectoryPlanner();
	}
	public int random(int range)
	{
		return randomGenerator.nextInt(range);
	}
	public void debug(String message)
	{
		System.out.println(message);
	} 
	
	public ABPig randomPig() 
	{
		ABList pigs = state.findPigs();
		if(pigs.isEmpty())
			return ABPig.nullPig;
		else
			return (ABPig)pigs.get(randomGenerator.nextInt(pigs.size())); 
	}
	public ABBlock randomBlock() 
	{
		ABList blocks = state.findBlocks();
		if(blocks.isEmpty())
			return ABBlock.nullBlock;
		else
			return (ABBlock)blocks.get(randomGenerator.nextInt(blocks.size()));
	}
	public ABPig leftmostPig() 
	{ 
	
		ABList pigs = state.findPigs();
	
		if(pigs.isEmpty())
			return ABPig.nullPig;
		else
		{
			pigs.sortByX();
			return (ABPig)pigs.getFirst();		}
	}
	public ABPig rightmostPig() 
	{
		ABList pigs = state.findPigs();
		if(pigs.isEmpty())
			return ABPig.nullPig;
		else
		{
			pigs.sortByX();
			return (ABPig)pigs.getLast();
		} 
		}
	public ABPig highestPig() 
	{
		ABList pigs = state.findPigs();
		if(pigs.isEmpty())
			return ABPig.nullPig;
		else
		{
			pigs.sortByY();
			return (ABPig)pigs.getFirst();
		} 
		
	}
	public ABPig lowestPig() 
	{ 
		ABList pigs = state.findPigs();
		if(pigs.isEmpty())
			return ABPig.nullPig;
		else
		{
			pigs.sortByY();
			return (ABPig)pigs.getLast();
		} 
	}
	public ABSupport randomSupport(ABPig pig) 
	{ 
		return randomSupport((ABObject)pig);
	}
	public ABSupport randomSupport(ABBlock block)
	{
	    return randomSupport((ABObject)block);
	}
	public ABSupport randomSupport(ABObject abobj)
	{
		ABSupport support = ABSupport.nullSupport;
		ABList blocks = state.findBlocks();
		if(!blocks.isEmpty())
		{
			ABList supporters = getSupports(abobj, blocks);
			if(!supporters.isEmpty())
					support = new ABSupport(supporters.get(randomGenerator.nextInt(supporters.size())));
		}
		return support;
	}
	public ABSupport leftmostSupport(ABPig pig) 
	{ 

		ABSupport support = ABSupport.nullSupport;
		ABList buildingBlocks = state.findBlocks();
		if(!buildingBlocks.isEmpty())
		{
			ABList supporters = getSupports(pig, buildingBlocks);
			if(!supporters.isEmpty())
			{
				supporters.sortByX();
				support = new ABSupport(supporters.getFirst());
				}
		}
	return support;
	}
	public ABSupport rightmostSupport(ABPig pig) 
	{ 
		ABSupport support = ABSupport.nullSupport;
		ABList buildingBlocks = state.findBlocks();
		if(!buildingBlocks.isEmpty())
		{
			ABList supporters = getSupports(pig, buildingBlocks);
			if(!supporters.isEmpty())
			{
				supporters.sortByX();
				support = new ABSupport(supporters.getLast());
				}
		}
	return support;
	}
	public ABSupport highestSupport(ABPig pig) 
	{ 
		ABSupport support = ABSupport.nullSupport;
		ABList buildingBlocks = state.findBlocks();
		if(!buildingBlocks.isEmpty())
		{
			ABList supporters = getSupports(pig, buildingBlocks);
			if(!supporters.isEmpty())
			{
				supporters.sortByY();
				support = new ABSupport(supporters.getFirst());
			}
		}
		return support;
	}
	public ABSupport lowestSupport(ABPig pig) 
	{ 
		ABSupport support = ABSupport.nullSupport;
		ABList buildingBlocks = state.findBlocks();
		if(!buildingBlocks.isEmpty())
		{
			ABList supporters = getSupports(pig, buildingBlocks);
			if(!supporters.isEmpty())
			{
				supporters.sortByY();
				support = new ABSupport(supporters.getLast());
			}
		}
		return support;
	
	}
	public ABType getBirdTypeOnSling() { 
		
		ActionRobot.fullyZoomIn();

		ABState state = ABUtil.getState();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
		ActionRobot.fullyZoomOut();
		ABList _birds = state.findBirds();
		if(_birds.isEmpty())
			return ABType.Unknown;
		_birds.sortByY();
		
		return _birds.get(0).getType();
	 }
	public ABPoint randomPoint() { return new ABPoint(400 + randomGenerator.nextInt(400), 100 + randomGenerator.nextInt(300)); }
	public ABPoint getTarget(ABState state)
	{
		this.state = state;
		return getTarget();
	}

public boolean useHighTrajectory(ABState state)
{
	this.state = state;
	return useHighTrajectory();
}
public int getTapPoint(ABState state)
{
	this.state = state;
	return getTapPoint();
}
public  void runAgent()
{
	ABRunner.runAgent(getClass());
}
public void runControlPanel()
{
	ABRunner.runControlPanel(getClass());
}
public static void runAgent(Class<? extends Strategy> strategyClass, boolean useControlPanel)
{
	if (!useControlPanel)
		ABRunner.runAgent(strategyClass);
	else
		ABRunner.runControlPanel(strategyClass);
}	
//============================== Advanced Functions ==========================
public void updateState()
{
	state = ABUtil.getState();
}

public ABList findBlocks()
{
	return state.findBlocks();
}
public ABList findPigs()
{
	return state.findPigs();
}
public ABList findBirds()
{
	return state.findBirds();
}
	// If o1 supports o2, return true
 public boolean isSupport(ABObject o2, ABObject o1)
	{
		int ex_o1 = o1.x + o1.width;
		int ex_o2 = o2.x + o2.width;
		
		int ey_o2 = o2.y + o2.height;
		if(
			(o1.y - ey_o2 < ABUtil.gap)&&(o1.y - ey_o2 > 0)
			&& 
 			!( o2.x - ex_o1  > ABUtil.gap || o1.x - ex_o2 > ABUtil.gap )
		  )
	        return true;	
		
		return false;
		
	}
	//Return a link list of ABObjects that support o1 (test by isSupport function ). 
	//objs refers to a list of potential supporters.
	//Empty list will be returned if no such supporters. 
 public ABList getSupports(ABObject o2, 
			List<ABObject> objs)
			{
				ABList result = ABList.newList();
				//Loop through the potential supporters
		        for(ABObject o1: objs)
		        {
		        	if(isSupport(o2,o1))
		        		result.add(new ABSupport(o1));
		        }
		        return result;
			}

	/**
	 * @param target: the target point
	 * @param useHighTraj: if true, high trajectory will be applied
	 * @return a boolean (true or false).  If true, no other object on trajectory
	 */
public boolean isReachable(Point target, boolean useHighTraj)
	{
		boolean result = true;
		Rectangle sling = state.findSlingshot();
		Point releasePoint = trajectoryPlanner.getReleasePoint(target, sling, useHighTraj);
		List<Point> points = trajectoryPlanner.predictTrajectory(sling, releasePoint);
		
		//int counter = 10;
		for(Point point: points)
		{
		  if(/*(counter++)%3 == 0 &&*/ point.y < 480 && point.x < 840 && point.y > 100 && point.x > 400)
			for(ABObject ab: findBlocks())
			{
				if( ((ab.contains(point) && !ab.contains(target))||Math.abs(state.vision._scene[point.y][point.x] - 72 ) < 10) 
						&& point.x < target.x
						)
					return false;			
			}
		  
		}
		return result;
	}

public boolean isReachable(ABObject abObject, boolean useHighTraj)
{
	return isReachable(abObject.getCenter(), useHighTraj);

}



public Shot getShot()
{
	return trajectoryPlanner.getShot(state, getTarget(), useHighTrajectory(), getTapPoint());
}
//This method is used in ControlPanel. By clicking "Set Target", we get a target point and we want to use the same target point (we do not want to call getTarget() again) when making a shot. 
public Shot getShot(Point target)
{
	return trajectoryPlanner.getShot(state, target, useHighTrajectory(), getTapPoint());
}


public abstract ABPoint getTarget();
public abstract boolean useHighTrajectory();
public abstract int getTapPoint();

}
