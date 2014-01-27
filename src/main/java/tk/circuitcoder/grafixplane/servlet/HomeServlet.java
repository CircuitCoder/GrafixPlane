package tk.circuitcoder.grafixplane.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tk.circuitcoder.grafixplane.GrafixPlane;
import tk.circuitcoder.grafixplane.User;

/**
 * Generate one's HOME! How IMPORTANT!
 * @author CircuitCoder
 */
public class HomeServlet extends HttpServlet {
	private static final long serialVersionUID = GrafixPlane.VERSION;
	
	@Override
	public void doGet(HttpServletRequest req,HttpServletResponse resp) throws IOException {
		//Well do nothing now
		if(!User.isLogined(req.getSession())) resp.sendRedirect("/login.jsp");
		resp.sendRedirect("/mail");
	}
	
	@Override
	public void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
}
