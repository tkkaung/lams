package ntu.celt.eUreka2.pages.modules.teameffectiveness;

import java.util.List;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.teameffectiveness.TEDAO;
import ntu.celt.eUreka2.modules.teameffectiveness.TEDimension;
import ntu.celt.eUreka2.modules.teameffectiveness.TQuestion;
import ntu.celt.eUreka2.modules.teameffectiveness.TEQuestionSet;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class QuestionsetView extends AbstractPageTE{
	private long id;
	@Property
	private TEQuestionSet qset;
	private String pid;
	@Property
	private Project project;
	
	
	@Property
	private TQuestion que; 
	
	@Property
	private int rowIndex;
	@Property
	private List<TEDimension> cdimensions;
	@Property
	private TEDimension cdimension;
	
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private TEDAO teDAO;
	@Inject
	private Messages messages;
	
	
	void onActivate(EventContext ec) {
		pid = ec.get(String.class, 0);
		id = ec.get(Long.class, 1);
		
		qset = teDAO.getTEQuestionSetById(id);
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
		cdimensions = teDAO.getAllTEDimensions();


	}
	
	
	
}
