package tk.circuitcoder.eschool.db;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class DatabaseManager {
	public abstract void startDB(int port) throws SQLException;
	public abstract void stopDB();
	public Connection getConn() throws ClassNotFoundException, SQLException {
		return getConn("", "", "eschool", true);
	}
	public Connection getConn(String username,String passwd) throws SQLException, ClassNotFoundException {
		return getConn(username, passwd, "eschool", true);
	}
	public abstract Connection getConn(String username,String passwd,String db,boolean checkExist) throws SQLException, ClassNotFoundException;
}
