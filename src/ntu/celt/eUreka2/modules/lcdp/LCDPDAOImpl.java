package ntu.celt.eUreka2.modules.lcdp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tapestry5.ioc.annotations.Inject;
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
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurvey;
import ntu.celt.eUreka2.modules.ipsp.IQuestionSet;

public class LCDPDAOImpl implements LCDPDAO {
	
	@Inject
	private Session session;
	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_LEADERSHIP_COMPETENCY;
	}
	
	@Override
	public void addPQuestionSet(PQuestionSet r) {
		session.save(r);
	}
	@Override
	public void updatePQuestionSet(PQuestionSet r) {
		session.merge(r);
	}
	
	
	
	@Override
	public void deletePQuestionSet(PQuestionSet r) {
		session.delete(r);
	}
	@Override
	public void immediateAddPQuestionSet(PQuestionSet r) {
		session.save(r);
	}

	@Override
	public PQuestionSet getPQuestionSetById(long id) {
		return (PQuestionSet) session.get(PQuestionSet.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PQuestionSet> getPQuestionSetsByOwner(User u) {
		Query q = session.createQuery("SELECT r FROM PQuestionSet AS r WHERE r.owner = :rUser "
				+ " ORDER BY r.name ASC ")
				.setParameter("rUser", u);
		return q.list();
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<PQuestionSet> searchPQuestionSets(String searchText, User owner, School school) {
		Criteria crit = session.createCriteria(PQuestionSet.class);
		
		if(searchText != null){
			String[] words = searchText.split(" ");
			for(String word : words){
				crit = crit.add(Restrictions.or(Restrictions.like("name", "%"+word+"%"),
						Restrictions.like("des", "%"+word+"%" )));
			}
		}
		if(owner!=null){
			crit = crit.add(Restrictions.eq("owner", owner));
		}
		
		if(school!=null){
			crit = crit.add(Restrictions.eq("school", school));
		}
		
		crit.addOrder(Order.desc("mdate"));
		
		
		return crit.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PQuestionSet> searchPQuestionSets(Project proj) {
		Set<PQuestionSet> rSet = new HashSet<PQuestionSet>();
		Query q = session.createQuery("SELECT a FROM LCDPSurvey AS a " 
				+" WHERE a.project=:rProject "
				)
				.setParameter("rProject", proj);

		List<LCDPSurvey> aList = q.list();
		for( LCDPSurvey ipsp : aList){
			if(ipsp.getQuestionSet()!= null)
				rSet.add(ipsp.getQuestionSet());
		}
		
		return new ArrayList<PQuestionSet>(rSet);
	}
	
	@Override
	public void addLCDPSurvey(LCDPSurvey lcdp) {
		session.save(lcdp);
	}
	@Override
	public void updateLCDPSurvey(LCDPSurvey lcdp) {
		session.merge(lcdp);
	}
	@Override
	public void deleteLCDPSurvey(LCDPSurvey lcdp) {
		lcdp.getLcdpUsers().clear();
		lcdp.getQuestions().clear();
		session.delete(lcdp);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LCDPSurvey> getLCDPSurveysByProject(Project p) {
		Criteria crit = session.createCriteria(LCDPSurvey.class)
					.add(Restrictions.eq("project.id", p.getId()))
					.addOrder(Order.asc("orderNumber"))
					;
				
		return crit.list();
	}
	
	@Override
	public PQuestion getPQuestionById(long id) {
		return (PQuestion) session.get(PQuestion.class, id);
	}
	
	
	
	@Override
	public LCDPSurvey getLCDPSurveyById(long id) {
		return (LCDPSurvey) session.get(LCDPSurvey.class, id);
	}
	@Override
	public LCDPQuestion getLCDPQuestionById(long id) {
		return (LCDPQuestion) session.get(LCDPQuestion.class, id);
	}
	
	@Override
	public LCDPSurveyUser getLCDPSurveyUserById(long id) {
		return (LCDPSurveyUser) session.get(LCDPSurveyUser.class, id);
	}
	

	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		//Decided no need to have this
		return null;
	}

	@Override
	public List<LCDPSurveyUser> getLCDPSurveyUsersByProject(Project proj) {
		
		Query q = session.createQuery("SELECT pu FROM LCDPSurveyUser as pu WHERE pu.survey.project=:rProj" +
				" ORDER BY assessee.username ASC, assessor.username ASC ")
				.setParameter("rProj", proj)
				;
	
		return q.list();
	}


	@Override
	public long countLCDPSurveyByPQuestionSet(PQuestionSet qset) {
		Query q =  session.createQuery("SELECT COUNT(a.id) FROM LCDPSurvey AS a " +
				" WHERE a.questionSet=:rQuestionSet " )
				.setParameter("rQuestionSet", qset);
		
		return  (Long) q.uniqueResult();
	}
	
	@Override
	public List getLCDPSurveysByPQuestionSet(PQuestionSet qset) {
		Query q =  session.createQuery("SELECT a FROM LCDPSurvey AS a " +
				" WHERE a.questionSet=:rQuestionSet " )
				.setParameter("rQuestionSet", qset);
		
		return  q.list();
	}

	


	@Override
	public void saveLCDPSurveyUser(LCDPSurveyUser lcdpUser) {
		session.save(lcdpUser);
	}

	
	@Override
	public List<LCDPSurvey> searchLCDPSurvey(String searchName, School searchSchool, String searchTerm, String searchCourseCode) {
		if(searchName==null)
			searchName = "";
		
		Query q =  session.createQuery("SELECT a FROM LCDPSurvey AS a " 
				+ " WHERE a.name LIKE :rSearchTxt "
				+ (searchSchool!=null ? " AND a.project.school = :rSchool" : "")
				+ ((searchTerm!=null)? " AND a.project.term=:rTerm" : "")
				+ ((searchCourseCode!=null)? " AND a.project.courseCode=:rCourseCode" : "")
				+ " ORDER BY a.project.id ASC , a.orderNumber ASC "
				)
				.setString("rSearchTxt", "%"+searchName+"%")
				;
		if (searchSchool!=null){
			q.setParameter("rSchool", searchSchool);
		}
				 
		if (searchTerm!=null){
			q.setParameter("rTerm", searchTerm);
		}
		if (searchCourseCode!=null){
			q.setParameter("rCourseCode", searchCourseCode);
		}
		
		
		return q.list();
	}
	
	@Override
	public List<LCDPSurveyUser> searchLCDPSurveyUser(LCDPSurvey lcdp, User Assessor, User Assessee,
			Boolean finished) {
		
		Query q =  session.createQuery("SELECT a FROM LCDPSurveyUser AS a " 
				+ " WHERE 1=1 "
				+ ((lcdp!=null)? " AND a.survey= :rLCDPSurvey " : "")
				+ ((Assessor!=null) ? " AND a.assessor = :rAssessor" : "")
				+ (Assessee!=null? " AND a.assessee=:rAssessee" : "")
				+ ((finished!=null)? " AND a.finished=:rFinished " : "")
				+ "  "
				)
				;
		if (lcdp!=null){
			q.setParameter("rLCDPSurvey", lcdp);
		}else{
			return new ArrayList<LCDPSurveyUser>();
		}
		if (Assessor!=null){
			q.setParameter("rAssessor", Assessor);
		}
				 
		if (Assessee!=null){
			q.setParameter("rAssessee", Assessee);
		}
		if (finished!=null){
			q.setParameter("rFinished", finished);
		}
		
		
		return q.list();
	}
	@Override
	public double getAverageScoreByDimension(LCDPSurveyUser lcdpUser, Integer PDimensionID){
		if(lcdpUser==null)
			return -1;
		Query q =  session.createQuery("SELECT avg(s.score) FROM PQuestionScore AS s " 
				+ " WHERE s.lcdpUser=:rLcdpUser AND s.lcdpUser.finished= true  "
				+ (PDimensionID == null ? "": 
					(PDimensionID==1 ? " AND s.question.dimLeadership=1" :
						(PDimensionID==2 ? " AND s.question.dimManagement=1" :
							(PDimensionID==3 ? " AND s.question.dimCommand=1" :
							""))))
				)
				;
		q.setParameter("rLcdpUser", lcdpUser);
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	@Override
	public long getCountScoreByDimension(LCDPSurveyUser lcdpUser, Integer PDimensionID){
		if(lcdpUser==null)
			return -1;
		Query q =  session.createQuery("SELECT count(score) FROM PQuestionScore AS s " 
				+ " WHERE s.lcdpUser=:rlcdpUser AND s.lcdpUser.finished= true  "
				+ (PDimensionID == null ? "": 
					(PDimensionID==1 ? " AND s.question.dimLeadership=1" :
						(PDimensionID==2 ? " AND s.question.dimManagement=1" :
							(PDimensionID==3 ? " AND s.question.dimCommand=1" :
							""))))
				)
				;
		q.setParameter("rlcdpUser", lcdpUser);
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Long) r;
		
	}
	@Override
	public double getSTDEVScoreByDimension(LCDPSurveyUser lcdpUser, Integer PDimensionID){
		if(lcdpUser==null)
			return -1;
		Query q =  session.createQuery("SELECT sqrt((sum(score * score)/count(score)) - (avg(score) * avg(score))) FROM PQuestionScore AS s " 
				+ " WHERE s.lcdpUser=:rlcdpUser AND s.lcdpUser.finished= true "
				+ (PDimensionID == null ? "": 
					(PDimensionID==1 ? " AND s.question.dimLeadership=1" :
						(PDimensionID==2 ? " AND s.question.dimManagement=1" :
							(PDimensionID==3 ? " AND s.question.dimCommand=1" :
							""))))
				)
				;
		q.setParameter("rlcdpUser", lcdpUser);
		
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	@Override
	public double getAverageScoreByDimensionStyleGroup(LCDPSurveyUser lcdpUser, String StyleGroup){
		if(lcdpUser==null)
			return -1;
		Query q =  session.createQuery("SELECT avg(s.score) FROM PQuestionScore AS s " 
				+ " WHERE s.lcdpUser=:rlcdpUser AND s.lcdpUser.finished= true  "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("rlcdpUser", lcdpUser);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	@Override
	public long getCountScoreByDimensionStyleGroup(LCDPSurveyUser lcdpUser, String StyleGroup){
		if(lcdpUser==null)
			return -1;
		Query q =  session.createQuery("SELECT count(score) FROM PQuestionScore AS s " 
				+ " WHERE s.lcdpUser=:rlcdpUser AND s.lcdpUser.finished= true  "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("rlcdpUser", lcdpUser);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Long) r;
		
	}
	@Override
	public double getSTDEVScoreByDimensionStyleGroup(LCDPSurveyUser lcdpUser, String StyleGroup){
		if(lcdpUser==null)
			return -1;
		Query q =  session.createQuery("SELECT sqrt((sum(score * score)/count(score)) - (avg(score) * avg(score))) FROM PQuestionScore AS s " 
				+ " WHERE s.lcdpUser=:rlcdpUser AND s.lcdpUser.finished= true "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("rlcdpUser", lcdpUser);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	
	
	@Override
	public double getAverageNormsByDimension(Project proj, Integer PDimensionID){
		String sql = "SELECT avg(s.score) FROM PQuestionScore AS s " 
			+ " WHERE s.lcdpUser.finished= true "
			+ (proj == null ? "": " AND s.lcdpUser.survey.project=:rProj")
			+ (PDimensionID == null ? "": 
					(PDimensionID==1 ? " AND s.question.dimLeadership=1" :
						(PDimensionID==2 ? " AND s.question.dimManagement=1" :
							(PDimensionID==3 ? " AND s.question.dimCommand=1" :
							""))));
		Query q =  session.createQuery(sql)	;
		//System.err.println("..........sql="+ sql + "..PDimensionID="+PDimensionID);
		if(proj != null)
			q.setParameter("rProj", proj);
		
		Object r = q.uniqueResult();
		if(r==null)
			return 0;
		return (Double) r;
		
	}
	
	@Override
	public double getSTDEVNormsByDimension(Project proj, Integer PDimensionID){
		Query q =  session.createQuery("SELECT sqrt((sum(score * score)/count(score)) - (avg(score) * avg(score))) FROM PQuestionScore AS s " 
				+ " WHERE s.lcdpUser.finished= true "
				+ (proj == null ? "": " AND s.lcdpUser.survey.project=:rProj")
				+ (PDimensionID == null ? "": 
					(PDimensionID==1 ? " AND s.question.dimLeadership=1" :
						(PDimensionID==2 ? " AND s.question.dimManagement=1" :
							(PDimensionID==3 ? " AND s.question.dimCommand=1" :
							""))))
				)
				;
		if(proj != null)
			q.setParameter("rProj", proj);
		
		Object r = q.uniqueResult();
		if(r==null)
			return 0;
		return (Double) r;
		
	}
	
	@Override
	public double getAverageNormsByDimensionStyleGroup(Project proj, String StyleGroup){
		
		Query q =  session.createQuery("SELECT avg(s.score) FROM PQuestionScore AS s " 
				+ " WHERE s.lcdpUser.finished= true "
				+ (proj == null ? "": " AND s.lcdpUser.survey.project=:rProj")
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		if(proj != null)
			q.setParameter("rProj", proj);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return 0;
		return (Double) r;
		
	}
	
	@Override
	public double getSTDEVNormsByDimensionStyleGroup(Project proj, String StyleGroup){
		Query q =  session.createQuery("SELECT sqrt((sum(score * score)/count(score)) - (avg(score) * avg(score))) FROM PQuestionScore AS s " 
				+ " WHERE s.lcdpUser.finished= true "
				+ (proj == null ? "": " AND s.lcdpUser.survey.project=:rProj")
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		if(proj != null)
			q.setParameter("rProj", proj);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return 0;
		return (Double) r;
		
	}
	
	@Override
	public List<PQuestionScore> getScoresByDimension(LCDPSurveyUser lcdpUser, Integer PDimensionID){
		if(lcdpUser==null)
			return new ArrayList<PQuestionScore>();
		Query q =  session.createQuery("SELECT s.score FROM PQuestionScore AS s " 
				+ " WHERE s.lcdpUser=:rlcdpUser "
				+ (PDimensionID == null ? "": 
					(PDimensionID==1 ? " AND s.question.dimLeadership=1" :
						(PDimensionID==2 ? " AND s.question.dimManagement=1" :
							(PDimensionID==3 ? " AND s.question.dimCommand=1" :
							""))))
				)
				;
		q.setParameter("rlcdpUser", lcdpUser);
		return q.list();
	}
	

	
	@Override
	public boolean isGroupBeingUse(Group group){
		Query q = session.createQuery("SELECT COUNT(a.id) FROM LCDPSurvey AS a " +
				" WHERE a.group = :group "  )
				.setParameter("group", group);
		long size = (Long) q.uniqueResult();
		if(size==0)
			return false;
		
		return true;
	}

	

	@Override
	public List<LCDPSurveyUser> getExpiredLCDPSurveyUsers() {
		Calendar expiredTime = Calendar.getInstance();
		expiredTime.add(Calendar.MINUTE, (-1)* LCDPSurveyUser.VALID_MINUTE_PER_QUESTION);
		
		Query q = session.createQuery("SELECT pu FROM LCDPSurveyUser pu  " 
				+ " WHERE pu.lastAssessTime < :expireTime "
				+ " AND NOT(pu.finished = true) "
				+ " AND pu.lastQuestionNum > 0"
				)
				.setTimestamp("expireTime", expiredTime.getTime());
		return q.list();
	}

	@Override
	public List<User> getNotSubmitedUsersByLCDPSurvey(LCDPSurvey lcdp) {
		Query q = session.createQuery("SELECT pu.user FROM ProjUser pu  " 
				+ " WHERE pu.project = :rProject "
				+ " AND pu.user NOT IN (SELECT pfu.assessor FROM LCDPSurveyUser pfu" +
						" WHERE pfu.survey = :rlcdp" +
						" AND pfu.finished = true ) "
				+ " "
				)
				.setParameter("rProject", lcdp.getProject())
				.setParameter("rlcdp", lcdp);
				
		return q.list();
	}

	@Override
	public List<User> getNotAssessedUsersByLCDPSurvey(LCDPSurvey lcdp) {
		Query q = session.createQuery("SELECT pu.user FROM ProjUser pu  " 
				+ " WHERE pu.project = :rProject "
				+ " AND pu.user NOT IN (SELECT pfu.assessee FROM LCDPSurveyUser pfu" +
						" WHERE pfu.survey = :rlcdpiling" +
						" AND pfu.finished = true ) "
				+ " "
				)
				.setParameter("rProject", lcdp.getProject())
				.setParameter("rlcdpiling", lcdp);
		
		
		return q.list();
	}


	@Override
	public List<LCDPSurvey> getLCDPsToSendReminder() {
		Date today = new Date();
		Calendar next8days = Calendar.getInstance();
		next8days.add(Calendar.DAY_OF_YEAR, 8);
		Query q = session.createQuery("SELECT p FROM LCDPSurvey AS p " +
				" WHERE p.edate >= :today AND p.edate <= :next8days "  )
				.setDate("next8days", next8days.getTime())
				.setDate("today", today);
		
		
		//HQL has no standard way to obtain Date Different
		//		"   AND (day(t.endDate) - day(current_date())) in elements(t.daysToRemind) "); 
		List<LCDPSurvey> objList = q.list();
		for(int i = objList.size()-1; i>=0; i--){
			LCDPSurvey e = (LCDPSurvey) objList.get(i);
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

	
}
