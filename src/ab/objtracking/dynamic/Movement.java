package ab.objtracking.dynamic;

import ab.objtracking.MagicParams;
import ab.vision.ABObject;

public class Movement {
	//TODO add rotation..
	
	public static final int POSITIVE = 1;
	public static final int NEGATIVE = -1;
	public static final int NONSHIFT = 0;
	
	public static final int StrongMovement = 3;
	public static final int NormalMovement = 2;
	public static final int WeakMovement = 1;
	public static final int NoMovement = 0;
	
	public static final int MAX_SCOPE = 1200;
	public static final int BOUNDING_SCOPE = 0;
	public static final int NOT_ALLOWED = -1;
	
	public int lastXshift = MAX_SCOPE;
	public int lastYshift = MAX_SCOPE;
	
	public double lastDistance =  MagicParams.NormalMovementDist;
	
	private int[] allowedXDirection, allowedYDirection;// value: 0: allow, -1: forbid. array[0]: allow Negative shift, array[1]: allow Non shift, array[2]: allow positive shift.
	public int movementType;
	
	public int xDirection;
	public int yDirection;
	
	public ABObject object;
	public boolean remainStatic = false;
	public boolean landMarkMovement = false; // The movement has a higher confidence to be correct


	public void setAllowedXDirection(int[] allowedXDirection)
	{
		this.allowedXDirection = allowedXDirection;
	}
	public void setAllowedXDirection(int allowPositive, int allowNegative, int allowStatic)
	{
		if (allowPositive == BOUNDING_SCOPE)
		{
			allowPositive = getBoundingScopeValue();
		} else
			if (allowPositive == MAX_SCOPE)
				allowPositive = getMaxXScope();
		
		if (allowNegative == BOUNDING_SCOPE)
		{
			allowNegative = getBoundingScopeValue();
		}  
		else
			if (allowNegative == MAX_SCOPE)
				allowNegative = getMaxXScope();

		allowedXDirection[2] = allowPositive;
		allowedXDirection[0] = allowNegative;
		allowedXDirection[1] = allowStatic;
	}
	
	public void setAllowedYDirection(int allowPositive, int allowNegative, int allowStatic)
	{
		if (allowPositive == BOUNDING_SCOPE)
		{
			allowPositive = getBoundingScopeValue();
		} 
		else if(allowPositive == MAX_SCOPE)
			allowPositive = getMaxYScope();
		
		if (allowNegative == BOUNDING_SCOPE)
		{
			allowNegative = getBoundingScopeValue();
		}
		 else  if(allowNegative ==  MAX_SCOPE)
				allowNegative = getMaxYScope();
		
		
		allowedYDirection[2] = allowPositive;
		allowedYDirection[0] = allowNegative;
		allowedYDirection[1] = allowStatic;
	}
	
