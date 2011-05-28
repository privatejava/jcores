package net.jcores.script.util.console;

import static net.jcores.CoreKeeper.$;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.jcores.interfaces.functions.F0;

/**
 * We use this console to output what has been printed on the actual console ...
 * 
 * @author Ralf Biedert
 */
public class JCoresConsole extends JFrame {
	/** */
	private static final long serialVersionUID = -6393982993147415420L;

	/** Logging area */
	protected JTextArea textArea;

	/** Scroll pane */
	protected JScrollPane scrollPane;

	/**
	 * Constructs a console.
	 * 
	 * @param title
	 */
	public JCoresConsole(String title) {
		setTitle(title);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 400);
		setVisible(true);

		this.textArea = new JTextArea();
		this.textArea.setBackground(Color.BLACK);
		this.textArea.setForeground(Color.LIGHT_GRAY);
		this.textArea.setEditable(false);
		this.scrollPane = new JScrollPane(this.textArea);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(this.scrollPane, BorderLayout.CENTER);

		addTerminationHook();
		
		// Redirect the std. streams.
		redirectSystemStreams();
	}

	
	private void addTerminationHook() {
		final Thread mainThread = Thread.currentThread();
		$.manyTimes(new F0() {
			boolean warned = false;
			
			@Override
			public void f() {
				if(!mainThread.isAlive() && !this.warned) {
					System.out.println("[Terminated]");
					this.warned = true;					
				}
			}
		}, 500);
	}


	/**
	 * Shamelessly stolen from
	 * http://unserializableone.blogspot.com/2009/01/redirecting-systemout-and-systemerr-to.html
	 */
	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textArea.append(text);
			}
		});
	}

	/**
	 * Shamelessly stolen from
	 * http://unserializableone.blogspot.com/2009/01/redirecting-systemout-and-systemerr-to.html
	 */
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
		System.setErr(new PrintStream(out, true));
	}
}
