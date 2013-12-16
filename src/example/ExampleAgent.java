/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2013, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys, Kar-Wai Lim, Zain Mubashir, Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package example;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.ExampleTrajectoryPlanner;
import ab.planner.Strategy;
import ab.utils.ImageSegFrame;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.ABState;
import ab.vision.ABUtil;
import ab.vision.GameStateExtractor.GameState;
import example.strategy.HitRandomPig;
//An example agent that will loop through 1 - 21 levels. 
public class ExampleAgent implements Runnable {


	private ActionRobot aRobot;
	public int currentLevel = 1;
	private HashMap<Integer,Integer> scores = new HashMap<Integer,Integer>();

	
	private Strategy strategy;
	
	
	private int shotCount = 0;

	public ExampleAgent() {
		aRobot = new ActionRobot();
		
		strategy = new HitRandomPig();
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();
		
	
	}
	public ExampleAgent(Strategy strategy)
	{
		aRobot = new ActionRobot();
		
		this.strategy = strategy;
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();
	
	}



	// run the client
	public void run() {

		aRobot.loadLevel(currentLevel);
		while (true) {
		
			GameState state = solve();
			
			if (state == GameState.WON) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				int score = StateUtil.checkCurrentScoreSafemode(ActionRobot.proxy);
				if(!scores.containsKey(currentLevel))
						scores.put(currentLevel, score);
				else
				{
					if(scores.get(currentLevel) < score)
						scores.put(currentLevel, score);
				}
				int totalScore = 0;
				for(Integer key: scores.keySet()){
				
					totalScore += scores.get(key);
					System.out.println(" Level " + key
						+ " Score: " + scores.get(key) + " ");
				}
				System.out.println("Total Score: " + totalScore);
				
				shotCount = 0;
				aRobot.loadLevel(++currentLevel);
				// make a new trajectory planner whenever a new level is entered
				strategy.trajectoryPlanner = new ExampleTrajectoryPlanner();

				
		
			} else if (state == GameState.LOST) {
				System.out.println("restart");
				//aRobot.restartLevel();
				shotCount = 0;
				aRobot.loadLevel(++currentLevel);
				// make a new trajectory planner whenever a new level is entered
				strategy.trajectoryPlanner = new ExampleTrajectoryPlanner();

				
			} else if (state == GameState.LEVEL_SELECTION) {
				System.out
						.println("unexpected level selection page, go to the last current level : "
								+ currentLevel);
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out
						.println("unexpected main menu page, go to the last current level : "
								+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out
						.println("unexpected episode menu page, go to the last current level : "
								+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				shotCount = 0;
				aRobot.loadLevel(currentLevel);
			}

		}

	}


	public GameState solve()

	{
		
		// get the state of the current game
		ABState state = ABUtil.getState();
		
		// find the sling
		Rectangle sling = state.findSlingshot();

		// confirm the sling
		while (sling == null && state.getGameState() == GameState.PLAYING) {
			System.out
					.println("no slingshot detected. Please remove pop up or zoom out");
			ActionRobot.fullyZoomOut();
			state = ABUtil.getState();
			sling = state.findSlingshot();
		}
	
		// if there is a sling, then play
		if (sling != null) 
		{
				state = ABUtil.getState();
				Point target = strategy.getTarget(state);
				
				if(target != null) {
			
					//Shot shot =  trajectoryPlanner.getShot(state, target, strategy.useHighTrajectory(state), strategy.getTapPoint(state));
					Shot shot = strategy.getShot();
					// check whether the slingshot is changed. the change of the Slingshot indicates a change in the scale.
					{
						ActionRobot.fullyZoomOut();
						state = ABUtil.getState();
						ABObject _sling = state.findSlingshot();
						if(_sling != null){
						
						double scale_diff = Math.pow((sling.width - _sling.width),2) +  Math.pow((sling.height - _sling.height),2);
						//Check whether a significant scale change happens
						if (scale_diff < 25) {
							
							//execute the shot
							aRobot.cFastshoot(shot);
							try {
								takeScreenshots(shotCount++);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							state = ABUtil.getState();
							
							// update parameters after a shot is executed
							if (state.getGameState() == GameState.PLAYING) 
							{			
								List<Point> traj = state.findTrajPoints();
								strategy.trajectoryPlanner.adjustTrajectory(traj, sling);
								//trajectoryPlanner.adjustTrajectory(traj, sling);
							}
						} 
						else
							System.out.println("scale is changed, can not execute the shot, will re-segement the image");
						}else
							System.out.println("no sling detected, can not execute the shot, will re-segement the image");
						
							
					}
				}
				
		
				
		}
		else
			state = ABUtil.getState();
	
		return state.getGameState();
	}
	
	public void takeScreenshots(int shotCount) throws IOException
	{
		int count = 201;
		List<BufferedImage> images = new LinkedList<BufferedImage>();
		BufferedImage screenshot = null;
		long time = 0;
		long avg = 0;
		while(count-- > 0)
		{
			time = System.nanoTime();
			screenshot = ActionRobot.doScreenShot();
			images.add(screenshot);
			avg += (System.nanoTime() - time);
		}
		String saveFileDir = "L" + currentLevel + "_" + shotCount + "_" + (avg/images.size()/1000000) + "\\";
		int _saveCount = 0;
		File file = new File(saveFileDir);
    		if(!file.exists())
    			file.mkdir();
    	for (BufferedImage image : images)
    	{
    		String imgFilename = saveFileDir + String.format("img%04d.png", _saveCount ++);
    		ImageIO.write(image, "png", new File(imgFilename));
    	}
	} 
	
	public static void main(String args[]) {

		ExampleAgent na = new ExampleAgent();
		if (args.length > 0)
			na.currentLevel = Integer.parseInt(args[0]);
		na.run();

	}
}
