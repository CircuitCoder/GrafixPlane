package tk.circuitcoder.grafixplane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Represent a mailbox used to store mail information<br/>
 * A mailbox, stores a series of mail belonging to the same user in a string in the database<br/>
 * DB columns & data:
 * <table>
 * 	<thead><th>UID</th><th>Mails</th><th>BoxCount</th><th>MailCount</th></thead>
 *  <tbody><tr>
 *  	<td>The UID of this box's owner</td>
 *  	<td>The mail storage string</td>
 *  	<td>The index of this box</td>
 *  	<td>Indicates that how many mails are stored in this box</td>
 *  </tr></tbody>
 * </table><br/>
 * The mail storage string is a list of MailString connected end to end, which's syntax is<br/>
 * <tt>MID,status'Type@Box</tt><br/>
 * <strong>Status</strong>: Indicates whether this mail is unread(U), deleted(D), flagged(F), or so on<br/>
 * <strong>MID</strong>: The ID of this mail<br/>
 * <strong>Type</strong>: Indicates whether this mail is received as a personal mail (P),
 * a group mail (G GID), or a forwarded mail(F forwarder's UID)<br/>
 * <strong>Box</strong>: The displayed box this mail is in, in the ID form<br/>
 * And each MailString is followed by a |<br/><br/>
 * So all in all, a MailString should looks like this:<br/><br/>
 * <tt>2145,UF'F3@3|</tt>
 * @author CircuitCoder
 *
 */
public class Mailbox {
	public static class WrappedMail {
		private Mail mail;
		private char typeC;
		private int typeD;
		private boolean unread;
		private boolean deleted;
		private boolean flagged;
		private int box;
		
		private WrappedMail(int MID,char tc,int td,boolean u,boolean d,boolean f,int b) {
			mail=Mail.getMail(MID);
			typeC=tc;
			typeD=td;
			unread=u;
			deleted=d;
			flagged=f;
			box=b;
		}
		
		private WrappedMail() {}
		
		public Mail getMail() {return mail;}
		public char getType() {return typeC;}
		public int getTData() {return typeD;}
		public boolean unread() {return unread;}
		public boolean deleted() {return deleted;}
		public boolean flagged() {return flagged;}
		public int getBox() {return box;}
		//TODO: Box class
	}
	
	private static PreparedStatement getBox;
	private static PreparedStatement saveBox;
	private static PreparedStatement newBox;
	
	private boolean isNew;
	private ArrayList<WrappedMail> seq;
	private final int UID;
	private int mailCount;
	private final int boxCount;
	
	private Mailbox(int UID,int index) {
		this(false,UID,index);
	}
	
	private Mailbox(boolean isNew,int UID,int index) {
		this.UID=UID;
		this.isNew=isNew;
		if(isNew) mailCount=0;
		this.boxCount=index;
		seq=new ArrayList<WrappedMail>();
	}
	
	public void save() throws SQLException {
		if(isNew) {
			newBox.setInt(1,UID);
			newBox.setString(2, this.toString());
			newBox.setInt(3,boxCount);
			newBox.setInt(4,mailCount);
			newBox.executeUpdate();
			isNew=false;
		}
		else {
			saveBox.setString(1,this.toString());
			saveBox.setInt(2,mailCount);
			saveBox.setInt(3,UID);
			saveBox.setInt(4,boxCount);
			saveBox.executeUpdate();
		}
	}
	
	@Override
	public void finalize() {
		try {
			save();
		} catch (SQLException e) {
			e.printStackTrace();
			//TODO: handle this
		}
	}
	
	private void readMail(String mailString) {
		seq.add(parseMail(mailString));
	}
	
	/**
	 * Add a new mail into this mailbox
	 * @param mail The mail to be added
	 * @return The mail count of this mailbox
	 */
	public int addMail(WrappedMail mail) {
		seq.add(mail);
		return ++mailCount;
	}
	
	public WrappedMail getMail(int index) {
		return seq.get(index);
	}
	
	@Override
	public String toString() {
		String str="";
		for(int i=0;i<mailCount;i++) str+=genMailStr(seq.get(i));
		return str;
	}

	public static String genMailStr(WrappedMail wmail) {
		String str="";
		str+=String.valueOf(wmail.mail.MID)+',';
		if(wmail.unread) str+='U';
		if(wmail.deleted) str+='D';
		if(wmail.flagged) str+='F';
		str+="\'"+wmail.typeC;
		if(wmail.typeD!=Integer.MIN_VALUE) str+=String.valueOf(wmail.typeD);
		str+="@"+wmail.box+'|';
		return str;
	}
	
	public static WrappedMail parseMail(String mailString) {
		int pointer=mailString.indexOf(',');
		WrappedMail mail=new WrappedMail();
		mail.mail=Mail.getMail(Integer.parseInt(mailString.substring(0,	 pointer)));

		char s;
		while((s=mailString.charAt(++pointer))!='\'') {
			if(s=='U') mail.unread=true;
			else if(s=='D') mail.deleted=true;
			else if(s=='F') mail.flagged=true;
		}
		mail.typeC=mailString.charAt(++pointer);
		try {
			mail.typeD=Integer.parseInt(mailString.substring(pointer+1, (pointer=mailString.indexOf('@'))));
		} catch(NumberFormatException e) {	//No additional data
			mail.typeD=Integer.MIN_VALUE;
		}
		mail.box=Integer.parseInt(mailString.substring(pointer+1,mailString.length()-1));
		
		return mail;
	}
	
	public static Mailbox parseBox(int UID,int index,String mails,int mailCount) {
		String mailstrs[]=mails.split("|");
		Mailbox result=new Mailbox(UID,index);
		result.mailCount=mailCount;
		for(int i=0;i<mailCount;i++) 
			result.readMail(mailstrs[i]);
		return result;
	}
	
	public static Mailbox getBox(int uid,int index) throws SQLException {
		getBox.setInt(1, uid);
		getBox.setInt(2, index);
		ResultSet resultSet=getBox.executeQuery();
		Mailbox result=parseBox(uid,index,resultSet.getString(2), resultSet.getInt(4));
		return result;
	}
	
	public static void init(Connection conn) throws SQLException {
		getBox=conn.prepareStatement("SELECT * FROM MAILBOX WHERE UID = ? AND BoxCount = ?");
		saveBox=conn.prepareStatement("UPDATE MAILBOX SET Mails = ?, MailCount = ? WHERE UID = ? AND BoxCount = ?");
		newBox=conn.prepareStatement("INSERT INTO MAILBOX VALUES (?,?,?,?)");
	}
}
