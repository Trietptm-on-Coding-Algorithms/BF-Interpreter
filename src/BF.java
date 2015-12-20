import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class BF {
	// GUI Components
	private JFrame frame = new JFrame( "JBF" );
	private JTextArea bf = new JTextArea();
	private JTextArea output = new JTextArea();
	private JTextField input = new JTextField();
	private JButton start = new JButton( "\u25b6" );
	private JButton stop = new JButton( "\u25a0" );
	private volatile boolean running = false;
	
	/**
	 * Memory array for BF
	 */
	private byte[] mem;
	
	/**
	 * Memory pointer
	 */
	private int mp;
	
	/**
	 * Instruction pointer - points at the current instruction in the program
	 */
	private int ip;
	
	/**
	 * Holds all characters entered through the input field in a queue
	 */
	private LinkedList<Byte> inStream = new LinkedList<Byte>();
	
	public BF( int memSize ) {
		if ( memSize <= 0 ) {
			System.err.println( "Memory size cannot be <= 0, exiting..." );
			System.exit( 1 );
		}
		mem = new byte[memSize];
		initGUI();
	}
	
	private void initGUI() {
		output.setEditable( false );
		input.addActionListener( event -> {
			String text = input.getText();
			input.setText( "" );
			text.chars().forEach( chr -> inStream.offer( (byte) chr ) );
		} );
		start.addActionListener( event -> {
			start.setEnabled( false );
			stop.setEnabled( true );
			final String bfText = bf.getText();
			new Thread( ( ) -> {
				BF.this.run( bfText );
				start.setEnabled( true );
				stop.setEnabled( false );
			} ).start();
		} );
		stop.addActionListener( event -> {
			running = false;
		} );
		stop.setEnabled( false );
		
		class LabelledJPanel extends JPanel {
			public LabelledJPanel( String label, Component child ) {
				TitledBorder tb = new TitledBorder( label );
				tb.setBorder( BorderFactory.createEtchedBorder() );
				tb.setTitleColor( Color.BLACK );
				setBorder( tb );
				setLayout( new BorderLayout() );
				add( child, BorderLayout.CENTER );
			}
		}
		bf.setFont( bf.getFont().deriveFont( 16f ) );
		output.setFont( bf.getFont() );
		
		JPanel main = new JPanel( new BorderLayout() );
		main.setPreferredSize( new Dimension( 800, 800 ) );
		
		JPanel textAreas = new JPanel( new GridLayout( 2, 1 ) );
		textAreas.add( new LabelledJPanel( "BF Program", new JScrollPane( bf ) ) );
		textAreas.add( new LabelledJPanel( "Output", new JScrollPane( output ) ) );
		main.add( textAreas, BorderLayout.CENTER );
		
		JPanel controls = new JPanel( new GridLayout( 2, 1 ) );
		
		JPanel inputPanel = new JPanel( new BorderLayout() );
		inputPanel.add( new JLabel( "Input:" ), BorderLayout.WEST );
		inputPanel.add( input, BorderLayout.CENTER );
		
		JPanel buttons = new JPanel( new GridLayout( 1, 2 ) );
		buttons.add( start );
		buttons.add( stop );
		
		controls.add( inputPanel );
		controls.add( buttons );
		main.add( controls, BorderLayout.SOUTH );
		
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.add( main );
		frame.pack();
		frame.setVisible( true );
	}
	
	/**
	 * Runs a BF program and returns the output
	 * 
	 * @param bf BF program to run
	 * @return Result of running program
	 */
	public String run( String bf ) {
		// Reset all variables
		mem = new byte[mem.length];
		mp = 0;
		ip = 0;
		inStream.clear();
		output.setText( "" );
		running = true;
		
		char[] c = bf.toCharArray();
		String out = "";
		while ( ip < c.length && running ) {
			switch ( c[ip] ) {
				case '>': // Move memory pointer right
					mp++;
					break;
				case '<': // Move memory pointer left
					mp--;
					break;
				case '+': // Increment byte at current memory location
					mem[mp]++;
					break;
				case '-': // Decrement byte at current memory location
					mem[mp]--;
					break;
				case '.': // Print character at current memory location
					out += (char) mem[mp];
					output.setText( output.getText() + (char) mem[mp] );
					output.setCaretPosition( output.getText().length() );
					break;
				case ',': // Read in a byte and place it in the current memory location
					if ( inStream.isEmpty() ) {
						ip--; // prevents moving forward
						if ( !input.hasFocus() ) {
							input.requestFocus();
						}
					} else {
						mem[mp] = inStream.poll();
					}
					break;
				case '[': // Enters while loop if current byte != 0
					if ( mem[mp] == 0 ) {
						int brac = 1;
						while ( brac > 0 ) {
							if ( c[++ip] == '[' ) {
								brac++;
							} else if ( c[ip] == ']' ) {
								brac--;
							}
						}
					}
					break;
				case ']': // Ends while loop if current byte == 0
					if ( mem[mp] != 0 ) {
						int brac = 1;
						while ( brac > 0 ) {
							if ( c[--ip] == '[' ) {
								brac--;
							} else if ( c[ip] == ']' ) {
								brac++;
							}
						}
					}
					break;
			}
			ip++; // Move to next instruction
		}
		return out;
	}
	
	public static void main( String[] args ) {
		new BF( 30000 );
	}
}
