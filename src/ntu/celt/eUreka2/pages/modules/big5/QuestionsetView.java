package ntu.celt.eUreka2.pages.modules.big5;

import java.util.List;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.big5.BIG5DAO;
import ntu.celt.eUreka2.modules.big5.BDimension;
import ntu.celt.eUreka2.modules.big5.BQuestion;
import ntu.celt.eUreka2.modules.big5.BQuestionSet;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class QuestionsetView extends AbstractPageBIG5{
	private long id;
	@Property
	private BQuestionSet qset;
	private String pid;
	@Property
	private Project project;
	
	
	@Property
	private BQuestion que; 
	
	@Property
	private int rowIndex;
	@Property
	private List<BDimension> cdimensions;
	@Property
	private BDimension cdimension;
	
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private BIG5DAO big5DAO;
	@Inject
	private Messages messages;
	
	
	void onActivate(EventContext ec) {
		pid = ec.get(String.class, 0);
		id = ec.get(Long.class, 1);
		
		qset = big5DAO.getBQuestionSetById(id);
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
		cdimensions = big5DAO.getAllBDimensions();


	}
	
	
	
}
