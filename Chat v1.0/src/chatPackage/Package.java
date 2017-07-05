package chatPackage;
import java.io.*;

/**
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Server
 * @author shay,dima and eran 
 */
public class Package implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * ONLINE_CLIENTS to receive the list of the users connected
	 */
	public static final int ONLINE_CLIENTS = 0;
	/**
	 * MESSAGE an ordinary message
	 */
	public static final int MESSAGE = 1;
	/**
	 * PRIVATE_MESSAGE a private message
	 */
	public static final int PRIVATE_MESSAGE = 2;
	/**
	 * LOGOUT to disconnect from the Server
	 */
	public static final int LOGOUT = 3;
	private int type;
	private String message,sender,receive;

	/**
	 * Constructor to build the Package message
	 * @param type the type of message
	 * @param message the message
	 */
	public Package(int type, String message) {
		this.type = type;
		this.message = message;
	}

	/**
	 * Constructor to build a Package for a PRIVATE message
	 * @param type the type of message
	 * @param message the message
	 * @param sender the sender of the message
	 * @param receive the receiver of the message
	 */
	public Package(int type, String message,String sender,String receive){
		this(type,message);
		this.sender = sender;
		this.receive = receive;
	}

	/**
	 * To get the type of message: ONLINE_CLIENTS = 0, MESSAGE = 1, PRIVATE_MESSAGE = 2, LOGOUT = 3
	 * @return the type of message 
	 */
	public int getType() {return type;}

	/**
	 * To get the sender of the message
	 * @return the sender of the message
	 */
	public String getSender() {return sender;}

	/**
	 * To get the receiver of the message
	 * @return the receiver of the message
	 */
	public String getReceive() {return receive;}

	/**
	 * To get the message
	 * @return the message
	 */
	public String getMessage() {return message;}
}