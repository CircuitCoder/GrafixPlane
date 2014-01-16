package tk.circuitcoder.grafixplane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import static tk.circuitcoder.grafixplane.GrafixPlane.*;

/**
 * Represents a GrafixPlane User</br>
 * Provides some utilities related to authorization and accounts
 * @author CircuitCoder
 * @since 0.0.1
 */
public class User { 
	private int UID;
	private String username;
	private AccessLevel accessLevel;
	private int boxCount;
	private HashSet<HttpSession> sessions;
	
	private static PreparedStatement userByID;
	private static PreparedStatement userByName;
	private static PreparedStatement newUser;
	
	private static HashMap<Integer,User> userPool;
	//TODO: clear user with no session logged in when ran out of memory
	
	public int getUID() {
		return UID;	
	}
	public String getUsername() {
		return username;
	}
	
	public AccessLevel getAccessLevel() {
		return accessLevel;
	}
	
	public boolean modifyPasswd(String passwd) throws SQLException {
		try {
			return modifyPasswd(UID, passwd);
		} catch (NameExistsException e) {
			//This shouldn't happen
			e.printStackTrace();
			GrafixPlane.stop();
			return false;
		}
	}
	
	/**
	 * Removes the session from its user's sessionList when it is about to invalidate automatically
	 * @author CircuitCoder
	 */
	public static class SessionTimeoutListener implements HttpSessionListener {
		@Override
		public void sessionCreated(HttpSessionEvent se) {}
		@Override
		public void sessionDestroyed(HttpSessionEvent se) {
			if(se.getSource() instanceof User) return;
			User u=(User) se.getSession().getAttribute("g_user");
			if(u!=null) u.sessions.remove(se.getSession());
		}
	}
	
	public static enum AccessLevel {
		ROOT(0,"Root User"),ADMIN(1,"Administrator");
		public final int value; //The value stored in the database, representing this level
		public final String display; //The string displayed in the account information page
		
		private AccessLevel(int v,String ds) {
			value=v;
			display=ds;
		}
		
		/**
		 * Get the AccessLevel object based on its value
		 * @param level The value of this level
		 * @return The AccessLevel object, or <em>null</em> if their are no level matches this value
		 */
		public static AccessLevel valueOf(int level) {
			for(AccessLevel al:AccessLevel.values()) if(al.value==level) return al;
			return null;
		}
	}
	
	public static class UIDExistsException extends Exception {
		private static final long serialVersionUID = GrafixPlane.VERSION;
	}
	
	public static class NameExistsException extends Exception {
		private static final long serialVersionUID = GrafixPlane.VERSION;
	}
	
	/**
	 * Get all mails received by this User
	 * @return The mails
	 */
	public Set<Mail> getReceivedMail() {
		return Mail.getMailByReceiver(UID);
	}
	
	/**
	 * Get all mails sent by this User
	 * @return The mails
	 */
	public Set<Mail> getSentMail() {
		return Mail.getMailBySender(UID);
	}
	
	/**
	 * Set the provided session's user with this user, and add it into this user's session set
	 * @param session The provided session
	 * @return <tt>true</tt> if this session hasn't login yet
	 */
	public boolean loginSession(HttpSession session) {
		if(session.getAttribute("g_user")!=null) return false;
		session.setAttribute("g_user", this);
		return sessions.add(session);
	}
	
	/**
	 * Remove the session away from this user's session set, and end the session
	 * @param session The provided session
	 * @return <tt>true</tt> if this session was logged in to this user
	 */
	public boolean logoutSession(HttpSession session) {
		if(!sessions.remove(session)) return false;
		session.invalidate();
		return true;
	}
	
	/**
	 * User instance can only be created by calling getUser()
	 */
	private User(int UID,String uname,AccessLevel level,int BoxCount) {
		this.UID=UID;
		this.username=uname;
		this.accessLevel=level;
		this.boxCount=BoxCount;
		this.sessions=new HashSet<HttpSession>();
	}
	
	/**
	 * Checks if the incoming request's session is logged in
	 * @param session The HTTP session
	 * @return <tt>true</tt> if this session is logged in
	 */
	public static boolean isLogined(HttpSession session) {
		return session.getAttribute("g_user")!=null;
	}
	
	/**
	 * Gets the current logged in user
	 * @param session The HTTP session
	 * @return The user of this session, or <strong>NULL</strong> if the session isn't currently logged in 
	 */
	public static User getCurrentUser(HttpSession session) {
		return (User) session.getAttribute("g_user");
	}
	
