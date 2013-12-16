package ab.objtracking.tracker;

import java.awt.Point;
import java.awt.Shape;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ab.objtracking.Tracker;
import ab.objtracking.dynamic.Movement;
import ab.objtracking.representation.util.GlobalObjectsToolkit;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.real.shape.Circle;
import ab.vision.real.shape.Rect;
import ab.vision.real.shape.RectType;

public abstract class TrackerTemplate implements Tracker{

	List<ABObject> iniObjs = null;
	List<ABObject> lastInitialObjs = null;
	Map<ABObject, List<Pair>> prefs;
	Map<ABObject, List<Pair>> iniPrefs;
	List<ABObject> unmatchedIniObjs;
	List<ABObject> unmatchedNewObjs;
	Map<ABObject, ABObject> matchedObjs;
	List<ABObject> newComingObjs;
	long maximum_distance;
	int timegap;
	
	public TrackerTemplate(int timegap)
	{
		this.timegap = timegap;
		maximum_distance = (timegap/3 + 1) * (timegap/3 + 1);
	}
	public void setTimeGap(int timegap)
	{
		this.timegap = timegap;
		maximum_distance = (timegap/3 + 1) * (timegap/3 + 1);
	}
	private boolean startTracking = false;
	protected void log(String message) {
		System.out.println(message);
	}

	/**
	 * @param: objects in the next frame create preferences based on the mass
	 *         center shift
	 * */
	public abstract void createPrefs(List<ABObject> objs);

	/**
	 * @param targetObj
	 *            : the object 
	 * @param lastObj
	 *            : the last matched object of targetObj
	 * @param rivalObj
	 *            : a new coming object that maybe more suitable for targetObj
	 * @return true: if targetObj prefers lastMatchedObj over rivalObject.
	 *       
	 * */
	public abstract boolean prefers(ABObject targetObj, ABObject lastObj, ABObject rivalObj, Map<ABObject, List<Pair>> prefs);

	@Override
	public boolean isTrackingStart() {
		return startTracking;
	}

	@Override
	public void startTracking(List<ABObject> initialObjs) {
		startTracking = true;
		//reset 
		this.iniObjs = initialObjs;
		//System.out.println("@@@@@@@@@");
		GlobalObjectsToolkit.registerIniObjs(initialObjs);
		lastInitialObjs = null;
		newComingObjs = null;
	}
	
	protected void swap(Map<ABObject, ABObject> iniToNewMatch, Map<ABObject, ABObject> NewToIniMatch, ABObject o1, ABObject o2, ABObject newO1, ABObject newO2)
	{
		iniToNewMatch.remove(o1);
		iniToNewMatch.remove(o2);
		
		NewToIniMatch.remove(newO1);
		NewToIniMatch.remove(newO2);
		
		iniToNewMatch.put(o1, newO2);
		iniToNewMatch.put(o2, newO1);
		
		NewToIniMatch.put(newO1, o2);
		NewToIniMatch.put(newO2, o1);
	}

	protected float calMassShift(ABObject o1, ABObject o2) {
	
		Point center1 = o1.getCenter();
		Point center2 = o2.getCenter();
	
		double diff = (center1.getX() - center2.getX())
				* (center1.getX() - center2.getX())
				+ (center1.getY() - center2.getY())
				* (center1.getY() - center2.getY());
		/*if(o2.id == 14 && o1.id == 18)
			System.out.println(center1.getX() + "    " + center1.getY() + "  " + 
								center2.getX() + "  " + center2.getY() + "  " + diff + "  " + (float)diff);*/
		return (float) diff;
	
	}
	/**
	 * Intended for tolerating vision error in detecting wood
	 * Convert all wood circles to 1x1 wood blocks since vision is likely to treat 1x1 wood as circle
	 * */
	protected void preprocessObjs(List<ABObject> objs)
	{
		List<ABObject> removal = new LinkedList<ABObject>();
		List<ABObject> addedBlocks = new LinkedList<ABObject>();
		for (ABObject obj : objs)
		{
			if (obj instanceof Circle && obj.type == ABType.Wood)
			{
			
				ABObject newBlock = new Rect(
						obj.getCenterX(), obj.getCenterY(), obj.getBounds().width, 
						obj.getBounds().height, 0, -1, obj.area);
				log(" circle to rec conversion: Circle " + obj);
				log(" circle to rec conversion: Rect " + newBlock);
				 
				newBlock.id = obj.id;
				 newBlock.type = ABType.Wood;
				 addedBlocks.add(newBlock);
				 removal.add(obj);
			}
		}
		objs.removeAll(removal);
		objs.addAll(addedBlocks);
		/*for (ABObject obj : objs)
			log(obj.toString());*/
	}
	@Override
	public void setInitialObjects(List<ABObject> objs) {
	
		iniObjs = objs;
	
	}

