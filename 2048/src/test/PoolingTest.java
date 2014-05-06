package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import solver.DecisionState;
import solver.DecisionState.MoveChild;
import solver.RandomState;
import solver.SolverAgent;
import solver.State;
import solver.State.Heuristic;

public class PoolingTest {

	private final static Heuristic heuristic = new Heuristic() {

		@Override
		public double getValue(State state) {
			double value = 0;
			for (int i = 0; i < State.GRID_SIZE; i++) {
				for (int j = 0; j < State.GRID_SIZE; j++) {
					int val = state.get(i, j);
					value += val * val;

					int x = i + 1;
					int y = j;
					if (x < State.GRID_SIZE && val != 0 && state.get(x, y) != 0) {
						value -= Math.abs(state.get(x, y) - val);
					}

					x = i - 1;
					y = j;
					if (x > 0 && val != 0 && state.get(x, y) != 0) {
						value -= Math.abs(state.get(x, y) - val);
					}

					x = i;
					y = j - 1;
					if (y > 0 && val != 0 && state.get(x, y) != 0) {
						value -= Math.abs(state.get(x, y) - val);
					}

					x = i;
					y = j + 1;
					if (y < State.GRID_SIZE && val != 0 && state.get(x, y) != 0) {
						value -= Math.abs(state.get(x, y) - val);
					}
				}
			}
			return value;
		}
	};

	@Test
	public void test() {

		RandomState expectedState = RandomState.buildState();
		SolverAgent.instantiatedStates = 0;
		SolverAgent.returnedToPool = 0;
		SolverAgent.reusedStates = 0;
		DecisionState state = DecisionState.buildState();
		DecisionState receivedState = DecisionState.buildState();
		for (int i = 0; i < State.GRID_SIZE; i++) {
			for (int j = 0; j < State.GRID_SIZE; j++) {
				state.set(i, j, State.EMPTY_VALUE);
			}
		}
		state.set(0, 0, 4);
		state.set(0, 2, 4);
		state.set(1, 1, 2);
		state.set(2, 0, 2);
		// state.print();

		SolverAgent.reattachedStates = 0;
		MoveChild res = state.getBestMove(heuristic,3);
		// res.state.print();
		// System.out.println(SolverAgent.instantiatedStates);
		// System.out.println(res.state.getRecChildCount());
		// System.out.println(state.getRecChildCount());
		// System.out.println(SolverAgent.reusedStates);
		for (int i = 0; i < State.GRID_SIZE; i++) {
			for (int j = 0; j < State.GRID_SIZE; j++) {
				expectedState.set(i, j, State.EMPTY_VALUE);
			}
		}
		expectedState.set(0, 0, 8);
		expectedState.set(1, 0, 2);
		expectedState.set(2, 0, 2);

		assertEquals(expectedState, res.state);

		expectedState.copyInto(receivedState);
		receivedState.set(3, 3, 2);
		expectedState.print();
		receivedState.print();

		SolverAgent.returnedToPool = 0;
		System.out.println("Instantiated       : "
				+ SolverAgent.instantiatedStates);
		System.out.println("expectedStateCount : "
				+ res.state.getRecChildCount());
		System.out.println("Master Count       : " + state.getRecChildCount());
		System.out
				.println("Pooled             : " + SolverAgent.returnedToPool);
		System.out.println("Reattached         : "
				+ SolverAgent.reattachedStates);
		System.out.println(SolverAgent.reusedStates);

		DecisionState child = res.state.findMatchingChildren(receivedState);
		assertNotSame(child, null);
		child.incrementRefrenceCount();
		DecisionState.poolState(receivedState, "receivedState");
		// state.decrementRefrenceCount();
		
		
		//----------------------Checking Reference count integrity-------------------------------------
		Map<State, Integer> refCount = new HashMap<>();
		state.filReferenceMap(refCount);
		//adding on reference count for child state because we increased it's ref count artificialy.
		Integer count = refCount.get(child);
		count++;
		refCount.remove(child);
		refCount.put(child, count);
		//since the root state has a refcount of 0 we remove it from the check.
		refCount.remove(state);		
		
//		int i = 0;
		for (Entry<State, Integer> entry : refCount.entrySet()) {
//			System.out.println(""+(i++)+" "+entry.getKey().getReferenceCount()+" "+entry.getValue()
//					.intValue());
			assertEquals(entry.getKey().getReferenceCount(), entry.getValue()
					.intValue());
		}
		
		//--------------------------------Pooling state-------------------------------------------------
		DecisionState.poolState(state, "state");

		System.out.println("----Pooling------");
		System.out.println("Instantiated       : "
				+ SolverAgent.instantiatedStates);
		System.out.println("Master Count       : " + child.getRecChildCount());
		System.out
				.println("Pooled             : " + SolverAgent.returnedToPool);
		assertEquals(SolverAgent.instantiatedStates, child.getRecChildCount()
				+ SolverAgent.returnedToPool);

	}

}
