package tk.circuitcoder.grafixplane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static tk.circuitcoder.grafixplane.GrafixPlane.*;

/**
 * Represent A Mail...
 * @author CircuitCoder
 *
 */
public class Mail {
	public int MID;
	public int sender;
	public Set<Integer> receivers;
	public String subject;
	public String content;
	public Timestamp sentTime;
	public String attachments;
	public int replyTo;
	
	private static PreparedStatement mailByID;
	private static PreparedStatement newMail;
	private static PreparedStatement mailBySender;
	
	private static Map<Integer,Mail> mailPool;

	private Mail() {
		receivers=new HashSet<Integer>();
	}
	
	public static Set<Mail> getMailBySender(int UID) throws SQLException {
		mailBySender.setInt(1,UID);
		ResultSet resultSet=mailBySender.executeQuery();
		return getMails(resultSet);
	}
	
	public static Mail getMail(int MID) {
		try {
			if(mailPool.containsKey(MID)) return mailPool.get(MID);
			
			mailByID.setInt(1, MID);
			ResultSet result=mailByID.executeQuery();
			if(!result.first()) return null;
			return getMail(result);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Mail send(User sender,Set<Integer> receivers,String subject,String content) throws SQLException {
		//TODO: attachment
		return newMail(sender.getUID(),receivers,subject,content,"",0).insertAll();
	}
	
	public static Mail reply(User sender,Mail ori,String subject,String content) throws SQLException {
		HashSet<Integer> rec=new HashSet<Integer>();
		rec.add(ori.sender);
		return newMail(sender.getUID(),rec,subject,content,"",ori.MID).insertAll();
	}
	
	private static synchronized Mail newMail(int sender,Set<Integer> receivers,String subject,String content,String attachments,int replyTo) throws SQLException {
		int mid=getGP().getConfig().getInt("mailCount")+1;
		getGP().getConfig().setInt("mailCount", mid);
		String rec="";
		for(Integer i:receivers) rec+=i.toString()+"|";
		
		newMail.setInt(1,mid);
		newMail.setInt(2,sender);
		newMail.setString(3,rec);
		newMail.setString(4,subject);
		newMail.setString(5,content);
		newMail.setString(6, attachments);
		newMail.setInt(7, replyTo);
		
		int updateCount=newMail.executeUpdate();
		if(updateCount!=1) return null;	//Not sent
		
		Mail result=new Mail();
		result.MID=mid;
		result.receivers=receivers;
		result.sender=sender;
		result.subject=subject;
		result.content=content;
		result.attachments=attachments;
		result.replyTo=replyTo;
		
		mailPool.put(mid,result);
		return result;
	}
	
	private Mail insertAll() throws SQLException {
		for(Integer i:receivers) {
			User receiver=User.getUser(i);
			receiver.recMail(this);
		}
		return this;
	}
	
	private static Mail getMail(ResultSet resultSet) throws SQLException {
		Mail mail=new Mail();
		mail.MID=resultSet.getInt(1);
		mail.sender=resultSet.getInt(2);
		
		String receivers[]=resultSet.getString(3).split("\\|",-1);
		for(int i=0;i<receivers.length-1;i++)
			mail.receivers.add(Integer.parseInt(receivers[i]));
		
		mail.subject=resultSet.getString(4);
		mail.content=resultSet.getString(5);
		mail.attachments=resultSet.getString(6);
		mail.replyTo=resultSet.getInt(7);
		
		return mail;
	}
	
	private static Set<Mail> getMails(ResultSet resultSet) throws SQLException {
		if(!resultSet.first()) return null;
		Set<Mail> mails=new HashSet<Mail>();
		int mid;
		do {
			if(mailPool.containsKey(mid=resultSet.getInt(1))) {
				mails.add(mailPool.get(mid));
			} else {
				Mail m=getMail(resultSet);
				mails.add(m);
				mailPool.put(mid,m);
			}
		} while(resultSet.next());
		return mails;
	}
	
	public static void init(Connection conn) throws SQLException {
		mailByID=conn.prepareStatement("SELECT * FROM MAIL WHERE(MID=?)");
		newMail=conn.prepareStatement("INSERT INTO MAIL VALUES(?,?,?,?,?,?,?)");
		mailBySender=conn.prepareStatement("SELECT * FROM MAIL WHERE(Sender = ?)");
		mailPool=new HashMap<Integer,Mail>();
	}
	
	public static void clear() {
	}
}