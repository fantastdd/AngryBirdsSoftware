package example;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import example.strategy.HitLeftmostPig;

import ab.demo.other.ActionRobot;
import ab.planner.ExampleTrajectoryPlanner;
import ab.planner.Strategy;
import ab.utils.ImageSegFrame;
import ab.vision.ABState;
import ab.vision.ABUtil;
import ab.vision.VisionUtils;

public class StarterControlPanel {

	private JFrame frmControlPanel;
	
	private ExampleTrajectoryPlanner tp = null;
	private Strategy exampleStrategy;
	private ImageSegFrame segFrame = null;
	private ABState currentState = null;
	private Point target = null;
	private ActionRobot actionRobot = new ActionRobot();
	private JButton btnScenarioRecognition;
	private JButton btnFindTarget;
	private JButton btnShoot;
	private int panel_x = -1;
	private int panel_y = -1;
	private int segImage_x = -1;
	private int segImage_y = -1;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new StarterControlPanel();
				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public StarterControlPanel() {
		initialize();
		frmControlPanel.setVisible(true);
		exampleStrategy = new HitLeftmostPig();
	}
	public StarterControlPanel(Strategy strategy)
	{
		exampleStrategy = strategy;
		initialize();
		frmControlPanel.setVisible(true);
		
		
	}
	public StarterControlPanel(Strategy strategy, int panel_x, int panel_y,
			int segImage_x, int segImage_y)
	{
		this.panel_x = panel_x;
		this.panel_y = panel_y;
		this.segImage_x = segImage_x;
		this.segImage_y = segImage_y;
		exampleStrategy = strategy;
		initialize();
		frmControlPanel.setVisible(true);
		
		
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmControlPanel = new JFrame();
		frmControlPanel.setTitle("Control Panel");
		if(panel_x != -1 && panel_y != -1)
			frmControlPanel.setBounds(panel_x, panel_y, 445, 80);
		else
			{
				frmControlPanel.setSize(445, 80);
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	  	        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
	  	        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
	  	        int x = (int) rect.getMaxX() - 856 - frmControlPanel.getWidth();
	  	        int y = 0;
	  	        frmControlPanel.setLocation(x, y);
			}
		frmControlPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmControlPanel.getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnAutoRun = new JButton("Auto Run");
		btnAutoRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			
				 btnScenarioRecognition.doClick();
					SwingUtilities.invokeLater(new Runnable() {
					    public void run() {    	
					    	 btnFindTarget.doClick();
					    }
					  });
					SwingUtilities.invokeLater(new Runnable() {
					    public void run() {    	
					    	btnShoot.doClick();
					    }
					  });
				 
			}
		});
		frmControlPanel.getContentPane().add(btnAutoRun);
		
		 btnScenarioRecognition = new JButton("Vision Process");
		//Vision Process: 1. Zoom out 2. Take screenshot 3. Identify Objects
		btnScenarioRecognition.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//Add a loading-image here
				ActionRobot.fullyZoomOut();
				currentState = ABUtil.getState();
				currentState.PrintAllObjects();
				int[][] meta = VisionUtils.computeMetaInformation(currentState.image);			
				if (segFrame == null) {
					segFrame = new ImageSegFrame("Vision Process: Scenario Recognition", VisionUtils.analyseScreenShot(currentState.image), meta
							,segImage_x, segImage_y);			
					
				} else {
					segFrame.refresh(VisionUtils.analyseScreenShot(currentState.image), meta);
				}
			
				
			}
		}); 
		frmControlPanel.getContentPane().add(btnScenarioRecognition);
		
		btnFindTarget = new JButton("Set Target");
		btnFindTarget.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(currentState == null)
					btnScenarioRecognition.doClick();		
				if(exampleStrategy == null)
					exampleStrategy = new HitLeftmostPig();
				//Get the target point
				target = exampleStrategy.getTarget(currentState);
				//System.out.println(target);
				if(target != null)
				{
					segFrame.getFrame().setTitle("Set Target");
					//draw the point on the image segmentation frame
					SwingUtilities.invokeLater(new Runnable() {
					    public void run() {    	
					    	
					    	segFrame.highlightTarget(target);
					    }
					  });
				} else
					JOptionPane.showMessageDialog(null, " The strategy module cannot find the target");
				
			}
		});
		frmControlPanel.getContentPane().add(btnFindTarget);
		
		btnShoot = new JButton("Shoot");
		btnShoot.addActionListener(new ActionListener() {
			//Find a release point according the target
			public void actionPerformed(ActionEvent e) {
			     
				//initialize the trajectory planner
				if (tp == null)
					tp = new ExampleTrajectoryPlanner();
				if(exampleStrategy == null)
					exampleStrategy = new HitLeftmostPig();
				if(target != null)
				{
					//ABState state = ABUtil.getState();
					exampleStrategy.updateState();
				    //tp.getShot(state, target, exampleStrategy.useHighTrajectory(state), exampleStrategy.getTapPoint(state));
				    //System.out.println("Reachable: " + ABUtil.isReachable(state, target, tp.shot));
					//BufferedImage plot = tp.plotTrajectory();
					//System.out.println("Reachable: " + ABUtil.isReachable(exampleStrategy.state, target, exampleStrategy.getShot()));
					//System.out.println("Reachable: " + exampleStrategy.isReachable(target,true));
					exampleStrategy.getShot(target);
					BufferedImage plot = exampleStrategy.trajectoryPlanner.plotTrajectory();
					int[][] meta = VisionUtils.computeMetaInformation(plot);
			    	segFrame.refresh(plot, meta);
			    	segFrame.getFrame().setTitle("Plotting the Trajectory");
			    	
					SwingUtilities.invokeLater(new Runnable() {
						    public void run() {    	
						    	actionRobot.fshoot(exampleStrategy.trajectoryPlanner.shot);	
						    }
						  });
					
				}
				else
					System.out.println(" You need a target before shooting");
				
					
			}
		});
		frmControlPanel.getContentPane().add(btnShoot);
	}
	

}
