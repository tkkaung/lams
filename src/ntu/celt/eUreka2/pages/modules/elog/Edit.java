package ntu.celt.eUreka2.pages.modules.elog;

import java.util.Date;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.elog.Elog;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.elog.ElogFile;
import ntu.celt.eUreka2.modules.elog.ElogStatus;
import ntu.celt.eUreka2.modules.elog.PrivilegeElog;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.upload.services.UploadedFile;

public class Edit extends AbstractPageElog {
	@Property
	private Project project;
	@Property
	private Elog elog;
	@Property
	private String pid;
	@Property
	private String eid;
	@Property
	private boolean previewB4RequestApproval;
	
//	@Persist("flash")
	@Property
	private String subject;
	//@Persist("flash")
	@Property
	private String content;
	
	@SuppressWarnings("unused")
	@Property
	private ElogFile tempFile;
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
	
	
	@SessionState
	private AppState appState;
	@Inject
	private ElogDAO eDAO;
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private Request request;
	@Inject
	private RequestGlobals requestGlobals;
	
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private AttachedFileManager attFileManager;
	
	void onActivate(EventContext ec) {
		if (ec.getCount() == 1) {
			eid = ec.get(String.class, 0);
		}
		else if (ec.getCount() == 2) {
			pid = ec.get(String.class, 0);
			eid = ec.get(String.class, 1);
		}
		else if (ec.getCount() == 3) {
			eid = ec.get(String.class, 0);
			subject = ec.get(String.class, 1);
			content = ec.get(String.class, 2);
		}
		else if (ec.getCount() == 4) {
			pid = ec.get(String.class, 0);
			eid = ec.get(String.class, 1);
			subject = ec.get(String.class, 2);
			content = ec.get(String.class, 3);
		}
	}
	Object[] onPassivate() {
		if (pid == null) {
		   return new Object[] {eid, subject, content};
		}
		else {
			return new Object[] {pid, eid, subject, content};
		}
	}
	
	public void setContext(String eid, String subject, String content){
		this.eid = eid;
		this.subject = subject;
		this.content = content;
	}
	
	public boolean isCreateMode(){
		if(eid==null || "".equals(eid))
			return true;
		return false;
	}
	
	 public String getTitle(){
    	if(isCreateMode())
    		return messages.get("add-new")+" "+messages.get("elog");
    	return messages.get("edit")+" "+messages.get("elog");
    }
	
	
	void setupRender() {
		if(isCreateMode()){
			project = pDAO.getProjectById(pid);
			if(project==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectID", pid));
			
			if(!canCreateElog(project)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
			
			elog = new Elog();
			//elog.setSubject(messages.get("untitled"));
			//elog.setContent(messages.get("edit-your-content-here"));
	//		subject = messages.get("untitled");
	//		content = messages.get("edit-your-content-here");
		}
		else{
			elog = eDAO.getElogById(eid);
			if(elog==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "ElogId", eid));
			project = elog.getProject();
			if(!canEditElog(elog)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
			if(subject==null)
				subject = elog.getSubject();
			if(content==null)
				content = elog.getContent();
			
		}
		
	}
	void onPrepareForSubmitFromElogForm(){
		if(isCreateMode()){
			project = pDAO.getProjectById(pid);
			elog = new Elog();
		}
		else{
			elog = eDAO.getElogById(eid);
			project = elog.getProject();
		}
	}
		
	void onSelectedFromPreview(){ //when submit button 'Preview >' is clicked
		previewB4RequestApproval = true;
	}
	
	void onValidateFormFromElogForm(){

	}
	
	@InjectPage
	private Submit submitPage;
	@CommitAfter
	Object onSuccessFromElogForm() {
		if(isCreateMode()){
			elog.setCdate(new Date());
			elog.setStatus(ElogStatus.DRAFT);
			elog.setAuthor(getCurUser());
			elog.setMdate(new Date());
			elog.setIp(requestGlobals.getHTTPServletRequest().getRemoteAddr());
			elog.setProject(project);
		}
		
		if(!previewB4RequestApproval){
			//elog.setAuthor(getCurUser());
			//elog.setMdate(new Date());
			//elog.setIp(wsData.getIp());
			//elog.setProject(project);
			elog.setSubject(Util.filterOutRestrictedHtmlTags(Util.nvl(subject)));
			elog.setContent(Util.filterOutRestrictedHtmlTags(Util.nvl(content)));
		}
		
		
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
		
		
		if(isCreateMode()){
			eDAO.addElog(elog);  //commit to database at this point, ID is initialized
		}
		else{
			eDAO.updateElog(elog);
		}
		
		if (("auto").equalsIgnoreCase(request.getParameter("mode"))) {
			appState.recordInfoMsg(messages.format("auto-save-on-x", Util.formatDateTime2(new Date())));
		}
		else{
			if(previewB4RequestApproval){
				subject = Util.filterOutRestrictedHtmlTags(subject);
				content = Util.filterOutRestrictedHtmlTags(content);
				
				submitPage.setContext(elog.getId(), subject, content);
				return submitPage;
			}
			//else, save-as-draft
			appState.recordInfoMsg(messages.format("save-draft-on-x", Util.formatDateTime2(new Date())));
		}
		return linkSource.createPageRenderLinkWithContext(this.getClass(), elog.getId()); 
		
	}
	
	
	
	
	private void saveFile(UploadedFile upFile, String aliasFileName){
		ElogFile f = new ElogFile();
		f.setId(Util.generateUUID());
		f.setFileName(upFile.getFileName());
		f.setAliasName(aliasFileName);
		f.setContentType(upFile.getContentType());
		f.setSize(upFile.getSize());
		f.setCreator(getCurUser());
		f.setPath(attFileManager.saveAttachedFile(upFile, f.getId(), getModuleName(), project.getId()));
		
		elog.addFile(f);
	}
	
	@InjectComponent
	private Zone attachedFilesZone;
	@CommitAfter
	Object onActionFromRemoveFile(String fId){
		ElogFile f = eDAO.getElogFileById(fId);
		if(f==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "FileID", fId));
		elog = f.getElog();
		if(!canEditElog(elog))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		attFileManager.removeAttachedFile(f);
		elog.removeFile(f);
		eDAO.updateElog(elog);
		if(request.isXHR())
			return attachedFilesZone.getBody();
		return null;
	}
	
	
	
	
}
