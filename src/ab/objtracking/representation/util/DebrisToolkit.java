package ab.objtracking.representation.util;

import java.awt.Point;
import java.awt.Polygon;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;

import ab.objtracking.MagicParams;
import ab.objtracking.representation.ConstraintEdge;
import ab.objtracking.representation.Relation;
import ab.vision.ABObject;
import ab.vision.ABShape;
import ab.vision.ABType;
import ab.vision.real.shape.DebrisGroup;
import ab.vision.real.shape.Rect;
import ab.vision.real.shape.RectType;

public class DebrisToolkit {

	
	public static List<DebrisGroup> getAllDummyRectangles(DirectedGraph<ABObject, ConstraintEdge> network)
	{
		List<DebrisGroup> debrisList = new LinkedList<DebrisGroup>();
		Set<ABObject> vertices = network.vertexSet();
		
		//Set<ABObject> usedDebris = new HashSet<ABObject>();
		
		for (ABObject obj : vertices)
		{
			if(obj.rectType == RectType.rec8x1 || obj.type == ABType.Pig)
				continue;
			else
			{
				Set<ConstraintEdge> edges = network.edgesOf(obj);
				for (ConstraintEdge edge : edges)
				{
					ABObject target = edge.getTarget();
					
					if(obj.id < target.id && obj.type == target.type && canBeSameDebrisGroup(obj, target, edge.label) )
					{
						DebrisGroup debris = debrisReconstruct(obj, target, edge.label);
						
						if(debris != null)
						{
							if(debris.p.contains(obj.getCenter()) && debris.p.contains(target.getCenter()))
							{	
								debrisList.add(debris);
								//usedDebris.add(obj);
								//usedDebris.add(target);
							}
						}
					}
				}
			}
		}
		
		return debrisList;
	}
	/**
	 * Recover the shape of the original from two debris (the original shape is maintained)
	 * only recover rec8*1 
	 * **/
	public static DebrisGroup debrisReconstruct(ABObject o1, ABObject o2)
	{
		if(o1.shape != ABShape.Rect && o2.shape != ABShape.Rect)
			return null;
		if(o1.isLevel && o2.isLevel && ((o1.getPreciseWidth() > MagicParams.SlimRecWidth ) || (o2.getPreciseWidth()> MagicParams.SlimRecWidth)))
				return null;
		if(o1.getOriginalShape().rectType.id < RectType.rec6x1.id)
			return null;
		double orientationDiff = Math.abs(o1.angle - o2.angle);
		double angle = getAngle(o1.getCenterX() - o2.getCenterX(), o1.getCenterY() - o2.getCenterY());
		double diff = Math.abs(angle - o1.angle);
		
		if(sameAngle(diff)&& sameAngle(orientationDiff)) 
		{
			double centerX = ( o1.getCenterX() + o2.getCenterX() )/2;
			double centerY = ( o1.getCenterY() + o2.getCenterY() )/2;
			ABObject originalShape = o1.getOriginalShape();
			DebrisGroup debris = new DebrisGroup(centerX, centerY, originalShape.getPreciseWidth(), originalShape.getPreciseHeight(), 
					angle, -1, (int)(originalShape.getPreciseHeight() * originalShape.getPreciseWidth()));
			debris.type = o1.type;
			debris.id = o1.id;
			debris.addMember(o1);
			debris.addMember(o2);
			return debris;
			
		}
		
		return null;
	}
	
