package ab.objtracking.representation.util;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.SimpleDirectedGraph;

import ab.demo.other.ActionRobot;
import ab.objtracking.MagicParams;
import ab.objtracking.representation.ConstraintEdge;
import ab.objtracking.representation.Relation;
import ab.utils.ImageSegFrame;
import ab.vision.ABList;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.VisionUtils;
import ab.vision.real.MyVision;
import ab.vision.real.shape.Rect;

public class GSRConstructor {

	/**
	 * @param grnetwork: network with GR relations
	 * @return a list of all possible set of objects that have the same kinematics configurations (evaluating by GR relations)
	 * */
	public static List<Set<ABObject>> getAllKinematicsGroups(DirectedGraph<ABObject, ConstraintEdge> grnetwork)
	{

		
		List<Set<ABObject>> allGroups = new LinkedList<Set<ABObject>>();

		Set<ABObject> vertices = grnetwork.vertexSet();

		//GR Group
		List<Set<ABObject>> grgroups = new LinkedList<Set<ABObject>>();
		
		ArrayList<ABObject> vlist = new ArrayList<ABObject>();
		
		vlist.addAll(vertices);
		//Sort by ID
		Collections.sort(vlist, new Comparator<ABObject>(){

			@Override
			public int compare(ABObject o1, ABObject o2) {

				return ((Integer)o1.id).compareTo(o2.id);
			}});
		for (int i = 0; i < vlist.size(); i++)
		{
			ABObject o1 = vlist.get(i);
			
			Set<ConstraintEdge> o1set = grnetwork.edgesOf(o1);//get all edges;
			
			Set<ABObject> v1set = new HashSet<ABObject>();//get neighbor objs: those objects will potentially give force

			for (ConstraintEdge edge : o1set)
			{
				if(edge.distance > MagicParams.VisionGap)
					continue;
				
				ABObject vertex = edge.getTarget();
				if(vertex != o1)
					v1set.add(vertex);
				else
					v1set.add(edge.getSource());
				
				//================  Test and Add GR Groups ============
				//if (Relation.isEE(edge.label))
				if(Relation.isGRRelation(edge.label))
				{
					Set<ABObject> set = new HashSet<ABObject>();
					set.add(o1);
					set.add(vertex);
					grgroups.add(set);
				}
				//===============  Test and Add End ===================
				//System.out.println(edge);
			}
			Set<ABObject> sameGroup = new HashSet<ABObject>();
			int o1Degree = grnetwork.inDegreeOf(o1) + grnetwork.outDegreeOf(o1);
			
			
			//Search o1 neighbor's neighbors
			for (int j = 0; j < vlist.size(); j++)
			{
				ABObject o2 = vlist.get(j);

				if(v1set.contains(o2))
				{					
					ConstraintEdge e;
					Relation r;
					e = grnetwork.getEdge(o2, o1);
					if(e == null)
					{	
						e = grnetwork.getEdge(o1, o2);
						r = Relation.inverse(e.label);
					}
					else
						r = e.label;

					Set<ConstraintEdge> o2set = grnetwork.edgesOf(o2);
					Set<ABObject> sameLabelSet = new HashSet<ABObject>();
					for (ConstraintEdge edge : o2set)
					{
						if(edge.distance > MagicParams.VisionGap)
							continue;
						ABObject vertex = edge.getTarget();
						int vertexDegree = grnetwork.inDegreeOf(vertex) + grnetwork.outDegreeOf(vertex);
						Relation _r = edge.label;
						if(vertex == o2)
						{	
							vertex = edge.getSource();
							_r = Relation.inverse(_r);
						}
						if ( r == _r)
						{
							if(vertex != o1 && o1Degree == vertexDegree && vertex.type == o1.type)
								sameLabelSet.add(vertex);
						}

					}
					
				/*	log("print same group " + o1);
					if(sameGroup.isEmpty())
						log("empty group size ");
					for (ABObject obj : sameGroup)
					{
						log(obj + "");
					}
					log("\n print same label set " + o2);
					if(sameLabelSet.isEmpty())
						log("empty label set size");
					else
						for (ABObject obj : sameLabelSet)
							log(obj + "");
					 */

					if(sameGroup.isEmpty())
						sameGroup.addAll(sameLabelSet);
					else
						sameGroup.retainAll(sameLabelSet);	

				}

			}
			sameGroup.add(o1);
			/*log("\nprint same group before adding " + o1);
			if(sameGroup.isEmpty())
				log("\nempty group size ");
			for (ABObject obj : sameGroup)
			{
				log(obj + "");
			}*/

		/*	log(" add to all groups " + o1);
			log(allGroups.contains(sameGroup) + "");*/

			if(!allGroups.contains(sameGroup) && sameGroup.size() > 1)
				allGroups.add(sameGroup);

		}

		
	
		
		allGroups.addAll(grgroups);
		
		//Remove subset stuff
		List<Set<ABObject>> subsets = new LinkedList<Set<ABObject>>();
		for (int i = 0; i < allGroups.size() - 1; i++)
		{
			Set<ABObject> set1 = allGroups.get(i);
			for (int j = i + 1; j < allGroups.size(); j++)
			{
				Set<ABObject> set2 = allGroups.get(j);
				if(set1.containsAll(set2))
					subsets.add(set2);
				else
					if(set2.containsAll(set1))
					{
						subsets.add(set1);
						break;
					}
			}
		}
		allGroups.removeAll(subsets);

		//printNetwork(grnetwork);
		//printGroup(allGroups);
		
		return allGroups;
		
	
	}
	
