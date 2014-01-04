package tk.circuitcoder.grafixplane;

import java.util.Set;

/**
 * Represent A Mail...
 * @author CircuitCoder
 *
 */
public class Mail {
	public User sender;
	public User receiver;
	public String content;
	//TODO: attachment
	
	public static Set<Mail> getMailByReceiver(int UID) {
		return null;
	}
	
	public static Set<Mail> getMailBySender(int UID) {
		return null;
	}
	
	public static Set<Mail> getMailByUser(int SenderID,int ReceiverID) {
		return null;
	}
	
	public static Mail getMail(int MID) {
		return null;
	}
}