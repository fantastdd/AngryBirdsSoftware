package ab.vision;


public class ABPoint extends  java.awt.Point {

	private static final long serialVersionUID = 3031430109595460264L;
	public ABPoint() { super(); }
	public ABPoint(ABObject object) { super((int) object.getCenterX(), (int) object.getCenterY());	}
	public ABPoint(int x, int y) {super(x,y);}
	public ABPoint(double x, double y)
	{
		super();
		setLocation(x,y);
	}
	
}
