package solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import solver.DecisionState.MoveChild;

public class RandomState extends State {

	private class RandomChild {
		public Double proba;
		public DecisionState state;

		public RandomChild(Double proba, DecisionState state) {
			this.proba = proba;
			this.state = state;
		}
	}

	private int lastDepth = -100;
	private double lastHeuristicValue;

	private List<RandomChild> childs;

	private final static HashMap<RandomState, RandomState> visitedStates = new HashMap<RandomState, RandomState>();

	public static RandomState tryFindState(RandomState genState) {
		RandomState ret;
		if (State.REUSE_STATES) {
			long time = System.currentTimeMillis();
			ret = visitedStates.get(genState);
			if (ret != null) {
				RandomState.poolState(genState, "RandomState.tryFindState");
				SolverAgent.reattachedStates++;
			} else {
				ret = genState;
				visitedStates.put(genState, genState);
			}
			SolverAgent.reusing_time += System.currentTimeMillis() - time;
			SolverAgent.reusing_count += 1;
		} else {
			ret = genState;
		}
		ret.incrementRefrenceCount();
		return ret;
	}

	// Pooling
	private final static List<RandomState> pool = new ArrayList<RandomState>();

	public static RandomState buildState() {
		RandomState ret;
		if (pool.isEmpty()) {
			ret = new RandomState();
			SolverAgent.instantiatedStates++;
		} else {
			ret = pool.remove(0);
			// trace.remove(0);
			SolverAgent.reusedStates++;
		}
		return ret;
	}

	public static class DebugInfo {
		State state;
		String call;

		public DebugInfo(State state, String call) {
			this.state = state;
			this.call = call;
		}
	}

	public static List<DebugInfo> trace = new ArrayList<DebugInfo>();

	public static void poolState(RandomState state, String caller) {
		if (state.getReferenceCount() <= 0) {
			if (state.childs != null) {
				for (RandomChild child : state.getChilds()) {
					child.state.decrementRefrenceCount();
					DecisionState.poolState(child.state, caller);
				}
				state.childs = null;
			}
			// DEBUG
			// for (DebugInfo info : trace) {
			// if (info.state == state) {
			// System.out.println(info.call);
			// throw new NullPointerException();
			// }
			// }
			// trace.add(new DebugInfo(state, caller));
			// DEBUG
			state.reset();
			visitedStates.remove(state);
			pool.add(state);
			SolverAgent.returnedToPool++;
		}
	}

	// Pooling

	public void checkChildNotPooled(int depth) {
		for (RandomState state : pool) {
			if (state == this) {
				System.out.println(depth);
				throw new NullPointerException();
			}
		}
		if (childs != null) {
			for (RandomChild child : childs) {
				child.state.checkChildNotPooled(depth - 1);
			}
		}
	}

	public DecisionState findMatchingChildren(DecisionState state) {
		for (RandomChild child : getChilds()) {
			if (state.equals(child.state)) {
				return child.state;
			}
		}
		return null;
	}

	protected RandomState() {

	}

	public int getChildCount() {
		int ret = 0;
		if (childs != null) {
			ret = childs.size();
		}
		return ret;
	}

	public int getRecChildCount() {
		int count = 1;
		if (childs != null) {
			for (RandomChild child : childs) {
				count += child.state.getRecChildCount();
			}
		}
		return count;
	}

	public double evaluate(int depth, Heuristic heuristic) {
		double ret = 0;
		if (lastDepth == depth) {
			SolverAgent.calculationaved += getRecChildCount();
			return lastHeuristicValue;
		} else {
			if (depth <= 0) {
				ret = getHeuristicValue(heuristic);
			} else {
				double sum = 0;
				for (RandomChild child : getChilds()) {
					ret += child.proba
							* child.state.evaluate(depth - 1, heuristic);
					sum += child.proba;
				}
				ret /= sum;
			}
			lastHeuristicValue = ret;
			lastDepth = depth;
		}
		return ret;
	}

	public double evaluate2(int depth, Heuristic heuristic) {
		double ret = 0;
		if (depth <= 0) {
			ret = getHeuristicValue(heuristic);
		} else {
			double min = -100;
			for (RandomChild child : getChilds()) {
				min = Math.min(min, child.state.evaluate(depth - 1, heuristic));
			}
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
		lastDepth = -100;
		Map<Integer, Double> proba = SolverAgent.getInstance()
				.getSpawnableSquareProba();
		Set<Entry<Integer, Double>> set = proba.entrySet();
		for (int l = 0; l < GRID_SIZE; l++) {
			for (int c = 0; c < GRID_SIZE; c++) {
				if (get(l, c) == EMPTY_VALUE) {
					for (Entry<Integer, Double> entry : set) {
						DecisionState state = DecisionState.buildState();
						copyInto(state);
						state.set(l, c, entry.getKey());
						state = DecisionState.tryFindState(state);
						// state.incrementRefrenceCount();
						try {
							childs.add(new RandomChild(entry.getValue(), state));
						} catch (NullPointerException e) {
							System.out.println("entry : " + entry.getValue());
							childs.add(new RandomChild(entry.getValue(), state));
						}
					}
				}
			}
		}
		return childs;
	}

	public Map<State, Integer> filReferenceMap(Map<State, Integer> map) {
		Integer refCount = map.get(this);
		if (refCount != null) {
			map.remove(this);
		} else {
			refCount = new Integer(0);
		}
		refCount++;
		map.put(this, refCount);
		if (childs != null) {
			for (RandomChild child : childs) {
				map = child.state.filReferenceMap(map);
			}
		}
		return map;
	}
}
