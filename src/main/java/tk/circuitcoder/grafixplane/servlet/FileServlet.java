package tk.circuitcoder.grafixplane.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import tk.circuitcoder.grafixplane.GrafixPlane;
import tk.circuitcoder.grafixplane.file.File;
import tk.circuitcoder.grafixplane.user.User;

public class FileServlet extends HttpServlet{
	private static final long serialVersionUID = GrafixPlane.VERSION;

	@Override
	public void doGet(HttpServletRequest req,HttpServletResponse resp) throws IOException, ServletException {
		if(ServletFileUpload.isMultipartContent(req)) {
			doPost(req, resp);
			return;
		}
		
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
	
	@Override
	public void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException, ServletException {
		//FROM: https://github.com/klaalo/jQuery-File-Upload-Java/blob/master/src/info/sudr/file/UploadServlet.java
		if(!ServletFileUpload.isMultipartContent(req)) {
			doGet(req,resp);
			return;
		}
		
		ServletFileUpload upload=new ServletFileUpload(new DiskFileItemFactory());
		PrintWriter output=resp.getWriter();
		JSONArray result=new JSONArray();
		
		List<FileItem> files=null;
		try {
			files = upload.parseRequest(req);
		} catch (FileUploadException e1) {
			output.write("{\"error\": \"Invalid Request\"}");
			output.close();
			e1.printStackTrace();
			return;
		}
		String dir=files.get(0).getString();
		for(FileItem i:files) {
			if(!i.isFormField()) {
				try {
					File f=File.newFile(User.getCurrentUser(req.getSession()).getUID(),dir+"/"+i.getName());
					if(f==null) {
						JSONObject slot=new JSONObject();
						slot.put("name",i.getName());
						slot.put("size",i.getSize());
						slot.put("error","File duplicated");
						result.add(slot);
						continue;
					}
					GrafixPlane.getGP().getLogger().info("Uploading file: "+f.getFile().getAbsolutePath()+" with dir "+dir+"/"+i.getName());
					i.write(f.getFile());
					JSONObject slot=new JSONObject();
					slot.put("name",i.getName());
					slot.put("size",i.getSize());
					slot.put("url","/file?FID="+f.FID());
					slot.put("delete_url","/file?FID="+f.FID());
					slot.put("delete_type","DELETE");
					//TODO: thumbnail
					result.add(slot);
				} catch (Exception e) {
					JSONObject slot=new JSONObject();
					slot.put("name",i.getName());
					slot.put("size",i.getSize());
					slot.put("error","Server Side Exception");
					result.add(slot);
					e.printStackTrace();
				}
			}
		}
		output.write(result.toString());
		output.close();
	}
	
	@Override
	public void doDelete(HttpServletRequest req,HttpServletResponse resp) {
		
	}
}
