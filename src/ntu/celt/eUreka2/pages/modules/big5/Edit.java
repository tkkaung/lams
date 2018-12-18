package ntu.celt.eUreka2.pages.modules.big5;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.big5.BIG5DAO;
import ntu.celt.eUreka2.modules.big5.BIG5Question;
import ntu.celt.eUreka2.modules.big5.BIG5Survey;
import ntu.celt.eUreka2.modules.big5.BQuestionSet;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.DateField;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Select;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;

public class Edit extends AbstractPageBIG5{
	@Property
	private Project project;
	private long big5Id;
	@Property
	private BIG5Survey big5;
	
	@Property
	private Integer[] availableDaysToRemind = {0, 1, 3, 5, 7};
	@Property
	private Integer tempInt;
	@Property
	private Integer rowIndex;
	@Property
	private BQuestionSet qset;
	@Property
	private BIG5Question big5Que;
	@Property
	private List<BIG5Question> big5QueList;
	@Property
	private boolean qsetChanged = false;
	
	@Inject
	private BIG5DAO big5DAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	
	void onActivate(EventContext ec) {
		big5Id = ec.get(Long.class, 0);
		
		big5 = big5DAO.getBIG5SurveyById(big5Id);
		if(big5==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", big5Id));
		
		project = big5.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {big5Id};
	}
	
	
	
	void setupRender(){
		if(!canManageBIG5Survey(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		big5QueList = big5.getQuestions();
		qset = big5.getQuestionSet();
	}
	
	void onPrepareFromForm(){
		
	}
	
	@Component
	private Form form;
	@Component(id="edate")
	private DateField edateField;
	void onValidateFormFromForm(){
		
		if (big5.getEdate().before(big5.getSdate())) {
			form.recordError(edateField, messages.get("endDate-must-be-after-startDate"));
		}
	}	
	
	
	@InjectComponent
	private Zone questionDetailZone;
	Object onSelectQuestionset(){
		String qsId = request.getParameter("param");
		qset = big5.getQuestionSet();
		if(qset != null && (!qsId.equals(String.valueOf(qset.getId())) || qsId==null )){
			qsetChanged = true;
		}
		
		if(qsId==null){
			qset = null;
			big5QueList = new ArrayList<BIG5Question>();
		}
		else{
					
			qset = big5DAO.getBQuestionSetById(Long.parseLong(qsId));
			big5QueList = new ArrayList<BIG5Question>();
			big5QueList.addAll(qset.getQuestions());
		}
		return questionDetailZone.getBody();
	}
	
	
	@CommitAfter
	Object onSuccessFromForm(){
		big5.setMdate(new Date());
		big5.setEditor(getCurUser());
		if(qset!=null){
			if(big5.getQuestionSet()==null || (qset.getId() != big5.getQuestionSet().getId())){
				big5.setQuestionSet(qset);
				big5.setQuestionSetName(qset.getName());
				big5.getBig5Users().clear();
				big5.getQuestions().clear();
				for(BIG5Question q: qset.getQuestions()){
					big5.addQuestions(q.createCopy());   //NOTE: create Copy because want to remove field "questionSet" which is in LQuestion
				}
			}
		}
		else{
			big5.setQuestionSet(null);
			big5.setQuestionSetName("");
			big5.getBig5Users().clear();
			big5.getQuestions().clear();
		}
		
		
		
		//set reminder
		big5.getDaysToRemind().clear();
		String[] daysToRemind = request.getParameters("dayChkBox");
		if(daysToRemind !=null){
			for(String _day : daysToRemind){
				big5.addDaysToRemind(Integer.parseInt(_day));
			}
		}
		
		big5DAO.updateBIG5Survey(big5);
		
		return linkSource.createPageRenderLinkWithContext(Manage.class, project.getId());
	}
	
	
	
	
	
}
