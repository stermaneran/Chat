package ChatServer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/** 
 * The Server GUI
 * @author shay,dima and eran
 */
public class ServerGUI extends JFrame implements ActionListener, WindowListener {
	private static final long serialVersionUID = 1L;
	private JButton stopStart;
	private JTextArea event;
	private JTextField tPortNumber;
	private Server server;

	/** 
	 * Constructor to set port and build gui
	 * @param port the port number
	 */
	ServerGUI(int port) {
		super("Chat Server");
		server = null;
		JPanel north = new JPanel();
		stopStart = new JButton("Start");
		stopStart.addActionListener(this);
		north.add(stopStart);
		north.add(new JLabel("Port: "));
		tPortNumber = new JTextField("  " + port);
		north.add(tPortNumber);
		getContentPane().add(north, BorderLayout.NORTH);

		// the event win
		JPanel center = new JPanel(new GridLayout(1,1));
		center.setBorder(new EmptyBorder(0, 3, 3, 3));
		event = new JTextArea(10,10);
		event.setForeground(Color.WHITE);
		event.setBackground(Color.BLACK);
		event.setEditable(false);
		center.add(new JScrollPane(event));	
		getContentPane().add(center);			
		addWindowListener(this);
		setSize(500, 280);
		setVisible(true);
	}		

	/** 
	 * Append text in the TextArea
	 * @param str the message to append
	 */
	void appendEvent(String str) {
		event.append(str);
		event.setCaretPosition(event.getText().length() - 1);
	}

	/** 
	 * Actions to do if start or stop where clicked
	 */
	public void actionPerformed(ActionEvent e) {
		if(server != null) {
			server.stop();
			server = null;
			tPortNumber.setEditable(true);
			stopStart.setText("Start");
			return;
		}
		int port;
		try {
			port = Integer.parseInt(tPortNumber.getText().trim());
			if(port<1023||port>49151||port==1433) throw new Exception();
		}
		catch(Exception er) {
			appendEvent("Invalid port number\n");
			return;
		}
		server = new Server(port, this);
		new ServerRunning().start();
		stopStart.setText("Stop");
		tPortNumber.setEditable(false);
	}

	/*
	 * start the Server gui
	 */
	public static void main(String[] arg) {
		new ServerGUI(5060);
	}

	/** 
	 * If the client click the X button to close the application
	 * close the connection with the server to free the port
	 */
	public void windowClosing(WindowEvent e) {
		if(server != null) {
			try {
				server.stop();			
			}
			catch(Exception eClose) {
			}
			server = null;
		}
		dispose();
		System.exit(0);
	}
	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}

	/** 
	 * A thread for Server gui
	 * @author shay,dima and eran
	 *
	 */
	class ServerRunning extends Thread {

		/** 
		 * A thread to run the server gui
		 */
		public void run() {
			server.start();        
			stopStart.setText("Start");
			tPortNumber.setEditable(true);
			appendEvent("Server crashed\n");
			server = null;
		}
	}

}