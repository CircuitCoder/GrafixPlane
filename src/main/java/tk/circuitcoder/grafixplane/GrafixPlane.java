package tk.circuitcoder.grafixplane;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;

import javax.servlet.DispatcherType;

import joptsimple.OptionSet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.FilterHolder;
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
import tk.circuitcoder.grafixplane.filter.ControlFilter;
import tk.circuitcoder.grafixplane.mail.Mail;
import tk.circuitcoder.grafixplane.mail.MailManager;
import tk.circuitcoder.grafixplane.mail.Mailbox;
import tk.circuitcoder.grafixplane.user.User;
import tk.circuitcoder.grafixplane.user.User.AccessLevel;;
/**
 * Represent a GrafixPlane instance, containing all runtime data
 * @author CircuitCoder
 * @since 0.0.1
 */
public class GrafixPlane {
	static final public String VERSION_TEXT="GrafixPlane 0.0.4 SNAPSHOT";
	static final public long VERSION=4L;
	static private GrafixPlane instance;
	
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
		
		//Now default user & passwd
		db=new H2DatabaseManager();
		db.setDefaultDB((String) options.valueOf("d"));
		db.setDBUser((String) options.valueOf("du"));
		db.setDBPasswd((String) options.valueOf("dp"));
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
	
	private void createTable(Connection conn) throws SQLException {
		//Check if user table are exist
		DatabaseMetaData dbmd=conn.getMetaData();
		ResultSet tables=dbmd.getTables(null, null, "GRAFIX", new String[]{"TABLE"});
		if(!tables.first()) {
			this.logger.info("Creating table GRAFIX");
			Statement stat=conn.createStatement();
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
			Statement stat=conn.createStatement();
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
			Statement stat=conn.createStatement();
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
			Statement stat=conn.createStatement();
			stat.execute("CREATE TABLE MAILBOX ("
					+ "BID int UNSIGNED,"
					+ "UID int(10) UNSIGNED,"	//The UID of its owner
					+ "Mails varchar,"	//Mails, Syntax: ......||MID@Box'Type,status||......
					+ "BoxCount smallint,"	//The index of this Box
					+ "MailCount smallint,"	//How many mails are there in this box
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
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean onExit() {
		try {
			User.clear();
			Mail.clear();
			webserver.stop();
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
