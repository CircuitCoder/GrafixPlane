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
			width:600px;
			border-radius:0;
			border-bottom:none;
		}
		
		.msubject {
			border-radius: 5px 5px 0 0;
			display: inline;
			margin-bottom: 0;
			font-size:1.5em;
			border-bottom:none;
			width: 600px;
		}
		
		.mcontent {
			margin-top: 0;
			border-radius: 0 0 5px 5px;
			width:600px;
			height:15em;
			resize: none;
		}
		
		.m_button {
			display:table-cell;
			font-size:16px;
			margin: 0 3px 0 0;
			float: right;
			padding:3px 5px 3px 5px;
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
		
		#mailCtrl {
			padding-left:18px;
		}
		
		#mailSend {
			display:none;
			position: fixed;
			top: 0;
			left:0;
			width:100%;
			height:100%;
			
			background-color: rgba(0,0,0,0.5);
			text-align:center;
		}
		
		#mailSendPane {
			display:inline-block;
			margin: auto;
			
			margin-top:50px;
			text-align:center;
			
			padding: 15px;
			background-color:white;
			border-radius:10px;
			border: #AAA 3px solid;
		}
		</style>
	</head>
	
	<%
	MailManager mails=((User) request.getSession().getAttribute("g_user")).getMManager();
	ArrayList<WrappedMail> mailList;
	String t=request.getParameter("type");

	if(t==null) mailList=mails.getMails(0, mails.size(),0,-1,0);
	else if(t.equals("deleted")) mailList=mails.getMails(0,mails.size(),0,1,0);
	else if(t.equals("flagged")) mailList=mails.getMails(0,mails.size(),0,0,1);
	else if(t.equals("unread")) mailList=mails.getMails(0,mails.size(),1,0,0);
	else mailList=mails.getMails(0, mails.size());
	%>
	
	<body class="mailBody">
		<div class="mailPanel">
			<div id="mailCtrl">
				<div class="g_checkbox" id="checkAll"></div>
				<button class="g_button" id="openSend">New</button>
				<%= (t!=null&&t.equals("deleted"))?
						"<button class=\"g_button requireSelect\" id=\"delAll\" onclick=\"delAll()\">Undelete</button>":
						"<button class=\"g_button requireSelect\" id=\"delAll\" onclick=\"delAll()\">Delete</button>"%>
				<button class="g_button requireSelect" id="rmAll" onclick="rmAll()">Remove</button> 
			</div>
			<%
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
					<span mid=<%=m.getMID()%> class="g_button m_button rmBtn"><span class="icon" style="background-image:url('/icons/mail.png'); background-position: -144px 0px;"></span>Remove</span>
					<span mid=<%=m.getMID()%> class="g_button m_button delBtn"><%=m.deleted()?
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
		</div>
		
		<div id="mailSend">
			<div id="mailSendPane">
				<input class="g_input empty_input msubject non_empty" empty_value="Subject" type="text"/><br/>
				<input class="g_input empty_input mto non_empty" empty_value="Receivers" type="text"/><br/>
				<textarea class="g_input empty_input mcontent non_empty" empty_value="Content"/></textarea><br/>
				<button class="g_button main_button msend" onclick="sendMail();">Send</button>
				<button class="g_button mclose" onclick="$('#mailSend').fadeOut(500);">Close</button>
			</div>
		</div>
	</body>
</html>