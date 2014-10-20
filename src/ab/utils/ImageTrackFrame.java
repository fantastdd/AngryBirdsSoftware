/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2013,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
 **  Sahan Abeyasinghe, Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package ab.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;

import ab.demo.other.ActionRobot;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.ShowSeg;
import ab.vision.Vision;
import ab.vision.VisionUtils;
import ab.vision.real.shape.Poly;

import com.sun.demo.jvmti.hprof.Tracker;

public class ImageTrackFrame {

	private static int _saveCount = 0;
	public static String saveFileDir = "";
	public List<ABObject> objs;
	
	public class ImagePanel extends JPanel implements KeyListener,
			MouseListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3066463463036194802L;
		/**
		 * 
		 */

		protected JFrame _parent;
		protected Image pgImg = null;
		protected Popup _tip = null;
		protected int[][] _meta = null;
		protected Boolean _highlightMode = false;
		protected int _highlightIndex = -1;

		public Boolean bWaitingForKey = false;

		public ImagePanel(JFrame parent) {
			_parent = parent;
			addKeyListener(this);
			addMouseListener(this);
			setDoubleBuffered(true);
			// set focusable then you can use key listener
			setFocusable(true);
			
		}

		public void refresh(Image img) {
			refresh(img, null);
		}

		public void refresh(Image img, int[][] meta) {
			pgImg = img;
			_meta = meta;
			repaint();
		}

		public void paint(Graphics g) {

			if (pgImg != null) {
				if ((_meta != null) && (_highlightIndex != -1)) {

				

					BufferedImage canvas = VisionUtils.highlightRegions(pgImg,
							_meta, _highlightIndex, Color.RED);
					g.drawImage(canvas, 0, 0, null);
				} else {
				
					g.drawImage(pgImg, 0, 0, null);
				}
			}
		}

		public void highlightTarget(Point point) {
			Graphics2D g = (Graphics2D) getGraphics();
			paint(g);
			if (pgImg != null) {

				g.setColor(Color.red);
				g.setStroke(new BasicStroke(3));
				g.drawLine(point.x - 5, point.y - 5, point.x + 5, point.y + 5);
				g.drawLine(point.x + 5, point.y - 5, point.x - 5, point.y + 5);
			}
		}

		public void keyPressed(KeyEvent key) {
			// process key
			if (key.getKeyCode() == KeyEvent.VK_ENTER) {
				_parent.setVisible(false);
				_parent.dispose();

			} else if (key.getKeyCode() == KeyEvent.VK_ESCAPE) {
				System.exit(0);

			} else if (key.getKeyCode() == KeyEvent.VK_D) {
				String imgFilename = saveFileDir
						+ String.format("img%04d.png", _saveCount);
				System.out.println("saving image to " + imgFilename);
				BufferedImage bi = new BufferedImage(pgImg.getWidth(null),
						pgImg.getHeight(null), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = bi.createGraphics();
				g2d.drawImage(pgImg, 0, 0, null);
				g2d.dispose();
				try {
					ImageIO.write(bi, "png", new File(imgFilename));
				} catch (IOException e) {
					System.err.println("failed to save image " + imgFilename);
					e.printStackTrace();
				}

				if (_meta != null) {
					String metaFilename = String.format("meta%04d.txt",
							_saveCount);
					System.out.println("saving meta-data to " + metaFilename);
					try {
						PrintWriter ofs = new PrintWriter(new FileWriter(
								metaFilename));
						for (int i = 0; i < _meta.length; i++) {
							for (int j = 0; j < _meta[i].length; j++) {
								if (j > 0)
									ofs.print(' ');
								ofs.print(_meta[i][j]);
							}
							ofs.println();
						}
						ofs.close();

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				_saveCount += 1;

			} else if (key.getKeyCode() == KeyEvent.VK_H) {
				// toggle highlight mode
				if (_highlightMode) {
					_highlightMode = false;
					this.repaint();
				} else {
					_highlightMode = true;
					_highlightIndex = -1;
				}

			} else if (key.getKeyCode() == KeyEvent.VK_S) {
				String imgFilename = String.format("img%04d.png", _saveCount);
				System.out.println("saving image to " + imgFilename);
				BufferedImage bi = new BufferedImage(pgImg.getWidth(null),
						pgImg.getHeight(null), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = bi.createGraphics();
				g2d.drawImage(pgImg, 0, 0, null);
				g2d.dispose();
				try {
					ImageIO.write(bi, "png", new File(imgFilename));
				} catch (IOException e) {
					System.err.println("failed to save image " + imgFilename);
					e.printStackTrace();
				}
				_saveCount += 1;
			}

			// check if usercode is waiting for a keypress
			if (bWaitingForKey) {
				bWaitingForKey = false;
				return;
			}
		}

		public void keyTyped(KeyEvent key) {

		}

		public void keyReleased(KeyEvent key) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseClicked(MouseEvent e) {
			if (_tip == null) {
				JToolTip toolTip = this.createToolTip();
				if (_meta == null) {
					
					
						for (ABObject obj : objs) {
							Point center = obj.getCenter();
							
							double x = e.getX();
							double y = e.getY();
							double diff = (x - center.getX())
									* (x - center.getX()) + (y - center.getY())
									* (y - center.getY());
							// System.out.println(diff);
							if (diff < 36) {
								toolTip.setTipText(obj.toString());
								break;
							}
						}
					
				} else {
					toolTip.setTipText("(" + e.getX() + ", " + e.getY() + "): "
							+ _meta[e.getY()][e.getX()]);

					if (_highlightMode) {
						_highlightIndex = _meta[e.getY()][e.getX()];
						this.repaint();
					}
				}

				Point p = new Point(e.getX(), e.getY());
				SwingUtilities.convertPointToScreen(p, this);
				_tip = PopupFactory.getSharedInstance().getPopup(this, toolTip,
						p.x, p.y);
				_tip.show();
				toolTip.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (_highlightMode) {
							_highlightIndex = -1;
							repaint();
						}
						_tip.hide();
						_tip = null;
					}
				});
			} else {
				if (_highlightMode) {
					_highlightIndex = -1;
					this.repaint();
				}
				_tip.hide();
				_tip = null;
			}
				
		}
	}

