package tk.circuitcoder.grafixplane.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import tk.circuitcoder.grafixplane.user.User;

/**
 * Represents a single, read-only uploaded file on this server<br/>
 * Also provides some utility function relatives to File operation<br/>
 * No modification option for file since they're fixed after being uploaded
 * @author CircuitCoder
 */
public class File {
	//TODO: need a pool?
	private int FID;
	private String dir;
	private long createTime;
	private int ownerId;
	private User owner;
	
	private static PreparedStatement getFile;
	private static PreparedStatement newFile;
	private static PreparedStatement delFile;
	private static DateFormat TFormat;
	private static String contFolder;
	
	/**
	 * Get the ID of this file
	 * @return
	 */
	public int FID() {
		return FID;
	}
	
	/**
	 * Get the directory of this file
	 * @return the directory
	 */
	public String dir() {
		return dir;
	}
	
	/**
	 * Get the full name (base name + extend name) of this file
	 * @return the file name
	 */
	public String fileName() {
		int pos=dir.lastIndexOf(java.io.File.separator);
		if(pos>=0) return dir.substring(pos+1);
		else return dir;
	}
	
	/**
	 * Get the extend name of this file
	 * @return the file name
	 */
	public String extendName() {
		String fn=fileName();
		int pos=fn.lastIndexOf('.');
		if(pos>=0) return fn.substring(pos+1);
		else return ""; //No extend name
	}
	
	/**
	 * Get the base name of this file
	 * @return the file name
	 */
	public String baseName() {
		String fn=fileName();
		int pos=fn.lastIndexOf('.');
		if(pos>=0) return fn.substring(0,pos);
		else return fn; //No extend name
	}
	
	/**
	 * Get the create time of this file, in the form of millisecond from the epoch
	 * @return The create time
	 */
	public long createTime() {
		return createTime;
	}
	
	/**
	 * Get the create time of this file, in the form of converted Date object
	 * @return the Date object representing the create time
	 */
	public Date createDate() {
		return new Date(createTime);
	}
	
	/**
	 * Get the string formed create time of this file, formatted in default formatting style or specified style if there is one
	 * @return the String object representing the create time
	 */
	public String formattedTime() {
		return TFormat.format(new Date(createTime));
	}
	
	/**
	 * Get the id of the owner of this file
	 * @return the UID
	 */
	public int ownerID() {
		return ownerId;
	}
	
	/**
	 * Get the User object of the owner of this file
	 * @return the User object
	 * @throws SQLException when there were something going wrong while finding the user
	 */
	public User owner() throws SQLException {
		if(owner==null) owner=User.getUser(ownerId);
		return owner;
	}
	
	/**
	 * Get the input stream for this file
	 * @return the input stream
	 * @throws FileNotFoundException If the file was accidentally deleted or GrafixPlane is broken
	 */
	public InputStream getInputStream() throws FileNotFoundException {
		return new FileInputStream(new java.io.File(contFolder,ownerId+java.io.File.separatorChar+dir));
	}
	
	/**
	 * 
	 * @param FID
	 * @return The File object, or <tt>NULL</tt> if there is no file with this FID
	 * @throws SQLException 
	 */
	public static File getFile(int FID) throws SQLException {
		File result=new File();
		ResultSet rset;
		synchronized(getFile) {
			getFile.setInt(1,FID);
			rset=getFile.executeQuery();
		}
		if(!rset.first()) return null;
		result.FID=FID;
		result.dir=rset.getString(2);
		result.ownerId=rset.getInt(3);
		result.createTime=rset.getLong(4);
		result.owner=null;
		return result;
	}
	
	/**
	 * Set the format style when formatting create time
	 * @param format the format to feed into a SimpleDateFormat
	 * @see java.text.SimpleDateFormat
	 */
	public static void setTimeFormat(String format) {
		TFormat=new SimpleDateFormat(format);
	}
	
	public static void init(Connection conn) throws SQLException {
		getFile=conn.prepareStatement("SELECT * FROM FILE WHERE FID = ?");
		newFile=conn.prepareStatement("INSERT INTO FILE VALUES(?,?,?,?)");
		delFile=conn.prepareStatement("DELETE FROM FILE WHERE FID=?");
		TFormat=DateFormat.getDateInstance();	//After setting default locale
		contFolder="uploaded";	//TODO: preference
	}
	
	public static void clear() throws SQLException {
		getFile.close();
		newFile.close();
		delFile.close();
	}
}
