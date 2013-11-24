package tk.circuitcoder.eschool.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import tk.circuitcoder.eschool.Eschool;

public class AuthServlet extends HttpServlet {
	private static final long serialVersionUID = Eschool.VERSION;
	
	public void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException {
		String action=req.getParameter("action");
		if(action!=null&&action.equals("logout")) { //logout
			HttpSession session=req.getSession();
			if(session.getAttribute("e_user")==null) resp.getWriter().write("1"); //already logout
			else {
				session.invalidate(); //logout now
				resp.getWriter().write("0");
				//TODO: add an option to jump to login page
			}
		} else { //Otherwise login
			String uname=req.getParameter("uname");
			String passwd=req.getParameter("passwd");
			
			System.out.println("User login: "+uname+" with "+passwd);
			
			if(uname==null||passwd==null) resp.getWriter().write("9"); //Something strange happened
			else if(verify(uname, passwd)) {
				resp.getWriter().write("0"); //login succeed
				req.getSession().setAttribute("e_user", uname);
			}
			else resp.getWriter().write("1"); //login failed
		}
		
		resp.flushBuffer();
	}
	
	public void doGet(HttpServletRequest req,HttpServletResponse resp) throws IOException {
		doPost(req, resp);
	}
	
	private boolean verify(String uname,String passwd) {
		//TODO: Finish IT!
		if(uname.equals("admin")&&passwd.equals("123456")) return true;
		return false;
	}
}