	private static void printGroup(List<Set<ABObject>> allGroups)
	{
		log("\nPrint Group");
		int count = 0;
		for (Set<ABObject> objs : allGroups)
		{
			System.out.println("======== Group " + ++count + "=========");
			for (ABObject obj : objs)
			{
				System.out.println(obj);
			}

		}
		
	}
	
	
	/***
	 * @return graphs[]: graphs[0]: Full network, graph[1]: GR network
	 * Construct two Constraint network: Full and GR
	 */
	public static List<DirectedGraph<ABObject, ConstraintEdge>> contructNetworks(List<ABObject> objs)
	{
		DirectedGraph<ABObject, ConstraintEdge> fullGraph,grGraph;

		fullGraph = new SimpleDirectedGraph<ABObject, ConstraintEdge>(new ClassBasedEdgeFactory<ABObject, ConstraintEdge>(ConstraintEdge.class));
		grGraph = new SimpleDirectedGraph<ABObject, ConstraintEdge>(new ClassBasedEdgeFactory<ABObject, ConstraintEdge>(ConstraintEdge.class));

		List<DirectedGraph<ABObject, ConstraintEdge>> graphs = new ArrayList<DirectedGraph<ABObject, ConstraintEdge>>();

		//Sort by ID
		Collections.sort(objs, new Comparator<ABObject>(){

			@Override
			public int compare(ABObject o1, ABObject o2) {

				return ((Integer)o1.id).compareTo(o2.id);
			}});
		//Create Node
		for (ABObject obj : objs)
		{
			fullGraph.addVertex(obj);
			grGraph.addVertex(obj);
		}
		for ( int i = 0; i < objs.size() - 1; i++ )
		{
			ABObject sourceVertex = objs.get(i);
			for (int j = i + 1; j < objs.size(); j++ )
			{
				ABObject targetVertex = objs.get(j);
				RelationPair pair = computeRelation(sourceVertex, targetVertex);
				if (sourceVertex.equals(targetVertex))
					System.out.println(" Duplicate: " + sourceVertex + "  " + sourceVertex.hashCode() + "  " + targetVertex + "  " + targetVertex.hashCode());
				
				fullGraph.addEdge(sourceVertex, targetVertex, new ConstraintEdge(sourceVertex, targetVertex, pair.r, pair.distance));	
				if(Relation.isGRRelation(pair.r))
					grGraph.addEdge(sourceVertex, targetVertex, new ConstraintEdge(sourceVertex, targetVertex, pair.r, pair.distance));
			}
		}
		graphs.add(fullGraph);
		graphs.add(grGraph);
		return graphs;
	}
	//Construct Constraint network (directed-graph)
	public static DirectedGraph<ABObject, ConstraintEdge> constructFullNetwork(List<ABObject> objs)
	{
		DirectedGraph<ABObject, ConstraintEdge> graph = new SimpleDirectedGraph<ABObject, ConstraintEdge>(new ClassBasedEdgeFactory<ABObject, ConstraintEdge>(ConstraintEdge.class));

		//Sort by ID
		Collections.sort(objs, new Comparator<ABObject>(){

			@Override
			public int compare(ABObject o1, ABObject o2) {

				return ((Integer)o1.id).compareTo(o2.id);
			}});

		//Create Node
		for (ABObject obj : objs)
		{
			graph.addVertex(obj);
		}

		for ( int i = 0; i < objs.size() - 1; i++ )
		{
			ABObject sourceVertex = objs.get(i);
			for (int j = i + 1; j < objs.size(); j++ )
			{
				ABObject targetVertex = objs.get(j);
				RelationPair pair = computeRelation(sourceVertex, targetVertex);
				graph.addEdge(sourceVertex, targetVertex, new ConstraintEdge(sourceVertex, targetVertex, pair.r, pair.distance));	
			}
		}
		return graph;
	}

