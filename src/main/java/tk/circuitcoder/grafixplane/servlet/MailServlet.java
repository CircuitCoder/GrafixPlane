package tk.circuitcoder.grafixplane.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
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
		w.write("<link rel=\"stylesheet\" href=\"/styles/default.css\" type=\"text/css\">");
		w.write("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" /></head><body>");
		
		for(int i=0;i<mailList.size();i++) {
			WrappedMail m=mailList.get(i);
			w.println("<div style=\"font-weight:bold;\" id=\"M"+i+"\" class=\"mailRow\" onClick=\"getMail("+i+");\">"+m.toString()+"<br/></div>");
		}
		w.println("<hr/>");
		w.println("Subject: <input class=\"g_input\" type=\"text\" id=\"subject\"/>"
				+ "Content: <input class=\"g_input\" type=\"text\" id=\"content\"/>"
				+ "To: <input class=\"g_input\" type=\"text\" id=\"to\"/>"
				+ "<div class=\"g_button\" onClick=\"sendMail();\">Send</div>");
		
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
		
		if(action.equals("send")) {
			//TODO: invalid symbols
			String recstr[]=req.getParameter("rec").split("\\|");
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
				resp.getWriter().print(m.subject+","+m.content+","+User.getName(m.sender));
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