	/**
	 * Recover the shape of the original from two pieces
	 * */
	public static DebrisGroup debrisReconstruct(ABObject o1, ABObject o2, Relation o1Too2)
	{
		DebrisGroup debris;
		if (!o1.isLevel)
		{
			Relation leftpart = Relation.getLeftpart(o1Too2);
			   
				double centerX = (leftpart == Relation.S8 || leftpart == Relation.S6)?( o1.sectors[2].getX1() + o2.sectors[6].getX1())/2: ( o1.sectors[6].getX1() + o2.sectors[2].getX1())/2;
				double centerY = (leftpart == Relation.S8 || leftpart == Relation.S6)? (o1.sectors[4].getY1() + o2.sectors[0].getY1())/2 :  (o1.sectors[0].getY1() + o2.sectors[4].getY1())/2;
				double width = o1.getPreciseWidth();
				double height = o1.getPreciseHeight() + o2.getPreciseHeight();
				double angle = o1.angle;
				int area = (int)(width * height);
			/*	if( o2 instanceof Rect)
				{
					if( area < ((Rect)o2).area )
					{	System.out.println("Error : " + o1 + "  " + o2);
						System.exit(0);
					}
				}*/
					
			/*	System.out.println(o1);
				System.out.println(String.format("centerX: %.2f centerY: %.2f width: %.2f height: %.2f", centerX, centerY, width, height));*/
				debris = new DebrisGroup(centerX, centerY, width, height, angle, -1,area);
				debris.type = o1.type;
				debris.addMember(o1);
				debris.addMember(o2);
				return debris;
			
		}
		else
		{
			Relation rightpart = Relation.getRightpart(o1Too2);
			
				double centerX = (rightpart == Relation.S8 || rightpart == Relation.S6)?( o2.sectors[2].getX1() + o1.sectors[6].getX1())/2: ( o2.sectors[6].getX1() + o1.sectors[2].getX1())/2;
				double centerY = (rightpart == Relation.S8 || rightpart == Relation.S6)? (o2.sectors[4].getY1() + o1.sectors[0].getY1())/2 :  (o2.sectors[0].getY1() + o1.sectors[4].getY1())/2;
				double width = o2.getPreciseWidth();
				double height = o1.getPreciseHeight() + o2.getPreciseHeight();
				double angle = o2.angle;
				int area = (int)(width * height);
				debris = new DebrisGroup(centerX, centerY, width, height, angle, -1,area);
				debris.type = o2.type;
				debris.addMember(o1);
				debris.addMember(o2);
				return debris;
			
		}
		
	}
	public static Rect debrisReconstruct(List<ABObject> group) 
	{
		if (group.size() == 1)
		{
			ABObject obj = group.get(0);

			Rect rec = new Rect(obj.getCenterX(), obj.getCenterY(),
					obj.getOriginalShape().getPreciseWidth(), obj.getOriginalShape().getPreciseHeight(), -1 , obj.getOriginalShape().area);
			rec.type = obj.type;
		}
		else
			if (group.size() == 2)
			{
				
				ABObject member1 = group.get(0);
				ABObject member2 = group.get(1);
				//log(" ^^^ " + member1 + "\n" + member2);
				if(canFormDebris(member1, member2))
				{
					//log("&&");
					ABObject originalShape = member1.getOriginalShape();
					double x = (member1.getCenterX() + member2.getCenterX())/2;
					double y = (member1.getCenterY() + member2.getCenterY())/2;
					double angle = divide( (member1.getCenterY() - member2.getCenterY()), (member1.getCenterX() - member2.getCenterX()));
					angle = Math.atan(angle);
					angle = (angle > 0)? angle : (Math.PI - angle);
					Rect rect = new Rect(x, y, originalShape.getPreciseWidth(), originalShape.getPreciseHeight(), angle, -1, originalShape.area);
					rect.type = member1.type;
					return rect;
				}
			} 
			else 
			{
			   double x, y, angle;
			   x = y = angle = 0;
			   ABObject originalShape = null;
			   ABType type = null;
			   for (int i = 0; i < group.size() - 1; i++)
			   {
				   ABObject member1 = group.get(i);
				   type = member1.type;
				   x += member1.getCenterX();
				   y += member1.getCenterY();
				   originalShape = member1.getOriginalShape();
				   for (int j = i + 1; j < group.size(); j++)
				   {
					   ABObject member2 = group.get(j);
					   if(canFormDebris(member1, member2))
					   {
						    angle = divide( (member1.getCenterY() - member2.getCenterY()), (member1.getCenterX() - member2.getCenterX()));
							angle = Math.atan(angle);
							angle = (angle > 0)? angle : (Math.PI - angle);
					   }
					   else
						   return null;
				   }
			   }
			   x = x/group.size();
			   y = y/group.size();
			   Rect rect = new Rect(x, y, originalShape.getPreciseWidth(), originalShape.getPreciseHeight(), angle, -1, originalShape.area);
			   rect.type = type;
			   return rect;
			   
			}

		return null;
	}
	
