package tk.circuitcoder.grafixplane.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides utilities for database management
 * @author CircuitCoder
 *
 */
public abstract class DatabaseManager {
	/**
	 * The name of the default database
	 */
	protected String dbName;
	protected String dbUser;
	protected String dbPasswd;
	
	/**
	 * Basic constructor, initialize data fields
	 */
	public DatabaseManager() {
		dbName="GrafixPlane";
		dbUser="";
		dbPasswd="";
	}
	
	/**
	 * Set the default database name. 
	 * This database will be used to store and load all data by other components of GrafixPlane
	 * @param name  The default database name
	 */
	public void setDefaultDB(String name) {
		if(name==null) throw new IllegalArgumentException("Null database name");
		dbName=name;
	}
	
	/**
	 * Set the username for connecting to the database. 
	 * Maybe useful when the default database is set to a shared database
	 * @param user The username
	 */
	public void setDBUser(String user) {
		if(user==null) throw new IllegalArgumentException("Null database user");
		dbUser=user;
	}
	
	/**
	 * Set the password for connecting to the database. 
	 * Maybe useful when the default database is set to a shared database
	 * @param passwd The password
	 */
	public void setDBPasswd(String passwd) {
		if(passwd==null) throw new IllegalArgumentException("Null database password");
		dbPasswd=passwd;
	}
	
	/**
	 * Start the database server
	 * @param port The specified port for the server
	 * @throws SQLException
	 */
	public abstract void startDB(int port) throws SQLException;
	
	/**
	 * Stops the database server
	 */
	public abstract void stopDB();
	
	/**
	 * Get default database connection
	 * @return A new connection of the database "GrafixPlane", using root user, and disable automatically creating
	 * @throws ClassNotFoundException If the package is corrupted and the DB driver could not be found
	 * @throws SQLException If a database access error occurs
	 */
	public Connection getConn() throws ClassNotFoundException, SQLException {
		return getConn(dbUser,dbPasswd, dbName, true);
	}
	
	/**	 
	 * Get default database connection with specified identity
	 * @param username The user name that will be used for creating connection
	 * @param passwd Password of the user
	 * @return A new connection of the database "GrafixPlane", with automatically creating disabled
	 * @throws ClassNotFoundException If the package is corrupted and the DB driver could not be found
	 * @throws SQLException If a database access error occurs
	 */
	public Connection getConn(String username,String passwd) throws SQLException, ClassNotFoundException {
		return getConn(username, passwd,dbName, true);
	}
	
	/**
	 * Get a database connection
	 * @param username The user name that will be used for creating connection
	 * @param passwd Password of the use
	 * @param db The name of target database
	 * @param checkExist Whether disable automatically creating database that doesn't exist
	 * @return A new connection of the requested database
	 * @throws SQLException If a database access error occur
	 * @throws ClassNotFoundException If the package is corrupted and the DB driver could not be found
	 */
	public abstract Connection getConn(String username,String passwd,String db,boolean checkExist) throws SQLException, ClassNotFoundException;
}
