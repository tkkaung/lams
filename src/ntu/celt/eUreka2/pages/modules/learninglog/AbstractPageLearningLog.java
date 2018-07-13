package ntu.celt.eUreka2.pages.modules.learninglog;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.learninglog.LLogFile;
import ntu.celt.eUreka2.modules.learninglog.LearningLogDAO;
import ntu.celt.eUreka2.services.attachFiles.AttachedFile;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileNotFoundException;

public abstract class AbstractPageLearningLog {
	public String getModuleName(){
		return PredefinedNames.MODULE_LEARNING_LOG;
	}
	
	@Inject
	private ModuleDAO moduleDAO;
	@Inject
	private LearningLogDAO learninglogDAO;
	@Inject
	private AttachedFileManager attFileManager;
	@Inject
	private Logger logger;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	public Module getModule(){
		return moduleDAO.getModuleByName(PredefinedNames.MODULE_LEARNING_LOG);
	}
	public String encode(String str){
		return Util.encode(str);
	}
	public String truncateStr(String str){
		return Util.truncateString(str, 50);
	}
	public Object[] getParams(Object o1, Object o2){
		return new Object[] {o1, o2};
	}
	
	public StreamResponse onRetrieveFile(String id) {
		LLogFile f = learninglogDAO.getLLogFileById(id);
		if(f==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "FileID", id));
		if(!f.getLogEntry().getCreator().equals(getCurUser()))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		try {
			return attFileManager.getAttachedFileAsStream(f);
		} catch (FileNotFoundException e) {
			logger.error("FileNotFound: path=" + f.getPath() +"\n"+e.getMessage());
			throw new AttachedFileNotFoundException(messages.format("attachment-x-not-found", f.getDisplayName()));
		}
	}
	public List<AttachedFile> getImageAttachedFiles(List<AttachedFile> attList){
		List<AttachedFile> list = new ArrayList<AttachedFile>();
		for(AttachedFile att : attList){
			if(Util.isImageExtension(att.getFileName())){
				list.add(att);
			}
		}
		return list;
	}
	public List<AttachedFile> getNonImageAttachedFiles(List<AttachedFile> attList){
		List<AttachedFile> list = new ArrayList<AttachedFile>();
		for(AttachedFile att : attList){
			if(!Util.isImageExtension(att.getFileName())){
				list.add(att);
			}
		}
		return list;
	}
	public String getRetrieveImageThumbLink(String imageId){
		return request.getContextPath() + "/getfsvc/getllogimagethumb/"+imageId;
	}
	public String getRetrieveImageLink(String imageId){
		return request.getContextPath() + "/getfsvc/getllogimage/"+imageId;
	}
}
