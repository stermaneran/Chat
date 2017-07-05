package ChatClient;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import chatPackage.Package;

/**
 * The client
 * @author shay,dima and eran
 */
public class Client  {
	private ObjectInputStream sInput;    
	private ObjectOutputStream sOutput;    
	private Socket socket;
	private ClientGUI gui;
	private String server, username;
	private int port;

	/**
	 * Constructor call from gui
	 * @param server the server Address
	 * @param port the port number
	 * @param username the username
	 */
	Client(String server, int port, String username, ClientGUI gui) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.gui = gui;
	}

	/**
	 * To start the dialog between the client and the server
	 * @return true is succeed false if not
	 */
	public boolean start() {
		display("Connecting...");
		try {
			socket = new Socket(server, port);
		}
		catch(Exception ec) {
			display("Unable to connect to server check address and port number");
			return false;
		}
		display("Connected");
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}
		new Listen().start();
		try
		{
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exception in login : " + eIO);
			disconnect();
			return false;
		}
		return true;
	}

	/**
	 * To send a message to the gui
	 * @param msg is the message
	 * @return true if sending has succeed false if not 
	 */
	private void display(String msg) {
		if(gui != null){
			gui.append(msg + "\n");      
		}
	}

	/**    
	 * To send a message to the server
	 * @param msg is the message
	 */
	void sendMessage(Package msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	/**
	 * When something goes wrong
	 * Close the Input/Output streams and disconnect
	 */
	private void disconnect() {
		try {
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} 
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} 
		try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} 
		if(gui != null){
			gui.connectionFailed();
		}
	}

	/**
	 * A thread for the client
	 * @author shay,dima and eran
	 */
	class Listen extends Thread{
		Package cm;

		/**
		 * A thread to run the client
		 */
		public void run() {
			while(true) {
				try {
					cm = (Package) sInput.readObject();
				}
				catch(IOException e) {
					display("Connection closed");
					if(gui != null){
						gui.connectionFailed();
						break;
					}
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				String msg = cm.getMessage();
				switch(cm.getType()) 
				{
				case Package.MESSAGE:
					gui.append(msg);
					break;
				case Package.ONLINE_CLIENTS:
					String[] arr =msg.split("@");
					gui.setData(arr);
					break;
				case Package.LOGOUT:
					gui.append("Sorry, Username Already Taken. Please Choose Another \n");
					disconnect();
					break;
				}
			}
		}
	}
}