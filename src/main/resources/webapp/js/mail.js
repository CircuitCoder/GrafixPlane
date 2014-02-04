var checked=0;
var checkCount=0;
var sideBarOpen=true;

function sendMail() {
	if(!checkEmpty()) return;
	var btn=$(".msend");
	
	btn.css("color","#AAA");
	btn.html("Sending...");
	btn.attr("onclick","");
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
		btn.css("color","inhert")
		btn.attr("onclick","sendMail();");
		if(data!="0") {
			btn.html(SENTSUC);
			$("#mailSend").fadeOut(500,function() {
				$(".mto").val("");
				$(".mcontent").val(""),
				$(".msubject").val("");
				$(".msend").html(SENTORI);
			});
		}
		else btn.html(SENTFAIL);
	}).fail(function() {
		btn.css("color","inhert");
		btn.html(SENTORI);
		btn.attr("onclick","sendMail();");
		alert(REQFAIL);
	})
}

function unread(index) {
	var xhr=$.ajax({
		type: "POST",
		url:"/mail",
		async: false,
		dataType: "text",
		data: {
			action: 'toggleread',
			index: index
		}
	}).done(function(data) {
		var slot=$("#MT"+index);
		if(slot.hasClass("unreadTitle")) slot.removeClass("unreadTitle");
		else slot.addClass("unreadTitle");
	})
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
			if($("#MT"+index+" > .g_checkbox").hasClass("g_checked"))
				if((--checked)==0) $(".requireSelect").hide();
			$("#M"+index).remove();
			--checkCount;
			if(checked==checkCount&&checkCount!=0) $("#checkAll").addClass("g_checked");
			if(checkCount==0) $("#checkAll").removeClass("g_checked");
		});
		else $("#MT"+index+" .delBtn").val(DELFAIL);
	}).fail(function() {
		$("#MT"+index+" .delBtn").css("color","inhert");
		$("#MT"+index+" .delBtn").val(ori);
		alert(REQFAIL);
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
			if($("#MT"+index+" > .g_checkbox").hasClass("g_checked")) 
				if((--checked)==0) $(".requireSelect").hide();
			$("#M"+index).remove();
			--checkCount;
			if(checked==checkCount&&checkCount!=0) $("#checkAll").addClass("g_checked");
			if(checkCount==0) $("#checkAll").removeClass("g_checked");
		});
		else $("#MT"+index+" .rmBtn").val(RMFAIL);
	}).fail(function() {
		$("#MT"+index+" .rmBtn").css("color","inhert");
		$("#MT"+index+" .rmBtn").val(ori);
		alert(REQFAIL);
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
		alert(REQFAIL);
	})
}

function delAll() {
	var slots=new String();
	var rows=new Array()
	$('.g_checkbox.g_checked').each(function() {
		if($(this).attr("id")=="checkAll") return;
		slots=slots.concat($(this).parent().attr("id").substring(2)+"|");
		rows.push($(this));
	})
	
	var xhr=$.ajax({
		type: "POST",
		url:"/mail",
		async: false,
		dataType: "text",
		data: {
			action: 'toggledel',
			index: slots.substr(0,slots.length-1)
		}
	}).always(function(data) {
		for(var i=0;i<data.length;i++) {
			if(data.charAt(i)=='0'){
				rows[i].parent().parent().fadeOut(500,function() {
					if(rows[i].hasClass("g_checked"))
						if((--checked)==0) $(".requireSelect").hide();
					$("#M"+index).remove();
					--checkCount;
					if(checked==checkCount&&checkCount!=0) $("#checkAll").addClass("g_checked");
					if(checkCount==0) $("#checkAll").removeClass("g_checked");
				});
			}
			else rows[i].css("background-color","#FEE");
		}
	}).fail(function(data) {
		alert(REQFAIL);
	})
}

function rmAll() {
	$('.g_checkbox.g_checked').each(function() {
		if($(this).attr("id")=="checkAll") return;
		rm($(this).parent().attr("id").substring(2));
	})
}

function titleClick(index) {
	var parent=document.getElementById("M"+index);
	var cont=$("#MC"+index+" > .mcont");
	if($("#MT"+index).hasClass("unreadTitle")) unread(index);
	if(parent.getAttribute("status")=="closed") {
		$("#MC"+index).slideDown(500);
		parent.setAttribute("status","opened");
	}
	else {
		$("#MC"+index).slideUp(500);
		parent.setAttribute("status","closed")
	}
}

function switchType(type) {
	//TODO: do not refreash the page
	if(type=='') window.location.href = "/mail";
	else window.location.href="/mail?type="+type;
}

function openSidebar() {
	$("#sidePanel").css("left","0");
	$("#mailPanel").css("margin-left","250px");
	sideBarOpen=true;
	
	$("#toggleSidebar").css("border-radius","5px 5px 0 0");
	
	$("#toggleSidebar").css("-webkit-transform","rotate(270deg)");
	$("#toggleSidebar").css("-ms-transform","rotate(270deg)");
	$("#toggleSidebar").css("transform","rotate(270deg)");
}

function closeSidebar() {
	$("#sidePanel").css("left","-210px");
	$("#mailPanel").css("margin-left","40px");
	sideBarOpen=false;
	
	$("#toggleSidebar").css("border-radius","0 0 5px 5px");
	
	$("#toggleSidebar").css("-webkit-transform","rotate(90deg)");
	$("#toggleSidebar").css("-ms-transform","rotate(90deg)");
	$("#toggleSidebar").css("transform","rotate(90deg)");
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
	
	$('.g_checkbox').each(function(index) {
		if($(this).attr("id")=="checkAll") return;
		++checkCount;
		$(this).click(function(e) {
			if($(this).hasClass('g_checked')) {
				$(this).removeClass('g_checked');
				if((--checked)==0) $(".requireSelect").hide();
				//TODO: add half-select for select all
				$("#checkAll").removeClass("g_checked");
			}
			else {
				$(this).addClass('g_checked');
				if((++checked)==checkCount) $("#checkAll").addClass("g_checked");
				$(".requireSelect").show();
			}
			e.stopPropagation();
		})
	})
	
	$('#checkAll').click(function(e) {
		
		if($(this).hasClass('g_checked')) {
			$('.g_checkbox').removeClass("g_checked");
			checked=0;
			$(".requireSelect").hide();
		}
		else {
			if(checkCount==0) return;
			$('.g_checkbox').each(function(e) {
				if(!$(this).hasClass('g_checked')) $(this).addClass("g_checked");
			})
			checked=checkCount;
			$(".requireSelect").show();
		}
	})
	
	$('#openSend').click(function(e) {
		$('#mailSend').fadeIn(500);
	})
	
	var currentType=$("#sidePanel").attr("current");
	var current=$("#entry_"+currentType);
	current.addClass("entry_current");
	current.attr("onclick","closeSidebar()");
	
	$("#toggleSidebar").click(function(e) {
		if(sideBarOpen) {
			closeSidebar();
		}
		else {
			openSidebar();
		}
		e.stopPropagation();
	})
})
