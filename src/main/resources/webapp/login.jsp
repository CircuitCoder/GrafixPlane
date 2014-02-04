<!DOCTYPE html>
<%@page import="tk.circuitcoder.grafixplane.user.User"%>
<%@page import="tk.circuitcoder.grafixplane.GrafixPlane"%>
<%@page import="java.util.ResourceBundle" %>
<%!
ResourceBundle bundle;

public void jspInit() {
	bundle=GrafixPlane.getGP().getTranslation();
}
%>
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
			<input id="uname" type="text" class="g_input empty_input non_empty" empty_value="<%=bundle.getString("user.uname")%>" style="font-size: 1.25em"/><br/>
			<input id="passwd" type="text" class="g_input empty_input non_empty" empty_value="<%=bundle.getString("user.passwd") %>" style="font-size: 1.25em"/><br/>
			<input name="submit" type="submit" value="<%=bundle.getString("user.login")%>" class="g_button main_button" style="font-size: 1.25em" onclick="login()"/>
		</div>
	
		<%
		} else {
		%>
		<h2>Hello, <%=User.getCurrentUser(request.getSession()).getUsername() %></h2>
		<a href="/auth?action=logout" class="g_button main_button"><%=bundle.getString("user.logout")%></a>
		<%
		}
		%>
	</body>
</html>