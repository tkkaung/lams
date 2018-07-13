package ntu.celt.eUreka2.pages.project;


import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjExtraInfoDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.ProjCAOExtraInfo;
import ntu.celt.eUreka2.entities.Project;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ScopeView extends AbstractPageProject{
	private int peInfoId;
	@Property
	private ProjCAOExtraInfo peInfo;
	@Property
	private Project curProj;
		
	@Inject
	private ProjExtraInfoDAO peInfoDAO;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		peInfoId = ec.get(Integer.class, 0);
	}
	Object[] onPassivate() {
		return new Object[] {peInfoId};
	}
	
	
	
	void setupRender() {
		peInfo = peInfoDAO.getProjExtraInfoById(peInfoId);
		if(peInfo==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "peInfoId", peInfoId));
		
		curProj = peInfo.getProject();
		if(!canApprovedScope(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
	}	
	
	
	
}
