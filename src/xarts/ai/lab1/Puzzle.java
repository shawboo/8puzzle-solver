/**
 * File contains class Puzzle.
 */
package xarts.ai.lab1;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import javax.swing.SwingWorker;

/** Representation of 8-puzzle problem.
 * @author xarts
 * Contains problem initial state and algorithms for finding solution.
 * Problem's initial state is immutable, you need to create new class
 * for new problem state. 
 */
public class Puzzle extends SwingWorker<Boolean, byte[][]> {
	
	private final boolean RESTRICTIONS;
	/** Max memory for use in program in MB. */
	private final int MAX_MEMORY = 1024;
	/** Max time for use in program in ms. */
	private final int MAX_TIME = 30*60*1000;
	
	private Runtime rt = Runtime.getRuntime();
	private View view;
	
	/**	Board sizes. Should be that sqrt(SIZE) = ROW_SIZE. */
	private static final int ROW_SIZE = 3;
	private static final int SIZE = 9;
	
	/** Enums for actions with the board. */
//	private static final byte NONE = -1;
//	private static final byte MOVE_UP = 0;
//	private static final byte MOVE_DOWN = 1;
//	private static final byte MOVE_LEFT = 2;
//	private static final byte MOVE_RIGHT = 3;
	
	/**	board configuration */
	private final byte[][] board;
	
	/** Resulting node (from which we can backtrack whole result). */
	private Node result;
	
	/** little statistics */
	private Statistics stat = new Statistics();
	
	/** Creates new instance of game.
	 * @param initialState initial positions of chips
	 * @param restrictions true to enable the memory and time restrictions
	 * @throws MyException This exception message contains information about problem.
	 */
	Puzzle(byte[][] initialState, boolean restrictions , View view) throws MyException {
		RESTRICTIONS = restrictions;
		int[] availableChips = new int[SIZE];
		for (int i = 0; i < SIZE; i++) {
			availableChips[i] = i;
		}
		
		byte[][] board = new byte[ROW_SIZE][ROW_SIZE];
		for (int i = 0; i < SIZE; i++) {
			if (initialState[i/ROW_SIZE][i%ROW_SIZE] < 0 
				|| initialState[i/ROW_SIZE][i%ROW_SIZE] >= 9) 
				throw new MyException("Chip has bad value (has to be between 0 and 8).");
			
			if (availableChips[initialState[i/ROW_SIZE][i%ROW_SIZE]] != -1) {
				board[i/ROW_SIZE][i%ROW_SIZE] = 
					(byte)availableChips[initialState[i/ROW_SIZE][i%ROW_SIZE]];
				availableChips[initialState[i/ROW_SIZE][i%ROW_SIZE]] = -1;
			} else {
				throw new MyException("Chip is repeated twice.\n");
			}
		}
		
		if (!isSolvable(board)) {
		//	throw new MyException("This configuration is not solvable.\n");
		}
		
		this.board = board;
		this.view = view;
		
	}
	
	/** Checks whether problem can be solved.
	 * @param board initial state of a board
	 * @return true if problem is solvable
	 */
	private boolean isSolvable(byte[][] board) {
		int counter = 0;
		for (int i = 0; i < SIZE - 1; i++) {
			for (int j = i + 1; j < SIZE; j++) {
				if (board[i/ROW_SIZE][i%ROW_SIZE] > board[j/ROW_SIZE][j%ROW_SIZE]
				       && board[i/ROW_SIZE][i%ROW_SIZE] != 0 
				       && board[j/ROW_SIZE][j%ROW_SIZE] != 0) {
					counter++;
				}
			}
		}
		return (counter%2 == 0);
	}
	
	/**
	 * @return string representation of board state
	 */
	public String toStringMatrix() {
		String str = new String();
		for (int i = 0; i < SIZE; i++) {
			if ((i%ROW_SIZE == 0) && (i > 0)) str += "\n";
			str += board[i/ROW_SIZE][i%ROW_SIZE] + " ";
		}
		return str;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String s = "[";
		for (int i = 0; i < ROW_SIZE; i++) {
			s += Arrays.toString(board[i]) + ", ";
		}
		s = s.substring(0, s.length() - 2) + "]";
		return s;
	}
	
