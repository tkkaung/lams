package ntu.celt.eUreka2.pages.admin.project;

import java.util.List;

import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.Project;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;

public class ManageAdminModule extends AbstractPageAdminProject {
	@Property
	private String projId;
	@Property
	private Project curProj;
	@Property
	private boolean admin;
	
	private enum SubmitType {ASSIGN, UNASSIGN}; 
	
	@Property
	@Persist
	private List<Module> resultMods;
	@SuppressWarnings("unused")
	@Property
	private Module mod;
	@Property
	private List<Module> selMods;
	@SuppressWarnings("unused")
	@Property
	private Module selMod;
	@SuppressWarnings("unused")
	@Property
	private int selModIndex;
	
	private SubmitType submitType;
	
	
	@Inject
	private Request rqust;
	@Inject
	private ProjectDAO projDAO;
	
	@Inject
	private ModuleDAO moduleDAO;
	@Inject
	private Messages messages;
	
	
	void onActivate(EventContext ec) {
		projId = ec.get(String.class, 0);
		if(ec.getCount()==2){
			admin = ec.get(Boolean.class, 1);
		}
	}
	Object[] onPassivate() {
		return new Object[] {projId, admin };
	}
	
	void setupRender(){
		if(!canManageProjects()){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjId", projId));
		}
		
		selMods = curProj.getModules();
		resultMods = moduleDAO.getAllModules();
		resultMods.removeAll(selMods);
		resultMods.removeAll(curProj.getType().getNonModules()); 
		if(!canAssignLeaderShipProfiling()){
			resultMods.remove(moduleDAO.getModuleByName(PredefinedNames.MODULE_LEADERSHIP_PROFILING));
		}
	}
	
	void onPrepareForSubmit(){
		if(!canManageProjects()){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjId", projId));
		}
	}
	
	public boolean shouldnotUnassign(Module mod){
		if(curProj.getType().getModules().contains(mod))
			return true;
		return false;
	}
	
	void onSelectedFromAssign() {
		submitType = SubmitType.ASSIGN;
	}
	void onSelectedFromUnassign() {
		submitType = SubmitType.UNASSIGN;
	}
	
	@Component(id = "assignForm")
	private Form assignForm;
	void onValidateFormFromAssignForm() {
		if(SubmitType.ASSIGN.equals(submitType)){
			String[] selModId = rqust.getParameters("chkBox");
			if(selModId==null || selModId.length==0){
				assignForm.recordError(messages.get("select-at-least-one-from-left"));
			}
		}
		else if(SubmitType.UNASSIGN.equals(submitType)){
			String[] selModId = rqust.getParameters("selChkBox");
			if(selModId==null || selModId.length==0){
				assignForm.recordError(messages.get("select-at-least-one-from-right"));
			}
		}
		
	}
	@CommitAfter
	void onSuccessFromAssignForm(){
		if(SubmitType.ASSIGN.equals(submitType)){
			String[] selModId = rqust.getParameters("chkBox");
			for(String mId : selModId){
				Module m = moduleDAO.getModuleById(Integer.parseInt(mId));
				curProj.addModule(new ProjModule(curProj, m));	
			}
			projDAO.saveProject(curProj);
		}
		else if(SubmitType.UNASSIGN.equals(submitType)){
			String[] selModId = rqust.getParameters("selChkBox");
			for(String mId : selModId){
				Module m = moduleDAO.getModuleById(Integer.parseInt(mId));
				ProjModule pm = curProj.getProjModule(m);
				curProj.removeModule(pm);
			}
			projDAO.saveProject(curProj);
		}
		
	}
	
	@CommitAfter
	void onActionFromMoveUp(String projId, Integer mId){
		Project proj = projDAO.getProjectById(projId);
		Module m = moduleDAO.getModuleById(mId);
		ProjModule pm = proj.getProjModule(m);
		List<ProjModule> pmList = proj.getProjmodules();
		int i = pmList.indexOf(pm);
		int moveToI = i - 1;
		if(moveToI >= 0){
			ProjModule tempPm = pmList.get(moveToI);
			pmList.set(moveToI, pm);
			pmList.set(i, tempPm);
		}
		proj.setProjmodules(pmList);
		projDAO.saveProject(proj);
	}
	@CommitAfter
	void onActionFromMoveDown(String projId, Integer mId){
		Project proj = projDAO.getProjectById(projId);
		Module m = moduleDAO.getModuleById(mId);
		ProjModule pm = proj.getProjModule(m);
		List<ProjModule> pmList = proj.getProjmodules();
		int i = pmList.indexOf(pm);
		int moveToI = i + 1;
		if(moveToI < pmList.size()){
			ProjModule tempPm = pmList.get(moveToI);
			pmList.set(moveToI, pm);
			pmList.set(i, tempPm);
		}
		proj.setProjmodules(pmList);
		projDAO.saveProject(proj);
	}
	
}
