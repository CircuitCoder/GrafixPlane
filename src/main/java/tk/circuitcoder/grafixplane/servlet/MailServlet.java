package tk.circuitcoder.grafixplane.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

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
	 */
	@Override
	public void doGet(HttpServletRequest req,HttpServletResponse resp) throws IOException {
		if(!User.isLogined(req.getSession())) {
			resp.sendRedirect("/login.jsp");
			return;
		}
		
		MailManager mails=((User) req.getSession().getAttribute("g_user")).getMManager();
		ArrayList<WrappedMail> mailList=mails.getMails(0, mails.size());
		
		PrintWriter w=resp.getWriter();
		w.write("<!DOCTYPE html><html><head><title>Mailbox</title><script src=\"/js/mail.js\"></script>");
		w.write("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" /></head><body>");
		
		Iterator<WrappedMail> it=mailList.iterator();
		while(it.hasNext()) w.println(it.next().toString()+"<br/>");
		w.println("<hr/>");
		w.println("Subject: <input type=\"text\" id=\"subject\"/>"
				+ "Content: <input type=\"text\" id=\"content\"/>"
				+ "To: <input type=\"text\" id=\"to\"/>"
				+ "<div onClick=\"sendMail();\">Send</div>");
		
		w.write("</body></html>");
		
		w.flush();
		w.close();
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
		String recstr[]=req.getParameter("rec").split("\\|");
		HashSet<Integer> rec=new HashSet<Integer>();
		for(String s:recstr)
			rec.add(Integer.valueOf(s));
		
		if(action.equals("send")) {
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
		}
	}
}
