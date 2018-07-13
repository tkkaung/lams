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
import ntu.celt.eUreka2.modules.scheduling.PrivilegeSchedule;
import ntu.celt.eUreka2.modules.scheduling.Schedule;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.modules.scheduling.UrgencyLevel;

public class MilestoneEdit extends AbstractPageScheduling {
	@Property
	private Project curProj;
	private Schedule schedule;
	private Long scheduleId;
	@Property
	private Milestone milestone;
	private Long milestoneId;
	
	@Inject
	private SchedulingDAO scheduleDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@SuppressWarnings("unused")
	@Inject
	private Logger logger;
	@Inject
	private Messages messages;
	
	@Component
	private Form editForm;
	
	void onActivate(EventContext ec) {
		if(ec.getCount()==1){
			milestoneId = ec.get(Long.class, 0);
		}
		if(ec.getCount()==2){
			scheduleId = ec.get(Long.class, 0);
			milestoneId = ec.get(Long.class, 1);
		}
	}
	Object[] onPassivate() {
		if(scheduleId==null)
			return new Object[] {milestoneId};
		else return new Object[] {scheduleId, milestoneId};
	}
	public boolean isCreateMode(){
		if(milestoneId<=0)
			return true;
		return false;
	}
	
	void setupRender(){
		if(isCreateMode()){
			schedule = scheduleDAO.getScheduleById(scheduleId);
			if(schedule==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "ScheduleID", scheduleId));
			curProj = schedule.getProject();
			if(!canCreateMilestone(curProj)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
		}
		else{
			milestone = scheduleDAO.getMilestoneById(milestoneId);
			if(milestone==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "MilestoneID", milestoneId));
			schedule = milestone.getSchedule();
			curProj = schedule.getProject();
			if(!canEditMilestone(milestone)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
		}
	}
	
	void onPrepareFromEditForm(){ //note: cant just use onPrepareOnSubmit, because we need to set milestone.Manager when render
		if(isCreateMode()){ 
			milestone = new Milestone();
			schedule = scheduleDAO.getScheduleById(scheduleId);
			curProj = schedule.getProject();
			milestone.setSchedule(schedule);
			milestone.setManager(getCurUser());
			milestone.setDeadline(curProj.getEdate());
		}
		else{
			milestone = scheduleDAO.getMilestoneById(milestoneId);
			schedule = milestone.getSchedule();
			curProj = schedule.getProject();
		}
	}
	
	@Component(id="deadline")
	private DateField deadlineField;
	void onValidateFormFromEditForm(){
		if(milestone.getDeadline().before(curProj.getSdate()))
			editForm.recordError(deadlineField, messages.get("deadline-before-startDate-error"));
		
	  	if(curProj.getEdate()!=null && milestone.getDeadline().after(curProj.getEdate()))
			editForm.recordError(deadlineField, messages.get("deadline-after-endDate-error"));
	
	}
	
	@CommitAfter
	Object onSuccessFromEditForm(){
		if(isCreateMode()){
			schedule.addMilestone(milestone);
			milestone.setCreateDate(new Date());
			milestone.setUrgency(UrgencyLevel.MEDIUM);
		}
		milestone.setModifyDate(new Date());
		
		scheduleDAO.saveMilestone(milestone);
		
		if(isCreateMode())
			return linkSource.createPageRenderLinkWithContext(SchedulingIndex.class, curProj.getId());
		else
			return linkSource.createPageRenderLinkWithContext(MilestoneView.class, milestone.getId());
	}
	
	public String getBreadcrumbLink(){
		if(isCreateMode())
			return messages.get("add-new")+" "+messages.get("milestone")+"=modules/scheduling/milestoneedit?"+ scheduleId + ":" +milestoneId;
		else
			return messages.get("milestone")+": "+encode(milestone.getName())+"=modules/scheduling/milestoneview?"+ milestoneId+
				","+ messages.get("edit")+" "+messages.get("milestone")+"=modules/scheduling/milestoneedit?"+ milestoneId;
	}
	public String getPageTitle(){
		if(isCreateMode())
			return messages.get("add-new")+" "+messages.get("milestone");
		else
			return messages.get("edit")+" "+messages.get("milestone");
		
	}
}
