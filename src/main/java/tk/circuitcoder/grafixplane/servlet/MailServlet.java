package tk.circuitcoder.grafixplane.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tk.circuitcoder.grafixplane.GrafixPlane;
import tk.circuitcoder.grafixplane.mail.Mail;
import tk.circuitcoder.grafixplane.mail.Mailbox.WrappedMail;
import tk.circuitcoder.grafixplane.user.User;

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
			WrappedMail m=User.getCurrentUser(req.getSession()).getMManager().getMail(index);
			if(m.unread()) m.read();
			
			Iterator<String> rec;
			try {
				rec=User.getNames(m.getMail().receivers).iterator();
			} catch (SQLException e1) {
				resp.getWriter().print(",ERROR");
				e1.printStackTrace();
				return;
			}
			
			StringBuilder recStr=new StringBuilder(rec.next());
			
			while(rec.hasNext()) recStr.append('|').append(rec.next());
			
			try {
				resp.getWriter().print(User.getName(m.getMail().sender)+','+recStr+','+m.getMail().content);
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
