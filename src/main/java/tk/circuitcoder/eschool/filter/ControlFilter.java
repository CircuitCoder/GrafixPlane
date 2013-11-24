package tk.circuitcoder.eschool.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.Filter;

public class ControlFilter implements Filter {
	
	private String host;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		host=filterConfig.getInitParameter("host");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if(!(request instanceof HttpServletRequest)) chain.doFilter(request, response);
		HttpServletRequest req=(HttpServletRequest) request;
		HttpServletResponse resp=(HttpServletResponse) response;
		
		resp.setCharacterEncoding("UTF-8");
		
		String uri=req.getRequestURI();
		if(uri.length()!=1&&uri.endsWith("/")) uri=uri.substring(0, uri.length()-1);
		
		System.out.println("Client "+req.getRemoteAddr()+" requested "+uri);
		HttpSession session=req.getSession();
		if(session.isNew()) session.setMaxInactiveInterval(600);
		if(!uri.equals("/login.jsp")&&!uri.equals("/auth")&&session.getAttribute("e_user")==null) {
			resp.sendRedirect("/login.jsp");
			return;
		}
		else if(uri.equals("/")) {
			resp.sendRedirect("/index.jsp");
			return;
		}
		
		req.setAttribute("e_host", host);
		
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {	}
}
