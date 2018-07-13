package ntu.celt.eUreka2.pages.admin.project;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class ProjectAdminDelete extends AbstractPageAdminProject {
	@Property
	private Project proj;
	@Property
	private List<Project> projs ;
	@Property
	private String confirmText;
	@Property
	private int rowIndex;
	
	@Persist
	private String[] projIDs;
	
	
	@SessionState
	private AppState appState;
	
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private ProjectDAO projDAO;
	
/*	void onActivate(String id){
	}
	Object onPassivate(){
		return projId;
	}
*/
	public void setContext(String[] projIDs){
		this.projIDs = projIDs;
	}
	void setupRender(){
		if(!canManageProjects()){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		projs = new ArrayList<Project>();
		if(projIDs!=null){
			for(String projId: projIDs){
				proj = projDAO.getProjectById(projId);
				if(proj!=null)
					projs.add(proj);
			}
		}
	}
	void onPrepareForSubmitFromForm(){  
		projs = new ArrayList<Project>();
		for(String projId: projIDs){
			proj = projDAO.getProjectById(projId);
			if(proj!=null)
				projs.add(proj);
		}
	}
	
	Object onSelectedFromCancel(){
		projIDs = null;
		return linkSource.createPageRenderLink(ManageAdminProjects.class);
	}
	
	
	@Component
	private Form form;
	@Component(id="confirmText")
	private TextField confirmTextField;
	public void onValidateFormFromForm() {
		if (!messages.get("default-confirm-text").equalsIgnoreCase(confirmText)) {
			form.recordError(confirmTextField, messages.get("confirm-text-not-matched"));
		}
	}
	
	@CommitAfter
	Object onSuccessFromForm(){
		int count = 0;
		for(Project proj : projs){
			projDAO.deleteProjectPermanently(proj);
			count++;
		}
		appState.recordInfoMsg(messages.format("x-project-successfully-deleted", count));
		return linkSource.createPageRenderLink(ManageAdminProjects.class);
	}
	
	public int getNo(){
		return rowIndex+1;
	}
}
