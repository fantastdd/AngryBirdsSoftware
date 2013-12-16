package ab.objtracking.representation;

import java.awt.geom.Line2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import ab.objtracking.dynamic.Movement;
import ab.objtracking.representation.util.GSRConstructor;
import ab.vision.ABObject;
import ab.vision.real.shape.Rect;

public enum Relation {
	
	INVALID,INVALID1, INVALID2,
	UNKNOWN,
	UNASSIGNED,
	
	
	//Atomic Sectors
	S1(0), S2(1), S3(2), S4(3), S5(4), S6(5), S7(6), S8(7), 
    R1(8), R2(9), R3(10), R4(11), R5(12), R6(13), R7(14), R8(15),
	
	//GR relations
	S1_S3(S1,S2), S1_S4(S1,S4), S1_S5(S1,S5), S1_S6(S1,S6), S1_S7(S1,S7),
	
	S2_S5(S2,S5), S2_S6(S2,S6), S2_S7(S2,S7),

	S3_S1(S3,S1), S3_S5(S3,S5), S3_S6(S3,S6), S3_S7(S3,S7), S3_S8(S3,S8),
	
	S4_S7(S4,S7), S4_S8(S4,S8), S4_S1(S4,S1),
	
	S5_S1(S5,S1), S5_S2(S5,S2), S5_S3(S5,S3), S5_S7(S5,S7), S5_S8(S5,S8),
	
	S6_S1(S6,S1), S6_S2(S6, S2), S6_S3(S6,S3),
	
	S7_S1(S7,S1), S7_S2(S7,S2), S7_S3(S7,S3), S7_S4(S7,S4), S7_S5(S7,S5),
	
	S8_S3(S8,S3), S8_S4(S8,S4), S8_S5(S8,S5),
	
	//GR Normal(Not Rotated) Rectangle Relations
	R1_R5(R1,R5), 
	R1_S5(R1, S5), R1_S6(R1,S6), R1_S7(R1, S7),  
	S5_R1(S5, R1), S6_R1(S6,R1), S7_R1(S7, R1),
	
	R2_R6(R2,R6),
	R2_S7(R2,S7),
	S7_R2(S7,R2),
	
	R3_R7(R3,R7),
	R3_S1(R3,S1), R3_S7(R3,S7), R3_S8(R3,S8),
	S1_R3(S1,R3), S7_R3(S7,R3), S8_R3(S8,R3),
	
	R4_R8(R4,R8),
	R4_S1(R4,S1),
	S1_R4(S1,R4),
	
	R5_R1(R5,R1),
	R5_S1(R5,S1), R5_S2(R5,S2), R5_S3(R5,S3),
	S1_R5(S1,R5), S2_R5(S2,R5), S3_R5(S3,R5),
	
	R6_R2(R6,R2),
	R6_S3(R6,S3),
	S3_R6(S3,R6),
	
	R7_R3(R7,R3),
	R7_S3(R7,S3), R7_S4(R7,S4), R7_S5(R7,S5),
	S3_R7(S3,R7), S4_R7(S4,R7), S5_R7(S5,R7),
	
	R8_R4(R8,R4),
	R8_S5(R8,S5),
	S5_R8(S5,R8),
	
	//Boxes Relations
	TOP, BOTTOM, LEFT, RIGHT, TOP_LEFT(TOP, LEFT), TOP_RIGHT(TOP, RIGHT), BOTTOM_LEFT(BOTTOM, LEFT), BOTTOM_RIGHT(BOTTOM, RIGHT);
	

	private  Relation left;
	private  Relation right;
	private  int aRI; //atomicRelationIndex
	private static Relation[] aRelations = {S1, S2, S3, S4, S5, S6, S7, S8, R1, R2, R3, R4, R5, R6, R7, R8 };
	private Relation(){}
	private Relation(int index)
	{
		aRI = index;
	}
	private static SimpleGraph<Relation,DefaultEdge> graph; //neighbor graph of relations
	private static SimpleGraph<Relation, DefaultEdge> sectorsGraph;// Neighbor graph of sectors
	private static DijkstraShortestPath<Relation, DefaultEdge> shortestPath;
	public static boolean isNeighbor(Relation r1, Relation r2)
	{
		if(r1 == r2)
			return true;
		if(graph.getEdge(r1, r2) == null)
			return false;
		return true;
	}
	
