package tk.circuitcoder.eschool.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.Filter;

import tk.circuitcoder.eschool.Eschool;

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
		
		Eschool.getEschool().getLogger().info("Client "+req.getRemoteAddr()+" requested "+uri);
		
		req.setAttribute("e_host", host);
		req.setAttribute("e_uri", uri);
		
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {	}
}
