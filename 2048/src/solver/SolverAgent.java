package solver;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solver.DecisionState.MOVE;
import solver.DecisionState.MoveChild;
import solver.State.Heuristic;

public class SolverAgent {
	public final static Color BORDER_COLOR = new Color(193, 177, 155);
	public final static Color COLOR_ZERO = new Color(204, 192, 179);
	public final static Color COLOR_TWO = new Color(238, 228, 218);
	public final static Color COLOR_FOUR = new Color(237, 224, 200);
	public final static Color COLOR_HEIGHT = new Color(242, 177, 121);
	public final static Color COLOR_SIXTEEN = new Color(245, 149, 99);
	public final static Color COLOR_THIRTY_TWO = new Color(246, 124, 95);
	public final static Color COLOR_SIXTY_FOUR = new Color(246, 94, 59);
	public final static Color COLOR_128 = new Color(237, 207, 114);
	public final static Color COLOR_256 = new Color(237, 204, 97);
	public final static Color COLOR_512 = new Color(237, 200, 80);
	public final static Color COLOR_1024 = new Color(237, 197, 63);
	public final static Color COLOR_2048 = new Color(237, 194, 46);

	public final static int WAIT_IDL = 300;
	public final static int WAIT_ERROR = 0;
	public final static int WAIT_MOVE = 350;

	public final static int UNDEFINED_VALUE = -100;

	private static Map<Integer, Integer> colorToValue = new HashMap<Integer, Integer>();

	private float[] borderRatio = new float[2];

	private static SolverAgent instance;

	private final Map<Integer, Double> spawnProba = new HashMap<Integer, Double>();
	private IOManager mIOManager;
	private GameInfo info;

	private DecisionState oldState;
	private DecisionState currentState;
	private RandomState expectedState;
	private MOVE lastMove;

	private List<Integer> knownPattern = new ArrayList<>();

	// private HashMap<Integer, ColorAgregator> knownPattern = new
	// HashMap<Integer, ColorAgregator>();

	// public class ColorAgregator {
	// private HashMap<Integer, Integer> counts = new HashMap<Integer,
	// Integer>();
	// private int bestCount = 0;
	// private int bestColor = 0;
	//
	// public int get() {
	// return bestColor;
	// }
	//
	// public void add(int color) {
	// Integer count = counts.get(color);
	// if(count==null){
	// count=new Integer(0);
	// }
	// count++;
	// counts.put(color, count);
	// if (count > bestCount) {
	// bestCount = count;
	// bestColor = color;
	// }
	//
	// }
	// }

	public static SolverAgent getInstance() {
		return instance != null ? instance : (instance = new SolverAgent());
	}

	protected SolverAgent() {
		spawnProba.put(new Integer(2), new Double(0.8));
		spawnProba.put(new Integer(4), new Double(0.2));
		borderRatio[0] = (float) SolverAgent.BORDER_COLOR.getGreen()
				/ (float) SolverAgent.BORDER_COLOR.getRed();
		borderRatio[1] = (float) SolverAgent.BORDER_COLOR.getBlue()
				/ (float) SolverAgent.BORDER_COLOR.getRed();

		// colorToValue.put(BORDER_COLOR.getRGB(), -1);
		colorToValue.put(COLOR_ZERO.getRGB(), 0);
		colorToValue.put(COLOR_TWO.getRGB(), 2);
		colorToValue.put(COLOR_FOUR.getRGB(), 4);
		colorToValue.put(COLOR_HEIGHT.getRGB(), 8);
		colorToValue.put(COLOR_SIXTEEN.getRGB(), 16);
		colorToValue.put(COLOR_THIRTY_TWO.getRGB(), 32);
		colorToValue.put(COLOR_SIXTY_FOUR.getRGB(), 64);
		colorToValue.put(COLOR_128.getRGB(), 128);
		colorToValue.put(COLOR_256.getRGB(), 256);
		colorToValue.put(COLOR_512.getRGB(), 512);
		colorToValue.put(COLOR_1024.getRGB(), 1024);
		colorToValue.put(COLOR_2048.getRGB(), 2048);

		mIOManager = new IOManager();
	}

	public Map<Integer, Double> getSpawnableSquareProba() {
		return spawnProba;
	}

