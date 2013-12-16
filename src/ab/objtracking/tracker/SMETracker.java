package ab.objtracking.tracker;

import java.awt.Polygon;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ab.objtracking.MagicParams;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.real.shape.Rect;


/**
 * Create prefs by taking object categories into consideration
 * Detects explosion/debris
 * */
public class SMETracker extends TrackerTemplate {

	public List<ABObject> currentOccludedObjs; //intended for realtime tracking. stored the potential occluded objs, and add them to the next intial objs
	public List<ABObject> occludedObjsBuffer = new LinkedList<ABObject>();
	public SMETracker(int timegap) {
		super(timegap);
	}

	@Override
	public void createPrefs(List<ABObject> objs) 
	{
		prefs = new HashMap<ABObject, List<Pair>>();
		iniPrefs = new HashMap<ABObject, List<Pair>>();
	
		for (ABObject obj : objs) 
		{	
			List<Pair> diffs = new LinkedList<Pair>();
			ABType objType = obj.type;
			for (ABObject iniObj : iniObjs) {
				if(objType == iniObj.type)
				{
					boolean sameShape = iniObj.isSameShape(obj);
					diffs.add(new Pair(iniObj, calMassShift(obj, iniObj), sameShape));
					if (!iniPrefs.containsKey(iniObj)) {
						List<Pair> iniDiffs = new LinkedList<Pair>();
						iniDiffs.add(new Pair(obj, calMassShift(obj, iniObj), sameShape));
						iniPrefs.put(iniObj, iniDiffs);
					} else {
						iniPrefs.get(iniObj).add(
								new Pair(obj, calMassShift(obj, iniObj), sameShape));
					}
			}
	
			}
			Collections.sort(diffs, new PairComparator());
			prefs.put(obj, diffs);
		}
		for (ABObject iniObj : iniPrefs.keySet()) {
			Collections.sort(iniPrefs.get(iniObj), new PairComparator());
		}
		newComingObjs = objs;
		//printPrefs(iniPrefs);
		//printPrefs(prefs);
	}

	@Override
	public void debrisRecognition(List<ABObject> newObjs, List<ABObject> initialObjs) {
		
		List<ABObject> debrisList = new LinkedList<ABObject>();
		currentOccludedObjs.addAll(initialObjs);
		for (ABObject newObj : newObjs) 
		{
			
			List<Pair> pairs = prefs.get(newObj);
			Pair pair = null;
			int pointer = 0;
			while (!pairs.isEmpty()&& pointer < pairs.size() && newObj.type != ABType.Pig)
			{	
				pair = pairs.get(pointer);
				//assuming circles in the initial frame will never turn to rect //TODO robust damage components detection
				if(initialObjs.contains(pair.obj))
				{	
					/*for (ABObject _obj : initialObjs)
						System.out.println(_obj + "  " + _obj.hashCode());
					System.out.println(pair.obj + "  " + pair.obj.hashCode() + "  " + initialObjs.contains(pair.obj));*/
					break;
				}
				else
					pointer++;
			}
			newObj.id = ABObject.unassigned;
			// log(" unmatched new object: " + newObj + "  " + (newObj.type != ABType.Pig) + " " + pair);
				
			if (pair != null)
			{
				//System.out.println(" pair check");
				for (ABObject initialObj : initialObjs) {
					//System.out.println(initialObj);
					//if(pair.obj.id == 2)
					//	System.out.println(pair.obj + "   " + initialObj + "   " + pair.obj.equals(initialObj));
					// pair.diff's threshold can be estimated by frame frequency
					if (pair.obj.equals(initialObj) && pair.diff < MagicParams.DiffTolerance) {
						// System.out.println(pair.obj + "  " +
						// pair.obj.hashCode() + "   " + initialObj + "  " +
						// initialObj.hashCode() + "   " +
						// pair.obj.equals(initialObj));
						newObj.id = initialObj.id;
						matchedObjs.put(newObj, initialObj);
						debrisList.add(newObj);
						currentOccludedObjs.remove(initialObj);
						break;
						// log(" matched initial object: " + initialObjs);
					}
	
				}
			}
		}
		// Damage Recognition, call back schema: if an object has been detected as damaged, and only one part of the object has been found, the algo will go back to check for 
		//the other part, even though that part has been matched
		for (ABObject debris: debrisList)
		{
			ABObject initialObj = matchedObjs.get(debris);
			if( initialObj instanceof Rect )//&& debris instanceof Rect)
			{
				Rect _initialObj = (Rect)initialObj;
				//Rect _debris = (Rect)debris;
				for (ABObject newObj : newObjs)
				{
					if(/*unmatchedDebris.id == ABObject.unassigned &&*/ newObj.type != ABType.Pig)
					{
						//System.out.println(" debris " + debris);
						//System.out.println(" unmatched " + unmatchedDebris);
						Rect dummy = debris.extend(_initialObj.rectType);
						//System.out.println(" initial " + _initialObj + " newobj " + newObj + " dummy" + dummy);
						//System.out.println(" dummy " + dummy);
						Polygon p = dummy.p;
						
						if(p.contains(newObj.getCenter()))// && newObj instanceof Rect)//damage detection only supports rect currently
						{
							newObj.id = debris.id;
							currentOccludedObjs.remove(initialObj);
							matchedObjs.put(newObj, initialObj);
						}
					}
				}
			}
		}
		
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
		currentOccludedObjs = new LinkedList<ABObject>();
		
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
			for (ABObject occludedObj : currentOccludedObjs)
				System.out.println(occludedObj);
			
			objs.addAll(currentOccludedObjs);
			objs.removeAll(occludedObjsBuffer); // remove all the remembered occluded objects from the previous frame. We only buffer one frame.
			occludedObjsBuffer.addAll(currentOccludedObjs);
			
			this.setInitialObjects(objs);
			//printPrefs(prefs);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean prefers(ABObject targetObj, ABObject lastObj, ABObject rivalObj, Map<ABObject, List<Pair>> prefs){
	 	
		for (Pair pair : prefs.get(targetObj)) {
			if (pair.obj.equals(lastObj))
				return true;
			if (pair.obj.equals(rivalObj))
				return false;
		}
		System.out.println("Error in prefs ");
		return false;
	}
	
	public static void main(String args[])
	{
		double x = 644;
		double y = 346.5;
		double _x = 636.5;
		double _y = 340;
		float r = (float)(((x - _x)*(x - _x) + (y - _y) * (y - _y)));
		System.out.println(r);
	}

	@Override
	public Map<ABObject, ABObject> getLastMatch() {
		
		return matchedObjs;
	}
}