	public static boolean isNeighbor(Relation r1, Relation r2, int dis)
	{
		if(r1 == r2)
			return true;
		shortestPath = new DijkstraShortestPath<Relation, DefaultEdge>(graph, r1, r2);
		
		//System.out.println(shortestPath.getPathLength());
		if(shortestPath.getPathLength() < dis)
			return true;
		return false;
	}
	/* **
	 * @param r1: GR relation
	 * @param r2: GR relation
	 * @return the distance between r1 and r2 in the neighborhood graph
	 * **/
	private static double distance(Relation r1, Relation r2)
	{
		shortestPath = new DijkstraShortestPath<Relation, DefaultEdge>(graph, r1, r2);
		/*List<DefaultWeightedEdge> edges = shortestPath.getPathEdgeList();
		for(DefaultWeightedEdge edge : edges)
		{
			System.out.println(edge);
		}*/
		return shortestPath.getPathLength();
	}
	/**
	 * @param r1: atomic GR relation
	 * @param r2: atomic GR relation
	 * @return the distance between r1 and r2 in the neighborhood graph
	 * **/
	public static double sectorDistance(Relation r1, Relation r2)
	{
		shortestPath = new DijkstraShortestPath<Relation, DefaultEdge>(sectorsGraph, r1, r2);
		return shortestPath.getPathLength();
	}
    static 
    {
    	System.out.println(" Generate Neighborhood graph ");
    	//TODO Serialize graph;
    	graph = new SimpleGraph<Relation, DefaultEdge>(DefaultEdge.class);
    	
    	for (Relation rel : Relation.values())
    	{
    		 graph.addVertex(rel);
    	}
    	
    	Map<Relation, Relation[]> vnMap = new HashMap<Relation, Relation[]>(); //vertex neighborhood map;
    	//Calculate Atomic Relation Neighborhood graph
    	Relation[] R1N = {R1, S1, S3, R2, R8}; vnMap.put(R1, R1N);
    
    	Relation[] R2N = {R2, S3, S2, S4, R1, R3}; vnMap.put(R2, R2N);
    	
    	Relation[] R3N = {R3, S3, S5, R2, R4}; vnMap.put(R3, R3N);
    	
    	Relation[] R4N = {R4, S5, S4, S6, R3, R5}; vnMap.put(R4, R4N);
    	
    	Relation[] R5N = {R5, S5, S7, R3, R6}; vnMap.put(R5, R5N);
    	
    	Relation[] R6N = {R6, S7, S6, S8, R5, R7}; vnMap.put(R6, R6N);
    	
    	Relation[] R7N = {R7, S1, S7, R6, R8}; vnMap.put(R7, R7N);
    	
    	Relation[] R8N = {R8, S1, S8, S2, R7, R1}; vnMap.put(R8, R8N);
    	
    	Relation[] S1N = {S1, R1, R7, R8, S2, S8}; vnMap.put(S1, S1N);
    	Relation[] S2N = {S2, R2, R8, S1, S3}; vnMap.put(S2, S2N);
    	Relation[] S3N = {S3, R1, R2, R3, S2, S4}; vnMap.put(S3, S3N);
    	Relation[] S4N = {S4, R2, R4, S3, S5}; vnMap.put(S4, S4N);
    	Relation[] S5N = {S5, R3, R4, R5, S4, S6}; vnMap.put(S5, S5N);
    	Relation[] S6N = {S6, R4, R6, S5, S7}; vnMap.put(S6, S6N);
    	Relation[] S7N = {S7, R5, R6, R7, S6, S8}; vnMap.put(S7, S7N);
    	Relation[] S8N = {S8, R6, R8, S7, S1}; vnMap.put(S8, S8N);
    	
    	
    	sectorsGraph = new SimpleGraph<Relation, DefaultEdge>(DefaultEdge.class);
    	for (Relation rel: aRelations)
    	{
    		sectorsGraph.addVertex(rel);
    	}
    	Set<Relation> sectors = sectorsGraph.vertexSet();
    	for(Relation rel : sectors)
    	{
    		Relation[] neighborRels = vnMap.get(rel);
    		for (Relation _rel : neighborRels)
    		{
    			if( rel != _rel)
    				sectorsGraph.addEdge(rel, _rel);
    		}
    	}
    	
    	
    	
    	
    	for (Relation relation: Relation.values())
    	{
    	
    		if(!isGRRelation(relation))
    			continue;
    	
    		{

    			Relation left = relation.left;
    			Relation right = relation.right;
    			Relation[] lns = vnMap.get(left);
    			Relation[] rns = vnMap.get(right);
    			for (Relation ln : lns)
    			{
    				boolean isSector1Level = ln.aRI > 7;
    				
    				int lnIndex = (!isSector1Level)?ln.aRI : (ln.aRI - 8); 
    				for(Relation rn: rns)
    				{
    					boolean isSector2Level = rn.aRI > 7;
    					int rnIndex = (!isSector2Level)?rn.aRI : (rn.aRI - 8);
    					
    					Relation r = Relation.getRelation(lnIndex, isSector1Level, rnIndex, isSector2Level);
    					/*
    					System.out.println(String.format("%s %s %d %b %s %d %b %s", relation, ln, lnIndex, isSector1Level,
    							rn, rnIndex, isSector2Level,r));*/

    					if(r == INVALID || relation == r)
    						continue;
    					graph.addEdge(relation, r);
    				
    					
    				}
    			}
    			
    		}
    	}
    	//Process Graph, the v-v relations connecting two edge-edge relations and can be removed.
/*    	for (Relation r : graph.vertexSet())
    	{
    		if(isGRRelation(r) && r.left.atomicRelationIndex%2 == 0 && r.right.atomicRelationIndex%2 == 0)
    		{
    			Set<DefaultWeightedEdge> edges = graph.edgesOf(r);
    			List<Relation> connectedVertices = new ArrayList<Relation>();
    			for (DefaultWeightedEdge edge : edges)
    			{
    				Relation _r = graph.getEdgeTarget(edge);
    				if( r == _r)
    					_r = graph.getEdgeSource(edge);
    				
    				connectedVertices.add(_r);
    			}
    			for (int i = 0; i < connectedVertices.size() - 1; i++)
    			{
    				Relation _r = connectedVertices.get(i);
    				for (int j = i + 1; j < connectedVertices.size(); j++)
    				{
    					if(_r == R4_S1 && connectedVertices.get(j) == S8_S4 )
    						System.out.println(" saboteur: " + r);
    					graph.addEdge(_r, connectedVertices.get(j));
    				}
    			}
    		    
    		
    		}
    	}*/
    	

    }
	private Relation(Relation left, Relation right)
	{
		this.left = left;
		this.right = right;
	}
	public static boolean isGRRelation(Relation relation)
	{
		if(relation.left == null)
			return false;
		String str = relation.left.toString();
		if(str.contains("S") || str.contains("R"))
			return true;
		return false;
	}
	/**
	 * @return true if relation is one of the angular edge-edge relations
	 * */
	public static boolean isAEE(Relation relation)
	{
		if (relation == S2_S6 || relation == S6_S2 || relation == S8_S4 || relation == S4_S8)
			return true;
		return false;
	}
	/**
	 * @return true if relation is one of the regular edge-edge relations
	 * 
	 * **/
	public static boolean isREE(Relation relation)
	{
		if (relation == R4_R8 || relation == R2_R6 || relation == R8_R4 || relation == R6_R2)
			return true;
		return false;
	}
	/**
	 * @return true if relation is one of the edge-edge relations
	 * */
	public static boolean isEE(Relation relation)
	{
		return isREE(relation) || isAEE(relation);
	}
	
	
	public static Relation getLeftpart(Relation relation)
	{
		if(relation.left == null)
			return relation;
		return relation.left;
	}
	public static Relation getRightpart(Relation relation)
	{
		if(relation.right == null)
			return relation;
		return relation.right;
	}
	public static Relation inverse(Relation relation)
	{
		switch(relation)
		{
			case S1_S3: return S3_S1;
			case S1_S4: return S4_S1;
			case S1_S5: return S5_S1;
			case S1_S6: return S6_S1;
			case S1_S7: return S7_S1;
			case S2_S5: return S5_S2;
			case S2_S6: return S6_S2;
			case S2_S7: return S7_S2;
			case S3_S1: return S1_S3;
			case S3_S5: return S5_S3;
			case S3_S6: return S6_S3;
			case S3_S7: return S7_S3;
			case S3_S8: return S8_S3;
			case S4_S7: return S7_S4;
			case S4_S8: return S8_S4;
			case S4_S1: return S1_S4;
			case S5_S1: return S1_S5;
			case S5_S2: return S2_S5;
			case S5_S3: return S3_S5;
			case S5_S7: return S7_S5;
			case S5_S8: return S8_S5;
			case S6_S1: return S1_S6;
			case S6_S2: return S2_S6;
			case S6_S3: return S3_S6;
			case S7_S1: return S1_S7;
			case S7_S2: return S2_S7;
			case S7_S3: return S3_S7;
			case S7_S4: return S4_S7;
			case S7_S5: return S5_S7;
			case S8_S3: return S3_S8;
			case S8_S4: return S4_S8;
			case S8_S5: return S5_S8;
			
			case R1_R5: return R5_R1;
			case R1_S5: return S5_R1;
			case R1_S6: return S6_R1;
			case R1_S7: return S7_R1;  
			case S5_R1: return R1_S5;
			case S6_R1: return R1_S6;
			case S7_R1: return R1_S7;
			case R2_R6: return R6_R2;
			case R2_S7: return S7_R2;
			case S7_R2: return R2_S7;
			case R3_R7: return R7_R3;
			case R3_S8: return S8_R3;
			case R3_S7: return S7_R3;
			case R3_S1: return S1_R3;
			case S1_R3: return R3_S1;
			case S7_R3: return R3_S7;
			case S8_R3: return R3_S8;
			case R4_R8: return R8_R4;
			case R4_S1: return S1_R4;
			case S1_R4: return R4_S1;
			case R5_R1: return R1_R5;
			case R5_S1: return S1_R5;
			case R5_S2: return S2_R5;
			case R5_S3: return S3_R5;
			case S1_R5: return R5_R1;
			case S2_R5: return R5_S2;
			case S3_R5: return R5_S3;
			case R6_R2: return R2_R6;
			case R6_S3: return S3_R6;
			case S3_R6: return R6_S3;
			case R7_R3: return R3_R7;
			case R7_S3: return S3_R7;
			case R7_S4: return S4_R7;
			case R7_S5: return S5_R7;
			case S3_R7: return R7_S3;
			case S4_R7: return R7_S4;
			case S5_R7:	return R7_S5;
			case R8_R4: return R4_R8;
			case R8_S5: return S5_R8;
			case S5_R8: return R8_S5;
		
			case TOP: return BOTTOM;
			case BOTTOM: return TOP;
			case LEFT: 	return RIGHT;
			case RIGHT: return LEFT;
			case TOP_LEFT: return BOTTOM_RIGHT;
			case TOP_RIGHT: return BOTTOM_LEFT;
			case BOTTOM_LEFT: return TOP_RIGHT;
			case BOTTOM_RIGHT: return TOP_LEFT;
			case UNKNOWN: return UNKNOWN;
			case INVALID: return INVALID;
			case INVALID1: return INVALID1;
			case INVALID2: return INVALID2;
			default: return UNASSIGNED;
		}
		
	}
	
