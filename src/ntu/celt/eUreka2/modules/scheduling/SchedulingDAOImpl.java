package ntu.celt.eUreka2.modules.scheduling;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import ntu.celt.eUreka2.data.LastUpdateItem;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.pages.modules.scheduling.MilestoneView;
import ntu.celt.eUreka2.pages.modules.scheduling.PhaseView;
import ntu.celt.eUreka2.pages.modules.scheduling.TaskView;

public class SchedulingDAOImpl implements SchedulingDAO{
	@Inject
    private Session session;
	private PageRenderLinkSource linkSource;
	private Messages messages;
	
	public SchedulingDAOImpl(Session session, PageRenderLinkSource linkSource,
			Messages messages) {
		super();
		this.session = session;
		this.linkSource = linkSource;
		this.messages = messages;
	}

	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_SCHEDULING;
	}
	
	@Override
	public void deleteMilestone(Milestone milestone) {
		milestone.getTasks().clear();
		milestone.getPhases().clear();
		session.delete(milestone);
	}

	@Override
	public void deletePhase(Phase phase) {
		Milestone ms = phase.getMilestone();
		ms.removePhase(phase);
		List<Task> taskList = phase.getTasks();
		for (Task t : taskList) {
			ms.removeTask(t);
		}
		phase.getTasks().clear();
		session.delete(phase);
	}

	@Override
	public void deleteTask(Task task) {
		Milestone mt = task.getMilestone(); 
		if (task.getPhase() != null) {
			Phase ph = task.getPhase();
			ph.removeTask(task);
			session.update(ph);
		}
		mt.removeTask(task);
		session.delete(task);
	}

	@Override
	public Milestone getMilestoneById(Long id) {
		if(id==null) return null;
		return (Milestone) session.get(Milestone.class, id);
	}

	@Override
	public Phase getPhaseById(Long id) {
		if(id==null) return null;
		return (Phase) session.get(Phase.class, id);
	}

	@Override
	public Task getTaskById(Long id) {
		if(id==null) return null;
		return (Task) session.get(Task.class, id);
	}

	@Override
	public void saveMilestone(Milestone milestone) {
		session.persist(milestone);
	}

	@Override
	public void savePhase(Phase phase) {
		session.persist(phase);
	}

	@Override
	public void saveTask(Task task) {
		session.persist(task);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Schedule> getInactiveSchedules(Project project) {
		Criteria crit = session.createCriteria(Schedule.class)
				.add(Restrictions.eq("active", false))
				.add(Restrictions.eq("project.id", project.getId()))
				.addOrder(Order.desc("createDate"))
				;
		return crit.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Schedule> getInactiveSchedules() {
		Criteria crit = session.createCriteria(Schedule.class)
				.add(Restrictions.eq("active", false))
				;
		return crit.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Schedule getActiveSchedule(Project project) {
		Criteria crit = session.createCriteria(Schedule.class)
				.add(Restrictions.eq("active", true))
				.add(Restrictions.eq("project.id", project.getId()))
				;
		List<Schedule> schdList = crit.list();
		if(schdList.isEmpty())
			return null;
		else
			return schdList.get(0);
	}

	@Override
	public Schedule getScheduleById(Long id) {
		if(id==null) return null;
		return (Schedule) session.get(Schedule.class, id);
	}

	@Override
	public void deleteSchedule(Schedule schedule) {
		schedule.getMilestones().clear();
		session.delete(schedule);
	}

	@Override
	public void saveSchedule(Schedule schedule) {
		session.persist(schedule);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Task> getTasksToSendReminder() {
		//task's Schedule is active, 
		//    task not 100% done, 
		//    endDate - now() == is in List of DaysToRemind
		Query q = session.createQuery("SELECT t FROM Task AS t " +
				" LEFT JOIN t.milestone.schedule AS s" +
				" WHERE s.active=true" +
				"	AND t.percentageDone<>100 " );
		
		//HQL has no standard way to obtain Date Different
		//		"   AND (day(t.endDate) - day(current_date())) in elements(t.daysToRemind) "); 
		Date today = new Date();
		List<Task> objList = q.list();
		for(int i = objList.size()-1; i>=0; i--){
			Task t = (Task) objList.get(i);
			if(! t.getDaysToRemind().contains(Util.dateDiff(today, t.getEndDate()))){
				objList.remove(i);
			}
		}
		
		return objList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Task> getUnfinishedTasks(Schedule s, User u) {
		Query q = session.createQuery(
				"FROM Task a1 WHERE a1.id IN ("+
					"SELECT DISTINCT t.id FROM Task AS t " +
					" LEFT OUTER JOIN t.milestone.schedule AS s" +
					" WHERE " +
					"   s.active = true " +
					"   AND s = :rSchdl " +
					"	AND t.percentageDone<>100 " +
					"	AND :rUser in elements(t.assignedPersons) " +
				" )" +
				"   ORDER BY a1.startDate " 
				)
				.setParameter("rSchdl", s)
				.setParameter("rUser", u)
				;

		return q.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_YEAR, -1 * numPastDay);
		
		Query q = session.createQuery("SELECT m FROM Milestone AS m " +
				" WHERE m.schedule.project.id=:rPid " +
				"   AND (m.schedule.active = true) " +
				"   AND m.modifyDate>=:rPastDay " +
				"   ORDER BY modifyDate DESC" +
				" 	 ")
				.setString("rPid", project.getId())
				.setDate("rPastDay", pastDay.getTime())
				;
		List<Milestone> mList = q.list();
		List<LastUpdateItem> lList = new ArrayList<LastUpdateItem>();
		for(Milestone m : mList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(m.getModifyDate());
			l.setTitle(m.getName());
			l.setType(messages.get("milestone"));
			l.setUrl(linkSource.createPageRenderLinkWithContext(MilestoneView.class, m.getId())
					.toAbsoluteURI());
			lList.add(l);
		}
		
		
		q = session.createQuery("SELECT p FROM Phase AS p " +
				" WHERE p.milestone.schedule.project.id=:rPid " +
				"   AND (p.milestone.schedule.active = true) " +
				"   AND p.modifyDate>=:rPastDay " +
				"   ORDER BY modifyDate DESC" +
				" 	 ")
				.setString("rPid", project.getId())
				.setDate("rPastDay", pastDay.getTime())
				;
		List<Phase> pList = q.list();
		for(Phase p : pList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(p.getModifyDate());
			l.setTitle(p.getName());
			l.setType(messages.get("phase"));
			l.setUrl(linkSource.createPageRenderLinkWithContext(PhaseView.class, p.getId())
					.toAbsoluteURI());
			lList.add(l);
		}
		
		q = session.createQuery("SELECT t FROM Task AS t " +
				" WHERE t.milestone.schedule.project.id=:rPid " +
				"   AND (t.milestone.schedule.active = true) " +
				"   AND t.modifyDate>=:rPastDay " +
				"   ORDER BY modifyDate DESC" +
				" 	 ")
				.setString("rPid", project.getId())
				.setDate("rPastDay", pastDay.getTime())
				;
		List<Task> tList = q.list();
		for(Task t : tList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(t.getModifyDate());
			l.setTitle(t.getName());
			l.setType(messages.get("task"));
			l.setUrl(linkSource.createPageRenderLinkWithContext(TaskView.class, t.getId())
					.toAbsoluteURI());
			lList.add(l);
		}
		
		
		Collections.sort(lList);
		
		return lList;
	}

	@Override
	public long countMilestones(Project proj) {
		Query q = session.createQuery("SELECT count(m) FROM Milestone AS m " +
			" WHERE m.schedule.project.id=:rPid " +
			" AND m.schedule.active = true"	)
			.setString("rPid", proj.getId())
			;
		return (Long) q.uniqueResult();
	}

	@Override
	public long countPhases(Project proj) {
		Query q = session.createQuery("SELECT count(p) FROM Phase AS p " +
			" WHERE p.milestone.schedule.project.id=:rPid " +
			" AND p.milestone.schedule.active = true"	)
			.setString("rPid", proj.getId())
			;
		return (Long) q.uniqueResult();
	}

	@Override
	public long countTasks(Project proj) {
		Query q = session.createQuery("SELECT count(t) FROM Task AS t " +
			" WHERE t.milestone.schedule.project.id=:rPid " +
			" AND t.milestone.schedule.active = true"	)
			.setString("rPid", proj.getId())
			;
		return (Long) q.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Milestone> getAllMilestone() {
		return session.createCriteria(Milestone.class).list();
	}

	@Override
	public long countMilestones(Project proj, User creator) {
		Query q = session.createQuery("SELECT count(m) FROM Milestone AS m " +
				" WHERE m.schedule.project.id=:rPid " +
				" AND m.schedule.active = true " +
				" AND m.manager=:rCreator "
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", creator)
				;
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Milestone> getMilestones(Project proj, User creator) {
		Query q = session.createQuery("SELECT m FROM Milestone AS m " +
				" WHERE m.schedule.project.id=:rPid " +
				" AND m.schedule.active = true " +
				" AND m.manager=:rCreator "
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", creator)
				;
		return q.list();
	}
	@Override
	public long countPhases(Project proj, User creator) {
		Query q = session.createQuery("SELECT count(p) FROM Phase AS p " +
				" WHERE p.milestone.schedule.project.id=:rPid " +
				" AND p.milestone.schedule.active = true "	+
				" AND p.manager=:rCreator "
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", creator)
				;
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Phase> getPhases(Project proj, User creator) {
		Query q = session.createQuery("SELECT p FROM Phase AS p " +
				" WHERE p.milestone.schedule.project.id=:rPid " +
				" AND p.milestone.schedule.active = true "	+
				" AND p.manager=:rCreator "
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", creator)
				;
		return q.list();
	}

	@Override
	public long countTasks(Project proj, User creator) {
		Query q = session.createQuery("SELECT count(t) FROM Task AS t " +
				" WHERE t.milestone.schedule.project.id=:rPid " +
				" AND milestone.schedule.active = true "	+
				" AND t.manager=:rCreator "
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", creator)
				;
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Task> getTasks(Project proj, User creator) {
		Query q = session.createQuery("SELECT t FROM Task AS t " +
				" WHERE t.milestone.schedule.project.id=:rPid " +
				" AND milestone.schedule.active = true "	+
				" AND t.manager=:rCreator "
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", creator)
				;
		return q.list();
	}
	@Override
	public long countTasksAssigned(Project proj, User creator) {
		Query q = session.createQuery("SELECT count(t) FROM Task AS t " +
				" WHERE t.milestone.schedule.project.id=:rPid " +
				"  AND milestone.schedule.active = true "	+
				"  AND :rUser in elements(t.assignedPersons) " 
				)
				.setString("rPid", proj.getId())
				.setParameter("rUser", creator)
				;
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Task> getTasksAssigned(Project proj, User creator) {
		Query q = session.createQuery("SELECT t FROM Task AS t " +
				" WHERE t.milestone.schedule.project.id=:rPid " +
				"  AND milestone.schedule.active = true "	+
				"  AND :rUser in elements(t.assignedPersons) " 
				)
				.setString("rPid", proj.getId())
				.setParameter("rUser", creator)
				;
		return q.list();
	}

	

	
}
