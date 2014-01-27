package tk.circuitcoder.grafixplane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ListIterator;

import tk.circuitcoder.grafixplane.Mailbox.WrappedMail;
import static tk.circuitcoder.grafixplane.GrafixPlane.*;

/**
 * Manages the mail boxes, receives new mails or sent the new mails for its owner
 * @author CircuitCoder
 *
 */
public class MailManager {
	private ArrayList<Mailbox> boxes;
	private int boxCount;
	private int UID;
	
	/**
	 * The number of mails in total
	 */
	private int size;
	
	/**
	 * The maximum mail count of a mailbox
	 */
	private static int capacity;
	
	private static PreparedStatement saveBoxCount;
	
	private MailManager() {
		size=0;
	}
	public static MailManager getManager(int owner,int boxCount) throws SQLException {
		MailManager result=new MailManager();
		result.UID=owner;
		if(boxCount==0) {
			result.boxes=new ArrayList<Mailbox>();
			result.boxes.add(new Mailbox(owner,1));
			result.boxCount=1;
			result.size=0;
		} else {
			result.boxCount=boxCount;
			result.boxes=Mailbox.getBoxes(owner, boxCount);
			for(Mailbox box:result.boxes) result.size+=box.size();
		}
		return result;
	}
	
	/**
	 * get the number of mails in total
	 * @return The number of mails in total
	 */
	public int size() {
		return size;
	}
	
	public void insert(Mail mail,char typeC,int typeD) {
		if(boxes.get(boxCount-1).insert(mail,typeC,typeD)==capacity)	//Time to create a new box
			boxes.add(new Mailbox(UID,++boxCount));
		++size;
	}
	
	public WrappedMail getMail(int index) {
		int mailC=0;
		for(int i=0;i<boxCount;i++)
			if((mailC+=boxes.get(i).size())>=index)
				return boxes.get(i).getMail(index-mailC+boxes.get(i).size());
		return null;
	}
	
	/**
	 * begin: Poster integer
	 * [begin,end)
	 * @param begin
	 * @param end
	 * @return
	 */
	public ArrayList<WrappedMail> getMails(int begin,int end) {
		if(begin>end||begin<0||end>size) return null;
		if(begin==end) return new ArrayList<WrappedMail>();
		
		ArrayList<WrappedMail> result=new ArrayList<WrappedMail>();
		int mailC=0;
		ListIterator<Mailbox> it=boxes.listIterator();
		
		Mailbox current=it.next();
		while(mailC+current.size()<=begin) {
			mailC+=current.size();
			current=it.next();
		}
		
		end-=mailC;
		int pointer=begin-mailC;

		while(pointer<end) {
			if(pointer>=current.size()) {
				pointer-=current.size();
				end-=current.size();
				current=it.next();
			}
			result.add(current.getMail(pointer++));
		}
		
		return result;
	}
	
	public void save() throws SQLException {
		for(Mailbox box:boxes)
			try {
				box.save();
			} catch (SQLException e) {
				getGP().getLogger().error("Error occured when saving mailbox:\n"+e.getLocalizedMessage());
			}
		saveBoxCount.setInt(1,boxCount);
		saveBoxCount.setInt(2,UID);
		saveBoxCount.execute();
	}
	
	public static void init(Connection conn) throws SQLException {
		capacity=getGP().getConfig().getInt("BoxCapacity");
		saveBoxCount=conn.prepareStatement("UPDATE USER SET BoxCount = ? WHERE UID = ?");
	}
}