	public static Relation getRelation(int sector1, boolean isSector1Level, int sector2, boolean isSector2Level )
	{
		//System.out.println(String.format("%d %b %d %b", sector1, isSector1Level, sector2, isSector2Level));
	  if(!isSector1Level)
		switch(sector1)
		{
			case 0: 
				{
				  if(!isSector2Level)
						switch(sector2)
						{
							case 2: return Relation.S1_S3;
							case 3: return Relation.S1_S4;
							case 4: return Relation.S1_S5;
							case 5: return Relation.S1_S6;
							case 6: return Relation.S1_S7;
							default: return Relation.INVALID;
						}
				  else
					  switch(sector2)
						{
					  		
							case 2: return Relation.S1_R3;
							case 3: return Relation.S1_R4;
							case 4: return Relation.S1_R5;
							default: return Relation.INVALID;
						}
				}
			case 2: 
			{
				if(!isSector2Level)
					switch(sector2)
					{
						case 0: return Relation.S3_S1;
						case 4: return Relation.S3_S5;
						case 5: return Relation.S3_S6;
						case 6: return Relation.S3_S7;
						case 7: return Relation.S3_S8;
						default: return Relation.INVALID;
					}
				else
					switch(sector2)
					{
						case 4: return Relation.S3_R5;
						case 5: return Relation.S3_R6;
						case 6: return Relation.S3_R7;
						default: return Relation.INVALID;
					}
			}
			case 4: 
			{
				if(!isSector2Level)
				switch(sector2)
				{
					case 0: return Relation.S5_S1;
					case 1: return Relation.S5_S2;
					case 2: return Relation.S5_S3;
					case 6: return Relation.S5_S7;
					case 7: return Relation.S5_S8;
					default: return Relation.INVALID;
				}
				else
					switch(sector2)
					{
						case 0: return Relation.S5_R1;
						case 6: return Relation.S5_R7;
						case 7: return Relation.S5_R8;
						default: return Relation.INVALID;
					}
			}
			case 6: 
			{
				if(!isSector2Level)
				switch(sector2)
				{
					case 0: return Relation.S7_S1;
					case 1: return Relation.S7_S2;
					case 2: return Relation.S7_S3;
					case 3: return Relation.S7_S4;
					case 4: return Relation.S7_S5;
					default: return Relation.INVALID;
				}
				else
					switch(sector2)
					{
						case 0: return Relation.S7_R1;
						case 1: return Relation.S7_R2;
						case 2: return Relation.S7_R3;
						default: return Relation.INVALID;
					}
			}
			case 1: 
			{
				if(!isSector2Level)
					switch(sector2)
					{
						case 4: return Relation.S2_S5;
						case 5: return Relation.S2_S6;
						case 6: return Relation.S2_S7;
						default: return Relation.INVALID;
					}
				else
					switch(sector2)
					{
						case 4: return Relation.S2_R5;
						default: return Relation.INVALID;
					}
			}
			case 3: 
			{
				if(!isSector2Level)
					switch(sector2)
					{
						case 6: return Relation.S4_S7;
						case 7: return Relation.S4_S8;
						case 0: return Relation.S4_S1;
						default: return Relation.INVALID;
					}
				else
					switch(sector2)
					{
						case 6: return Relation.S4_R7;
						default: return Relation.INVALID;
					}
			}
			case 5: 
			{
				if(!isSector2Level)
					switch(sector2)
					{
						case 0: return Relation.S6_S1;
						case 1: return Relation.S6_S2;
						case 2: return Relation.S6_S3;
						default:return Relation.INVALID;
					}
				else
					switch(sector2)
					{
						case 0: return Relation.S6_R1;
						default: return Relation.INVALID;
					}
			}
			case 7: 
			{
				if(!isSector2Level)
					switch(sector2)
					{
						case 2: return Relation.S8_S3;
						case 3: return Relation.S8_S4;
						case 4: return Relation.S8_S5;
						default: return Relation.INVALID;
					}
				else
					switch(sector2)
					{
						case 2: return Relation.S8_R3;
						default: return Relation.INVALID;
					}
			}
			default: return Relation.INVALID;
			
		}
	 
	else
	  {
		switch(sector1)
		{
			case 0: 
				{
				  if(!isSector2Level)
						switch(sector2)
						{
							case 4: return Relation.R1_S5;
							case 5: return Relation.R1_S6;
							case 6: return Relation.R1_S7;
							default: return Relation.INVALID;
						}
				  else
					  switch(sector2)
						{
							case 4: return Relation.R1_R5;
							default: return Relation.INVALID;
						}
				}
			case 2: 
			{
				if(!isSector2Level)
					switch(sector2)
					{
						case 0: return Relation.R3_S1;
						case 6: return Relation.R3_S7;
						case 7: return Relation.R3_S8;
						default: return Relation.INVALID;
					}
				else
					switch(sector2)
					{
						case 6: return Relation.R3_R7;
						default: return Relation.INVALID;
					}
			}
			case 4: 
			{
				if(!isSector2Level)
					switch(sector2)
					{
						case 0: return Relation.R5_S1;
						case 1: return Relation.R5_S2;
						case 2: return Relation.R5_S3;
						default: return Relation.INVALID;
					}
				else
					switch(sector2)
					{
						case 0: return Relation.R5_R1;
						default: return Relation.INVALID;
					}
			}
			case 6: 
			{
				if(!isSector2Level)
					switch(sector2)
					{
						case 2: return Relation.R7_S3;
						case 3: return Relation.R7_S4;
						case 4: return Relation.R7_S5;
						default: return Relation.INVALID;
					}
				else
					switch(sector2)
					{
						case 2: return Relation.R7_R3;
						default: return Relation.INVALID;
					}
			}
			case 1: 
			{
				if(!isSector2Level)
					switch(sector2)
					{
						case 6: return Relation.R2_S7;
						default: return Relation.INVALID;
					}
				else
					switch(sector2)
					{
						case 5: return Relation.R2_R6;
						default: return Relation.INVALID;
					}
			}
			case 3: 
			{
				if(!isSector2Level)
					switch(sector2)
					{
						case 0: return Relation.R4_S1;
						default: return Relation.INVALID;
					}
				else
					switch(sector2)
					{
						case 7: return Relation.R4_R8;
						default: return Relation.INVALID;
					}
			}
			case 5: 
			{
				if(!isSector2Level)
					switch(sector2)
					{
						case 2: return Relation.R6_S3;
						default:return Relation.INVALID;
					}
				else
					switch(sector2)
					{
						case 1: return Relation.R6_R2;
						default: return Relation.INVALID;
					}
			}
			case 7: 
			{
				if(!isSector2Level)
					switch(sector2)
					{
						case 4: return Relation.R8_S5;
						default: return Relation.INVALID;
					}
				else
					switch(sector2)
					{
						case 3: return Relation.R8_R4;
						default: return Relation.INVALID;
					}
			}
			default: return Relation.INVALID;
	
		}
		}
	}
	/**
	 * @param sector: the sector of newObj
	 * @param iniObj: the initial object of a match
	 * @param newObj: the new object of a match;
	 * @return the newObj's sector's corresponding sector of the initial obj
	 * 
	 * */
	private static Relation getCorrespondingRelation(Relation sector, ABObject iniObj, ABObject newObj)
	{
		
		if (iniObj.isLevel && newObj.isLevel)
		{
			return sector;
		} else
			if( iniObj.isLevel)
			{
				switch(newObj.lean)
				{
					case ABObject.LEAN_LEFT: 
					{
						return sector;
					}
					case ABObject.LEAN_RIGHT:
					{
						switch(sector)
						{
						case S1: return R7;
						case S2: return R8;
						case S3: return R1;
						case S4: return R2;
						case S5: return R3;
						case S6: return R4;
						case S7: return R5;
						case S8: return R6;
						default: return INVALID;
						}
					}
					default: return INVALID;
				}
			}
			else
				if (newObj.isLevel)
				{
					switch(iniObj.lean)
					{
						case ABObject.LEAN_RIGHT: 
						{
							return sector;
						}
						case ABObject.LEAN_LEFT:
						{
							switch(sector)
							{
							case R7: return S1;
							case R8: return S2;
							case R1: return S3;
							case R2: return S4;
							case R3: return S5;
							case R4: return S6;
							case R5: return S7;
							case R6: return S8;
							default: return INVALID;
							}
						}
						default: return INVALID;
					}
				}
				else
				{
					return sector;
					/*if (iniObj.lean == newObj.lean)
						return sector;
					else
					{
						if(iniObj.lean == ABObject.LEAN_LEFT)
						{
							switch(sector)
							{
								case S3: return S1;
								case S4: return S2;
								case S5: return S3;
								case S6: return S4;
								case S7: return S5;
								case S8: return S6;
								case S1: return S7;
								case S2: return S8;
								default: return INVALID;
							}
						}
						else
						{
							switch(sector)
							{
								case S1: return S3;
								case S2: return S4;
								case S3: return S5;
								case S4: return S6;
								case S5: return S7;
								case S6: return S8;
								case S7: return S1;
								case S8: return S2;
								default: return INVALID;
							}
						}
					}*/
				}
		
	}
	
