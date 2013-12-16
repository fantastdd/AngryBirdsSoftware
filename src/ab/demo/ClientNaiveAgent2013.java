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

import ab.demo.other.ClientActionRobot;
import ab.demo.other.ClientActionRobotJava;
import ab.demo.other.NaiveMind;
import ab.planner.ExampleTrajectoryPlanner;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
//Naive agent (server/client version)

public class ClientNaiveAgent2013 implements Runnable {

	//focus point
	private int focus_x;
	private int focus_y;
	//Wrapper of the communicating messages
	private ClientActionRobotJava ar;
	public byte currentLevel = -1;
	public int failedCounter = 0;
	public int[] solved;
	ExampleTrajectoryPlanner tp;
	private int id = 28888;
	private boolean firstShot;
	private Point prevTarget;
	/**
	 * Constructor using the default IP
	 * */
	public ClientNaiveAgent2013() {
		// the default ip is the localhost
		ar = new ClientActionRobotJava("127.0.0.1");
		tp = new ExampleTrajectoryPlanner();
		prevTarget = null;
		firstShot = true;

	}
	/**
	 * Constructor with a specified IP
	 * */
	public ClientNaiveAgent2013(String ip) {
		ar = new ClientActionRobotJava(ip);
		tp = new ExampleTrajectoryPlanner();
		prevTarget = null;
		firstShot = true;

	}
	public ClientNaiveAgent2013(String ip, int id)
	{
		ar = new ClientActionRobotJava(ip);
		tp = new ExampleTrajectoryPlanner();
		prevTarget = null;
		firstShot = true;
		this.id = id;
	}
	public int getNextLevel()
	{
		int level = 0;
		boolean unsolved = false;
		//all the levels have been solved, then get the first unsolved level
		for (int i = 0; i < solved.length; i++)
		{
			if(solved[i] == 0 )
			{
					unsolved = true;
					level = (byte)(i + 1);
					if(level <= currentLevel && currentLevel < solved.length)
						continue;
					else
						return level;
			}
		}
		if(unsolved)
			return level;
	    level = (byte)((this.currentLevel + 1)%solved.length);
		if(level == 0)
			level = solved.length;
		return level; 
	}

