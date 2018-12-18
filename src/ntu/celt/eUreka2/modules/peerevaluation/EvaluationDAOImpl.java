package ntu.celt.eUreka2.modules.peerevaluation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.pages.modules.peerevaluation.Manage;

public class EvaluationDAOImpl implements EvaluationDAO {
	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_PEER_EVALUATION;
	}
	@Inject
    private Session session;
	private PageRenderLinkSource linkSource;
	
	
	public EvaluationDAOImpl(Session session, PageRenderLinkSource linkSource) {
		super();
		this.session = session;
		this.linkSource = linkSource;
	}

	@Override
	public void addEvaluation(Evaluation eval) {
		session.save(eval);
	}
	@Override
	public void updateEvaluation(Evaluation eval) {
		session.merge(eval);
	}
	@Override
	public void deleteEvaluation(Evaluation eval) {
		eval.getEvalUsers().clear();
		eval.getCriterias().clear();
		session.delete(eval);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Evaluation> getEvaluationsByProject(Project p) {
		Criteria crit = session.createCriteria(Evaluation.class)
					.add(Restrictions.eq("project.id", p.getId()))
					.addOrder(Order.asc("orderNumber"))
					;
				
		return crit.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Evaluation> getVisibleEvaluations(Project p) {
		Query q = session.createQuery("SELECT a FROM Evaluation AS a" 
				+ " WHERE project.id = :rPid "
				+ " AND (a.sdate IS NULL OR a.sdate <= :rToday) "
				+ " ORDER BY orderNumber ASC"
				)
				.setString("rPid", p.getId())
				.setDate("rToday", new Date());
		return q.list();
	}
	
	
	@Override
	public Evaluation getEvaluationById(long id) {
		return (Evaluation) session.get(Evaluation.class, id);
	}
	@Override
	public EvaluationCriterion getEvaluationCriterionById(long id) {
		return (EvaluationCriterion) session.get(EvaluationCriterion.class, id);
	}
	
	
	

	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_YEAR, -1 * numPastDay);
		
		Query q = session.createQuery("SELECT e FROM Evaluation AS e " +
				" WHERE e.project.id=:rPid " +
				"   AND e.mdate>=:rPastDay " +
				"   ORDER BY mdate DESC" +
				" 	 ")
				.setString("rPid", project.getId())
				.setDate("rPastDay", pastDay.getTime())
				;
		
		List<Evaluation> eList = q.list();
		List<LastUpdateItem> lList = new ArrayList<LastUpdateItem>();
		for(Evaluation e : eList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(e.getMdate());
			l.setTitle(e.getName());
			l.setUrl(linkSource.createPageRenderLinkWithContext(Manage.class
					, e.getProject().getId()
					)
					.toAbsoluteURI());
			
			lList.add(l);
		}
		
		return lList;
	}

	@Override
	public int getNextOrderNumber(Project project) {
		Query q = session.createQuery("SELECT MAX(orderNumber) FROM Evaluation " +
				" WHERE project.id = :rPid ")
				.setString("rPid", project.getId());
		if(q.uniqueResult()==null)
			return 1;
		
		int num = (Integer) q.uniqueResult();
		
		return num+1;
	}

	@Override
	public void reorderEvaluationNumber(Project p) {
		List<Evaluation> evalList = this.getEvaluationsByProject(p);
		int i = 1;
		for(Evaluation eval : evalList){
			eval.setOrderNumber(i);
			i++;
			this.updateEvaluation(eval);
		}
	}

	@Override
	public long countEvaluationByRubric(Rubric rubric) {
		Query q =  session.createQuery("SELECT COUNT(e.id) FROM Evaluation AS e " +
				" WHERE e.rubric=:rRubric " )
				.setParameter("rRubric", rubric);
		
		return  (Long) q.uniqueResult();
	}
	@Override
	public long countEvaluationByProject(Project proj) {
		Query q =  session.createQuery("SELECT COUNT(e.id) FROM Evaluation AS e " +
				" WHERE e.project=:rProject " )
				.setParameter("rProject", proj);
		
		return  (Long) q.uniqueResult();
	}
	
	

	@Override
	public long countEvaluationsByProject(Project proj) {
		int count = getEvaluationsByProject(proj).size();
		return count;
	}

	@Override
	public void saveEvaluationCriterion(EvaluationCriterion acrion) {
		session.save(acrion);
	}

	@Override
	public void saveEvaluationUser(EvaluationUser evalUser) {
		session.save(evalUser);
	}

	public List<Evaluation> getOldEvaluations(String dateStr, int firtResult, int maxResult) {
		
		Query q =  session.createQuery("SELECT a FROM Evaluation AS a " 
				 + " WHERE mdate < :rMdate ")
				 .setString("rMdate", dateStr)
				 .setFirstResult(firtResult)
				.setMaxResults(maxResult)
				 ;
		
		return q.list();
	}
	
	public List<Evaluation> searchEvaluation(String searchName, School searchSchool, String searchTerm, String searchCourseCode) {
		return searchEvaluation(searchName, searchSchool, searchTerm, searchCourseCode, null);
	}
	@Override
	public List<Evaluation> searchEvaluation(String searchName, School searchSchool, String searchTerm, String searchCourseCode, Integer searchRubricID) {
		return searchEvaluation(searchName, searchSchool, searchTerm, searchCourseCode, searchRubricID, null);
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Evaluation> searchEvaluation(String searchName, School searchSchool, String searchTerm, String searchCourseCode, Integer searchRubricID, String projName) {
		if(searchName==null)
			searchName = "";
		Query q =  session.createQuery("SELECT a FROM Evaluation AS a " 
				+ " WHERE a.project IN (SELECT p FROM Project p " 
				+		(projName!=null ? " WHERE p.name LIKE :rProjName" : "")
				+		") AND a.name LIKE :rSearchTxt "
				+ (searchSchool!=null ? " AND a.project.school = :rSchool" : "")
				+ ((searchTerm!=null)? " AND a.project.term=:rTerm" : "")
				+ ((searchCourseCode!=null)? " AND a.project.courseCode=:rCourseCode" : "")
				+ ((searchRubricID!=null)? " AND (a.rubric.id=:rRubricID )   " : "")
				+ " ORDER BY a.project.id ASC , a.orderNumber ASC "
				)
				.setString("rSearchTxt", "%"+searchName+"%")
				;
		if(projName!=null)
			q.setString("rProjName", "%"+projName+"%");
		if (searchSchool!=null){
			q.setParameter("rSchool", searchSchool);
		}
		if (searchTerm!=null){
			q.setParameter("rTerm", searchTerm);
		}
		if (searchCourseCode!=null){
			q.setParameter("rCourseCode", searchCourseCode);
		}
		if (searchRubricID!=null){
			q.setParameter("rRubricID", searchRubricID);
		}

		return q.list();
	}
	

	@Override
	public EvaluationUser getEvaluationUserById(long id) {
		return (EvaluationUser) session.get(EvaluationUser.class, id);
	}

	@Override
	public List<Evaluation> getEvalsToSendReminder() {
		Date today = new Date();
		Calendar next8days = Calendar.getInstance();
		next8days.add(Calendar.DAY_OF_YEAR, 8);
		Query q = session.createQuery("SELECT e FROM Evaluation AS e " +
				" WHERE e.edate >= :today AND e.edate <= :next8days "  )
				.setDate("next8days", next8days.getTime())
				.setDate("today", today);
		
		
		//HQL has no standard way to obtain Date Different
		//		"   AND (day(t.endDate) - day(current_date())) in elements(t.daysToRemind) "); 
		List<Evaluation> objList = q.list();
		for(int i = objList.size()-1; i>=0; i--){
			Evaluation e = (Evaluation) objList.get(i);
			if(e.getEdate()==null){
				objList.remove(i);
			}else{
				if(! e.getDaysToRemind().contains(Util.dateDiff(today, e.getEdate()))){
					objList.remove(i);
				}
			}
		}
		
		return objList;

	}
	@Override
	public List<Evaluation> getEvalsToSendReminderInstructor() {
		Date today = new Date();
		Calendar next8days = Calendar.getInstance();
		next8days.add(Calendar.DAY_OF_YEAR, 8);
		Query q = session.createQuery("SELECT e FROM Evaluation AS e " +
				" WHERE e.edate >= :today AND e.edate <= :next8days "  )
				.setDate("next8days", next8days.getTime())
				.setDate("today", today);
		
		
		//HQL has no standard way to obtain Date Different
		//		"   AND (day(t.endDate) - day(current_date())) in elements(t.daysToRemind) "); 
		List<Evaluation> objList = q.list();
		for(int i = objList.size()-1; i>=0; i--){
			Evaluation e = (Evaluation) objList.get(i);
			if(e.getEdate()==null){
				objList.remove(i);
			}else{
				if(! e.getDaysToRemindInstructor().contains(Util.dateDiff(today, e.getEdate()))){
					objList.remove(i);
				}
			}
		}
		
		return objList;

	}

	@Override
	public List<Evaluation> getEvalsToSendLaunchReminder() {
		Date today = new Date();
		Query q = session.createQuery("SELECT e FROM Evaluation AS e " +
				" WHERE e.sdate = :today  "  )
				.setDate("today", today);
		
		
		List<Evaluation> objList = q.list();
		
		return objList;
	}
	
	
	public boolean isGroupBeingUse(Group group){
		Query q = session.createQuery("SELECT e FROM Evaluation AS e " +
				" WHERE e.group = :group "  )
				.setParameter("group", group);
		List<Evaluation> eList = q.list();
		if(eList.size()==0)
			return false;
		/*for (Evaluation e : eList){
			if(e.getEvalUsers().size() > 0) 
				return true;
		}
		return false;
		*/
		
		return true;
	}

	@Override
	public void deleteEvaluationUsersByUserProject(User user, Project proj, Group group) {
		Query q = session.createQuery("DELETE FROM EvaluationUser AS eu " +
				" WHERE eu.evaluation IN (SELECT e FROM Evaluation AS e WHERE e.project = :proj AND e.group = :group) AND (eu.assessor = :user OR eu.assessee = :user ) "  )
				.setParameter("proj", proj)
				.setParameter("group", group)
				.setParameter("user", user)
				;
		q.executeUpdate();
	}

	
}
