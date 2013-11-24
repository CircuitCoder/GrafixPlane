<!DOCTYPE html>
<html>
	<head>
		<title>Login</title>
	</head>
	<body>
		<%	RequestDispatcher header= request.getRequestDispatcher("/comp/header.jsp"); 
			header.include(request, response);
		%>
		<form action="/auth" method="post">
			<input type="text" name="uname" />
			<input type="password" name="passwd" />
			<input type="submit" name="submit" />
		</form>
		<hr/>
		<a href="/auth?action=logout">Logout</a>
	</body>
</html>