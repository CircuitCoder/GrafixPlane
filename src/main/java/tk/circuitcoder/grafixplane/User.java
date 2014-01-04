package tk.circuitcoder.grafixplane;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
	
	public int getUID() {
		return UID;	
	}
	public String getUsername() {
		return username;
	}
	
	public int getAccessLevel() {
		return accessLevel;
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
			Connection conn=GrafixPlane.getGP().getDB().getConn();
			Statement stat=conn.createStatement();
			ResultSet result=stat.executeQuery("SELECT * FROM USER WHERE(Username='"+uname+"');");
			GrafixPlane.getGP().getLogger().info("SELECT * FROM USER WHERE(Username='"+uname+"');");
			if(!result.first()) return false;
			String storedPasswd=result.getString("Passwd");
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
			Connection conn=GrafixPlane.getGP().getDB().getConn();
			Statement stat=conn.createStatement();
			ResultSet resultSet=stat.executeQuery("SELECT * FROM USER WHERE(Username='"+uname+"')");
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
			Connection conn=GrafixPlane.getGP().getDB().getConn();
			Statement stat=conn.createStatement();
			ResultSet resultSet=stat.executeQuery("SELECT * FROM USER WHERE(UID="+UID+")");
			if(!resultSet.first()) return null;
			
			User result=new User();
			result.UID=UID;
			result.username=resultSet.getString("Username");
			result.accessLevel=resultSet.getInt("AccessLevel");
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
