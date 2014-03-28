package solver;

public class LearningManager {
	
	private LearningManager instance;
	
	public LearningManager getInstance() {
		return instance!=null ? instance : (instance = new LearningManager());
	}
	
	protected LearningManager(){
		
	}
	
	

}
