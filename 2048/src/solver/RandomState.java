package solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class RandomState extends State {

	private class RandomChild {
		public Double proba;
		public DecisionState state;

		public RandomChild(Double proba, DecisionState state) {
			this.proba = proba;
			this.state = state;
		}
	}

	private List<RandomChild> childs;
	
	private final static HashMap<RandomState,RandomState> visitedStates = new HashMap<RandomState,RandomState>();
	
	public static RandomState tryFindState(RandomState genState){
		RandomState ret = visitedStates.get(genState);
		if(ret!=null){
			RandomState.poolState(genState);
		}else{
			ret = genState;
			visitedStates.put(genState, genState);
		}
		ret.incrementRefrenceCount();
		return ret;		
	}
	
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
			if(child.state.getReferenceCount()==0){
				DecisionState.poolState(child.state);
			}
		}
		visitedStates.remove(state);
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
//		 TODO
//		 Get all possible spawnable number
//		 get associated proba.
		Map<Integer,Double> proba = LearningManager.getInstance().getSpawnableSquareProba();
		Set<Entry<Integer,Double>> set = proba.entrySet();
		for(int l =0;l<GRID_SIZE;l++){
			for(int c =0;c<GRID_SIZE;c++){
				if(get(l, c)==EMPTY_VALUE){
					for(Entry<Integer,Double> entry : set){
						DecisionState state = DecisionState.buildState();
						copyInto(state);
						state.set(l, c, entry.getKey());
						childs.add(new RandomChild(entry.getValue(), state));
					}
				}
			}
		}
		return childs;
	}
}
