package ab.vision.real.shape;

public enum RectType {
	rec8x1(8), rec7x1(7), rec6x1(6), rec5x1(5), rec4x1(4), rec3x1(3),rec2x1(2),rec1x1(1),rec0(0);
	public final int id;
	//used for template matching
/*	public static int getHeight(double width)
	{
		
	}*/
	RectType(int id)
	{
		this.id = id;
	}
	public static RectType getType(int ratio)
	{
		switch(ratio)
		{
			case 0: 
			case 1: return rec1x1;
			case 2: return rec2x1;
			case 3: return rec3x1;
			case 4: return rec4x1;
			case 5: return rec5x1;
			case 6: return rec6x1;
			case 7: return rec7x1;
			case 8: return rec8x1;
			default: return rec8x1;
		} 
		
	}
	
}
