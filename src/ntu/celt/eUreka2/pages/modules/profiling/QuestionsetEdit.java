package ntu.celt.eUreka2.pages.modules.profiling;

import java.util.Date;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.profiling.LDimension;
import ntu.celt.eUreka2.modules.profiling.LQuestion;
import ntu.celt.eUreka2.modules.profiling.LQuestionSet;
import ntu.celt.eUreka2.modules.profiling.ProfilingDAO;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;

public class QuestionsetEdit extends AbstractPageProfiling{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private long qsid;
	@Property
	private LQuestionSet qset;
	@Property
	private LQuestion que;
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
	private ProfilingDAO profDAO;
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
		qset = profDAO.getLQuestionSetById(qsid);
		
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
			profDAO.updateLQuestionSet(qset);
			
			return linkSource.createPageRenderLinkWithContext(ManageQuestionset.class, project.getId());
			
		case UPDATE_NUM_FIELDS:
			int curNumQuestion = qset.getQuestions().size();
			if(numQuestion<curNumQuestion){
				for(int i=curNumQuestion-1; i>=numQuestion; i--){
					qset.getQuestions().remove(i);
				}
			}
			else if(numQuestion>curNumQuestion){
				LDimension defaultDim = profDAO.getLDimensionById(LDimension._1_INDIVIDUAL_CONSIDERATION);
				for(int i=curNumQuestion; i<numQuestion; i++){
					LQuestion q = new LQuestion("", qset, defaultDim , false, i+1, false);
					qset.addQuestion(q);
				}
			}
			profDAO.updateLQuestionSet(qset);
			
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
