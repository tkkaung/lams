package ntu.celt.eUreka2.pages.modules.scheduling;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.DateField;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
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
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

public class TaskEdit extends AbstractPageScheduling {
	@Property
	private Project curProj;
	@SessionState
	private AppState appState;
	private Schedule schedule;
	@Property
	private Milestone milestone;
	private Long milestoneId;
	@Property
	private Phase phase;
	private Long phaseId;
	@Property
	private Task task;
	private Long taskId;
	@Property
	private List<String> assignedPersonIds;
	@SuppressWarnings("unused")
	@Property
	private StringValueEncoder stringEncoder = new StringValueEncoder();
	@Property
	private boolean notifyMember;
	@SuppressWarnings("unused")
	@Property
	private Integer[] availableDaysToRemind = {0, 1, 3, 5, 7};
	@SuppressWarnings("unused")
	@Property
	private Integer tempInt;
	
	@Inject
	private SchedulingDAO scheduleDAO;
	@Inject
	private UserDAO userDAO;
	
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Logger logger;
	@Inject
	private EmailManager emailManager;
	@Inject
	private Request request;
	@Inject
	private Messages messages;
	
	@Component
	private Form editForm;
	
	void onActivate(EventContext ec) {
		if(ec.getCount()==1){
			taskId = ec.get(Long.class, 0);
		}
		if(ec.getCount()==2){
			phaseId = ec.get(Long.class, 0);
			taskId = ec.get(Long.class, 1);
		}
		if(ec.getCount()==3){
			milestoneId = ec.get(Long.class, 0);
			phaseId = ec.get(Long.class, 1);
			taskId = ec.get(Long.class, 2);
		}
	}
	Object[] onPassivate() {
		if(milestoneId==null)
			if(phaseId==null)
				return new Object[] {taskId};
			else
				return new Object[] {phaseId, taskId};
		else return new Object[] {milestoneId, phaseId, taskId};
	}
	public boolean isCreateMode(){
		if(taskId<=0)
			return true;
		return false;
	}
	
