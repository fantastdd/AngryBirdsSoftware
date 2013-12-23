package ab.vision;

import java.awt.Point;
import java.awt.Rectangle;

public class ABObject extends Rectangle {
 private static final long serialVersionUID = 1L;
 private static int counter = 0;

 public ABType type;
 public int id;
 public int area = -1;
 public ABShape shape = ABShape.Rect;
 
 //====== attributes added to comply with new vision
 public double angle;
 
public ABObject(Rectangle mbr, ABType type) {
	super(mbr);
	this.type = type;
	this.id = counter++;
}
public ABObject(Rectangle mbr, ABType type, int id) {
	super(mbr);
	this.type = type;
	this.id = id;
}
public ABObject(ABObject ab)
{
	super(ab.getBounds());
	this.type = ab.type;
	this.id = ab.id;
}
public ABObject()
{
	this.id = counter ++;
	this.type = ABType.Unknown;
}
public ABType getType()
{
	return type;
}

public Point getCenter() {
   return new Point((int)getCenterX(), (int)getCenterY());
}


public static void resetCounter() {
	counter = 0;	
}

}
