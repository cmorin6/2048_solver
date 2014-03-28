package solver;

import java.util.HashMap;
import java.util.Map;

public class LearningManager {
	
	private static LearningManager instance;

	private final Map<Integer,Double> spawnProba = new HashMap<Integer,Double>();
	
	
	public static LearningManager getInstance() {
		return instance!=null ? instance : (instance = new LearningManager());
	}
	
	protected LearningManager(){
		spawnProba.put(new Integer(2),new Double(0.8));
		spawnProba.put(new Integer(4),new Double(0.2));
	}
	
	
	public Map<Integer,Double> getSpawnableSquareProba(){
		return spawnProba;
	}
	
	

}
