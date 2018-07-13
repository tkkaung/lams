package ntu.celt.eUreka2.pages.modules.care;


import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.care.CAREDAO;
import ntu.celt.eUreka2.modules.care.CARESurvey;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;

public class AssesseeFinish extends AbstractPageCARE{
	@Property
	private Project project;
	private long careId;
	@Property
	private CARESurvey care;
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private CAREDAO careDAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	void onActivate(EventContext ec) {
		careId = ec.get(Long.class, 0);
		
		care = careDAO.getCARESurveyById(careId);
		if(care==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", careId));
		
		project = care.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {careId};
	}
	
	void setupRender(){
				
	}
	
	

}