	public Statistics getStatistics() {
		return stat;
	}
	
	/** string representation of result
	 * @return formated string of needed actions to get to result from the initial state
	 */
	public String getResult() {
		Stack<String> stack = new Stack<String>();
		Node n = result;
		while (n != null) {
			stack.push(n.getActionToString());
			n = n.getParent();
		}
		String res = "";
		while (!stack.isEmpty()) {
			res += stack.pop();
		}
		res += "Depth: " + result.getDepth() + "\n";
		return res;
	}
	
	/** Trying to solve the problem by exhaustive search of all nodes using queue as fringe 
	 * @return true if result was found
	 * @throws MyOutOfMemoryException
	 * @throws MyOutOfTimeException
	 */
	public boolean BFS() throws MyOutOfMemoryException, MyOutOfTimeException {
		stat.nodesCreated = 0;
		stat.totalTime = System.currentTimeMillis();
		
		ArrayDeque<Node> fringe = new ArrayDeque<Node>();
		fringe.add(new Node(board));
		while (!fringe.isEmpty()) {
			Node node = fringe.remove();		//take first element out of queue
		//	System.out.println("Current node:" + node.toString());
			if (node.isSolution()) {
				result = node;
				stat.nodesInMemory = fringe.size();
				stat.totalTime = System.currentTimeMillis() - stat.totalTime;
				return true;
			}
			expand(node, fringe);		//add descendants to the end of a queue
			if (RESTRICTIONS) {
				if ((rt.totalMemory() - rt.freeMemory())/1048576 > MAX_MEMORY) {
					stat.nodesInMemory = fringe.size();
					stat.totalTime = System.currentTimeMillis() - stat.totalTime;
					throw new MyOutOfMemoryException();
				}
				if (System.currentTimeMillis() - stat.totalTime > MAX_TIME) {
					stat.nodesInMemory = fringe.size();
					stat.totalTime = System.currentTimeMillis() - stat.totalTime;
					throw new MyOutOfTimeException();
				}
			}
		}
		stat.nodesInMemory = fringe.size();
		stat.totalTime = System.currentTimeMillis() - stat.totalTime;
		return false;
	}
	
	/** Creates a collection of descendants of some node.
	 * @param node parent
	 * @param collection where to put descendants
	 */
	private void expand(Node node, Collection<Node> collection) {
		for (int i = 0; i < 4; i++) {		//for each direction(UP, DOWN, LEFT, RIGHT)
			if (node.canMove(i)) {	
				collection.add(new Node(node, (byte)i));
				stat.nodesCreated++;
			}
		}
	}
	
	/** Wrapper function for recursiveSearch
	 * @return true if result was found
	 * @throws MyOutOfMemoryException 
	 * @throws MyOutOfTimeException 
	 */
	public boolean RBFS() throws MyOutOfMemoryException, MyOutOfTimeException {
		stat.nodesCreated = 0;
		stat.totalTime = System.currentTimeMillis();
		HeuristicNode initialState = new HeuristicNode(board);
		short result = 0;
		try {
			result = recursiveSearch(initialState, (short)30000);
		} catch (InterruptedException e) {}
		stat.totalTime = System.currentTimeMillis() - stat.totalTime;
		if (result == -1) {
			publish(this.result.getBoard());
			return true;			//-1 stands for success
		}
		return false;
	}
	
	int depth = 0;