	/**
	 * Checks if the login information is valid
	 * @param uname The user name
	 * @param passwd The password
	 * @return Whether the user exists, and matches the password
	 */
	public static boolean verify(String uname,String passwd) {
		try {
			userByName.setString(1, uname);
			ResultSet result=userByName.executeQuery();
			if(!result.first()) return false;
			String storedPasswd=result.getString(3);
			if(storedPasswd==null||!storedPasswd.equalsIgnoreCase(passwd)) return false;
			else return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Gets a user's information and creates the User instance base on its name
	 * @param uname The user's name
	 * @return The created instance, or Null if the user is not found or some unexpected error occurred
	 * @throws SQLException If there are some thing wrong with the database
	 */
	public static User getUser(String uname) throws SQLException {
		userByName.setString(1, uname);
		ResultSet resultSet=userByName.executeQuery();
		if(!resultSet.first()) return null;
		int UID=resultSet.getInt(1);
		if(userPool.containsKey(UID)) return userPool.get(UID);
		
		User newUser=new User(resultSet.getInt(1),
				uname,
				AccessLevel.valueOf(resultSet.getInt(4)),
				resultSet.getInt(5));
		userPool.put(UID, newUser);
		return newUser;
	}
	
	/**
	 * Gets a user's information and creates the User instance base on its ID<br/>
	 * If there this user has been logged in, and still stored in the user pool, just return the exact same user
	 * @param UID The user's ID
	 * @return The created instance, or Null if the user is not found
	 * @throws SQLException If there are some thing wrong with the database
	 */
	public static User getUser(int UID) throws SQLException {
		if(userPool.containsKey(UID)) return userPool.get(UID);
		
		userByID.setInt(1, UID);
		ResultSet resultSet=userByID.executeQuery();
		if(!resultSet.first()) return null;
		
		User newUser=new User(UID,
				resultSet.getString(2),
				AccessLevel.valueOf(resultSet.getInt(4)),
				resultSet.getInt(5));
		userPool.put(UID, newUser);
		return newUser;
	}
	
	/**
	 * Add a new user into GrafixPlane
	 * @param name The new user's name
	 * @param passwd The new user's password
	 * @param level The access level that would be assigned to the user
	 * @return The newly created user
	 * @throws SQLException If there are some thing wrong with the database
	 * @throws NameExistsException if this name already belongs to another user
	 */
	public synchronized static User newUser(String name,String passwd,AccessLevel level) throws SQLException, NameExistsException {
		int newID=getGP().getConfig().getInt("UIDCount")+1;
		getGP().getConfig().setInt("UIDCount", newID);
		try {
			return newUser(newID, name, passwd, level);
		} catch (UIDExistsException e) {
			//NOT POSSIBLE
			e.printStackTrace();
			GrafixPlane.stop();
			return null;
		}
	}
	
	/**
	 * Add a new user into GrafixPlane with specified UID
	 * @param UID The specified UID
	 * @param name The new user's name
	 * @param passwd The new user's password
	 * @param level The access level that would be assigned to the user
	 * @return The newly created user, never null
	 * @throws SQLException If there are some thing wrong with the database
	 * @throws NameExistsException if this name already belongs to another user
	 * @throws UIDExistsException if this UID already belongs to another user
	 */
	public synchronized static User newUser(int UID,String name,String passwd,AccessLevel level) throws SQLException, NameExistsException, UIDExistsException {
		userByName.setString(1,name);
		if(userByName.executeQuery().first()) throw new NameExistsException();	//A user with same name already exists
		userByID.setInt(1, UID);
		if(userByID.executeQuery().first()) throw new UIDExistsException();	//A user with same UID already exists
		
		newUser.setInt(1,UID);
		newUser.setString(2, name);
		newUser.setString(3, passwd);
		newUser.setInt(4, level.value);
		newUser.setInt(5, 0);
		
		newUser.execute();
		
		return new User(UID,name,level,0);
	}
	
	/**
	 * Overwrite an <strong>existing</strong> user data with some new data
	 * @param UID The ID of the user that is being updating
	 * @param name The new name for this user, or <em>null</em> to leave it unchanged 
	 * @param passwd the new password for this user, or <em>null</em> to leave it unchanged 
	 * @param level The new access level for this user, or <em>null</em> to leave it unchanged 
	 * @return <em>True</em> if everything was fine, or <em>False</em> if that UID doesn't point to any user, 
	 * @throws NameExistsException If this name already belongs to another user,
	 * @throws SQLException  If there are some thing wrong with the database
	 */
	public synchronized static boolean overrideUser(int UID,String name,String passwd,AccessLevel level) throws NameExistsException, SQLException {
		userByName.setString(1,name);
		if(userByName.executeQuery().first()) throw new NameExistsException();	//A user with same name already exists
		
		String sql="UPDATE USER SET ";
		if(name!=null) sql+=String.format("Username = '%s', ", name);
		if(passwd!=null) sql+=String.format("Passwd = '%s', ", passwd);
		if(level!=null) sql+=String.format("AccessLevel = %d, ", level.value);
		sql=sql.substring(0, sql.length()-2)+String.format(" WHERE UID = %d", UID);
		
		if(getGP().getConn().createStatement().executeUpdate(sql)!=1) return false;
		//TODO: invalidate all sessions that is login as this user
		return true;
	}
	
	/**
	 * Update a user's password with a new one
	 * @param UID The target user's ID
	 * @param passwd The new password
	 * @return <em>true</em> if the password is updated, or <em>false</em> if the user dosen't exist 
	 * or there are something wrong with the SQL query
	 * @throws SQLException 
	 * @throws NameExistsException 
	 */
	public synchronized static boolean modifyPasswd(int UID,String passwd) throws NameExistsException, SQLException {
		return overrideUser(UID, null, passwd, null);
	}
	
	public static void init(Connection conn) throws SQLException {
		userByID=conn.prepareStatement("SELECT * FROM USER WHERE UID = ?");
		userByName=conn.prepareStatement("SELECT * FROM USER WHERE Username = ?");
		newUser=conn.prepareStatement("INSERT INTO USER VALUES (?,?,?,?,?)");
		userPool=new HashMap<Integer,User>();
	}
}
