package ntu.celt.eUreka2.pages.modules.teameffectiveness;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.teameffectiveness.TEDAO;
import ntu.celt.eUreka2.modules.teameffectiveness.TEQuestion;
import ntu.celt.eUreka2.modules.teameffectiveness.TESurvey;
import ntu.celt.eUreka2.modules.teameffectiveness.TEQuestionSet;

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

public class Edit extends AbstractPageTE{
	@Property
	private Project project;
	private long teId;
	@Property
	private TESurvey te;
	
	@Property
	private Integer[] availableDaysToRemind = {0, 1, 3, 5, 7};
	@Property
	private Integer tempInt;
	@Property
	private Integer rowIndex;
	@Property
	private TEQuestionSet qset;
	@Property
	private TEQuestion teQue;
	@Property
	private List<TEQuestion> teQueList;
	@Property
	private boolean qsetChanged = false;
	
	@Inject
	private TEDAO teDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	
	void onActivate(EventContext ec) {
		teId = ec.get(Long.class, 0);
		
		te = teDAO.getTESurveyById(teId);
		if(te==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", teId));
		
		project = te.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {teId};
	}
	
	
	
	void setupRender(){
		if(!canManageTESurvey(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		teQueList = te.getQuestions();
		qset = te.getQuestionSet();
	}
	
	void onPrepareFromForm(){
		
	}
	
	@Component
	private Form form;
	@Component(id="edate")
	private DateField edateField;
	void onValidateFormFromForm(){
		
		if (te.getEdate().before(te.getSdate())) {
			form.recordError(edateField, messages.get("endDate-must-be-after-startDate"));
		}
	}	
	
	
	@InjectComponent
	private Zone questionDetailZone;
	Object onSelectQuestionset(){
		String qsId = request.getParameter("param");
		qset = te.getQuestionSet();
		if(qset != null && (!qsId.equals(String.valueOf(qset.getId())) || qsId==null )){
			qsetChanged = true;
		}
		
		if(qsId==null){
			qset = null;
			teQueList = new ArrayList<TEQuestion>();
		}
		else{
					
			qset = teDAO.getTEQuestionSetById(Long.parseLong(qsId));
			teQueList = new ArrayList<TEQuestion>();
			teQueList.addAll(qset.getQuestions());
		}
		return questionDetailZone.getBody();
	}
	
	
	@CommitAfter
	Object onSuccessFromForm(){
		te.setMdate(new Date());
		te.setEditor(getCurUser());
		if(qset!=null){
			if(te.getQuestionSet()==null || (qset.getId() != te.getQuestionSet().getId())){
				te.setQuestionSet(qset);
				te.setQuestionSetName(qset.getName());
				te.getTEUsers().clear();
				te.getQuestions().clear();
				for(TEQuestion q: qset.getQuestions()){
					te.addQuestions(q.createCopy());   //NOTE: create Copy because want to remove field "questionSet" which is in LQuestion
				}
			}
		}
		else{
			te.setQuestionSet(null);
			te.setQuestionSetName("");
			te.getTEUsers().clear();
			te.getQuestions().clear();
		}
		
		
		
		//set reminder
		te.getDaysToRemind().clear();
		String[] daysToRemind = request.getParameters("dayChkBox");
		if(daysToRemind !=null){
			for(String _day : daysToRemind){
				te.addDaysToRemind(Integer.parseInt(_day));
			}
		}
		
		teDAO.updateTESurvey(te);
		
		return linkSource.createPageRenderLinkWithContext(Manage.class, project.getId());
	}
	
	
	
	
	
}
