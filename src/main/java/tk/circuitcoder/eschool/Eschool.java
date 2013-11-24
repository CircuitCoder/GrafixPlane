package tk.circuitcoder.eschool;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import joptsimple.OptionSet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.WebAppContext;

import tk.circuitcoder.eschool.filter.ControlFilter;

public class Eschool {
	static final public String VERSION_TEXT="Eschool 0.0.1 SNAPSHOT";
	static final public long VERSION=1L;
	static private Eschool instance;
	
	private Server webserver;
	private Integer port;
	private String host;
	
	public static void start(OptionSet options) {
		//configuration files goes here
		try {
			instance=new Eschool(options);
		} catch (Exception e) {
			System.out.println("Unable to start the Eschool instance. Error: ");
			e.printStackTrace();
		}
	}
	
	private Eschool(OptionSet options) throws Exception {
		System.out.println("Starting Eschool Instance...");
		
		port=(Integer) options.valueOf("p");
		host=(String) options.valueOf("h");
		if(host==null) {
			System.out.println("You must specify a host using -h arg");
			System.exit(-1);
		}
		if(host.endsWith("/")) host=host.substring(0,host.length()-2);
		
		webserver=new Server();
		Logger logger=Log.getLogger(Server.class);
		logger.setDebugEnabled(true);
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
		
		System.out.println("Starting WebServer on port "+port);
		webserver.start();
		webserver.join();
	}
	
	public static Eschool getEschool() {
		return instance;
	}
	
}
