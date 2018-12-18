package ntu.celt.eUreka2.modules.ipsp;

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
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.profiling.LQuestionType;

public class IPSPDAOImpl implements IPSPDAO {
	
	@Inject
	private Session session;
	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_INFLUENCING_PESUADING_SURVEY;
	}
	
	@Override
	public void addIQuestionSet(IQuestionSet r) {
		session.save(r);
	}
	@Override
	public void updateIQuestionSet(IQuestionSet r) {
		session.merge(r);
	}
	
	
	
	@Override
	public void deleteIQuestionSet(IQuestionSet r) {
		session.delete(r);
	}
	@Override
	public void immediateAddIQuestionSet(IQuestionSet r) {
		session.save(r);
	}

	@Override
	public IQuestionSet getIQuestionSetById(long id) {
		return (IQuestionSet) session.get(IQuestionSet.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IQuestionSet> getIQuestionSetsByOwner(User u) {
		Query q = session.createQuery("SELECT r FROM IQuestionSet AS r WHERE r.owner = :rUser "
				+ " ORDER BY r.name ASC ")
				.setParameter("rUser", u);
		return q.list();
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<IQuestionSet> searchIQuestionSets(String searchText, User owner, School school) {
		Criteria crit = session.createCriteria(IQuestionSet.class);
		
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
	public List<IQuestionSet> searchIQuestionSets(Project proj) {
		Set<IQuestionSet> rSet = new HashSet<IQuestionSet>();
		Query q = session.createQuery("SELECT a FROM IPSPSurvey AS a " 
				+" WHERE a.project=:rProject "
				)
				.setParameter("rProject", proj);

		List<IPSPSurvey> aList = q.list();
		for( IPSPSurvey ipsp : aList){
			if(ipsp.getQuestionSet()!= null)
				rSet.add(ipsp.getQuestionSet());
		}
		
		return new ArrayList<IQuestionSet>(rSet);
		
	}
	
	
	
	
	@Override
	public void addIPSPSurvey(IPSPSurvey ipsp) {
		session.save(ipsp);
	}
	@Override
	public void updateIPSPSurvey(IPSPSurvey ipsp) {
		session.merge(ipsp);
	}
	@Override
	public void deleteIPSPSurvey(IPSPSurvey ipsp) {
		ipsp.getIpspUsers().clear();
		ipsp.getQuestions().clear();
		session.delete(ipsp);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IPSPSurvey> getIPSPSurveysByProject(Project p) {
		Criteria crit = session.createCriteria(IPSPSurvey.class)
					.add(Restrictions.eq("project.id", p.getId()))
					.addOrder(Order.asc("orderNumber"))
					;
				
		return crit.list();
	}
	
	@Override
	public IQuestion getIQuestionById(long id) {
		return (IQuestion) session.get(IQuestion.class, id);
	}
	
	
	
	@Override
	public IPSPSurvey getIPSPSurveyById(long id) {
		return (IPSPSurvey) session.get(IPSPSurvey.class, id);
	}
	@Override
	public IPSPQuestion getBIGQuestionById(long id) {
		return (IPSPQuestion) session.get(IPSPQuestion.class, id);
	}
	
	@Override
	public IPSPSurveyUser getIPSPSurveyUserById(long id) {
		return (IPSPSurveyUser) session.get(IPSPSurveyUser.class, id);
	}
	

	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		//Decided no need to have this
		return null;
	}

	@Override
	public List<IPSPSurveyUser> getIPSPSurveyUsersByProject(Project proj) {
		
		Query q = session.createQuery("SELECT pu FROM IPSPSurveyUser as pu WHERE pu.survey.project=:rProj" +
				" ORDER BY assessee.username ASC, assessor.username ASC ")
				.setParameter("rProj", proj)
				;
	
		return q.list();
	}


	@Override
	public long countIPSPSurveyByIQuestionSet(IQuestionSet qset) {
		Query q =  session.createQuery("SELECT COUNT(a.id) FROM IPSPSurvey AS a " +
				" WHERE a.questionSet=:rQuestionSet " )
				.setParameter("rQuestionSet", qset);
		
		return  (Long) q.uniqueResult();
	}
	
	@Override
	public List getIPSPSurveysByIQuestionSet(IQuestionSet qset) {
		Query q =  session.createQuery("SELECT a FROM IPSPSurvey AS a " +
				" WHERE a.questionSet=:rQuestionSet " )
				.setParameter("rQuestionSet", qset);
		
		return  q.list();
	}

	


	@Override
	public void saveIPSPSurveyUser(IPSPSurveyUser ipspUser) {
		session.save(ipspUser);
	}

	
	@Override
	public List<IPSPSurvey> searchIPSPSurvey(String searchName, School searchSchool, String searchTerm, String searchCourseCode) {
		if(searchName==null)
			searchName = "";
		
		Query q =  session.createQuery("SELECT a FROM IPSPSurvey AS a " 
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
	public List<IPSPSurveyUser> searchIPSPSurveyUser(IPSPSurvey ipsp, User Assessor, User Assessee, IQuestionType qType,
			Boolean finished) {
		
		Query q =  session.createQuery("SELECT a FROM IPSPSurveyUser AS a " 
				+ " WHERE 1=1 "
				+ ((ipsp!=null)? " AND a.survey= :rIPSPSurvey " : "")
				+ ((Assessor!=null) ? " AND a.assessor = :rAssessor" : "")
				+ (Assessee!=null? " AND a.assessee=:rAssessee" : "")
				+ ((qType!=null)? " AND a.survey.questionType=:rQuestionType " : "")
				+ ((finished!=null)? " AND a.finished=:rFinished " : "")
				+ "  "
				)
				;
		if (ipsp!=null){
			q.setParameter("rIPSPSurvey", ipsp);
		}else{
		//	return new ArrayList<IPSPSurveyUser>();
		}
		if (Assessor!=null){
			q.setParameter("rAssessor", Assessor);
		}
				 
		if (Assessee!=null){
			q.setParameter("rAssessee", Assessee);
		}
		if (qType!=null){
			q.setParameter("rQuestionType", qType);
		}
		if (finished!=null){
			q.setParameter("rFinished", finished);
		}
		
		
		return q.list();
	}
	
	@Override
	public List<IPSPSurveyUser> searchIPSPSurveyUser(Project proj, User Assessor, User Assessee, IQuestionType qType,
			Boolean finished) {
		
		Query q =  session.createQuery("SELECT a FROM IPSPSurveyUser AS a " 
				+ " WHERE 1=1 "
				+ ((proj!=null)? " AND a.survey.project= :rProject " : "")
				+ ((Assessor!=null) ? " AND a.assessor = :rAssessor" : "")
				+ (Assessee!=null? " AND a.assessee=:rAssessee" : "")
				+ ((qType!=null)? " AND a.survey.questionType=:rQuestionType " : "")
				+ ((finished!=null)? " AND a.finished=:rFinished " : "")
				+ "  "
				)
				;
		if (proj!=null){
			q.setParameter("rProject", proj);
		}else{
		//	return new ArrayList<IPSPSurveyUser>();
		}
		if (Assessor!=null){
			q.setParameter("rAssessor", Assessor);
		}
				 
		if (Assessee!=null){
			q.setParameter("rAssessee", Assessee);
		}
		if (qType!=null){
			q.setParameter("rQuestionType", qType);
		}
		if (finished!=null){
			q.setParameter("rFinished", finished);
		}
		
		
		return q.list();
	}

	
	@Override
	public double getAverageScoreByDimension(IPSPSurveyUser ipspUser, Integer DimensionID){
		if(ipspUser==null)
			return -1;
		Query q =  session.createQuery("SELECT avg(s.score"+DimensionID+") FROM IQuestionScore AS s " 
				+ " WHERE s.ipspUser=:rBig5User AND s.ipspUser.finished= true  "
				+ " AND s.score"+DimensionID + ">0"
				)
				;
		q.setParameter("rBig5User", ipspUser);
		Object r = q.uniqueResult();
		if(r==null)
			return -1.0;
		return (Double) r;
		
	}
	@Override
	public double getSumScoreByDimension(IPSPSurveyUser ipspUser, Integer DimensionID){
		if(ipspUser==null)
			return -1;
		Query q =  session.createQuery("SELECT sum(s.score"+DimensionID+") FROM IQuestionScore AS s " 
				+ " WHERE s.ipspUser=:rBig5User AND s.ipspUser.finished= true  "
				+ " AND s.score"+DimensionID + ">0"
				)
				;
		q.setParameter("rBig5User", ipspUser);
		Long r = (Long) q.uniqueResult();
		if(r==null)
			return -1.0;
		return (double) r;
		
	}
	
	@Override
	public long getCountScoreByDimension(IPSPSurveyUser ipspUser, Integer DimensionID){
		if(ipspUser==null)
			return -1;
		Query q =  session.createQuery("SELECT count(score"+DimensionID+") FROM IQuestionScore AS s " 
				+ " WHERE s.ipspUser=:ripspUser AND s.ipspUser.finished= true "
				+  " AND score"+DimensionID + ">0"
				)
				;
		q.setParameter("ripspUser", ipspUser);
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Long) r;
		
	}
	@Override
	public double getSTDEVScoreByDimension(IPSPSurveyUser ipspUser, Integer DimensionID){
		if(ipspUser==null)
			return -1;
		Query q =  session.createQuery("SELECT sqrt((sum(score"+DimensionID+" * score"+DimensionID+")/count(score"+DimensionID+"))" +
				" - (avg(score"+DimensionID+") * avg(score"+DimensionID+"))) FROM IQuestionScore AS s " 
				+ " WHERE s.ipspUser=:ripspUser AND s.ipspUser.finished= true "
				+ " AND score"+DimensionID+">0"
				
				)
				;
		q.setParameter("ripspUser", ipspUser);
		
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	@Override
	public double getAverageScoreByDimensionStyleGroup(IPSPSurveyUser ipspUser, String StyleGroup){
		if(ipspUser==null)
			return -1;
		Query q =  session.createQuery("SELECT avg(s.score) FROM IQuestionScore AS s " 
				+ " WHERE s.ipspUser=:ripspUser AND s.ipspUser.finished= true  "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("ripspUser", ipspUser);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	@Override
	public long getCountScoreByDimensionStyleGroup(IPSPSurveyUser ipspUser, String StyleGroup){
		if(ipspUser==null)
			return -1;
		Query q =  session.createQuery("SELECT count(score) FROM IQuestionScore AS s " 
				+ " WHERE s.ipspUser=:ripspUser AND s.ipspUser.finished= true  "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("ripspUser", ipspUser);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Long) r;
		
	}
	@Override
	public double getSTDEVScoreByDimensionStyleGroup(IPSPSurveyUser ipspUser, String StyleGroup){
		if(ipspUser==null)
			return -1;
		Query q =  session.createQuery("SELECT sqrt((sum(score * score)/count(score)) - (avg(score) * avg(score))) FROM IQuestionScore AS s " 
				+ " WHERE s.ipspUser=:ripspUser AND s.ipspUser.finished= true "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("ripspUser", ipspUser);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	
	
	@Override
	public double getAverageNormsByDimension(Project proj, Integer DimensionID){
		String sql = "SELECT avg(su.score"+DimensionID+") FROM IPSPSurveyUser AS su " 
			+ " WHERE su.finished= true "
			+ (proj == null ? "": " AND su.survey.project=:rProj ")
			+ " AND  su.score"+DimensionID+">0"
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
	public double getSTDEVNormsByDimension(Project proj, Integer DimensionID){
		Query q =  session.createQuery("SELECT sqrt((sum(score"+DimensionID+" * score"+DimensionID+")/count(score"+DimensionID+")) " +
				" - (avg(score"+DimensionID+") * avg(score"+DimensionID+"))) FROM IPSPSurveyUser AS su " 
				+ " WHERE su.finished= true "
				+ (proj == null ? "": " AND su.survey.project=:rProj")
				+ " AND  su.score"+DimensionID+">0"
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
		
		Query q =  session.createQuery("SELECT avg(s.score) FROM IQuestionScore AS s " 
				+ " WHERE s.ipspUser.finished= true "
				+ (proj == null ? "": " AND s.ipspUser.survey.project=:rProj")
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
		Query q =  session.createQuery("SELECT sqrt((sum(score * score)/count(score)) - (avg(score) * avg(score))) FROM IQuestionScore AS s " 
				+ " WHERE s.ipspUser.finished= true "
				+ (proj == null ? "": " AND s.ipspUser.survey.project=:rProj")
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
	public List<IQuestionScore> getScoresByDimension(IPSPSurveyUser ipspUser, Integer DimensionID){
		if(ipspUser==null)
			return new ArrayList<IQuestionScore>();
		Query q =  session.createQuery("SELECT s.score"+DimensionID+" FROM IQuestionScore AS s " 
				+ " WHERE s.ipspUser=:ripspUser "
				+ " AND s.score"+DimensionID + ">0"
				)
				;
		q.setParameter("ripspUser", ipspUser);
		return q.list();
	}
	

	
	@Override
	public boolean isGroupBeingUse(Group group){
		Query q = session.createQuery("SELECT COUNT(a.id) FROM IPSPSurvey AS a " +
				" WHERE a.group = :group "  )
				.setParameter("group", group);
		long size = (Long) q.uniqueResult();
		if(size==0)
			return false;
		
		return true;
	}

	

	@Override
	public List<IPSPSurveyUser> getExpiredIPSPSurveyUsers() {
		Calendar expiredTime = Calendar.getInstance();
		expiredTime.add(Calendar.MINUTE, (-1)* IPSPSurveyUser.VALID_MINUTE_PER_QUESTION);
		
		Query q = session.createQuery("SELECT pu FROM IPSPSurveyUser pu  " 
				+ " WHERE pu.lastAssessTime < :expireTime "
				+ " AND NOT(pu.finished = true) "
				+ " AND pu.lastQuestionNum > 0"
				)
				.setTimestamp("expireTime", expiredTime.getTime());
		return q.list();
	}

	@Override
	public List<User> getNotSubmitedUsersByIPSPSurvey(IPSPSurvey ipsp) {
		Query q = session.createQuery("SELECT pu.user FROM ProjUser pu  " 
				+ " WHERE pu.project = :rProject "
				+ " AND pu.user NOT IN (SELECT pfu.assessor FROM IPSPSurveyUser pfu" +
						" WHERE pfu.survey = :ripsp" +
						" AND pfu.finished = true ) "
				+ " "
				)
				.setParameter("rProject", ipsp.getProject())
				.setParameter("ripsp", ipsp);
				
		return q.list();
	}

	@Override
	public List<User> getNotAssessedUsersByIPSPSurvey(IPSPSurvey ipsp) {
		Query q = session.createQuery("SELECT pu.user FROM ProjUser pu  " 
				+ " WHERE pu.project = :rProject "
				+ " AND pu.user NOT IN (SELECT pfu.assessee FROM IPSPSurveyUser pfu" +
						" WHERE pfu.survey = :ripspiling" +
						" AND pfu.finished = true ) "
				+ " "
				)
				.setParameter("rProject", ipsp.getProject())
				.setParameter("ripspiling", ipsp);
		
		
		return q.list();
	}


	@Override
	public List<IPSPSurvey> getIPSPsToSendReminder() {
		Date today = new Date();
		Calendar next8days = Calendar.getInstance();
		next8days.add(Calendar.DAY_OF_YEAR, 8);
		Query q = session.createQuery("SELECT p FROM IPSPSurvey AS p " +
				" WHERE p.edate >= :today AND p.edate <= :next8days "  )
				.setDate("next8days", next8days.getTime())
				.setDate("today", today);
		
		
		//HQL has no standard way to obtain Date Different
		//		"   AND (day(t.endDate) - day(current_date())) in elements(t.daysToRemind) "); 
		List<IPSPSurvey> objList = q.list();
		for(int i = objList.size()-1; i>=0; i--){
			IPSPSurvey e = (IPSPSurvey) objList.get(i);
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
	public List<IDimension> getAllIDimensions(){
		Query q =  session.createQuery("SELECT a FROM IDimension AS a "
				+ " WHERE active=true "
				+ " ORDER BY a.orderNum ASC "
				)
				;
		
		return q.list();
	}
	
	@Override
	public void addIDimension(IDimension r) {
		session.save(r);
	}
	@Override
	public void updateIDimension(IDimension r) {
		session.merge(r);
	}


}
