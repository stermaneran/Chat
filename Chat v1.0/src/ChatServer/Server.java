package ChatServer;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import chatPackage.Package;

/**
 * The server
 * @author shay,dima and eran
 */
public class Server {
	public ArrayList<ClientThread> users;
	private static int uniqueId;
	private ServerGUI gui;
	private SimpleDateFormat sdf;
	private int port;
	private boolean keepGoing ;

	/** 
	 * server constructor that receive the port to listen to for connection
	 * @param gui the Server GUI
	 * @param port the port number
	 */
	public Server(int port, ServerGUI gui) {
		this.gui = gui;
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss");
		users = new ArrayList<ClientThread>();
	}

	/**
	 * To start the server
	 */
	public void start() {
		keepGoing = true;
		display("Server","Starting server on port " + this.port);
		try
		{
			ServerSocket serverSocket = new ServerSocket(port);
			while(keepGoing)
			{
				display("Server", "Waiting for Clients");
				Socket socket = serverSocket.accept();     
				if(!keepGoing)
					break;
				ClientThread thread = new ClientThread(socket);
				if(addClient(thread)) thread.start();
			}
			try {
				serverSocket.close();
				for(int i = 0; i < users.size(); ++i) {
					ClientThread threadTemp = users.get(i);
					try {
						threadTemp.sInput.close();
						threadTemp.sOutput.close();
						threadTemp.socket.close();
					}
					catch(IOException ioE) {
					}
				}
			}
			catch(Exception e) {
				display("Server","Exception closing the server and clients: " + e);
			}
		}
		catch (IOException e) {
			String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display("Server",msg);
		}
	}

	/**
	 * For the gui to stop the server
	 */
	@SuppressWarnings("resource")
	protected void stop() {
		keepGoing = false;
		// connect to myself as Client to exit statement
		// Socket socket = serverSocket.accept();
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
		}
	}

	/**
	 * Display an event to the the gui
	 * @param sender is the sender
	 * @param msg is the message
	 */
	private void display(String sender, String msg) {
		String time = sdf.format(new Date())+" ["+sender+"]: "+msg;
		gui.appendEvent(time + "\n");
	}

	/**
	 * Check if client name not occupied, if not add the new client 
	 * to the server data
	 * @param c the client
	 * @return true is succeed to add client false if not
	 */
	private synchronized boolean addClient(ClientThread c){
		for(int i = users.size(); --i >= 0;) {
			ClientThread ct = users.get(i);
			if(c.username.equals(ct.username)){
				c.writeMsg(new Package(Package.LOGOUT,""));
				display("Server","Duplicate username request: Disconnect Client- "+ct.username);
				return false;
			}
		}
		users.add(c);
		syncData();
		return true;
	}

	/**
	 * To send a message to a specific client
	 * @param message the message
	 * @param send the sender of the message
	 * @param receive the receiver of the message
	 */
	private synchronized void privateMessage(String message,String send ,String receive){
		String time = sdf.format(new Date());
		String messageLf = time + " "+"[PRIVATE]"+" " + message + "\n";
		for(int i = users.size(); --i >= 0;) {
			ClientThread ct = users.get(i);
			if(ct.username.equalsIgnoreCase(send)||ct.username.equalsIgnoreCase(receive)){
				if(!ct.writeMsg(new Package(Package.MESSAGE,messageLf))) {
					users.remove(i);
					syncData();
					display("Server","Disconnected Client " + ct.username + " removed from list.");
				}
			}
		}
	}

	/**
	 * Send client list to all clients
	 */
	private synchronized void syncData(){
		display("Server","Transferring database to clients...");
		String clients="ALL USERS    ";
		for(int j = 0; j < users.size(); ++j) {
			clients+="@"+users.get(j).username;
		}
		for(int i = users.size(); --i >= 0;) {
			ClientThread ct = users.get(i);
			if(!ct.writeMsg(new Package(Package.ONLINE_CLIENTS, clients))){
				users.remove(i);
				syncData();
				display("Server","Disconnected Client " + ct.username + " removed from list.");
			}	
		}
		display("Server","Database transfer complete");
	}

	/**
	 *  to send a message to all Clients
	 */
	private synchronized void broadcast(String message) {
		String time = sdf.format(new Date());
		String messageLf = time + " " + message + "\n";
		for(int i = users.size(); --i >= 0;) {
			ClientThread ct = users.get(i);
			if(!ct.writeMsg(new Package(Package.MESSAGE, messageLf))) {
				users.remove(i);
				syncData();
				display("Server","Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}

	/**
	 * Disconnecting the client who log off using the LOGOUT
	 * and update the active clients
	 * @param id the client id
	 */
	synchronized void remove(int id) {
		for(int i = 0; i < users.size(); ++i) {
			ClientThread ct = users.get(i);
			if(ct.id == id) {
				users.remove(i);
				syncData();
				return;
			}
		}
	}

	/** 
	 * One thread will run for each client 
	 * @author shay,dima and eran
	 */
	class ClientThread extends Thread {
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		int id;
		String username;
		Package cm;
		String date;

		/** 
		 * Constructor ClientThread receiving a socket number
		 * @param socket the socket
		 */
		ClientThread(Socket socket) {
			id = ++uniqueId;
			this.socket = socket;
			try
			{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				username = (String) sInput.readObject();
				display("Client",username + " just connected");
			}
			catch (IOException e) {
				display("Server","Exception creating new Input/output Streams: " + e);
				return;
			}
			catch (ClassNotFoundException e) {
			}
			date = new Date().toString() + "\n";
		}

		/** 
		 * A thread to run the server
		 */
		public void run() {
			boolean keepGoing = true;
			while(keepGoing) {
				try {
					cm = (Package) sInput.readObject();
				}
				catch (IOException e) {
					display("Server",username + " Exception reading Streams: " + e);
					break;             
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				String message = cm.getMessage();
				switch(cm.getType()) 
				{
				case Package.MESSAGE:
					broadcast(username + ": " + message);
					break;
				case Package.PRIVATE_MESSAGE:
					privateMessage(username  + ": " + message,cm.getSender(),cm.getReceive());
					break;
				case Package.LOGOUT:
					display("Client",username + " disconnected");
					keepGoing = false;
					break;
				}
			}
			remove(id);
			close();
		}

		/** 
		 * close socket, ObjectOutputStream and ObjectInputStream
		 */
		private void close() {
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		/** 
		 * Write a String to the Client output stream
		 * @param cm the message in Package form
		 * @return true if sending has succeed false if not 
		 */
		private boolean writeMsg(Package cm) {
			if(!socket.isConnected()) {
				close();
				return false;
			}
			try {
				sOutput.writeObject(cm);
			}
			catch(IOException e) {
				display("Server","Error sending message to " + username+"\n"+e.toString());
			}
			return true;
		}
	}
}