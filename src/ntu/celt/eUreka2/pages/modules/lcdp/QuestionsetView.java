package ntu.celt.eUreka2.pages.modules.lcdp;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.lcdp.LCDPDAO;
import ntu.celt.eUreka2.modules.lcdp.PQuestion;
import ntu.celt.eUreka2.modules.lcdp.PQuestionSet;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class QuestionsetView extends AbstractPageLCDP{
	private long id;
	@Property
	private PQuestionSet qset;
	private String pid;
	@Property
	private Project project;
	
	
	@SuppressWarnings("unused")
	@Property
	private PQuestion que; 
	
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private LCDPDAO profDAO;
	@Inject
	private Messages messages;
	
	
	void onActivate(EventContext ec) {
		pid = ec.get(String.class, 0);
		id = ec.get(Long.class, 1);
		
		qset = profDAO.getPQuestionSetById(id);
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
