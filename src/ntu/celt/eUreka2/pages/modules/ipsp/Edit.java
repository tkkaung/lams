package ntu.celt.eUreka2.pages.modules.ipsp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupDAO;
import ntu.celt.eUreka2.modules.ipsp.IPSPDAO;
import ntu.celt.eUreka2.modules.ipsp.IPSPQuestion;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurvey;
import ntu.celt.eUreka2.modules.ipsp.IQuestionSet;

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

public class Edit extends AbstractPageIPSP{
	@Property
	private Project project;
	private long ipspId;
	@Property
	private IPSPSurvey ipsp;
	
	@Property
	private Integer[] availableDaysToRemind = {0, 1, 3, 5, 7};
	@Property
	private Integer tempInt;
	@Property
	private Integer rowIndex;
	@Property
	private IQuestionSet qset;
	@Property
	private IPSPQuestion ipspQue;
	@Property
	private List<IPSPQuestion> ipspQueList;
	@Property
	private boolean qsetChanged = false;
	
	@Inject
	private IPSPDAO ipspDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	@Inject
	private GroupDAO gDAO;

	
	@Property
	private Group group;

	
	void onActivate(EventContext ec) {
		ipspId = ec.get(Long.class, 0);
		
		ipsp = ipspDAO.getIPSPSurveyById(ipspId);
		if(ipsp==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", ipspId));
		
		project = ipsp.getProject();
		group = ipsp.getGroup();

	}
	Object[] onPassivate() {
		return new Object[] {ipspId};
	}
	
	
	
	void setupRender(){
		if(!canManageIPSPSurvey(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		ipspQueList = ipsp.getQuestions();
		qset = ipsp.getQuestionSet();
	}
	
	void onPrepareFromForm(){
		
	}

	
	
	
	@InjectComponent
	private Zone groupDetailZone;
	Object onSelectGroup(){
		String gId = request.getParameter("param");
		if(gId==null){
			group = null;
		}
		else
			group = gDAO.getGroupById(Long.parseLong(gId));
		
		return groupDetailZone.getBody();
	}
	
	@Component
	private Form form;
	@Component
	private Select selectGroup;
	@Component(id="edate")
	private DateField edateField;
	void onValidateFormFromForm(){
		if(group == null){
			
		}
		else if(isInvalidGroup(group)){
			form.recordError(selectGroup, messages.format("invalid-group-set"));
		}
		if (ipsp.getEdate().before(ipsp.getSdate())) {
			form.recordError(edateField, messages.get("endDate-must-be-after-startDate"));
			if (ipsp.getEdate().before(ipsp.getSdate())) {
			}
		}
	}	
	
	
	@InjectComponent
	private Zone questionDetailZone;
	Object onSelectQuestionset(){
		String qsId = request.getParameter("param");
		qset = ipsp.getQuestionSet();
		if(qset != null && (!qsId.equals(String.valueOf(qset.getId())) || qsId==null )){
			qsetChanged = true;
		}
		
		if(qsId==null){
			qset = null;
			ipspQueList = new ArrayList<IPSPQuestion>();
		}
		else{
					
			qset = ipspDAO.getIQuestionSetById(Long.parseLong(qsId));
			ipspQueList = new ArrayList<IPSPQuestion>();
			ipspQueList.addAll(qset.getQuestions());
		}
		return questionDetailZone.getBody();
	}
	
	
	@CommitAfter
	Object onSuccessFromForm(){
		ipsp.setMdate(new Date());
		ipsp.setEditor(getCurUser());
		ipsp.setGroup(group);
		if(qset!=null){
			if(ipsp.getQuestionSet()==null || (qset.getId() != ipsp.getQuestionSet().getId())){
				ipsp.setQuestionSet(qset);
				ipsp.setQuestionSetName(qset.getName());
				ipsp.getIpspUsers().clear();
				ipsp.getQuestions().clear();
				for(IPSPQuestion q: qset.getQuestions()){
					ipsp.addQuestions(q.createCopy());   //NOTE: create Copy because want to remove field "questionSet" which is in LQuestion
				}
			}
		}
		else{
			ipsp.setQuestionSet(null);
			ipsp.setQuestionSetName("");
			ipsp.getIpspUsers().clear();
			ipsp.getQuestions().clear();
		}
		
		
		
		//set reminder
		ipsp.getDaysToRemind().clear();
		String[] daysToRemind = request.getParameters("dayChkBox");
		if(daysToRemind !=null){
			for(String _day : daysToRemind){
				ipsp.addDaysToRemind(Integer.parseInt(_day));
			}
		}
		
		ipspDAO.updateIPSPSurvey(ipsp);
		
		return linkSource.createPageRenderLinkWithContext(Manage.class, project.getId());
	}
	
	
	
	
	
}