	public void setAllowedYDirection (int[] allowedYDirection)
	{
		this.allowedYDirection = allowedYDirection;
	}
	public Movement(ABObject object) 
	{
		super();
		this.object = object;
		allowedXDirection = new int[3];
		allowedYDirection = new int[3];
		for (int i = 0; i < 3; i++)
		{
			allowedXDirection[i] = MAX_SCOPE;
			allowedYDirection[i] = MAX_SCOPE;
		}
	}
	public Movement(int xshift, int yshift, ABObject object) {
		super();
		this.object = object;
		allowedXDirection = new int[3];
		allowedYDirection = new int[3];
		for (int i = 0; i < 3; i++)
		{
			allowedXDirection[i] = MAX_SCOPE;
			allowedYDirection[i] = MAX_SCOPE;
		}
	    setDirectionAndType(xshift, yshift);
	    
	}
	public Movement(Movement movement, ABObject object)
	{
		this.object = object;
		int count = -1;
		allowedXDirection = new int[3];
		allowedYDirection = new int[3];
		for (int scope : movement.allowedXDirection){
			count ++;
			if (scope == NOT_ALLOWED)
				allowedXDirection[count] = NOT_ALLOWED;
			else
				allowedXDirection[count] = MAX_SCOPE;
			
		}
		count = -1;
		for (int scope : movement.allowedYDirection){
			count ++;
			if (scope == NOT_ALLOWED )
				allowedYDirection[count] = NOT_ALLOWED;
			else
				allowedYDirection[count] = MAX_SCOPE;
			
		}
		movementType = movement.movementType;
	}
	public void generateInertia(ABObject obj)
	{
		int xshift = (int) (this.object.getCenterX() - obj.getCenterX());
		int yshift = (int) (this.object.getCenterY() - obj.getCenterY());
		int xMovementType = getMovementType(xshift);
		int yMovementType = getMovementType(yshift);
		
		xshift = Math.abs(xshift);
		yshift = Math.abs(yshift);
		double distance = Math.sqrt(xshift * xshift + yshift * yshift);
		int xdirection = getDirection(xshift);
		int ydirection = getDirection(yshift);
		
		movementType = getMovementType(distance);
		
		
		lastXshift = (xshift < getBoundingScopeValue())? NoMovement: Math.abs(xshift);
		lastYshift = (yshift < getBoundingScopeValue())? NoMovement: Math.abs(yshift);
		lastDistance = (distance < getBoundingScopeValue()) ? MagicParams.NormalMovementDist : distance;
		
		
		
		setAllowedXDirection(MAX_SCOPE, MAX_SCOPE, MAX_SCOPE);
		setAllowedYDirection(MAX_SCOPE, MAX_SCOPE, MAX_SCOPE);
		/*if(obj.id == 15)
			System.out.println(yMovementType);*/
		//System.out.println(" initial " + obj + "  y " + this.object.getCenterY() + " oy: " + obj.getCenterY() +" yshift " + yshift + " y direction: " + ydirection);
		if(xMovementType > NormalMovement)
		{
			if(xdirection > 0 )
				setAllowedXDirection(MAX_SCOPE, NOT_ALLOWED, MAX_SCOPE);
			else
				setAllowedXDirection(NOT_ALLOWED, MAX_SCOPE, MAX_SCOPE);
		}
		/*else 
			if (xMovementType > WeakMovement)
			{
				if(xdirection > 0 )
					setAllowedXDirection(MAX_SCOPE, BOUNDING_SCOPE, MAX_SCOPE);
				else
					setAllowedXDirection(BOUNDING_SCOPE, MAX_SCOPE, MAX_SCOPE);
			}*/
		
		
		if (yMovementType > NormalMovement)
		{	
		
			if(ydirection > 0 )
				setAllowedYDirection(MAX_SCOPE, NOT_ALLOWED, MAX_SCOPE);
			else
				setAllowedYDirection(NOT_ALLOWED, MAX_SCOPE, MAX_SCOPE);
	
		} 
		/*else if (yMovementType > WeakMovement)
		{		
				if(ydirection > 0 )
					setAllowedYDirection(MAX_SCOPE, BOUNDING_SCOPE, MAX_SCOPE);
				else
					setAllowedYDirection(BOUNDING_SCOPE, MAX_SCOPE, MAX_SCOPE);
			
		} */
		
			
		
	}
	public void setDirectionAndType(int xshift, int yshift)
	{
		setXDirection(xshift);
		setYDirection(yshift);
		double distance = Math.sqrt(xshift * xshift + yshift * yshift);
		movementType = getMovementType(distance);
		remainStatic = (xDirection== NONSHIFT) && (yDirection == NONSHIFT);
	}
	public int getMovementType(double distance)
	{
		if (distance > MagicParams.StrongMovementDist)
			return StrongMovement;
		else
			if(distance > MagicParams.NormalMovementDist)
				return NormalMovement;
			else if (distance > MagicParams.WeakMovementDist)
					return WeakMovement;
			else
				return NoMovement;
	}
	private void setXDirection(int xshift)
	{
		this.xDirection = getDirection(xshift);
	}
	private void setYDirection(int yshift)
	{
		this.yDirection = getDirection(yshift); 	
		}
	
	private int getDirection(double shift)
	{
		if(shift > 0 )//MagicParams.MovementTolearance)
	    	 return POSITIVE;
	    else if (shift < 0 )// - MagicParams.MovementTolearance)
	    	return  NEGATIVE;
	    	else
	    		return  NONSHIFT;
	}
	