	protected Map<ABObject, ABObject> matchObjs(List<ABObject> moreObjs, List<ABObject> lessObjs, Map<ABObject, List<Pair>> morePrefs, Map<ABObject, List<Pair>> lessPrefs) {
	
		HashMap<ABObject, ABObject> current = new HashMap<ABObject, ABObject>();
		LinkedList<ABObject> freeObjs = new LinkedList<ABObject>();
		freeObjs.addAll(lessObjs);
	
		HashMap<ABObject, Integer> next = new HashMap<ABObject, Integer>();
	
		for (ABObject obj : moreObjs)
			current.put(obj, null);
	
		// System.out.println(" freeObjs size: " + freeObjs.size());
		for (ABObject obj : freeObjs) {
			next.put(obj, 0);
		}
		// System.out.println(" key size: " + next.keySet().size());
		// while there are no free objects or all the original objects have been
		// assigned.
		unmatchedIniObjs = new LinkedList<ABObject>();
	
		while (!freeObjs.isEmpty()) {
	
			ABObject freeObj = freeObjs.remove();
			int index = next.get(freeObj);
			/*
			 * System.out.println(obj + " " + index); for (ABObject t :
			 * next.keySet()) System.out.println(next.get(t));
			 */
			List<Pair> pairs = lessPrefs.get(freeObj);
			if (pairs == null || index == pairs.size())
				unmatchedIniObjs.add(freeObj);
			else 
			{
					Pair pair = pairs.get(index);
					ABObject moreObj = pair.obj;
					next.put(freeObj, ++index);
					if(pair.sameShape && pair.diff < 1000)
					{
						if (current.get(moreObj) == null)
							current.put(moreObj, freeObj);
						else 
						{
							ABObject rival = current.get(moreObj);
							if (prefers(moreObj, freeObj, rival, morePrefs)) 
							{
								current.put(moreObj, freeObj);
								freeObjs.add(rival);
							}
							else
								freeObjs.add(freeObj);
						}
					}
					else
						freeObjs.add(freeObj);
				}
		}
	
		return current;
	}

	@Override
	public boolean matchObjs(List<ABObject> objs) {
		/*
		 * if(initialObjs != null) System.out.println(initialObjs.size());
		 */
		// System.out.println(objs.size());
		// Do match, assuming initialObjs.size() > objs.size(): no objects will
		// be created
		matchedObjs = new HashMap<ABObject, ABObject>();
		if (iniObjs != null /*&& initialObjs.size() >= objs.size()*/) 
		{
	
			lastInitialObjs = iniObjs;
	
			boolean lessIni = (objs.size() > iniObjs.size()); // If the num
																	// of3.d
																	// initial
																	// objects >
																	// next
																	// frame obj
			// log(" " + initialObjs.size() + "  " + objs.size());
			createPrefs(objs);
			//printPrefs(prefs);
			Map<ABObject, ABObject> match;
			unmatchedNewObjs = new LinkedList<ABObject>();
			if (!lessIni) {
				match = matchObjs(iniObjs, objs, iniPrefs, prefs);
	
				// Assign Id
				for (ABObject iniObj : match.keySet()) {
					ABObject obj = match.get(iniObj);
					if (obj != null)
					{
						obj.id = iniObj.id;
						matchedObjs.put(obj, iniObj);
					}
					else
						unmatchedNewObjs.add(iniObj);
				}
	
				// log(" debris recognition WAS performed: more objects in the initial");
				debrisRecognition(unmatchedIniObjs, unmatchedNewObjs);
			} else {
				log(" more objs in next frame");
				/*
				 * Map<ABObject, List<Pair>> temp; temp = iniPrefs; iniPrefs =
				 * prefs; prefs = temp;
				 */
				match = matchObjs(objs, iniObjs, prefs, iniPrefs);
				// Assign Id
				for (ABObject obj : match.keySet()) {
					ABObject iniObj = match.get(obj);
					if (iniObj != null)
					{	
						obj.id = iniObj.id; 
						matchedObjs.put(obj, iniObj);
					}
					else
						unmatchedNewObjs.add(obj);
				}
				// Process unassigned objs
				// log("debris recognition WAS performed");
				debrisRecognition(unmatchedNewObjs, unmatchedIniObjs);
	
			}
	
			this.setInitialObjects(objs);
			//printPrefs(prefs);
			return true;
		}
		return false;
	}

