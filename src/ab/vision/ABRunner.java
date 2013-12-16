package ab.vision;

import javax.swing.JOptionPane;

import ab.planner.Strategy;
import example.ExampleAgent;
import example.StarterControlPanel;

public class ABRunner {
	public static void runAgent(Class<? extends Strategy> strategy) {
		(new ExampleAgent(getStrategy(strategy.getName()))).run();
	}
	public static void runControlPanel(Class<? extends Strategy> strategy) {
		new StarterControlPanel(getStrategy(strategy.getName()));
	}

	//New a strategy by the class name
	public static Strategy getStrategy(String strategyFullName)
	{
		Strategy strategy = null;
		try {
			strategy = (Strategy) Class.forName(strategyFullName).newInstance();
		} catch (InstantiationException e1) {
		
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			JOptionPane.showMessageDialog(null, "Can not find the strategy: " + strategyFullName);
		}
		return strategy;
	}
}
