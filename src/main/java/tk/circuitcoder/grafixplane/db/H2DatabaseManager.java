package tk.circuitcoder.grafixplane.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.h2.tools.Server;

import tk.circuitcoder.grafixplane.GrafixPlane;

/**
 * An implement of DatabaseManager for H2Database
 * @author CircuitCoder
 * @since 0.0.1
 */
public class H2DatabaseManager extends DatabaseManager {
	
	Server database;
	int port;
	
	public void startDB(int port,boolean verbose,boolean debug) throws SQLException {	//For testing
		if(verbose) GrafixPlane.getGP().getLogger().info("Starting DB...");
		
		this.port=port;
		if(database==null) {
			if(verbose) GrafixPlane.getGP().getLogger().info("Creating DB instance...");
			//Add an option to modify the ports;
			if(debug)  //Allow Other debug software to interact with DB
				database=Server.createTcpServer(new String[] {"-tcpAllowOthers"});
			else
				database=Server.createTcpServer();
		}
		
		if(database.isRunning(true)) {
			if(verbose) GrafixPlane.getGP().getLogger().info("DB already running");
			return;
		}
		database.start();
		if(verbose) GrafixPlane.getGP().getLogger().info("DB started");
	}

	@Override
	public void startDB(int port) throws SQLException {
		startDB(port,true,GrafixPlane.getGP().isDebug());
	}

	@Override
	public void stopDB() {
		GrafixPlane.getGP().getLogger().info("Stopping DB...");
		if(!database.isRunning(true)) {
			GrafixPlane.getGP().getLogger().info("DB already stopped");
			return;
		}
		database.stop();
		GrafixPlane.getGP().getLogger().info("DB stopped");
	}

	@Override
	public Connection getConn(String username, String passwd, String db,
			boolean checkExist) throws SQLException, ClassNotFoundException {
		if(!database.isRunning(true)) return null;
		Class.forName("org.h2.Driver");
		String str="jdbc:h2:"+db;
		if(checkExist) str=str+";IFEXISTS=TRUE";
		return DriverManager.getConnection(str,username,passwd);
	}
}
