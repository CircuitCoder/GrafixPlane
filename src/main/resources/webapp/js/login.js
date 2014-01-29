$(document).ready(function() {
	$("#passwd").focusin(function() {
			$(this).attr("type","password");
	})
	$("#passwd").focusout(function() {
		if($(this).val()=="") $(this).attr("type","text");
	})
	
	$("#uname,#passwd").keydown(function(e) {
		if(e.which==13) {
			login();
		}
	})
})

function login() {
	req = new XMLHttpRequest();
	req.open("POST", "/auth", false)
	req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	req.send("uname=" + document.getElementById("uname").value + "&passwd="
			+ document.getElementById("passwd").value);
	console.log("Return: " + req.responseText);
	if (req.responseText == "0") {
		window.location.href = "/home"; //Login succeed
	} else if(req.responseText ==2) {
		alert("already logined!");
		window.location.href = "/home";
	} else {
		alert("Wrong User/Password combination!");
	}
}