    /* 
     * Run the Client (Naive Agent)
     */
	public void run() {	
		byte[] info = ar.configure(ClientActionRobot.intToByteArray(id));
		solved = new int[info[2]];
		
		//load the initial level (default 1)
		//Check my score
		int[] _scores = ar.checkMyScore();
		int counter = 0;
		for(int i: _scores)
		{
			System.out.println(" level " + ++counter + "  " + i);
		}
		currentLevel = (byte)getNextLevel(); 
		ar.loadLevel(currentLevel);
		//ar.loadLevel((byte)9);
		GameState state;
		while (true) {
			
			state = solve();
			
			//If the level is solved , go to the next level
			if (state == GameState.WON) {
							
				///System.out.println(" loading the level " + (currentLevel + 1) );
				System.out.println(" My score: ");
				int[] scores = ar.checkMyScore();
				for (int i = 0; i < scores.length ; i ++)
				{
				   
				    	  System.out.print( " level " + (i+1) + ": " + scores[i]);
				    		if(scores[i] > 0)
								solved[i] = 1;
				    		
				}
				System.out.println();
				currentLevel = (byte)getNextLevel(); 
				ar.loadLevel(currentLevel);
				//ar.loadLevel((byte)9);
				//display the global best scores
				scores = ar.checkScore();
				System.out.println("The global best score: ");
				for (int i = 0; i < scores.length ; i ++)
				{
				
					System.out.print( " level " + (i+1) + ": " + scores[i]);
				}
				System.out.println();
				
				// make a new trajectory planner whenever a new level is entered
				tp = new ExampleTrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
				
			} else 
				//If lost, then restart the level
				if (state == GameState.LOST) {
				failedCounter++;
				if(failedCounter > 3)
				{
					failedCounter = 0;
					currentLevel = (byte)getNextLevel(); 
					ar.loadLevel(currentLevel);
					
					//ar.loadLevel((byte)9);
				}
				else
				{		
					System.out.println("restart");
					ar.restartLevel();
				}
						
			} else 
				if (state == GameState.LEVEL_SELECTION) {
				System.out.println("unexpected level selection page, go to the last current level : "
								+ currentLevel);
				ar.loadLevel(currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out
						.println("unexpected main menu page, reload the level : "
								+ currentLevel);
				ar.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out.println("unexpected episode menu page, reload the level: "
								+ currentLevel);
				ar.loadLevel(currentLevel);
			}

		}

	}


	  /** 
	   * Solve a particular level by shooting birds directly to pigs
	   * @return GameState: the game state after shots.
     */
	public GameState solve()

	{

		// capture Image
		BufferedImage screenshot = ar.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);
		
		Rectangle sling = vision.findSlingshotMBR();

		//If the level is loaded (in PLAYING　state)but no slingshot detected, then the agent will request to fully zoom out.
		while (sling == null && ar.checkState() == GameState.PLAYING) {
			System.out.println("no slingshot detected. Please remove pop up or zoom out");
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
			ar.fullyZoomOut();
			screenshot = ar.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshotMBR();
		}

		/*//find birds and pigs
		List<Rectangle> red_birds = vision.findRedBirds();
		List<Rectangle> blue_birds = vision.findBlueBirds();
		List<Rectangle> yellow_birds = vision.findYellowBirds();
		
		int bird_count = 0;
		bird_count = red_birds.size() + blue_birds.size() + yellow_birds.size();

		System.out.println("...found " + pigs.size() + " pigs and "
				+ bird_count + " birds");*/
		List<Rectangle> pigs = vision.findPigsMBR();
		GameState state = ar.checkState();
		int tap_time = 0;
		// if there is a sling, then play, otherwise skip.
		if (sling != null) {
			
			ar.fullyZoomIn();
			screenshot = ar.doScreenShot();
			int bird_type = NaiveMind.getBirdOnSlingShot(screenshot);
			ar.fullyZoomOut();
			
			//If there are pigs, we pick up a pig randomly and shoot it. 
			if (!pigs.isEmpty()) {		
				Point releasePoint;
				{
					// random pick up a pig
					Random r = new Random();

					int index = r.nextInt(pigs.size());
					Rectangle pig = pigs.get(index);
					Point _tpt = new Point((int) pig.getCenterX(),
							(int) pig.getCenterY());

					System.out.println("the target point is " + _tpt);

					// if the target is very close to before, randomly choose a point near it
					if (prevTarget != null && distance(prevTarget, _tpt) < 10) {
						double _angle = r.nextDouble() * Math.PI * 2;
						_tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
						_tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
						System.out.println("Randomly changing to " + _tpt);
					}

					prevTarget = new Point(_tpt.x, _tpt.y);

					// estimate the trajectory
					ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);

					// do a high shot when entering a level to find an accurate velocity
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
					//Get the center of the active bird as focus point 
					focus_x = refPoint.x;
					focus_y = refPoint.y;
					System.out.println("the release point is: " + releasePoint);

					// Get the release point from the trajectory prediction module
					System.out.println("Shoot!!");

					if (releasePoint != null) {
						double releaseAngle = tp.getReleaseAngle(sling,
								releasePoint);
						System.out.println(" The release angle is : "
								+ Math.toDegrees(releaseAngle));
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
						
						System.out.println("tap_time " + tap_time);
						
					} else
						System.err.println("Out of Knowledge");
				}
				
				// check whether the slingshot is changed. the change of the slingshot indicates a change in the scale.
				{
					ar.fullyZoomOut();
					screenshot = ar.doScreenShot();
					vision = new Vision(screenshot);
					Rectangle _sling = vision.findSlingshotMBR();
					if (sling.equals(_sling)) {
						
						
						// make the shot
						if(bird_type == NaiveMind.black_bird)
							ar.shoot(focus_x, focus_y, (int) releasePoint.getX()
									- focus_x, (int) releasePoint.getY() - focus_y,
									0, 0, false);
						else
							ar.shoot(focus_x, focus_y, (int) releasePoint.getX()
									- focus_x, (int) releasePoint.getY() - focus_y,
									0, tap_time, false);
						
						
						// check the state after the shot
						state = ar.checkState();
						// update parameters after a shot is made
						if (state == GameState.PLAYING) {
							screenshot = ar.doScreenShot();
							vision = new Vision(screenshot);
							List<Point> traj = vision.findTrajPoints();
							tp.adjustTrajectory(traj, sling, releasePoint);
							firstShot = false;
						}
					} else
						{
							System.out.println("scale is changed, can not execute the shot, will re-segement the image");
							System.out.println(" Sling: " + sling);
							System.out.println(" _Sling: " + _sling);
						}
				}
			}
		}
		return state;
	}

	private double distance(Point p1, Point p2) {
		return Math.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)* (p1.y - p2.y)));
	}

	public static void main(String args[]) {

		ClientNaiveAgent2013 na;
		if(args.length > 0)
			na = new ClientNaiveAgent2013(args[0]);
		else
			na = new ClientNaiveAgent2013();
		na.run();
		
	}
}
