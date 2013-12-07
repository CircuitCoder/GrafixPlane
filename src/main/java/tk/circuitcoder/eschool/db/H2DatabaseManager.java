package tk.circuitcoder.eschool.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.h2.tools.Server;

import tk.circuitcoder.eschool.Eschool;

public class H2DatabaseManager extends DatabaseManager {
	
	Server database;
	int port;

	@Override
	public void startDB(int port) throws SQLException {
		Eschool.getEschool().getLogger().info("Starting DB...");
		
		this.port=port;
		if(database==null) {
			Eschool.getEschool().getLogger().info("Creating DB instance...");
			//Add an option to modify the ports;
			if(Eschool.getEschool().isDebug())  //Allow Other debug software to interact with DB
				database=Server.createTcpServer(new String[] {"-tcpAllowOthers"});
			else
				database=Server.createTcpServer(new String[] {"-tcpAllowOthers"});
		}
		
		if(database.isRunning(true)) {
			Eschool.getEschool().getLogger().info("DB already running");
			return;
		}
		database.start();
		Eschool.getEschool().getLogger().info("DB started");
	}

	@Override
	public void stopDB() {
		Eschool.getEschool().getLogger().info("Stopping DB...");
		if(!database.isRunning(true)) {
			Eschool.getEschool().getLogger().info("DB already stopped");
			return;
		}
		database.stop();
		Eschool.getEschool().getLogger().info("DB stopped");
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