	private static double divide(double x, double y)
	{
		if ( y == 0)
			return Double.MAX_VALUE;
		else
			return x/y;
	}
	/**
	 * @return true if o1 and o2 can form a same object without considering the type and other info
	 * */
	private static boolean canFormDebris(ABObject o1, ABObject o2)
	{
		double orientationDiff = Math.abs(o1.angle - o2.angle);
		double angle = getAngle(o1.getCenterX() - o2.getCenterX(), o1.getCenterY() - o2.getCenterY());
		double diff = Math.abs(angle - o1.angle);
		if(sameAngle(orientationDiff) && sameAngle(diff)
				&&  distance(o1.getCenter(), o2.getCenter()) < MagicParams.maximumHeight)
		{
			return true;
		}
		
		return false;
	}
	private static double distance(Point p1, Point p2)
	{
		return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)); 
	}
	protected static void log(String message){ System.out.println(message);}
	/*
	 * Determine whether two objects are likely to be of the same debris group. Only consider rotated rectangles otherwise most of stacked leveled objects will be considered as debris
	 * **/
	public static boolean canBeSameDebrisGroup(ABObject o1, ABObject o2, Relation o1Too2)
	{
		//(o1.)
		if(o1.shape != ABShape.Rect && o2.shape != ABShape.Rect)
			return false;
		if(o1.isLevel && o2.isLevel && ((o1.getPreciseWidth() > MagicParams.SlimRecWidth ) || (o2.getPreciseWidth()> MagicParams.SlimRecWidth)))
				return false;
		/*if(o1.id == 10 && o2.id == 11)
			System.out.println( o1 + "  " + o2 + "  " +  sameAngle(Math.abs(o1.angle - o2.angle) ));*/;
		double orientationDiff = Math.abs(o1.angle - o2.angle);
		double angle = getAngle(o1.getCenterX() - o2.getCenterX(), o1.getCenterY() - o2.getCenterY());
		double diff = Math.abs(angle - o1.angle);
		
		if(sameAngle(diff)) /*&& (o1.getPreciseWidth() < MagicParams.SlimRecWidth ) && (o2.getPreciseWidth()< MagicParams.SlimRecWidth)*///since circle are also "level", but circle some times are just tiny mis-detected rectangles which are highly likely to be debris
		{
		
		   
		   if(sameAngle(orientationDiff)|| Math.abs(o2.getPreciseHeight() - o1.getPreciseWidth()) < MagicParams.VisionGap ){
			
			   Relation left = Relation.getLeftpart(o1Too2);
			if(o1.angle < Math.PI/2)
			{
				
				if(left == Relation.S2 || left == Relation.S6
						||  left == Relation.R2 || left == Relation.R6)
					return true;
			}
			else
				if(o1.angle > Math.PI/2)
				{
					
					if(left == Relation.S4 || left == Relation.S8
							||  left == Relation.R4 || left == Relation.R8)
						return true;
				}
		   }		
		}
		return false;
	}
	/*
	 * Return true if debris and newObj can form initialObj
	 * 
	 * If debris and newObj can form a bigger shape, but the shape is bigger than initialObj, then return true;
	 * **/
	public static boolean isSameDebris(ABObject debris, Rect initialObj, ABObject newObj)
	{
		if(debris.type == newObj.type)
		{
			/*if(debris.isSameShape(initialObj))
				return false;*/
			
			Rect dummy = debris.extend(initialObj.rectType);
			//Relation r = GSRConstructor.computeRectToRectRelation(debris, initialObj);
			Polygon p = dummy.p;
			if(p.contains(newObj.getCenter()))// && newObj instanceof Rect)//damage detection only supports rect currently
			{
				//Inverse Check
				dummy = newObj.extend(initialObj.rectType);
				if(dummy.p.contains(debris.getCenter()))
				{
					//Spatial Consistency Check
					double angle, diff, orientationDiff;
					double contactWidth, contactHeight;
					if(debris.shape != ABShape.Circle)
					{
						angle = getAngle(debris.getCenterX() - newObj.getCenterX(), debris.getCenterY() - newObj.getCenterY());
						diff = Math.abs(angle - debris.angle);
					    contactWidth = debris.getPreciseWidth();
					    contactHeight = newObj.getPreciseHeight();
					}
					else
					{
						angle = getAngle(newObj.getCenterX() - debris.getCenterX(), newObj.getCenterY() - debris.getCenterY());
						diff = Math.abs(angle - newObj.angle);
						contactWidth = newObj.getPreciseWidth();
						contactHeight = debris.getPreciseHeight();
						
					}
					if(debris.shape == ABShape.Circle || newObj.shape == ABShape.Circle)
						orientationDiff = 0;
					else
						orientationDiff = Math.abs(debris.angle - newObj.angle);
					/*if(debris.id == 2)
					{
						System.out.println(" debris " + debris + " newObj " + newObj + " angle " + angle + " diff " + diff );
					}*/
					//
					 if(sameAngle(diff) && (sameAngle(orientationDiff)|| Math.abs(contactWidth - contactHeight) < MagicParams.VisionGap ))
					{
						return true;
					}
					return false;
				}
			}
		}
		return false;
	}
	private static boolean sameAngle(double diff)
	{
		return Math.abs(diff - Math.PI) < MagicParams.AngleTolerance * 2 || diff < MagicParams.AngleTolerance * 2;
	}
	private static double getAngle(double x, double y)
	{
		if (y < 0)
		{
			x = -x;
			y = -y;
		}
		return Math.atan2(y, x);
			
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Rect rect1 = new Rect(578.0, 306.5, 5.691, 31.382, 1.696, -1, 155);
		Rect rect2 = new Rect(575.0, 328.0, 5.587, 12.281, 1.696, -1, 60);
		System.out.println(canBeSameDebrisGroup(rect1, rect2, GSRConstructor.computeRectToRectRelation(rect1, rect2).r));
	}
	
	

}
