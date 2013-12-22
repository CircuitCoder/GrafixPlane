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
		return getConn("", "", "GrafixPlane", true);
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
		return getConn(username, passwd, "GrafixPlane", true);
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
