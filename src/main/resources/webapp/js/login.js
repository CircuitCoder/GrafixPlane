function login() {
	req = new XMLHttpRequest();
	req.open("POST", "/auth", false)
	req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	req.send("uname=" + document.getElementById("uname").value + "&passwd="
			+ document.getElementById("passwd").value);
	console.log("Return: " + req.responseText);
	if (req.responseText === "0") {
		window.location.href = "/home"; //Login succeed
	} else {
		alert("Wrong User/Password combination!");
	}
}