package ntu.celt.eUreka2.pages.project;


import java.util.Date;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.ProjectAttachedFile;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.DateField;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.upload.services.UploadedFile;

public class Edit extends AbstractPageProject{
	@Property
	private Project proj;
	private String projId;
	
	@SessionState
	private AppState appState;
	
	@Property
    private UploadedFile file1;
	@Property
	private String keywords;
	@SuppressWarnings("unused")
	@Property
	private ProjectAttachedFile tempAttFile;
	@SuppressWarnings("unused")
	@Property
	private String tempKeyword;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private AttachedFileManager attFileManager;
	
	@Inject
	private Messages _messages;
	@Inject
	private PageRenderLinkSource linkSource;

	void onActivate(String id){
		projId = id;
	}
	Object onPassivate(){
		return projId;
	}
	void setupRender(){
		proj = projDAO.getProjectById(projId);
		if(proj==null){
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "ProjId", projId));
		}
		if(!canEditProject(proj)){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
	}
	void onPrepareForRenderFromForm(){
		proj.setDescription(Util.html2textarea(proj.getDescription()));
	}
	void onPrepareForSubmitFromForm(){
		proj = projDAO.getProjectById(projId);
	}
	
	@Component
	private Form form;
	@Component(id="edate")
	private DateField edateField;
	public void onValidateFormFromForm() {
		if (proj.getEdate().before(proj.getSdate())) {
			form.recordError(edateField, _messages.get("endDate-must-be-after-startDate"));
		}
	}
	
	@CommitAfter
	Object onSuccessFromForm(){
		proj.setDescription(Util.textarea2html(proj.getDescription()));
		proj.setEditor(getCurUser());
		proj.setMdate(new Date());
		proj.setCompanyInfo(Util.textarea2html(proj.getCompanyInfo()));
		projDAO.saveProject(proj);
		
		if(keywords!=null){
			String keywordsStr[] = keywords.split(",");
			for(String keyword : keywordsStr){
				keyword = keyword.trim();
				if(!keyword.isEmpty())
					proj.addKeywords(keyword);
			}
		}
		
		if(file1!=null)
			saveFile(file1, null);
		
		projDAO.updateProject(proj);
		
		appState.recordInfoMsg(_messages.format("successfully-update-x", proj.getDisplayName()));
		
		return linkSource.createPageRenderLinkWithContext(Home.class, proj.getId());
	}
	
	private void saveFile(UploadedFile upFile, String aliasFileName){
		ProjectAttachedFile f = new ProjectAttachedFile();
		f.setId(Util.generateUUID());
		f.setFileName(upFile.getFileName());
		f.setAliasName(aliasFileName);
		f.setContentType(upFile.getContentType());
		f.setSize(upFile.getSize());
		f.setCreator(getCurUser());
		f.setPath(attFileManager.saveAttachedFile(upFile, f.getId(), getModuleName(), proj.getId()));
		
		proj.addAttachFile(f);
	}
	
	@InjectComponent
	private Zone attachedFilesZone;
	@CommitAfter
	Object onActionFromRemoveAttachment(String attachId){
		ProjectAttachedFile attFile = projDAO.getAttachedFileById(attachId);
		if(attFile==null)
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "AttachmentID", attachId));
		
		proj = attFile.getProj();
		
		if(!canEditProject(proj)){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		attFileManager.removeAttachedFile(attFile);
		
		proj.removeAttachFile(attFile);
		proj.setEditor(getCurUser());
		proj.setMdate(new Date());
		projDAO.saveProject(proj);
		return attachedFilesZone.getBody();
	}
	@InjectComponent
	private Zone selectedKeywordsZone;
	
	@CommitAfter
	Object onRemoveKeyword(String pId, String keyword){
		proj = projDAO.getProjectById(pId);
		if(proj==null)
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "ProjectID", pId));
		if(!canEditProject(proj))
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		
		proj.removeKeywords(keyword);
		projDAO.saveProject(proj);
		return selectedKeywordsZone.getBody();
	}
	
}
