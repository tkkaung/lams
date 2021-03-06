package ntu.celt.eUreka2.pages.modules.scheduling;

import org.slf4j.Logger;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.scheduling.Milestone;
import ntu.celt.eUreka2.modules.scheduling.Phase;
import ntu.celt.eUreka2.modules.scheduling.PrivilegeSchedule;
import ntu.celt.eUreka2.modules.scheduling.Schedule;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.modules.scheduling.Task;

public class PhaseView extends AbstractPageScheduling {
	@Property
	private Project curProj;
	private Schedule schedule;
	@Property
	private Milestone milestone;
	@Property
	private Phase phase;
	private Long phaseId;
	@SuppressWarnings("unused")
	@Property
	private Task tempTask;
	
	@Inject
	private SchedulingDAO scheduleDAO;
	@SuppressWarnings("unused")
	@Inject
	private Logger logger;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		phaseId = ec.get(Long.class, 0);
	}
	Object[] onPassivate() {
		return new Object[] {phaseId};
	}
	
	void setupRender(){
		phase = scheduleDAO.getPhaseById(phaseId);
		if(phase==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "phaseID", phaseId));
		milestone = phase.getMilestone();
		schedule = milestone.getSchedule();
		curProj = schedule.getProject();
		if(!canViewScheduling(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
	}
	
	public Object[] getPhaseIdAndTaskId(){
		return new Object[]{phase.getId() , 0};
	}

}
