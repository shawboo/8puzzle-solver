/**
 * 
 */
package xarts.ai.lab1;

/**
 * @author xarts
 *
 */
public class HeuristicNode extends Node {

	private short heuristic = evaluateHeuristic(); /* = evaluateHeuristicManhattan(); */
	private short criterion;

	/** constructor for descendants
	 * @param parent
	 * @param action action that must be taken
	 */
	public HeuristicNode(HeuristicNode parent, byte action) {
		super(parent, action);
		criterion = max((short)(getHeuristic() + getDepth()), parent.criterion);
	}
	
	private short max(short a, short b) {
		if (a > b) return a;
		return b;
	}
	
	/** Constructor for the initial node
	 * @param board
	 */
	public HeuristicNode(byte[][] board) {
		super(board);
		criterion = (short)(getHeuristic() + getDepth());
	}
	
	public short evaluateHeuristic() {
		short h = SIZE - 1;
		for (int i = 1; i < SIZE; i++) {
			if (board[i/ROW_SIZE][i%ROW_SIZE] == i) h--;
		}
		return h;
	}
	
	public short evaluateHeuristicManhattan() {
		short h = 0;
		for (int i = 0; i < SIZE; i++) {
			int chip = board[i/ROW_SIZE][i%ROW_SIZE];
			if (chip != 0) {
				h += Math.abs(i/ROW_SIZE - chip/ROW_SIZE) 
					+ Math.abs(i%ROW_SIZE - chip%ROW_SIZE);
			}
		}
		return h;
	}
	
	public short getHeuristic() {
		return heuristic;
	}
	
	public short getCriterion() {
		return criterion;
	}
	
	public void setCriterion(short criterion) {
		this.criterion = criterion;
	}
	
}
