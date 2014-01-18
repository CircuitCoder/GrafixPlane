<%@page import="tk.circuitcoder.grafixplane.User"%>
<html>
	<body>
		<% 
		if(!User.isLogined(request.getSession())) response.getWriter().println("## not logined!");
		else response.getWriter().println("Hello! You're "+User.getCurrentUser(request.getSession()).getUsername());
		
		response.getWriter().println(String.format("There are %d active session online.",User.getSessionCount()));
		%>
		
	</body>
</html>