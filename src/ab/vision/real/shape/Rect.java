/**
 * @author      Andrew Wang <u4853279@anu.edu.au>
 */
 
package ab.vision.real.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Line2D;

import ab.objtracking.MagicParams;
import ab.vision.ABObject;
import ab.vision.real.ImageSegmenter;

public class Rect extends Body
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// width and height of the rectangle
    public Polygon p;
    public int widthType;
    //Note Rect's width is not always the Rectangle's width 
    public Rect(double xs, double ys,  double w, double h, double theta, int t)
    {
        vision_type = t;
        
        if (h >= w)
        {
            angle = theta;
            preciseWidth = w;
            preciseHeight = h;
        }
        else
        {
            angle = theta + Math.PI / 2;
            preciseWidth = h;
            preciseHeight = w;
        }
        
        centerY = ys;
        centerX = xs;
        
        width = (int)preciseWidth;
        height = (int)preciseHeight;
        
        
        
        
        area = width * height;
     
        assignType(vision_type);
        createPolygonAndSectors();
        calRecType();
    } 
    
   /* public static void main(String args[])
    {
    	Line2D l = new Line2D.Float(10,10,10,10);
    	Point p = new Point(10,13);
    	System.out.println(l.ptSegDist(p));
    }*/
    private void calRecType()
    {
    	double ratio= preciseHeight/preciseWidth;
    	int round =  (int)Math.round(ratio);
    	rectType = RectType.getType(round);
    	if (rectType != RectType.rec1x1)
    		isFat = false;
    		
    	double _pw = getPreciseWidth();
    		if(_pw < MagicParams.SlimRecWidth)
    			widthType = MagicParams.SlimRecWidth;
    		else
    			if(_pw < MagicParams.NormalRecWidth)
    				widthType = MagicParams.NormalRecWidth;
    			else
    				widthType = MagicParams.FatRecWidth;
    	//Assign Leaning type
    	if (isLevel)
    		lean = LEAN_NOLEAN;
    	else
    	{
    		if ( angle > Math.PI/2)
    			lean = LEAN_RIGHT;
    		else
    			lean = LEAN_LEFT;
    	}
    }
    
    private void createPolygonAndSectors()
    {
    	 sectors = new Line2D[8];
    	 
    	 double angle1 = angle;
         double angle2 = perpendicular(angle1);
         
         // starting point for drawing
         double _xs, _ys;
         _ys = centerY + Math.sin(angle) * preciseHeight / 2 + 
              Math.sin(Math.abs(Math.PI/2 - angle)) * preciseWidth / 2;
         if (angle < Math.PI / 2)
             _xs = centerX + Math.cos(angle) * preciseHeight / 2 -
                 Math.sin(angle) * preciseWidth / 2;
         else if (angle > Math.PI / 2)
             _xs = centerX + Math.cos(angle) * preciseHeight / 2 +
                 Math.sin(angle) * preciseWidth / 2;
         else
             _xs = centerX - preciseWidth / 2;
             
         p = new Polygon();
         p.addPoint(round(_xs), round(_ys));
         
        
         
         _xs -= Math.cos(angle1) * preciseHeight;
         _ys -= Math.sin(angle1) * preciseHeight;
         p.addPoint(round(_xs), round(_ys));
         
       
         
         _xs -= Math.cos(angle2) * preciseWidth;
         _ys -= Math.sin(angle2) * preciseWidth;
         p.addPoint(round(_xs), round(_ys));
         
         
         
         _xs += Math.cos(angle1) * preciseHeight;
         _ys += Math.sin(angle1) * preciseHeight;
         p.addPoint(round(_xs), round(_ys));
   
         if( !(Math.abs(angle - Math.PI/2) < MagicParams.AngleTolerance) && !(Math.abs(angle - Math.PI) < MagicParams.AngleTolerance)
     			&& !(angle < MagicParams.AngleTolerance))
     		isLevel = false;
         
         if (isLevel)
         {
        	 createSectors(getBounds());
         }
         else
         if(angle > Math.PI/2)
         {
        	 sectors[4] = new Line2D.Float(p.xpoints[0], p.ypoints[0], p.xpoints[0], p.ypoints[0]);
        	 sectors[5] = new Line2D.Float(p.xpoints[0], p.ypoints[0], p.xpoints[1], p.ypoints[1]);
             sectors[6] = new Line2D.Float(p.xpoints[1], p.ypoints[1], p.xpoints[1], p.ypoints[1]);
             sectors[7] = new Line2D.Float(p.xpoints[1], p.ypoints[1], p.xpoints[2], p.ypoints[2]);
             sectors[0] = new Line2D.Float(p.xpoints[2], p.ypoints[2], p.xpoints[2], p.ypoints[2]);
             sectors[1] = new Line2D.Float(p.xpoints[2], p.ypoints[2], p.xpoints[3], p.ypoints[3]);
             sectors[2] = new Line2D.Float(p.xpoints[3], p.ypoints[3], p.xpoints[3], p.ypoints[3]);
             sectors[3] = new Line2D.Float(p.xpoints[3], p.ypoints[3], p.xpoints[0], p.ypoints[0]);
         } 
         else
         {
        	 
        	 sectors[4] = new Line2D.Float(p.xpoints[0], p.ypoints[0], p.xpoints[0], p.ypoints[0]);
        	 sectors[3] = new Line2D.Float(p.xpoints[0], p.ypoints[0], p.xpoints[1], p.ypoints[1]);
             sectors[2] = new Line2D.Float(p.xpoints[1], p.ypoints[1], p.xpoints[1], p.ypoints[1]);
             sectors[1] = new Line2D.Float(p.xpoints[1], p.ypoints[1], p.xpoints[2], p.ypoints[2]);
             sectors[0] = new Line2D.Float(p.xpoints[2], p.ypoints[2], p.xpoints[2], p.ypoints[2]);
             sectors[7] = new Line2D.Float(p.xpoints[2], p.ypoints[2], p.xpoints[3], p.ypoints[3]);
             sectors[6] = new Line2D.Float(p.xpoints[3], p.ypoints[3], p.xpoints[3], p.ypoints[3]);
             sectors[5] = new Line2D.Float(p.xpoints[3], p.ypoints[3], p.xpoints[0], p.ypoints[0]);
         }
    }
    @Override
    public Rectangle getBounds()
    {
    	return p.getBounds();
    }
    @Override
    public boolean isSameShape(ABObject ao)
    {
    	if (ao instanceof Rect)
    	{
    		//Rect _rect = (Rect)ao;
    		if (isSameSize(ao))
    				return true;
    	}
    	return false;
    }
   
    
    protected boolean isSameSize(ABObject ao)
    {
    
    	Rect _rect = (Rect)ao;
    	double ratio = ((area > _rect.area)? ((double)area/_rect.area) : ((double)_rect.area/area));
    	if (ao.rectType == RectType.rec8x1 && rectType == ao.rectType /*&& ao.getPreciseWidth() < MagicParams.SlimRecWidth && getPreciseWidth() < MagicParams.SlimRecWidth*/ )
    		return true;
    	if (  ((Math.abs(rectType.id - _rect.rectType.id) < 2 )&&
    				 ( ratio < MagicParams.AreaRatio
    						 || (getPreciseWidth() <= MagicParams.SlimRecWidth && ao.getPreciseWidth() <= MagicParams.SlimRecWidth )
    						 )) || ((Math.abs(preciseWidth - ao.getPreciseWidth()) <= MagicParams.VisionGap)&&(Math.abs(preciseHeight - ao.getPreciseHeight()) <= MagicParams.VisionGap))
    						 
    			
    			)
    			return true;
    		
    		/*double _width = ao.getPreciseWidth();
    		double _height = ao.getPreciseHeight();
    		if( Math.abs(preciseWidth - _width) < MagicParams.VisionGap/5 && Math.abs(preciseHeight - _height) < MagicParams.VisionGap/5);
    			return true;*/
    	
    	return false;
    }
    public Rect(int box[], int t)
    {
        centerX = (box[0] + box[2]) / 2.0;
        centerY = (box[3] + box[1]) / 2.0;
        preciseWidth = box[2] - box[0];
        preciseHeight = box[3] - box[1];
        angle = Math.PI / 2;
        
        if (preciseHeight < preciseWidth)
        {
            preciseWidth = preciseHeight;
            preciseHeight = box[2] - box[0];
            angle = 0;
        }
        vision_type = t;
        assignType(vision_type);
        
        width = (int)preciseWidth;
        height = (int)preciseHeight;
        
        area = width * height;
        createPolygonAndSectors();
        calRecType();
    }
    public Rect(double centerX, double centerY, double width, double height, double angle, int vision_type, int area)
    {
    	  this.centerX = centerX;
    	  this.centerY = centerY;
    	  this.preciseWidth = width;
    	  this.preciseHeight = height;
    	  this.width = (int)width;
    	  this.height = (int)height;
    	  this.vision_type = vision_type;
    	  this.angle = angle;
    	  this.area = area;
    	  createPolygonAndSectors();
          calRecType();    	
    }
    @Override
    public Rect extend(RectType rectType)
    {
    	if(rectType.id <= this.rectType.id)
    	 return this;
    	 
    	double extensionDegree = (double)rectType.id / this.rectType.id - 1;
    	double height = this.preciseHeight * extensionDegree * 2.2 + this.preciseHeight;
    	//System.out.println(" height: " + height + " extensionDegree: " + extensionDegree + " rectType" + rectType + " id ");
    	int area = (int)(this.preciseWidth * height);
    	return new Rect(this.centerX, this.centerY, this.preciseWidth, height, this.angle, this.vision_type, area);	
    }
    
    
    public static void main(String args[])
    {
    	Line2D line = new Line2D.Float(10,10,10,10);
    	Line2D _line = new Line2D.Float(10,11,10,12);
    	System.out.println(_line.ptSegDist(line.getP1()));
    }
    
    /* draw the rectangle onto canvas */
    public void draw(Graphics2D g, boolean fill, Color boxColor)
    {        
    
        
        if (fill) {
            g.setColor(ImageSegmenter._colors[vision_type]);
            g.fillPolygon(p);
        }
        else {
            g.setColor(boxColor);
            g.drawPolygon(p);
        }
    }
    
    public static double perpendicular(double angle)
    {
        return angle > Math.PI / 2 ? angle - Math.PI / 2 : angle + Math.PI / 2;
    }
	
	public String toString()
	{
		return String.format("Rect: id:%d type:%s Area:%d w:%7.3f h:%7.3f a:%3.3f at x:%3.1f y:%3.1f isDebris:%b", id, rectType, area, preciseWidth, preciseHeight, angle, centerX, centerY, isDebris);
	}
}
