package tk.circuitcoder.grafixplane.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.server.UID;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tk.circuitcoder.grafixplane.GrafixPlane;
import tk.circuitcoder.grafixplane.Mail;
import tk.circuitcoder.grafixplane.MailManager;
import tk.circuitcoder.grafixplane.Mailbox.WrappedMail;
import tk.circuitcoder.grafixplane.User;

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
			return;
		}
		
		MailManager mails=((User) req.getSession().getAttribute("g_user")).getMManager();
		ArrayList<WrappedMail> mailList=mails.getMails(0, mails.size());
		
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
		} else if(action.equals("read")) {
			int index=Integer.parseInt(req.getParameter("index"));
			Mail m=User.getCurrentUser(req.getSession()).getMManager().getMail(index).getMail();
			try {
				resp.getWriter().print(User.getName(m.sender)+","+m.subject+","+m.content);
			} catch (SQLException e) {
				resp.getWriter().print(",ERROR");
				e.printStackTrace();
			}
		} else if(action.equals("delete")) {
			int index=Integer.parseInt(req.getParameter("index"));
			boolean b=User.getCurrentUser(req.getSession()).getMManager().delete(index);
			if(b) resp.getWriter().print(0);
			else resp.getWriter().print(1);
		} else if(action.equals("remove")) {
			int index=Integer.parseInt(req.getParameter("index"));
			boolean b;
			try {
				b = User.getCurrentUser(req.getSession()).getMManager().remove(index);
				if(b) resp.getWriter().print(0);
				else resp.getWriter().print(1);
			} catch (SQLException e) {
				resp.getWriter().print(1);
				e.printStackTrace();
			}
		}
	}
}
