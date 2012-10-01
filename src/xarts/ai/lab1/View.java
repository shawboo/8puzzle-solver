/**
 * 
 */
package xarts.ai.lab1;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

/**
 * @author xarts
 *
 */
@SuppressWarnings("serial")
public class View extends JFrame implements Runnable, ActionListener {
	
	private static final int BUTTON_SIZE = 166;
	private static final int MIN_FRAME_X = 3 * BUTTON_SIZE + 10 + 500;
	private static final int MIN_FRAME_Y = 600;
	
	View self;
	
	private JPanel buttons;
	public JButton[] grid;
	private int[] allX = {6, 177, 347};
	private int[] allY = {6, 177, 347};
	private JTextField depthField;
	private JTextField speedField;
	private JTextField numExpField;
	private JButton startButton;
	private JButton stopButton;
	private JTextArea status;
	private JButton experimentButton;
	private JButton randomizeButton;
	private JPanel options;
	JRadioButton BFSButton;
	JRadioButton RBFSButton;
	
	private Puzzle puzzle;
	private byte[][] board;

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		View view = new View();
		SwingUtilities.invokeLater(view);
	}
	
	public void run() {
		self = this;
		redirectSystemStreams();
		setTitle("8-puzzle solver");
		Dimension screenSize = getToolkit().getScreenSize();
		setPreferredSize(new Dimension(MIN_FRAME_X + 50,MIN_FRAME_Y));
		setMinimumSize(new Dimension(MIN_FRAME_X + 50,MIN_FRAME_Y));
		setLocation(((int)screenSize.getWidth() - MIN_FRAME_X) / 2,
				((int)screenSize.getHeight() - MIN_FRAME_Y) / 2);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
		
		MigLayout layout = new MigLayout();
		setLayout(layout);
		
		buttons = new JPanel(new MigLayout());
		grid = new JButton[9];
		for (int i = 0; i < 9; i++) {
			if (i%3 == 0 && i!=0) {
				buttons.add(new JLabel(" "), "wrap");
			}
			grid[i] = new JButton("" + i);
			grid[i].setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
			grid[i].setMaximumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
			grid[i].setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
			grid[i].setEnabled(false);
			grid[i].setBackground(Color.decode("0xffffbb"));
			buttons.add(grid[i]);
		}
		grid[0].setVisible(false);
		add(buttons, "span 3");
	//	add(new JLabel(" "), "wrap");
		
		options = new JPanel(new MigLayout());
		
		randomizeButton = new JButton("randomize");
		options.add(randomizeButton);
		options.add(new JLabel(" "), "wrap");
		
		depthField = new JTextField("20");
		depthField.setPreferredSize(new Dimension(40, 20));
		speedField = new JTextField("70");
		speedField.setPreferredSize(new Dimension(50, 20));
		startButton = new JButton("start");
		stopButton = new JButton("stop");
		options.add(new JLabel("Random steps(0-100): "));
		options.add(depthField);
		options.add(new JLabel("   Speed(0-100): "));
		options.add(speedField);
		options.add(startButton);
		options.add(stopButton);
		
		options.add(new JLabel(" "), "wrap");
		status = new JTextArea(10,40);
		status.setText("status\n");
		status.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(status,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		options.add(scrollPane, "span 7");
		
		add(options, "span 3");
		options.add(new JLabel(" "), "wrap");
		experimentButton = new JButton("start experiment");
		numExpField = new JTextField("10");
		numExpField.setPreferredSize(new Dimension(50, 20));
		
		BFSButton = new JRadioButton("BFS");
		BFSButton.setActionCommand("BFS");
		RBFSButton = new JRadioButton("RBFS");
		RBFSButton.setActionCommand("RBFS");
		RBFSButton.setSelected(true);
		
		ButtonGroup group = new ButtonGroup();
	    group.add(RBFSButton);
	    group.add(BFSButton);
	    BFSButton.addActionListener(this);
	    RBFSButton.addActionListener(this);
	    
	    JPanel other = new JPanel(new MigLayout());
	    other.add(new JLabel(" "), "wrap");
	    other.add(RBFSButton);
	    other.add(BFSButton);
	    other.add(new JLabel("Number of tries"));
	    other.add(numExpField);
	    other.add(experimentButton);
	    options.add(other, "span 6");
				
		startButton.addActionListener(this);
		stopButton.addActionListener(this);
		randomizeButton.addActionListener(this);
		experimentButton.addActionListener(this);
		depthField.setActionCommand("start");
		speedField.setActionCommand("start");
		
		try {
			board = Experiment.randomPuzzle(0);
			puzzle = new Puzzle(board, true, self);
			updateGrid();
		} catch (MyException e) {
			
			e.printStackTrace();
		}
	}
	
	public int getSleepTime() {
		return (100-Integer.parseInt(this.speedField.getText())) * 10;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("start")) {
			try {
				puzzle = new Puzzle(board, true, self);
				puzzle.execute();
			} catch (MyException e1) {
				status.append("MyException");
				e1.printStackTrace();
			}
		} else if (e.getActionCommand().equals("stop")) {
			puzzle.cancel(true);
			board = Experiment.randomPuzzle(0);
			updateGrid();
		} else if (e.getActionCommand().equals("randomize")) {
			puzzle.cancel(true);
			board = Experiment.randomPuzzle(Integer.parseInt(depthField.getText()));
			updateGrid();
		} else if (e.getActionCommand().equals("BFS")) {
			BFSButton.setSelected(true);
			RBFSButton.setSelected(false);
		} else if (e.getActionCommand().equals("RBFS")) {
			RBFSButton.setSelected(true);
			BFSButton.setSelected(false);
		} else if (e.getActionCommand().equals("start experiment")) {
			Experiment exp = new Experiment();
			if (RBFSButton.isSelected()) exp.setType(1);
									else exp.setType(0);
			exp.setNumTests(Integer.parseInt(numExpField.getText()));
			exp.setMaxDepth(Integer.parseInt(depthField.getText()));
			exp.execute();
		} else {
			status.append("Wrong command\n");
		}
	}
	
	/**
	 * 
	 */
	private void updateGrid() {
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				grid[board[i][j]].setLocation(allX[j], allY[i]);
			}
	}
	
	public void paint(byte[][] board) {
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				grid[board[i][j]].setLocation(allX[j], allY[i]);
			}
		buttons.paintImmediately(0, 0, 500, 500);
	}

	private void updateTextArea(final String text) {  
		SwingUtilities.invokeLater(new Runnable() {  
			public void run() {  
				status.append(text);  
			}  
		});  
	}  
		  
	private void redirectSystemStreams() {  
		OutputStream out = new OutputStream() {  
			@Override  
			public void write(int b) throws IOException {  
				updateTextArea(String.valueOf((char) b));  
			}  
			
			@Override  
			public void write(byte[] b, int off, int len) throws IOException {  
				updateTextArea(new String(b, off, len));  
			}  
			
			@Override  
				public void write(byte[] b) throws IOException {  
			write(b, 0, b.length); 
			}
		};
		
		System.setOut(new PrintStream(out, true));  
	//	System.setErr(new PrintStream(out, true));  
	}
	
}
