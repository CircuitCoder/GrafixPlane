<!DOCTYPE html>
<%@page import="tk.circuitcoder.grafixplane.User"%>
<html>
	<head>
		<%
		RequestDispatcher importer=request.getRequestDispatcher("/comp/import.jsp");
		importer.include(request, response);
		%>
		<title>Login</title>
		<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
		<script type="text/javascript" src="/js/login.js"></script>
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
		if(!User.isLogined(request.getSession())) //not currently logined
		{
		%>
		
		<div style="text-align:center;">
			Username: <input id="uname" type="text" class="g_input" style="font-size: 1.25em"/><br/>
			Password: <input id="passwd" type="password" class="g_input" style="font-size: 1.25em"/><br/>
			<input name="submit" type="submit" class="g_button main_button" style="font-size: 2em" onclick="login()"/>
		</div>
	
		<%
		} else {
		%>
		<h2>Hello, <%=User.getCurrentUser(request.getSession()).getUsername() %></h2>
		<a href="/auth?action=logout" class="g_button main_button">Logout</a>
		<%
		}
		%>
	</body>
</html>