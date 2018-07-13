package ntu.celt.eUreka2.pages.admin.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.ProjStatus;
import ntu.celt.eUreka2.entities.ProjType;
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

public class ProjectAdminEdit extends AbstractPageAdminProject {
	@Property
	private Project proj;
	private String projId;
	
	@SuppressWarnings("unused")
	@Property
	private List<ProjStatus> allStatus;
	@SuppressWarnings("unused")
	@Property
	private ProjStatus tempStatus;
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
	private Messages _messages;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private ProjStatusDAO projStatusDAO;
	@Inject
	private ProjTypeDAO projTypeDAO;
	@Inject
	private SchoolDAO schoolDAO;
	@Inject
	private AttachedFileManager attFileManager;
	
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
		if(!canManageProjects()){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		if(isCreateMode()){ 
			proj = new Project();
			proj.setSdate(new Date());
			proj.setEdate(new Date());
			proj.setType(projTypeDAO.getTypeByName(PredefinedNames.PROJTYPE_ADHOC));
		}else{ 
			proj = projDAO.getProjectById(projId);
			if(proj==null){
				throw new RecordNotFoundException(_messages.format("entity-not-exists", "ProjId", projId));
			}
		}
		allStatus = projStatusDAO.getAllStatus();
	}
	void onPrepareForRenderFromForm(){
		proj.setDescription(Util.html2textarea(proj.getDescription()));
		proj.setRemarks(Util.html2textarea(proj.getRemarks()));
	}
	void onPrepareForSubmitFromForm(){   
		if(isCreateMode()){ 
			proj = new Project();
		}else{ 
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
			proj.setId(projDAO.generateId(proj.getType(), proj.getSchool(), proj.getSdate()));
			
			List<Module> defaultModules = proj.getType().getDefaultModules();
			for(Module m : defaultModules){
				proj.addModule(new ProjModule(proj, m));
			}
			
		}
		proj.setEditor(getCurUser());
		proj.setMdate(new Date());
		proj.setDescription(Util.textarea2html(proj.getDescription()));
		proj.setRemarks(Util.textarea2html(proj.getRemarks()));
		
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
		
		projDAO.saveProject(proj);
		
		if(isCreateMode()){
			appState.recordInfoMsg(_messages.format("successfully-create-x", proj.getDisplayName()));
			appState.recordInfoMsg(_messages.get("suggest-manage-membership-modules"));
			return linkSource.createPageRenderLinkWithContext(ManageAdminMember.class, proj.getId());
		}
		else{
			appState.recordInfoMsg(_messages.format("successfully-update-x", proj.getDisplayName()));
			return linkSource.createPageRenderLink(ManageAdminProjects.class);
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
	
	public SelectModel getProjStatusModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<ProjStatus> projStatusList = projStatusDAO.getAllStatus();
		for (ProjStatus ps : projStatusList) {
			OptionModel optModel = new OptionModelImpl(ps.getDisplayName(), ps);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	public SelectModel getProjTypeModel() {
		List<OptionModel> optionList = new ArrayList<OptionModel>();
		List<ProjType> typeList = projTypeDAO.getAllTypes();
		for (ProjType type: typeList) {
			OptionModel option = new OptionModelImpl(type.getDisplayName(), type);
			optionList.add(option);
		}
		SelectModel selModel = new SelectModelImpl(null, optionList);
		return selModel;
	}
	

	@InjectComponent
	private Zone attachedFilesZone;
	@CommitAfter
	Object onActionFromRemoveAttachment(String attachId){
		ProjectAttachedFile attFile = projDAO.getAttachedFileById(attachId);
		if(attFile==null)
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "AttachmentID", attachId));
		
		proj = attFile.getProj();
		
		if(!canManageProjects()){
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
		if(!canManageProjects()){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		proj.removeKeywords(keyword);
		projDAO.saveProject(proj);
		return selectedKeywordsZone.getBody();
	}
	
	
	
	public int getID_ProjType_Course(){
		ProjType t = projTypeDAO.getTypeByName(PredefinedNames.PROJTYPE_COURSE);
		if(t==null)
			return 0;
		return t.getId();
	}
	public int getID_ProjType_CAO(){
		ProjType t = projTypeDAO.getTypeByName(PredefinedNames.PROJTYPE_CAO);
		if(t==null)
			return 0;
		return t.getId();
	}
	
	
}
