/**
 * @author      Andrew Wang <u4853279@anu.edu.au>
 */

package ab.vision.real.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import ab.vision.ABObject;

public abstract class Body extends ABObject

{
	private static final long serialVersionUID = 1L;
	public Body()
	{
		super();
	}
    // position (x, y) as center of the object
    public double centerX = 0;
    public double centerY = 0;
    protected double preciseWidth = -1, preciseHeight = -1;
    
    public double getPreciseWidth()
    {
	   	 if(preciseWidth != -1)
	   		 return preciseWidth;
	   	 return width;
    }
    
    public double getPreciseHeight()
    {
   	 if(preciseHeight != -1)
   		 return preciseHeight;
   	 return height;
    }
    
    public static int round(double i)
    {
        return (int) (i + 0.5);
    }
    @Override
    public Point getCenter()
    {
    	Point point = new Point();
    	point.setLocation(centerX, centerY);
    	return point;
    }
    @Override
    public double getCenterX()
    {
    	return centerX;
    }
    @Override 
    public double getCenterY()
    {
    	return centerY;
    }
    @Override
    public int hashCode() {
    	 int hash = 1;
         hash += shape.hashCode();
         hash += hash * 3 + type.hashCode();
         hash = hash * 7 + (int)(angle * 1000);
         hash = hash * 13 + (int)(getCenterX() * 10);
         hash = hash * 29 + (int)(getCenterY() * 10);
         return hash;
    }
    @Override
    public boolean equals(Object body)
    {
    	if (body instanceof Body)
    	{
    		Body b = (Body)body;
    		if(getPreciseWidth() == b.getPreciseWidth() && getPreciseHeight() == b.getPreciseHeight() 
    				&& shape == b.shape && type == b.type && getCenterX() == b.getCenterX() && getCenterY() == b.getCenterY())
    			return true;
    		return false;
    	}
    	else
    		return hashCode() == body.hashCode();
    }
    public abstract void draw(Graphics2D g, boolean fill, Color boxColor);
}
