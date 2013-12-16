package ab.objtracking.tracker;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.DirectedGraph;

import ab.objtracking.MagicParams;
import ab.objtracking.dynamic.Movement;
import ab.objtracking.dynamic.MovementPredictor;
import ab.objtracking.isomorphism.IsomorphismTest;
import ab.objtracking.representation.ConstraintEdge;
import ab.objtracking.representation.util.DebrisToolkit;
import ab.objtracking.representation.util.GSRConstructor;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.real.shape.DebrisGroup;
import ab.vision.real.shape.Rect;


/**
 * 
 * Create Prefs by taking object categories into consideration
 * Detects explosion/debris
 * Analyze neighbor movement trend, Neighbor: which hold GR relations
 * 
 * */
public class KnowledgeTracker_2 extends SMETracker {


	public KnowledgeTracker_2(int timegap) {
		super(timegap);
		// TODO Auto-generated constructor stub
	}




	public DirectedGraph<ABObject, ConstraintEdge> initialNetwork, newNetwork;
	protected Map<ABObject, Movement> initialObjsMovement = new HashMap<ABObject, Movement>();
	protected List<DebrisGroup> debrisGroupList;
	protected List<ABObject> debrisList;
	@Override
	public void createPrefs(List<ABObject> objs) 
	{
		initialNetwork = GSRConstructor.constructGRNetwork(iniObjs);
		newNetwork = GSRConstructor.constructGRNetwork(objs);
		debrisList = new LinkedList<ABObject>();
		//If no previous movement detected
		if(initialObjsMovement.isEmpty()){

			initialObjsMovement = MovementPredictor.predict(initialNetwork);
		}  
		//Create dummy debris
	    debrisGroupList = DebrisToolkit.getAllDummyRectangles(newNetwork);
		for (DebrisGroup debris : debrisGroupList)
		{
			System.out.println(String.format(" Debris:%s member1:%s member2:%s ", debris, debris.member1, debris.member2));
		}
		objs.addAll(debrisGroupList);		


		//initialObjsMovement.putAll(occludedObjsMovement);
		/*log(" Print New Coming Network");
		GSRConstructor.printNetwork(newNetwork);*/
		

		log("isomorphism Check: ");
		System.out.println(IsomorphismTest.isIsomorphic(newNetwork, initialNetwork));
		
		prefs = new HashMap<ABObject, List<Pair>>();
		iniPrefs = new HashMap<ABObject, List<Pair>>();

		for (ABObject obj : objs) 
		{	
			List<Pair> diffs = new LinkedList<Pair>();
			ABType objType = obj.type;
			for (ABObject iniObj : iniObjs) 
			{   

				if(objType == iniObj.type)
				{
					Movement movement = initialObjsMovement.get(iniObj);
					if( movement != null)
					{
						//Evaluate movement by taking spatial change into consideration
						movement = MovementPredictor.adjustMovementOnGR(movement, initialNetwork);
						
						/*if(iniObj.id == 6)
							System.out.println("\n movement " + movement + "\n" + obj + "  xshift " + (int)(obj.getCenterX() - iniObj.getCenterX()) + " yshift " + (int)(obj.getCenterY() - iniObj.getCenterY()) + 
						
									movement.isValidMovement((int)(obj.getCenterX() - iniObj.getCenterX()), (int)(obj.getCenterY() - iniObj.getCenterY()), false));*/
					
					}
					if(movement == null || movement.isValidMovement((int)(obj.getCenterX() - iniObj.getCenterX()), (int)(obj.getCenterY() - iniObj.getCenterY()), false) )
					{
						float squareShift = calMassShift(obj, iniObj);
						boolean sameShape = iniObj.isSameShape(obj);
						diffs.add(new Pair(iniObj, squareShift, sameShape));
						if (!iniPrefs.containsKey(iniObj)) 
						{
							List<Pair> iniDiffs = new LinkedList<Pair>();
							iniDiffs.add(new Pair(obj, squareShift, sameShape));
							iniPrefs.put(iniObj, iniDiffs);
						}
						else
						{
							iniPrefs.get(iniObj).add(
									new Pair(obj, squareShift, sameShape));
						}	
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
		initialObjsMovement.clear();
		printPrefs(iniPrefs);
		//printPrefs(prefs);
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
				if(pair.sameShape && pair.diff < MagicParams.DiffTolerance)
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
	//check whether the initialObj has been matched with a non-debris object
	public void debrisRecognition(List<ABObject> newObjs, List<ABObject> initialObjs) {

	   
		currentOccludedObjs.addAll(initialObjs);
		for (ABObject newObj : newObjs) 
		{
			
			List<Pair> pairs = prefs.get(newObj);
			Pair pair = null;
			int pointer = 0;
			while (pairs != null && !pairs.isEmpty()&& pointer < pairs.size() && newObj.type != ABType.Pig)
			{	
				pair = pairs.get(pointer);
				//assuming circles in the initial frame will never turn to rect //TODO robust damage components detection
				if(initialObjs.contains(pair.obj))
				{	
					break;
				}
				else
					pointer++;
			}
			newObj.id = ABObject.unassigned;
			// log(" unmatched new object: " + newObj + "  " + (newObj.type != ABType.Pig) + " " + pair);

			if (pair != null)
			{
				//TODO non necessary loop
				for (ABObject initialObj : initialObjs) {
					//if(pair.obj.id == 2)
					//	System.out.println(pair.obj + "   " + initialObj + "   " + pair.obj.equals(initialObj));
					// pair.diff's threshold can be estimated by frame frequency
					if (pair.obj.equals(initialObj) && pair.diff < MagicParams.DiffTolerance) {
					
								
						link(newObj, initialObj, true);
						matchedObjs.put(newObj, initialObj);
						debrisList.add(newObj);
						currentOccludedObjs.remove(initialObj);
						/*if(initialObj.id == 9)
							System.out.println("@@@" + initialObj + "  " + currentOccludedObjs.contains(initialObj));*/
						break;
						// log(" matched initial object: " + initialObjs);
					}

				}
			}
		}
		/* for (ABObject object : currentOccludedObjs)
	     {
	    	 log("##" + object.toString());
	     }*/
		//newObjs.removeAll(debrisList);
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
					if(debris!= newObj && newObj.type != ABType.Pig)
					{
						
						if (_initialObj.isDebris)
						{
							for(DebrisGroup group : debrisGroupList)
							{
								if (group.member1 == debris || group.member2 == debris)
								{	
									_initialObj = group;
									break;
								}
							}
							
							
						}
						/*if(debris.id == 3)
						{
							System.out.println(" debris " + debris);
							System.out.println(" initial " + _initialObj + " newobj " + newObj);
							System.out.println(DebrisToolKit.isSameDebris(debris, _initialObj, newObj));
						}*/
						if(DebrisToolkit.isSameDebris(debris, _initialObj, newObj))
						{
							ABObject newObjLastMatch = matchedObjs.get(newObj);
							if(newObjLastMatch != null && newObj.id != debris.id && !currentOccludedObjs.contains(newObjLastMatch))
							{
								
								//TODO optimize the following search;
								boolean anotherMatch = false;
								for (ABObject matched : matchedObjs.keySet())
								{
									ABObject _lastmatch = matchedObjs.get(matched);
									if(_lastmatch == newObjLastMatch && matched != newObj)
									{
										anotherMatch = true;
									}
											
								}
								if(!anotherMatch)
									currentOccludedObjs.add(newObjLastMatch);
							}
							link(newObj, debris, true);
							currentOccludedObjs.remove(initialObj);
							matchedObjs.put(newObj, initialObj);
						}
						//	}
						//}
						//}
					}
				}

			}
		}
	    /* for (ABObject object : currentOccludedObjs)
	     {
	    	 log("@@" + object.toString());
	     }*/
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

			boolean lessIni = (objs.size() > iniObjs.size()); // If the num of initial objects > the next
			
			// log(" " + initialObjs.size() + "  " + objs.size());
			createPrefs(objs);
			//printPrefs(prefs);
			Map<ABObject, ABObject> match;
			unmatchedNewObjs = new LinkedList<ABObject>();
			List<ABObject> membersOfMatchedDebrisGroup = new LinkedList<ABObject>();
			if (!lessIni) 
			{
				match = matchObjs(iniObjs, objs, iniPrefs, prefs);

				// Assign Id
				for (ABObject iniObj : match.keySet()) {
					
					ABObject obj = match.get(iniObj);
					if (obj != null)
					{
					    link(obj, iniObj, iniObj.isDebris);
						matchedObjs.put(obj, iniObj);
						if(obj.isDebris)
							debrisList.add(obj);
						if(obj instanceof DebrisGroup)
						{
							DebrisGroup debris = (DebrisGroup)obj;
							ABObject member1 = debris.member1;
							ABObject member2 = debris.member2;
							//assign id after debris recognition, otherwise unmatchedLessObjs cannot remove 
							unmatchedIniObjs.remove(member1);
							unmatchedIniObjs.remove(member2);
							matchedObjs.remove(member1);
							matchedObjs.remove(member2);
							link(member1, obj, true);
							link(member2, obj, true);
							matchedObjs.put(member1, iniObj);
							matchedObjs.put(member2, iniObj);

						}
						if(iniObj instanceof DebrisGroup)
						{
							DebrisGroup debris = (DebrisGroup)iniObj;
							membersOfMatchedDebrisGroup.add(debris.member1);
							membersOfMatchedDebrisGroup.add(debris.member2);
						}
					}
					else	
							unmatchedNewObjs.add(iniObj);
						
				}
				unmatchedNewObjs.removeAll(membersOfMatchedDebrisGroup);
				// log(" debris recognition WAS performed: more objects in the initial");
				debrisRecognition(unmatchedIniObjs, unmatchedNewObjs);
			} else {
				log(" Next frame has more objs");
				/*
				 * Map<ABObject, List<Pair>> temp; temp = iniPrefs; iniPrefs =
				 * prefs; prefs = temp;
				 */
				match = matchObjs(objs, iniObjs, prefs, iniPrefs);
				// Assign Id
				for (ABObject obj : match.keySet()) {

					if(matchedObjs.containsKey(obj))
						continue;
					else
					{

						ABObject iniObj = match.get(obj);

						if (iniObj != null)
						{	
							link(obj, iniObj, iniObj.isDebris);
							matchedObjs.put(obj, iniObj);
							if(obj.isDebris)
								debrisList.add(obj);
							if(obj instanceof DebrisGroup)
							{
								DebrisGroup debris = (DebrisGroup)obj;
								ABObject member1 = debris.member1;
								ABObject member2 = debris.member2;

								unmatchedNewObjs.remove(member1);
								unmatchedNewObjs.remove(member2);
								/*   if (obj.id == 7)
						    {
						    	System.out.println(String.format("member1: %s %s member2: %s %s ", 
						    			member1, objs.contains(member1), member2, objs.contains(member2)));
						    	for (ABObject _obj : unmatchedMoreObjs)
						    	{
						    		System.out.println(_obj);
						    	}
						    	System.out.println("======================");
						    }*/
								matchedObjs.remove(member1);
								matchedObjs.remove(member2);
								link(member1, obj, true);
								link(member2, obj, true);
								matchedObjs.put(member1, iniObj);
								matchedObjs.put(member2, iniObj);

							}
						}
						else
							unmatchedNewObjs.add(obj);
					}
				}
				// Process unassigned objs
				/*//log("debris recognition was performed");
				for (abobject obj : unmatchedmoreobjs)
				{
					log(obj.tostring());
				}*/
				debrisRecognition(unmatchedNewObjs, unmatchedIniObjs);

			}

			/*log(" Movement Consistency Check ");
			List<Movement> movementConflicts = new LinkedList<Movement>();
			for (ABObject source: initialNetwork.vertexSet())
			{
					validateMovement(source, initialNetwork, movementConflicts);
			}
			for (Movement movement: movementConflicts)
			{

			}*/


			log("Print Occluded Objects");
			for (ABObject occludedObj : currentOccludedObjs)
				System.out.println(occludedObj);

			objs.addAll(currentOccludedObjs);
			objs.removeAll(occludedObjsBuffer); // remove all the remembered occluded objects from the previous frame. We only buffer one frame.
			occludedObjsBuffer.addAll(currentOccludedObjs);
			//Those who has the same id and has rectangular shape 

			//Set Initial Objs Movements
			initialObjsMovement.clear();
			for (ABObject obj : matchedObjs.keySet())
			{
				ABObject initial = matchedObjs.get(obj);
				if(initial != null){
					Movement movement = new Movement(obj);
					movement.generateInertia(initial);
					initialObjsMovement.put(obj, movement);
					/*if(obj.id == 7)
						System.out.println(" Generate Initial " + " obj " + obj + " initial " + initial + " " +movement);*/
				}
			}


			this.setInitialObjects(objs);
			
			//printPrefs(prefs);
			/*log(" Print all Objects (next frame) after matching");
			for (ABObject obj : objs)
			{
				log(obj.toString());
			}*/
			return true;
		}
		return false;
	}




	public static void main(String args[])
	{
		double x = 644;
		double y = 346.5;
		double _x = 636.5;
		double _y = 340;
		float r = (float)(((x - _x)*(x - _x) + (y - _y) * (y - _y)));
		assert(_y < 0);
		System.out.println(r);
	}
}
