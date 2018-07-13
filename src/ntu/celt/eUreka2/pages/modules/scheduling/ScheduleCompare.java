package ntu.celt.eUreka2.pages.modules.scheduling;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.scheduling.Activity;
import ntu.celt.eUreka2.modules.scheduling.Milestone;
import ntu.celt.eUreka2.modules.scheduling.Phase;
import ntu.celt.eUreka2.modules.scheduling.PrivilegeSchedule;
import ntu.celt.eUreka2.modules.scheduling.Schedule;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.modules.scheduling.Task;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

public class ScheduleCompare extends AbstractPageScheduling {
	@Property
	private Project curProj;
	private Long scheduleId ;
	private Long activeScheduleId ;
	@Property
	private Schedule schedule;
	@Property
	private Schedule activeSchedule;
	@SuppressWarnings("unused")
	@Property
	private Milestone milestone;
	private Activity activity; //getter and setter are define separately
	@SuppressWarnings("unused")
	@Property
	private Phase phase;
	@Property
	private Task task;

	
	@Inject
	private SchedulingDAO scheduleDAO;
	@Inject
	private Logger logger;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		if(ec.getCount()==2){
			activeScheduleId = ec.get(Long.class, 0);
			scheduleId = ec.get(Long.class, 1);
		}
	}
	Object[] onPassivate() {
		return new Object[] {activeScheduleId, scheduleId};
	}
	
	
	void setupRender(){
		activeSchedule = scheduleDAO.getScheduleById(activeScheduleId);
		schedule = scheduleDAO.getScheduleById(scheduleId);
		if(activeSchedule==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ActiveScheduleID", activeScheduleId));
		}
		if(schedule==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ScheduleID", scheduleId));
		}
		
		curProj = activeSchedule.getProject();
		
		if(!curProj.equals(schedule.getProject())){
			throw new NotAuthorizedAccessException(messages.get("cant-compare-from-different-project"));
		}
		if(!canViewScheduling(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		
	}
	@Property
	private Object foundItem;
	@Property
	private String nameField = "name";
	@Property
	private String dateField = "date";
	@Property
	private String percentDoneField = "percentDone";
	
	public String getComparedClassName(Object obj, Schedule schd, String fieldName, boolean sameObjAsPrev, boolean comparingLeftToRight){
		if(!sameObjAsPrev){ 
			foundItem = findItem(obj, schd);
		}
		if(foundItem == null){ //item not found
			if(comparingLeftToRight)
				return "removed";
			else
				return "added";
		}
		if(equalsField(obj, foundItem, fieldName))
			return "same";
		else
			return "changed";
	}
	
	private Object findItem(Object obj, Schedule schd){
		//check by identifier
		//check by createDate since it's never been changed
		
		if(obj instanceof Task){
			Task tObj = (Task) obj;
			for(Milestone m : schd.getMilestones()){
				for(Task t: m.getTasks()){
					if(tObj.getIdentifier().equals(t.getIdentifier())){
						return t;
					}
				}
			}
		}
		else if(obj instanceof Phase){
			Phase pObj = (Phase) obj;
			for(Milestone m : schd.getMilestones()){
				for(Phase p : m.getPhases()){
					if(pObj.getIdentifier().equals(p.getIdentifier())){
						return p;
					}
				}
			}		
		}
		else if(obj instanceof Milestone){
			Milestone mObj = (Milestone) obj;
			for(Milestone m : schd.getMilestones()){
				if(mObj.getIdentifier().equals(m.getIdentifier())){
					return m;
				}
			}
		}
		
		return null;
	}
	/**
	 * note that this method is very dependent to methods defined in Task.java , Phase.java, Milestone.java
	 * @param o1
	 * @param o2
	 * @param fieldName
	 * @return
	 */
	private boolean equalsField(Object o1, Object o2, String fieldName){
		if(o2 instanceof Task && o1 instanceof Task){
			Task t1 = (Task) o1;
			Task t2 = (Task) o2;
			if(fieldName.equals(nameField)){
				if(t1.getName().equals(t2.getName()))
					return true;
			}
			else if(fieldName.equals(dateField)){
				if(t1.getStartDate().equals(t2.getStartDate()) && t1.getEndDate().equals(t2.getEndDate()))
					return true;
			}
			else if(fieldName.equals(percentDoneField)){
				if(t1.getPercentageDone().equals(t2.getPercentageDone()))
					return true;
			}
		}
		else if(o2 instanceof Phase && o1 instanceof Phase){
			Phase p1 = (Phase) o1;
			Phase p2 = (Phase) o2;
			if(fieldName.equals(nameField)){
				if(p1.getName().equals(p2.getName()))
					return true;
			}
			else if(fieldName.equals(dateField)){
				if(p1.getStartDate().equals(p2.getStartDate()) && p1.getEndDate().equals(p2.getEndDate()))
					return true;
			}
		}
		else if(o2 instanceof Milestone && o1 instanceof Milestone){
			Milestone m1 = (Milestone) o1;
			Milestone m2 = (Milestone) o2;
			if(fieldName.equals(nameField)){
				if(m1.getName().equals(m2.getName()))
					return true;
			}
			else if(fieldName.equals(dateField)){
				if(m1.getDeadline().equals(m2.getDeadline()))
					return true;
			}
		}
		else{
			logger.warn("Unsupport Object type to compare");
			throw new RuntimeException("Unsupport Object type to compare");
		}
		return false;
	}
	
	public String getTaskAssignedPersons(){
		String result = "";
		for(User u : task.getAssignedPersons()){
			result += u.getDisplayName() + ", ";
		}
		
		return Util.removeLastSeparator(result, ", ");
	}
	public boolean isInstanceofPhase(Activity activity){
		if(activity instanceof Phase){
			return true;
		}
		return false;
	}
	public boolean isInstanceofTask(Activity activity){
		if(activity instanceof Task){
			return true;
		}
		return false;
	}
	public void setActivity(Activity activity){
		this.activity = activity;
		if(activity instanceof Phase)
			phase = (Phase) activity;
		else if(activity instanceof Task)
			task = (Task) activity;
	}
	public Activity getActivity(){
		return activity;
	}
	
}
