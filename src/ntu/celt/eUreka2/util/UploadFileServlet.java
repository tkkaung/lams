package ntu.celt.eUreka2.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.resources.ResourceFile;
import ntu.celt.eUreka2.modules.resources.ResourceFileVersion;
import ntu.celt.eUreka2.modules.resources.ResourceFolder;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

/* use to upload files in 'Resource' Module */
public class UploadFileServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private SessionFactory sf;
	private SessionFactory getSessionFactory() {
		return sf;
	}
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			sf = new Configuration().configure().buildSessionFactory();
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse res) {
		processing(req, res);
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) {
		processing(req, res);
	}
	
	
	private void processing(HttpServletRequest req, HttpServletResponse res) {
		String out = "";
		
		String sessionId = req.getSession(false).getId();
		String pid = req.getParameter("pid");	//project ID
		String fid = req.getParameter("fid");  //folder ID
		String uid = req.getParameter("uid");  //user ID
		
		try {
			
			if (pid == null || fid==null || uid==null) {
				//throw new RuntimeException("There is no Project being selected.");
				res.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, "Required parameter(s) is missing");
			    return;
			}
			
			int _uid = Integer.parseInt(uid);
			Map<String, String> params = new HashMap<String, String>();
			
			//file name location:  /<pid>/<module name>/<parent folder>/<filename>
			String tempDir = Config.getString(Config.VIRTUAL_DRIVE) + "/temp/";
			String rootDir = Config.getString(Config.VIRTUAL_DRIVE) + "/" + pid + "/"+PredefinedNames.MODULE_RESOURCE;
			File rootFolder = new File(rootDir);
			if (!rootFolder.exists() || !rootFolder.isDirectory()) {
				rootFolder.mkdirs();
			}
			File dirFile = new File(rootFolder + "/" + fid);
			if (!dirFile.exists() || !dirFile.isDirectory()) {
				dirFile.mkdirs();
			}
			
			int maxPostSizeBytes = 2000 * 1024 * 1024; //2GB
			MultipartParser mp;

		
			mp = new MultipartParser(req, maxPostSizeBytes);
			Part part = null;
			
			while ((part = mp.readNextPart()) != null) {
				String name = part.getName();
				if (part.isParam()) {
					// it's a parameter part
					ParamPart paramPart = (ParamPart) part;
					String value = paramPart.getStringValue();

					out += "\n param; " + name + "=" + value;
					
					params.put(name, value);
				}
				else if (part.isFile()) { // file part: 
					FilePart filePart = (FilePart) part;
					String fileName = filePart.getFileName();
					
					//save to temp folder with name:  sessionId-fileId-partitionIndex
					File tempFile = new File(tempDir + sessionId + params.get("fileId")+params.get("partitionIndex"));
					if (fileName != null) { //save file
						long size = filePart.writeTo(tempFile);
						params.put("contentType", filePart.getContentType());
						
						out += ("\n file; name=" + name + "; filename=" + fileName +
								  ", filePath=" + filePart.getFilePath() +
								  ", content type=" + filePart.getContentType() +
								  ", size=" + size);
					}
					else {
						// the field did not contain a file
						out += "\n  --file; name=" + name + "; EMPTY";
					}
				}
			}
		
			// check if we have collected all partitions properly
			boolean all_in_place = true;
			long partitions_length = 0;
			for( int i = 0; all_in_place && i < Integer.parseInt(params.get("partitionCount")); i++ ) {
				File tempFile = new File(tempDir + sessionId + params.get("fileId")+i);
				if(tempFile.exists()){
					partitions_length += tempFile.length();
				} else{
					all_in_place = false;
				}
			}
	
			//issue error if last partition uploaded, but partitions validation failed
			if( Integer.parseInt(params.get("partitionIndex")) == Integer.parseInt(params.get("partitionCount")) - 1 
					&& ( !all_in_place || partitions_length != Long.parseLong(params.get("fileLength")))) {
//			    throw new IOException("Upload file validation error");
			    res.sendError(HttpServletResponse.SC_CONFLICT, "Upload file validation error");
			    return;
			}
	
			//reconstruct original file if all ok
			if( all_in_place ) {
				int BUFFER = 1024;
				int count;
				
				String fileName = Util.decode(params.get("fileName"));
				
		//		String orgFileName = fileName;
				while(checkFileNameExist(fileName, pid, fid)){ //check with database
					fileName = Util.appendSequenceNo(fileName,"_", "."+FilenameUtils.getExtension(fileName));
					
				}
		/*		if(!orgFileName.equals(fileName)){
					appState.recordWarningMsg(messages.format("rename-filename-x-to-y", orgFileName, fileName));
				}
		*/		
				Date now = new Date();
				String prefix = Util.formatDateTime(now, "yyyyMMdd-HHmmss")+"_"+_uid;
				File theFile = new File (dirFile + "/" + prefix +"_"+ fileName.replace(" ", "_"));
				
				
				
				BufferedOutputStream bfos = new BufferedOutputStream(new FileOutputStream(theFile));
				for( int i = 0; all_in_place && i < Integer.parseInt(params.get("partitionCount")); i++ ) {
					File partitionFile = new File(tempDir + sessionId + params.get("fileId")+i);
					BufferedInputStream bfis = new BufferedInputStream(new FileInputStream(partitionFile)); 
					
					byte data[] = new byte[BUFFER];
					while ((count = bfis.read(data, 0, BUFFER))  != -1) {
						bfos.write(data, 0, count);
			        }
					bfis.close();
					partitionFile.delete(); //delete the temp file
				}
				bfos.close();
				
				
				Session ss = getSessionFactory().getCurrentSession();
				ss.beginTransaction();
				try{
					Project p = (Project) ss.get(Project.class, pid);
					if(p==null){
						throw new RecordNotFoundException("Invalid ProjectID "+ pid);
					}
					User u = (User) ss.get(User.class, _uid);
					if(u==null){
						throw new RecordNotFoundException("Invalid UserID "+ uid);
					}
					ResourceFolder f = (ResourceFolder) ss.get(ResourceFolder.class, fid);
					if(f==null){
						throw new RecordNotFoundException("Invalid FolderID "+ uid);
					}
					
//assume these are new files, no versioning is needed to be done here
					
					ResourceFile rs = new ResourceFile();
					rs.setProj(p);
					rs.setCdate(new Date());
					rs.setMdate(new Date());
					rs.setOwner(u);
					rs.setParent(f);
					rs.setShared(f.isShared());
					rs.setName(fileName);
					//rs.setDes(Util.textarea2html(Util.filterOutRestrictedHtmlTags(upDes)));
					
					//not save VIRTUAL_DRIVE path to DB, to keep it relative path
					String path = theFile.getAbsolutePath().replace(Config.getString(Config.VIRTUAL_DRIVE), ""); 
					ResourceFileVersion rfv = new ResourceFileVersion();
					rfv.setPath(path);
					rfv.setVersion(1);
					rfv.setCmmt(rs.getDes());
					rfv.setContentType(Util.additionContentTypeCheck(params.get("contentType"), fileName));
					rfv.setSize(Long.parseLong(params.get("fileLength")));
					rfv.setCdate(new Date());
					rfv.setName(fileName);
					//rfv.setNumDownload(numDownload);
					rfv.setOwner(u);
					//rfv.setRfile(rs);
					
					rs.addFileVersion(rfv);
					
					ss.save(rs);
					ss.getTransaction().commit();
					
				}
				catch(Exception e){
					ss.getTransaction().rollback();
					//throw new RuntimeException(e.getMessage());
					res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
					return;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			//throw new RuntimeException(e.getMessage());
			try {
				res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				return;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
//		logger.debug("....."+out);
	}
	
	private boolean checkFileNameExist(String fileName, String projId, String folderId){
		
		Session ss = getSessionFactory().getCurrentSession();
		ss.beginTransaction();
		
		Query q = ss.createQuery("SELECT f FROM ResourceFile AS f " 
				+ " WHERE f.proj.id = :rPID " 
				+ " AND f.name = :rName"
				+ " AND f.parent.id = :rFolderID"
				)
				.setString("rPID", projId)
				.setString("rName", fileName)
				.setString("rFolderID", folderId)
				.setMaxResults(1)
				;
	
		ResourceFile f = (ResourceFile) q.uniqueResult();  //get only the latest version
		ss.getTransaction().commit();

		if(f!=null ){ //if File of the same name exist
			return true;
		}
		else{
			return false;
		}
		
	}
	
	
}
