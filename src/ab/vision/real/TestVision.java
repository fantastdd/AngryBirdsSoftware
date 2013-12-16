/**
 * @author      Andrew Wang <u4853279@anu.edu.au>
 */
 
package ab.vision.real;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.Timer;

import ab.demo.other.ActionRobot;
import ab.vision.GameStateExtractor;
import ab.vision.GameStateExtractor.GameState;

public class TestVision extends JFrame implements KeyListener, MouseListener, ActionListener, Runnable {
        
    // game related handlers
    private GameStateExtractor gameStateExtractor;
    private ActionRobot ar;
    private BufferedImage screenshot;
    private boolean playing = false;
    
    // timer
    private Timer timer;
    
    // vision output to draw (full detection/original image/classified image/edge image/connected components)
    public final static int FULL = 0;
    public final static int IMAGE = 1;
    public final static int CLASSIFY = 2;
    public final static int EDGE = 3;
    public final static int COMPONENT = 4;
    public final static int NUM_MODE = 5;
    public final static String modeStr[] = {"Detected objects", "Original image", "Labelled image", "Edge image",
                                            "Connected components"};
    private int mode = FULL;
    private String rgbString = "";
    
    public TestVision() {
        super("Test Vision");
        
        // initiate game handlers
        gameStateExtractor = new GameStateExtractor();
        ar = new ActionRobot();
        screenshot = ActionRobot.doScreenShot();
        
        final int nHeight = screenshot.getHeight();
        final int nWidth = screenshot.getWidth();
        
        // set size for the frame
        setBounds(0, 0, nWidth, nHeight);
        setResizable(false);
               
        // add key and mouse listener
        addKeyListener(this);
        addMouseListener(this);
        
        // add timer for repainting
        timer = new Timer(50, this);
        timer.setInitialDelay(1000);
        timer.start();
                
        // make the frame visible
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);      
    }
        
    
    // override paint method
    public void paint(Graphics g)
    {
       
    	File file = new File("t\\img0002.png");
    	try {
			screenshot = ImageIO.read(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//screenshot = ActionRobot.doScreenShot();
        
        if (screenshot != null)
        {
            if (gameStateExtractor.getGameState(screenshot) != GameState.PLAYING)
            {
                g.drawImage(screenshot, 0, 0, null);
                return;
            }
            
            if (mode == FULL)
            {
              //long time = System.nanoTime();
                MyVision vision = new MyVision(screenshot);
                vision.findBirds();
                vision.findObjects();
                vision.findTrajectory();
               // System.out.println(System.nanoTime() - time);
                vision.drawObjects(screenshot, true);
               //System.out.println(System.nanoTime() - time);
            }
            else if (mode == CLASSIFY) 
            {
                ImageSegmenter seg = new ImageSegmenter(screenshot);
                seg.drawClassification(screenshot);
            }
            else if (mode == EDGE)
            {
                ImageSegmenter seg = new ImageSegmenter(screenshot);
                seg.drawEdges(screenshot);
            }
            else if (mode == COMPONENT)
            {
                ImageSegmenter seg = new ImageSegmenter(screenshot);
                seg.drawComponents(screenshot, true);
            }
            else if (mode == IMAGE)
            {
                ImageSegmenter seg = new ImageSegmenter(screenshot);
                seg.drawImage(screenshot);
            }
            drawMode(screenshot);
            g.drawImage(screenshot, 0, 0, null);
        }        
    }
    
    
    // launch shot when Enter is pressed
    public void keyPressed(KeyEvent e) 
    {
        if (e.getKeyCode() == KeyEvent.VK_SPACE)
        {
            // switch between modes
            mode = (mode + 1) % NUM_MODE;
        }
    }
    
    // timer event
    public void actionPerformed(ActionEvent e)
    {   
        if (e.getSource() == timer)
        {
            screenshot = ar.doScreenShot();
            GameState state = gameStateExtractor.getGameState(screenshot);
            
            if (state == GameState.PLAYING)
            {
                // a new level is entered, fully zoomed out
                if (!playing)
                {
                    System.out.println("zooming out");
                    ar.fullyZoomOut();
                    playing = true;
                }
            }
            else if (playing)
            {
                // reset playing flag
                playing = false;
            }
            //System.out.println(state);
            repaint();
        }
    }
    
    // Fix release point or tap point depending on part of the image clicked
    public void mouseClicked(MouseEvent e)
    {
        screenshot = ar.doScreenShot();
        Color color = new Color(screenshot.getRGB(e.getX(), e.getY()));
        
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int hue = ImageSegmenter.getHue(r, g, b);
        int sat = ImageSegmenter.getSaturation(r, g, b);
        int val = ImageSegmenter.getValue(r, g, b);
        rgbString = String.format("(%d, %d): {r %d, g %d, b %d}; {hue %d, sat %d, val %d},",
                            e.getX()+264, e.getY()+156, r, g, b, hue, sat, val);
        //System.out.println(rgbString);
        System.out.println((r << 10) | (g << 5) | b);
    }
    
    
    public void mousePressed(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseReleased(MouseEvent e){}
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
    
    private void drawMode(BufferedImage canvas)
    {
        Graphics2D g = canvas.createGraphics();
        g.setColor(Color.RED);
        g.drawString(modeStr[mode], 350, 20);
    }
    
    public void run()
    {
        
    }
    
    public static void main(String args[]) {
        System.out.println("Usage java ab.demo.TestVision");
        System.out.println("Press SPACE to change different views\n");
        
        TestVision tv = new TestVision();
    }
}
