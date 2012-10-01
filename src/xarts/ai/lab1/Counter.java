/**
 * 
 */
package xarts.ai.lab1;

/**
 * @author xarts
 *
 */
public class Counter implements Runnable {

	private boolean run = true;
	private Puzzle p;
	
	public Counter(Puzzle p, String mes) {
		System.out.print(mes);
		this.p = p;
	}
	
	@Override
	public void run() {
		run = true;
		int count = 1;
		while (run) {
			System.out.print(".");
			if (count%10 == 0) {
				System.out.println("Nodes created: " + p.getStatistics().nodesCreated);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			count++;
		}
	}
	
	public void stop() {
		run = false;
	}

}