	public TrackerTemplate() {
		super();
	}

	public abstract void debrisRecognition(List<ABObject> newObjs, List<ABObject> initialObjs);

	@Override
	public boolean isMatch(Shape a, Shape b) {
	
		return false;
	}

	@Override
	public List<ABObject> getMatchedObjects() {
	
		return newComingObjs;
	}

	@Override
	public List<ABObject> getInitialObjects() {
		// TODO Auto-generated method stub
		return lastInitialObjs;
	}

	public void printPrefs(Map<ABObject, List<Pair>> prefs) {
		
		log("  ====================  Print Prefs =========================\n  ");
		for (ABObject ao : prefs.keySet()) {
			System.out.println(ao);
			List<Pair> pairs = prefs.get(ao);
			for (Pair pair : pairs) {
				log(pair.toString());
			}
			log("----------------------");
		}
		log("============ Prefs End ============\n");
	
	}
	class PairComparator implements Comparator<Pair> {
		@Override
		public int compare(Pair o1, Pair o2) {
			return ((Float) o1.diff).compareTo((Float) o2.diff);
		}
	}
	/**
	 *@param newObj, iniObj: new object and the matched initial object
	 *@param isDebris: whether the newObj is a piece of iniObj
	 *
	 * Link newObj to iniObj by setting the id equal to iniObj's id, and set the original shape 
	 * of newObj if newObj is debris (otherwise the original shape of newObj is newObj itself)
	 * 
	 * */
	protected void link(ABObject newObj, ABObject iniObj, boolean isDebris)
	{
		newObj.id = iniObj.id;
		if(isDebris && (! (iniObj.getOriginalShape().isSameShape(newObj) || 
		  (newObj.rectType == RectType.rec8x1/* && iniObj.getOriginalShape().rectType == RectType.rec8x1*/))
				))
		{	
			newObj.setOriginalShape(GlobalObjectsToolkit.getIniObjById(iniObj.id));
			newObj.isDebris = true;
		}
		else 
			/*if(isDebris && (iniObj instanceof DebrisGroup))
			{
				newObj.isDebris = true;
			}
			else*/
				newObj.isDebris = false;
	
	} 
	protected void printMatch(Map<ABObject, ABObject> newToIniMatch, boolean newToIni)
	{
		String str1 = "";
		String str2 = "";
		if ( newToIni)
		{	
			str1 = "newObj: ";
			str2 = "iniObj: ";
		}
		else
		{
			str1 = "iniObj: ";
			str2 = "newObj: ";
		}
			
		log(" ===========  Print Match ============= ");
		for (ABObject newObj : newToIniMatch.keySet())
		{
			System.out.println(str1 + newObj);
			System.out.println(str2 + newToIniMatch.get(newObj));
			System.out.println("==========");
		}
	}
	protected void printMovement(Map<ABObject, Movement> movements)
	{
		log("\n Print Initial Objects Movements");
		for (ABObject obj : movements.keySet())
		{
			log(movements.get(obj) + "");
		}
	}
	protected void printMatch(List<ABObject> interestObj, Map<ABObject, ABObject> newToIniMatch, boolean newToIni)
	{
		String str1 = "";
		String str2 = "";
		if ( newToIni)
		{	
			str1 = "newObj: ";
			str2 = "iniObj: ";
		}
		else
		{
			str1 = "iniObj: ";
			str2 = "newObj: ";
		}
			
		log(" ===========  Print Match ============= ");
		for (ABObject newObj : interestObj)
		{
			System.out.println(str1 + newObj);
			System.out.println(str2 + newToIniMatch.get(newObj));
			System.out.println("==========");
		}
	}
	
	class Pair {
		public ABObject obj;
		public float diff;
		public boolean sameShape;
	
		public Pair(ABObject obj, float diff, boolean sameShape) {
			super();
			this.obj = obj;
			this.diff = diff;
			this.sameShape = sameShape;
		}

		@Override
		public String toString() {
			return " comparedObj: " + obj + "  diff: " + diff + " isSameShape "
					+ sameShape;
		}

	}

}