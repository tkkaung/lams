package ntu.celt.eUreka2.pages.modules.ipsp;


import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.ipsp.IPSPDAO;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurvey;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;

public class AssesseeFinish extends AbstractPageIPSP{
	@Property
	private Project project;
	private long ipspId;
	@Property
	private IPSPSurvey ipsp;
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private IPSPDAO ipspDAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	void onActivate(EventContext ec) {
		ipspId = ec.get(Long.class, 0);
		
		ipsp = ipspDAO.getIPSPSurveyById(ipspId);
		if(ipsp==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", ipspId));
		
		project = ipsp.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {ipspId};
	}
	
	void setupRender(){
				
	}
	
	

}
