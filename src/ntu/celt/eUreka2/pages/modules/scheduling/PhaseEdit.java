package ntu.celt.eUreka2.pages.modules.scheduling;

import java.util.Date;

import org.slf4j.Logger;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.DateField;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.scheduling.Milestone;
import ntu.celt.eUreka2.modules.scheduling.Phase;
import ntu.celt.eUreka2.modules.scheduling.PrivilegeSchedule;
import ntu.celt.eUreka2.modules.scheduling.Schedule;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;

public class PhaseEdit extends AbstractPageScheduling {
	@Property
	private Project curProj;
	private Schedule schedule;
	@Property
	private Milestone milestone;
	private Long milestoneId;
	@Property
	private Phase phase;
	private Long phaseId;
	
	@Component
	private Form editForm;
	
	
	@Inject
	private SchedulingDAO scheduleDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@SuppressWarnings("unused")
	@Inject
	private Logger logger;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		if(ec.getCount()==1){
			phaseId = ec.get(Long.class, 0);
		}
		if(ec.getCount()==2){
			milestoneId = ec.get(Long.class, 0);
			phaseId = ec.get(Long.class, 1);
		}
	}
	Object[] onPassivate() {
		if(milestoneId==null)
			return new Object[] {phaseId};
		else return new Object[] {milestoneId, phaseId};
	}
	public boolean isCreateMode(){
		if(phaseId<=0)
			return true;
		return false;
	}
	
	void setupRender(){
		if(isCreateMode()){
			milestone = scheduleDAO.getMilestoneById(milestoneId);
			if(milestone==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "MilestoneID", milestoneId));
			schedule = milestone.getSchedule();
			curProj = schedule.getProject();
			if(!canCreatePhase(curProj)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
		}
		else{
			phase = scheduleDAO.getPhaseById(phaseId);
			if(phase==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "phaseID", phaseId));
			milestone = phase.getMilestone();
			schedule = milestone.getSchedule();
			curProj = schedule.getProject();
			if( !canEditPhase(phase) ){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
		}
	}
	
	void onPrepareFromEditForm(){
		phase = scheduleDAO.getPhaseById(phaseId);
		if(isCreateMode()){ 
			phase = new Phase();
			milestone = scheduleDAO.getMilestoneById(milestoneId);
			schedule = milestone.getSchedule();
			curProj = schedule.getProject();
			phase.setMilestone(milestone);
			phase.setManager(getCurUser());
			phase.setStartDate(curProj.getSdate());
			phase.setEndDate(milestone.getDeadline());
		}
		else{
			milestone = phase.getMilestone();
			schedule = milestone.getSchedule();
			curProj = schedule.getProject();
		}
	}
	
	
	@Component(id="startDate")
	private DateField startDateField;
	@Component(id="endDate")
	private DateField endDateField;
	void onValidateFormFromEditForm(){
		if(phase.getStartDate().after(phase.getEndDate()))
			editForm.recordError(endDateField, messages.get("endDate-must-be-after-startDate"));
		if(phase.getStartDate().before(curProj.getSdate()) || phase.getStartDate().after(milestone.getDeadline()))
			editForm.recordError(startDateField, messages.get("sDate-before-projSDate-nor-after-mileDeadline-error"));
		if(phase.getEndDate().before(curProj.getSdate()) || phase.getEndDate().after(milestone.getDeadline()))
			editForm.recordError(endDateField, messages.get("eDate-before-projSDate-nor-after-mileDeadline-error"));
	
	}
	
	@CommitAfter
	Object onSuccessFromEditForm(){
		if(isCreateMode()){
			milestone.addPhase(phase);
			phase.setCreateDate(new Date());
		}
		phase.setModifyDate(new Date());
		scheduleDAO.savePhase(phase);
		
		if(isCreateMode())
			return linkSource.createPageRenderLinkWithContext(SchedulingIndex.class, curProj.getId());
		else
			return linkSource.createPageRenderLinkWithContext(PhaseView.class, phase.getId());
	}
	
	public String getBreadcrumbLink(){
		if(isCreateMode())
			return messages.get("add-new")+" "+messages.get("phase")+"=modules/scheduling/phaseedit?"+ milestoneId + ":" +phaseId;
		else
			return messages.get("phase")+": "+encode(phase.getName())+"=modules/scheduling/phaseview?"+ phaseId +
			","+ messages.get("edit")+" "+messages.get("phase")+"=modules/scheduling/phaseedit?"+ phaseId;
	}
	public String getPageTitle(){
		if(isCreateMode())
			return messages.get("add-new")+" "+messages.get("phase");
		return messages.get("edit")+" "+messages.get("phase");
	}
}
