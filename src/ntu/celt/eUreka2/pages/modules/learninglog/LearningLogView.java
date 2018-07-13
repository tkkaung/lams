package ntu.celt.eUreka2.pages.modules.learninglog;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.learninglog.LLogFile;
import ntu.celt.eUreka2.modules.learninglog.LearningLogDAO;
import ntu.celt.eUreka2.modules.learninglog.LogEntry;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Response;

public class LearningLogView extends AbstractPageLearningLog
{
	@Property
	private LogEntry entry;
	private Long entryId;
	@SuppressWarnings("unused")
	@Property
	private Project curProj;

	@SuppressWarnings("unused")
	@Property
	private LLogFile tempFile;
	
	@Inject
	private Messages messages;
	@Inject
	private LearningLogDAO learninglogDAO;
	@Inject
	private Response response;
	
	void onActivate(EventContext ec) {
		entryId = ec.get(Long.class, 0);
	}
	Object[] onPassivate() {
		return new Object[] {entryId};
	}
	
	void setupRender(){
		entry = learninglogDAO.getLogEntryById(entryId);
		if(entry==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AnnmtId", entryId));
		}
		curProj = entry.getProject(); 
		if(!entry.getCreator().equals(getCurUser())){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		response.setIntHeader("X-XSS-Protection", 0); //to let browser execute script after the script was added, 
														// e.g. youtube iframe was added
	}
}
