package ntu.celt.eUreka2.pages.modules.ipsp;

import java.util.List;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.ipsp.IPSPDAO;
import ntu.celt.eUreka2.modules.ipsp.IDimension;
import ntu.celt.eUreka2.modules.ipsp.IQuestion;
import ntu.celt.eUreka2.modules.ipsp.IQuestionSet;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class QuestionsetView extends AbstractPageIPSP{
	private long id;
	@Property
	private IQuestionSet qset;
	private String pid;
	@Property
	private Project project;
	
	
	@Property
	private IQuestion que; 
	
	@Property
	private int rowIndex;
	@Property
	private List<IDimension> cdimensions;
	@Property
	private IDimension cdimension;
	
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private IPSPDAO ipspDAO;
	@Inject
	private Messages messages;
	
	
	void onActivate(EventContext ec) {
		pid = ec.get(String.class, 0);
		id = ec.get(Long.class, 1);
		
		qset = ipspDAO.getIQuestionSetById(id);
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
		cdimensions = ipspDAO.getAllIDimensions();


	}
	
	
	
}
