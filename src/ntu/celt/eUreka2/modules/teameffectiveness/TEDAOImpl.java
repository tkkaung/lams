package ntu.celt.eUreka2.modules.teameffectiveness;

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

public class TEDAOImpl implements TEDAO {
	
	@Inject
	private Session session;
	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_TE_PERSONALITY_SURVEY;
	}
	
	@Override
	public void addTEQuestionSet(TEQuestionSet r) {
		session.save(r);
	}
	@Override
	public void updateTEQuestionSet(TEQuestionSet r) {
		session.merge(r);
	}
	
	
	
	@Override
	public void deleteTEQuestionSet(TEQuestionSet r) {
		session.delete(r);
	}
	@Override
	public void immediateAddTEQuestionSet(TEQuestionSet r) {
		session.save(r);
	}

	@Override
	public TEQuestionSet getTEQuestionSetById(long id) {
		return (TEQuestionSet) session.get(TEQuestionSet.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TEQuestionSet> getTEQuestionSetsByOwner(User u) {
		Query q = session.createQuery("SELECT r FROM TEQuestionSet AS r WHERE r.owner = :rUser "
				+ " ORDER BY r.name ASC ")
				.setParameter("rUser", u);
		return q.list();
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<TEQuestionSet> searchTEQuestionSets(String searchText, User owner, School school) {
		Criteria crit = session.createCriteria(TEQuestionSet.class);
		
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
	public List<TEQuestionSet> searchTEQuestionSets(Project proj) {
		Set<TEQuestionSet> rSet = new HashSet<TEQuestionSet>();
		Query q = session.createQuery("SELECT a FROM TESurvey AS a " 
				+" WHERE a.project=:rProject "
				)
				.setParameter("rProject", proj);

		List<TESurvey> aList = q.list();
		for( TESurvey ipsp : aList){
			if(ipsp.getQuestionSet()!= null)
				rSet.add(ipsp.getQuestionSet());
		}
		
		return new ArrayList<TEQuestionSet>(rSet);
		
	}
	
	
	
	@Override
	public void addTESurvey(TESurvey TE) {
		session.save(TE);
	}
	@Override
	public void updateTESurvey(TESurvey TE) {
		session.merge(TE);
	}
	@Override
	public void deleteTESurvey(TESurvey TE) {
		TE.getTEUsers().clear();
		TE.getQuestions().clear();
		session.delete(TE);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TESurvey> getTESurveysByProject(Project p) {
		Criteria crit = session.createCriteria(TESurvey.class)
					.add(Restrictions.eq("project.id", p.getId()))
					.addOrder(Order.asc("orderNumber"))
					;
				
		return crit.list();
	}
	
	@Override
	public TQuestion getTQuestionById(long id) {
		return (TQuestion) session.get(TQuestion.class, id);
	}
	
	
	
	@Override
	public TESurvey getTESurveyById(long id) {
		return (TESurvey) session.get(TESurvey.class, id);
	}
	@Override
	public TEQuestion getTEQuestionById(long id) {
		return (TEQuestion) session.get(TEQuestion.class, id);
	}
	
	@Override
	public TESurveyUser getTESurveyUserById(long id) {
		return (TESurveyUser) session.get(TESurveyUser.class, id);
	}
	

	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		//Decided no need to have this
		return null;
	}

	@Override
	public List<TESurveyUser> getTESurveyUsersByProject(Project proj) {
		
		Query q = session.createQuery("SELECT pu FROM TESurveyUser as pu WHERE pu.survey.project=:rProj" +
				" ORDER BY assessee.username ASC, assessor.username ASC ")
				.setParameter("rProj", proj)
				;
	
		return q.list();
	}


	@Override
	public long countTESurveyByTEQuestionSet(TEQuestionSet qset) {
		Query q =  session.createQuery("SELECT COUNT(a.id) FROM TESurvey AS a " +
				" WHERE a.questionSet=:rQuestionSet " )
				.setParameter("rQuestionSet", qset);
		
		return  (Long) q.uniqueResult();
	}
	
	@Override
	public List getTESurveysByTEQuestionSet(TEQuestionSet qset) {
		Query q =  session.createQuery("SELECT a FROM TESurvey AS a " +
				" WHERE a.questionSet=:rQuestionSet " )
				.setParameter("rQuestionSet", qset);
		
		return  q.list();
	}

	


	@Override
	public void saveTESurveyUser(TESurveyUser TEUser) {
		session.save(TEUser);
	}

	
	@Override
	public List<TESurvey> searchTESurvey(String searchName, School searchSchool, String searchTerm, String searchCourseCode) {
		if(searchName==null)
			searchName = "";
		
		Query q =  session.createQuery("SELECT a FROM TESurvey AS a " 
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
	public List<TESurveyUser> searchTESurveyUser(TESurvey TE, User Assessor, User Assessee,
			Boolean finished) {
		
		Query q =  session.createQuery("SELECT a FROM TESurveyUser AS a " 
				+ " WHERE 1=1 "
				+ ((TE!=null)? " AND a.survey= :rTESurvey " : "")
				+ ((Assessor!=null) ? " AND a.assessor = :rAssessor" : "")
				+ (Assessee!=null? " AND a.assessee=:rAssessee" : "")
				+ ((finished!=null)? " AND a.finished=:rFinished " : "")
				+ "  "
				)
				;
		if (TE!=null){
			q.setParameter("rTESurvey", TE);
		}else{
		//	return new ArrayList<TESurveyUser>();
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
	public double getAverageScoreByDimension(TESurveyUser TEUser, Integer TEDimensionID){
		if(TEUser==null)
			return -1;
		Query q =  session.createQuery("SELECT avg(s.score"+TEDimensionID+") FROM TEQuestionScore AS s " 
				+ " WHERE s.TEUser=:rTEUser AND s.TEUser.finished= true  "
				+ " AND s.score"+TEDimensionID + ">0"
				)
				;
		q.setParameter("rTEUser", TEUser);
		Object r = q.uniqueResult();
		if(r==null)
			return -1.0;
		return (Double) r;
		
	}
	
	@Override
	public long getCountScoreByDimension(TESurveyUser TEUser, Integer TEDimensionID){
		if(TEUser==null)
			return -1;
		Query q =  session.createQuery("SELECT count(score"+TEDimensionID+") FROM TEQuestionScore AS s " 
				+ " WHERE s.TEUser=:rTEUser AND s.TEUser.finished= true "
				+  " AND score"+TEDimensionID + ">0"
				)
				;
		q.setParameter("rTEUser", TEUser);
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Long) r;
		
	}
	@Override
	public double getSTDEVScoreByDimension(TESurveyUser TEUser, Integer TEDimensionID){
		if(TEUser==null)
			return -1;
		Query q =  session.createQuery("SELECT sqrt((sum(score"+TEDimensionID+" * score"+TEDimensionID+")/count(score"+TEDimensionID+"))" +
				" - (avg(score"+TEDimensionID+") * avg(score"+TEDimensionID+"))) FROM TEQuestionScore AS s " 
				+ " WHERE s.TEUser=:rTEUser AND s.TEUser.finished= true "
				+ " AND score"+TEDimensionID+">0"
				
				)
				;
		q.setParameter("rTEUser", TEUser);
		
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	@Override
	public double getAverageScoreByDimensionStyleGroup(TESurveyUser TEUser, String StyleGroup){
		if(TEUser==null)
			return -1;
		Query q =  session.createQuery("SELECT avg(s.score) FROM TEQuestionScore AS s " 
				+ " WHERE s.TEUser=:rTEUser AND s.TEUser.finished= true  "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("rTEUser", TEUser);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	@Override
	public long getCountScoreByDimensionStyleGroup(TESurveyUser TEUser, String StyleGroup){
		if(TEUser==null)
			return -1;
		Query q =  session.createQuery("SELECT count(score) FROM TEQuestionScore AS s " 
				+ " WHERE s.TEUser=:rTEUser AND s.TEUser.finished= true  "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("rTEUser", TEUser);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Long) r;
		
	}
	@Override
	public double getSTDEVScoreByDimensionStyleGroup(TESurveyUser TEUser, String StyleGroup){
		if(TEUser==null)
			return -1;
		Query q =  session.createQuery("SELECT sqrt((sum(score * score)/count(score)) - (avg(score) * avg(score))) FROM TEQuestionScore AS s " 
				+ " WHERE s.TEUser=:rTEUser AND s.TEUser.finished= true "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("rTEUser", TEUser);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	
	
	@Override
	public double getAverageNormsByDimension(Project proj, Integer TEDimensionID){
		String sql = "SELECT avg(su.score"+TEDimensionID+") FROM TESurveyUser AS su " 
			+ " WHERE su.finished= true "
			+ (proj == null ? "": " AND su.survey.project=:rProj ")
			+ " AND  su.score"+TEDimensionID+">0"
			;
		Query q =  session.createQuery(sql)	;
		if(proj != null)
			q.setParameter("rProj", proj);
		
		Object r = q.uniqueResult();
		if(r==null)
			return 0;
		return (Double) r;
		
	}
	
	@Override
	public double getSTDEVNormsByDimension(Project proj, Integer TEDimensionID){
		Query q =  session.createQuery("SELECT sqrt((sum(score"+TEDimensionID+" * score"+TEDimensionID+")/count(score"+TEDimensionID+")) " +
				" - (avg(score"+TEDimensionID+") * avg(score"+TEDimensionID+"))) FROM TESurveyUser AS su " 
				+ " WHERE su.finished= true "
				+ (proj == null ? "": " AND su.survey.project=:rProj")
				+ " AND  su.score"+TEDimensionID+">0"
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
		
		Query q =  session.createQuery("SELECT avg(s.score) FROM TEQuestionScore AS s " 
				+ " WHERE s.TEUser.finished= true "
				+ (proj == null ? "": " AND s.TEUser.survey.project=:rProj")
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
		Query q =  session.createQuery("SELECT sqrt((sum(score * score)/count(score)) - (avg(score) * avg(score))) FROM TEQuestionScore AS s " 
				+ " WHERE s.TEUser.finished= true "
				+ (proj == null ? "": " AND s.TEUser.survey.project=:rProj")
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
	public List<TEQuestionScore> getScoresByDimension(TESurveyUser TEUser, Integer TEDimensionID){
		if(TEUser==null)
			return new ArrayList<TEQuestionScore>();
		Query q =  session.createQuery("SELECT s.score"+TEDimensionID+" FROM TEQuestionScore AS s " 
				+ " WHERE s.TEUser=:rTEUser "
				+ " AND s.score"+TEDimensionID + ">0"
				)
				;
		q.setParameter("rTEUser", TEUser);
		return q.list();
	}
	

	
	@Override
	public boolean isGroupBeingUse(Group group){
		Query q = session.createQuery("SELECT COUNT(a.id) FROM TESurvey AS a " +
				" WHERE a.group = :group "  )
				.setParameter("group", group);
		long size = (Long) q.uniqueResult();
		if(size==0)
			return false;
		
		return true;
	}

	

	@Override
	public List<TESurveyUser> getExpiredTESurveyUsers() {
		Calendar expiredTime = Calendar.getInstance();
		expiredTime.add(Calendar.MINUTE, (-1)* TESurveyUser.VALID_MINUTE_PER_QUESTION);
		
		Query q = session.createQuery("SELECT pu FROM TESurveyUser pu  " 
				+ " WHERE pu.lastAssessTime < :expireTime "
				+ " AND NOT(pu.finished = true) "
				+ " AND pu.lastQuestionNum > 0"
				)
				.setTimestamp("expireTime", expiredTime.getTime());
		return q.list();
	}

	@Override
	public List<User> getNotSubmitedUsersByTESurvey(TESurvey TE) {
		Query q = session.createQuery("SELECT pu.user FROM ProjUser pu  " 
				+ " WHERE pu.project = :rProject "
				+ " AND pu.user NOT IN (SELECT pfu.assessor FROM TESurveyUser pfu" +
						" WHERE pfu.survey = :rTE" +
						" AND pfu.finished = true ) "
				+ " "
				)
				.setParameter("rProject", TE.getProject())
				.setParameter("rTE", TE);
				
		return q.list();
	}

	@Override
	public List<User> getNotAssessedUsersByTESurvey(TESurvey TE) {
		Query q = session.createQuery("SELECT pu.user FROM ProjUser pu  " 
				+ " WHERE pu.project = :rProject "
				+ " AND pu.user NOT IN (SELECT pfu.assessee FROM TESurveyUser pfu" +
						" WHERE pfu.survey = :rTEiling" +
						" AND pfu.finished = true ) "
				+ " "
				)
				.setParameter("rProject", TE.getProject())
				.setParameter("rTEiling", TE);
		
		
		return q.list();
	}


	@Override
	public List<TESurvey> getTEsToSendReminder() {
		Date today = new Date();
		Calendar next8days = Calendar.getInstance();
		next8days.add(Calendar.DAY_OF_YEAR, 8);
		Query q = session.createQuery("SELECT p FROM TESurvey AS p " +
				" WHERE p.edate >= :today AND p.edate <= :next8days "  )
				.setDate("next8days", next8days.getTime())
				.setDate("today", today);
		
		
		//HQL has no standard way to obtain Date Different
		//		"   AND (day(t.endDate) - day(current_date())) in elements(t.daysToRemind) "); 
		List<TESurvey> objList = q.list();
		for(int i = objList.size()-1; i>=0; i--){
			TESurvey e = (TESurvey) objList.get(i);
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
	public List<TEDimension> getAllTEDimensions(){
		Query q =  session.createQuery("SELECT a FROM TEDimension AS a "
				+ " WHERE active=true "
				+ " ORDER BY a.orderNum ASC "
				)
				;
		
		return q.list();
	}
	
	@Override
	public void addTEDimension(TEDimension r) {
		session.save(r);
	}
	@Override
	public void updateTEDimension(TEDimension r) {
		session.merge(r);
	}

}
