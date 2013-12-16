package ab.objtracking.isomorphism;

import org.jgrapht.Graph;
import org.jgrapht.experimental.equivalence.EquivalenceComparator;

import ab.objtracking.representation.ConstraintEdge;
import ab.objtracking.representation.Relation;
import ab.vision.ABObject;

public class GRVertexChecker<V extends ABObject, E extends ConstraintEdge> implements EquivalenceComparator<V, Graph<V,E>>{

	@Override
	public boolean equivalenceCompare(V arg1, V arg2, Graph<V, E> context1,
			Graph<V, E> context2) {
		
		/*System.out.println(" ==== Vertex Check ==== ");
		System.out.println(arg1);
		System.out.println(arg2);*/
		return true;
	}

	@Override
	public int equivalenceHashcode(V arg1, Graph<V, E> context) {
		
		return 0; //arg1.id;
	}




	


}
