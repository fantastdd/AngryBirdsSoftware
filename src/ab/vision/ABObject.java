package ab.vision;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.io.Serializable;

import ab.objtracking.MagicParams;
import ab.vision.real.ImageSegmenter;
import ab.vision.real.shape.Rect;
import ab.vision.real.shape.RectType;

public class ABObject extends Rectangle implements Serializable {
 
 private static final long serialVersionUID = 737741268251922637L;
 
 public static final int LEAN_LEFT = -1;
 public static final int LEAN_RIGHT = 1;
 public static final int LEAN_NOLEAN = 0;
 public ABType type;
 private static int counter = 0;
 public final static int unassigned = -1;
 public int id;
 public int area = -1;
 public ABShape shape = ABShape.Rect;
 public RectType rectType = RectType.rec1x1;
 
 public boolean isFat = true;
 public boolean isLevel = true;
 public boolean isDebris = false;
 public int lean = LEAN_NOLEAN;
 
 
 public ABObject originalShape = null;
 public void setOriginalShape(ABObject obj)
 {
	 originalShape = obj.getOriginalShape();
 }
 public ABObject getOriginalShape()
 {
	 if(originalShape == null)
		 originalShape = this;
	 return originalShape;
 }
 // ======= Precise Width/Height =======
 protected double preciseWidth = -1, preciseHeight = -1;
 public double getPreciseWidth()
 {
	 if(preciseWidth != -1)
		 return preciseWidth;
	 return getBounds().width;
 }
 public double getPreciseHeight()
 {
	 if(preciseHeight != -1)
		 return preciseHeight;
	 return getBounds().height;
 }
 //====== attributes added to comply with new vision
 public double angle;
 public Line2D[] sectors;
 
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
public boolean isSameShape(ABObject ao)
{
	if (Math.abs(width - ao.width) < MagicParams.VisionGap && Math.abs(height - ao.height) < MagicParams.VisionGap)
		return true;
	return false;
}

public ABPoint getCenter() {
	
   return new ABPoint(getCenterX(), getCenterY());
}

/**
 * @param rectType: the target rect Type
 * Extend this rectangle of a rect Type to the target. Return the original rectangle when the target rect type is smaller
 * By default, all the shape has rec1x1 recTye
 */
public Rect extend(RectType rectType)
{
	double extensionDegree = (double)rectType.id  - 1;
	Rectangle bounds = getBounds();
	double height = bounds.height * extensionDegree * 2.2 + bounds.height;
	//System.out.println(" height: " + height + " extensionDegree: " + extensionDegree + " rectType" + rectType + " id ");
	int area = (int)(bounds.width * height);
	return new Rect(bounds.getCenterX(), bounds.getCenterY(), bounds.width, height, this.angle, -1 , area);	
}





public static void resetCounter() {
	
	counter = 0;
	
}
public void assignType(int vision_type)
{
	switch(vision_type)
	{
		case ImageSegmenter.PIG: type = ABType.Pig; break;
		case ImageSegmenter.STONE: type = ABType.Stone;break;
		case ImageSegmenter.WOOD: type = ABType.Wood; break;
		case ImageSegmenter.ICE: type = ABType.Ice; break;
		case ImageSegmenter.HILLS: type = ABType.Hill; break;
		default: type = ABType.Unknown;
	}
}
protected void createSectors(Rectangle rec)
{
	sectors = new Line2D[8]; 
    sectors[6] = new Line2D.Float(rec.x + rec.width, rec.y, rec.x + rec.width, rec.y );
    sectors[7] = new Line2D.Float(rec.x + rec.width, rec.y, rec.x , rec.y );
    sectors[0] = new Line2D.Float(rec.x, rec.y, rec.x , rec.y );
    sectors[1] = new Line2D.Float(rec.x, rec.y, rec.x , rec.y + rec.height);
    sectors[2] = new Line2D.Float(rec.x, rec.y + rec.height, rec.x, rec.y + rec.height);
    sectors[3] = new Line2D.Float(rec.x, rec.y + rec.height, rec.x + rec.width, rec.y + rec.height);
    sectors[4] = new Line2D.Float(rec.x + rec.width, rec.y + rec.height, rec.x + rec.width, rec.y + rec.height);
    sectors[5] = new Line2D.Float(rec.x + rec.width, rec.y + rec.height, rec.x + rec.width, rec.y);
    
}



public boolean isLevel()
{
	return isLevel;
}
@Override 
public int hashCode()
{
	 int hash = 1;
     hash = hash * 17 + type.hashCode();
     //hash = hash * 31 + id;
     hash = hash * 13 + (int)getCenterX();
     hash = hash * 67 + (int)getCenterY();
     return hash;
}
@Override
public boolean equals(Object body) {

	return hashCode() == body.hashCode();
}
 
 
}
