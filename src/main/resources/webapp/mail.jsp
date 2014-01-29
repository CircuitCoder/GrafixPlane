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
		<script src="/js/jquery.js" type="text/javascript"></script>
		<script src="/js/mail.js" type="text/javascript"></script>
		<script src="/js/util.js" type="text/javascript"></script>
		<style>
		.mto {
			margin-bottom: 0;
			margin-top: 0;
			width:50em;
			border-radius:0 5px 0 0;
			border-bottom:none;
		}
		
		.msubject {
			border-radius: 0;
			display: inline;
			margin-bottom: 0;
			font-size:1.5em;
			border-bottom:none;
		}
		
		.mcontent {
			margin-top: 0;
			border-radius: 0 0 5px 5px;
			width:50em;
			height:30em;
			resize: none;
		}
		</style>
	</head>
	<body class="mailBody">
		<%
		MailManager mails=((User) request.getSession().getAttribute("g_user")).getMManager();
		ArrayList<WrappedMail> mailList=mails.getMails(0, mails.size());
		for(int i=0;i<mailList.size();i++) {
			WrappedMail m=mailList.get(i);
		%>
		<div class="mailRow" id="M<%=i%>" status="closed">
			<div class="mailTitle" id="MT<%=i%>" onclick="titleClick(<%=i%>);"><%=m.getMail().subject%></div>
			<div class="mailCon" id="MC<%=i%>"></div>
		</div>
		<%} %>
		
		<div class="mailBody">
			<br/>
			<input class="g_input empty_input msubject" empty_value="Subject" type="text"/><br/>
			<input class="g_input empty_input mto" empty_value="Receivers" type="text"/><br/>
			<textarea class="g_input empty_input mcontent" empty_value="Content"/></textarea><br/>
			<input class="g_button msend" onclick="sendMail();" type="submit" value="Send" />
		</div>
	</body>
</html>