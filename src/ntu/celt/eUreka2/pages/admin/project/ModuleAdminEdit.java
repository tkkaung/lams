package ntu.celt.eUreka2.pages.admin.project;


import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.Project;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

@Deprecated
public class ModuleAdminEdit extends AbstractPageAdminProject{
	@Property
	private Project curProj;
	private String projId;
	@Property
	private ProjModule pMod;
	private int modId;
	@Property
	private Module mod;
	
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private ModuleDAO modDAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;

	void onActivate(EventContext ec){
		projId = ec.get(String.class, 0);
		modId = ec.get(Integer.class, 1);
	}
	Object[] onPassivate(){
		return new Object[]{projId, modId};
	}
	
	void setupRender(){
		if(!canManageProjects()){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjId", projId));
		}
		mod = modDAO.getModuleById(modId);
		if(mod==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ModuleId", modId));
		}
		pMod = curProj.getProjModule(mod);
		if(pMod==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "projID-ModuleID", projId+" "+modId));
		}
		
	}
	
	void onPrepareForSubmitFromForm(){
		curProj = projDAO.getProjectById(projId);
		mod = modDAO.getModuleById(modId);
		pMod = curProj.getProjModule(mod);
	}
	
	public void onValidateFormFromForm() {
	
	}
	
	@CommitAfter
	Object onSuccessFromForm(){
		//pMod.setCustomModuleName(customModuleName);
		projDAO.saveProject(curProj);
		
		return linkSource.createPageRenderLinkWithContext(ManageAdminModule.class, curProj.getId()); 
	}
	
	
	
}
