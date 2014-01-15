package tk.circuitcoder.grafixplane.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import tk.circuitcoder.grafixplane.GrafixPlane;
import tk.circuitcoder.grafixplane.User;

/**
 * Handles requests related to account & authorization
 * @author CircuitCoder
 */
public class AuthServlet extends HttpServlet {
	private static final long serialVersionUID = GrafixPlane.VERSION;
	
	public void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException {
		String action=req.getParameter("action");
		if(action!=null&&action.equals("logout")) { //logout
			HttpSession session=req.getSession();
			if(session.getAttribute("g_user")==null) resp.getWriter().write("1"); //already logout
			else {
				session.invalidate(); //logout now
				resp.getWriter().write("0");
				//TODO: add an option to jump to login page
			}
		} else { //Otherwise login
			String uname=req.getParameter("uname");
			String passwd=req.getParameter("passwd");
			
			GrafixPlane.getGP().getLogger().info("User login: "+uname+" with "+passwd);
			
			if(uname==null||passwd==null) resp.getWriter().write("9"); //Something strange happened
			else if(User.verify(uname, passwd)) {
				resp.getWriter().write("0"); //login succeed
				try {
					req.getSession().setAttribute("g_user", User.getUser(uname));
				} catch (SQLException e) {
					resp.sendRedirect("#E:"+e.getLocalizedMessage());
					e.printStackTrace();
				}
			}
			else resp.getWriter().write("1"); //login failed
		}
		
		resp.flushBuffer();
	}
	
	public void doGet(HttpServletRequest req,HttpServletResponse resp) throws IOException {
		doPost(req, resp);
	}
}
