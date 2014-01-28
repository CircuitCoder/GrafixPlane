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

function getMail(index) {
	req = new XMLHttpRequest();
	req.open("POST", "/mail", false);
	req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	req.send("action=read" +
			"&index="+index);
	var resp=req.responseText;
	if(resp==",ERROR") alert("Error occurred!");
	else {
		var data=new Array();
		data=resp.split(",");
		var html="Send from: "+data[0]+"<br/>Subject: "+data[1]+"<br/>Content: "+data[2]+"<br/><br/>";
		var div=document.createElement("div");
		div.style.fontWeight="none";
		div.innerHTML=html;
		document.getElementById("M"+index).appendChild(div);
	}
}