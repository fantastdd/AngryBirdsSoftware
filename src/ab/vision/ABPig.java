package ab.vision;

import java.awt.Rectangle;
import java.util.Random;

import ab.vision.ABObject;
import ab.vision.ABType;

public class ABPig extends ABObject {
	public static final ABPig nullPig = new ABPig(new ABObject(new Rectangle(0,0,1,1),ABType.Pig,-1));	
	private static Random random = new Random();
	public ABPig(Rectangle mbr, ABType type) {
		super(mbr, type);
		// TODO Auto-generated constructor stub
	}

    public ABPig(ABObject obj)
    {
    	super(obj);
    }
	public ABPoint getCenter() {
		
		if(equals(nullPig))
			return new ABPoint( 400 + random.nextInt(400), 100 + random.nextInt(300)); 
		else
			return new ABPoint((int)getCenterX(), (int)getCenterY());
	}
	public boolean isNull()
	{
		return equals(nullPig);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


}
