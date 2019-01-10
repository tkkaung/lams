package ntu.celt.eUreka2.pages.modules.teameffectiveness;


import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.teameffectiveness.TEDAO;
import ntu.celt.eUreka2.modules.teameffectiveness.TESurvey;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;

public class AssesseeFinish extends AbstractPageTE{
	@Property
	private Project project;
	private long teId;
	@Property
	private TESurvey te;
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private TEDAO teDAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	void onActivate(EventContext ec) {
		teId = ec.get(Long.class, 0);
		
		te = teDAO.getTESurveyById(teId);
		if(te==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", teId));
		
		project = te.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {teId};
	}
	
	void setupRender(){
				
	}
	
	

}
