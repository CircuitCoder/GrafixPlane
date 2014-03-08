package tk.circuitcoder.grafixplane;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import joptsimple.OptionSet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;
import tk.circuitcoder.grafixplane.db.DatabaseManager;
import tk.circuitcoder.grafixplane.db.H2DatabaseManager;
import tk.circuitcoder.grafixplane.mail.Mail;
import tk.circuitcoder.grafixplane.mail.MailManager;
import tk.circuitcoder.grafixplane.mail.Mailbox;
import tk.circuitcoder.grafixplane.user.User;
import tk.circuitcoder.grafixplane.user.User.AccessLevel;
import tk.circuitcoder.grafixplane.user.User.NameExistsException;
import tk.circuitcoder.grafixplane.user.User.UIDExistsException;
/**
 * Represent a GrafixPlane instance, containing all runtime data
 * @author CircuitCoder
 * @since 0.0.1
 */
public class GrafixPlane {
	static final public String VERSION_TEXT="GrafixPlane 0.0.4 SNAPSHOT";
	static final public long VERSION=4L;
	static private GrafixPlane instance;
	
	static private boolean standalone=false;
	static private boolean inited=false;
	
	private Server webserver;
	private Integer port;
	private String host;
	private Logger logger;
	private DatabaseManager db;
	private boolean debug;
	private Config config;
	private Connection conn;
	private Locale locale;
	private ResourceBundle bundle;
	
