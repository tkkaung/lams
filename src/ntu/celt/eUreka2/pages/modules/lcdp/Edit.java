package ntu.celt.eUreka2.pages.modules.lcdp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.lcdp.LCDPDAO;
import ntu.celt.eUreka2.modules.lcdp.LCDPQuestion;
import ntu.celt.eUreka2.modules.lcdp.LCDPSurvey;
import ntu.celt.eUreka2.modules.lcdp.PQuestionSet;

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

public class Edit extends AbstractPageLCDP{
	@Property
	private Project project;
	private long lcdpId;
	@Property
	private LCDPSurvey lcdp;
	
	@SuppressWarnings("unused")
	@Property
	private Integer[] availableDaysToRemind = {0, 1, 3, 5, 7};
	@SuppressWarnings("unused")
	@Property
	private Integer tempInt;
	@Property
	private Integer rowIndex;
	@Property
	private PQuestionSet qset;
	@Property
	private LCDPQuestion lcdpQue;
	@Property
	private List<LCDPQuestion> lcdpQueList;
	@Property
	private boolean qsetChanged = false;
	
	
	@Inject
	private LCDPDAO lcdpDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	
	void onActivate(EventContext ec) {
		lcdpId = ec.get(Long.class, 0);
		
		lcdp = lcdpDAO.getLCDPSurveyById(lcdpId);
		if(lcdp==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", lcdpId));
		
		project = lcdp.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {lcdpId};
	}
	
	
	
	void setupRender(){
		if(!canManageLCDPSurvey(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		lcdpQueList = lcdp.getQuestions();
		qset = lcdp.getQuestionSet();
	}
	
	void onPrepareFromForm(){
		
	}
	
	@Component
	private Form form;
	@Component(id="edate")
	private DateField edateField;
	void onValidateFormFromForm(){
		
		if (lcdp.getEdate().before(lcdp.getSdate())) {
			form.recordError(edateField, messages.get("endDate-must-be-after-startDate"));
		}
	}	
	
	
	@InjectComponent
	private Zone questionDetailZone;
	Object onSelectQuestionset(){
		String qsId = request.getParameter("param");
		qset = lcdp.getQuestionSet();
		if(qset != null && (!qsId.equals(String.valueOf(qset.getId())) || qsId==null )){
			qsetChanged = true;
		}
		if(qsId==null){
			qset = null;
			lcdpQueList = new ArrayList<LCDPQuestion>();
		}
		else{
			qset = lcdpDAO.getPQuestionSetById(Long.parseLong(qsId));
			lcdpQueList = new ArrayList<LCDPQuestion>();
			lcdpQueList.addAll(qset.getQuestions());
		}
		return questionDetailZone.getBody();
	}
	
	
	@CommitAfter
	Object onSuccessFromForm(){
		lcdp.setMdate(new Date());
		lcdp.setEditor(getCurUser());
		if(qset!=null){
			if(lcdp.getQuestionSet()==null || (qset.getId() != lcdp.getQuestionSet().getId())){
				lcdp.setQuestionSet(qset);
				lcdp.setQuestionSetName(qset.getName());	
				lcdp.getLcdpUsers().clear();
				lcdp.getQuestions().clear();
				for(LCDPQuestion q: qset.getQuestions()){
					lcdp.addQuestions(q.createCopy());   //NOTE: create Copy because want to remove field "questionSet" which is in LQuestion
				}
			}
		}
		else{
			lcdp.setQuestionSet(null);
			lcdp.setQuestionSetName("");
			lcdp.getLcdpUsers().clear();
			lcdp.getQuestions().clear();
		}
		
		
		
		//set reminder
		lcdp.getDaysToRemind().clear();
		String[] daysToRemind = request.getParameters("dayChkBox");
		if(daysToRemind !=null){
			for(String _day : daysToRemind){
				lcdp.addDaysToRemind(Integer.parseInt(_day));
			}
		}
		
		lcdpDAO.updateLCDPSurvey(lcdp);
		
		return linkSource.createPageRenderLinkWithContext(Manage.class, project.getId());
	}
	
	
	
	
	
}
