package ab.vision;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("serial")
public class ABList extends LinkedList<ABObject> {
	Random r = null;
	public static ABList newList()
	{
		return new ABList();
	}
	public ABObject random()
	{
		if(r == null)
			r = new Random();
		if(isEmpty())
			return null;
		else
			return this.get(r.nextInt(size()));
	}
	public ABList(){
		super();
	}
	public ABList(List<ABObject> list)
	{
		super(list);
	}

/**by
 * Sort the ABObjects according their X coordinate (top-left corner)
 * */
public LinkedList<ABObject> sortByX()
{
	Collections.sort(this, new Comparator<Rectangle>(){

		@Override
		public int compare(Rectangle o1, Rectangle o2) {
			
			return ((Integer)(o1.x)).compareTo((Integer)(o2.x));
		}
		
		
	});
	return this;
}

/**
 * Sort the ABObjects according their Y coordinate (top-left corner)
 * */
public LinkedList<ABObject> sortByY()
{
	Collections.sort(this, new Comparator<Rectangle>(){

		@Override
		public int compare(Rectangle o1, Rectangle o2) {
			
			return ((Integer)(o1.y)).compareTo((Integer)(o2.y));
		}
		
		
	});
	return this;
	
	
}
}
