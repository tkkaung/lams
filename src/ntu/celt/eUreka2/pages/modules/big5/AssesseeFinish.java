package ntu.celt.eUreka2.pages.modules.big5;


import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.big5.BIG5DAO;
import ntu.celt.eUreka2.modules.big5.BIG5Survey;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;

public class AssesseeFinish extends AbstractPageBIG5{
	@Property
	private Project project;
	private long big5Id;
	@Property
	private BIG5Survey big5;
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private BIG5DAO big5DAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	void onActivate(EventContext ec) {
		big5Id = ec.get(Long.class, 0);
		
		big5 = big5DAO.getBIG5SurveyById(big5Id);
		if(big5==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", big5Id));
		
		project = big5.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {big5Id};
	}
	
	void setupRender(){
				
	}
	
	

}
