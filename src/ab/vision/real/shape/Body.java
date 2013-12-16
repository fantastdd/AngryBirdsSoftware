/**
 * @author      Andrew Wang <u4853279@anu.edu.au>
 */

package ab.vision.real.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;

import ab.vision.ABObject;
import ab.vision.ABPoint;

public abstract class Body extends ABObject
{
   
	private static final long serialVersionUID = 4126732384091164666L;
	
	public Body()
	{
		super();
	}

	// type of the object, specified by Andrew, 
    public int vision_type; 
    // position (x, y) as center of the object
    public double centerX = 0;
    public double centerY = 0;
    public abstract void draw(Graphics2D g, boolean fill, Color boxColor);
    
    public static int round(double i)
    {
        return (int) (i + 0.5);
    }
    @Override
    public ABPoint getCenter()
    {
    	return new ABPoint(centerX, centerY);
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
         //hash = hash * 31 + id;
         hash = hash * 13 + (int)(getCenterX() * 10);
         hash = hash * 29 + (int)(getCenterY() * 10);
        //System.out.println(String.format("%d %d %d %d", (int)(angle * 1000), (int)(getCenterX()*10), (int)(getCenterY()*10), hash));
         return hash;
    }
    @Override
    public boolean equals(Object body)
    {
//    	/System.out.println(hashCode() + "  " + body.hashCode());
    	if ( body instanceof Body)
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
}
