package ntu.celt.eUreka2.pages.modules.profiling;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupDAO;
import ntu.celt.eUreka2.modules.profiling.LQuestionSet;
import ntu.celt.eUreka2.modules.profiling.ProfileQuestion;
import ntu.celt.eUreka2.modules.profiling.Profiling;
import ntu.celt.eUreka2.modules.profiling.ProfilingDAO;

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

public class Edit extends AbstractPageProfiling{
	@Property
	private Project project;
	private long profId;
	@Property
	private Profiling prof;
	
	@SuppressWarnings("unused")
	@Property
	private Integer[] availableDaysToRemind = {0, 1, 3, 5, 7};
	@SuppressWarnings("unused")
	@Property
	private Integer tempInt;
	@Property
	private Integer rowIndex;
	@Property
	private Group group;
	@Property
	private LQuestionSet qset;
	@Property
	private ProfileQuestion profQue;
	@Property
	private List<ProfileQuestion> profQueList;
	
	
	@Inject
	private ProfilingDAO profDAO;
	@Inject
	private GroupDAO gDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	
	void onActivate(EventContext ec) {
		profId = ec.get(Long.class, 0);
		
		prof = profDAO.getProfilingById(profId);
		if(prof==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProfilingID", profId));
		
		project = prof.getProject();
		group = prof.getGroup();
	}
	Object[] onPassivate() {
		return new Object[] {profId};
	}
	
	
	
	void setupRender(){
		if(!canManageProfiling(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		profQueList = prof.getQuestions();
		qset = prof.getQuestionSet();
	}
	
	void onPrepareFromForm(){
		
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
		
		if (prof.getEdate().before(prof.getSdate())) {
			form.recordError(edateField, messages.get("endDate-must-be-after-startDate"));
		}
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
	
	@InjectComponent
	private Zone questionDetailZone;
	Object onSelectQuestionset(){
		String qsId = request.getParameter("param");
		if(qsId==null){
			qset = null;
			profQueList = new ArrayList<ProfileQuestion>();
		}
		else{
			qset = profDAO.getLQuestionSetById(Long.parseLong(qsId));
			profQueList = new ArrayList<ProfileQuestion>();
			profQueList.addAll(qset.getQuestions());
		}
		return questionDetailZone.getBody();
	}
	
	
	@CommitAfter
	Object onSuccessFromForm(){
		prof.setMdate(new Date());
		prof.setEditor(getCurUser());
		prof.setGroup(group);
		if(qset!=null){
			if(prof.getQuestionSet()==null || (qset.getId() != prof.getQuestionSet().getId())){
				prof.setQuestionSet(qset);
				prof.setQuestionSetName(qset.getName());	
				prof.getQuestions().clear();
				for(ProfileQuestion q: qset.getQuestions()){
					prof.addQuestions(q.createCopy());   //NOTE: create Copy because want to remove field "questionSet" which is in LQuestion
				}
			}
		}
		else{
			prof.setQuestionSet(null);
			prof.setQuestionSetName("");
			prof.getQuestions().clear();
		}
		
		
		
		//set reminder
		prof.getDaysToRemind().clear();
		String[] daysToRemind = request.getParameters("dayChkBox");
		if(daysToRemind !=null){
			for(String _day : daysToRemind){
				prof.addDaysToRemind(Integer.parseInt(_day));
			}
		}
		
		profDAO.updateProfiling(prof);
		
		return linkSource.createPageRenderLinkWithContext(Manage.class, project.getId());
	}
	
	
	
	
	
}
