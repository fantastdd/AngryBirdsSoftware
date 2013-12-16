package ab.objtracking.isomorphism;

import org.jgrapht.Graph;
import org.jgrapht.experimental.equivalence.EquivalenceComparator;

import ab.objtracking.representation.ConstraintEdge;
import ab.objtracking.representation.Relation;
import ab.vision.ABObject;

public class GREdgeChecker<V extends ABObject, E extends ConstraintEdge> implements EquivalenceComparator<E, Graph<V,E>>{

	public ABObject lastConflictSource = null;
	public ABObject lastConflictTarget = null;
	@Override
	public boolean equivalenceCompare(E arg1, E arg2, Graph<V, E> context1,
			Graph<V, E> context2) {
	
		/*if(arg1.label == arg2.label)
			return true;*/
		Relation r1 = arg1.label;
		Relation r2 = arg2.label;
		if(arg1.getSource().id == arg2.getTarget().id)
			r1 = Relation.inverse(r1);
		else
			if(arg1.getTarget().id == arg2.getTarget().id)
			{
				r1 = Relation.inverse(r1);
				r2 = Relation.inverse(r2);
			}
		
	/*	System.out.println(" ======  Edge Check ====== ");
		System.out.println(arg1);
		System.out.println(arg2);
		System.out.println(Relation.isNeighbor(r1, r2, 2));*/
		
		
	/*	Relation r1 = (arg1.getSource().id == arg2.getSource().id)? arg1.label : Relation.inverseRelation(arg1.label);
		Relation r2 = (arg1.getSource().id == arg2.getSource().id)? arg2.label : Relation.inverseRelation(arg2.label);*/
		if((r1 == Relation.UNASSIGNED || r2 == Relation.UNASSIGNED) || Relation.isNeighbor(r1, r2, 2))
			return true;
		
		lastConflictSource = arg1.getSource();
		lastConflictTarget = arg1.getTarget();
	    
		//System.out.println(" ====== Coflicts Detected ====== ");
		
		return false;
	}

	@Override
	public int equivalenceHashcode(E arg1, Graph<V, E> context) {
		
		return arg1.label.toString().hashCode();
	}
    




	


}