	public boolean isBorder(int rgb) {
		int range = 10;
		Color c = new Color(rgb);
		// float[] ratio = new float[2];
		// ratio[0] = (float) c.getGreen() / (float) c.getRed();
		// ratio[1] = (float) c.getBlue() / (float) c.getRed();
		// float error = Math.abs(ratio[0] - borderRatio[0])
		// + Math.abs(ratio[1] - borderRatio[1]);
		// // MAX_ERROR = Math.max(MAX_ERROR, error);
		// // System.out.println("MAX_ERROR : "+MAX_ERROR);
		boolean ret = Math.abs(c.getRed() - BORDER_COLOR.getRed()) < range
				&& Math.abs(c.getBlue() - BORDER_COLOR.getBlue()) < range
				&& Math.abs(c.getGreen() - BORDER_COLOR.getGreen()) < range;
		// System.out.println(error+" "+c+" "+ret);
		return ret;
		// return rgb==BORDER_COLOR.getRGB();
	}

	public static int instantiatedStates;
	public static int reusedStates;
	public static int returnedToPool;

	public void execute() {
		int wait = WAIT_IDL;
		while (true) {
			info = mIOManager.getGameInfo(info);
			if (info != null) {
				currentState = convertColorToValue(info.colorState);
				if (isValidState(currentState)
						|| (expectedState != null && tryLearnColor(
								currentState, info.colorState))) {

					instantiatedStates = 0;
					reusedStates = 0;
					returnedToPool = 0;
					if (!currentState.equals(oldState)) {
						if (expectedState != null) {
							DecisionState child = expectedState
									.findMatchingChildren(currentState);
							if (child != null) {
								DecisionState.poolState(currentState);
								oldState.decrementRefrenceCount();
								DecisionState.poolState(oldState);
								currentState = child;
							}
						}
						MoveChild res = currentState.getBestMove(heuristic);
						if (res != null) {

							expectedState = res.state;
							oldState = currentState;
							lastMove = res.move;
							// TODO handle pooling
							// TODO learn '4' proba
							// TODO learn execution time
							// TODO add map to handle error in learning.
							wait = WAIT_MOVE;
							System.out.println("action : " + res.move);
							System.out.println("" + instantiatedStates + " "
									+ reusedStates + " " + returnedToPool);
							mIOManager.executeAction(res.move);
						}
					} else {
						mIOManager.executeAction(lastMove);
						wait = WAIT_ERROR;
					}
				} else {
					System.err.println("invalid");
					currentState.print();
					wait = WAIT_ERROR;
				}
			} else {
				wait = WAIT_IDL;
			}
			if (wait != 0) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean tryLearnColor(State state, State colorState) {
		int valueToLearn = 0;
		int colorToLearn = 0;
		boolean canLearn = false;
		int difCount = 0;
		for (int l = 0; l < State.GRID_SIZE; l++) {
			for (int c = 0; c < State.GRID_SIZE; c++) {
				if (expectedState.get(l, c) != state.get(l, c)) {
					if (!knownPattern.contains(expectedState.get(l, c))
							&& state.get(l, c) == UNDEFINED_VALUE
							&& expectedState.get(l, c) != State.EMPTY_VALUE) {
						canLearn = true;
						valueToLearn = expectedState.get(l, c);
						colorToLearn = colorState.get(l, c);
					} else if (difCount > 0) {
						return false;
					} else {
						System.out.println("error at " + l + " , " + c);
						// state.print();
						difCount++;
					}
				}
			}
		}
		if (canLearn) {
			System.out.println("LEARNING ==> " + new Color(colorToLearn)
					+ " <=> " + valueToLearn);
			knownPattern.add(valueToLearn);
			colorToValue.put(colorToLearn, valueToLearn);
			return true;
		}
		return false;
	}

	public boolean isValidState(State state) {
		for (int l = 0; l < State.GRID_SIZE; l++) {
			for (int c = 0; c < State.GRID_SIZE; c++) {
				if (state.get(l, c) == UNDEFINED_VALUE) {
					return false;
				}
			}
		}
		return true;
	}

	public DecisionState convertColorToValue(State state) {
		DecisionState ret = DecisionState.buildState();
		for (int l = 0; l < State.GRID_SIZE; l++) {
			for (int c = 0; c < State.GRID_SIZE; c++) {
				int color = state.get(l, c);
				int value = UNDEFINED_VALUE;
				if (colorToValue.containsKey(color)) {
					value = colorToValue.get(color);
				}
				ret.set(l, c, value);
			}
		}
		return ret;
	}

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
}
