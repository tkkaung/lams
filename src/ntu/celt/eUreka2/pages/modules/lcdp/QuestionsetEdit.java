package ntu.celt.eUreka2.pages.modules.lcdp;

import java.util.Date;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.lcdp.LCDPDAO;
import ntu.celt.eUreka2.modules.lcdp.PQuestion;
import ntu.celt.eUreka2.modules.lcdp.PQuestionSet;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;

public class QuestionsetEdit extends AbstractPageLCDP{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private long qsid;
	@Property
	private PQuestionSet qset;
	@Property
	private PQuestion que;
	@Property
	private int rowIndex;
	@Property
	private int numQuestion;
	
	enum SubmitType {SUBMIT, UPDATE_NUM_FIELDS};
	private SubmitType submitType = SubmitType.SUBMIT;
	
	
	@SessionState
	private AppState appState;
	
	@Inject
	private RequestGlobals requestGlobal;
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private LCDPDAO profDAO;
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
		qset = profDAO.getPQuestionSetById(qsid);
		
	}
	Object onPassivate() {
		return new Object[] {pid, qsid};
	}
	void setupRender(){
		
		
		numQuestion = qset.getQuestions().size();
	}
	
	
	
	@CommitAfter
	Object onSuccessFromForm() {
		
		qset.setMdate(new Date());
		
		switch(submitType){
		case SUBMIT:
			profDAO.updatePQuestionSet(qset);
			
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
					PQuestion q = new PQuestion("", qset, i+1, false, false, false);
					qset.addQuestion(q);
				}
			}
			profDAO.updatePQuestionSet(qset);
			
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
