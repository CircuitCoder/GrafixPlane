<%@page import="tk.circuitcoder.grafixplane.mail.MailManager"%>
<%@page import="tk.circuitcoder.grafixplane.user.User" %>
<%@page import="tk.circuitcoder.grafixplane.mail.Mailbox.WrappedMail"%>
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
			border-radius: 5px 5px 0 0;
			display: inline;
			margin-bottom: 0;
			font-size:1.5em;
			border-bottom:none;
			width: 20em;
		}
		
		.mcontent {
			margin-top: 0;
			border-radius: 0 0 5px 5px;
			width:50em;
			height:30em;
			resize: none;
		}
		
		.delBtn {
			display:inline;
			font-size:16px;
			float:right;
			margin-top:0;
			margin-bottom:0;
			cursor:pointer;
		}
		
		.rmBtn {
			display:inline;
			font-size:16px;
			float:right;
			margin-top:0;
			margin-bottom:0;
			cursor:pointer;
		}
		
		.msender {
			font-size:1.5em;
		}
		
		.mrec {
			font-size:0.75em;
			color: #777;
		}
		
		.mcont {
			padding-left: 20px;
			white-space: pre;
		}
		</style>
	</head>
	<body class="mailBody">
		<%
		MailManager mails=((User) request.getSession().getAttribute("g_user")).getMManager();
		ArrayList<WrappedMail> mailList;
		String t=request.getParameter("type");
		
		if(t==null) mailList=mails.getMails(0, mails.size(),0,-1,0);
		else if(t.equals("deleted")) mailList=mails.getMails(0,mails.size(),0,1,0);
		else if(t.equals("flagged")) mailList=mails.getMails(0,mails.size(),0,0,1);
		else if(t.equals("unread")) mailList=mails.getMails(0,mails.size(),1,0,0);
		else mailList=mails.getMails(0, mails.size());
		
		String colorStr,tColorStr;
		for(int i=0;i<mailList.size();i++) {
			WrappedMail m=mailList.get(i);
			if(m.getType()=='P') {
				colorStr="#999";
				tColorStr="#CCC";
			}
			else if(m.getType()=='G') {
				colorStr="#FFF";
				tColorStr="#FFF";
			}
			else {	//Same as personal mail
				colorStr="#999";
				tColorStr="#CCC";
			}
		%>
		<div class="mailRow" id="M<%=m.getMID()%>" status="closed">
			<div class="mailTitle" id="MT<%=m.getMID()%>" onclick="titleClick(<%=m.getMID()%>);" style="border-left: <%=tColorStr%> 10px solid; border-top: <%=tColorStr%> 5px solid;">
				<div class="g_checkbox"></div>
				<%=m.flagged()?"<div class=\"g_flag g_flagged\"></div>":"<div class=\"g_flag\"></div>"%>
				<%=m.getMail().subject%>
				<span class="g_button rmBtn"><span class="icon" style="background-image:url('/icons/mail.png'); background-position: -144px 0px;"></span>Remove</span>
				<span class="g_button delBtn"><%=m.deleted()?
						"<span class=\"icon\" style=\"background-image:url('/icons/mail.png'); background-position: -80px 0px;\"></span>Undelete":
						"<span class=\"icon\" style=\"background-image:url('/icons/mail.png'); background-position: -64px 0px;\"></span>Delete"%></span>
			</div>
			<div class="mailCon" id="MC<%=m.getMID()%>" style="border-left: <%=colorStr%> 10px solid;">
				<span class="msender"></span>
				<span class="mrec"></span>
				<div class="mcont"></div>
			</div>
		</div>
		<%} %>
		
		<div class="mailBody">
			<br/>
			<input class="g_input empty_input msubject non_empty" empty_value="Subject" type="text"/><br/>
			<input class="g_input empty_input mto non_empty" empty_value="Receivers" type="text"/><br/>
			<textarea class="g_input empty_input mcontent non_empty" empty_value="Content"/></textarea><br/>
			<input class="g_button msend" onclick="sendMail();" type="submit" value="Send" />
		</div>
	</body>
</html>