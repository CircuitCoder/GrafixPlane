<%@page import="tk.circuitcoder.grafixplane.User"%>
<html>
	<body>
		<% 
		HttpSession s=request.getSession(false);
		if(s==null) response.getWriter().println("## not logined!");
		else response.getWriter().println("Hello! You're "+User.getCurrentUser(request).getUsername());
		%>
		
	</body>
</html>