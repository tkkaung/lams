package ntu.celt.eUreka2.pages.modules.learninglog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.learninglog.LLogFile;
import ntu.celt.eUreka2.modules.learninglog.LearningLogDAO;
import ntu.celt.eUreka2.modules.learninglog.LogEntry;
import ntu.celt.eUreka2.pages.modules.blog.View;
import ntu.celt.eUreka2.pages.modules.forum.ThreadView;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.upload.services.UploadedFile;

public class LearningLogEdit extends AbstractPageLearningLog{

	private String projId;
	@Property
	private Project curProj;
	private long entryId;
	@Property
	private LogEntry lLog;
	private String callBackType; 
	
	@Property
	private String typeSelect; 
	@Property
    private UploadedFile file1;
	@Property
    private UploadedFile file2;
	@Property
    private UploadedFile file3;
	@Property
    private UploadedFile file4;
	@Property
    private UploadedFile file5;
	
	@SuppressWarnings("unused")
	@Property
	private LLogFile tempFile;
	
	
	@Inject
	private Request request;
	@Inject
	private LearningLogDAO learningLogDAO;
	@Inject
	private ProjectDAO projDAO;
	@Inject
    private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private AttachedFileManager attFileManager;
	
    
    void onActivate(EventContext ec) {
		if(ec.getCount()==1){
			entryId = ec.get(Long.class, 0);
		}
		else if(ec.getCount()==2){
			projId = ec.get(String.class, 0);
			entryId = ec.get(Long.class, 1);
		}
		else if(ec.getCount()==3){
			projId = ec.get(String.class, 0);
			entryId = ec.get(Long.class, 1);
			callBackType = ec.get(String.class, 2);
		}
	}
    Object[] onPassivate() {
		if(callBackType!=null)
			return new Object[]{projId, entryId, callBackType};
		if(projId!=null)
			return new Object[]{projId, entryId};
		return new Object[]{entryId};
	}
    public boolean isCreateMode(){
    	if(entryId==0)
    		return true;
    	return false;
    }
    public String getTitle(){
    	if(isCreateMode())
    		return messages.get("add-new")+" "+messages.get("logEntry");
    	return messages.get("edit")+" "+messages.get("logEntry");
    }
    public String getBreadcrumb(){
    	if(isCreateMode())
    		return getTitle()+"=modules/learninglog/edit";
    	return encode(truncateStr(lLog.getTitle()))+"=modules/learninglog/view?"+ lLog.getId() + ","+ getTitle()+"=modules/learninglog/edit";
    }
    @Cached
	public String getShouldDisableInputType(){
		if(LogEntry.TYPE_BLOG_REFLECTION.equals(lLog.getType())
				|| LogEntry.TYPE_ELOG_REFLECTION.equals(lLog.getType())
				|| LogEntry.TYPE_FORUM_REFLECTION.equals(lLog.getType()))
			return "disabled";
		return null;
	}
    
    public void setContext(String projId, long entryId, String callBackType){
    	this.projId = projId;
    	this.entryId = entryId;
    	this.callBackType = callBackType;
    }
    
	void setupRender() {
		if(isCreateMode()){
			curProj = projDAO.getProjectById(projId);
			if(curProj==null){
				throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", projId));
			}
			
			lLog = new LogEntry();
			lLog.setTitle(messages.get("untitled"));
		}
		else{  //edit
			lLog = learningLogDAO.getLogEntryById(entryId);
			if(lLog==null){
				throw new RecordNotFoundException(messages.format("entity-not-exists", "EntryId", entryId));
			}
			curProj = lLog.getProject();
			
			//check owner of the entry
			if(!lLog.getCreator().equals(getCurUser()))
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			
			typeSelect = lLog.getType();
		}
	}
	void onPrepareForSubmitFromForm(){
		if(isCreateMode()){
			curProj = projDAO.getProjectById(projId);
			lLog = new LogEntry();
			lLog.setTitle(messages.get("untitled"));
		}
		else{
			lLog = learningLogDAO.getLogEntryById(entryId);
			curProj = lLog.getProject();
			typeSelect = lLog.getType();
		}
	}
	
