package tk.circuitcoder.eschool.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tk.circuitcoder.eschool.Eschool;

public class ControlServlet extends HttpServlet {
	private static final long serialVersionUID = Eschool.VERSION;
	
	public void doGet(HttpServletRequest req,HttpServletResponse resp) throws IOException {
		resp.setCharacterEncoding("UTF-8");
		resp.getWriter().println("Hello");
	}
}
