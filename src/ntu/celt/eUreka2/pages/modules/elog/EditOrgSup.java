package ntu.celt.eUreka2.pages.modules.elog;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.elog.Elog;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.elog.ElogFile;
import ntu.celt.eUreka2.modules.elog.ElogStatus;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.ajax.MultiZoneUpdate;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;

public class EditOrgSup extends AbstractPageElog {
	@Property
	private Project project;
	@Property
	private Elog elog;
	@Property
	private String eid;
	@Property
	private boolean saveAndApprove;
	
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
	private Request request;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private AttachedFileManager attFileManager;
	@Inject
	private EmailManager emailManager;
	@Inject
	private Logger logger;
	
	void onActivate(EventContext ec) {
		if (ec.getCount() == 1) {
			eid = ec.get(String.class, 0);
		}
	}
	Object[] onPassivate() {
		return new Object[] {eid};
	}
	
	
	
	
	void setupRender() {
		elog = eDAO.getElogById(eid);
		if(elog==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ElogId", eid));
		project = elog.getProject();
		if(!canApproveElog(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
	}
	void onPrepareForSubmitFromElogForm(){
		elog = eDAO.getElogById(eid);
		project = elog.getProject();
	}
		
	void onSelectedFromSaveandapprove(){ //when submit button 'Preview >' is clicked
		saveAndApprove = true;
	}
	
	void onValidateFormFromElogForm(){

	}
	
	
	@CommitAfter
	Object onSuccessFromElogForm() {
		elog.setEditor(getCurUser());
		elog.setMdate(new Date());
		elog.setSubject(Util.filterOutRestrictedHtmlTags(Util.nvl(elog.getSubject())));
		elog.setContent(Util.filterOutRestrictedHtmlTags(Util.nvl(elog.getContent())));
	
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
		
		eDAO.updateElog(elog);
		
		if (("auto").equalsIgnoreCase(request.getParameter("mode"))) {
			appState.recordInfoMsg(messages.format("auto-save-on-x", Util.formatDateTime2(new Date())));
			return linkSource.createPageRenderLinkWithContext(this.getClass(), elog.getId()); 
		}
		else{
			if(saveAndApprove){
				elog.setStatus(ElogStatus.APPROVED);
				elog.setPublished(true);
				elog.appendlnHtmlRemarks("<span class='date'>"+Util.formatDateTime2(new Date())+"</span> - "
						+messages.format("remarks-edited-by-x", getCurUser().getDisplayName()));
				elog.appendlnHtmlRemarks("<span class='date'>"+Util.formatDateTime2(new Date())+"</span> - "
						+messages.format("remarks-approved-by-x", getCurUser().getDisplayName()));
				elog.appendlnHtmlRemarks("<span class='date'>"+Util.formatDateTime2(new Date())+"</span> - "
						+messages.format("remarks-published-by-x", getCurUser().getDisplayName()));
				eDAO.updateElog(elog);
				
				//prepare to email to update students and other approvers
				List<User> recipients = project.getUsers();
				EmailTemplateVariables var = new EmailTemplateVariables(
						elog.getCdate().toString(),(new Date()).toString(),
						elog.getSubject(), 
						Util.truncateString(Util.stripTags(elog.getContent()), Config.getInt("max_content_lenght_in_email")), 
						getCurUser().getDisplayName(),
						project.getDisplayName(), 
						emailManager.createLinkBackURL(request, View.class, elog.getId()), 
						elog.getAuthor().getDisplayName(), null);
				try{
					emailManager.sendHTMLMail(recipients, EmailTemplateConstants.ELOG_APPROVED, var);
					appState.recordInfoMsg(messages.format("approval-update-sent-to-x", Util.extractDisplayNames(recipients)));
				}
				catch(SendEmailFailException e){
					logger.warn(e.getMessage(), e);
		//			appState.recordWarningMsg(e.getMessage());
				}
				
				
				return linkSource.createPageRenderLinkWithContext(ApproveOverview.class, project.getId());
			}
			//else, save
			
			appState.recordInfoMsg(messages.format("successfully-save-x", elog.getSubject()));
			elog.appendlnHtmlRemarks("<span class='date'>"+Util.formatDateTime2(new Date())+"</span> - "
					+messages.format("remarks-edited-by-x", getCurUser().getDisplayName()));
			
			return linkSource.createPageRenderLinkWithContext(Approve.class, elog.getId()); 
		}
		
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
	@InjectComponent
	private Zone remarkLogsZone;
	@CommitAfter
	Object onActionFromRemoveFile(String fId){
		ElogFile f = eDAO.getElogFileById(fId);
		if(f==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "FileID", fId));
		elog = f.getElog();
		if(!canApproveElog(elog.getProject()))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		attFileManager.removeAttachedFile(f);
		elog.removeFile(f);
		eDAO.updateElog(elog);
		elog.appendlnHtmlRemarks("<span class='date'>"+Util.formatDateTime2(new Date())+"</span> - "
				+messages.format("remarks-remove-attach-x-by-y", f.getDisplayName(), getCurUser().getDisplayName()));
		
		if(request.isXHR())
			return new MultiZoneUpdate("attachedFilesZone", attachedFilesZone.getBody()).add("remarkLogsZone",remarkLogsZone.getBody());
		return null;
		
	}
	
	
	
	
}
