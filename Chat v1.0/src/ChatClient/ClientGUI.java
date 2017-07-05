package ChatClient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;
import chatPackage.Package;

/** 
 * The Client GUI
 * @author shay,dima and eran
 */
public class ClientGUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 12222L;
	private JLabel label;
	private JTextField tf;
	private JTextField tfServer, tfPort;
	private JButton login, logout;
	private JTextArea ta;
	private boolean connected;
	private Client client;
	private int defaultPort;
	private String defaultHost, username,dataDef[];
	private JList<String> list;
	private JScrollPane scrollPane;

	/** 
	 * Constructor connection receiving a socket number and build gui
	 * @param host the server ip
	 * @param port the port number
	 */
	ClientGUI(String host, int port) {
		super("Chat client");
		this.dataDef=new String[1];
		dataDef[0]="ALL USERS    ";
		getContentPane().setBackground(new Color(255, 255, 255));
		setResizable(false);
		defaultPort = port;
		defaultHost = host;

		// The NorthPanel:
		JPanel northPanel = new JPanel(new GridLayout(3,1));
		northPanel.setBorder(new EmptyBorder(0, 10, 6, 10));
		JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
		serverAndPort.setBorder(new EmptyBorder(8, 0, 0, 0));
		serverAndPort.setBackground(new Color(153, 204, 204));
		tfServer = new JTextField(host);
		tfPort = new JTextField(""+port);
		tfPort.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel label_1 = new JLabel("Host:");
		label_1.setHorizontalAlignment(SwingConstants.RIGHT);
		serverAndPort.add(label_1);
		serverAndPort.add(tfServer);
		JLabel label_2 = new JLabel("Port:");
		label_2.setHorizontalAlignment(SwingConstants.RIGHT);
		serverAndPort.add(label_2);
		serverAndPort.add(tfPort);
		serverAndPort.add(new JLabel(" "));
		northPanel.add(serverAndPort);
		label = new JLabel("Enter your username", SwingConstants.CENTER);
		northPanel.add(label);
		tf = new JTextField("Name");
		tf.setBackground(new Color(255, 255, 255));
		northPanel.add(tf);
		northPanel.setBackground(new Color(153, 204, 204));
		getContentPane().add(northPanel, BorderLayout.NORTH);

		//Text Area 
		ta = new JTextArea(15, 39);
		ta.setTabSize(15);
		JPanel centerPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) centerPanel.getLayout();
		flowLayout.setAlignOnBaseline(true);
		centerPanel.setBackground(new Color(0, 51, 51));
		centerPanel.setBorder(new EmptyBorder(0, 6, 0, 0));
		centerPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		getContentPane().add(centerPanel, BorderLayout.CENTER);

		//the user list	
		JPanel right = new JPanel();
		right.setBorder(new EmptyBorder(4, 0, 0, 0));
		right.setBackground(new Color(0, 51, 51));
		getContentPane().add(right,BorderLayout.EAST);
		scrollPane = new JScrollPane();
		right.add(scrollPane);
		list = new JList<String>(dataDef);
		list.setToolTipText("Online users");
		scrollPane.setViewportView(list);
		list.setVisibleRowCount(13);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// the 2 buttons
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);      
		JPanel southPanel = new JPanel();
		southPanel.setBackground(new Color(153, 204, 204));
		southPanel.add(login);
		southPanel.add(logout);
		getContentPane().add(southPanel, BorderLayout.SOUTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(530, 430);
		setVisible(true);
		tf.requestFocus();
	}

	/** 
	 * display active clients
	 * @param arr the active clients
	 */
	void setData(String arr[]){
		list.setListData(arr);
	}

	/** 
	 * called by the Client to append text in the TextArea
	 * @param str the message to append
	 */
	void append(String str) {
		ta.append(str);
		ta.setCaretPosition(ta.getText().length() - 1);
	}

	/** 
	 * Reset gui
	 * called by the GUI if the connection failed
	 */
	void connectionFailed() {
		setTitle("Chat client");
		setData(dataDef);
		login.setEnabled(true);
		logout.setEnabled(false);
		label.setText("Enter your username");
		tf.setText("Name");
		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);
		tfServer.setEditable(true);
		tfPort.setEditable(true);
		tf.removeActionListener(this);
		connected = false;
	}

	/** 
	 * Actions to do if Button or JTextField clicked
	 */
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o == logout) {
			client.sendMessage(new Package(Package.LOGOUT, ""));
			return;
		}
		if(connected && tf.getText().length()>100){
			ta.append("max. message length 100 characters \n");
			return;
		}
		if(connected&&(list.isSelectionEmpty()||list.isSelectedIndex(0))) {
			client.sendMessage(new Package(Package.MESSAGE, tf.getText()));            
			tf.setText("");
			return;
		}
		if(connected) {
			String to=(String) list.getSelectedValue();
			if(username.equals(to)) return;
			client.sendMessage(new Package(Package.PRIVATE_MESSAGE, tf.getText(),username,to));    
			tf.setText("");
			return;
		}
		if(o == login) {
			this.username = tf.getText().trim();
			if(username.length() == 0)
				return;
			if(username.length() > 9){
				ta.append("Max. username length 9 characters \n");
				return;
			}
			if(username.contains("@")){
				ta.append("invalid characters in username\n");
				return;
			}
			String server = tfServer.getText().trim();
			if(server.length() == 0)
				return;
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				return;   
			}
			client = new Client(server, port, username, this);
			if(!client.start())
				return;
			setTitle("Chat client: "+this.username);
			tf.setText("");
			label.setText("Enter your message");
			connected = true;
			login.setEnabled(false);
			logout.setEnabled(true);
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			tf.addActionListener(this);
		}
	}

	/*
	 * start the Client gui
	 */
	public static void main(String[] args) {
		new ClientGUI("localhost", 5060);
	}
}