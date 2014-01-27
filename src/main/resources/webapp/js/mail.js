function sendMail() {
	req = new XMLHttpRequest();
	req.open("POST", "/mail", false);
	req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	req.send("action=send"
			+ "&subject="+document.getElementById("subject").value
			+ "&content="+document.getElementById("content").value
			+ "&rec="+document.getElementById("to").value);
	
	if(req.responseText!="0") alert("Mail Sent: "+req.responseText);
	else alert("Mail not sent!");
}