package tk.circuitcoder.grafixplane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
	private int accessLevel;
	
	private static PreparedStatement userByID;
	private static PreparedStatement userByName;
	private static PreparedStatement modifyUser;
	private static PreparedStatement modifyPasswd;
	
	public int getUID() {
		return UID;	
	}
	public String getUsername() {
		return username;
	}
	
	public int getAccessLevel() {
		return accessLevel;
	}
	
	public static enum AccessLevel {
		ROOT(0,"Root User"),ADMIN(1,"Administrator");
		int value; //The value stored in the database, representing this level
		String display; //The string displayed in the account information page
		
		private AccessLevel(int v,String ds) {
			value=v;
			display=ds;
		}
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
	 * User instance can only be created by calling getUser()
	 */
	private User() {}
	
	/**
	 * Checks if the incoming request's session is logined
	 * @param req The HTTP request
	 * @return
	 */
	public static boolean isLogined(HttpServletRequest req) {
		return !(getCurrentUser(req)==null);
	}
	
	/**
	 * Gets the current logined user
	 * @param req The HTTP request
	 * @return The user of this session, or <strong>NULL</strong> if the session isn't currently logined 
	 */
	public static User getCurrentUser(HttpServletRequest req) {
		HttpSession session=req.getSession();
		if(session.isNew()) session.setMaxInactiveInterval(600);
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
	 */
	public static User getUser(String uname) {
		try {
			userByName.setString(1, uname);
			ResultSet resultSet=userByName.executeQuery();
			if(!resultSet.first()) return null;
			
			User result=new User();
			result.UID=resultSet.getInt("UID");
			result.username=uname;
			result.accessLevel=resultSet.getInt("AccessLevel");
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gets a user's information and creates the User instance base on its ID
	 * @param UID The user's ID
	 * @return The created instance, or Null if the user is not found or some unexpected error occurred
	 */
	public static User getUser(int UID) {
		try {
			userByID.setInt(1, UID);
			ResultSet resultSet=userByID.executeQuery();
			if(!resultSet.first()) return null;
			
			User result=new User();
			result.UID=UID;
			result.username=resultSet.getString(2);
			result.accessLevel=resultSet.getInt(4);
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Add a new user into GrafixPlane
	 * @param name The new user's name
	 * @param passwd The new user's password
	 * @param level The access level that would be assigned to the user
	 * @return
	 */
	public synchronized static User newUser(String name,String passwd,AccessLevel level) {
		int newID=getGP().getConfig().getInt("userCount")+1;
		getGP().getConfig().setInt("userCount", newID);
		
		try {
			userByName.setString(1,name);
			if(!userByName.execute()) return null;	//A user with same name already exists
		} catch(SQLException e) {
			e.printStackTrace();
			return null;	//Unknowing whether a user with the same name already exists, interpreting as yes
		}

		return overrideUser(newID, name, passwd, level);
	}
	
	public synchronized static User overrideUser(int UID,String name,String passwd,AccessLevel level) {
		try {
			modifyUser.setInt(1,UID);
			modifyUser.setString(2,name);
			modifyUser.setString(3,passwd);
			modifyUser.setInt(4,level.value);
			modifyUser.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
		User result=new User();
		result.UID=UID;
		result.accessLevel=level.value;
		result.username=name;
		return result;
	}
	
	/**
	 * Update a user's password with a new one
	 * @param UID The target user's ID
	 * @param passwd The new password
	 * @return <em>true</em> if the password is updated, or <em>false</em> if the user dosen't exist 
	 * or there are something wrong with the SQL query
	 */
	public synchronized static boolean modifyPasswd(int UID,String passwd) {
		try {
			modifyPasswd.setInt(2,UID);
			modifyPasswd.setString(1,passwd);
			return modifyPasswd.executeUpdate()==1;
		} catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static void init(Connection conn) throws SQLException {
		userByID=conn.prepareStatement("SELECT * FROM USER WHERE(UID=?)");
		userByName=conn.prepareStatement("SELECT * FROM USER WHERE(Username=?)");
		modifyUser=conn.prepareStatement("INSERT INTO USER VALUES (?,?,?,?)");
		modifyPasswd=conn.prepareStatement("UPDATE USER SET Passwd = ? WHERE UID = ?");
	}
}
