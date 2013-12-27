<!DOCTYPE html>
<%@page import="tk.circuitcoder.grafixplane.User"%>
<html>
	<head>
		<%
		RequestDispatcher importer=request.getRequestDispatcher("/comp/import.jsp");
		importer.include(request, response);
		%>
		<title>Login</title>
		<script type="text/javascript">
			function login() {
				req=new XMLHttpRequest();
				req.open("POST","/auth",false)
				req.setRequestHeader("Content-type","application/x-www-form-urlencoded");
				req.send("uname="+document.getElementById("uname").value+"&passwd="+document.getElementById("passwd").value);
				console.log("Return: "+req.responseText);
				if(req.responseText === "0") {
					window.location.href="/home"; //Login succeed
				} else {
					alert("Wrong User/Password combination!");
				}
			}
		</script>
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
		
		<div style="text-align:center;">
			Username: <input id="uname" type="text" class="g_input" style="font-size: 1.25em"/><br/>
			Password: <input id="passwd" type="password" class="g_input" style="font-size: 1.25em"/><br/>
			<input name="submit" type="submit" class="g_button main_button" style="font-size: 2em" onclick="login()"/>
		</div>
	
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