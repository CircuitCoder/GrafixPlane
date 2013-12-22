package tk.circuitcoder.grafixplane;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Represents a GrafixPlane User</br>
 * Provides some utilities related to authorization and accounts
 * @author CircuitCoder
 * @since 0.0.1
 */
public class User {
	String username;
	int accessLevel;
	
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
		return !(getName(req)==null);
	}
	
	/**
	 * Gets the name of the session's user
	 * @param req The HTTP request
	 * @return Username of the session, or <strong>NULL</strong> if the session isn't currently logined 
	 */
	public static String getName(HttpServletRequest req) {
		HttpSession session=req.getSession();
		if(session.isNew()) session.setMaxInactiveInterval(600);
		return (String) session.getAttribute("g_user");
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
	
	public static User getUser(String uname) {
		//TODO:Nothing here!
		return null;
	}
}
