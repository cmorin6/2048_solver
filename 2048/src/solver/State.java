package solver;


public abstract class State {

	public final static int GRID_SIZE = 4;
	public final static int EMPTY_VALUE = 0;

	protected final int[][] grid = new int[GRID_SIZE][GRID_SIZE];
	

	// for possible later pooling
	protected int referenceCount=0;


	protected State() {

	}
	
	public int getReferenceCount(){
		return referenceCount;
	}
	
	public void incrementRefrenceCount(){
		referenceCount++;
	}
	
	public void decrementRefrenceCount(){
		referenceCount--;
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
				if(first){
					first =false;
				}else{
					sb.append(' ');
				}
				sb.append(get(line, col));
			}
			sb.append("\n");
		}
		System.out.println(sb.toString());
	}
	
	public interface Heuristic{
		public double getValue(State state);
	}
	
	public double getHeuristicValue(Heuristic heuristic){
		return heuristic.getValue(this);
	}

	// private boolean isUpEnable() {
	// for (int col = 0; col < GRID_SIZE; col++) {
	// int OldVal = EMPTY_VALUE;
	// for (int line = 0; line < GRID_SIZE; line++) {
	// int val = get(line, col);
	// if (val == EMPTY_VALUE) {
	// return true;
	// }
	// }
	// }
	// return false;
	// }

	

	

}
