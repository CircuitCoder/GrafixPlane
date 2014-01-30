<!DOCTYPE html>
<%@page import="tk.circuitcoder.grafixplane.user.User"%>
<html>
	<head>
		<link rel="stylesheet" type="text/css" href="/styles/default.css" />
		<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
		<title>Login</title>
		<script type="text/javascript" src="/js/jquery.js"></script>
		<script type="text/javascript" src="/js/login.js"></script>
		<script type="text/javascript" src="/js/util.js"></script>
	</head>
	<body class="g_frame">
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
			<input id="uname" type="text" class="g_input empty_input non_empty" empty_value="Username" style="font-size: 1.25em"/><br/>
			<input id="passwd" type="text" class="g_input empty_input non_empty" empty_value="Password" style="font-size: 1.25em"/><br/>
			<input name="submit" type="submit" value="Login" class="g_button main_button" style="font-size: 1.25em" onclick="login()"/>
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