	/**
	 * @param iniObj1
	 * @param iniObj2
	 * @param iniR: the relation between iniObj1 and iniObj2. 
	 * @param newObj1
	 * @param newObj2: the relation between newObj1 and newObj2
	 * @param newR: the relation between newObj1 and newObj2
	 * return true if iniR can become newR
	 * */
	public static boolean isOpposite(ABObject iniObj1, ABObject iniObj2, Relation iniR, ABObject newObj1, ABObject newObj2, Relation newR )
	{
		if ( !Relation.isGRRelation(iniR) || !Relation.isGRRelation(newR))
		{
			return iniR == Relation.inverse(newR);
		}
		Relation _newRLeft = Relation.getCorrespondingRelation(newR.left, iniObj1, newObj1);
		Relation _newRRight = Relation.getCorrespondingRelation(newR.right, iniObj2, newObj2);
		double distanceLeft = sectorDistance(_newRLeft, iniR.left);
		double distanceRight = sectorDistance(_newRRight, iniR.right);
		//double distance = Math.max(distanceLeft, distanceRight);
		double distance = distanceLeft + distanceRight;
//		/System.out.println(distance);
		if(distance > 5 && distance < 10)
			return true;
		return false;
	}
	//TODO return true if iniR can become newR by the movements
	public static boolean isOpposite(ABObject iniObj1, ABObject iniObj2, Relation iniR, ABObject newObj1, ABObject newObj2, Relation newR, Movement newObj1Movement, Movement newObj2Movement)
	{
		return false;
	}
	
	

	
	
	
	
