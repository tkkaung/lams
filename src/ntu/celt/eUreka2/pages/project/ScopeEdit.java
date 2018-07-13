package ntu.celt.eUreka2.pages.project;


import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjExtraInfoDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.ProjCAOExtraInfo;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ScopeEdit extends AbstractPageProject{
	@Property
	private Project curProj;
	private String projId;
	@Property
	private ProjCAOExtraInfo peInfo;
	@Persist("flash")
	@Property
	private String scope;
	@Persist("flash")
	@Property
	private String prerequisite;
	
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private ProjExtraInfoDAO projExtraInfoDAO;
	@Inject
	private Messages messages;

	void onActivate(EventContext ec){
			projId = ec.get(String.class, 0);
		
	}
	Object[] onPassivate(){
		
		return new Object[]{projId};
	}
	
	public void setContext(String projId, String scope, String prerequisite){
		this.projId = projId;
		this.scope = scope;
		this.prerequisite = prerequisite;
	}
	
	void setupRender(){
		curProj = projDAO.getProjectById(projId);
		if(curProj==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjId", projId));
		if(!canUpdateScope(curProj))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		peInfo = projExtraInfoDAO.getProjExtraInfoByProject(curProj);
		
		if(peInfo==null)
			peInfo = new ProjCAOExtraInfo();
		if(scope==null)
			scope = peInfo.getScope();
		if(prerequisite==null)
			prerequisite = peInfo.getPrerequisite();
	
	}
	void onPrepareForSubmitFromForm(){
		curProj = projDAO.getProjectById(projId);
		peInfo = projExtraInfoDAO.getProjExtraInfoByProject(curProj);
		
	}
	
	
	@InjectPage
	private ScopeSubmit scopePreviewPage;
	
	Object onSuccessFromForm(){
		scope = Util.filterOutRestrictedHtmlTags(scope);
		prerequisite = Util.filterOutRestrictedHtmlTags(prerequisite);
		
		scopePreviewPage.setContext(projId, scope, prerequisite);
		
		return scopePreviewPage;
	}
	
	
	
}
