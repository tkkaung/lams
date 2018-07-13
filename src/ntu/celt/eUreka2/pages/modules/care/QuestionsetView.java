package ntu.celt.eUreka2.pages.modules.care;

import java.util.List;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.care.CAREDAO;
import ntu.celt.eUreka2.modules.care.CDimension;
import ntu.celt.eUreka2.modules.care.CQuestion;
import ntu.celt.eUreka2.modules.care.CQuestionSet;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class QuestionsetView extends AbstractPageCARE{
	private long id;
	@Property
	private CQuestionSet qset;
	private String pid;
	@Property
	private Project project;
	
	
	@SuppressWarnings("unused")
	@Property
	private CQuestion que; 
	
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	@Property
	private List<CDimension> cdimensions;
	@Property
	private CDimension cdimension;
	
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private CAREDAO careDAO;
	@Inject
	private Messages messages;
	
	
	void onActivate(EventContext ec) {
		pid = ec.get(String.class, 0);
		id = ec.get(Long.class, 1);
		
		qset = careDAO.getCQuestionSetById(id);
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
		cdimensions = careDAO.getAllCDimensions();


	}
	
	
	
}
