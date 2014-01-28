<%@page import="tk.circuitcoder.grafixplane.MailManager"%>
<%@page import="tk.circuitcoder.grafixplane.User" %>
<%@page import="tk.circuitcoder.grafixplane.Mailbox.WrappedMail" %>
<%@page import="java.util.ArrayList" %>
<!DOCTYPE html>
<html>
	<head>
		<%
		if(request.getAttribute("blocker")==null) response.sendError(403);	//Forbid direct access
		%>
		<link rel="stylesheet" type="text/css" href="/styles/default.css" />
		<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
		<title>Mailbox</title>
		<script src="/js/mail.js"></script>
	</head>
	<body>
		<%
		MailManager mails=((User) request.getSession().getAttribute("g_user")).getMManager();
		ArrayList<WrappedMail> mailList=mails.getMails(0, mails.size());
		for(int i=0;i<mailList.size();i++) {
			WrappedMail m=mailList.get(i);
		%>
		<div class="mailRow" id="M<%=i%>" status="closed">
			<div class="mailTitle" id="MT<%=i%>" onclick="titleClick(<%=i%>);"><%=m.getMail().subject%></div>
			<div class="mailCon" id="MC<%=i%>" style="height: 0;"></div>
		</div>
		<%} %>
	</body>
</html>