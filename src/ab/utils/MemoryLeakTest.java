package ab.utils;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;

public class MemoryLeakTest {

	  private static final int NEIGHBOURS[][] = {
	        {0, -1, 0, 1, 0, 0},  // horizontal neighbours
	        {1, -1, -1, 1, 0, 0}, // 45 degress
	        {-1, 0, 1, 0, 0, 0},  // vertical
	        {-1, -1, 1, 1, 0, 0}  // 135 degrees
	    };
	 private int distance(int x1, int y1, int x2, int y2)
	 {
		 return 0;
	 }
	/**
	 * @param args
	 */
	public boolean[][] memo()
	{
		int _height = 480;
		int _width = 840;
		  int G[][][] = new int[_height][_width][4];
	        int G1[][][] = new int[_height][_width][4];
	        int G2[][][] = new int[_height][_width][4];
	        boolean isEdge[][][] = new boolean[_height][_width][4];
	       
	        // calculate individual edge strength in each direction
	        for (int y = _height - 2; y > 0; y--)
		        for (int x = 1; x < _width - 1; x++)
		        {
		            for (int o = 0; o < 4; o++)
		            {
		                int x2 = x + NEIGHBOURS[o][0];
		                int y2 = y + NEIGHBOURS[o][1];
		                int x3 = x + NEIGHBOURS[o][2];
		                int y3 = y + NEIGHBOURS[o][3];
		                
		                G[y][x][o] = distance(x, y, x2, y2) + distance(x, y, x3, y3);
		            }
		            G[y][x][0] *= 1.5;
		            G[y][x][2] *= 1.5;
		        }
	        
	        // cross-correlate with neighbouring points
	        for (int y = _height - 3; y > 1; y--)
	        for (int x = 2; x < _width - 2; x++)
	        {
	            for (int o = 0; o < 4; o++)
	            {    
	                int o2 = (o + 2) % 4;
	                int x2 = x + NEIGHBOURS[o2][0];
	                int y2 = y + NEIGHBOURS[o2][1];
	                int x3 = x + NEIGHBOURS[o2][2];
	                int y3 = y + NEIGHBOURS[o2][3];
	                
	                G1[y][x][o] = (G[y][x][o] + G[y2][x2][o] + G[y3][x3][o]) / 3;
	            }
	        } 
	        
	        // apply non-maximum suppression for each direction
	        for (int y = _height-3; y > 1; y--)
	        	for (int x = 2; x < _width-2; x++)
	        	{
		            for (int o = 0; o < 4; o++)
		            {
		                G2[y][x][o] = G1[y][x][o];
		                   
		                int x1 = x + NEIGHBOURS[o][0];
		                int y1 = y + NEIGHBOURS[o][1];
		                int x2 = x + NEIGHBOURS[o][2];
		                int y2 = y + NEIGHBOURS[o][3];
		                
		                if (G1[y][x][o] <= G1[y1][x1][o] || G1[y][x][o] < G1[y2][x2][o])
		                    G2[y][x][o] = 0;
		            }
	        }
	        
	        // Trace edge using two thresholds       
	        for (int y = _height - 3; y > 1; y--)
	        for (int x = 2; x < _width - 2; x++)
	        {
	            // add pixel if gradient is greater than threshold1
	            for (int o = 0; o < 4; o++)                   
	            if (G2[y][x][o] > 300 && !isEdge[y][x][o])
	            {
	                isEdge[y][x][o] = true;
	                
	                // perform BFS for edge pixels
	                Queue<Point> q = new LinkedList<Point>();
	                q.add(new Point(x, y));
	                
	                while (!q.isEmpty())
	                {
	                    Point p = q.poll();
	                    
	                    for (int i = -1; i < 2; i++)
	                    for (int j = -1; j < 2; j++)
	                    {
	                        if (i == 0 && j == 0)
	                            continue;
	                                
	                        int ny = p.y + i;
	                        int nx = p.x + j;
	                        
	                        // if the gradient is greater than threshold2
	                        if (G2[ny][nx][o] > 125 && !isEdge[ny][nx][o])
	                        {
	                            isEdge[ny][nx][o] = true;
	                            q.add(new Point(nx, ny));
	                        }
	                    }
	                }
	            }
	        }
	        
	        // combine edge in all four directions
	        boolean ret[][] = new boolean[_height][_width];
	        for (int y = _height - 3; y > 1; y--)
	        for (int x = 2; x < _width - 2; x++)
	        {
	            if (isEdge[y][x][0] || isEdge[y][x][1] ||
	                isEdge[y][x][2] || isEdge[y][x][3])
	                ret[y][x] = true;
	        }
	        return ret;
	                
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MemoryLeakTest memo = new MemoryLeakTest();
		while(true)
		{	
			System.out.println(" memo test ");
			memo.memo();
		}
	}

}
