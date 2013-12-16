package ab.objtracking.tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;

import ab.objtracking.MagicParams;
import ab.objtracking.Tracker;
import ab.objtracking.TrackingFrameComparison;
import ab.objtracking.dynamic.Movement;
import ab.objtracking.dynamic.MovementPredictor;
import ab.objtracking.representation.ConstraintEdge;
import ab.objtracking.representation.Relation;
import ab.objtracking.representation.util.DebrisToolkit;
import ab.objtracking.representation.util.GSRConstructor;
import ab.objtracking.representation.util.GlobalObjectsToolkit;
import ab.objtracking.representation.util.ShapeToolkit;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.real.shape.DebrisGroup;
import ab.vision.real.shape.Poly;
import ab.vision.real.shape.Rect;


/**
 * 
 * Create Prefs by taking object categories into consideration
 * Detects explosion/debris
 * Analyze neighbor movement trend, Neighbor: which hold GR relations
 * 
 * 
 * Solve Dynamic Problems
 * Add Debris Collector
 * Add speed consideration: 3 ms per pixel
 * Integrate with Occluded Toolkit
 * Refine Debris Management
 * Preprocess Objs
 * */
public class KnowledgeTrackerBaseLine_8 extends SMETracker {


	public DirectedGraph<ABObject, ConstraintEdge> iniGRNetwork, newGRNetwork, iniFullNetwork, newFullNetwork;
	protected Map<ABObject, Movement> iniObjsMovement = new HashMap<ABObject, Movement>();
	protected List<DebrisGroup> debrisGroupList = new LinkedList<DebrisGroup>();
	protected List<ABObject> debrisList;
	protected boolean lessIniObjs = false;
	protected List<Set<ABObject>> iniKGroups, newKGroups;
	public KnowledgeTrackerBaseLine_8(int timegap) {
		super(timegap);
		maximum_distance = (timegap/3 + 1) * (timegap/3 + 1);
	}
	
	
	protected void reassociatePieces(List<ABObject> newObjs, Map<ABObject, ABObject> newToIniMatch)
	{
		List<ABObject> pieces = new LinkedList<ABObject>();
		List<Integer> debrisGroupId = new LinkedList<Integer>();
		for(ABObject newObj : newObjs)
		{
			if(newObj instanceof DebrisGroup)
			{
				debrisGroupId.add(newObj.id);
			}	
		}
			
		Map<Integer, List<ABObject>> disassociatedObjs = new HashMap<Integer, List<ABObject>>();
		
		for (ABObject newObj : newObjs)
		{
			if(newObj instanceof DebrisGroup)
				continue;
			
			if(debrisGroupId.contains(newObj.id))
			{	
				pieces.add(newObj);
				continue;
			}
			
			if (newObj.isDebris)
			{
				if(disassociatedObjs.containsKey(newObj.id))
				{
					disassociatedObjs.get(newObj.id).add(newObj);
				}
				else
				{
					disassociatedObjs.put(newObj.id, new LinkedList<ABObject>());
					disassociatedObjs.get(newObj.id).add(newObj);
				}
				
			}
		}
		
		//Reconstruct based on disassociatedObjs
		for (Integer key : disassociatedObjs.keySet())
		{
			List<ABObject> group = disassociatedObjs.get(key);
			Rect rect = DebrisToolkit.debrisReconstruct(group);
			if(rect != null)
			{
			  if(!newObjs.contains(rect))
				  newObjs.add(rect);
			  /*	log("^^^ " + group.get(0) + "  " + newToIniMatch.get(group.get(0))
			  			);
			  	log("^^^"  + newToIniMatch.get(group.get(0)).id );
			  	log("^^^^" + rect.id);
			  	*/
			  	//printMatch(newToIniMatch, true);
			  	
				link(rect, newToIniMatch.get(group.get(0)), false);
				rect.type = group.get(0).type;
				matchedObjs.put(rect, newToIniMatch.get(group.get(0)));
				pieces.addAll(group);
			}
		
		/*	{
				//If A, and B are two potential Debris of C, and A has the same shape with C, then B cannot be the debris
				boolean sign = false;
				List<ABObject> removal = new LinkedList<ABObject>();
				for (ABObject ab : group)
				{
					if(!ab.isDebris)
						sign = true;
					else
						removal.add(ab);
				}
				if (sign)
					pieces.addAll(removal);
				
			}*/
			
		}
		newObjs.removeAll(pieces);
	}