	public static DirectedGraph<ABObject, ConstraintEdge> addVertexToGRNetwork(ABObject obj, DirectedGraph<ABObject, ConstraintEdge> graph)
	{

		//Create Node
		graph.addVertex(obj);

		for (ABObject vertex : graph.vertexSet())
		{
			if (vertex.id > obj.id)
			{
				RelationPair pair = computeRelation(obj, vertex);

				if(Relation.isGRRelation(pair.r))
					graph.addEdge(obj, vertex, new ConstraintEdge(obj, vertex, pair.r, pair.distance));
				else
					graph.addEdge(obj, vertex, new ConstraintEdge(obj, vertex, Relation.UNASSIGNED, 0));
			}
			else
				if(obj.id > vertex.id)
				{
					RelationPair pair = computeRelation(vertex, obj);
					if(Relation.isGRRelation(pair.r))
						graph.addEdge(vertex, obj, new ConstraintEdge(vertex, obj, pair.r, pair.distance));
					else
						graph.addEdge(vertex, obj, new ConstraintEdge(vertex, obj, Relation.UNASSIGNED, 0));

				}

		}
		return graph;


	}

	public static DirectedGraph<ABObject, ConstraintEdge> constructGRNetwork(List<ABObject> objs)
	{

		DirectedGraph<ABObject, ConstraintEdge> graph = new SimpleDirectedGraph<ABObject, ConstraintEdge>(new ClassBasedEdgeFactory<ABObject, ConstraintEdge>(ConstraintEdge.class));


		//Sort by ID
		Collections.sort(objs, new Comparator<ABObject>(){

			@Override
			public int compare(ABObject o1, ABObject o2) {

				return ((Integer)o1.id).compareTo(o2.id);
			}});
		//Create Node
		for (ABObject obj : objs)
		{
			graph.addVertex(obj);
		}
		for ( int i = 0; i < objs.size() - 1; i++ )
		{
			ABObject sourceVertex = objs.get(i);
			for (int j = i + 1; j < objs.size(); j++ )
			{
				ABObject targetVertex = objs.get(j);
				RelationPair pair = computeRelation(sourceVertex, targetVertex);


				if(Relation.isGRRelation(pair.r))
					graph.addEdge(sourceVertex, targetVertex, new ConstraintEdge(sourceVertex, targetVertex, pair.r, pair.distance));	
				else
					graph.addEdge(sourceVertex, targetVertex, new ConstraintEdge(sourceVertex, targetVertex, Relation.UNASSIGNED, 0));


			}

		}
		return graph;


	}
	public static DirectedGraph<ABObject, ConstraintEdge> constructGRNetworkWithoutUnassignedRelations(List<ABObject> objs)
	{

		DirectedGraph<ABObject, ConstraintEdge> graph = new SimpleDirectedGraph<ABObject, ConstraintEdge>(new ClassBasedEdgeFactory<ABObject, ConstraintEdge>(ConstraintEdge.class));
		//Create Node
		for (ABObject obj : objs)
		{
			graph.addVertex(obj);
		}

		//Sort by ID
		Collections.sort(objs, new Comparator<ABObject>(){

			@Override
			public int compare(ABObject o1, ABObject o2) {

				return ((Integer)o1.id).compareTo(o2.id);
			}});

		for ( int i = 0; i < objs.size() - 1; i++ )
		{
			ABObject sourceVertex = objs.get(i);
			for (int j = i + 1; j < objs.size(); j++ )
			{
				ABObject targetVertex = objs.get(j);
				RelationPair pair = computeRelation(sourceVertex, targetVertex);
				if(Relation.isGRRelation(pair.r))
					graph.addEdge(sourceVertex, targetVertex, new ConstraintEdge(sourceVertex, targetVertex, pair.r, pair.distance));	


			}

		}
		return graph;


	}
	private static RelationPair computeRelation(ABObject source, ABObject target)
	{

		return computeRectToRectRelation(source, target);
	}