	/** Recursively searches the node for solution.
	 * @param node node to be searched
	 * @param limit value of best alternative criterion
	 * @return criterion limit for current node or success indicator(-1)
	 * @throws MyOutOfMemoryException 
	 * @throws MyOutOfTimeException 
	 * @throws InterruptedException 
	 */
	private short recursiveSearch(HeuristicNode node, short limit) throws MyOutOfMemoryException, MyOutOfTimeException, InterruptedException {
		if (node.getDepth() > depth) {
			System.out.println("Depth:" + depth);
			depth = node.getDepth();
		}
		if (node.isSolution()) {
			result = node;
			stat.nodesInMemory = 1;

			publish(node.getBoard());
			if (view != null) Thread.sleep(view.getSleepTime());

			return -1;
		}
		ArrayList<HeuristicNode> descendants = new ArrayList<HeuristicNode>();
		expand_RBFS(node, descendants);
		while (true) {
			if (RESTRICTIONS) {
				if (System.currentTimeMillis() - stat.totalTime > MAX_TIME) {
					stat.totalTime = System.currentTimeMillis() - stat.totalTime;
					throw new MyOutOfTimeException();
				}
			}
			int bestNum = bestNode(descendants);
			HeuristicNode best = descendants.get(bestNum);
			if (best.getCriterion() > limit) return best.getCriterion();
			short alternative;
			if (descendants.size() > 1) {
				alternative = descendants.get(secondBestNode(descendants, bestNum)).getCriterion();
			} else alternative = 30000;

			publish(node.getBoard());
			if (view != null) Thread.sleep(view.getSleepTime());

			short result = recursiveSearch(best, min(limit, alternative));
			if (result == -1) {		//if succeeded
				stat.nodesInMemory += descendants.size();
				return -1;
			}
			
			publish(node.getBoard());
			if (view != null) Thread.sleep(view.getSleepTime());

			best.setCriterion(result);
		}
	}
	
	/**
	 * @param limit
	 * @param alternative
	 * @return
	 */
	private short min(short limit, short alternative) {
		if (limit < alternative) return limit;
		return alternative;
	}

	/** Finds node with best (minimal) criterion.
	 * @param descendants collection of nodes
	 * @return number of node with best criterion in collection of descendants
	 */
	private int bestNode(ArrayList<HeuristicNode> descendants) {
		HeuristicNode best = descendants.get(0);
		int bestNum = 0;
		for (int i = 1; i < descendants.size(); i++) {
			if (descendants.get(i).getCriterion() < best.getCriterion()) {
				best = descendants.get(i);
				bestNum = i;
			}
		}
		return bestNum;
	}
	
	/** Finds node with second best (minimal) criterion.
	 * @param descendants collection of nodes
	 * @param best
	 * @return number of node with second best criterion in collection of descendants
	 */
	private int secondBestNode(ArrayList<HeuristicNode> descendants, int best) {
		HeuristicNode secondBest;
		int secondBestNum;
		if (best == 0) {
			secondBest = descendants.get(1);
			secondBestNum = 1;
		}
		else {
			secondBest = descendants.get(0);
			secondBestNum = 0;
		}
		for (int i = 0; i < descendants.size(); i++) {
			if (i != best && i != secondBestNum
					&& descendants.get(i).getCriterion() < secondBest.getCriterion()) {
				secondBest = descendants.get(i);
				secondBestNum = i;
			}
		}
		return secondBestNum;
	}

	/** Creates a collection of descendants of some node.
	 * @param node parent
	 * @param collection where to put descendants
	 */
	private void expand_RBFS(HeuristicNode node, ArrayList<HeuristicNode> collection) {
		for (int i = 0; i < 4; i++) {		//for each direction(UP, DOWN, LEFT, RIGHT)
			if (node.canMove(i)/* && node.getAction() != i*/) {	
				collection.add(new HeuristicNode(node, (byte)i));
				stat.nodesCreated++;
			}
		}
	}
	
	@Override
	protected Boolean doInBackground() {
		boolean res = false;
		try {
			res = RBFS();
		} catch (MyOutOfMemoryException e) {
			res = false;
		} catch (MyOutOfTimeException e) {
			res = false;
		}
	    return res;
	}
	
    @Override
    protected void process(List<byte[][]> boards) {
    	if (view != null) {
    		for (int i = 0; i < boards.size(); i++)
    			view.paint(boards.get(i));
    	}
    }
	
}





