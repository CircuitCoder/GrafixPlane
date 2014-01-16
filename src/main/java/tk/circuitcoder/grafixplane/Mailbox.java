package tk.circuitcoder.grafixplane;

import java.util.ArrayList;

/**
 * Represent a mailbox used to store mail information<br/>
 * A mailbox, stores a series of mail belonging to the same user in a string in the database<br/>
 * DB columns & data:
 * <table>
 * 	<thead><th>UID</th><th>Mails</th><th>BoxCounter</th><th>MailCount</th></thead>
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
	
	private ArrayList<WrappedMail> seq;
	
	private Mailbox() {
		seq=new ArrayList<WrappedMail>();
	}
	
	private void addMail(String mailString) {
		seq.add(parseMail(mailString));
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
	
	public static Mailbox parseBox(String mails) {
		String mailstrs[]=mails.split("|");
		Mailbox result=new Mailbox();
		for(int i=0;i<mailstrs.length-1;i++) 
			result.addMail(mailstrs[i]);
		return result;
	}
}
