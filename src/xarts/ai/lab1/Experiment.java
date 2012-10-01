/**
 * 
 */
package xarts.ai.lab1;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.swing.SwingWorker;

/**
 * @author xarts
 *
 */
public class Experiment extends SwingWorker<Void, Void>  {
	
	private int successfulTests = 0;
	private int totalTests = 0;
	private Statistics[] s = new Statistics[100];
	private int numTests = 10;
	private int maxDepth = 40;
	private int type = 1;
	
	public void setNumTests(int nTests) {
		numTests = nTests;
	}
	
	public void setMaxDepth(int depth) {
		maxDepth = depth;
	}
	
	public void setType(int t) {
		type = t;
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public void runExperiment(int numTries) throws IOException, InterruptedException {
		BufferedWriter bw = null;
		bw = new BufferedWriter(new PrintWriter("log.txt"));
		while (totalTests <= numTries) {
			bw.write(runTest(true));
			System.gc();
			Thread.sleep(1000);
		}
		bw.write("Successful tests: " + successfulTests + "\n");
		for (int i = 1; i < totalTests; i++) {
			s[0].nodesCreated += s[i].nodesCreated;
			s[0].nodesInMemory += s[i].nodesInMemory;
			s[0].totalTime += s[i].totalTime;
		}
		s[0].nodesCreated /= totalTests;
		s[0].nodesInMemory /= totalTests;
		s[0].totalTime /= totalTests;
		bw.write("Average nodes created: " + s[0].nodesCreated + "\n");
		bw.write("Average nodes in memory: " + s[0].nodesInMemory + "\n");
		bw.write("Average total time: " + (double)s[0].totalTime/1000 + " s\n");
		bw.close();
		System.out.println("File \"log.txt\" created");
	}
	
	private String runTest(boolean restrict) {
		String log = "\nTest #" + totalTests + "\n";
		try {
			Random rnd = new Random();
			Puzzle p = new Puzzle(randomPuzzle(maxDepth + rnd.nextInt(maxDepth / 2)), restrict, null);		//generate puzzles with depth of solution in [7,20]
			log += "Initial state: " + p.toString() + "\n";
			Counter c = new Counter(p, "Next test started\n");
			new Thread(c).start();
			try {
				boolean res = false;
				if (type == 0) res = p.BFS();
				else if (type == 1) res = p.RBFS();
				if (res) {
					successfulTests++;
					log += "Action sequence:\n" + p.getResult();
				} else {
					log += "Solution not found\n";
				}
			} catch (MyOutOfMemoryException e) {
				log += "Out of memory\n";
			} catch (MyOutOfTimeException e) {
				log += "Out of time\n";
			} finally {
				s[totalTests] = p.getStatistics();
				log += "Nodes created: " + s[totalTests].nodesCreated + "\n";
				log += "Nodes in memory: " + s[totalTests].nodesInMemory + "\n";
				log += "Total time: " + (double)s[totalTests].totalTime/1000 + " s\n";
			}
			c.stop();
			System.out.println("Test #" + totalTests + " completed\n");
			totalTests++;
		} catch (MyException e) {
			System.out.println(e.getMessage());
			log = "\n" + e.getMessage();
		}
		return log;
	}
	
	public static byte[][] randomPuzzle(int maxDepth) {
		byte[][] puzzle = new byte[3][3];
		int[] chips = new int[9];
		for (int i = 0; i < 9; i++) {
			chips[i] = i;
		}
		for (int i = 0; i < 9; i++) {
			puzzle[i/3][i%3] = (byte)chips[i];
		}
		Node n = new Node(puzzle);
		Random rnd = new Random();
		for (int i = 0; i < maxDepth; i++) {
			boolean done = false;
			while (!done) {
				int direction = rnd.nextInt(4);
				if (n.canMove(direction)) {
					n = new Node(n, (byte)direction);
					done = true;
				}
			}
		}
		System.out.println(n.toString());
		return n.getBoard();
	}
	
	@Override
	protected Void doInBackground() {
		try {
			runExperiment(numTests);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

}
