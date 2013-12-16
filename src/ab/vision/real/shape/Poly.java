package ab.vision.real.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import ab.objtracking.MagicParams;
import ab.vision.ABObject;
import ab.vision.ABShape;
import ab.vision.real.ImageSegmenter;
import ab.vision.real.LineSegment;
/**
 * @author      Andrew Wang <u4853279@anu.edu.au>
 */

public class Poly extends Body
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3628493742939819604L;
	public Polygon polygon = null;
	
    
    public Poly(ArrayList<LineSegment> lines, int left, int top, int t, double xs, double ys)
    {
        polygon = new Polygon();
        vision_type = t;
        assignType(vision_type);
        shape = ABShape.Poly;
        if (lines != null)
        {
            for (LineSegment l : lines)
            {
                Point start = l._start;
                polygon.addPoint(start.x + left, start.y + top);
            }
        }
        centerX = xs;
        centerY = ys;
        angle = 0;
        area = getBounds().height * getBounds().width;
        createSectors(getBounds());
        
    }
    @Override
    public boolean isSameShape(ABObject ao)
    {
    	if (ao instanceof Poly)
    	{
    		Polygon _polygon = ((Poly)ao).polygon;
    		if(
    				Math.abs( polygon.getBounds().width -
    						_polygon.getBounds().width) < MagicParams.VisionGap 
    						&& 
    						Math.abs( polygon.getBounds().height -
    	    						_polygon.getBounds().height) < MagicParams.VisionGap
    	    	)
    			return true;
    	
    			
    	}
    	else
    	{
    		if(isDebris)
    			{
    				return ao.isSameShape(getOriginalShape());
    			}
    		
    	}
    	return false;
    }
    @Override
    public Rectangle getBounds()
    {
    	return polygon.getBounds();
    }
    public void draw(Graphics2D g, boolean fill, Color boxColor)
    {
        if (fill) {
            g.setColor(ImageSegmenter._colors[vision_type]);
            g.fillPolygon(polygon);
        }
        else {
            g.setColor(boxColor);
            g.drawPolygon(polygon);
        }
    }
	
	public String toString()
	{
		return String.format("Poly: id:%d %s %dpts at x:%3.1f y:%3.1f isDebris:%b", id, type, polygon.npoints, centerX, centerY, isDebris);
	}
}
