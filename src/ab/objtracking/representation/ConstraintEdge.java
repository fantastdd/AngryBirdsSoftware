package ab.objtracking.representation;

import org.jgrapht.graph.DefaultEdge;

import ab.vision.ABObject;

public class ConstraintEdge extends DefaultEdge {

	 private ABObject source;
     private ABObject target;
     public Relation label;
     public double distance = 0;

     public ConstraintEdge(ABObject source, ABObject target, Relation label, double distance) {
         
         this.source = source;
         this.target = target;
         this.label = label;
         this.distance = distance;
     }
     //Just for compatibility;
     public ConstraintEdge(ABObject source, ABObject target, Relation label) {
         
         this.source = source;
         this.target = target;
         this.label = label;
      
     }

     public ABObject getSource() {
         return source;
     }

     public void inverseDirection()
     {
    	 ABObject temp = source;
    	 source = target;
    	 target = temp;
    	 label = Relation.inverse(label);
     }
     public ABObject getTarget() {
         return target;
     }

     public String toString() {
         return source + " [ " + label + " ] "+ target;
     }

	private static final long serialVersionUID = -3762172925571437811L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
