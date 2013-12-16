package ab.objtracking.dynamic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;

import ab.objtracking.representation.ConstraintEdge;
import ab.objtracking.representation.Relation;
import ab.vision.ABObject;

public class MovementPredictor {
	
	/**
	 * 
	 * Adjust movement on GRFullNetwork
	 * 
	 * */
	
	public static Movement adjustMovementOnAll(Movement movement, DirectedGraph<ABObject, ConstraintEdge> network)
	{
		ABObject obj = movement.object;
		int count = -1;
		Set<ConstraintEdge> edges = network.edgesOf(obj);
		if(edges.isEmpty())
		{
			movement.setAllowedYDirection(Movement.POSITIVE, Movement.MAX_SCOPE);
			if(obj.angle > Math.PI/2 && movement.getAllowedXDirection(Movement.POSITIVE) == Movement.NOT_ALLOWED)
				movement.setAllowedXDirection(Movement.POSITIVE, Movement.BOUNDING_SCOPE);
				else
					if(obj.angle < Math.PI/2 && movement.getAllowedXDirection(Movement.NEGATIVE) == Movement.NOT_ALLOWED){
						movement.setAllowedXDirection(Movement.NEGATIVE, Movement.BOUNDING_SCOPE);
			}
		}
		for (ConstraintEdge edge: edges)
		{
			count ++;
			ABObject target = edge.getTarget();
			Relation r = edge.label;
			// Note, debris all have the same ID.
			if (target.id == obj.id) {
				target = edge.getSource();
				r = Relation.inverse(r);
			}
			Relation left = Relation.getLeftpart(r);
			if(obj.isLevel)
			{
				if( left == Relation.S3 || left == Relation.S4 || left == Relation.S5)
					break;
			}
			else
				if(obj.isFat)
				{
					if( left == Relation.S3 || left == Relation.S4 || left == Relation.S5
							|| left == Relation.S6 || left == Relation.S7)
						break;
				
				}
				else if (obj.angle > Math.PI/2)
				{
					if( left == Relation.S6 || left == Relation.S7)
						break;
				} 
				else if (obj.angle < Math.PI/2)
				{
					if( left == Relation.S3 || left == Relation.S4)
						break;
				}
			
			if(count == edges.size() - 1)
			{	
				movement.setAllowedYDirection(Movement.POSITIVE, Movement.MAX_SCOPE);
				if(obj.angle > Math.PI/2 && movement.getAllowedXDirection(Movement.POSITIVE) == Movement.NOT_ALLOWED)
					movement.setAllowedXDirection(Movement.POSITIVE, Movement.BOUNDING_SCOPE);
					else
						if(obj.angle < Math.PI/2 &&  movement.getAllowedXDirection(Movement.NEGATIVE) == Movement.NOT_ALLOWED){
							movement.setAllowedXDirection(Movement.NEGATIVE, Movement.BOUNDING_SCOPE);
				}
			}
		}
		
		return movement;
	}
	
