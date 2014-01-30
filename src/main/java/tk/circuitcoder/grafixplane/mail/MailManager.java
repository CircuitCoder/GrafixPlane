package tk.circuitcoder.grafixplane.mail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ListIterator;

import tk.circuitcoder.grafixplane.mail.Mailbox.WrappedMail;
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
		} else {
			result.boxCount=boxCount;
			result.boxes=(ArrayList<Mailbox>) Mailbox.getBoxes(owner, boxCount);
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
		synchronized (boxes) {
			if(boxes.get(boxCount-1).insert(mail,typeC,typeD)==capacity)	//Time to create a new box
				boxes.add(new Mailbox(UID,++boxCount));
		}
		++size;
	}
	
	public boolean toggleDel(int MID) {
		WrappedMail mail=getByID(MID);
		if(mail==null) return false;
		mail.toggleDel();
		return true;
	}
	
	public boolean toggleFlag(int MID) {
		WrappedMail mail=getByID(MID);
		if(mail==null) return false;
		mail.toggleFlag();
		return true;
	}
	
	public boolean toggleUnread(int MID) {
		WrappedMail mail=getByID(MID);
		if(mail==null) return false;
		mail.toggleUnread();
		return true;
	}
	
	public boolean remove(int MID) throws SQLException {
		synchronized (boxes) {
			for(int i=0;i<boxCount;i++) {
				int oriS=boxes.get(i).size();
				if(oriS!=boxes.get(i).remove(MID)) {
					if(oriS==1) {
						for(int j=i+1;j<boxCount;j++)	//NOT TESTED!
							boxes.get(j).decIndex();
						boxes.remove(i);
					}
					--size;
					return true;
				}
			}
		}
		return false;
	}
	
	public WrappedMail getMail(int index) {
		int mailC=0;
		synchronized (boxes) {
			for(int i=0;i<boxCount;i++)
				if((mailC+=boxes.get(i).size())>=index)
					return boxes.get(i).getMail(index-mailC+boxes.get(i).size());
		}
		return null;
	}
	
	public WrappedMail getByID(int MID) {
		synchronized (boxes) {
			for(int i=0;i<boxCount;i++) {
				WrappedMail m=boxes.get(i).getByID(MID);
				if(m!=null) return m;
			}
		}
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
		return getMails(begin, end,0,0,0);
	}
	
	/**
	 * Get all mails with specified status(negative: false,positive: true,zero: dosen't care) in range [begin,end)
	 * @param begin
	 * @param end
	 * @param unread
	 * @param deleted
	 * @param flagged
	 * @return
	 */
	public ArrayList<WrappedMail> getMails(int begin,int end,int unread,int deleted,int flagged) {
		if(begin>end||begin<0||end>size) return null;
		if(begin==end) return new ArrayList<WrappedMail>();
		
		ArrayList<WrappedMail> result=new ArrayList<WrappedMail>();
		synchronized (boxes) {
			ListIterator<Mailbox> it=boxes.listIterator();
			
			Mailbox current=it.next();
			while(begin-current.size()>=0) {
				begin-=current.size();
				end-=current.size();
				current=it.next();
			}

			while(begin<end) {
				if(begin-current.size()>=0) {
					begin-=current.size();
					end-=current.size();
					current=it.next();
				}
				WrappedMail mail=current.getMail(begin++);
				if(!((unread>0&&!mail.unread()||unread<0&&mail.unread())||
						(deleted>0&&!mail.deleted()||deleted<0&&mail.deleted())||
						(flagged>0&&!mail.flagged()||flagged<0&&mail.flagged())))
					result.add(mail);
			}
		}
		return result;
	}
	
	public void save() throws SQLException {
		synchronized (boxes) {
			for(Mailbox box:boxes)
				try {
					box.save();
				} catch (SQLException e) {
					getGP().getLogger().error("Error occured when saving mailbox:\n"+e.getLocalizedMessage());
				}
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
