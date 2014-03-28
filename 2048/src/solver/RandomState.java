package solver;

import java.util.ArrayList;
import java.util.List;

public class RandomState extends State {

	private class RandomChild {
		public Integer proba;
		public DecisionState state;

		public RandomChild(Integer proba, DecisionState state) {
			this.proba = proba;
			this.state = state;
		}
	}

	private List<RandomChild> childs;
	
	//Pooling
	private final static List<RandomState> pool = new ArrayList<RandomState>();
	
	public static RandomState buildState() {
		RandomState ret;
		if(pool.isEmpty()){
			ret = new RandomState();
		}else{
			ret = pool.remove(0);
		}
		return ret;
	}
	
	public static void poolState(RandomState state){
		for(RandomChild child : state.getChilds()){
			child.state.decrementRefrenceCount();
			if(state.getReferenceCount()==0){
				DecisionState.poolState(child.state);
			}
		}
		state.childs=null;
		pool.add(state);
	}
	//Pooling

	protected RandomState() {

	}

	public double evaluate(int depth, Heuristic heuristic) {
		double ret=0;
		if (depth == 0) {
			ret = getHeuristicValue(heuristic);
		} else {
			double sum = 0;
			for (RandomChild child : getChilds()) {
				ret += child.proba*child.state.evaluate(depth-1, heuristic);
				sum += child.proba;
			}
			ret/=sum;
		}
		return ret;
	}

	protected List<RandomChild> getChilds() {
		if (childs == null) {
			childs = computeChilds();
		}
		return childs;
	}

	protected List<RandomChild> computeChilds() {
		childs = new ArrayList<RandomChild>();
		// TODO
		// Get all possible spawnable number
		// get associated proba.
		return childs;
	}
}
