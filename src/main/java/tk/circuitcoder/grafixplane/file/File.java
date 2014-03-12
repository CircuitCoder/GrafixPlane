package tk.circuitcoder.grafixplane.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import tk.circuitcoder.grafixplane.GrafixPlane;
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
	
	/**
	 * Users who have permission to access this file
	 * |UID|UID|UID|.....|UID|
	 */
	private String accessilbe;
	
	private static PreparedStatement getFile;
	private static PreparedStatement newFile;
	private static PreparedStatement delFile;
	private static PreparedStatement checkFile;
	private static PreparedStatement getAll;
	private static PreparedStatement setAccessible;
	private static PreparedStatement getAccessible;
	private static DateFormat TFormat;
	private static String contFolder;
	
	private static Integer currentID;
	
	private File(int ID,String d,int own,long ct,String accstr) {
		FID=ID;
		dir=d;
		ownerId=own;
		createTime=ct;
		owner=null;
		accessilbe=accstr;
	}
	
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
		int pos=dir.lastIndexOf('/');
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
	 * Check if a provided user has permission to grant access to this file
	 * @param UID The user's ID
	 * @return Whether this user can access this file
	 */
	public boolean isAccessible(int UID) {
		try {
			updateAccessible();
		} catch (SQLException e) {
			e.printStackTrace();
			//DO nothing
		}
		return accessilbe.contains("|"+UID+"|");
	}
	
	/**
	 * Grant a single user the permission to access this file
	 * @param UID The id of that user
	 * @throws SQLException If something went wrong when trying to access the database
	 */
	public void addAccessible(int UID) throws SQLException {
		updateAccessible();
		if(!isAccessible(UID))
			accessilbe=accessilbe+UID+"|";
		saveAccessible();
	}
	
	/**
	 * Grant a set of users the permission to access this file
	 * @param UIDs A collection containing IDs of that set of users
	 * @throws SQLException If something went wrong when trying to access the database
	 */
	public void addAccessible(Collection<Integer> UIDs) throws SQLException {
		updateAccessible();
		StringBuilder builder=new StringBuilder(accessilbe);
		for(Integer i:UIDs) if(!isAccessible(i)) {	//Not in the accessable string yet
			builder.append(i).append('|');
		accessilbe=builder.toString();
		}
		saveAccessible();
	}
	
	//TODO: doc plz
	public void removeAccessible(int UID) throws SQLException {
		updateAccessible();
		accessilbe=accessilbe.replaceAll("\\|"+UID+"\\|","|");
		saveAccessible();
	}
	
	//TODO: optimize
	public void removeAccessible(Collection<Integer> UIDs) throws SQLException {
		updateAccessible();
		for(Integer i:UIDs) accessilbe=accessilbe.replaceAll("\\|"+i+"\\|","|");
		saveAccessible();
	}
	
	/**
	 * Get the input stream for this file
	 * @return the input stream
	 * @throws FileNotFoundException If the file was accidentally deleted or GrafixPlane is broken
	 */
	public InputStream getInputStream() throws FileNotFoundException {
		return new FileInputStream(new java.io.File(contFolder,ownerId+String.valueOf(FID)
				.replaceAll(java.io.File.pathSeparator,"").replaceAll("/",java.io.File.pathSeparator)));
	}
	
	/**
	 * Get the output stream for this file<br/>
	 * @return the output stream
	 * @throws FileNotFoundException If the file was accidentally deleted or GrafixPlane is broken
	 */
	public OutputStream getOutputStream() throws FileNotFoundException {
		return new FileOutputStream(new java.io.File(contFolder,ownerId+String.valueOf(FID)
				.replaceAll(java.io.File.pathSeparator,"").replaceAll("/",java.io.File.pathSeparator)));
	}
	
	private void updateAccessible() throws SQLException {
		synchronized (getAccessible) {
			getAccessible.setInt(1,FID);
			ResultSet rset=getAccessible.executeQuery();
			rset.first();
			accessilbe=rset.getString(1);
		}
	}
	
	private void saveAccessible() throws SQLException {
		synchronized (setAccessible) {
			setAccessible.setString(1,accessilbe);
			setAccessible.setInt(2,FID);
			setAccessible.execute();
		}
	}
	
	/**
	 * Get the File object for a file specified by its ID
	 * @param FID The file's ID
	 * @return The File object, or <tt>NULL</tt> if there is no file bundled to this FID
	 * @throws SQLException If something went wrong when trying to access the database
	 */
	public static File getFile(int FID) throws SQLException {
		ResultSet rset;
		synchronized(getFile) {
			getFile.setInt(1,FID);
			rset=getFile.executeQuery();
		}
		if(!rset.first()) return null;
		return new File(FID,rset.getString(2),rset.getInt(3),rset.getLong(4),rset.getString(5));
	}
	
	public static File newFile(int ownerID,String dir) throws SQLException {
		int thisID;
		long time=System.currentTimeMillis();
		String accstr=String.format("|%d|",ownerID);
		synchronized(currentID) {
			thisID=++currentID;
		}
		synchronized(newFile){
			newFile.setInt(1,thisID);
			newFile.setString(2,formatDir(dir));
			newFile.setInt(3,ownerID);
			newFile.setLong(4,time);
			newFile.setString(5,accstr);
			
			newFile.execute();
		}
		
		return new File(thisID,dir,ownerID,time,accstr);
	}
	
	/**
	 * Check if a user have a file matches the specified directory
	 * @param dir the directory
	 * @param owner the user
	 * @return Whether there is a file matches the specified directory
	 * @throws SQLException If something went wrong when trying to access the database
	 */
	public static boolean hasFileIn(String dir,int owner) throws SQLException {
		synchronized (checkFile) {
			checkFile.setInt(1,owner);
			try {
				checkFile.setString(2,formatDir(dir));
			} catch (IllegalArgumentException ex) {
				return false;
			}
			return checkFile.executeQuery().first();
		}
	}
	
	/**
	 * Format the directory string
	 * @param ori the input string
	 * @return The formatted directory string
	 * @throws IllegalArgumentException 
	 */
	public static String formatDir(String ori) throws IllegalArgumentException {
		//TODO: forbidden char set
		String dirs[]=ori.trim().split("/");
		for(int i=dirs.length-1;i>=0;--i) {
			if(dirs[i].equals("")) dirs[i]=null;
			else if(dirs[i].equals(".")) dirs[i]=null;
			else if(dirs[i].equals("..")) {
				int back=1;
				dirs[i]=null;
				while(back!=0) {
					--i;
					if(i<0) throw new IllegalArgumentException();
					if(dirs[i].equals("..")) ++back;
					else if(!((dirs[i].equals("")||dirs[i].equals(".")))) --back;
					dirs[i]=null;
				}
			}
		}
		StringBuilder builder=new StringBuilder();
		for(int i=0;i<dirs.length;i++) {
			if(dirs[i]!=null) builder.append('/').append(dirs[i]);
		}
		return builder.toString();
	}
	
	/**
	 * Get all file uploaded by a specified user with a specified prefix in it directory
	 * @param u The user's ID
	 * @param prefix The directory prefix, or an empty string for root directory
	 * @return The set of all that kind of files
	 * @throws SQLException If something went wrong when trying to access the database
	 */
	public static Set<File> getAllFile(int u,String prefix) throws SQLException {
		HashSet<File> result=new HashSet<File>();
		ResultSet rset;
		synchronized (getAll) {
			getAll.setInt(1,u);
			rset=getAll.executeQuery();
		}
		if(rset.first()) {
			do {
				String pathStr=rset.getString(2);
				if(pathStr.startsWith(prefix))
					result.add(new File(rset.getInt(1),rset.getString(2),rset.getInt(3),rset.getLong(4),rset.getString(5)));
			} while(rset.next());
		}
		
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
	
	public static void init(Connection conn,int cID) throws SQLException {
		getFile=conn.prepareStatement("SELECT * FROM FILE WHERE FID = ?");
		newFile=conn.prepareStatement("INSERT INTO FILE VALUES(?,?,?,?,?)");
		delFile=conn.prepareStatement("DELETE FROM FILE WHERE FID=?");
		checkFile=conn.prepareStatement("SELECT * FROM FILE WHERE(Owner = ? AND Dir = ?)");
		getAll=conn.prepareStatement("SELECT * FROM FILE WHERE Owner = ?");
		setAccessible=conn.prepareStatement("UPDATE FILE SET Accable = ? WHERE FID = ?");
		getAccessible=conn.prepareStatement("SELECT Accable FROM FILE WHERE FID = ?");
		
		TFormat=DateFormat.getDateInstance();	//After setting default locale
		contFolder="uploaded";	//TODO: preference
		currentID=cID;
	}
	
	public static void clear() throws SQLException {
		getFile.close();
		newFile.close();
		delFile.close();
		GrafixPlane.getGP().getConfig().setInt("FIDCount",currentID);
	}
}