	public static RelationPair computeRectToRectRelation(ABObject source, ABObject target)
	{
		Rectangle mbr_1 = source.getBounds();
		Rectangle mbr_2 = target.getBounds();
		boolean vertical_intersect =  isIntervalIntersect(mbr_1.y, mbr_1.y + mbr_1.height, mbr_2.y, mbr_2.y + mbr_2.height);
		boolean horizontal_intersect = isIntervalIntersect(mbr_1.x, mbr_1.x + mbr_1.width, mbr_2.x, mbr_2.x + mbr_2.width);
		if (vertical_intersect && horizontal_intersect)
			return computeRectToRectContactRelation(source, target);
		else 
			return computeNonContactRelation(source, target, vertical_intersect, horizontal_intersect);

	}

	private static RelationPair computeRectToRectContactRelation(ABObject source, ABObject target)
	{
		if (source.type == ABType.Hill)
		{
			return new RelationPair(Relation.BOTTOM, 0);
		} 
		else
			if(target.type == ABType.Hill)
				return new RelationPair(Relation.TOP, 0);
		Line2D[] sourceSectors = source.sectors;
		Line2D[] targetSectors = target.sectors;
		double distance;
		int sIndex = -1;
		int tIndex = -1;
		double minDistance = Integer.MAX_VALUE;
		/*if(sourceSectors == null)
			System.out.println(source);*/
		int[] EdgeSumDist = new int[8];

		for (int i = 0; i < sourceSectors.length; i ++)
		{
			Line2D ss = sourceSectors[i];
			for (int j = (i%2 + 1)%2; j < targetSectors.length; j += 2)
			{
				Line2D ts = targetSectors[j];
				if(i%2 == 0)
				{	
					distance = ts.ptSegDist(ss.getP1());
					EdgeSumDist[4 + (j - 1)/2] += distance; 
				}
				else
				{
					distance = ss.ptSegDist(ts.getP1());
					EdgeSumDist[(i - 1)/2] += distance;
				}

				if (distance <= minDistance)
				{
					minDistance = distance;
					sIndex = i;
					tIndex = j;
				}	
			}
		}
	
		// check edge-edge relation, relaxation here
		double angleDiff;
		double _sourceAngle, _targetAngle;
		_sourceAngle =  (source.angle >= Math.PI/2)? source.angle - Math.PI/2: source.angle;
		_targetAngle =  (target.angle >= Math.PI/2)? target.angle - Math.PI/2: target.angle;

		angleDiff = Math.abs(_sourceAngle - _targetAngle);
		
		if ( ((source.isLevel && target.isLevel)||(!source.isLevel && !target.isLevel) ) && (angleDiff < MagicParams.AngleTolerance || angleDiff > Math.PI/2 - MagicParams.AngleTolerance))
		{
			//edge touch
			double sMin = Integer.MAX_VALUE;
			int index = -1;
			int counter = -1;
			for (int sum : EdgeSumDist)
			{
				counter ++;
				if(sum < sMin)
				{
					sMin = sum;
					index = counter; 
				}
			}
			if (index < 4)
			{
				
				index = index * 2 + 1;
				
				if(!source.isLevel && !target.isLevel)
					switch(index)
					{
					case 1: return new RelationPair( Relation.getRelation(1, false, 5, false), minDistance);
					case 3: return new RelationPair( Relation.getRelation(3, false, 7, false), minDistance);
					case 5: return new RelationPair( Relation.getRelation(5, false, 1, false), minDistance);
					case 7: return new RelationPair( Relation.getRelation(7, false, 3, false), minDistance);
					default: return new RelationPair( Relation.INVALID1, 0);
					}
				else
					if(source.isLevel && target.isLevel)
					{
						switch(index)
						{
						case 1: return new RelationPair( Relation.getRelation(1, true, 5, true), minDistance);
						case 3: return new RelationPair( Relation.getRelation(3, true, 7, true), minDistance);
						case 5: return new RelationPair( Relation.getRelation(5, true, 1, true), minDistance);
						case 7: return new RelationPair( Relation.getRelation(7, true, 3, true), minDistance);
						default: return new RelationPair( Relation.INVALID1, 0);
						}
					}
					else
					{
						if(source.isLevel)
						{
							switch(index)
							{
							case 1: return new RelationPair( Relation.getRelation(1, true, 6, false), minDistance);
							case 3: return new RelationPair( Relation.getRelation(3, true, 0, false), minDistance);
							case 5: return new RelationPair( Relation.getRelation(5, true, 2, false), minDistance);
							case 7: return new RelationPair( Relation.getRelation(7, true, 4, false), minDistance);
							default: return new RelationPair( Relation.INVALID1, 0);
							}
						} 
						else
							switch(index)
							{

							case 1: return new RelationPair( Relation.getRelation(6, false, 1, true), minDistance);
							case 3: return new RelationPair( Relation.getRelation(0, false, 3, true), minDistance);
							case 5: return new RelationPair( Relation.getRelation(2, false, 5, true), minDistance);
							case 7: return new RelationPair( Relation.getRelation(4, false, 7, true), minDistance);
							default: return new RelationPair( Relation.INVALID1, 0);
							}
					}

			} 
			else
			{
				//System.out.println(" tIndex" + tIndex + " sIndex " + "   " + sIndex);
				index -= 4;	
				index = index * 2 + 1;
				if(!source.isLevel && !target.isLevel)
					switch(index)
					{
					case 1: return new RelationPair( Relation.getRelation(5, false, 1, false), minDistance);
					case 3: return new RelationPair( Relation.getRelation(7, false, 3, false), minDistance);
					case 5: return new RelationPair( Relation.getRelation(1, false, 5, false), minDistance);
					case 7: return new RelationPair( Relation.getRelation(3, false, 7, false), minDistance);
					default: return new RelationPair( Relation.INVALID1, 0);
					}
				else
					if(source.isLevel && target.isLevel)
					{
						switch(index)
						{
						case 1: return new RelationPair( Relation.getRelation(5, true, 1, true), minDistance);
						case 3: return new RelationPair( Relation.getRelation(7, true, 3, true), minDistance);
						case 5: return new RelationPair( Relation.getRelation(1, true, 5, true), minDistance);
						case 7: return new RelationPair( Relation.getRelation(3, true, 7, true), minDistance);
						default: return new RelationPair( Relation.INVALID1, 0);
						}
					}
					else
					{
						if(source.isLevel)
						{
							switch(index)
							{
							case 1: return new RelationPair( Relation.getRelation(6, true, 1, false), minDistance);
							case 3: return new RelationPair( Relation.getRelation(0, true, 3, false), minDistance);
							case 5: return new RelationPair( Relation.getRelation(2, true, 5, false), minDistance);
							case 7: return new RelationPair( Relation.getRelation(4, true, 7, false), minDistance);
							default: return new RelationPair( Relation.INVALID1, 0);
							}
						} 
						else
							switch(index)
							{
							case 1: return new RelationPair( Relation.getRelation(1, false, 6, true), minDistance);
							case 3: return new RelationPair( Relation.getRelation(3, false, 0, true), minDistance);
							case 5: return new RelationPair( Relation.getRelation(5, false, 2, true), minDistance);
							case 7: return new RelationPair( Relation.getRelation(7, false, 4, true), minDistance);
							default: return new RelationPair( Relation.INVALID1, 0);
							}
					}
			}

		}
		else 
		{
			//System.out.println(sIndex + "  " +  source.isLevel + "  " + tIndex + "  " + target.isLevel);
			//Find corner
			if(sIndex%2 == 0)
			{
				tIndex = getCorrectSectorIndex(source, target, sIndex, tIndex);						
			}
			else
				sIndex = getCorrectSectorIndex(target, source, tIndex, sIndex);
		}
		//System.out.println(sIndex + "  " +  source.isLevel + "  " + tIndex + "  " + target.isLevel);
		Relation r =  Relation.getRelation(sIndex, source.isLevel, tIndex, target.isLevel);
		//log(r + "");
		RelationPair pair = new RelationPair(r, minDistance);
		return pair;
	}
	private static int getCorrectSectorIndex(ABObject source, ABObject target, int sIndex, int tIndex)
	{
		//System.out.println(" " + source.isLevel + "  " + sIndex + " " + target.isLevel + "  " + tIndex);
		int sIndex1 = (sIndex == 0)? 7 : sIndex - 1;
		int sIndex2 = sIndex + 1;

		Line2D sector1 = source.sectors[sIndex1];
		Line2D sector2 = source.sectors[sIndex2];
		Line2D tsector = target.sectors[tIndex];
		/*System.out.println(
		String.format("%d %s %s %d %s %s %b", sIndex1, sector1.getP1(), sector1.getP2(), sIndex2, sector2.getP1(), sector2.getP2(), (sector1.getP1().equals(sector2.getP2()))));*/
		if(!sectorsTouchPossible(sector1, sIndex1, sector2, sIndex2, tsector))
		{
			//System.out.println(" @@ ");
			if(sIndex == 4 && tIndex == 7)
				tIndex = 1;
			else if(sIndex == 4 && tIndex == 1 )
				tIndex = 7;
			else if(sIndex == 0 && tIndex == 5 )
				tIndex = 3;
			else if(sIndex == 0 && tIndex == 3 )
				tIndex = 5;
			else if(sIndex == 2 && tIndex == 5 )
				tIndex = 7;
			else if(sIndex == 2 && tIndex == 7 )
				tIndex = 5;
			else if(sIndex == 6 && tIndex == 1 )
				tIndex = 3;
			else if(sIndex == 6 && tIndex == 3 )
				tIndex = 1;
		}
		return tIndex;
	}
	/**
	 * Test whether sector3 can touch the corner of sector1 and sector2 without penetrating.
	 * */
	private static boolean sectorsTouchPossible(Line2D sector1, int sid1, Line2D sector2, int sid2, Line2D sector3)
	{
		double angle1, angle2;
		Point2D commonCorner = null, corner1 = null, corner2 = null;
		if (sector1.getP1().equals(sector2.getP1()))
		{
			commonCorner = sector1.getP1();
			corner1 = sector1.getP2();
			corner2 = sector2.getP2();
		}
		else
			if (sector1.getP1().equals(sector2.getP2()))
			{
				commonCorner = sector1.getP1();
				corner1 = sector1.getP2();
				corner2 = sector2.getP1();
			}
			else
				if (sector1.getP2().equals(sector2.getP1()))
				{
					commonCorner = sector1.getP2();
					corner1 = sector1.getP1();
					corner2 = sector2.getP2();
				}
				else if (sector1.getP2().equals(sector2.getP2()))
				{
					commonCorner = sector1.getP2();
					corner1 = sector1.getP1();
					corner2 = sector2.getP1();
				}
				else
					log("Error in GSRConstructor: sectorsTouchPossible Method");

		angle1 = divide( corner1.getY() - commonCorner.getY(), corner1.getX() - commonCorner.getX());
		angle2 = divide( corner2.getY() - commonCorner.getY(), corner2.getX() - commonCorner.getX());
		double angle3 = divide( (sector3.getY1() - sector3.getY2()), (sector3.getX1() - sector3.getX2()));
		
		angle1 = Math.atan(angle1);
		angle2 = Math.atan(angle2);
		angle3 = Math.atan(angle3);
		
		angle1 = (angle1 == Math.PI/2)? - angle1 : angle1;
		angle2 = (angle2 == Math.PI/2)? - angle2 : angle2;
		angle3 = (angle3 == Math.PI/2)? - angle3 : angle3;
		
		sid1 = (sid1 > 7)? sid1 - 8: sid1;
		sid2 = (sid2 > 7)? sid2 - 8: sid2;
		
		sid1 ++;
		sid2 ++;
		
		int cornerId = (sid1 + sid2)/2;
		
		if (cornerId == 5 && (sid1 == 2 || sid1 == 8))
			cornerId = 1;
		
		if (cornerId == 1 || cornerId == 5)
		{
			if(angle1 > angle2)
			{
				if(angle3 >= angle1 || angle3 <= angle2)
					return false;
				else
					return true;
			}
			else
			{
				if(angle3 >= angle2 || angle3 <= angle1)
					return false;
				else
					return true;
			}
		}
		else
		{
			if(angle1 > angle2)
			{
				if(angle3 <= angle1 && angle3 >= angle2)
					return false;
				else
					return true;
			}
			else
			{
				if(angle3 <= angle2 && angle3 >= angle1)
					return false;
				else
					return true;
			}
		}
		/*log(angle1 + " " + angle2 + " " + angle3);
		if(angle1 > angle2)
		{
			if(angle3 >= angle1 || angle3 <= angle2)
				return false;
			else
				return true;
		}
		else
		{
			log (angle3 + "  " + angle1 + " " + (angle3 <= angle1));
			if(angle3 >= angle2 || angle3 <= angle1)
				return false;
			else
				return true;
		}*/
	}
	private static double divide(double x, double y)
	{
		if ( y == 0)
			return Double.MAX_VALUE;
		else
			return x/y;
	}
	private static void log(String message)
	{
		System.out.println(message);
	}
	//TODO Perform some relaxiaion here
	private static RelationPair computeNonContactRelation(ABObject source, ABObject target, boolean vertical_intersect, boolean horizontal_intersect)
	{
		Point source_center = source.getCenter();
		Point target_center = target.getCenter();
		boolean above = source_center.getY() < target_center.getY();
		boolean left = 	source_center.getX() < target_center.getX();
		double distance = 0;
		if (horizontal_intersect)
		{
			
			if(above)
			{	
				distance = target.getBounds().getMinY() - source.getBounds().getMaxY();
				return new RelationPair(Relation.TOP, distance);
			}
			else
			{
				distance = source.getBounds().getMinY() - target.getBounds().getMaxY();
				return new RelationPair(Relation.BOTTOM, distance);
			}
		}
		else
			if(vertical_intersect)
			{
				if(left)
				{
					distance = target.getBounds().getMinX() - source.getBounds().getMaxX();
					return new RelationPair(Relation.LEFT, distance);
				}
				else
				{
					distance = source.getBounds().getMinX() - target.getBounds().getMaxX();
					return new RelationPair(Relation.RIGHT, distance);
				}
			}
			else 
			{
				if(above && left)
				{
					distance = getDistance(source.getBounds().getMaxX(), source.getBounds().getMaxY(), target.getBounds().getMinX(), target.getBounds().getMinY()); 
					return new RelationPair(Relation.TOP_LEFT, distance);
				}
				else
					if(above)
					{	
						distance = getDistance(source.getBounds().getMinX(), source.getBounds().getMaxY(), target.getBounds().getMaxX(), target.getBounds().getMinY()); 
						return new RelationPair(Relation.TOP_RIGHT, distance);
					}
					else
						if(left)
						{	
							distance = getDistance(source.getBounds().getMaxX(), source.getBounds().getMinY(), target.getBounds().getMinX(), target.getBounds().getMaxY()); 
							return new RelationPair(Relation.BOTTOM_LEFT, distance);
						}
						else
						{
							distance = getDistance(source.getBounds().getMinX(), source.getBounds().getMinY(), target.getBounds().getMaxX(), target.getBounds().getMaxY());
							return new RelationPair(Relation.BOTTOM_RIGHT, distance);
						}
			}



	}
	private static double getDistance(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt( (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}
	private static double getDistance(Point p1, Point p2)
	{
		return Math.sqrt( (p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY()));
	}
	/*	private static boolean isMBRIntersect(Rectangle mbr_1, Rectangle mbr_2)
	{
		if( isIntervalIntersect(mbr_1.x, mbr_1.x + mbr_1.width, mbr_2.x, mbr_2.x + mbr_2.width)
				&& isIntervalIntersect(mbr_1.y, mbr_1.y + mbr_1.height, mbr_2.y, mbr_2.y + mbr_2.height))
			return true;
		return false;
	}*/
	private static boolean isIntervalIntersect(int s1, int e1, int s2, int e2)
	{
		if( (s1 > e2 + MagicParams.VisionGap || s2 > e1 + MagicParams.VisionGap))
			return false;
		return true;
	}
	public static void printNetwork(Graph<ABObject, ConstraintEdge> network){

		//System.out.println(network);
		for (ABObject vertex: network.vertexSet())
		{
			System.out.println(" vertex: " + vertex);
			for (ConstraintEdge edge: network.edgesOf(vertex))
			{
				System.out.println(edge);
			}
			System.out.println("------------------------------");
		}
	}
	public static void performanceTesting()
	{
		new ActionRobot();
		BufferedImage screenshot = ActionRobot.doScreenShot();
		screenshot = VisionUtils.resizeImage(screenshot, 800, 1200);

		MyVision vision = new MyVision(screenshot);


		ABList allInterestObjs = ABList.newList();
		allInterestObjs.addAll(vision.findObjects());
		vision.drawObjectsWithID(screenshot, true);
		ImageSegFrame frame = new ImageSegFrame(" Test GSR Constructor ", screenshot, null);
		screenshot = VisionUtils.resizeImage(screenshot, 800, 1200);
		frame.refresh(screenshot);
		long time = System.nanoTime();
		DirectedGraph<ABObject, ConstraintEdge> network = GSRConstructor.constructGRNetwork(allInterestObjs);
		System.out.println(" Time: " + (System.nanoTime() - time) + " nanos ");
		for (ABObject vertex: network.vertexSet())
		{
			System.out.println(" vertex: " + vertex);
			for (ConstraintEdge edge: network.edgesOf(vertex))
			{
				System.out.println(edge);
			}
			System.out.println("------------------------------");
		}

	}

	public static void main(String[] args) {


	
		//Rect: id:2 type:rec8x1 area:208 w:  4.697 h: 52.162 a:  2.545 at x:543.5 y:344.0 isDebris:false [ S2_S6 ] 
		//Rect: id:3 type:rec2x1 area:72 w:  6.119 h: 12.205 a:  2.545 at x:533.0 y:343.5 isDebris:false
		Rect rec2 =new Rect(646.0, 342.0, 6.914, 50.437, 3.047, -1, 300);
		Rect rec1 = new Rect(649.0, 353.0, 6.217, 51.176, 2.953, -1, 306);

		
/*
 		Set<ABObject> set1 = new HashSet<ABObject>();
		Set<ABObject> set2 = new HashSet<ABObject>();

		set1.add(rec1);
		set1.add(rec2);

		set2.add(rec1);
		set2.add(rec2);

		List<Set<ABObject>> sets = new LinkedList<Set<ABObject>>();
		sets.add(set1);
		if(!sets.contains(set2))
			sets.add(set2);
		System.out.println(sets.size());
		System.out.println(set1.equals(set2));*/


		System.out.println(rec1.isLevel + "  " + rec2.isLevel);
		/*for (Line2D line : rec1.sectors)
		{
			System.out.println(line.getP1() + "  " + line.getP2());
		}*/
		System.out.println(GSRConstructor.computeRectToRectRelation(rec1, rec2).r);
	}

}

