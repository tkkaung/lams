package ntu.celt.eUreka2.pages.modules.big5;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.big5.BIG5DAO;
import ntu.celt.eUreka2.modules.big5.BDimension;
import ntu.celt.eUreka2.modules.big5.BQuestion;
import ntu.celt.eUreka2.modules.big5.BQuestionSet;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;

public class QuestionsetEdit extends AbstractPageBIG5{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private long qsid;
	@Property
	private BQuestionSet qset;
	@Property
	private BQuestion que;
	@Property
	private int rowIndex;
	@Property
	private int numQuestion;
	@Property
	private List<BDimension> cdimensions;
	@Property
	private BDimension cdimension;
	
	
	enum SubmitType {SUBMIT, UPDATE_NUM_FIELDS};
	private SubmitType submitType = SubmitType.SUBMIT;
	
	
	@SessionState
	private AppState appState;
	
	@Inject
	private RequestGlobals requestGlobal;
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private BIG5DAO big5DAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Request request;
	
	void onActivate(EventContext ec) {
		pid = ec.get(String.class, 0);
		qsid = ec.get(Long.class, 1);
		
		
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", pid));
		qset = big5DAO.getBQuestionSetById(qsid);
		
	}
	Object onPassivate() {
		return new Object[] {pid, qsid};
	}
	void setupRender(){
		cdimensions = big5DAO.getAllBDimensions();

		
		numQuestion = qset.getQuestions().size();
	}
	
	
	
	@CommitAfter
	Object onSuccessFromForm() {
		
		qset.setMdate(new Date());
		
		switch(submitType){
		case SUBMIT:
			big5DAO.updateBQuestionSet(qset);
			
			return linkSource.createPageRenderLinkWithContext(ManageQuestionset.class, project.getId());
			
		case UPDATE_NUM_FIELDS:
			int curNumQuestion = qset.getQuestions().size();
			if(numQuestion<curNumQuestion){
				for(int i=curNumQuestion-1; i>=numQuestion; i--){
					qset.getQuestions().remove(i);
				}
			}
			else if(numQuestion>curNumQuestion){
				for(int i=curNumQuestion; i<numQuestion; i++){
					String[] dims = new String[BDimension.NUM_DIMENSIONS];
					BQuestion q = new BQuestion("", qset, i+1, dims );
					qset.addQuestion(q);
				}
			}
			big5DAO.updateBQuestionSet(qset);
			
			break;
		}	
		
		
		return null;
	}
	void onSelectedFromUpdateNumFields(){
		submitType = SubmitType.UPDATE_NUM_FIELDS;
	}
	void onSelectedFromBSubmit(){
		submitType = SubmitType.SUBMIT;
	}
	  
}
