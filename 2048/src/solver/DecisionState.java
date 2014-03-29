package solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class DecisionState extends State {

	public enum MOVE {
		UP, DOWN, LEFT, RIGHT
	};

	public class MoveChild {
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

	private static Random rand = new Random();

	private final static HashMap<DecisionState, DecisionState> visitedStates = new HashMap<DecisionState, DecisionState>();

	public static DecisionState tryFindState(DecisionState genState) {
		DecisionState ret = visitedStates.get(genState);
		if (ret != null) {
			DecisionState.poolState(genState);
		} else {
			visitedStates.put(genState, genState);
		}
		ret.incrementRefrenceCount();
		return ret;
	}

	// Pooling
	private final static List<DecisionState> pool = new ArrayList<DecisionState>();

	public static DecisionState buildState() {
		DecisionState ret;
		if (pool.isEmpty()) {
			ret = new DecisionState();
			SolverAgent.instantiatedStates++;
		} else {
			ret = pool.remove(0);
			SolverAgent.reusedStates++;
		}
		return ret;
	}

	public static void poolState(DecisionState state) {
		for (MoveChild child : state.getChilds()) {
			child.state.decrementRefrenceCount();
			if (child.state.getReferenceCount() == 0) {
				RandomState.poolState(child.state);
			}
		}
		visitedStates.remove(state);
		state.childs = null;
		pool.add(state);
		SolverAgent.returnedToPool++;
	}

	// Pooling

	public MoveChild getBestMove(Heuristic heuristic) {
		int depth = estimateDepth();
		double bestValue = 0;
		List<MoveChild> bestMove = new ArrayList<MoveChild>();
		for (MoveChild child : getChilds()) {
			double value = child.state.evaluate(depth - 1, heuristic);
			if (value > bestValue) {
				bestValue = value;
				bestMove.clear();
				bestMove.add(child);
			} else if (value == bestValue) {
				bestMove.add(child);
			}
		}
		MoveChild move = null;
		if (!bestMove.isEmpty()) {
			move = bestMove.get(rand.nextInt(bestMove.size()));
		}
		return move;
	}

	public double evaluate(int depth, Heuristic heuristic) {
		double ret = 0;
		if (depth == 0) {
			ret = getHeuristicValue(heuristic);
		} else {
			for (MoveChild child : getChilds()) {
				ret = Math.max(ret, child.state.evaluate(depth - 1, heuristic));
			}
		}
		return ret;
	}

	private int estimateDepth() {
		int avg = 0;
		for (int col = 0; col < GRID_SIZE; col++) {
			for (int line = 0; line < GRID_SIZE; line++) {
				avg += get(line, col);
			}
		}
		avg /= GRID_SIZE * GRID_SIZE;

		System.out.println("avg : " + avg);
		if (avg > 180) {
			return 7;
		} else if (avg> 75) {
			return 6;
		} else {
			return 5;
		}
		// // if(avg>)
		// return 5;
	}

	protected List<MoveChild> getChilds() {
		if (childs == null) {
			childs = computeChilds();
		}
		return childs;
	}

	protected List<MoveChild> computeChilds() {
		childs = new ArrayList<MoveChild>();

		// UP
		if (isUpEnable()) {
			RandomState state = generateUpState();
			state = RandomState.tryFindState(state);
			childs.add(new MoveChild(MOVE.UP, state));
		}

		// RIGHT
		if (isRightEnable()) {
			RandomState state = generateRightState();
			state = RandomState.tryFindState(state);
			childs.add(new MoveChild(MOVE.RIGHT, state));
		}

		// DOWN
		if (isDownEnable()) {
			RandomState state = generateDownState();
			state = RandomState.tryFindState(state);
			childs.add(new MoveChild(MOVE.DOWN, state));
		}

		// LEFT
		if (isLeftEnable()) {
			RandomState state = generateLeftState();
			state = RandomState.tryFindState(state);
			childs.add(new MoveChild(MOVE.LEFT, state));
		}

		return childs;
	}

	// TESTED
	public boolean isUpEnable() {
		for (int col = 0; col < GRID_SIZE; col++) {
			int lastVal = -1;
			for (int line = 0; line < GRID_SIZE; line++) {
				int val = get(line, col);
				if (lastVal != -1) {
					if (lastVal == EMPTY_VALUE) {
						if (val != EMPTY_VALUE) {
							return true;
						}
					} else {
						if (val == lastVal) {
							return true;
						}
					}
				}
				lastVal = val;
			}
		}
		return false;
	}

	// TESTED
	public RandomState generateUpState() {
		RandomState ret = RandomState.buildState();
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
	public boolean isDownEnable() {
		for (int col = 0; col < GRID_SIZE; col++) {
			int lastVal = -1;
			for (int line = GRID_SIZE - 1; line >= 0; line--) {
				int val = get(line, col);
				if (lastVal != -1) {
					if (lastVal == EMPTY_VALUE) {
						if (val != EMPTY_VALUE) {
							return true;
						}
					} else {
						if (val == lastVal) {
							return true;
						}
					}
				}
				lastVal = val;
			}
		}
		return false;
	}

	// TESTED
	public RandomState generateDownState() {
		RandomState ret = RandomState.buildState();
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

	// TESTED
	public boolean isLeftEnable() {
		for (int line = 0; line < GRID_SIZE; line++) {
			int lastVal = -1;
			for (int col = 0; col < GRID_SIZE; col++) {
				int val = get(line, col);
				if (lastVal != -1) {
					if (lastVal == EMPTY_VALUE) {
						if (val != EMPTY_VALUE) {
							return true;
						}
					} else {
						if (val == lastVal) {
							return true;
						}
					}
				}
				lastVal = val;
			}
		}
		return false;
	}

	// TEST
	public RandomState generateLeftState() {
		RandomState ret = RandomState.buildState();
		for (int line = 0; line < GRID_SIZE; line++) {
			int readIndex = 0;
			int writeIndex = 0;
			int oldVal = EMPTY_VALUE;
			while (readIndex < GRID_SIZE) {
				int val = get(line, readIndex);
				if (val != EMPTY_VALUE) {
					if (val == oldVal) {
						ret.set(line, writeIndex, 2 * val);
						writeIndex++;
						oldVal = EMPTY_VALUE;
					} else {
						if (oldVal != EMPTY_VALUE) {
							writeIndex++;
						}
						ret.set(line, writeIndex, val);
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
				ret.set(line, writeIndex, EMPTY_VALUE);
			}
		}
		return ret;
	}

	// TESTED
	public boolean isRightEnable() {
		for (int line = 0; line < GRID_SIZE; line++) {
			int lastVal = -1;
			for (int col = GRID_SIZE - 1; col >= 0; col--) {
				int val = get(line, col);
				if (lastVal != -1) {
					if (lastVal == EMPTY_VALUE) {
						if (val != EMPTY_VALUE) {
							return true;
						}
					} else {
						if (val == lastVal) {
							return true;
						}
					}
				}
				lastVal = val;
			}
		}
		return false;
	}

	// TEST
	public RandomState generateRightState() {
		RandomState ret = RandomState.buildState();
		for (int line = 0; line < GRID_SIZE; line++) {
			int readIndex = GRID_SIZE - 1;
			int writeIndex = GRID_SIZE - 1;
			int oldVal = EMPTY_VALUE;
			while (readIndex >= 0) {
				int val = get(line, readIndex);
				if (val != EMPTY_VALUE) {
					if (val == oldVal) {
						ret.set(line, writeIndex, 2 * val);
						writeIndex--;
						oldVal = EMPTY_VALUE;
					} else {
						if (oldVal != EMPTY_VALUE) {
							writeIndex--;
						}
						ret.set(line, writeIndex, val);
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
				ret.set(line, writeIndex, EMPTY_VALUE);
			}
		}
		return ret;
	}

}
