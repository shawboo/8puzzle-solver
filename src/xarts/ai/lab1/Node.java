/**
 * 
 */
package xarts.ai.lab1;

import java.util.Arrays;


/**
 * @author xarts
 *
 */
class Node {
	
	/**	Board sizes. Should be that sqrt(SIZE) = ROW_SIZE */
	protected static final int ROW_SIZE = 3;
	protected static final int SIZE = 9;
	
	/** Enums for actions with the board */
	protected static final byte NONE = -1;
	protected static final byte MOVE_UP = 0;
	protected static final byte MOVE_DOWN = 1;
	protected static final byte MOVE_LEFT = 2;
	protected static final byte MOVE_RIGHT = 3;
	
	/** Board state for this node. */
	protected byte[][] board;
	/** Action that was taken in parent node to create this node. */
	protected byte action;
	/** How much action was taken since the initial state. */
	protected byte depth;
	/** Position of a free square (to accelerate the calculations). */
	protected byte freeCell = -1;
	protected Node parent;
	
	/** Constructor for initial node.
	 * @param board
	 */
	public Node(byte[][] board) {
		this.board = new byte[3][3];
		for (int i = 0; i < SIZE; i++) {
			this.board[i/ROW_SIZE][i%ROW_SIZE] = board[i/ROW_SIZE][i%ROW_SIZE];
		}
		this.parent = null;
		this.action = NONE;
		this.depth = (byte)(0);
	}

	/** Constructor for descendants. (Move constructor)
	 * @param parent
	 * @param action action that must be taken
	 */
	public Node(Node parent, byte action) {
		this.board = move(parent,action);
		this.parent = parent;
		this.action = action;
		this.depth = (byte)(parent.getDepth() + 1);
	}
	
	/** Check whether this node is a solution.
	 * @return true if solution
	 */
	protected boolean isSolution() {
		for (int i = 0; i < SIZE; i++) {
			if (board[i/ROW_SIZE][i%ROW_SIZE] != i) return false; 
		}
		return true;
	}
	
	protected byte[][] cloneBoard(Node parent) {
		byte[][] newBoard = new byte[3][3];
		for (int i = 0; i < SIZE; i++) {
			newBoard[i/ROW_SIZE][i%ROW_SIZE] = parent.board[i/ROW_SIZE][i%ROW_SIZE];
		}
		return newBoard;
	}
	
	public byte getDepth() {
		return depth;
	}
	
	public byte[][] getBoard() {
		return board;
	}
	
	public String getActionToString() {
		if (action == 0) return "Move up\n";
		if (action == 1) return "Move down\n";
		if (action == 2) return "Move left\n";
		if (action == 3) return "Move right\n";
		return "Start\n";
	}
	
	public byte getAction() {
		return action;
	}
	
	public Node getParent() {
		return parent;
	}
	
	public byte getFreeCell() {
		if (freeCell == -1) {
			for (int i = 0; i < SIZE; i++) {
				if (board[i/ROW_SIZE][i%ROW_SIZE] == 0) {
					freeCell = (byte)(i);
					break;
				}
			}
		}
		return freeCell;
	}
	
	protected byte getFreeCellX() {
		return (byte)(getFreeCell()%3);
	}
	
	protected byte getFreeCellY() {
		return (byte)(getFreeCell()/3);
	}
	
	public boolean canMove(int direction) {
		if (direction == MOVE_LEFT) return (getFreeCellX() > 0);
		if (direction == MOVE_RIGHT) return (getFreeCellX() < 2);
		if (direction == MOVE_UP) return (getFreeCellY() > 0);
		if (direction == MOVE_DOWN) return (getFreeCellY() < 2);
		return false;
	}
	
	public byte[][] move(Node parent, int direction) {
		byte[][] newBoard = cloneBoard(parent);
		byte freeCellY = parent.getFreeCellY();
		byte freeCellX = parent.getFreeCellX();
		if (direction == MOVE_LEFT) {
			byte tmp = newBoard[freeCellY][freeCellX - 1];
			newBoard[freeCellY][freeCellX - 1] = newBoard[freeCellY][freeCellX];
			newBoard[freeCellY][freeCellX] = tmp;
		} else if (direction == MOVE_RIGHT) {
			byte tmp = newBoard[freeCellY][freeCellX + 1];
			newBoard[freeCellY][freeCellX + 1] 
			                             = newBoard[freeCellY][freeCellX];
			newBoard[freeCellY][freeCellX] = tmp;
		} else if (direction == MOVE_UP) {
			byte tmp = newBoard[freeCellY - 1][freeCellX];
			newBoard[freeCellY - 1][freeCellX] 
			                             = newBoard[freeCellY][freeCellX];
			newBoard[freeCellY][freeCellX] = tmp;
		} else if (direction == MOVE_DOWN) {
			byte tmp = newBoard[freeCellY + 1][freeCellX];
			newBoard[freeCellY + 1][freeCellX] 
			                             = newBoard[freeCellY][freeCellX];
			newBoard[freeCellY][freeCellX] = tmp;
		}
		return newBoard;
	}
	
	@Override
	public String toString() {
		String s = "[";
		for (int i = 0; i < ROW_SIZE; i++) {
			s += Arrays.toString(board[i]) + ", ";
		}
		s = s.substring(0, s.length() - 2) + "]";
		return s;
	}

}