	public static boolean isOpposite(Relation rel1, Relation rel2)
	{
		if ( !Relation.isGRRelation(rel1) || !Relation.isGRRelation(rel2))
		{
			return rel1 == Relation.inverse(rel2);
		}
		double distance = distance(rel1, rel2);
		if (distance > 2 && distance < 5) // since the neighborhood graph does not contain non-gr relatoions, distance(gr, nongr) will give a infinity distance given non  present of the non gr relations in the graph
			return true;
		
		return false;
	}
	
	public static void main(String args[])
	{ 
		Rect iniObj1 = new Rect(646.0, 342.0, 6.914, 50.437, 3.047, -1, 300);
		Rect iniObj2 = new Rect(648.0, 353.0, 6.217, 51.176, 2.953, -1, 306);
		Relation rel1 = GSRConstructor.computeRectToRectRelation(iniObj1, iniObj2).r;
		Rect newObj2 = new Rect(653.0, 358.0, 6.172, 50.792, 0.157, -1, 300);
		Rect newObj1 = new Rect(653.0, 364.9, 6.659, 48.661, 0.157, 01, 288);
		Relation rel2 = GSRConstructor.computeRectToRectRelation(newObj1, newObj2).r;
		
		/*for (Line2D line : iniObj1.sectors)
		{
			System.out.println(line.getP1() + "  " + line.getP2());
		}*/
		
		System.out.println("  " + rel1 + "  " + rel2);
		
		//System.out.println(Relation.sectorDistance(S8, r2));
		System.out.println(Relation.isOpposite(iniObj1, iniObj2, rel1, newObj1, newObj2, rel2));
		/*System.out.println(Relation.isNeighbor(Relation.R4_S1, Relation.S8_S4, 2));
		for (DefaultEdge edge : graph.edgesOf(Relation.R4_S1))
		{
			System.out.println(edge);
		}*/
		//System.out.println(Relation.sectorDistance(R5_S2, S8_S4));
	}
}
