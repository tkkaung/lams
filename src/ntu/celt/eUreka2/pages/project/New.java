package ntu.celt.eUreka2.pages.project;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
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
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.DateField;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.upload.services.UploadedFile;

public class New extends AbstractPageProject{
	@Property
	private Project proj;
	
	@Property
    private UploadedFile file1;
	@Property
	private String keywords;
	
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

	
	void setupRender(){
		if(!canCreateProject()){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		proj = new Project();
		proj.setSchool(getCurUser().getSchool());
		proj.setType(projTypeDAO.getTypeByName(PredefinedNames.PROJTYPE_ADHOC));
		proj.setSdate(new Date());
		proj.setEdate(new Date());
	}
	
	void onPrepareForSubmitFromForm(){
		proj = new Project();
		proj.setSchool(getCurUser().getSchool());
		proj.setType(projTypeDAO.getTypeByName(PredefinedNames.PROJTYPE_ADHOC));
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
		proj.setCdate(new Date());
		proj.setMdate(new Date());
		proj.setCreator(getCurUser());
		proj.setEditor(getCurUser());
		proj.setStatus(projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_ACTIVE));
		proj.setLastStatusChange(new Date());
		proj.setId(projDAO.generateId(proj.getType(), proj.getSchool(), proj.getSdate()));
		proj.addMember(new ProjUser(proj, getCurUser(), projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_LEADER)));
		
		List<Module> defaultModules = proj.getType().getDefaultModules();
		for(Module m : defaultModules){
			proj.addModule(new ProjModule(proj, m));
		}
		
		projDAO.immediateSaveProject(proj);
		
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
		
		appState.recordInfoMsg(_messages.format("successfully-create-x", proj.getDisplayName()));
		appState.recordInfoMsg(_messages.get("suggest-manage-membership-modules"));
		
		
		return linkSource.createPageRenderLinkWithContext(ManageMember.class, proj.getId());
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
	
}
