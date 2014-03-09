package tk.circuitcoder.grafixplane.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tk.circuitcoder.grafixplane.GrafixPlane;
import tk.circuitcoder.grafixplane.file.File;
import tk.circuitcoder.grafixplane.user.User;

public class FileServlet extends HttpServlet{
	private static final long serialVersionUID = GrafixPlane.VERSION;

	@Override
	public void doGet(HttpServletRequest req,HttpServletResponse resp) throws IOException {
		doPost(req,resp);
	}
	
	@Override
	public void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException {
		String action=req.getParameter("action");
		if(action.equals("download")) {
			int FID=Integer.parseInt(req.getParameter("FID"));
			File f;
			try {
				f=File.getFile(FID);
			} catch (SQLException e) {
				e.printStackTrace();
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				resp.flushBuffer();
				return;
			}
			if(f.isAccessible(User.getCurrentUser(req.getSession()).getUID())) {
				resp.setHeader("Content-Disposition", "attachment;filename="+f.fileName());
				byte buffer[]=new byte[10240];
				InputStream is=f.getInputStream();
				OutputStream os=resp.getOutputStream();
				int len;
				while((len=is.read(buffer))!=0) os.write(buffer, 0, len);
				resp.flushBuffer();
			} else {
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
		}
	}
}
