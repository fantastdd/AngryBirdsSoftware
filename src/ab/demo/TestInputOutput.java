package ab.demo;

import ab.demo.other.ActionRobot;
import ab.utils.StateUtil;

public class TestInputOutput {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		StateUtil state = new StateUtil();
		ActionRobot ar = new ActionRobot();
		
		long time = System.currentTimeMillis();
		
		for (int i = 100; i > 0; i--)
		{
			state.getGameState(ar.proxy);
			//System.out.println("Shot Done by Client 123456 ");
		}
		
		System.out.println( (System.currentTimeMillis() - time)/100 );
		System.exit(0);
	}

}
