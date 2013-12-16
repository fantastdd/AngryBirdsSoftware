package example.strategy;

import ab.planner.Strategy;
import ab.vision.ABList;
import ab.vision.ABObject;
import ab.vision.ABPoint;
import ab.vision.ABType;

/**
 * Use this class as a template for writing your own AB strategy.
 * 
 * This template implements very simple-minded choices for each
 * of the three strategic decisions you must make.
 */


public class Hit2ndLeftmostWoodblock extends Strategy {
	/**
	 * @param state The state of the game
	 * @return a point that identifies the target for the bird
	 */
	@Override
	public ABPoint getTarget() {
		

		//Get all the building blocks
		ABList blocks = findBlocks();
		//Sort all the building blocks
		blocks.sortByX();
		//Loop through the blocks and get the 2nd wood block
		int counter = 1;
		for(ABObject block : blocks)
		{
			if(block.getType() == ABType.Wood)
				if(counter == 2)
					return getSupports(block, findBlocks()).getFirst().getCenter();
				else 
					counter++;
		}
		
		return randomPoint();
		
	}

	/**
	 * @param state The state of the game
	 * @return a boolean (true or false).  If true, the bird will
	 * follow a high trajectory.  Otherwise it will follow a low
	 * trajectory.
	 */
	@Override
	public boolean useHighTrajectory() {
		return random(6) == 0;     // return 'true' with 1/6 probability
	}

	/**
	 * @param state The state of the game
	 * @return a number between 0 and 100 that identifies the tap point
	 * as a percentage of the bird's passage along the trajectory.
	 * 0 is the start, 100 is the end, 50 means midway.
	 */
	@Override
	public int getTapPoint() {
		switch (getBirdTypeOnSling()) {
		case RedBird:
			return 0;               // start of trajectory
		case YellowBird:
			return 65 + random(25); // 65-90% of the way
		case WhiteBird:
			return 70 + random(20); // 70-90% of the way
		case BlackBird:
			return 70 + random(20); // 70-90% of the way
		case BlueBird:
			return 65 + random(20); // 65-85% of the way
		default:
			return 60;
		}
	}
	
	/**
	 * The main program.   When you press run (the green 'play' button),
	 * this code will be executed.
	 */
	public static void main(String[] args) {
		boolean useControlPanel = true;
		runAgent(Hit2ndLeftmostWoodblock.class, useControlPanel);
		
		
		//new HitLeftmostPig().runAgent();
	}

}
