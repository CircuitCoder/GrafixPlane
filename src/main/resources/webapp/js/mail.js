function sendMail() {
	if(!checkEmpty()) return;
	
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
	var cont=$("#MC"+index+" > .mcont");
	if(parent.getAttribute("status")=="closed") {
		if(cont.html()=="") {
			var data=getMail(index);
			
			var rec=data[1].split("|");
			var recStr=new String("To: "+rec[0]);
			//Add links
			for(var i=1;i<rec.length;i++) recStr+=", "+rec[i];
			
			$("#MC"+index+" > .msender").html(data[0]);
			$("#MC"+index+" > .mrec").html(recStr);
			cont.html(data[2]);
		}
		$("#MC"+index).slideDown(500);
		parent.setAttribute("status","opened");
	}
	else {
		$("#MC"+index).slideUp(500);
		parent.setAttribute("status","closed")
	}
}

function del(index) {
	var ori=$("#MT"+index+" .delBtn").val();
	
	var xhr=$.ajax({
		type: "POST",
		url:"/mail",
		async: false,
		dataType: "text",
		data: {
			action: 'toggledel',
			index: index
		}
	}).done(function(data) {
		$("#MT"+index+" .delBtn").css("color","inhert")
		if(data=="0") $("#M"+index).fadeOut(500,function() {
			$("#M"+index).remove();
		});
		else $("#MT"+index+" .delBtn").val("Failed!");
	}).fail(function() {
		$("#MT"+index+" .delBtn").css("color","inhert");
		$("#MT"+index+" .delBtn").val(ori);
		alert("Request failed!");
	})
}

function rm(index) {
	var ori=$("#MT"+index+" .rmBtn").val();
	
	var xhr=$.ajax({
		type: "POST",
		url:"/mail",
		async: false,
		dataType: "text",
		data: {
			action: 'remove',
			index: index
		}
	}).done(function(data) {
		$("#MT"+index+" .rmBtn").css("color","inhert")
		if(data=="0") $("#M"+index).fadeOut(500,function() {
			$("#M"+index).remove();
		});
		else $("#MT"+index+" .rmBtn").val("Failed!");
	}).fail(function() {
		$("#MT"+index+" .rmBtn").css("color","inhert");
		$("#MT"+index+" .rmBtn").val(ori);
		alert("Request failed!");
	})
}

function flag(index) {
	var ori=$("#MT"+index+" > .flagBtn").val();
	
	var xhr=$.ajax({
		type: "POST",
		url:"/mail",
		async: false,
		dataType: "text",
		data: {
			action: 'toggleflag',
			index: index
		}
	}).done(function(data) {
		$("#MT"+index+" > .flagBtn").css("color","inhert")
		if(data!="0") $("#MT"+index+" > .delBtn").val("Failed!");
	}).fail(function() {
		$("#MT"+index+" > .flagBtn").css("color","inhert");
		$("#MT"+index+" > .flagBtn").val(ori);
		alert("Request failed!");
	})
}

$(document).ready(function() {
	$(".delBtn").each(function() {
		var mid=$(this).attr("mid");
		$(this).click(function(e) {
			$(this).css("color","#AAA");
			$(this).val("...");
			del(mid);
			e.stopPropagation()
		})
	})
	
	$(".rmBtn").each(function() {
		var mid=$(this).attr("mid");
		$(this).click(function(e) {
			$(this).css("color","#AAA");
			$(this).val("...");
			rm(mid);
			e.stopPropagation()
		})
	})
	
	$('.g_flag').each(function(index) {
		$(this).click(function(e) {
			var mid=$(this).parent().attr("id").substring(2);
			flag(mid);
			if($(this).hasClass('g_flagged')) $(this).removeClass('g_flagged');
			else $(this).addClass('g_flagged');
			e.stopPropagation();
		})
	})
})
