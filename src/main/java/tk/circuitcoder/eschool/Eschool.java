package tk.circuitcoder.eschool;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.Random;

import javax.servlet.DispatcherType;

import joptsimple.OptionSet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tk.circuitcoder.eschool.db.DatabaseManager;
import tk.circuitcoder.eschool.db.H2DatabaseManager;
import tk.circuitcoder.eschool.filter.ControlFilter;

public class Eschool {
	static final public String VERSION_TEXT="Eschool 0.0.1 SNAPSHOT";
	static final public long VERSION=1L;
	static private Eschool instance;
	
	private Server webserver;
	private Integer port;
	private String host;
	private Logger logger;
	private DatabaseManager db;
	private boolean debug;
	
	public static void start(OptionSet options) {
		//configuration files goes here
		try {
			instance=new Eschool();
			instance.run(options);
		} catch (Exception e) {
			System.err.println("Unable to start the Eschool instance. Error: ");
			e.printStackTrace();
		}
	}
	
	private void run(OptionSet options) throws Exception {		
		this.logger=LoggerFactory.getLogger(this.getClass());
		this.logger.info("Starting Eschool Instance...");
		
		port=(Integer) options.valueOf("p");
		host=(String) options.valueOf("h");
		debug=options.has("d");
		if(debug) this.logger.info("Debug mode is enabled!");
		
		if(host==null) {
			logger.error("You must specify a host using -h arg");
			System.exit(-1);
		}
		if(host.endsWith("/")) host=host.substring(0,host.length()-2);
		
		webserver=new Server();
		org.eclipse.jetty.util.log.Logger logger=Log.getLogger(Server.class);
		logger.setDebugEnabled(debug);	//Enable additional output when debug mode is set, But currently not working
		HandlerList handlers=new HandlerList();
		
		ServerConnector defaultConnector=new ServerConnector(webserver);
		defaultConnector.setPort(80);
		defaultConnector.setHost(host);
		defaultConnector.setIdleTimeout(600);
		webserver.addConnector(defaultConnector);
		
//		ServletContextHandler servletHandler=new ServletContextHandler(ServletContextHandler.SESSIONS);
//		servletHandler.setContextPath("/");
//		servletHandler.setVirtualHosts(new String[] {host});
//		ServletHolder holder=new ServletHolder(new ControlServlet());
//		holder.setInitParameter("baseDir",
//				getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
//		holder.setInitParameter("host", host);
//		servletHandler.addServlet(holder, "/*");
//		handlers.addHandler(servletHandler);
		
		WebAppContext webappHandler=new WebAppContext();
		webappHandler.setServer(webserver);
		webappHandler.setContextPath("/");
		webappHandler.setVirtualHosts(new String[] {host});

		webappHandler.setWelcomeFiles(new String[] {"/login.jsp"});
		webappHandler.setResourceBase(this.getClass().getClassLoader().getResource("webapp").toExternalForm());
		webappHandler.setDescriptor(this.getClass().getClassLoader().getResource("webapp/WEB-INF/web.xml").toExternalForm());
		webappHandler.setParentLoaderPriority(true);
		
		FilterHolder holder=new FilterHolder(new ControlFilter());
		holder.setInitParameter("baseDir",
				getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		holder.setInitParameter("host", host);
		webappHandler.addFilter(holder, "/*",EnumSet.of(DispatcherType.REQUEST,DispatcherType.FORWARD));
		
		handlers.addHandler(webappHandler);
		
//		ContextHandler imapHandler=new ContextHandler();
		//to be finished

		webserver.setHandler(handlers);
		//Other POP IMAP SMTP Server...
		
		//TODO: add some options for databases
		//Now default user & passwd
		db=new H2DatabaseManager();
		db.startDB(9750);
		Connection conn;
		try {
			conn=db.getConn();
		} catch (SQLException ex) {
			//Create the Database
			conn=db.getConn("", "", "eschool", false);
		}
		//Check if user table are exist
		DatabaseMetaData dbmd=conn.getMetaData();
		ResultSet tables=dbmd.getTables(null, null, "E_USER", new String[]{"TABLE"});
		if(!tables.first()) {
			this.logger.info("Creating table E_USER");
			Statement stat=conn.createStatement();
			stat.execute("CREATE TABLE E_USER ("
					+ "Username varchar,"
					+ "Passwd varchar,"
					+ "Level tinyint(0)"
					+ ");");
		}
		//initialize the administrative account with random passwd
		Random rand=new Random();
		int passwd=rand.nextInt(100000000);
		this.logger.info("Updating admin password: "+passwd);
		Statement stat=conn.createStatement();
		if(stat.executeUpdate("UPDATE E_USER SET Passwd = '"+passwd+"' WHERE Username = 'eschool_admin';")==0) { //IF NO line was updated
			stat.executeUpdate("INSERT INTO E_USER VALUES ('eschool_admin','"+passwd+"',0);");
		}
		
		this.logger.info("Starting WebServer on port "+port);
		webserver.start();
		webserver.join();
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public DatabaseManager getDB() {
		return db;
	}
	
	public boolean isDebug() {
		return debug;
	}
	
	public static Eschool getEschool() {
		return instance;
	}
	
}
