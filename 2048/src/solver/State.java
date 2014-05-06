package solver;

import java.util.Arrays;

public abstract class State {


	public final static int GRID_SIZE = 4;
	public final static int EMPTY_VALUE = 0;
	public final static boolean REUSE_STATES=false;

	protected final int[][] grid = new int[GRID_SIZE][GRID_SIZE];
	
	protected int hash =0;
	protected boolean computeHash = false;

	// for possible later pooling
	protected int referenceCount = 0;

	protected State() {

	}

	public int getReferenceCount() {
		return referenceCount;
	}

	public void incrementRefrenceCount() {
		referenceCount++;
	}

	public void decrementRefrenceCount() {
		referenceCount--;
		if(referenceCount<0){
			throw new ArrayIndexOutOfBoundsException();
		}
	}

	public int get(int line, int column) {
		return grid[line][column];
	}

	public void set(int line, int column, int value) {
		grid[line][column] = value;
	}

	public void print() {
		StringBuilder sb = new StringBuilder();
		for (int line = 0; line < GRID_SIZE; line++) {
			boolean first = true;
			for (int col = 0; col < GRID_SIZE; col++) {
				if (first) {
					first = false;
				} else {
					sb.append(' ');
				}
				sb.append(get(line, col));
			}
			sb.append("\n");
		}
		System.out.println(sb.toString());
	}

	public interface Heuristic {
		public double getValue(State state);
	}

	public double getHeuristicValue(Heuristic heuristic) {
		return heuristic.getValue(this);
	}


	@Override
	public int hashCode() {
		if(computeHash){
			computeHash = false;
			final int prime = 31;
			hash = 0;
			for(int[] line : grid){
				for(int val : line){
					hash*=prime;
					hash += val;
				}
			}
		}
		return hash;
	}
	
	public void reset(){
		referenceCount=0;
		computeHash = true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		for(int i = 0;i<GRID_SIZE;i++){
			for(int j = 0;j<GRID_SIZE;j++){
				if(get(i, j)!=other.get(i,j)){
					return false;
				}
			}
		}
		return true;
	}
	
	public void copyInto(State state){
		for(int l =0;l<GRID_SIZE;l++){
			for(int c =0;c<GRID_SIZE;c++){
				state.set(l, c, get(l,c));
			}
		}
	}
}