	void setupRender(){
		if(isCreateMode()){
			if(phaseId<=0){
				milestone = scheduleDAO.getMilestoneById(milestoneId);
				if(milestone==null)
					throw new RecordNotFoundException(messages.format("entity-not-exists", "MilestoneID", milestoneId));
			}
			else{
				phase = scheduleDAO.getPhaseById(phaseId);
				if(phase==null)
					throw new RecordNotFoundException(messages.format("entity-not-exists", "phaseID", phaseId));
				milestone = phase.getMilestone();
			}
			schedule = milestone.getSchedule();
			curProj = schedule.getProject();
			
			if(!canCreateTask(curProj)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
		}
		else {
			//case: edit
			task = scheduleDAO.getTaskById(taskId);
			if(task==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "TaskID", taskId));
			phase = task.getPhase();
			if(phase==null)
				milestone = task.getMilestone();
			else
				milestone = phase.getMilestone();
			
			if (assignedPersonIds == null)
				assignedPersonIds = task.getAssignedPersonsAsStringIds();
			
			schedule = milestone.getSchedule();
			curProj = schedule.getProject();
			if(!canEditTask(task)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
		}
	}
	
	void onPrepareFromEditForm(){
		
		if(isCreateMode()){ 
			task = new Task();
			if(phaseId<=0){
				milestone = scheduleDAO.getMilestoneById(milestoneId);
				schedule = milestone.getSchedule();
				curProj = schedule.getProject();
				
				task.setMilestone(milestone);
				task.setStartDate(curProj.getSdate());
				task.setEndDate(milestone.getDeadline());
				task.setReminderType(ReminderType.ASSIGNED_MEMBERS_ONLY);
			}
			else{
				phase = scheduleDAO.getPhaseById(phaseId);
				milestone = phase.getMilestone();
				schedule = milestone.getSchedule();
				curProj = schedule.getProject();
				
				task.setPhase(phase);
				task.setMilestone(milestone);
				task.setStartDate(phase.getStartDate());
				task.setEndDate(phase.getEndDate());
				task.setReminderType(ReminderType.ASSIGNED_MEMBERS_ONLY);
			}
			task.setManager(getCurUser());
		}
		else{ 
			task = scheduleDAO.getTaskById(taskId);
			phase = task.getPhase();
			if(phase==null)
				milestone = task.getMilestone();
			else
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
		if(task.getStartDate().after(task.getEndDate())){
			editForm.recordError(endDateField, messages.get("endDate-must-be-after-startDate"));
		}
		if(phase==null){
			if(task.getStartDate().before(curProj.getSdate()) || task.getStartDate().after(milestone.getDeadline()))
				editForm.recordError(startDateField, messages.get("sDate-before-projSDate-nor-after-mileDeadline-error"));
			if(task.getEndDate().before(curProj.getSdate()) || task.getEndDate().after(milestone.getDeadline()))
				editForm.recordError(endDateField, messages.get("eDate-before-projSDate-nor-after-mileDeadline-error"));
		}
		else{
			if(task.getStartDate().before(phase.getStartDate()) || task.getStartDate().after(phase.getEndDate()))
				editForm.recordError(startDateField, messages.get("sDate-outside-phase-sdate-edate-error"));
			if(task.getEndDate().before(phase.getStartDate()) || task.getEndDate().after(phase.getEndDate()))
				editForm.recordError(endDateField, messages.get("eDate-outside-phase-sdate-edate-error"));
		}
	}
	
	@CommitAfter
	Object onSuccessFromEditForm(){
		if(isCreateMode()){
			if(phase==null){
				milestone.addTask(task);
			}
			else{
				phase.addTask(task);
				milestone.addTask(task);
			}
			task.setCreateDate(new Date());
			task.setPercentageDone(0);
		}
		else{
			task.setEditor(getCurUser());
			if(task.getPercentageDone()==100)
				task.setCompleteDate(new Date());
		}
		task.setModifyDate(new Date());
		
		Set<User> assignedUsers = new HashSet<User>();
		for (String uId : assignedPersonIds) {
			User user = userDAO.getUserById(Integer.parseInt(uId));
			assignedUsers.add(user);
		}
		task.setAssignedPersons(assignedUsers);
		
		//set reminder
		task.getDaysToRemind().clear();
		String[] daysToRemind = request.getParameters("dayChkBox");
		if(daysToRemind !=null){
			for(String _day : daysToRemind){
				task.addDaysToRemind(Integer.parseInt(_day));
			}
		}
		
		scheduleDAO.saveTask(task);
	
		//sent email to members
		if(notifyMember){
			sendEmailToMembers();
		}
		if(isCreateMode())
			return linkSource.createPageRenderLinkWithContext(SchedulingIndex.class, curProj.getId());
		else
			return linkSource.createPageRenderLinkWithContext(TaskView.class, task.getId());
	}
	
	private void sendEmailToMembers(){
		EmailTemplateVariables var = new EmailTemplateVariables(
				task.getCreateDate().toString(), task.getModifyDate().toString(),
				task.getName(), 
				Util.truncateString(Util.stripTags(task.getComment()), Config.getInt("max_content_lenght_in_email")), 
				getCurUser().getDisplayName(),
				curProj.getDisplayName(), 
				emailManager.createLinkBackURL(request, TaskView.class, task.getId()),
				task.getPercentageDone().toString(), task.getUrgency().toString());
		try{
			if(isCreateMode()){
				emailManager.sendHTMLMail(curProj.getUsers(), EmailTemplateConstants.SCHEDULING_TASK_ADDED, var);
			}
			else {
				emailManager.sendHTMLMail(curProj.getUsers(), EmailTemplateConstants.SCHEDULING_TASK_EDITED, var);
			}
			appState.recordInfoMsg(messages.get("notification-sent-to-members"));
		}
		catch(SendEmailFailException e){
			logger.warn(e.getMessage(), e);
			appState.recordWarningMsg(e.getMessage());
		}
	}
	public SelectModel getAssignedPersonModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<User> users = curProj.getUsers();
		//and also add users that was previous assigned
		if(assignedPersonIds!=null){
			for(String uId: assignedPersonIds){
				User u = userDAO.getUserById(Integer.parseInt(uId));
				if(!users.contains(u))
					users.add(u);
			}
		}
		
		for (User u : users) {
			OptionModel optModel = new OptionModelImpl(u.getDisplayName(), Integer.toString(u.getId()));
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
	public String getBreadcrumbLink(){
		String str = "";
		if(phase!=null){
			str = messages.get("phase")+": "+Util.encode(phase.getName()) +"=modules/scheduling/phaseview?"+phase.getId()+",";
		}
		if(isCreateMode()){
			if(phase!=null)
				str += messages.get("add-new")+" "+messages.get("task")+"=modules/scheduling/taskedit?"+ phaseId + ":" +taskId;
			else
				str += messages.get("add-new")+" "+messages.get("task")+"=modules/scheduling/taskedit?"+ milestoneId + ":"+ phaseId + ":" +taskId;
		}
		else{
			str += messages.get("task")+": "+encode(task.getName())+"=modules/scheduling/taskview?"+taskId+
				", "+ messages.get("edit")+" "+messages.get("task")+"=modules/scheduling/taskedit?"+ taskId;
		}
		return str;
	}
	public String getPageTitle(){
		if(isCreateMode())
			return messages.get("add-new")+" "+messages.get("task");
		else
			return messages.get("edit")+" "+messages.get("task");
	}
}
