package solver;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class DecisionState extends State {

	public enum MOVE {
		UP, DOWM, LEFT, RIGHT
	};

	private class MoveChild {
		public MOVE move;
		public RandomState state;

		public MoveChild(MOVE move, RandomState state) {
			super();
			this.move = move;
			this.state = state;
		}
	}

	private List<MoveChild> childs;

	protected DecisionState() {

	}

	private final static Multiset<DecisionState> visitedStates = HashMultiset.create();
	
	//Pooling
	private final static List<DecisionState> pool = new ArrayList<DecisionState>();
	
	public static DecisionState buildState() {
		DecisionState ret;
		if(pool.isEmpty()){
			ret = new DecisionState();
		}else{
			ret = pool.remove(0);
		}
		return ret;
	}
	
	public static void poolState(DecisionState state){
		for(MoveChild child : state.getChilds()){
			child.state.decrementRefrenceCount();
			if(state.getReferenceCount()==0){
				RandomState.poolState(child.state);
			}
		}
		state.childs=null;
		pool.add(state);
	}
	//Pooling

	public MOVE getBestMove(Heuristic heuristic){
		int depth = estimateDepth();
		double bestValue=0;
		MOVE bestMove=null;
		for(MoveChild child : getChilds()){
			double value = child.state.evaluate(depth,heuristic);
			if(value>bestValue){
				bestValue=value;
				bestMove=child.move;
			}
		}
		return bestMove;
	}
	
	public double evaluate(int depth,Heuristic heuristic){
		double ret=0;
		if (depth == 0) {
			ret = getHeuristicValue(heuristic);
		} else {
			for (MoveChild child : getChilds()) {
				ret = Math.max(ret, child.state.evaluate(depth-1, heuristic));
			}
		}
		return ret;
	}

	private int estimateDepth() {
		// TODO : use current state to determine branching factor and estimate a
		// suitable depth;
		return 3;
	}

	protected List<MoveChild> getChilds() {
		if (childs == null) {
			childs = computeChilds();
		}
		return childs;
	}

	protected List<MoveChild> computeChilds() {
		childs = new ArrayList<MoveChild>();
		// TODO for each move:
		// check if move is possible
		// compute child from move
		return childs;
	}

	// TESTED
	public State generateUpState() {
		State ret = buildState();
		for (int col = 0; col < GRID_SIZE; col++) {
			int readIndex = 0;
			int writeIndex = 0;
			int oldVal = EMPTY_VALUE;
			while (readIndex < GRID_SIZE) {
				int val = get(readIndex, col);
				if (val != EMPTY_VALUE) {
					if (val == oldVal) {
						ret.set(writeIndex, col, 2 * val);
						writeIndex++;
						oldVal = EMPTY_VALUE;
					} else {
						if (oldVal != EMPTY_VALUE) {
							writeIndex++;
						}
						ret.set(writeIndex, col, val);
						oldVal = val;
					}
				}
				readIndex++;
			}
			if (oldVal != EMPTY_VALUE) {
				writeIndex++;
			}
			// filling blanck fields;
			for (; writeIndex < GRID_SIZE; writeIndex++) {
				ret.set(writeIndex, col, EMPTY_VALUE);
			}
		}
		return ret;
	}

	// TESTED
	public State generateDownState() {
		State ret = buildState();
		for (int col = 0; col < GRID_SIZE; col++) {
			int readIndex = GRID_SIZE - 1;
			int writeIndex = GRID_SIZE - 1;
			int oldVal = EMPTY_VALUE;
			while (readIndex >= 0) {
				int val = get(readIndex, col);
				if (val != EMPTY_VALUE) {
					if (val == oldVal) {
						ret.set(writeIndex, col, 2 * val);
						writeIndex--;
						oldVal = EMPTY_VALUE;
					} else {
						if (oldVal != EMPTY_VALUE) {
							writeIndex--;
						}
						ret.set(writeIndex, col, val);
						oldVal = val;
					}
				}
				readIndex--;
			}
			if (oldVal != EMPTY_VALUE) {
				writeIndex--;
			}
			// filling blanck fields;
			for (; writeIndex >= 0; writeIndex--) {
				ret.set(writeIndex, col, EMPTY_VALUE);
			}
		}
		return ret;
	}
	
	

}