	void onValidateFormFromForm(){
		
	}
	@CommitAfter
	Object onSuccessFromForm(){
		if(isCreateMode()){
			lLog.setCdate(new Date());
			lLog.setProject(curProj);
			lLog.setCreator(getCurUser());
			//entry.addLink(linkItem);
		}
		else{ //editMode
			
		}
		if(!"NEW_VALUE".equals(typeSelect)){
			lLog.setType(typeSelect);
		}
		lLog.setContent(Util.filterOutRestrictedHtmlTags(lLog.getContent()));
		lLog.setMdate(new Date());
		
		
		
		if(file1!=null)
			saveFile(file1, null);
		if(file2!=null)
			saveFile(file2, null);
		if(file3!=null)
			saveFile(file3, null);
		if(file4!=null)
			saveFile(file4, null);
		if(file5!=null)
			saveFile(file5, null);
		
		
		
		learningLogDAO.saveLogEntry(lLog);
		
		if(callBackType!=null){
			Link link = null;
			if(callBackType.equalsIgnoreCase(LogEntry.TYPE_FORUM_REFLECTION))
				link = linkSource.createPageRenderLinkWithContext(ThreadView.class, lLog.getForumThreadId());
			else if(callBackType.equalsIgnoreCase(LogEntry.TYPE_BLOG_REFLECTION))
				link = linkSource.createPageRenderLinkWithContext(View.class, lLog.getBlogId());
			else if(callBackType.equalsIgnoreCase(LogEntry.TYPE_ELOG_REFLECTION))
				link = linkSource.createPageRenderLinkWithContext(ntu.celt.eUreka2.pages.modules.elog.View.class, lLog.getElogId());
			link.setAnchor("ref"+lLog.getId());
			return link;
		}
		
		return linkSource.createPageRenderLinkWithContext(LearningLogIndex.class, curProj.getId());
	}
	
	
	private void saveFile(UploadedFile upFile, String aliasFileName){
		LLogFile f = new LLogFile();
		f.setId(Util.generateUUID());
		f.setFileName(upFile.getFileName());
		f.setAliasName(aliasFileName);
		f.setContentType(upFile.getContentType());
		f.setSize(upFile.getSize());
		f.setCreator(getCurUser());
		f.setPath(attFileManager.saveAttachedFile(upFile, f.getId(), getModuleName(), curProj.getId()));
		//f.setLogEntry(entry);
		
		lLog.addFile(f);
	}
	
	@InjectComponent
	private Zone attachedFilesZone;
	@CommitAfter
	Object onActionFromRemoveFile(String fId){
		LLogFile f = learningLogDAO.getLLogFileById(fId);
		if(f==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "FileID", fId));
		lLog = f.getLogEntry();
		if(!lLog.getCreator().equals(getCurUser()))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		attFileManager.removeAttachedFile(f);
		lLog.removeFile(f);
		learningLogDAO.saveLogEntry(lLog);
		
		if(request.isXHR())
			return attachedFilesZone.getBody();
		return null;
	}
	
	
	
	
	private String[] defaultCats = {messages.get("default-type1"), messages.get("default-type2")
			,messages.get("default-type3"),messages.get("default-type4")};
	public SelectModel getTypeModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		optModelList.add(new OptionModelImpl(messages.get("other"), "NEW_VALUE"));
		List<String> catList = learningLogDAO.getLogEntryTypes(getCurUser());
		for(String cat : catList){
			optModelList.add(new OptionModelImpl(cat, cat));
		}
		for(String cat : defaultCats){
			if(!catList.contains(cat))
				optModelList.add(new OptionModelImpl(cat, cat));
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
	
    
	
}
