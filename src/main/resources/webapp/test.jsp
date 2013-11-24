<html>
	<body>
		Hello! You're <% 
		HttpSession s=request.getSession(false);
		if(s==null) response.getWriter().println("## not logined!");
		else response.getWriter().println(s.getAttribute("e_user"));
		%>
	</body>
</html>