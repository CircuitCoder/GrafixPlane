package tk.circuitcoder.grafixplane.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import tk.circuitcoder.grafixplane.GrafixPlane;
import tk.circuitcoder.grafixplane.user.User;

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
			if(!User.isLogined(session)) resp.getWriter().write("1"); //already logout
			else {
				User.getCurrentUser(session).logoutSession(session);
				resp.getWriter().write("0");
			}
		} else { //Otherwise login
			String uname=req.getParameter("uname");
			String passwd=req.getParameter("passwd");
			
			GrafixPlane.getGP().getLogger().debug("User login: "+uname+" with "+passwd);
			
			if(uname==null||passwd==null) resp.getWriter().write("9"); //Something strange happened
			else if(User.isLogined(req.getSession())) resp.getWriter().write(2);
			else if(User.verify(uname, passwd)) {
				resp.getWriter().write("0"); //login succeed
				try {
					User.getUser(uname).loginSession(req.getSession());
				} catch (SQLException e) {
					resp.getWriter().write("#E:"+e.getLocalizedMessage());
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
