/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2013, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys, Kar-Wai Lim, Zain Mubashir, Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ab.demo.other.ActionRobot;
import ab.demo.other.NaiveMind;
import ab.demo.other.Shot;
import ab.planner.ExampleTrajectoryPlanner;
import ab.utils.StateUtil;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class NaiveAgentTimeLimit implements Runnable {

	private int focus_x;
	private int focus_y;

	private ActionRobot ar;
	public static int currentLevel = 1;
	public static int time_limit = 12;
	ExampleTrajectoryPlanner tp;

	private boolean firstShot;
	private Point prevTarget;
	private int[] scores = new int[63];
	// a standalone implementation of the Naive Agent
	public NaiveAgentTimeLimit() {
		ar = new ActionRobot();
		tp = new ExampleTrajectoryPlanner();
		prevTarget = null;
		firstShot = true;
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();

	}

	public int getCurrent_level() {
		return currentLevel;
	}



	// run the client
	public void run() {

		System.out.println(" Time Limit: " + time_limit + "  initial level: " + currentLevel);
		ar.loadLevel(currentLevel);
		long start_time = System.currentTimeMillis();
		while (true) {
			
			GameState state = solve();
			if (state == GameState.WON) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				int score = StateUtil.checkCurrentScoreSafemode(ar.proxy);
				scores[currentLevel - 1] = score;
				for (int i = 0; i < scores.length; i++)
					System.out.print(" level " + (i + 1) + " : " + scores[i] + "");
				System.out.println();
				start_time = System.currentTimeMillis();
				if(currentLevel == 63)
					break;
				else
					ar.loadLevel(++currentLevel);
				// make a new trajectory planner whenever a new level is entered
				tp = new ExampleTrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
			} 
			else if (state == GameState.LOST) 
			{
				
				System.out.println(" Level is lost, check the timer ");
				long current_time = System.currentTimeMillis();
				long lapsed_time = current_time - start_time;
				if(lapsed_time < time_limit * 60 * 1000)
				{
				
					System.out.println(" " + lapsed_time/1000 + " secs have elapsed" );
					ar.restartLevel();
				}
				else
				{
					if(currentLevel == 63)
						break;
					else{
						System.out.println(" time limit reaches, go to the next level");
						start_time = System.currentTimeMillis();
						ar.loadLevel(++currentLevel);
					}
				}
				//ar.restartLevel();
			} else if (state == GameState.LEVEL_SELECTION) {
				System.out
						.println("unexpected level selection page, go to the lasts current level : "
								+ currentLevel);
				ar.loadLevel(currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out
						.println("unexpected main menu page, go to the lasts current level : "
								+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				ar.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out
						.println("unexpected episode menu page, go to the lasts current level : "
								+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				ar.loadLevel(currentLevel);
			}

		}
		System.out.println(" All 63 levels have been finished, print the scores and exit");
		for (int i = 0; i < scores.length; i++)
			System.out.print(" level " + (i + 1) + " : " + scores[i] + "");
		System.out.println();
		System.exit(0);

	}

	private double distance(Point p1, Point p2) {
		return Math
				.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
						* (p1.y - p2.y)));
	}

	public GameState solve()

	{

		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);

		Rectangle sling = vision.findSlingshotMBR();

		while (sling == null && ar.checkState() == GameState.PLAYING) {
			System.out
					.println("no slingshot detected. Please remove pop up or zoom out");
			ar.fullyZoomOut();
			screenshot = ActionRobot.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshotMBR();
		}

		List<Rectangle> pigs = vision.findPigsMBR();
		

		
		GameState state = ar.checkState();

		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {
			ar.fullyZoomIn();
			screenshot = ar.doScreenShot();
			int bird_type = NaiveMind.getBirdOnSlingShot(screenshot);
			ar.fullyZoomOut();
			if (!pigs.isEmpty()) {

				// Initialise a shot list
				ArrayList<Shot> shots = new ArrayList<Shot>();
				Point releasePoint;
				{
					// random pick up a pig
					Random r = new Random();

					int index = r.nextInt(pigs.size());
					Rectangle pig = pigs.get(index);
					Point _tpt = new Point((int) pig.getCenterX(),
							(int) pig.getCenterY());

					System.out.println("the target point is " + _tpt);

					// if the target is very close to before, randomly choose a
					// point near it
					if (prevTarget != null && distance(prevTarget, _tpt) < 10) {
						double _angle = r.nextDouble() * Math.PI * 2;
						_tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
						_tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
						System.out.println("Randomly changing to " + _tpt);
					}

					prevTarget = new Point(_tpt.x, _tpt.y);

					// estimate the trajectory
					ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);

					// do a high shot when entering a level to find an accurate
					// velocity
					if (firstShot && pts.size() > 1) {
						releasePoint = pts.get(1);
					} else if (pts.size() == 1)
						releasePoint = pts.get(0);
					else {
						// System.out.println("first shot " + firstShot);
						// randomly choose between the trajectories, with a 1 in
						// 6 chance of choosing the high one
						if (r.nextInt(6) == 0)
							releasePoint = pts.get(1);
						else
							releasePoint = pts.get(0);
					}
					Point refPoint = tp.getReferencePoint(sling);
					/* Get the center of the active bird */
					focus_x = refPoint.x;
					focus_y = refPoint.y;
					System.out.println("the release point is: " + releasePoint);
					/*
					 * =========== Get the release point from the trajectory
					 * prediction module====
					 */
					System.out.println("Shoot!!");
					if (releasePoint != null) {
						double releaseAngle = tp.getReleaseAngle(sling,
								releasePoint);
						System.out.println(" The release angle is : "
								+ Math.toDegrees(releaseAngle));
					
						int tap_time = 0;
						switch(bird_type)
						{
							case NaiveMind.black_bird:
							{
								System.out.println(" Bird Type: Black");
								tap_time = 0;
								break;
							}
							case NaiveMind.yellow_bird :
							{
								System.out.println(" Bird Type: Yellow");
								tap_time = tp.getYellowBirdTapTime(sling,releasePoint,_tpt);
								break;
							}
							case NaiveMind.blue_bird :
							{
								System.out.println(" Bird Type: Blue");
								tap_time = tp.getBlueBirdTapTime(sling,releasePoint,_tpt);
								break;
							}
							case NaiveMind.white_bird :
							{
								System.out.println(" Bird Type: White");
								tap_time = tp.getWhiteBirdTapTime(sling,releasePoint,_tpt);
								break;
							}
							default:
							{
								System.out.println(" Bird Type: Red");
								tap_time = tp.getYellowBirdTapTime(sling,releasePoint,_tpt);
								break;
							}
						}
						
						
						shots.add(new Shot(focus_x, focus_y, (int) releasePoint
								.getX() - focus_x, (int) releasePoint.getY()
								- focus_y, 0, tap_time));
					} else
						System.err.println("Out of Knowledge");
				}

				// check whether the slingshot is changed. the change of the
				// slingshot indicates a change in the scale.
				{
					ar.fullyZoomOut();
					screenshot = ActionRobot.doScreenShot();
					vision = new Vision(screenshot);
					Rectangle _sling = vision.findSlingshotMBR();
					if (sling.equals(_sling)) {
						state = ar.shootWithStateInfoReturned(shots);
						// update parameters after a shot is made
						if (state == GameState.PLAYING) {
							screenshot = ActionRobot.doScreenShot();
							vision = new Vision(screenshot);
							List<Point> traj = vision.findTrajPoints();
							tp.adjustTrajectory(traj, sling, releasePoint);
							firstShot = false;
							
						}
					} else
						System.out
								.println("scale is changed, can not execute the shot, will re-segement the image");
				}

			}

		}
		return state;
	}
	 public static String[] extract(String[] commands)
	    {
		   if(commands.length < 2)
			   return null;
		   else
		   {
			   String _option = commands[0];
			   String _value = commands[1];
			  
			   if (_option.equalsIgnoreCase("-t"))
			   {
				   int time_limit = Integer.parseInt(_value);
				   NaiveAgentTimeLimit.time_limit = time_limit;
						   
			   }
			   else
				   if(_option.equalsIgnoreCase("-l"))
				   {
					   int level = Integer.parseInt(_value);
					   NaiveAgentTimeLimit.currentLevel = level;
				   }
		    String[] _commands = new String[commands.length - 2];
		    System.arraycopy(commands, 2, _commands, 0, _commands.length);
		    extract(_commands);
		}
		return null;
		
	    }
	public static void main(String args[]) {

		NaiveAgentTimeLimit na = new NaiveAgentTimeLimit();
		extract(args);
		na.run();

	}
}