	@Override
	public void createPrefs(List<ABObject> newObjs) 
	{
		
		List<DirectedGraph<ABObject, ConstraintEdge>> graphs = GSRConstructor.contructNetworks(iniObjs);
		iniGRNetwork = graphs.get(1);
		iniFullNetwork = graphs.get(0);
		iniKGroups = GSRConstructor.getAllKinematicsGroups(iniGRNetwork);
		
		//=================  Remove Polygons  ===============================
		List<ABObject> polygons = new LinkedList<ABObject>();
		for (ABObject obj : newObjs)
		{
			if(obj instanceof Poly)
			{
				obj.id = ABObject.unassigned;
				if(obj.type != ABType.Hill)
					polygons.add(obj);
			}
		}
		newObjs.removeAll(polygons);
		//=================  Remove Polygon End  ==========================
		
		graphs = GSRConstructor.contructNetworks(newObjs);
		
		newGRNetwork = graphs.get(1);
		newFullNetwork = graphs.get(0);
				
	
		//If no previous movement detected
		if(iniObjsMovement.isEmpty()){

			iniObjsMovement = MovementPredictor.predict(iniGRNetwork);
		}  
	    debrisGroupList = DebrisToolkit.getAllDummyRectangles(newGRNetwork);
	    //Create dummy debris
		/*if (debrisGroupList.isEmpty())
			log("\n No Debris Group Detected ");
		else
			log(" Create Debris on New Objects");
	    for (DebrisGroup debris : debrisGroupList)	
			log(debris.toString());*/
		
		newObjs.addAll(debrisGroupList);
		
		//printMovement(iniObjsMovement);
		
		
		
		prefs = new HashMap<ABObject, List<Pair>>();
		iniPrefs = new HashMap<ABObject, List<Pair>>();

		for (ABObject obj : newObjs) 
		{	
			List<Pair> diffs = new LinkedList<Pair>();
			ABType objType = obj.type;
			for (ABObject iniObj : iniObjs) 
			{   

				if(objType == iniObj.type)
				{
					Movement movement = iniObjsMovement.get(iniObj);
					
					if( movement != null)
					{
						//Evaluate movement by taking spatial change into consideration, evaluating on iniGRFullNetwork
						movement = MovementPredictor.adjustMovementOnGR(movement, iniGRNetwork);
						//System.out.println(movement);
						//System.out.println(movement.isValidMovement((int)(obj.getCenterX() - iniObj.getCenterX()), (int)(obj.getCenterY() - iniObj.getCenterY()), false));
						/*if(iniObj.id == 6)
							System.out.println("\n movement " + movement + "\n" + obj + "  xshift " + (int)(obj.getCenterX() - iniObj.getCenterX()) + " yshift " + (int)(obj.getCenterY() - iniObj.getCenterY()) + 
						
									movement.isValidMovement((int)(obj.getCenterX() - iniObj.getCenterX()), (int)(obj.getCenterY() - iniObj.getCenterY()), false));*/
					}
					
					if(movement == null || movement.isValidMovement(iniObj, obj, false))
					{
						float squareShift = calMassShift(obj, iniObj);
						boolean sameShape = iniObj.isSameShape(obj);
						if(squareShift < maximum_distance)
						{
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

			}
			Collections.sort(diffs, new PairComparator());
			prefs.put(obj, diffs);
		}
		for (ABObject iniObj : iniPrefs.keySet()) {
			Collections.sort(iniPrefs.get(iniObj), new PairComparator());
		}
		newComingObjs = newObjs;
		
		//printPrefs(iniPrefs);
		//printPrefs(prefs);
	}
	/**
	 * Check if multiple objects have been assigned with a same ID
	 * */
	public void uniqueAssignment(List<ABObject> newObjs, Map<ABObject, ABObject> newToIniMatch)
	{
		Map<Integer, Set<ABObject>> groups = new HashMap<Integer, Set<ABObject>>();
		for (ABObject newObj : newObjs)
		{
			if(groups.containsKey(newObj.id))
			{
				groups.get(newObj.id).add(newObj);
			}
			else
			{
				Set<ABObject> set = new HashSet<ABObject>();
				set.add(newObj);
				groups.put(newObj.id, set);
			}
		}
		List<ABObject> nonRelevantPieces = new LinkedList<ABObject>();
		
		for (Integer key : groups.keySet())
		{
			//log("^^^^^" + key + "   " + groups.get(key).size());
			if (groups.get(key).size() > 1 && key != ABObject.unassigned)
			{
				boolean sign = false;
				List<ABObject> nonRelevantBuffer = new LinkedList<ABObject>();
				float distance = Integer.MAX_VALUE;
				for (ABObject obj : groups.get(key))
				{
					//log("@@@ " + obj + "   " + newToIniMatch.get(obj).getOriginalShape());
					
					//TODO if there are two identical objects have been assigned with the same ID, then retain the one with the shortest distance;
					if(obj.isSameShape(newToIniMatch.get(obj).getOriginalShape()))
					{
						sign = true;
						float _distance = calMassShift(obj, newToIniMatch.get(obj));
						if (_distance < distance)
							distance = _distance;
						else
							{
								ABObject matched = GlobalObjectsToolkit.getPossibleOccludedMatch(obj);
								if(matched != null)
								{
									link(obj, matched, true);
									matchedObjs.put(obj, matched);
								}
								else
									nonRelevantBuffer.add(obj);
							}
					}
					else
						{
							//log("^^^ " + obj);
							nonRelevantBuffer.add(obj);
						}
				}
				if(sign)
					nonRelevantPieces.addAll(nonRelevantBuffer);
			}
		}
		
		newObjs.removeAll(nonRelevantPieces);
		/*for (ABObject obj : nonRelevantPieces)
			matchedObjs.remove(obj);*/
		
	}
	protected Map<ABObject, ABObject> matchObjs(List<ABObject> iniObjs, List<ABObject> newObjs)
	{
		
		Map<ABObject, ABObject> newToIniMatch = new HashMap<ABObject, ABObject>();
		Map<ABObject, ABObject> iniToNewMatch = new HashMap<ABObject, ABObject>();
		
		getNextMatchSet(iniObjs, newObjs, newToIniMatch, iniToNewMatch);
		
        //printMatch(iniToNewMatch, false);
		
		evaluateMatch(iniToNewMatch, newToIniMatch, iniKGroups);
		
		return newToIniMatch;
	}
	/**
	 * 
	 * If two identical objects A and B with relation A LEFT B disappeared in image t and reappeared in image t + 3, then
	 * they will be "forgot" by normal occlusion process, a mismatch could be B RIGHT A. 
	 * This method is intended to solve this problem by checking the relations between re-appeared objects. 
	 *   
	 * */
	protected void occlusionMatch(List<ABObject> objs)
	{
	   	
	}
	/**
	 * Convert A - R - B, A - R - C, B - R - C to  A - R - B - R - C. Thus removing the indirect relation between A and C
	 * 
	 * **/
	protected void evaluateMatch(Map<ABObject, ABObject> iniToNewMatch, Map<ABObject, ABObject> NewToIniMatch, List<Set<ABObject>> kinematicGroups)
	{
		//check each group;
		 for (Set<ABObject> kg: kinematicGroups)
		 {
			 List<ABObject> list = new ArrayList<ABObject>();
			 list.addAll(kg);
			 int n = list.size();
			 //Most of the groups are of size 2
			 if( n == 2)
			 {
				 ABObject _o1 = list.get(0);
				 ABObject _newO1 = iniToNewMatch.get(_o1);
				 if(_newO1 != null){
					 ABObject _o2 = list.get(1);
					 ConstraintEdge e;
					 Relation r;
					 e = iniFullNetwork.getEdge(_o1, _o2);
					 if( e == null)
					 {
						 e = iniFullNetwork.getEdge(_o2, _o1);
						 r = Relation.inverse(e.label);
					 }
					 else
						  r = e.label;
					 		
					 ABObject _newO2 = iniToNewMatch.get(_o2);
					 if(_newO2 != null)
					 {
						 Relation _r = (GSRConstructor.computeRectToRectRelation(_newO1, _newO2)).r;
						 //System.out.println(newO1 + "  " + newO2 + "  " + _r + "   " + r);
						 if ( Relation.isOpposite(_o1, _o2, r, _newO1, _newO2, _r) && _o1.type == _o2.type
								 && !ShapeToolkit.isDifferentShape(_newO1, _newO2)) 
						 {
							 //log("@@ " + _o1.id + "  " + _o2.id + "  " + r + "  " + _r);
							 swap( iniToNewMatch, NewToIniMatch, _o1, _o2, _newO1, _newO2 );
					 }
					 }
				 }
			 }
			 else{
				 int newn = 0;
				 while( n > 0)
				 {
					 newn = 0;
					 for (int i = 1; i < n - 1; i++ )
					 {
						 ABObject o1 = list.get(i - 1);
						 ABObject newO1 = iniToNewMatch.get(o1);
						 if(newO1 != null)
						 {
							 ABObject o2 = list.get(i);
							 ConstraintEdge e;
							 Relation r;
							 e = iniFullNetwork.getEdge(o1, o2);
							 if( e == null)
							 {
								 e = iniFullNetwork.getEdge(o2, o1);
								 r = Relation.inverse(e.label);
							 }
							 else
								  r = e.label;
							
							 ABObject newO2 = iniToNewMatch.get(o2);
							 if(newO2 != null)
							 {
								 Relation _r = (GSRConstructor.computeRectToRectRelation(newO1, newO2)).r;
								 //System.out.println(newO1 + "  " + newO2 + "  " + _r + "   " + r);
								 if ( Relation.isOpposite(o1, o2, r, newO1, newO2, _r) && o1.type == o2.type
										 && !ShapeToolkit.isDifferentShape(newO1, newO2)) 
								 {
									
									 swap( iniToNewMatch, NewToIniMatch, o1, o2, newO1, newO2 );
									 newn = i;
								 }
							 }
						 }		 
					 }
					 n = newn;
				 }
			 }
			 
		 }
		
	}

	/**
	 * @param newToIniMatch: newObj -> iniObj
	 * @param iniToNewMatch: iniObj -> newObj
	 * 
	 * */
	protected void getNextMatchSet
	(List<ABObject> iniObjs, List<ABObject> newObjs, Map<ABObject, ABObject> newToIniMatch, Map<ABObject, ABObject> iniToNewMatch)
	{
		HashMap<ABObject, ABObject> current = new HashMap<ABObject, ABObject>();
		
		LinkedList<ABObject> freeObjs = new LinkedList<ABObject>();
		
		freeObjs.addAll(newObjs);
		
		HashMap<ABObject, Integer> next = new HashMap<ABObject, Integer>(); //prefs recorder
		
		//initialize the current map
		for(ABObject obj : iniObjs) current.put(obj, null);
		//initialize prefs recorder
		for(ABObject obj: freeObjs) next.put(obj, 0);
		
		
		while(!freeObjs.isEmpty())
		{
			ABObject freeObj = freeObjs.remove();
			int index = next.get(freeObj);

			List<Pair> pairs =  prefs.get(freeObj);
			if (pairs == null || index == pairs.size())
				unmatchedNewObjs.add(freeObj);
			else 
			{
				Pair pair = pairs.get(index);
				ABObject iniObj = pair.obj;
				next.put(freeObj, ++index);
				if(pair.sameShape && !iniObj.isDebris)
				{
					if (current.get(iniObj) == null)
						current.put(iniObj, freeObj);
					else 
					{
						ABObject rival = current.get(iniObj);
						if (prefers(iniObj, freeObj, rival, iniPrefs)) 
						{
							current.put(iniObj, freeObj);
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
		
		unmatchedIniObjs = new LinkedList<ABObject>();
		
		Map<ABObject,DebrisGroup> iniMatchedToDebris = new HashMap<ABObject, DebrisGroup>();
		for (ABObject obj : iniObjs)
		{
			ABObject newObj = current.get(obj);
			
			if(newObj != null)
			{
				if (newObj instanceof DebrisGroup)
				{
					iniMatchedToDebris.put(obj, (DebrisGroup)newObj);
				}
				else
				{
					iniToNewMatch.put(obj, newObj);
					newToIniMatch.put(newObj, obj);
				}
			} 
			else
				unmatchedIniObjs.add(obj);
		}
		//printNewToIniMatch(newToIniMatch);
		for (ABObject obj : iniMatchedToDebris.keySet())
		{
			 DebrisGroup newDebris = iniMatchedToDebris.get(obj);
			 ABObject member1 = newDebris.member1;
			 ABObject member2 = newDebris.member2;
			 
			 ABObject iniMember1 = newToIniMatch.get(member1);
			 if (iniMember1 != null && !iniMember1.isDebris)
			 {
				 ABObject iniMember2 = newToIniMatch.get(member2);
				 if (iniMember2 != null && !iniMember2.isDebris)
				 {
					 unmatchedIniObjs.add(obj);
					 continue;
				 }
			 }
			 iniToNewMatch.put(obj, newDebris);
			 newToIniMatch.put(newDebris, obj);
			
		}
	
		
	}
	

	
	
	@Override
	public void debrisRecognition(List<ABObject> unmatchedNewObjs, List<ABObject> unmatchedIniObjs) {

	   log(" Debris Recognition ");
		/*for (ABObject iniObj : unmatchedIniObjs)
			log("@@@ini " + iniObj);
	   for(ABObject newObj : unmatchedNewObjs)
	   {
		   log ("@@@new " + newObj);
	   }*/
		
		currentOccludedObjs.addAll(unmatchedIniObjs);
		List<DebrisGroup> groups = new LinkedList<DebrisGroup>();
		for (ABObject newObj : unmatchedNewObjs) 
		{
			
			List<Pair> pairs = prefs.get(newObj);
			Pair pair = null;
			int pointer = 0;
			while (pairs != null && !pairs.isEmpty()&& pointer < pairs.size() && newObj.type != ABType.Pig)
			{	
				pair = pairs.get(pointer);
				//assuming circles in the initial frame will never turn to rect //TODO robust damage components detection
				if(unmatchedIniObjs.contains(pair.obj))
				{	
					break;
				}
				else
					pointer++;
			}
			newObj.id = ABObject.unassigned;
			 //log(" unmatched new object: " + newObj);

			if (pair != null)
			{
				//TODO non necessary loop
				for (ABObject iniObj : unmatchedIniObjs) {
					
					/*log("--------\n" + newObj.toString() + "  \n" + iniObj.toString() + " \n " 
						+ iniObj.getOriginalShape().toString() + "\n"
					+ !ShapeToolkit.cannotBeDebris(newObj, iniObj));*/
					if (pair.obj.equals(iniObj) && pair.diff < MagicParams.DiffTolerance
							&& !ShapeToolkit.cannotBeDebris(newObj, iniObj)
						) {
					
						
						link(newObj, iniObj, true);
						//log("$$$");
						matchedObjs.put(newObj, iniObj);
						currentOccludedObjs.remove(iniObj);
						if(newObj instanceof DebrisGroup)
						{
							groups.add((DebrisGroup) newObj );
						}
						break;
						
					}

				}
			}
		}
		for(ABObject unmatchedNewObj : unmatchedNewObjs)
		{
			
			for (DebrisGroup group : groups )
			{
				if(group.member1 == unmatchedNewObj || group.member2 == unmatchedNewObj)
					{
						link(unmatchedNewObj, matchedObjs.get(group), true);
						matchedObjs.put(unmatchedNewObj, matchedObjs.get(group));
					}
			}
	     }
	}

	
	@Override
	public boolean matchObjs(List<ABObject> newObjs) {
		

		matchedObjs = new HashMap<ABObject, ABObject>();
		
		currentOccludedObjs = new LinkedList<ABObject>();

		
		if (iniObjs != null ) 
		{
			lastInitialObjs = iniObjs;
			//preprocessObjs(newObjs);
			createPrefs(newObjs);
			//printPrefs(prefs);
			Map<ABObject, ABObject> newToIniMatch;
			
			unmatchedNewObjs = new LinkedList<ABObject>();
			
			List<ABObject> membersOfMatchedDebrisGroup = new LinkedList<ABObject>();
			
			newToIniMatch = matchObjs(iniObjs, newObjs);
			
			//Assign Id
			for (ABObject obj: newToIniMatch.keySet())
			{
				if (matchedObjs.containsKey(obj))
					continue;
				else
				{
					ABObject iniObj = newToIniMatch.get(obj);
					if (iniObj != null)
					{
						
						link(obj, iniObj, iniObj.isDebris);
						matchedObjs.put(obj, iniObj);
					
						if(obj instanceof DebrisGroup)
						{
							DebrisGroup debris = (DebrisGroup)obj;
							ABObject member1 = debris.member1;
							ABObject member2 = debris.member2;

							unmatchedNewObjs.remove(member1);
							unmatchedNewObjs.remove(member2);
					
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
				}
			}
			
			//printMatch(matchedObjs, true);
			
			
			//log(" print match before debris recognition");
			//printMatch(matchedObjs, true);
			
			unmatchedIniObjs.removeAll(membersOfMatchedDebrisGroup);
			
			debrisRecognition(unmatchedNewObjs, unmatchedIniObjs);
			
			//printMatch(matchedObjs, true);
			//Remove false debris group. False Debris: new objs debris which is not matched, by its members are matched
			List<ABObject> falseDebris = new LinkedList<ABObject>();
			for (ABObject newObj : unmatchedNewObjs)
			{
				if (newObj instanceof DebrisGroup)
				{
					DebrisGroup debris = (DebrisGroup) newObj;
					if (matchedObjs.get(debris.member1) != null || matchedObjs.get(debris.member2) != null)
						falseDebris.add(debris);
				}
			}
			newObjs.removeAll(falseDebris);
			unmatchedNewObjs.removeAll(falseDebris);
			
			iniObjsMovement.clear();

		/*	log("Print Occluded Objects");
			for (ABObject occludedObj : currentOccludedObjs)
				System.out.println(occludedObj);*/
			
			//GlobalObjectsToolkit.addOccludedObjs(currentOccludedObjs);
			//GlobalObjectsToolkit.updateOccludedObjsAndRels(matchedObjs);
			GlobalObjectsToolkit.updateOccludedObjs(matchedObjs);
			
			//printMatch(matchedObjs, true);
			
			// ========== Match the remaining objs from previous occluded objs ==============
			unmatchedNewObjs.removeAll(matchedObjs.keySet());//recover later
			
		
			
			for (ABObject obj : unmatchedNewObjs)
			{
				//log(" unmatched new obj :" + obj.toString());
				ABObject _obj = GlobalObjectsToolkit.getPossibleOccludedMatch(obj);
				if(_obj != null)
				{
					link(obj, _obj, true);
					matchedObjs.put(obj, _obj);	
					//if(!newObjs.contains(obj))
						//newObjs.add(obj);
					
					if(obj instanceof DebrisGroup)
					{
						DebrisGroup group = (DebrisGroup)obj;
						link(group.member1, _obj, true);
						matchedObjs.put(group.member1, _obj);
						link(group.member2, _obj, true);
						matchedObjs.put(group.member2, _obj);
						
					}
				}
				
			}
			//log(" print match after occlude management");
			//printMatch(matchedObjs, true);
			// ========== End Match =========================================================
			
			
			// only retain those which have been matched;
			newObjs.retainAll(matchedObjs.keySet());
			
			reassociatePieces(newObjs, matchedObjs);
			
			// remove duplicates: If in the Initial scenario, we have Debris_A1, Debirs_A2, Debris_A3, and Debirs_A1 and A2 forms a dummy rectangle A12, in the resulting scenario, 
			//we have Debris_B1, Debris_B2, and Debris_B2 == Debris_A3, however, since Debris_A3 is a debris will not be matched first. So Debris_B2 will be matched to A12
			// and Debris_A3 will be treated as occlude obj and added to the next initial objs, which creates a duplicate.
			
			for (ABObject occ : currentOccludedObjs)
			{
				/*if (newObjs.contains(occ))
				{
					continue;
				}*/
				boolean sign = true;
				for (ABObject newObj : newObjs)
				{
					if(newObj.id == occ.id)
					{ 
						sign = false;
						break;
					}
				}
				if(sign && !newObjs.contains(occ))
					newObjs.add(occ);
			}
			
		
			
			// remove all the matched objs from occludedObjsBuffer (the pre of the pre frame may contain an occluded obj that is matched now)
			occludedObjsBuffer.removeAll(matchedObjs.keySet());	
			
			newObjs.removeAll(occludedObjsBuffer); // remove all the remembered occluded objects from the previous frame. We only buffer one frame.
			
			occludedObjsBuffer.addAll(currentOccludedObjs);
			
			//remove all new objs if they have the debris group being matched with the same id
			reassociatePieces(newObjs, matchedObjs);
			
				/*List<ABObject> removal = new LinkedList<ABObject>();
				for (ABObject obj : newObjs)
				{
					if(obj instanceof DebrisGroup)
					{
						DebrisGroup group = (DebrisGroup)obj;
						removal.add(group.member1);
						removal.add(group.member2);
					}
				}
				newObjs.removeAll(removal);*/
			
			//newObjs.retainAll(matchedObjs.keySet());
			
			//Update before unique assignment
			GlobalObjectsToolkit.updateOccludedObjs(matchedObjs);
			//Ensure unique assignments
			uniqueAssignment(newObjs, matchedObjs);
			
			//printMatch(newObjs, matchedObjs, true);
			
			//Set Initial Objs Movements
			iniObjsMovement.clear();
			
			//new edit remove on unmatched new objs. since they all have -1 id, which we do not want to use.
			
			for (ABObject obj : matchedObjs.keySet())
			{
				ABObject initial = matchedObjs.get(obj);
				
				if(initial != null){
					Movement movement = new Movement(obj);
					movement.generateInertia(initial);
					iniObjsMovement.put(obj, movement);
				}
			}
			setInitialObjects(newObjs);
			return true;
		}
		return false;
	}

	public static void main(String args[])
	{
		//String filename = "speedTest_48";
		String filename = "e1L10_52";//"e1L17_58";//"F:\\Samples\\L7_1_54";
				//"F:\\Samples\\L11_1_59";//"e1L17_58";//"e1L15_53";//"e1L7_54";//"t14";//"e1L16_55";//"e1L10_52";//"e1L7_62";//"t6";//"e1L9_62";//"e1L7_54";//"e1L9_62";//"t11";//"e1L7_54";//"e1L18_55";// "t11";//"t11";//"e2l3_65";//;
		int timegap = 200;
		int step = 1;
		if(filename.contains("_"))
			timegap = Integer.parseInt(filename.substring(filename.lastIndexOf("_") + 1));
		Tracker tracker = new KnowledgeTrackerBaseLine_8(timegap * step);
		TrackingFrameComparison tfc = new TrackingFrameComparison(filename, tracker, step);// t3,t9,t5,t13 Fixed: t11, t12, t6, t14, t15[not]
		TrackingFrameComparison.continuous = true;
		tfc.run();
	}
}
