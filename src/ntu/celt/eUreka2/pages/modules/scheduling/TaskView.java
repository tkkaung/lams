package ntu.celt.eUreka2.pages.modules.scheduling;


import java.util.Calendar;
import java.util.Date;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.scheduling.Milestone;
import ntu.celt.eUreka2.modules.scheduling.Phase;
import ntu.celt.eUreka2.modules.scheduling.PrivilegeSchedule;
import ntu.celt.eUreka2.modules.scheduling.ReminderType;
import ntu.celt.eUreka2.modules.scheduling.Schedule;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.modules.scheduling.Task;

public class TaskView extends AbstractPageScheduling {
	@Property
	private Project curProj;
	private Schedule schedule;
	@Property
	private Milestone milestone;
	@Property
	private Phase phase;
	@Property
	private Task task;
	private Long taskId;
	@SuppressWarnings("unused")
	@Property
	private User tempUser;
	@Property
	private int tempIndex;
	@SuppressWarnings("unused")
	@Property
	private int tempNumDay;
	
	@Inject
	private SchedulingDAO scheduleDAO;
	@Inject
	private Messages messages;
	
	
	void onActivate(EventContext ec) {
		taskId = ec.get(Long.class, 0);
	}
	Object[] onPassivate() {
		return new Object[] {taskId};
	}
	
	public String getBreadcrumbLink(){
		String str = "";
		if(phase!=null){
			str = messages.get("phase")+": "+Util.encode(phase.getName()) +"=modules/scheduling/phaseview?"+phase.getId()+",";
		}
		str += messages.get("view") + " "+messages.get("task")+"=modules/scheduling/taskview?"+taskId;
		return str;
	}
	
	
	void setupRender(){
			task = scheduleDAO.getTaskById(taskId);
			if(task==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "TaskID", taskId));
			phase = task.getPhase();
			if(phase==null)
				milestone = task.getMilestone();
			else
				milestone = phase.getMilestone();
			
			schedule = milestone.getSchedule();
			curProj = schedule.getProject();
			if(!canViewScheduling(curProj)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
		}
	
	
	public boolean isFirst(){
		if(tempIndex==0)
			return true;
		return false;
	}
	public String getReminderTypeLabel(ReminderType rt){
		switch(rt){
			case ALL_PROJECT_MEMBERS: return messages.get("ALL_PROJECT_MEMBERS-label");
			case ASSIGNED_MEMBERS_ONLY: return messages.get("ASSIGNED_MEMBERS_ONLY-label");
		}
		return null;
	}
	public String getDateDisplay(Date endDate, int numDate){
		Calendar c = Calendar.getInstance();
		c.setTime(endDate);
		c.add(Calendar.DAY_OF_YEAR, -1 * numDate);
		
		return Util.formatDateTime(c.getTime(), Config.getString(Config.FORMAT_DATE_PATTERN));
	}
}
