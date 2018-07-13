package ntu.celt.eUreka2.pages.modules.profiling;


import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.profiling.Profiling;
import ntu.celt.eUreka2.modules.profiling.ProfilingDAO;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;

public class AssesseeFinish extends AbstractPageProfiling{
	@Property
	private Project project;
	private long profId;
	@Property
	private Profiling prof;
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private ProfilingDAO profDAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	void onActivate(EventContext ec) {
		profId = ec.get(Long.class, 0);
		
		prof = profDAO.getProfilingById(profId);
		if(prof==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Profiling ID", profId));
		
		project = prof.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {profId};
	}
	
	void setupRender(){
				
	}
	
	

}
