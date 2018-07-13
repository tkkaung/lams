package ntu.celt.eUreka2.pages.admin.project;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.ProjectAttachedFile;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.DateField;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.upload.services.UploadedFile;

public class ProjectEdit extends AbstractPageAdminProject {
	@Property
	private Project proj;
	private String projId;
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
	
	@SessionState
	private AppState appState;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private SchoolDAO schoolDAO;
	@Inject
	private ProjStatusDAO projStatusDAO;
	@Inject
	private ProjTypeDAO projTypeDAO;
	@Inject
	private ProjRoleDAO projRoleDAO;
	
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
	public boolean isCreateMode(){
		if(projId == null)
			return true;
		return false;
	}
	
	void setupRender(){
		if(isCreateMode()){
			if(!canCreateProject()){
				throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
			}
			proj = new Project();
			proj.setSchool(getCurUser().getSchool());
			proj.setType(projTypeDAO.getTypeByName(PredefinedNames.PROJTYPE_ADHOC));
			proj.setSdate(new Date());
			proj.setEdate(new Date());
		}
		else{
			proj = projDAO.getProjectById(projId);
			if(proj==null){
				throw new RecordNotFoundException(_messages.format("entity-not-exists", "ProjectID", projId));
			}
			if(!canEditProject(proj)){
				throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
			}
		}
	}
	void onPrepareForRenderFromForm(){
		proj.setDescription(Util.html2textarea(proj.getDescription()));
	}
	void onPrepareForSubmitFromForm(){
		if(isCreateMode()){
			proj = new Project();
			proj.setSchool(getCurUser().getSchool());
			proj.setType(projTypeDAO.getTypeByName(PredefinedNames.PROJTYPE_ADHOC));
		}
		else{
			proj = projDAO.getProjectById(projId);
		}
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
		if(isCreateMode()){
			proj.setCdate(new Date());
			proj.setCreator(getCurUser());
			proj.setStatus(projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_ACTIVE));
			proj.setLastStatusChange(new Date());
			proj.setId(projDAO.generateId(proj.getType(), proj.getSchool(), proj.getSdate()));
			proj.addMember(new ProjUser(proj, getCurUser(), projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_LEADER)));
			List<Module> defaultModules = proj.getType().getDefaultModules();
			for(Module m : defaultModules){
				proj.addModule(new ProjModule(proj, m));
			}
			
		}
		proj.setEditor(getCurUser());
		proj.setMdate(new Date());
		proj.setDescription(Util.textarea2html(proj.getDescription()));
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
		
		if(isCreateMode()){
			appState.recordInfoMsg(_messages.format("successfully-create-x", proj.getDisplayName()));
			appState.recordInfoMsg(_messages.get("suggest-manage-membership-modules"));
			return linkSource.createPageRenderLinkWithContext(ManageMember.class, proj.getId());
		}
		else{
			appState.recordInfoMsg(_messages.format("successfully-update-x", proj.getDisplayName()));
			return linkSource.createPageRenderLink(ManageProjects.class);
		}

	}
	
	private void saveFile(UploadedFile upFile, String aliasFileName){
		ProjectAttachedFile f = new ProjectAttachedFile();
		f.setId(Util.generateUUID());
		f.setFileName(upFile.getFileName());
		f.setAliasName(aliasFileName);
		f.setContentType(upFile.getContentType());
		f.setSize(upFile.getSize());
		f.setCreator(getCurUser());
		f.setPath(attFileManager.saveAttachedFile(upFile, f.getId(), PredefinedNames.PROJECT_INFO, proj.getId()));
		
		proj.addAttachFile(f);
	}
	
	public SelectModel getSchoolModel() {
		List<OptionModel> optionList = new ArrayList<OptionModel>();
		List<School> sList = schoolDAO.getAllSchools();
		for (School s: sList) {
			OptionModel option = new OptionModelImpl(s.getDisplayNameLong(), s);
			optionList.add(option);
		}
		SelectModel selModel = new SelectModelImpl(null, optionList);
		return selModel;
	}
	public String getPageTitle(){
		if(isCreateMode())
			return _messages.get("new-adhoc-project");
		else
			return _messages.get("edit")+" "+_messages.get("project");
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
