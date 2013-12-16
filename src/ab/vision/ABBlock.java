package ab.vision;

import java.awt.Rectangle;
import java.util.Random;

import ab.vision.ABObject;
import ab.vision.ABType;

public class ABBlock extends ABObject {
	public static final ABBlock nullBlock = new ABBlock(new ABObject(new Rectangle(0,0,1,1),ABType.Unknown,-1));	
	private static Random random = new Random();
	public ABBlock(Rectangle mbr, ABType type) {
		super(mbr, type);
		// TODO Auto-generated constructor stub
	}

    public ABBlock(ABObject obj)
    {
    	super(obj);
    }
	public ABPoint getCenter() {
		
		if(equals(nullBlock))
			return new ABPoint( 400 + random.nextInt(400), 100 + random.nextInt(300)); 
		else
			return new ABPoint((int)getCenterX(), (int)getCenterY());
	}
	public boolean isNull()
	{
		return equals(nullBlock);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


}
