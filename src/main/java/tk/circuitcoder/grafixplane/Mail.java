package tk.circuitcoder.grafixplane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * Represent A Mail...
 * @author CircuitCoder
 *
 */
public class Mail {
	public int MID;
	public User sender;
	public Set<User> receivers;
	public String content;
	public Timestamp sentTime;
	public String attachments;
	public int replyTo;
	
	private static PreparedStatement mailByID;

	private Mail() {
		receivers=new HashSet<User>();
	}
	
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
		try {
			mailByID.setInt(1, MID);
			ResultSet result=mailByID.executeQuery();
			if(!result.first()) return null;
			return getMail(result);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static Mail getMail(ResultSet resultSet) throws SQLException {
		Mail mail=new Mail();
		mail.MID=resultSet.getInt(1);
		mail.sender=User.getUser(resultSet.getInt(2));
		
		String receivers[]=resultSet.getString(3).split("\\|");
		for(int i=0;i<receivers.length;i++)
			mail.receivers.add(User.getUser(Integer.parseInt(receivers[i])));
		
		mail.content=resultSet.getString(4);
		mail.attachments=resultSet.getString(5);
		mail.replyTo=resultSet.getInt(6);
		
		return mail;
	}
	
	private static Set<Mail> getMails(ResultSet resultSet) throws SQLException {
		if(!resultSet.first()) return null;
		Set<Mail> mails=new HashSet<Mail>();
		do {
			mails.add(getMail(resultSet));
		} while(resultSet.next());
		return mails;
	}
	
	public static void init(Connection conn) throws SQLException {
		mailByID=conn.prepareStatement("SELECT * FROM MAIL WHERE(MID=?)");
	}
}