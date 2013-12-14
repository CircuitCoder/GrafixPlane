package tk.circuitcoder.eschool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class User {
	private User() {}
	
	public static boolean isLogined(HttpServletRequest req) {
		return !(getName(req)==null);
	}
	
	public static String getName(HttpServletRequest req) {
		HttpSession session=req.getSession();
		if(session.isNew()) session.setMaxInactiveInterval(600);
		return (String) session.getAttribute("e_user");
	}
}
