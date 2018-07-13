package ntu.celt.eUreka2.pages.modules.care;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.care.CAREDAO;
import ntu.celt.eUreka2.modules.care.CAREQuestion;
import ntu.celt.eUreka2.modules.care.CARESurvey;
import ntu.celt.eUreka2.modules.care.CQuestionSet;

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

public class Edit extends AbstractPageCARE{
	@Property
	private Project project;
	private long careId;
	@Property
	private CARESurvey care;
	
	@SuppressWarnings("unused")
	@Property
	private Integer[] availableDaysToRemind = {0, 1, 3, 5, 7};
	@SuppressWarnings("unused")
	@Property
	private Integer tempInt;
	@Property
	private Integer rowIndex;
	@Property
	private CQuestionSet qset;
	@Property
	private CAREQuestion careQue;
	@Property
	private List<CAREQuestion> careQueList;
	@Property
	private boolean qsetChanged = false;
	
	@Inject
	private CAREDAO careDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	
	void onActivate(EventContext ec) {
		careId = ec.get(Long.class, 0);
		
		care = careDAO.getCARESurveyById(careId);
		if(care==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", careId));
		
		project = care.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {careId};
	}
	
	
	
	void setupRender(){
		if(!canManageCARESurvey(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		careQueList = care.getQuestions();
		qset = care.getQuestionSet();
	}
	
	void onPrepareFromForm(){
		
	}
	
	@Component
	private Form form;
	@Component(id="edate")
	private DateField edateField;
	void onValidateFormFromForm(){
		
		if (care.getEdate().before(care.getSdate())) {
			form.recordError(edateField, messages.get("endDate-must-be-after-startDate"));
		}
	}	
	
	
	@InjectComponent
	private Zone questionDetailZone;
	Object onSelectQuestionset(){
		String qsId = request.getParameter("param");
		qset = care.getQuestionSet();
		if(qset != null && (!qsId.equals(String.valueOf(qset.getId())) || qsId==null )){
			qsetChanged = true;
		}
		
		if(qsId==null){
			qset = null;
			careQueList = new ArrayList<CAREQuestion>();
		}
		else{
					
			qset = careDAO.getCQuestionSetById(Long.parseLong(qsId));
			careQueList = new ArrayList<CAREQuestion>();
			careQueList.addAll(qset.getQuestions());
		}
		return questionDetailZone.getBody();
	}
	
	
	@CommitAfter
	Object onSuccessFromForm(){
		care.setMdate(new Date());
		care.setEditor(getCurUser());
		if(qset!=null){
			if(care.getQuestionSet()==null || (qset.getId() != care.getQuestionSet().getId())){
				care.setQuestionSet(qset);
				care.setQuestionSetName(qset.getName());
				care.getCareUsers().clear();
				care.getQuestions().clear();
				for(CAREQuestion q: qset.getQuestions()){
					care.addQuestions(q.createCopy());   //NOTE: create Copy because want to remove field "questionSet" which is in LQuestion
				}
			}
		}
		else{
			care.setQuestionSet(null);
			care.setQuestionSetName("");
			care.getCareUsers().clear();
			care.getQuestions().clear();
		}
		
		
		
		//set reminder
		care.getDaysToRemind().clear();
		String[] daysToRemind = request.getParameters("dayChkBox");
		if(daysToRemind !=null){
			for(String _day : daysToRemind){
				care.addDaysToRemind(Integer.parseInt(_day));
			}
		}
		
		careDAO.updateCARESurvey(care);
		
		return linkSource.createPageRenderLinkWithContext(Manage.class, project.getId());
	}
	
	
	
	
	
}
