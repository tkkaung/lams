package ntu.celt.eUreka2.services;

/**
 * based on http://code.google.com/p/t5-restful-webservices/wiki/GettingStarted
 * <p>
 * Usage: 
 * 	- create a function :
 * 	<p>the function must, 
 * 		1. Be void return type
 * 		2. Be annotated with the @RestfulWebMethod annotation 
 *    	3. Take the Tapestry Request and Response objects as its first two arguments, in that order
 *  <p> 
 *  - access URL:   If the base URL for our Tapestry 5 web application is http://myapp.example.org/  
 *    (i.e., the root context path is "/") then the URL for our web service method is
 *    http://myapp.example.org/<class-map-key-configured-in-appmodule>/<lowercase-method-name>/args...
 *    e.g., http://myapp.example.org/my-web-service/foo/42/true 
 */

/*
 * - For *DAO to be able to 'commit', @CommitAfter should be added to the methods of those DAO, 
 *   and adviseTransactions() is defined in AppModule.java
 *   refer to tapestry-hibernate user guide:
 *   http://tapestry.apache.org/tapestry5/tapestry-hibernate/userguide.html
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.ImageResizer;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.blog.BlogFile;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.elog.ElogFile;
import ntu.celt.eUreka2.modules.forum.ForumAttachedFile;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.learninglog.LLogFile;
import ntu.celt.eUreka2.modules.learninglog.LearningLogDAO;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;

import us.antera.t5restfulws.RestfulWebMethod;

public class FileRetrievalService {
	private LearningLogDAO learninglogDAO;
	private ElogDAO elogDAO;
	private BlogDAO blogDAO;
	private ForumDAO forumDAO;
	private AttachedFileManager attFileManager;
	private Messages messages;
	private WebSessionDAO webSessionDAO;
	
	/**
	 * Receive all the services needed as constructor arguments. 
	 * When we bind this service, T5 IoC will provide all the services! (It works like using @Inject)
	 */
	public FileRetrievalService(
			AttachedFileManager _attFileManager,
			LearningLogDAO _learninglogDAO,
			ElogDAO _elogDAO,
			BlogDAO _blogDAO,
			ForumDAO _forumDAO,
			Messages _messages,
			WebSessionDAO _webSessionDAO
			){
		attFileManager = _attFileManager;
		learninglogDAO = _learninglogDAO;
		elogDAO = _elogDAO;
		blogDAO = _blogDAO;
		forumDAO = _forumDAO;
		messages = _messages;
		webSessionDAO = _webSessionDAO;
	}
	
	private boolean writeFileToResponse(Request request, Response response, File file, String contentType) throws IOException{
		if(!file.exists()){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, messages.get("file-not-found"));
			return false;
		}
		
		response.setContentLength((int)file.length()) ;
		
		FileInputStream in = new FileInputStream(file);
		OutputStream out = response.getOutputStream(contentType);
		
		byte[] buf = new byte[1024];
		int count = 0;
		while((count=in.read(buf))>=0){
			out.write(buf, 0, count);
		}
		in.close();
		out.close();
		return true;
	}
	private User validateLogin(Request request, Response response) throws IOException{
	
		User u = webSessionDAO.getCurrentUser(
				(String)request.getSession(true).getAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME));
		
		
		if(u==null){
			response.sendError(HttpServletResponse.SC_FORBIDDEN, messages.get("login-first"));
			return null;
		}
		return u;
	}
	
	
	@RestfulWebMethod 
	public void getLLogImageThumb(Request request, Response response, String imageId) throws IOException{
		User u = validateLogin(request, response);
		if(u==null)
			return;
		
		LLogFile img = learninglogDAO.getLLogFileById(imageId);
		if(img==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, messages.get("image-not-found"));
			return;
		}
		
		File file = new File(attFileManager.getRootFolder() + Util.appendToFileName(img.getPath(), THUMB_SUFFIX) );
		
		/*generate one, if not exist*/
		if(!file.exists()){
			File imgFile = new File(attFileManager.getRootFolder() + img.getPath() );
			if(imgFile.exists()){
				boolean success = ImageResizer.generateThumbnail(imgFile, file, THUMB_WIDTH_HEIGHT, false);
				if(success && file.exists())
					writeFileToResponse(request, response, file, img.getContentType());
				return;	
			}
		}
		writeFileToResponse(request, response, file, img.getContentType());
	}
	
	@RestfulWebMethod 
	public void getLLogImage(Request request, Response response, String imageId) throws IOException{
		User u = validateLogin(request, response);
		if(u==null)
			return;
		
		LLogFile img = learninglogDAO.getLLogFileById(imageId);
		if(img==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, messages.get("image-not-found"));
			return;
		}
		
		
		File file = new File(attFileManager.getRootFolder()+img.getPath() );
		writeFileToResponse(request, response, file, img.getContentType());
	}
	
	
	@RestfulWebMethod 
	public void getElogImageThumb(Request request, Response response, String imageId) throws IOException{
		User u = validateLogin(request, response);
		if(u==null)
			return;
		ElogFile img = elogDAO.getElogFileById(imageId);
		if(img==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, messages.get("image-not-found"));
			return;
		}
		
		File file = new File(attFileManager.getRootFolder() + Util.appendToFileName(img.getPath(), THUMB_SUFFIX) );
		
		/*generate one, if not exist*/
		if(!file.exists()){
			File imgFile = new File(attFileManager.getRootFolder() + img.getPath() );
			if(imgFile.exists()){
				boolean success = ImageResizer.generateThumbnail(imgFile, file, THUMB_WIDTH_HEIGHT, false);
				if(success && file.exists())
					writeFileToResponse(request, response, file, img.getContentType());
				return;	
			}
		}
		writeFileToResponse(request, response, file, img.getContentType());
	}
	
	@RestfulWebMethod 
	public void getElogImage(Request request, Response response, String imageId) throws IOException{
		User u = validateLogin(request, response);
		if(u==null)
			return;
		ElogFile img = elogDAO.getElogFileById(imageId);
		if(img==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, messages.get("image-not-found"));
			return;
		}
		
		File file = new File(attFileManager.getRootFolder()+img.getPath() );
		writeFileToResponse(request, response, file, img.getContentType());
	}

	
	final private String THUMB_SUFFIX = "_th";
	final private int THUMB_WIDTH_HEIGHT = Config.getInt("max_thumb_width_height");
	
	@RestfulWebMethod 
	public void getForumImageThumb(Request request, Response response, String imageId) throws IOException{
		User u = validateLogin(request, response);
		if(u==null)
			return;
		ForumAttachedFile img = forumDAO.getAttachedFileById(imageId);
		if(img==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, messages.get("image-not-found"));
			return;
		}
		
		File file = new File(attFileManager.getRootFolder() + Util.appendToFileName(img.getPath(), THUMB_SUFFIX) );
		
		/*generate one, if not exist*/
		if(!file.exists()){
			File imgFile = new File(attFileManager.getRootFolder() + img.getPath() );
			if(imgFile.exists()){
				boolean success = ImageResizer.generateThumbnail(imgFile, file, THUMB_WIDTH_HEIGHT, false);
				if(success && file.exists())
					writeFileToResponse(request, response, file, img.getContentType());
				return;	
			}
		}
		writeFileToResponse(request, response, file, img.getContentType());
		
	}
	@RestfulWebMethod 
	public void getForumImage(Request request, Response response, String imageId) throws IOException{
		User u = validateLogin(request, response);
		if(u==null)
			return;
		ForumAttachedFile img = forumDAO.getAttachedFileById(imageId);
		if(img==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, messages.get("image-not-found"));
			return;
		}
		
		File file = new File(attFileManager.getRootFolder()+img.getPath() );
		writeFileToResponse(request, response, file, img.getContentType());
	}
	
	
	@RestfulWebMethod 
	public void getBlogImageThumb(Request request, Response response, String imageId) throws IOException{
		User u = validateLogin(request, response);
		if(u==null)
			return;
		BlogFile img = blogDAO.getBlogFileById(imageId);
		if(img==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, messages.get("image-not-found"));
			return;
		}
		
		File file = new File(attFileManager.getRootFolder() + Util.appendToFileName(img.getPath(), THUMB_SUFFIX) );
		
		/*generate one, if not exist*/
		if(!file.exists()){
			File imgFile = new File(attFileManager.getRootFolder() + img.getPath() );
			if(imgFile.exists()){
				boolean success = ImageResizer.generateThumbnail(imgFile, file, THUMB_WIDTH_HEIGHT, false);
				if(success && file.exists())
					writeFileToResponse(request, response, file, img.getContentType());
				return;	
			}
		}
		writeFileToResponse(request, response, file, img.getContentType());
		
	}
	@RestfulWebMethod 
	public void getBlogImage(Request request, Response response, String imageId) throws IOException{
		User u = validateLogin(request, response);
		if(u==null)
			return;
		BlogFile img = blogDAO.getBlogFileById(imageId);
		if(img==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, messages.get("image-not-found"));
			return;
		}
		
		File file = new File(attFileManager.getRootFolder()+img.getPath() );
		writeFileToResponse(request, response, file, img.getContentType());
	}
}
