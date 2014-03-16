<%@page import="java.lang.ProcessBuilder.Redirect"%>
<%@page import="tk.circuitcoder.grafixplane.user.User"%>
<%@page import="tk.circuitcoder.grafixplane.file.File"%>
<%@page import="tk.circuitcoder.grafixplane.file.File.Folder"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat" %>
<!DOCTYPE html>
<%
	if(!User.isLogined(request.getSession())) {
		response.sendRedirect("/login.jsp");
		response.addCookie(new Cookie("redirect",request.getRequestURI()));
		return;
	}
	String dir=request.getParameter("dir");
	if(dir==null) dir="/";
	ArrayList<Set> l=File.getAllFile(User.getCurrentUser(request.getSession()).getUID(),dir);
%>
<html>
	<head>
		<title>Files - <%=dir%></title>
		<link rel="stylesheet" type="text/css" href="/styles/default.css" />
		<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
		<script type="text/javascript">
			var	_DIR="<%=dir%>";
		</script>
		<script src="/js/jquery.js" type="text/javascript"></script>
		<script src="/js/util.js" type="text/javascript"></script>
		<script src="/js/jquery.ui.widget.js"></script>
		<script src="/js/jquery.iframe-transport.js"></script>
		<script src="/js/jquery.fileupload.js"></script>
		<script src="/js/file.js" type="text/javascript"></script>
	</head>
	<body>
		<div id="uploadSec">
			<input id="upload" type="file" name="files[]" data-url="/file" data-form-data='{"dir":"<%=dir%>"}' multiple>
		</div>
		<hr/>
		<%
			SimpleDateFormat t=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			Iterator<Folder> it=l.get(1).iterator();
			while(it.hasNext()) {
				Folder f=it.next();
				String time=t.format(new Date(f.getLastMod()));
		%>
		<a href="/file.jsp?dir=<%=dir+f.getDir()+"/"%>" style="font-color: gray"><%=f.getDir()%> | <%=time%></a><br/>
		<%
			}
			Iterator<File>it2=l.get(0).iterator();
			while(it2.hasNext()) {
				File f=it2.next();
		%>
			<a href="/file?FID=<%=f.FID()%>"><%=f.fileName()%> | <%=f.formattedTime()%></a><br/>
		<%
			}
		%>
	</body>
</html>