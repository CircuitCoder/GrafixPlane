package tk.circuitcoder.grafixplane.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tk.circuitcoder.grafixplane.GrafixPlane;
import tk.circuitcoder.grafixplane.mail.Mail;
import tk.circuitcoder.grafixplane.user.User;

/**
 * Generates mailbox page & provides mail informations and operations。 Require login 
 * @author lenovo2012-3a
 *
 */
public class MailServlet extends HttpServlet {
	private static final long serialVersionUID = GrafixPlane.VERSION;

	/**
	 * Returns the page
	 * @throws IOException 
	 * @throws ServletException 
	 */
	@Override
	public void doGet(HttpServletRequest req,HttpServletResponse resp) throws IOException, ServletException {
		if(!User.isLogined(req.getSession())) {
			resp.sendRedirect("/login.jsp");
			resp.addCookie(new Cookie("redirect",req.getRequestURI()));
			return;
		}
		
		req.setAttribute("blocker", "0");
		RequestDispatcher reqDis=req.getRequestDispatcher("/mail.jsp");
		reqDis.include(req, resp);
	}
	
	/**
	 * Receive & send mails
	 * @throws IOException 
	 */
	@Override
	public void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException {
		if(!User.isLogined(req.getSession())) {
			resp.sendRedirect("/login.jsp");
			return;
		}
		
		String action=req.getParameter("action");
		
		if(action.equals("send")) {
			//TODO: invalid symbols
			String recstr[]=req.getParameter("to").split("\\|");
			HashSet<Integer> rec=new HashSet<Integer>();
			for(String s:recstr)
				rec.add(Integer.valueOf(s));
			
			try {
				Mail result=Mail.send(User.getCurrentUser(req.getSession()),
						rec,
						req.getParameter("subject"),
						req.getParameter("content"));
				if(result==null)
					resp.getWriter().print(0);
				else resp.getWriter().print(result.MID);
			} catch (SQLException e) {
				e.printStackTrace();
				resp.getWriter().print(0);
			}
		} else if(action.equals("toggleread")) {
			StringBuilder result=new StringBuilder();
			String indexs[]=req.getParameter("index").split("\\|");
			for(int i=0;i<indexs.length;i++) {
				try {
					User.getCurrentUser(req.getSession()).getMManager()
							.toggleUnread(Integer.parseInt(indexs[i]));
					result.append(0);
				} catch(NumberFormatException e) {
					result.append(1);
				}
			}
			resp.getWriter().print(result);
		} else if(action.equals("toggledel")) {
			StringBuilder result=new StringBuilder();
			String indexs[]=req.getParameter("index").split("\\|");
			for(int i=0;i<indexs.length;i++) {
				try {
					if(User.getCurrentUser(req.getSession()).getMManager()
							.toggleDel(Integer.parseInt(indexs[i])))
						result.append(0);
					else result.append(1);
				} catch(NumberFormatException e) {
					result.append(1);
				}
			}
			resp.getWriter().print(result);
		} else if(action.equals("toggleflag")) {
			int index=Integer.parseInt(req.getParameter("index"));
			boolean b=User.getCurrentUser(req.getSession()).getMManager().toggleFlag(index);
			if(b) resp.getWriter().print(0);
			else resp.getWriter().print(1);
		} else if(action.equals("remove")) {
			StringBuilder result=new StringBuilder();
			String indexs[]=req.getParameter("index").split("\\|");
			for(int i=0;i<indexs.length;i++) {
				try {
					if(User.getCurrentUser(req.getSession()).getMManager()
							.remove(Integer.parseInt(indexs[i])))
						result.append(0);
					else result.append(1);
				} catch (NumberFormatException | SQLException e) {
					result.append(1);
				}
			}
			resp.getWriter().print(result);
		}
	}
}
