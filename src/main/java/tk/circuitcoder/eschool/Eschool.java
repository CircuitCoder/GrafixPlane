package tk.circuitcoder.eschool;

import joptsimple.OptionSet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import tk.circuitcoder.eschool.servlet.ControlServlet;

public class Eschool {
	static final public String VERSION_TEXT="Eschool 0.0.1 SNAPSHOT";
	static final public long VERSION=1L;
	static private Eschool instance;
	
	private Server webserver;
	
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
		
		Integer port=(Integer) options.valueOf("p");
		String host=(String) options.valueOf("h");
		
		webserver=new Server();
		HandlerList handlers=new HandlerList();
		
		ServerConnector defaultConnector=new ServerConnector(webserver);
		defaultConnector.setPort(80);
		defaultConnector.setHost(host);
		webserver.addConnector(defaultConnector);
		
		ServletContextHandler servletHandler=new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletHandler.setContextPath("/");
		servletHandler.setVirtualHosts(new String[] {"www."+host,host});
		ServletHolder holder=new ServletHolder(new ControlServlet());
		holder.setInitParameter("baseDir",
				getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		servletHandler.addServlet( holder, "/*");
		handlers.addHandler(servletHandler);
		
		ContextHandler imapHandler=new ContextHandler();
		//to be finished

		webserver.setHandler(handlers);
		//Other POP IMAP SMTP Server...
		
		System.out.println("Starting WebServer on port "+port);
		webserver.start();
		webserver.join();
	}
	
	public Eschool getEschool() {
		return instance;
	}
}