	public boolean isValidMovement(double xshift, double yshift, boolean checkMovementType)
	{
		int xDirection, yDirection;
		xDirection = getDirection(xshift);
		yDirection = getDirection(yshift);
		//System.out.println(" shift " + xshift + " " +xDirection + "  " + allowedXDirection[xDirection + 1]);
		double distance =  Math.sqrt(xshift * xshift + yshift * yshift);
		if(distance < lastDistance * MagicParams.maxIncreaseRatio && allowedXDirection[xDirection + 1] > Math.abs(xshift) && allowedYDirection[yDirection + 1] > Math.abs(yshift) && (!checkMovementType || getMovementType(distance) == movementType))
			return true;
		else
			return false;
		
	}
	/**
	 * @return true if iniObj can move to newObj 
	 * */
	public boolean isValidMovement(ABObject iniObj, ABObject newObj, boolean checkMovementType)
	{
		double xshift = newObj.getCenterX() - iniObj.getCenterX();
		double yshift = newObj.getCenterY() - iniObj.getCenterY();
		return isValidMovement(xshift, yshift, checkMovementType);
	}
	
	public boolean isConflictDirection(Movement movement)
	{
		if (movement.xDirection + xDirection == 0 || movement.yDirection + yDirection == 0)
			return true;
		else
			if ( movement.remainStatic != this.remainStatic)
				return true;
			
			return false;
	}
	public boolean isConflictVelocity(Movement movement)
	{
		return movement.movementType != this.movementType;
	}
	public boolean isSameMovement(Movement movement)
	{
		return !isConflictDirection(movement) || !isConflictVelocity(movement);
	}

   public String toString()
   {
	   StringBuilder result = new StringBuilder();
	   result.append(object);

	    result.append("  X[-] : " + allowedXDirection[0]);
		
	    result.append("	 X[o] : " + allowedXDirection[1] );
		   
	    result.append("  X[+] : " + allowedXDirection[2]);
	
	    result.append("  Y[-] : " + allowedYDirection[0]);
		
		result.append("  Y[o] : " + allowedYDirection[1]);
		   
        result.append("  Y[+] : " + allowedYDirection[2]);
	
	   switch (movementType)
	   {
		   case StrongMovement: result.append(" Strong Movement");break;
		   case NormalMovement: result.append(" Normal Movement"); break;
		   case WeakMovement: result.append(" Weak Movement"); break;
		   case NoMovement: result.append(" NO Movement"); break;
		   default: result.append(" Unclear Movement "); break;
	   }
	   return result.toString();
   }
   
   private int getBoundingScopeValue()
   {
	   //return Math.max(MagicParams.NormalMovementDist, object.getBounds().height/2);
	   return object.getBounds().height/2;
   }
   public int getMaxYScope()
   {
	   if (lastYshift != NoMovement)
	   {
		   return lastYshift * MagicParams.maxIncreaseRatio + MagicParams.WeakMovementDist;
	   } else
		   return MagicParams.NormalMovementDist;
	   
   }
   public int getMaxXScope()
   {
	   if (lastXshift != NoMovement)
	   {
		   return lastXshift * MagicParams.maxIncreaseRatio + MagicParams.WeakMovementDist;
	   } 
	   else
		   return MagicParams.NormalMovementDist;
	  
   }
   public void setAllowedXDirection(int direction, int allowedValue)
   {
	   if (allowedValue == BOUNDING_SCOPE)
	   {
		   allowedValue = getBoundingScopeValue();
	   }
	   else
		   if (allowedValue == MAX_SCOPE)
			   allowedValue = getMaxXScope();
	   allowedXDirection[direction + 1] = allowedValue;
   }
   public void setAllowedYDirection(int direction, int allowedValue)
   {
	   if (allowedValue == BOUNDING_SCOPE)
	   {
		   allowedValue = getBoundingScopeValue();
	   }
	   else
		   if(allowedValue == MAX_SCOPE)
		   {
			   allowedValue = getMaxYScope();
		   }
	   allowedYDirection[direction + 1] = allowedValue;
   }
   
   public int getAllowedXDirection(int direction)
   {
	   return allowedXDirection[direction + 1];
   }
   public int getAllowedYDirection(int direction)
   {
	   return allowedYDirection[direction + 1];
   }
   
public static void main(String[] args) {
		boolean b1 = false;
		boolean b2 = true;
		System.out.println(b1 == b2);

	}

}
