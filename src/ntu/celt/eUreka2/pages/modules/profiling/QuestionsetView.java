package ntu.celt.eUreka2.pages.modules.profiling;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.profiling.LQuestion;
import ntu.celt.eUreka2.modules.profiling.LQuestionSet;
import ntu.celt.eUreka2.modules.profiling.ProfilingDAO;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class QuestionsetView extends AbstractPageProfiling{
	private long id;
	@Property
	private LQuestionSet qset;
	private String pid;
	@Property
	private Project project;
	
	
	@SuppressWarnings("unused")
	@Property
	private LQuestion que; 
	
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private ProfilingDAO profDAO;
	@Inject
	private Messages messages;
	
	
	void onActivate(EventContext ec) {
		pid = ec.get(String.class, 0);
		id = ec.get(Long.class, 1);
		
		qset = profDAO.getLQuestionSetById(id);
		if(qset==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "QuestionsetID", id));
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "projectID", pid));
	}
	Object[] onPassivate() {
		return new Object[] {pid, id};
	}
	
	void setupRender(){
		

	}
	
	
	
}
