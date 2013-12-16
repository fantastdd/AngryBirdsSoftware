package ab.objtracking.representation.util;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.ListUtils;

import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.real.shape.RectType;


public class GlobalObjectsToolkit {
	//Introduce timing schema later
	private static Map<Integer, LinkedHashSet<ABObject>> wood, stone, ice;
	private static Map<Integer, ABObject> allObjs;
	private static LinkedHashSet<Integer> occludedIds;
	private static LinkedList<Integer> allIds;
	public static int uniqueObjsNum = 0;
	public static int allObjsNum = 0;
	//private static Map<Integer, Map<Integer, Relation>> lastRels;
	private static Map<Integer, ABObject> lastObjs; // all the objects configurations at last time appearance.
	public static void registerIniObjs(List<ABObject> iniObjs) {
		
		
		System.out.println(" Register ini objs in GlobalObjectsToolkit");
		wood = new HashMap<Integer, LinkedHashSet<ABObject>>();
		stone = new HashMap<Integer, LinkedHashSet<ABObject>>();
		ice = new HashMap<Integer, LinkedHashSet<ABObject>>();
		
		//initialize the spatial relations map;
		//lastRels = new HashMap<Integer, Map<Integer, Relation>>();
		lastObjs = new HashMap<Integer, ABObject>();
		
		occludedIds = new LinkedHashSet<Integer>();
		
		for (RectType rectType : RectType.values()) {
			
			wood.put(rectType.id, new LinkedHashSet<ABObject>());
			stone.put(rectType.id, new LinkedHashSet<ABObject>());
			ice.put(rectType.id, new LinkedHashSet<ABObject>());
			
		}
		allObjs = new HashMap<Integer, ABObject>();
		//create an initial network
		//List<DirectedGraph<ABObject, ConstraintEdge>> graphs = GSRConstructor.contructNetworks(iniObjs);
		//DirectedGraph<ABObject, ConstraintEdge> fullNetwork = graphs.get(0);
		
		
		
		allObjsNum = iniObjs.size();
		uniqueObjsNum = 0;
		
		for (ABObject obj : iniObjs) {
		
			if(obj.type == ABType.Hill)
				uniqueObjsNum++;
			
			allObjs.put(obj.id, obj);
			
			//Map<Integer, Relation> relMap = new HashMap<Integer, Relation>(); 
			//lastRels.put(obj.id, relMap);
			//updateRels(obj, fullNetwork);
			
			switch (obj.type) {
			case Wood: {
				wood.get(obj.rectType.id).add(obj);
				break;
			}
			case Stone: {
				stone.get(obj.rectType.id).add(obj);
				break;
			}
			case Ice: {
				ice.get(obj.rectType.id).add(obj);
				break;
			}
			default:
				break;
			}
		}
		//count unique objects
		for (Integer key: wood.keySet())
		{
			if(wood.get(key).size() == 1)
				uniqueObjsNum++;
		}
		for (Integer key: stone.keySet())
		{
			if(stone.get(key).size() == 1)
				uniqueObjsNum++;
		}
		for (Integer key: ice.keySet())
		{
			if(ice.get(key).size() == 1)
				uniqueObjsNum++;
		}
		allIds = new LinkedList<Integer>();
		allIds.addAll(allObjs.keySet());
		lastObjs.putAll(allObjs);
	}
/*	public static void updateOccludedObjs(Map<ABObject, ABObject> newToIniMatch)
	{	
		List<Integer> matchedIds = new LinkedList<Integer>();
		for (ABObject newObj :newToIniMatch.keySet())
		{
			//if(newToIniMatch.get(newObj) != null)
			if(newObj.id == 16)
				log(" Report " + newObj + " " + newToIniMatch.get(newObj));
			matchedIds.add(newObj.id);
			//occludedIds.remove(newObj.id);
		}
		occludedIds.clear();
		occludedIds.addAll(ListUtils.subtract(allIds, matchedIds));
		
	}*/
	/*private static void updateRels(ABObject newObj, DirectedGraph<ABObject, ConstraintEdge> newFullNetwork)
	{
		Set<ConstraintEdge> edges = newFullNetwork.edgesOf(newObj);
		Map<Integer, Relation> relmap = lastRels.get(newObj.id);
		for (ConstraintEdge edge: edges)
		{
			Relation r;
			Integer id;
			if(edge.getSource().id != newObj.id)
			{
				r = Relation.inverse(edge.label);
				id = edge.getSource().id;
			}
			else
				{
					r = edge.label;
					id = edge.getTarget().id;
				}
			relmap.put(id, r);
		}
	}*/
	
