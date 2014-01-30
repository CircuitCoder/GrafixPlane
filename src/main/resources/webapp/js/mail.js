function sendMail() {
	$(".msend").css("color","#AAA");
	$(".msend").val("Sending...");
	$(".msend").attr("onclick","");
	var xhr=$.ajax({
		type: "POST",
		url:"/mail",
		async: false,
		dataType: "text",
		data: {
			action: "send",
			to: $(".mto").val(),
			content: $(".mcontent").val(),
			subject: $(".msubject").val()
		}
	}).done(function(data) {
		$(".msend").css("color","inhert")
		$(".msend").attr("onclick","sendMail();");
		if(data!="0") $(".msend").val("Sent!");
		else $(".msent").val("Failed!");
	}).fail(function() {
		$(".msend").css("color","inhert");
		$(".msend").val("Send");
		$(".msend").attr("onclick","sendMail();");
		alert("Request failed!");
	})
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
		return data;
	}
}

function titleClick(index) {
	var parent=document.getElementById("M"+index);
	var cont=document.getElementById("MC"+index);
	if(parent.getAttribute("status")=="closed") {
		if(cont.innerHTML=="") {
			var data=getMail(index);
			
			var rec=data[1].split("|");
			var recStr=new String(rec[0]);
			//Add links
			for(var i=1;i<rec.length;i++) recStr+=","+rec[i];
			
			cont.innerHTML="Send from: "+data[0]+"<br/>Receivers: "+recStr+"<br/>Content: "+data[2]+"<br/><br/>";
		}
		$(cont).slideDown(500);
		parent.setAttribute("status","opened");
	}
	else {
		$(cont).slideUp(500);
		parent.setAttribute("status","closed")
	}
}