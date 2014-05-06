package solver;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

import solver.State.Heuristic;

public class HeuristicSafe implements Heuristic {

	// for group dist
	private final int[] SUM_MIN_DIST = new int[] { 0, 0, 17, 33, 66, 82, 99,
			115, 132, 89, 71, 28, 19, 3, 1, 0 };

	// parent dist [currentNumberCount][parentCount]
	private final int[][] MAX_PARENT_DIST = new int[][] {
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 17, 12, 12, 9, 9, 8, 8, 7, 4, 4, 3, 3, 1, 0, 0 },
			{ 0, 29, 21, 19, 16, 15, 12, 11, 10, 7, 6, 4, 3, 1, 0 },
			{ 0, 41, 29, 26, 24, 19, 15, 14, 13, 8, 6, 4, 3, 1 },
			{ 0, 50, 37, 33, 32, 22, 18, 17, 14, 8, 6, 4, 3 },
			{ 0, 59, 44, 37, 35, 25, 20, 17, 14, 8, 6, 4 },
			{ 0, 67, 48, 40, 38, 28, 21, 17, 14, 8, 6 },
			{ 0, 75, 52, 43, 41, 29, 21, 17, 14, 8 },
			{ 0, 82, 55, 46, 44, 29, 21, 17, 14 },
			{ 0, 86, 58, 47, 44, 29, 21, 17 }, { 0, 90, 61, 47, 44, 29, 21 },
			{ 0, 93, 62, 47, 44, 29 }, { 0, 96, 62, 47, 44 },
			{ 0, 97, 62, 47 }, { 0, 97, 62 }, { 0, 97 }, { 0 } };

	private int[] bufferDoubleSize1;
	private int[] bufferDoubleSize2;
	private int[] parentDist;

	private HashMap<Integer, List<Integer>> numberPosition = new HashMap<Integer, List<Integer>>();
	private TreeMultiset<Integer> sortedValues = TreeMultiset.create();

	private boolean log;

	public HeuristicSafe(boolean log) {
		bufferDoubleSize1 = new int[State.GRID_SIZE * State.GRID_SIZE];
		bufferDoubleSize2 = new int[State.GRID_SIZE * State.GRID_SIZE];
		parentDist = new int[State.GRID_SIZE * State.GRID_SIZE];
		for (int i = 0; i < State.GRID_SIZE * State.GRID_SIZE; i++) {
			parentDist[i] = -1;
		}
		this.log = log;
	}

	@Override
	public double getValue(State state) {
		if (state instanceof DecisionState) {
			DecisionState ds = (DecisionState) state;
			if (!ds.isDownEnable() && !ds.isLeftEnable() && !ds.isRightEnable()
					&& !ds.isUpEnable()) {
				return -1;
			}
		}
		long time = System.currentTimeMillis();
		double value = 0;// greater=better
		// common
		int valuesSum = 0;
		numberPosition.clear();
		sortedValues.clear();
		// barycentre
		double baryValue = 0;
		// distance between same numbers x number value
		double distanceBetweenSame = 0;
		// max number possible
		double maxComb = 0;
		// closestparent
		double closestParent = 0;
		for (int i = 0; i < State.GRID_SIZE; i++) {
			for (int j = 0; j < State.GRID_SIZE; j++) {

				// common
				int val = state.get(i, j);
				valuesSum += state.get(i, j);
				List<Integer> points = numberPosition.get(val);
				if (points == null) {
					points = new ArrayList<>();
					numberPosition.put(val, points);
				}
				points.add(i * State.GRID_SIZE + j);

				sortedValues.add(val);

				// barycentre
				if (val != 0) {
					double dist = (((double) i - 1.5) * ((double) i - 1.5))
							+ (((double) j - 1.5) * ((double) j - 1.5));
					baryValue += dist * val;
				}

				// maxComb
				maxComb += val * val;
			}
		}
		// commons
		SortedMultiset<Integer> sortedValuesDesc = sortedValues
				.descendingMultiset();
		int maxVal = sortedValuesDesc.iterator().next();

		// bary
		// System.out.println(baryValue + " " + valuesSum);
		baryValue /= 4.5 * valuesSum;

		// System.out.println("Bary : " + baryValue);

		// distance between same numbers x number value
		distanceBetweenSame = 0;
		// System.out.println(numberPosition);
		Set<Entry<Integer, List<Integer>>> it = numberPosition.entrySet();
		for (Entry<Integer, List<Integer>> entry : it) {
			List<Integer> points = entry.getValue();
			int val = entry.getKey();
			double res = computeGroupDist(points);
			if (res > 0) {
				distanceBetweenSame += (double) (res * val)
						/ (double) (SUM_MIN_DIST[points.size()] * maxVal);
			}
		}

		distanceBetweenSame = 1 - distanceBetweenSame;
		// System.out.println("distance between same : " + distanceBetweenSame);
		// System.out.println("time : "+(System.currentTimeMillis()-time));

		// max number possible
		int maxMaxComb = 0;
		int maxNumber = 1;
		int valuesSumCopy = valuesSum;
		while (maxNumber < valuesSum) {
			maxNumber *= 2;
		}
		maxNumber /= 2;
		while (valuesSumCopy > 1) {
			if (valuesSumCopy > maxNumber) {
				maxMaxComb += maxNumber * maxNumber;
				valuesSumCopy -= maxNumber;
			}
			maxNumber /= 2;
		}
		// System.out.println(maxMaxComb+" "+maxComb);
		maxComb /= (double) maxMaxComb;
		// System.out.println(maxComb);

		// closestParent
		int lastVal = -1;
		List<Integer> parentPositions = null;
		for (Integer current : sortedValuesDesc) {
			if (current == 0) {
				break;
			}
			if (lastVal == -1) {
				lastVal = current;
				parentPositions = numberPosition.get(lastVal);
			}
			if (lastVal != current) {
				List<Integer> currentPositions = numberPosition.get(current);
				int res = computeParentDist(parentPositions, currentPositions);
				closestParent += (double) (res * current * 2)
						/ (double) (MAX_PARENT_DIST[currentPositions.size()][parentPositions
								.size()] * maxVal);
				parentPositions = currentPositions;
			}
		}
		closestParent = 1 - closestParent;
		if (log) {
			System.out.println("CLOSEST_PARENT : " + closestParent);
		}
		double ret = baryValue + distanceBetweenSame + 2 * closestParent + 3
				* maxComb;
		if (log) {
			System.out.println("GLOBAL         : " + ret);
		}
		return ret;
	}

	public int computeGroupDist(List<Integer> points) {
		List<List<Integer>> groups = new ArrayList<List<Integer>>();
		List<Integer> currentGroup = new ArrayList<>();
		groups.add(currentGroup);
		List<Integer> notVisited = new ArrayList<>();
		Queue<Integer> toCheck = new LinkedList<>();
		int[][] groupDist = new int[points.size()][points.size()];
		int[][] pointDistToGroup = new int[State.GRID_SIZE * State.GRID_SIZE][points
				.size()];
		for (int i = 0; i < points.size(); i++) {
			Integer point = points.get(i);
			if (i == 0) {
				toCheck.add(point);
				currentGroup.add(point);
			} else {
				notVisited.add(point);
			}
		}
		while (!toCheck.isEmpty() && !notVisited.isEmpty()) {
			Integer point = toCheck.poll();
			// System.out.println("selected point : " + point);
			Iterator<Integer> it = notVisited.iterator();
			while (it.hasNext()) {
				Integer currentPoint = it.next();
				int a = currentPoint;
				int ax = a % State.GRID_SIZE;
				int ay = a / State.GRID_SIZE;
				int b = point;
				int bx = b % State.GRID_SIZE;
				int by = b / State.GRID_SIZE;
				int dist = (ax - bx) * (ax - bx) + (ay - by) * (ay - by);
				dist -= 1;
				if (dist == 0) {
					currentGroup.add(currentPoint);
					it.remove();
					toCheck.add(currentPoint);
					int currentGroupIndex = groups.size() - 1;
					for (int i = 0; i < currentGroupIndex; i++) {
						int oldDist = groupDist[i][currentGroupIndex];
						int pointDist = pointDistToGroup[currentPoint][i];
						if (oldDist == 0 || oldDist > pointDist) {
							groupDist[i][currentGroupIndex] = pointDist;
						}
					}
				} else {
					int currentGroupIndex = groups.size() - 1;
					int oldDist = pointDistToGroup[currentPoint][currentGroupIndex];
					if (oldDist == 0 || oldDist > dist) {
						pointDistToGroup[currentPoint][currentGroupIndex] = dist;
					}
				}
			}
			if (toCheck.isEmpty() && !notVisited.isEmpty()) {
				currentGroup = new ArrayList<>();
				groups.add(currentGroup);
				Integer currentPoint = notVisited.remove(0);
				toCheck.add(currentPoint);
				currentGroup.add(currentPoint);
				int currentGroupIndex = groups.size() - 1;
				for (int i = 0; i < currentGroupIndex; i++) {
					int oldDist = groupDist[i][currentGroupIndex];
					int pointDist = pointDistToGroup[currentPoint][i];
					if (oldDist == 0 || oldDist > pointDist) {
						groupDist[i][currentGroupIndex] = pointDist;
					}
				}
			}
		}
		int ret = 0;
		for (int i = 0; i < groups.size(); i++) {
			for (int j = i + 1; j < groups.size(); j++) {
				ret += Math.min(groups.get(i).size(), groups.get(j).size())
						* groupDist[i][j];
			}
		}
		return ret;
	}

	/**
	 * Computes the cumulated minimal distances between a number and it's
	 * closest parent (in term of number value).
	 * 
	 * @param parentNumbers
	 *            Closest parents.
	 * @param currentNumbers
	 *            Number to be checked upon parents.
	 * @return
	 */
	public int computeParentDist(List<Integer> parentNumbers,
			List<Integer> currentNumbers) {

		int ret = 0;
		for (Integer parent : parentNumbers) {
			int a = parent;
			int ax = a % State.GRID_SIZE;
			int ay = a / State.GRID_SIZE;
			for (int i = 0; i < currentNumbers.size(); i++) {
				if (parentDist[i] == 0) {
					continue;
				}
				int b = currentNumbers.get(i);
				int bx = b % State.GRID_SIZE;
				int by = b / State.GRID_SIZE;
				int dist = (ax - bx) * (ax - bx) + (ay - by) * (ay - by);
				dist -= 1;
				if (parentDist[i] == -1 || dist < parentDist[i]) {
					parentDist[i] = dist;
				}
			}
		}
		for (int i = 0; i < currentNumbers.size(); i++) {
			ret += parentDist[i];
			parentDist[i] = -1;
		}
		return ret;
	}
}