	/**
	 * 
	 * Adjust movement on GRNetwork
	 * 
	 * */
	public static Movement adjustMovementOnGR(Movement movement, DirectedGraph<ABObject, ConstraintEdge> network)
	{
		ABObject obj = movement.object;
		//check whether the obj is supported
		//obj is stable : Level: S3-S5. Fat: S3-S7, >PI/2 S6-S7, < PI/2 S3-S4
		int count = -1;
		Set<ConstraintEdge> edges = network.edgesOf(obj);
		//All edges will have the UNASSIGNED relation
		/*		if(edges.isEmpty())
		{
			movement.setAllowedYDirection(Movement.POSITIVE, Movement.MAX_SCOPE);
			if(obj.angle > Math.PI/2 && movement.getAllowedXDirection(Movement.POSITIVE) == Movement.NOT_ALLOWED)
				movement.setAllowedXDirection(Movement.POSITIVE, Movement.BOUNDING_SCOPE);
				else
					if(obj.angle < Math.PI/2 && movement.getAllowedXDirection(Movement.NEGATIVE) == Movement.NOT_ALLOWED){
						movement.setAllowedXDirection(Movement.NEGATIVE, Movement.BOUNDING_SCOPE);
			}
		}*/
		for (ConstraintEdge edge: edges)
		{
			count ++;
			ABObject target = edge.getTarget();
			Relation r = edge.label;
			// Note, debris all have the same ID.
			if (target.id == obj.id) {
				target = edge.getSource();
				r = Relation.inverse(r);
			}
			Relation left = Relation.getLeftpart(r);
			if(obj.isLevel)
			{
				if( left == Relation.R3 || left == Relation.R4 || left == Relation.R5)
					break;
			}
			else
				if(obj.isFat)
				{
					if( left == Relation.S3 || left == Relation.S4 || left == Relation.S5
							|| left == Relation.S6 || left == Relation.S7)
						break;
				
				}
				else if (obj.angle > Math.PI/2)
				{
					if( left == Relation.S6 || left == Relation.S7)
						break;
				} 
				else if (obj.angle < Math.PI/2)
				{
					if( left == Relation.S3 || left == Relation.S4)
						break;
				}
			if(count == edges.size() - 1)
			{	
				//System.out.println("@@@@  " + obj);
				movement.setAllowedYDirection(Movement.POSITIVE, Movement.MAX_SCOPE);
				if(obj.angle > Math.PI/2 && movement.getAllowedXDirection(Movement.POSITIVE) == Movement.NOT_ALLOWED)
					movement.setAllowedXDirection(Movement.POSITIVE, Movement.BOUNDING_SCOPE);
					else
						if(obj.angle < Math.PI/2 &&  movement.getAllowedXDirection(Movement.NEGATIVE) == Movement.NOT_ALLOWED){
							movement.setAllowedXDirection(Movement.NEGATIVE, Movement.BOUNDING_SCOPE);
				}
			}
		}
				
		return movement;
	}
	
	
	// currently do not consider the debris
	public static Map<ABObject, Movement> predict(DirectedGraph<ABObject, ConstraintEdge> network) {
		Map<ABObject, Movement> movements = new HashMap<ABObject, Movement>();
		// Calculate landmark movement first
		for (ABObject obj : network.vertexSet()) {
			if (!obj.isLevel && !obj.isFat) {
				Movement movement = new Movement(obj);
				
				if (obj.angle > Math.PI / 2) {
					movement.setAllowedXDirection(Movement.MAX_SCOPE, Movement.BOUNDING_SCOPE, Movement.MAX_SCOPE);
					/*Set<ConstraintEdge> edges = network.edgesOf(obj);
					for (ConstraintEdge edge: edges)
					{
						ABObject target = edge.getTarget();
						Relation r = edge.label;
						// Note, debris all have the same ID.
						if (target.id == obj.id) {
							target = edge.getSource();
							r = Relation.inverse(r);
						}
						//Leveler effect
						Relation left = Relation.getLeftpart(r);
						if (left == Relation.S8||
								left == Relation.S7
								|| left == Relation.S6) 
						{
							movement.setAllowedXDirection(Movement.MAX_SCOPE, Movement.BOUNDING_SCOPE, Movement.MAX_SCOPE);
							break;
						}
						
						
					}*/
					
				} else{
					
						movement.setAllowedXDirection(Movement.BOUNDING_SCOPE, Movement.MAX_SCOPE, Movement.MAX_SCOPE);
				/*		Set<ConstraintEdge> edges = network.edgesOf(obj);
						for (ConstraintEdge edge: edges)
						{
							ABObject target = edge.getTarget();
							Relation r = edge.label;
							// Note, debris all have the same ID.
							if (target.id == obj.id) {
								target = edge.getSource();
								r = Relation.inverse(r);
							}
							//Leveler effect
							Relation left = Relation.getLeftpart(r);
							if ( left == Relation.S2 || left == Relation.S3 || left == Relation.S4) 
							{
								movement.setAllowedXDirection(Movement.BOUNDING_SCOPE, Movement.MAX_SCOPE, Movement.MAX_SCOPE);
								break;
							}
							
							
						}*/
					}
				movements.put(obj, movement);
			}
		}
		// Then use the landmarks to predict others' movement
		Set<ABObject> landmarks = movements.keySet();
		LinkedList<ABObject> landmarksList = new LinkedList<ABObject>();
		landmarksList.addAll(landmarks);
		while(!landmarksList.isEmpty())
		{
			ABObject landmark = landmarksList.remove();
			Set<ConstraintEdge> edges = network.edgesOf(landmark);
			Movement landmarkMovement = movements.get(landmark);
			for (ConstraintEdge edge : edges) {
				ABObject target = edge.getTarget();
				Relation r = edge.label;
				// Note, debris all have the same ID.
				if (target.id == landmark.id) {
					target = edge.getSource();
					r = Relation.inverse(r);
				}
				if (!movements.containsKey(target))
					if (landmark.isLevel)
					{
						Movement movement = new Movement(landmarkMovement,
								target);
						movements.put(target, movement);
						landmarksList.add(target);
					}
					else
					if (landmark.angle > Math.PI / 2) {
						Relation left = Relation.getLeftpart(r);
						if ( left == Relation.S1
								|| left == Relation.S8
								|| left == Relation.S7) {
							Movement movement = new Movement(landmarkMovement,
									target);
							movements.put(target, movement);
							landmarksList.add(target);

						}
					}
					else if (landmark.angle < Math.PI / 2) 
					{
						Relation left = Relation.getLeftpart(r);
						if ( left == Relation.S1
								|| left == Relation.S2
								|| left == Relation.S3) {
							Movement movement = new Movement(landmarkMovement,
									target);
							movements.put(target, movement);
							landmarksList.add(target);
						}
					}

			}
		}
		return movements;
	}

	


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
