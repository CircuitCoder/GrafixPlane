package tk.circuitcoder.grafixplane.mail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
		private int MID;
		private Mail mail;
		private char typeC;
		private int typeD;
		private boolean unread;
		private boolean deleted;
		private boolean flagged;
		private int box;
		
		private WrappedMail(int MID,char tc,int td,boolean u,boolean d,boolean f,int b) {
			this.MID=MID;
			mail=null;
			typeC=tc;
			typeD=td;
			unread=u;
			deleted=d;
			flagged=f;
			box=b;
		}
		
		private WrappedMail() {}
		
		public int getMID() {
			return MID;
		}
		public Mail getMail() {
			if(mail!=null) return mail;
			else return (mail=Mail.getMail(MID));
		}
		public char getType() {return typeC;}
		public int getTData() {return typeD;}
		public boolean unread() {return unread;}
		public boolean deleted() {return deleted;}
		public boolean flagged() {return flagged;}
		public int getBox() {return box;}
		//TODO: Box class
		
		/**
		 * Set field <tt>unread</tt> to false
		 */
		public void toggleUnread() {
			unread=!unread;
		}
		
		public void toggleDel() {
			deleted=!deleted;
		}
		
		public void toggleFlag() {
			flagged=!flagged;
		}
		
		@Override
		public String toString() {
			String str="";
			str+=String.valueOf(MID)+',';
			if(unread) str+='U';
			if(deleted) str+='D';
			if(flagged) str+='F';
			str+="\'"+typeC;
			if(typeD!=Integer.MIN_VALUE) str+=String.valueOf(typeD);
			str+="@"+box+'|';
			return str;
		}
	}
	
	private static PreparedStatement getBox;
	private static PreparedStatement saveBox;
	private static PreparedStatement newBox;
	
	private boolean isNew;
	private ArrayList<WrappedMail> seq;
	private final int UID;
	private int mailCount;
	private int boxCount;
	
	/**
	 * Create a new mailbox<br/>
	 * force isNew property equals true
	 * @param UID UID of the box's owner
	 * @param index The box's index
	 */
	public Mailbox(int UID,int index) {
		this(true,UID,index);
	}
	
	private Mailbox(boolean isNew,int UID,int index) {
		this.UID=UID;
		this.isNew=isNew;
		if(isNew) mailCount=0;
		this.boxCount=index;
		seq=new ArrayList<WrappedMail>();
	}
	
	/**
	 * Get the size of this box
	 * @return The number of mails this box contains
	 */
	public int size() {
		return mailCount;
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
	
	/**
	 * Insert a new mail into this mailbox<br/>
	 * Called when a new mail is sent to the owner of this box
	 * @param imail The mail to be inserted into this box
	 * @return The number of mails in this box after invoking this method
	 */
	public int insert(Mail mail,char typeC,int typeD) {
		WrappedMail wmail=new WrappedMail(mail.MID, typeC, typeD, true, false, false, 0);
		wmail.mail=mail;
		seq.add(wmail);
		return ++mailCount;
	}
	
	public int remove(int MID) throws SQLException {
		WrappedMail mail;
		synchronized (seq) {
			int index=find(MID);
			if(index<0) return mailCount;	//Didn't remove anything
			mail=seq.remove(index);
		}
			if((--mail.mail.refCount)==0) {
				Mail.remove(mail.MID);
			}
			return --mailCount;
	}
	
	/**
	 * Find the index of a mail with a specified ID
	 * @param MID The specified MID
	 * @return the index of the mail,or <tt>-1</tt> if the mail wasn't found in this box
	 */
	public int find(int MID) {
		for(int i=0;i<mailCount;i++) if(seq.get(i).MID==MID) return i;
		return -1;
	}
	
	private void readMail(String mailString) {
		seq.add(parseMail(mailString));
	}
	
	/**
	 * Add a new mail into this mailbox
	 * @param mail The mail to be added
	 * @return The number of mails in this box after invoking this method
	 */
	public int addMail(WrappedMail mail) {
		seq.add(mail);
		return ++mailCount;
	}
	
	public WrappedMail getMail(int index) {
		if(index<0||index>=mailCount) return null;
		return seq.get(index);
	}
	
	public WrappedMail getByID(int MID) {
		int index=find(MID);
		if(index<0) return null;
		return seq.get(index);
	}
	
	public void decIndex() {
		--boxCount;
	}
	
	@Override
	public String toString() {
		String str="";
		for(int i=0;i<mailCount;i++) str+=seq.get(i).toString();
		return str;
	}
	
	public static WrappedMail parseMail(String mailString) {
		int pointer=mailString.indexOf(',');
		WrappedMail mail=new WrappedMail();
//		mail.mail=Mail.getMail(Integer.parseInt(mailString.substring(0,pointer)));
		mail.mail=null;
		mail.MID=Integer.parseInt(mailString.substring(0,pointer));	//lazy load

		char s;
		while((s=mailString.charAt(++pointer))!='\'') {
			if(s=='U') mail.unread=true;
			else if(s=='D') mail.deleted=true;
			else if(s=='F') mail.flagged=true;
		}
		mail.typeC=mailString.charAt(pointer+1);
		mail.typeD=Integer.parseInt(mailString.substring(pointer+2, (pointer=mailString.indexOf('@'))));
		mail.box=Integer.parseInt(mailString.substring(pointer+1,mailString.length()));
		
		return mail;
	}
	
	public static Mailbox parseBox(int UID,int index,String mails,int mailCount) {
		String mailstrs[]=mails.split("\\|");
		Mailbox result=new Mailbox(false,UID,index);
		result.mailCount=mailCount;
		for(int i=0;i<mailCount;i++) 
			result.readMail(mailstrs[i]);	//lazyLoad
		return result;
	}
	
	public static List<Mailbox> getBoxes(int uid,int boxCount) throws SQLException {
		getBox.setInt(1, uid);
		ResultSet resultSet=getBox.executeQuery();
		List<Mailbox> result=new ArrayList<Mailbox>(boxCount);
		if(!resultSet.first()) return result;
		do {
			result.add(parseBox(uid,resultSet.getInt(3),resultSet.getString(2), resultSet.getInt(4)));
		} while(resultSet.next());
		return result;
	}
	
	public static void init(Connection conn) throws SQLException {
		getBox=conn.prepareStatement("SELECT * FROM MAILBOX WHERE UID = ? ORDER BY BoxCount ASC");
		saveBox=conn.prepareStatement("UPDATE MAILBOX SET Mails = ?, MailCount = ? WHERE UID = ? AND BoxCount = ?");
		newBox=conn.prepareStatement("INSERT INTO MAILBOX VALUES (?,?,?,?)");
	}
}
