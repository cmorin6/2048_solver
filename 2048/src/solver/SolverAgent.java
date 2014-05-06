package solver;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import solver.DecisionState.MOVE;
import solver.DecisionState.MoveChild;
import solver.State.Heuristic;

import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

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
	public final static int WAIT_MOVE = 400;

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
		spawnProba.put(new Integer(2), new Double(0.9));
		spawnProba.put(new Integer(4), new Double(0.1));
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
	public static int reattachedStates;
	public static int reusing_time;
	public static int reusing_count;
	public static int calculationaved;

	public void execute() {
		instantiatedStates = 0;
		reusedStates = 0;
		returnedToPool = 0;
		double count = 0;
		double avg = 0;
		long time;
		int wait = WAIT_IDL;
		while (true) {
			info = mIOManager.getGameInfo(info);
			if (info != null) {
				currentState = convertColorToValue(info.colorState);
				if (isValidState(currentState)
						|| (expectedState != null && tryLearnColor(
								currentState, info.colorState))) {

					if (!currentState.equals(oldState)) {
						if (expectedState != null) {
							// double proba =
							// learnProba(expectedState,currentState);
							// System.out.println("PROBA : "+proba);
							DecisionState child = expectedState
									.findMatchingChildren(currentState);
							if (child != null) {
								DecisionState.poolState(currentState,
										"execute | currentState | 155");
								// oldState.decrementRefrenceCount();
								currentState = child;
								currentState.incrementRefrenceCount();

								// ------------- ref check
								// Map<State, Integer> refCount = new
								// HashMap<>();
								// oldState.filReferenceMap(refCount);
								// //adding on reference count for child state
								// because we increased it's ref count
								// artificialy.
								// Integer childCount = refCount.get(child);
								// childCount++;
								// refCount.remove(child);
								// refCount.put(child, childCount);
								// //since the root state has a refcount of 0 we
								// remove it from the check.
								// refCount.remove(oldState);
								// System.out.println(">" + instantiatedStates +
								// " "
								// + reusedStates + " " +
								// returnedToPool+" "+reattachedStates);
								// // int i = 0;
								// for (Entry<State, Integer> entry :
								// refCount.entrySet()) {
								// //
								// System.out.println(""+(i++)+" "+entry.getKey().getReferenceCount()+" "+entry.getValue()
								// // .intValue());
								// System.out.println(entry.getKey().equals(oldState)+" "+entry.getKey().equals(currentState)+" "+entry.getKey().equals(expectedState));
								// assertEquals(entry.getKey().getReferenceCount(),
								// entry.getValue()
								// .intValue());
								// }
								// ------------- ref check

								// System.out.println("ref count  : "
								// + currentState.getReferenceCount());
								// time = System.currentTimeMillis();
								DecisionState.poolState(oldState,
										"execute | oldState | 159");
								// System.out.println("pooling parent : "+(System.currentTimeMillis()-time)+" ms");
								// time = System.currentTimeMillis();
								currentState.checkChildNotPooled(0);
								// System.out.println("checkChildNotPooled : "+(System.currentTimeMillis()-time)+" ms");
							}
						}

						heuristicSafeLog.getValue(currentState);

						// System.out.println("" + instantiatedStates + " "
						// + reusedStates + " " + returnedToPool);
						time = System.currentTimeMillis();
						reusing_time = 0;
						reusing_count = 0;
						calculationaved = 0;
						MoveChild res = currentState.getBestMove(heuristicSafe);
						avg = ((avg * count) + (System.currentTimeMillis() - time))
								/ (++count);
						// System.out.println("getBestMove : " + avg + " ms | "
						// + count);
						// System.out.println("reusing time : " + reusing_time
						// + " " + reusing_count);
						// if (res != null) {
						// System.out.println("calculation saved  : "
						// + calculationaved + "/"
						// + res.state.getRecChildCount());
						// }
						// System.out.println(">" + instantiatedStates + " "
						// + reusedStates + " " + returnedToPool + " "
						// + reattachedStates);
						instantiatedStates = 0;
						reusedStates = 0;
						returnedToPool = 0;
						reattachedStates = 0;
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
							mIOManager.executeAction(res.move);
						}
					} else {
						DecisionState.poolState((DecisionState) currentState,
								"execute | currentState | 186");
						mIOManager.executeAction(lastMove);
						wait = WAIT_ERROR;
					}
				} else {
					DecisionState.poolState((DecisionState) currentState,
							"execute | currentState | 191");
					// System.err.println("invalid");
					// currentState.print();
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

	private double total;
	private double fourCount;

	public double learnProba(State expectedState, State state) {
		for (int l = 0; l < State.GRID_SIZE; l++) {
			for (int c = 0; c < State.GRID_SIZE; c++) {
				if (expectedState.get(l, c) != state.get(l, c)
						&& expectedState.get(l, c) == State.EMPTY_VALUE) {
					if (state.get(l, c) == 4) {
						fourCount++;
					}
					total++;
				}
			}
		}
		return fourCount / total;
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
						// System.out.println("error at " + l + " , " + c);
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
					if (x < State.GRID_SIZE) {
						value -= Math.abs(state.get(x, y) - val);
					}

					x = i - 1;
					y = j;
					if (x > 0) {
						value -= Math.abs(state.get(x, y) - val);
					}

					x = i;
					y = j - 1;
					if (y > 0) {
						value -= Math.abs(state.get(x, y) - val);
					}

					x = i;
					y = j + 1;
					if (y < State.GRID_SIZE) {
						value -= Math.abs(state.get(x, y) - val);
					}
				}
			}
			return value;
		}
	};

	public final static Heuristic heuristic2 = new Heuristic() {

		// private final int[] SUM_MIN_DIST = new int[] { 0, 0, 34, 25, 32, 19,
		// 16, 13, 10, 7, 6, 4, 3, 3, 1, 0 };

		// for group dist
		private final int[] SUM_MIN_DIST = new int[] { 0, 0, 17, 33, 66, 82,
				99, 115, 132, 89, 71, 28, 19, 3, 1, 0 };

		@Override
		public double getValue(State state) {
			double value = 0;// greater=better
			// common
			int valuesSum = 0;
			HashMap<Integer, List<Point>> numberPosition = new HashMap<Integer, List<Point>>();
			TreeMultiset<Integer> sortedValues = TreeMultiset.create();
			// barycentre
			double baryValue = 0;
			// distance between same numbers x number value
			double distanceBetweenSame = 0;
			for (int i = 0; i < State.GRID_SIZE; i++) {
				for (int j = 0; j < State.GRID_SIZE; j++) {

					// common
					int val = state.get(i, j);
					valuesSum += state.get(i, j);
					List<Point> points = numberPosition.get(val);
					if (points == null) {
						points = new ArrayList<>();
						numberPosition.put(val, points);
					}
					points.add(new Point(i, j));

					sortedValues.add(val);

					// barycentre
					if (val != 0) {
						double dist = (((double) i - 1.5) * ((double) i - 1.5))
								+ (((double) j - 1.5) * ((double) j - 1.5));
						baryValue += dist * val;
					}

					// old
					value += val * val;

					int x = i + 1;
					int y = j;
					if (x < State.GRID_SIZE) {
						value -= Math.abs(state.get(x, y) - val);
					}

					x = i - 1;
					y = j;
					if (x > 0) {
						value -= Math.abs(state.get(x, y) - val);
					}

					x = i;
					y = j - 1;
					if (y > 0) {
						value -= Math.abs(state.get(x, y) - val);
					}

					x = i;
					y = j + 1;
					if (y < State.GRID_SIZE) {
						value -= Math.abs(state.get(x, y) - val);
					}
				}
			}
			// commons
			SortedMultiset<Integer> sortedValuesDesc = sortedValues
					.descendingMultiset();
			double maxVal = sortedValuesDesc.iterator().next();

			// bary
			System.out.println(baryValue + " " + valuesSum);
			baryValue /= 4.5 * valuesSum;

			System.out.println("Bary : " + baryValue);

			// distance between same numbers x number value
			distanceBetweenSame = 0;
			double maxDistanceBetweenSame = 0;
			// System.out.println(numberPosition);
			Set<Entry<Integer, List<Point>>> it = numberPosition.entrySet();
			for (Entry<Integer, List<Point>> entry : it) {
				List<Point> points = entry.getValue();
				int val = entry.getKey();
				if (val == 0) {
					continue;
				}
				int size = points.size();
				if (size > 1) {
					for (int i = 0; i < size; i++) {
						Point a = points.get(i);
						double minDist = -1;
						for (int j = 0; j < size; j++) {
							if (i != j) {
								Point b = points.get(j);
								double dist = ((a.x - b.x) * (a.x - b.x))
										+ ((a.y - b.y) * (a.y - b.y));
								dist -= 1;
								if (minDist == -1 || dist < minDist) {
									minDist = dist;
								}
								// if (minDist < 0) {
								// System.out
								// .println("error "
								// + a
								// + " "
								// + b
								// + " "
								// + dist
								// + " "
								// + (((a.x - b.x) * (a.x - b.x)) + ((a.y - b.y)
								// * (a.y + b.y))));
								// }
							}
						}
						distanceBetweenSame += minDist / SUM_MIN_DIST[size]
								* (double) val / maxVal;
					}
				}
				// maxDistanceBetweenSame += SUM_MIN_DIST[size];
			}

			distanceBetweenSame = 1 - distanceBetweenSame;
			System.out
					.println("distance between same : " + distanceBetweenSame);
			return value;
		}
	};
	
	public final static HeuristicSafe heuristicSafe = new HeuristicSafe(false);
	public final static HeuristicSafe heuristicSafeLog = new HeuristicSafe(true);
}