	protected JFrame frame;
	protected ImagePanel panel;
	protected Image gImg;
	protected int[][] meta;
	protected String name;
	protected volatile boolean refresh = false;
	private int bound_x = -1;
	private int bound_y = -1;
	public Tracker tracker = null;
	public boolean initialFrame = false;

	public void setInitialFrame(boolean initialFrame) {
		this.initialFrame = initialFrame;
	}

	public JFrame getFrame() {
		return frame;
	}

	public Tracker getTracker() {
		return tracker;
	}

	public void setTracker(Tracker tracker) {
		this.tracker = tracker;
	}

	public ImageTrackFrame(String name, Image img, int[][] meta, int bound_x,
			int bound_y) {
		this.name = name;
		this.gImg = img;
		this.meta = meta;
		frame = new JFrame(name);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new ImagePanel(frame);

		frame.getContentPane().add(panel);

		frame.pack();
		Insets insets = frame.getInsets();
		frame.setSize(img.getWidth(null) + insets.left + insets.right,
				img.getHeight(null) + insets.top + insets.bottom);
		if (bound_x != -1 && bound_y != -1)
			frame.setBounds(bound_x, bound_y, frame.getSize().width,
					frame.getSize().height);
		else {
			GraphicsEnvironment ge = GraphicsEnvironment
					.getLocalGraphicsEnvironment();
			GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
			Rectangle rect = defaultScreen.getDefaultConfiguration()
					.getBounds();
			int x = (int) rect.getMaxX() - frame.getWidth();
			int y = 0;
			// System.out.println(frame.getWidth());
			frame.setLocation(x, y);

		}
		frame.setVisible(true);
		if (img != null && meta != null)
			panel.refresh(img, meta);
	}
	public void setTitle(String title)
	{
		frame.setTitle(title);
	}
	public ImageTrackFrame(String name, Image img, List<ABObject> objs, int[][] meta) {

		this.name = name;
		this.gImg = img;
		this.meta = meta;
		this.objs = objs;
		
		frame = new JFrame(name);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new ImagePanel(frame);

		frame.getContentPane().add(panel);

		frame.pack();
		Insets insets = frame.getInsets();
		frame.setSize(img.getWidth(null) + insets.left + insets.right,
				img.getHeight(null) + insets.top + insets.bottom);
		if (bound_x != -1 && bound_y != -1)
			frame.setBounds(bound_x, bound_y, frame.getSize().width,
					frame.getSize().height);
		else {
			GraphicsEnvironment ge = GraphicsEnvironment
					.getLocalGraphicsEnvironment();
			GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
			defaultScreen.getDefaultConfiguration()
					.getBounds();
			// int x = (int) rect.getMaxX() - frame.getWidth();
			int x = 0;
			int y = 0;
			frame.setLocation(x, y);

		}
		frame.setVisible(true);
		frame.setResizable(false);
		

		if (img != null && meta != null)
			panel.refresh(img, meta);
		else
			panel.refresh(img);

		// panel.requestFocus();
	}

	public ImageTrackFrame(String name, Image img) {
		this(name, img, new LinkedList<ABObject>(), null);
	}

	// set new image
	public void refresh(Image img) {
		panel.refresh(img);
	}

	public void refreshNow(Image img, int[][] meta) {
		this.gImg = img;
		this.meta = meta;
		refresh = true;
	}

	// set new image and meta information for tooltip
	public void refresh(Image img, int[][] meta) {

		panel.refresh(img, meta);

	}

	public void highlightTarget(Point point) {
		panel.highlightTarget(point);
	}

	// close the window
	public void close() {
		frame.setVisible(false);
		frame.dispose();
	}

	// wait for user input
	public void waitForKeyPress() {
		panel.bWaitingForKey = true;
		while (panel.bWaitingForKey) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	public static void main(String args[]) {
	
		new ActionRobot();
		BufferedImage screenshot = null;
		ImageTrackFrame frame = null;
		screenshot = ActionRobot.doScreenShot();
		Vision vision = new Vision(screenshot);
		List<ABObject> objs = vision.findBlocksRealShape();
		/*for (ABObject obj : objs)
			if (obj instanceof Poly && obj.type != ABType.Hill)
			{
				Poly poly = (Poly)obj;
				System.out.println(poly);
				System.out.println("Points ====");
				for (int i = 0; i < poly.polygon.npoints; i++)
				System.out.println(poly.polygon.xpoints[i] + "  " + poly.polygon.ypoints[i]);
			}*/
		screenshot = ShowSeg.drawRealshape(screenshot);
		frame = new ImageTrackFrame(" Screenshots ", screenshot, objs, null);
		
	}


}