	/*public static Relation getCachedRel(ABObject sourceObj, ABObject targetObj)
	{
		return lastRels.get(sourceObj.id).get(targetObj.id);
	}*/
	public static void updateOccludedObjs(Map<ABObject, ABObject> newToIniMatch)
	{	
		List<Integer> matchedIds = new LinkedList<Integer>();
		for (ABObject newObj :newToIniMatch.keySet())
		{
			//if(newToIniMatch.get(newObj) != null)
			/*if(newObj.id == 16)
				log(" Report " + newObj + " " + newToIniMatch.get(newObj));*/
			matchedIds.add(newObj.id);
			lastObjs.put(newObj.id, newObj);
			//updateRels(newObj, newFullNetwork);
			//occludedIds.remove(newObj.id);
		}
		occludedIds.clear();
		occludedIds.addAll(ListUtils.subtract(allIds, matchedIds));
		
		
	}

	private static void printIniObjs()
	{
		//Print Wood
		log("================== Print all initial objects ================");
		log("Wood");
		for (Integer rectType : wood.keySet())
		{
			for (ABObject obj : wood.get(rectType))
				
			{
				log(obj.toString());
			}
		}
		log("Stone");
		for (Integer rectType : stone.keySet())
		{
			for (ABObject obj : stone.get(rectType))
			{
				log(obj.toString());
			}
		}
		log("Ice");
		for (Integer rectType : ice.keySet())
		{
			for (ABObject obj : ice.get(rectType))
			{
				log(obj.toString());
			}
		}
		log("=================== End of Print ========================== \n");
	}
	private static void log(String message){System.out.println(message);}
	// Assign the most recent disappeared objs.
	// unmatched cannot be DebrisGroup?
	public static ABObject getPossibleOccludedMatch(ABObject unmatched)
	{
		Map<Integer, LinkedHashSet<ABObject>> occObjs;
		
		//printIniObjs();
		
		if (unmatched.type == ABType.Wood)
		{
			occObjs = wood;
		}
		else
			if(unmatched.type == ABType.Stone)
				occObjs = stone;
			else
				if(unmatched.type == ABType.Ice)
					occObjs = ice;
				else
					return null;

		//Search for large-size occluded blocks
		ABObject matchedObj = null;
		for (int i = unmatched.rectType.id; i < RectType.values().length; i ++)
		{
			LinkedHashSet<ABObject> occByRectType = occObjs.get(i);
			
			float diff;
			diff = Integer.MAX_VALUE;
			for (ABObject obj : occByRectType)
			{
				if(occludedIds.contains(obj.id) && !ShapeToolkit.isDifferentShape(obj, unmatched))
				{  
					float _diff = ShapeToolkit.calMassShift(lastObjs.get(obj.id), unmatched);
					if (_diff < diff)
					{	
					
						diff = _diff;
						matchedObj = obj;
					}
					
				}
			}
			if (matchedObj != null)
			{
				/*if(matchedObj.id == 16)
					log(" Remove ^^ " + matchedObj + "   " + unmatched);*/
			  
				//check relations.
				//Relation r = GSRConstructor.computeRectToRectRelation(matchedObj, unmatched).r; 
				
				occludedIds.remove(matchedObj.id);
				return matchedObj;
			}
		}
		return null;
	
	}

	public static ABObject getIniObjById(int id) {
		return allObjs.get(id);
	}


}
