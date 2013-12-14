<!DOCTYPE html>
<%@page import="tk.circuitcoder.eschool.User"%>
<html>
	<head>
		<%
		RequestDispatcher importer=request.getRequestDispatcher("/comp/import.jsp");
		importer.include(request, response);
		%>
		<title>Login</title>
	</head>
	<body>
		<%	
		RequestDispatcher header= request.getRequestDispatcher("/comp/header.jsp"); 
		header.include(request, response);
		if(!User.isLogined(request)) //not currently logined
		{
		%>
		<form action="/auth" method="post">
			<input type="text" name="uname" />
			<input type="password" name="passwd" />
			<input type="submit" class="e_button main_button" name="submit" />
		</form>
		<%
		} else {
		%>
		<h2>Hello, <%=User.getName(request) %></h2>
		<a href="/auth?action=logout" class="e_button main_button">Logout</a>
		<%
		}
		%>
	</body>
</html>