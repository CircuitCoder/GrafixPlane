<!DOCTYPE html>
<%@page import="tk.circuitcoder.grafixplane.User"%>
<html>
	<head>
		<%
		RequestDispatcher importer=request.getRequestDispatcher("/comp/import.jsp");
		importer.include(request, response);
		%>
		<title>Login</title>
	</head>
	<body class="g_frame">
		<%	
		RequestDispatcher header= request.getRequestDispatcher("/comp/header.jsp"); 
		header.include(request, response);
		%>
		
		<div id="title" class="g_title">
		GrafixPlane
		</div>
		<br/>
		<hr/>
		
		<%
		if(!User.isLogined(request)) //not currently logined
		{
		%>
		
		<form action="/auth" method="post" style="text-align:center;">
			Username: <input type="text" name="uname" class="g_input" style="font-size: 1.25em"/><br/>
			Password: <input type="password" name="passwd" class="g_input" style="font-size: 1.25em"/><br/>
			<input type="submit" class="g_button main_button" name="submit" style="font-size: 2em"/>
		</form>
		<%
		} else {
		%>
		<h2>Hello, <%=User.getName(request) %></h2>
		<a href="/auth?action=logout" class="g_button main_button">Logout</a>
		<%
		}
		%>
	</body>
</html>