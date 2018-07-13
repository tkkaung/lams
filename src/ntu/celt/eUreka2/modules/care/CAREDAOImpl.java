package ntu.celt.eUreka2.modules.care;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import ntu.celt.eUreka2.modules.profiling.LDimension;

public class CAREDAOImpl implements CAREDAO {
	
	@Inject
	private Session session;
	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_CARE_PSYCHOMETRIC_SURVEY;
	}
	
	@Override
	public void addCQuestionSet(CQuestionSet r) {
		session.save(r);
	}
	@Override
	public void updateCQuestionSet(CQuestionSet r) {
		session.merge(r);
	}
	
	
	
	@Override
	public void deleteCQuestionSet(CQuestionSet r) {
		session.delete(r);
	}
	@Override
	public void immediateAddCQuestionSet(CQuestionSet r) {
		session.save(r);
	}

	@Override
	public CQuestionSet getCQuestionSetById(long id) {
		return (CQuestionSet) session.get(CQuestionSet.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CQuestionSet> getCQuestionSetsByOwner(User u) {
		Query q = session.createQuery("SELECT r FROM CQuestionSet AS r WHERE r.owner = :rUser "
				+ " ORDER BY r.name ASC ")
				.setParameter("rUser", u);
		return q.list();
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<CQuestionSet> searchCQuestionSets(String searchText, User owner, School school) {
		Criteria crit = session.createCriteria(CQuestionSet.class);
		
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
	
	
	
	@Override
	public void addCARESurvey(CARESurvey care) {
		session.save(care);
	}
	@Override
	public void updateCARESurvey(CARESurvey care) {
		session.merge(care);
	}
	@Override
	public void deleteCARESurvey(CARESurvey care) {
		care.getCareUsers().clear();
		care.getQuestions().clear();
		session.delete(care);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CARESurvey> getCARESurveysByProject(Project p) {
		Criteria crit = session.createCriteria(CARESurvey.class)
					.add(Restrictions.eq("project.id", p.getId()))
					.addOrder(Order.asc("orderNumber"))
					;
				
		return crit.list();
	}
	
	@Override
	public CQuestion getCQuestionById(long id) {
		return (CQuestion) session.get(CQuestion.class, id);
	}
	
	
	
	@Override
	public CARESurvey getCARESurveyById(long id) {
		return (CARESurvey) session.get(CARESurvey.class, id);
	}
	@Override
	public CAREQuestion getCAREQuestionById(long id) {
		return (CAREQuestion) session.get(CAREQuestion.class, id);
	}
	
	@Override
	public CARESurveyUser getCARESurveyUserById(long id) {
		return (CARESurveyUser) session.get(CARESurveyUser.class, id);
	}
	

	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		//Decided no need to have this
		return null;
	}

	@Override
	public List<CARESurveyUser> getCARESurveyUsersByProject(Project proj) {
		
		Query q = session.createQuery("SELECT pu FROM CARESurveyUser as pu WHERE pu.survey.project=:rProj" +
				" ORDER BY assessee.username ASC, assessor.username ASC ")
				.setParameter("rProj", proj)
				;
	
		return q.list();
	}


	@Override
	public long countCARESurveyByCQuestionSet(CQuestionSet qset) {
		Query q =  session.createQuery("SELECT COUNT(a.id) FROM CARESurvey AS a " +
				" WHERE a.questionSet=:rQuestionSet " )
				.setParameter("rQuestionSet", qset);
		
		return  (Long) q.uniqueResult();
	}
	
	@Override
	public List getCARESurveysByCQuestionSet(CQuestionSet qset) {
		Query q =  session.createQuery("SELECT a FROM CARESurvey AS a " +
				" WHERE a.questionSet=:rQuestionSet " )
				.setParameter("rQuestionSet", qset);
		
		return  q.list();
	}

	


	@Override
	public void saveCARESurveyUser(CARESurveyUser careUser) {
		session.save(careUser);
	}

	
	@Override
	public List<CARESurvey> searchCARESurvey(String searchName, School searchSchool, String searchTerm, String searchCourseCode) {
		if(searchName==null)
			searchName = "";
		
		Query q =  session.createQuery("SELECT a FROM CARESurvey AS a " 
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
	public List<CARESurveyUser> searchCARESurveyUser(CARESurvey care, User Assessor, User Assessee,
			Boolean finished) {
		
		Query q =  session.createQuery("SELECT a FROM CARESurveyUser AS a " 
				+ " WHERE 1=1 "
				+ ((care!=null)? " AND a.survey= :rCARESurvey " : "")
				+ ((Assessor!=null) ? " AND a.assessor = :rAssessor" : "")
				+ (Assessee!=null? " AND a.assessee=:rAssessee" : "")
				+ ((finished!=null)? " AND a.finished=:rFinished " : "")
				+ "  "
				)
				;
		if (care!=null){
			q.setParameter("rCARESurvey", care);
		}else{
		//	return new ArrayList<CARESurveyUser>();
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
	public double getAverageScoreByDimension(CARESurveyUser careUser, Integer CDimensionID){
		if(careUser==null)
			return -1;
		Query q =  session.createQuery("SELECT avg(s.score"+CDimensionID+") FROM CQuestionScore AS s " 
				+ " WHERE s.careUser=:rCareUser AND s.careUser.finished= true  "
				+ " AND s.score"+CDimensionID + ">0"
				)
				;
		q.setParameter("rCareUser", careUser);
		Object r = q.uniqueResult();
		if(r==null)
			return -1.0;
		return (Double) r;
		
	}
	
	@Override
	public long getCountScoreByDimension(CARESurveyUser careUser, Integer CDimensionID){
		if(careUser==null)
			return -1;
		Query q =  session.createQuery("SELECT count(score"+CDimensionID+") FROM CQuestionScore AS s " 
				+ " WHERE s.careUser=:rcareUser AND s.careUser.finished= true "
				+  " AND score"+CDimensionID + ">0"
				)
				;
		q.setParameter("rcareUser", careUser);
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Long) r;
		
	}
	@Override
	public double getSTDEVScoreByDimension(CARESurveyUser careUser, Integer CDimensionID){
		if(careUser==null)
			return -1;
		Query q =  session.createQuery("SELECT sqrt((sum(score"+CDimensionID+" * score"+CDimensionID+")/count(score"+CDimensionID+"))" +
				" - (avg(score"+CDimensionID+") * avg(score"+CDimensionID+"))) FROM CQuestionScore AS s " 
				+ " WHERE s.careUser=:rcareUser AND s.careUser.finished= true "
				+ " AND score"+CDimensionID+">0"
				
				)
				;
		q.setParameter("rcareUser", careUser);
		
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	@Override
	public double getAverageScoreByDimensionStyleGroup(CARESurveyUser careUser, String StyleGroup){
		if(careUser==null)
			return -1;
		Query q =  session.createQuery("SELECT avg(s.score) FROM CQuestionScore AS s " 
				+ " WHERE s.careUser=:rcareUser AND s.careUser.finished= true  "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("rcareUser", careUser);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	@Override
	public long getCountScoreByDimensionStyleGroup(CARESurveyUser careUser, String StyleGroup){
		if(careUser==null)
			return -1;
		Query q =  session.createQuery("SELECT count(score) FROM CQuestionScore AS s " 
				+ " WHERE s.careUser=:rcareUser AND s.careUser.finished= true  "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("rcareUser", careUser);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Long) r;
		
	}
	@Override
	public double getSTDEVScoreByDimensionStyleGroup(CARESurveyUser careUser, String StyleGroup){
		if(careUser==null)
			return -1;
		Query q =  session.createQuery("SELECT sqrt((sum(score * score)/count(score)) - (avg(score) * avg(score))) FROM CQuestionScore AS s " 
				+ " WHERE s.careUser=:rcareUser AND s.careUser.finished= true "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("rcareUser", careUser);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	
	
	@Override
	public double getAverageNormsByDimension(Project proj, Integer CDimensionID){
		String sql = "SELECT avg(su.score"+CDimensionID+") FROM CARESurveyUser AS su " 
			+ " WHERE su.finished= true "
			+ (proj == null ? "": " AND su.survey.project=:rProj ")
			+ " AND  su.score"+CDimensionID+">0"
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
	public double getSTDEVNormsByDimension(Project proj, Integer CDimensionID){
		Query q =  session.createQuery("SELECT sqrt((sum(score"+CDimensionID+" * score"+CDimensionID+")/count(score"+CDimensionID+")) " +
				" - (avg(score"+CDimensionID+") * avg(score"+CDimensionID+"))) FROM CARESurveyUser AS su " 
				+ " WHERE su.finished= true "
				+ (proj == null ? "": " AND su.survey.project=:rProj")
				+ " AND  su.score"+CDimensionID+">0"
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
		
		Query q =  session.createQuery("SELECT avg(s.score) FROM CQuestionScore AS s " 
				+ " WHERE s.careUser.finished= true "
				+ (proj == null ? "": " AND s.careUser.survey.project=:rProj")
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
		Query q =  session.createQuery("SELECT sqrt((sum(score * score)/count(score)) - (avg(score) * avg(score))) FROM CQuestionScore AS s " 
				+ " WHERE s.careUser.finished= true "
				+ (proj == null ? "": " AND s.careUser.survey.project=:rProj")
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
	public List<CQuestionScore> getScoresByDimension(CARESurveyUser careUser, Integer CDimensionID){
		if(careUser==null)
			return new ArrayList<CQuestionScore>();
		Query q =  session.createQuery("SELECT s.score"+CDimensionID+" FROM CQuestionScore AS s " 
				+ " WHERE s.careUser=:rcareUser "
				+ " AND s.score"+CDimensionID + ">0"
				)
				;
		q.setParameter("rcareUser", careUser);
		return q.list();
	}
	

	
	@Override
	public boolean isGroupBeingUse(Group group){
		Query q = session.createQuery("SELECT COUNT(a.id) FROM CARESurvey AS a " +
				" WHERE a.group = :group "  )
				.setParameter("group", group);
		long size = (Long) q.uniqueResult();
		if(size==0)
			return false;
		
		return true;
	}

	

	@Override
	public List<CARESurveyUser> getExpiredCARESurveyUsers() {
		Calendar expiredTime = Calendar.getInstance();
		expiredTime.add(Calendar.MINUTE, (-1)* CARESurveyUser.VALID_MINUTE_PER_QUESTION);
		
		Query q = session.createQuery("SELECT pu FROM CARESurveyUser pu  " 
				+ " WHERE pu.lastAssessTime < :expireTime "
				+ " AND NOT(pu.finished = true) "
				+ " AND pu.lastQuestionNum > 0"
				)
				.setTimestamp("expireTime", expiredTime.getTime());
		return q.list();
	}

	@Override
	public List<User> getNotSubmitedUsersByCARESurvey(CARESurvey care) {
		Query q = session.createQuery("SELECT pu.user FROM ProjUser pu  " 
				+ " WHERE pu.project = :rProject "
				+ " AND pu.user NOT IN (SELECT pfu.assessor FROM CARESurveyUser pfu" +
						" WHERE pfu.survey = :rcare" +
						" AND pfu.finished = true ) "
				+ " "
				)
				.setParameter("rProject", care.getProject())
				.setParameter("rcare", care);
				
		return q.list();
	}

	@Override
	public List<User> getNotAssessedUsersByCARESurvey(CARESurvey care) {
		Query q = session.createQuery("SELECT pu.user FROM ProjUser pu  " 
				+ " WHERE pu.project = :rProject "
				+ " AND pu.user NOT IN (SELECT pfu.assessee FROM CARESurveyUser pfu" +
						" WHERE pfu.survey = :rcareiling" +
						" AND pfu.finished = true ) "
				+ " "
				)
				.setParameter("rProject", care.getProject())
				.setParameter("rcareiling", care);
		
		
		return q.list();
	}


	@Override
	public List<CARESurvey> getCAREsToSendReminder() {
		Date today = new Date();
		Calendar next8days = Calendar.getInstance();
		next8days.add(Calendar.DAY_OF_YEAR, 8);
		Query q = session.createQuery("SELECT p FROM CARESurvey AS p " +
				" WHERE p.edate >= :today AND p.edate <= :next8days "  )
				.setDate("next8days", next8days.getTime())
				.setDate("today", today);
		
		
		//HQL has no standard way to obtain Date Different
		//		"   AND (day(t.endDate) - day(current_date())) in elements(t.daysToRemind) "); 
		List<CARESurvey> objList = q.list();
		for(int i = objList.size()-1; i>=0; i--){
			CARESurvey e = (CARESurvey) objList.get(i);
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
	public List<CDimension> getAllCDimensions(){
		Query q =  session.createQuery("SELECT a FROM CDimension AS a "
				+ " WHERE active=true "
				+ " ORDER BY a.orderNum ASC "
				)
				;
		
		return q.list();
	}
	
	@Override
	public void addCDimension(CDimension r) {
		session.save(r);
	}
	@Override
	public void updateCDimension(CDimension r) {
		session.merge(r);
	}

	@Override
	public CAREParticular getParticularByUser(User user){
		Query q =  session.createQuery("SELECT a FROM CAREParticular AS a "
				+" WHERE a.user=:rUser")
				.setParameter("rUser", user)
			;
		if(q.list().size()>0)
			return (CAREParticular) q.list().get(0);
		
		return null;
	}
	@Override
	public void saveOrUpdateParticular(CAREParticular p) {
		session.saveOrUpdate(p);
	}
}