	/**
	 * <p>When GrafixPlane is running as a webapp, 
	 * this servlet will be load on startup and initializes this application. </p>
	 * <p>Also provides server-wide information
	 * @author CircuitCoder
	 * @since 0.0.4
	 */
	public static class InfoServlet extends HttpServlet {
		private static final long serialVersionUID = GrafixPlane.VERSION;
		@Override
		public void init() {
			if(standalone||inited) return;
			instance=new GrafixPlane();
			instance.logger=LoggerFactory.getLogger(GrafixPlane.class);
			instance.logger.info("Starting GrafixPlane Instance In Webapp Mode...");
			instance.logger.info("Version: "+VERSION_TEXT);
			instance.preInit();
			
			try {
				instance.db=new H2DatabaseManager();
				String db=getInitParameter("database");
				String dbu=getInitParameter("database-user");
				String dbp=getInitParameter("database-passwd");
				if(db!=null) instance.db.setDefaultDB(db);
				if(dbu!=null) instance.db.setDBUser(dbu);
				if(dbp!=null) instance.db.setDBPasswd(dbp);
				instance.db.startDB(9750);
				try {
					instance.conn=instance.db.getConn();
				} catch (SQLException ex) {
					//Create the Database
					instance.conn=instance.db.getConn("", "", "GrafixPlane", false);
				}
				
				//Check if essential tables are exist
				instance.createTable(instance.conn);
			} catch(SQLException | ClassNotFoundException e) {
				instance.logger.error("Failed to initialize database");
			}
			
			if(instance.config==null) instance.config=new Config(instance.conn);
			
			if(!instance.postInit(instance.conn)) {
				instance.logger.error("Post-initializing section fails. Terminated");
				System.exit(-1);
			}
			
			String passwdStr=(String) getInitParameter("password");
			if(passwdStr==null) passwdStr="origin";
			instance.logger.info("Updating admin password: "+passwdStr);
				try {
					if(!User.modifyPasswd(0, passwdStr))
						User.newUser(0,"GAdmin", passwdStr, AccessLevel.ROOT);
				} catch (SQLException | NameExistsException
						| UIDExistsException e) {}	//Ignore
			
			String localeStr=getInitParameter("locale");
			if(localeStr!=null) {
				String lang[]=localeStr.split("_");
				if(lang.length==1) instance.locale=new Locale(lang[0]);
				else if(lang.length==2) instance.locale=new Locale(lang[0],lang[1]);
				else instance.locale=new Locale(lang[0],lang[1],lang[2]);
			}
			else instance.locale=Locale.getDefault();
			Locale.setDefault(instance.locale);
			instance.logger.info("Using locale: "+instance.locale.toString());
			
			instance.bundle=ResourceBundle.getBundle("locales/GrafixPlane",instance.locale);
			
			instance.postInit(instance.conn);
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					GrafixPlane.instance.onExit();
				}
			}));
			inited=true;
		}
		@Override
		public void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException {
			Writer out=res.getWriter();
			out.write("<!DOCTYPE html><html><head><title>GrafixPlane - info</title></head><body><table width=\"400px\" align=\"center\">");
			out.write("<tr><td>Standalone</td><td>"+String.valueOf(standalone)+"</td></tr>");
			out.write("<tr><td>Debug</td><td>"+String.valueOf(instance.debug)+"</td></tr>");
			out.write("<tr><td>Port</td><td>"+instance.port+"</td></tr>");
			out.write("<tr><td>Host</td><td>"+instance.host+"</td></tr>");
			out.write("<tr><td>Locale</td><td>"+instance.locale.toString()+"</td></tr>");
		}
		@Override
		public void doPost(HttpServletRequest req, HttpServletResponse res)
				throws ServletException, IOException {
			Writer out=res.getWriter();
			String field=req.getParameter("field");
			if(field.equals("standalone")) out.write(standalone?1:0);
			else if(field.equals("debug")) out.write(instance.debug?1:0);
			else if(field.equals("started")) out.write(instance.webserver.isStarted()?1:0);
			else if(field.equals("port")) out.write(instance.port);
			else if(field.equals("host")) out.write(instance.host);
			else if(field.equals("locale")) out.write(instance.locale.toString());
			//TODO: more fields
		}
		@Override
		public void destroy() {
			instance.onExit();
		}
	}
	
	/**
	 * Static Method. Creates a new GrafixPlane instance and start it
	 * @param options Options from the {@link Launcher}
	 */
	public static void start(OptionSet options) {
		//configuration files goes here
		try {
			instance=new GrafixPlane();
			instance.run(options);
		} catch (Exception e) {
			System.err.println("Unable to start the GrafixPlane instance. Error: ");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * Shutdown the internal GrafixPlane instance, and stop the virtual machine
	 */
	public static void stop() {
		instance.shutdown();
		System.exit(0);
	}
	
	/**
	 * Emergency stop
	 */
	public static void errorStop() {
		instance.logger.info("Some serious happened...");
		instance.shutdown();
		System.exit(-1);
	}
	
	/**
	 * Boots the entire system<br/>
	 * Does the following works:<br/>
	 * <ol>
	 * <li>Configures the Logger</li>
	 * <li>Verifies the database and creates essential slots</li>
	 * <li>Updates root user's password</li>
	 * <li>Set up locale for this instance</li>
	 * <li>Configures the Jetty web server and starts+joins it</li>
	 * </ol>
	 * @param options Options from the {@link Launcher}
	 * @throws Exception When an unexpected SQL error occurred or GrafixPlane failed to join the Jetty server
	 */
	private void run(OptionSet options) throws Exception {	
		//Referred: http://logback.qos.ch/faq.html#sharedConfiguration
		System.out.println("Starting GrafixPlane Instance...");
		System.out.print("Version: ");
		System.out.println(VERSION_TEXT);
		
		standalone=true;
		setupLogger();
		
		this.logger=LoggerFactory.getLogger(this.getClass());
		if(debug) this.logger.error("Well this is a ERROR... For testing if logback is working");
		this.logger.info("Logger configurated");
		
		port=(Integer) options.valueOf("p");
		host=(String) options.valueOf("h");
		debug=options.has("d");
		if(debug) this.logger.info("Debug mode is enabled!");
		
		if(host==null) {
			logger.error("You must specify a host using -h arg");
			System.exit(-1);
		}
		if(host.endsWith("/")) host=host.substring(0,host.length()-2);
		
		if(!preInit()) {
			logger.error("Pre-initializing section fails. Terminated");
			System.exit(-1);
		}
		
		webserver=new Server();
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
		
		if(options.has("no-extract")) {
			logger.info("Running in no-extract mode");
			webappHandler.setResourceBase(this.getClass().getClassLoader().getResource("webapp").toExternalForm());
		}
		else {
			String base=(String) options.valueOf("b");
			File baseFolder=new File(base);
			if(!baseFolder.exists()) {
				logger.info("Creating not existed resource base folder...");
				baseFolder.mkdir();
			}
			if(baseFolder.isDirectory()) {
				if(!extractWebapp(baseFolder)) System.exit(-1);
			} else {
				logger.error("Specified resource base is not a folder: "+baseFolder.getAbsolutePath());
				System.exit(-1);
			}
			
			webappHandler.setResourceBase(base);
			webappHandler.setDescriptor(baseFolder.getAbsolutePath()+"/WEB-INF/web.xml");
		}
		webappHandler.setParentLoaderPriority(true);
		
		handlers.addHandler(webappHandler);
		
//		ContextHandler imapHandler=new ContextHandler();
		//to be finished

		webserver.setHandler(handlers);
		//Other POP IMAP SMTP Server...
		
		//Now default user & passwd
		db=new H2DatabaseManager();
		if(options.has("d")) db.setDefaultDB((String) options.valueOf("d"));
		if(options.has("du")) db.setDBUser((String) options.valueOf("du"));
		if(options.has("dp")) db.setDBPasswd((String) options.valueOf("dp"));
		db.startDB(9750);
		try {
			conn=db.getConn();
		} catch (SQLException ex) {
			//Create the Database
			conn=db.getConn("", "", "GrafixPlane", false);
		}
		
		//Check if essential tables are exist
		createTable(conn);
		
		if(config==null) config=new Config(conn);
		
		if(!postInit(conn)) {
			logger.error("Post-initializing section fails. Terminated");
			System.exit(-1);
		}
		
		//initialize the administrative account with random passwd
		String passwdStr=(String) options.valueOf("r");
		if(passwdStr==null) passwdStr=String.valueOf(new Random().nextInt(100000000));
		this.logger.info("Updating admin password: "+passwdStr);
		if(!User.modifyPasswd(0, passwdStr)) {
			User.newUser(0,"GAdmin", passwdStr, AccessLevel.ROOT);
		}
		
		if(options.has("l")) {
			String lang[]=((String) options.valueOf("l")).split("_");
			if(lang.length==1) locale=new Locale(lang[0]);
			else if(lang.length==2) locale=new Locale(lang[0],lang[1]);
			else locale=new Locale(lang[0],lang[1],lang[2]);
		}
		else locale=Locale.getDefault();
		Locale.setDefault(locale);
		this.logger.info("Using locale: "+locale.toString());
		
		bundle=ResourceBundle.getBundle("locales/GrafixPlane",locale);
		
		this.logger.info("Starting WebServer on port "+port);
		webserver.start();
		
		Commander cmd=new Commander(true);
		cmd.startLoop();
	}
	
	private void shutdown() {
		logger.info("Shuting down GrafixPlane...");
		onExit();
		return;
	}

	/**
	 * Get the server-wide logger
	 * @return The Logger registered using GrafixPlane's class name
	 */
	public Logger getLogger() {
		return logger;
	}
	
	/**
	 * Get the database manager for this instance of GrafixPlane 
	 * @return The database manager
	 * @see tk.circuitcoder.grafixplane.db.DatabaseManager
	 */
	public DatabaseManager getDB() {
		return db;
	}
	
	/**
	 * Get the shared database connection for GrafixPlane
	 * @return
	 */
	public Connection getConn() {
		return conn;
	}
	
	/**
	 * Get the configuration (stored in the database) of this instance of GrafixPlane
	 * @return The Config object
	 * @see tk.circuitcoder.grafixplane.Config
	 */
	public Config getConfig() {
		return config;
	}
	
	/**
	 * Indicate whether the server is running in debug mode
	 * @return Whether running in debug mode
	 */
	public boolean isDebug() {
		return debug;
	}
	
	/**
	 * Get locale for this GrafixPlane instance
	 * @return The locale of this instance
	 */
	public Locale getLocale() {
		return locale;
	}
	
	/**
	 * Get the locale-specific ResourceBundle  of the translation file
	 * @return The ResourceBundle object
	 */
	public ResourceBundle getTranslation() {
		return bundle;
	}
	
	/**
	 * Get the GrafixPlane instance
	 * @return the instance
	 */
	public static GrafixPlane getGP() {
		return instance;
	}
	
	private void setupLogger() {
		LoggerContext context=(LoggerContext) LoggerFactory.getILoggerFactory();
		context.reset();
		ch.qos.logback.classic.Logger rootLogger =
				(ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		
		PatternLayoutEncoder debugPattern=new PatternLayoutEncoder();
		debugPattern.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
		debugPattern.setContext(context);
		debugPattern.start();
		
		PatternLayoutEncoder infoPattern=new PatternLayoutEncoder();
		infoPattern.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
		infoPattern.setContext(context);
		infoPattern.start();
		
		PatternLayoutEncoder errorPattern=new PatternLayoutEncoder();
		errorPattern.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
		errorPattern.setContext(context);
		errorPattern.start();
		
		PatternLayoutEncoder consolePattern=new PatternLayoutEncoder();
		consolePattern.setPattern("%-4relative [%thread] %-5level %logger{35} - %msg %n"); //TODO: Modifiable
		consolePattern.setContext(context);
		consolePattern.start();
		
		FileAppender<ILoggingEvent> debugAppender=new FileAppender<ILoggingEvent>();
		debugAppender.setName("debug");
		debugAppender.setFile("logs/debug.log");
		debugAppender.setContext(context);
		debugAppender.addFilter(new AbstractMatcherFilter<ILoggingEvent>() {
			@Override
			public FilterReply decide(ILoggingEvent event) {
				if(event.getLevel().isGreaterOrEqual(Level.DEBUG))
					return FilterReply.ACCEPT;
				else return FilterReply.DENY;
			}
		});
		debugAppender.setEncoder(debugPattern);
		debugAppender.start();
		
		FileAppender<ILoggingEvent> infoAppender=new FileAppender<ILoggingEvent>();
		infoAppender.setName("info");
		infoAppender.setFile("logs/info.log");
		infoAppender.setContext(context);
		infoAppender.addFilter(new AbstractMatcherFilter<ILoggingEvent>() {
			@Override
			public FilterReply decide(ILoggingEvent event) {
				if(event.getLevel().isGreaterOrEqual(Level.INFO))
					return FilterReply.ACCEPT;
				else return FilterReply.DENY;
			}
		});
		infoAppender.setEncoder(infoPattern);
		infoAppender.start();
		
		ConsoleAppender<ILoggingEvent> consoleAppender=new ConsoleAppender<ILoggingEvent>();
		consoleAppender.setName("console");
		consoleAppender.setContext(context);
		consoleAppender.addFilter(new AbstractMatcherFilter<ILoggingEvent>() {
			@Override
			public FilterReply decide(ILoggingEvent event) {
				if(event.getLevel().isGreaterOrEqual(Level.INFO)
						&&event.getLoggerName().equals(GrafixPlane.this.logger.getName()))
					return FilterReply.ACCEPT;
				else return FilterReply.DENY;
			}
		});
		consoleAppender.setEncoder(consolePattern);
		consoleAppender.start();
		
		FileAppender<ILoggingEvent> errorAppender=new FileAppender<ILoggingEvent>();
		errorAppender.setName("error");
		errorAppender.setFile("logs/error.log");
		errorAppender.setContext(context);
		errorAppender.addFilter(new AbstractMatcherFilter<ILoggingEvent>() {
			@Override
			public FilterReply decide(ILoggingEvent event) {
				if(event.getLevel().isGreaterOrEqual(Level.ERROR))
					return FilterReply.ACCEPT;
				else return FilterReply.DENY;
			}
		});
		errorAppender.setEncoder(errorPattern);
		errorAppender.start();
		
		rootLogger.addAppender(errorAppender);
		rootLogger.addAppender(consoleAppender);
		rootLogger.addAppender(infoAppender);
		rootLogger.addAppender(debugAppender);
	}
	
	private boolean extractWebapp(File dest) {
		logger.info("Extracting files into resource base folder: "+dest.getAbsolutePath()+"...");
		
		byte[] buf=new byte[8*1024];
		JarFile runningJar;
		try {
			runningJar=new JarFile(GrafixPlane.class.getProtectionDomain().
					getCodeSource().getLocation().getPath());
		} catch (IOException e) {
			logger.error("Extracting Failed: Unable to load jar file");
			System.out.println("-----Stack Trace----");
			e.printStackTrace();
			return false;
		}
		
		Enumeration<JarEntry> entries=runningJar.entries();
		String prefix="webapp";
		URI base=null;
		try {
			base=new URI(prefix);
		} catch (URISyntaxException e1) {
			logger.error("Internal Error");
			e1.printStackTrace();
		}
		
		int extractCounter=0;
		while(entries.hasMoreElements()) {
			JarEntry entry=entries.nextElement();
			if(entry.getName().startsWith(prefix)) {
				try {
					//TODO: hash
					URI srcPath=new URI(entry.getName());
					InputStream is=runningJar.getInputStream(entry);
					
					URI diff=base.relativize(srcPath);
					File destFile=new File(dest,diff.getPath());
					if(destFile.exists()) {
						logger.debug(destFile.getAbsolutePath()+" exists. skipped");
					}
					else if(srcPath.getPath().endsWith("/")) {
						logger.debug("Creating Dir: "+destFile.getAbsolutePath());
						destFile.mkdir();
					}
					else {
						logger.debug("Extracting: "+srcPath+" --> "+destFile.getAbsolutePath());
						FileOutputStream fos=new FileOutputStream(destFile);
						int len;
						while((len=is.read(buf))!=-1) fos.write(buf,0,len);
						++extractCounter;
						fos.close();
					}
					is.close();
				} catch (IOException | URISyntaxException e) {
					logger.error("Extracting Failed: Unable to read resources");
					System.out.println("-----Stack Trace----");
					e.printStackTrace();
				}
			}
		}
		
		logger.info("Extracting completed, total: "+extractCounter);
		try {
			runningJar.close();
		} catch (IOException e) {} //Ignore
		return true;
	}
	
	private void createTable(Connection conn) throws SQLException {
		Statement stat=conn.createStatement();
		
		DatabaseMetaData dbmd=conn.getMetaData();
		ResultSet tables=dbmd.getTables(null, null, "GRAFIX", new String[]{"TABLE"});
		if(!tables.first()) {
			this.logger.info("Creating table GRAFIX");
			stat.execute("CREATE TABLE GRAFIX ("
					+ "Entry varchar,"
					+ "Value varchar"
					+ ")");
			
			config=new Config();
			config.init(conn, Arrays.asList("mailCount:0","UIDCount:0","BIDCount:1","BoxCapacity:100")); //Initialize the table
		}
		
		tables=dbmd.getTables(null, null, "USER", new String[]{"TABLE"});
		if(!tables.first()) {
			this.logger.info("Creating table USER");
			stat.execute("CREATE TABLE USER ("
					+ "UID int(10) UNSIGNED,"
					+ "Username varchar,"
					+ "Passwd varchar,"
					+ "AccessLevel tinyint(3) UNSIGNED,"
					+ "BoxCount tinyint UNSIGNED"	//How many mailboxes belongs to this user
					+ ");");
		}
		
		tables=dbmd.getTables(null, null, "MAIL", new String[]{"TABLE"});
		if(!tables.first()) {
			this.logger.info("Creating table MAIL");
			stat.execute("CREATE TABLE MAIL ("
					+ "MID int(10) UNSIGNED,"
					+ "Sender int(10) UNSIGNED,"
					+ "Receiver varchar,"	//Syntax: MID|MID|MID|...MID|
					+ "Subject varchar,"
					+ "Content varchar,"
					+ "Attachment varchar,"	//Syntax: FID|FID|FID|...FID|
					+ "ReplyTo int(10) UNSIGNED," //0 for new mail
					+ "RefCount smallint UNSIGNED,"
					+ "SentTime bigint UNSIGNED"
					+ ");");
		}
		
		tables=dbmd.getTables(null, null, "MAILBOX", new String[]{"TABLE"});
		if(!tables.first()) {
			this.logger.info("Creating table MAILBOX");
			stat.execute("CREATE TABLE MAILBOX ("
					+ "BID int UNSIGNED,"
					+ "UID int(10) UNSIGNED,"	//The UID of its owner
					+ "Mails varchar,"	//Mails, Syntax: ......||MID@Box'Type,status||......
					+ "BoxCount smallint,"	//The index of this Box
					+ "MailCount smallint,"	//How many mails are there in this box
					+ ");");

		}
		
		tables=dbmd.getTables(null, null, "FILE", new String[]{"TABLE"});
		if(!tables.first()) {
			this.logger.info("Creating table FILE");
			stat.execute("CREATE TABLE FILE ("
					+ "FID int UNSIGNED,"
					+ "Dir varchar,"
					+ "Owner int,"
					+ "CTime bigint UNSIGNED"
					+ ");");

		}
	}
	
	private boolean preInit() {
		return true;
	}
	
	private boolean postInit(Connection conn) {
		try {
			Mail.init(conn);
			User.init(conn);
			Mailbox.init(conn);
			MailManager.init(conn);
			tk.circuitcoder.grafixplane.file.File.init(conn);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean onExit() {
		try {
			webserver.stop();
			User.clear();
			Mail.clear();
			tk.circuitcoder.grafixplane.file.File.clear();
			config.save(conn);
			conn.close();
			db.stopDB();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
