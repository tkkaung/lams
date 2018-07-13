package ntu.celt.eUreka2.pages.modules.lcdp;


import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.lcdp.LCDPDAO;
import ntu.celt.eUreka2.modules.lcdp.LCDPSurvey;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;

public class AssesseeFinish extends AbstractPageLCDP{
	@Property
	private Project project;
	private long lcdpId;
	@Property
	private LCDPSurvey lcdp;
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private LCDPDAO lcdpDAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	void onActivate(EventContext ec) {
		lcdpId = ec.get(Long.class, 0);
		
		lcdp = lcdpDAO.getLCDPSurveyById(lcdpId);
		if(lcdp==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", lcdpId));
		
		project = lcdp.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {lcdpId};
	}
	
	void setupRender(){
				
	}
	
	

}
