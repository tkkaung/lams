package ntu.celt.eUreka2.modules.profiling;

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

public class ProfilingDAOImpl implements ProfilingDAO {
	
	@Inject
	private Session session;
	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_LEADERSHIP_PROFILING;
	}
	
	@Override
	public void addLQuestionSet(LQuestionSet r) {
		session.save(r);
	}
	@Override
	public void updateLQuestionSet(LQuestionSet r) {
		session.merge(r);
	}
	
	
	
	@Override
	public void deleteLQuestionSet(LQuestionSet r) {
		session.delete(r);
	}
	@Override
	public void immediateAddLQuestionSet(LQuestionSet r) {
		session.save(r);
	}

	@Override
	public LQuestionSet getLQuestionSetById(long id) {
		return (LQuestionSet) session.get(LQuestionSet.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LQuestionSet> getLQuestionSetsByOwner(User u) {
		Query q = session.createQuery("SELECT r FROM LQuestionSet AS r WHERE r.owner = :rUser "
				+ " ORDER BY r.name ASC ")
				.setParameter("rUser", u);
		return q.list();
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<LQuestionSet> searchLQuestionSets(String searchText, User owner, School school, LQuestionType qType) {
		Criteria crit = session.createCriteria(LQuestionSet.class);
		
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
		if(qType!=null){
			crit = crit.add(Restrictions.eq("questionType", qType));
		}
		crit.addOrder(Order.desc("mdate"));
		
		
		return crit.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<LQuestionSet> searchLQuestionSets(Project proj) {
		Set<LQuestionSet> rSet = new HashSet<LQuestionSet>();
		Query q = session.createQuery("SELECT a FROM Profiling AS a " 
				+" WHERE a.project=:rProject "
				)
				.setParameter("rProject", proj);

		List<Profiling> aList = q.list();
		for( Profiling ipsp : aList){
			if(ipsp.getQuestionSet()!= null)
				rSet.add(ipsp.getQuestionSet());
		}
		
		return new ArrayList<LQuestionSet>(rSet);
	}
	
	
	
	@Override
	public void addProfiling(Profiling prof) {
		session.save(prof);
	}
	@Override
	public void updateProfiling(Profiling prof) {
		session.merge(prof);
	}
	@Override
	public void deleteProfiling(Profiling prof) {
		prof.getProfUsers().clear();
		prof.getQuestions().clear();
		session.delete(prof);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Profiling> getProfilingsByProject(Project p) {
		Criteria crit = session.createCriteria(Profiling.class)
					.add(Restrictions.eq("project.id", p.getId()))
					.addOrder(Order.asc("orderNumber"))
					;
				
		return crit.list();
	}
	
	@Override
	public LQuestion getLQuestionById(long id) {
		return (LQuestion) session.get(LQuestion.class, id);
	}
	
	@Override
	public LDimension getLDimensionById(String id) {
		return (LDimension) session.get(LDimension.class, id);
	}
	
	@Override
	public Profiling getProfilingById(long id) {
		return (Profiling) session.get(Profiling.class, id);
	}
	@Override
	public ProfileQuestion getProfileQuestionById(long id) {
		return (ProfileQuestion) session.get(ProfileQuestion.class, id);
	}
	
	@Override
	public ProfileUser getProfileUserById(long id) {
		return (ProfileUser) session.get(ProfileUser.class, id);
	}
	

	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		//Decided no need to have this
		return null;
	}

	@Override
	public List<ProfileUser> getProfileUsersByProject(Project proj) {
		
		Query q = session.createQuery("SELECT pu FROM ProfileUser as pu WHERE pu.profile.project=:rProj" +
				" ORDER BY assessee.username ASC, profile.questionType ASC, assessor.username ASC ")
				.setParameter("rProj", proj)
				;
	
		return q.list();
	}


	@Override
	public long countProfilingByLQuestionSet(LQuestionSet qset) {
		Query q =  session.createQuery("SELECT COUNT(a.id) FROM Profiling AS a " +
				" WHERE a.questionSet=:rQuestionSet " )
				.setParameter("rQuestionSet", qset);
		
		return  (Long) q.uniqueResult();
	}
	
	@Override
	public List getProfilingsByLQuestionSet(LQuestionSet qset) {
		Query q =  session.createQuery("SELECT a FROM Profiling AS a " +
				" WHERE a.questionSet=:rQuestionSet " )
				.setParameter("rQuestionSet", qset);
		
		return  q.list();
	}

	


	@Override
	public void saveProfileUser(ProfileUser profUser) {
		session.save(profUser);
	}

	
	@Override
	public List<Profiling> searchProfiling(String searchName, LQuestionType qType, School searchSchool, String searchTerm, String searchCourseCode) {
		if(searchName==null)
			searchName = "";
		
		Query q =  session.createQuery("SELECT a FROM Profiling AS a " 
				+ " WHERE a.name LIKE :rSearchTxt "
				+ (searchSchool!=null ? " AND a.project.school = :rSchool" : "")
				+ ((searchTerm!=null)? " AND a.project.term=:rTerm" : "")
				+ ((searchCourseCode!=null)? " AND a.project.courseCode=:rCourseCode" : "")
				+ ((qType!=null)? " AND (a.questionType=:rQuestionType )   " : "")
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
		if (qType!=null){
			q.setParameter("rQuestionType", qType);
		}
		if (searchCourseCode!=null){
			q.setParameter("rCourseCode", searchCourseCode);
		}
		
		
		return q.list();
	}
	
	@Override
	public List<ProfileUser> searchProfileUser(Profiling prof, User Assessor, User Assessee,
			LQuestionType qType, Boolean finished) {
		
		Query q =  session.createQuery("SELECT a FROM ProfileUser AS a " 
				+ " WHERE 1=1 "
				+ ((prof!=null)? " AND a.profile= :rProfile " : "")
				+ ((Assessor!=null) ? " AND a.assessor = :rAssessor" : "")
				+ (Assessee!=null? " AND a.assessee=:rAssessee" : "")
				+ ((qType!=null)? " AND a.profile.questionType=:rQuestionType " : "")
				+ ((finished!=null)? " AND a.finished=:rFinished " : "")
				+ "  "
				)
				;
		if (prof!=null){
			q.setParameter("rProfile", prof);
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
	public double getAverageScoreByDimension(ProfileUser profUser, String LDimensionID){
		if(profUser==null)
			return -1;
		Query q =  session.createQuery("SELECT avg(s.score) FROM QuestionScore AS s " 
				+ " WHERE s.profUser=:rProfUser AND s.profUser.finished= true  "
				+ (LDimensionID == null ? "": " AND s.question.dimension.id=:rDimensionID")
				)
				;
		q.setParameter("rProfUser", profUser);
		if(LDimensionID != null){
			q.setString("rDimensionID", LDimensionID);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	@Override
	public long getCountScoreByDimension(ProfileUser profUser, String LDimensionID){
		if(profUser==null)
			return -1;
		Query q =  session.createQuery("SELECT count(score) FROM QuestionScore AS s " 
				+ " WHERE s.profUser=:rProfUser AND s.profUser.finished= true  "
				+ (LDimensionID == null ? "": " AND s.question.dimension.id=:rDimensionID")
				)
				;
		q.setParameter("rProfUser", profUser);
		if(LDimensionID != null){
			q.setString("rDimensionID", LDimensionID);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Long) r;
		
	}
	@Override
	public double getSTDEVScoreByDimension(ProfileUser profUser, String LDimensionID){
		if(profUser==null)
			return -1;
		Query q =  session.createQuery("SELECT sqrt((sum(score * score)/count(score)) - (avg(score) * avg(score))) FROM QuestionScore AS s " 
				+ " WHERE s.profUser=:rProfUser AND s.profUser.finished= true "
				+ (LDimensionID == null ? "": " AND s.question.dimension.id=:rDimensionID")
				)
				;
		q.setParameter("rProfUser", profUser);
		if(LDimensionID != null){
			q.setString("rDimensionID", LDimensionID);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	@Override
	public double getAverageScoreByDimensionStyleGroup(ProfileUser profUser, String StyleGroup){
		if(profUser==null)
			return -1;
		Query q =  session.createQuery("SELECT avg(s.score) FROM QuestionScore AS s " 
				+ " WHERE s.profUser=:rProfUser AND s.profUser.finished= true  "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("rProfUser", profUser);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	@Override
	public long getCountScoreByDimensionStyleGroup(ProfileUser profUser, String StyleGroup){
		if(profUser==null)
			return -1;
		Query q =  session.createQuery("SELECT count(score) FROM QuestionScore AS s " 
				+ " WHERE s.profUser=:rProfUser AND s.profUser.finished= true  "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("rProfUser", profUser);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Long) r;
		
	}
	@Override
	public double getSTDEVScoreByDimensionStyleGroup(ProfileUser profUser, String StyleGroup){
		if(profUser==null)
			return -1;
		Query q =  session.createQuery("SELECT sqrt((sum(score * score)/count(score)) - (avg(score) * avg(score))) FROM QuestionScore AS s " 
				+ " WHERE s.profUser=:rProfUser AND s.profUser.finished= true "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("rProfUser", profUser);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	
	
	@Override
	public double getAverageNormsByDimension(Project proj, String LDimensionID){
		
		Query q =  session.createQuery("SELECT avg(s.score) FROM QuestionScore AS s " 
				+ " WHERE s.profUser.finished= true "
				+ (proj == null ? "": " AND s.profUser.profile.project=:rProj")
				+ (LDimensionID == null ? "": " AND s.question.dimension.id=:rDimensionID")
				)
				;
		if(proj != null)
			q.setParameter("rProj", proj);
		if(LDimensionID != null){
			q.setString("rDimensionID", LDimensionID);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return 0;
		return (Double) r;
		
	}
	
	@Override
	public double getSTDEVNormsByDimension(Project proj, String LDimensionID){
		Query q =  session.createQuery("SELECT sqrt((sum(score * score)/count(score)) - (avg(score) * avg(score))) FROM QuestionScore AS s " 
				+ " WHERE s.profUser.finished= true "
				+ (proj == null ? "": " AND s.profUser.profile.project=:rProj")
				+ (LDimensionID == null ? "": " AND s.question.dimension.id=:rDimensionID")
				)
				;
		if(proj != null)
			q.setParameter("rProj", proj);
		if(LDimensionID != null){
			q.setString("rDimensionID", LDimensionID);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return 0;
		return (Double) r;
		
	}
	
	@Override
	public double getAverageNormsByDimensionStyleGroup(Project proj, String StyleGroup){
		
		Query q =  session.createQuery("SELECT avg(s.score) FROM QuestionScore AS s " 
				+ " WHERE s.profUser.finished= true "
				+ (proj == null ? "": " AND s.profUser.profile.project=:rProj")
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
		Query q =  session.createQuery("SELECT sqrt((sum(score * score)/count(score)) - (avg(score) * avg(score))) FROM QuestionScore AS s " 
				+ " WHERE s.profUser.finished= true "
				+ (proj == null ? "": " AND s.profUser.profile.project=:rProj")
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
	public List<QuestionScore> getScoresByDimension(ProfileUser profUser, String LDimensionID){
		if(profUser==null)
			return new ArrayList<QuestionScore>();
		Query q =  session.createQuery("SELECT s.score FROM QuestionScore AS s " 
				+ " WHERE s.profUser=:rProfUser "
				+ (LDimensionID == null ? "": " AND s.question.dimension.id=:rDimensionID")
				)
				;
		q.setParameter("rProfUser", profUser);
		if(LDimensionID != null){
			q.setString("rDimensionID", LDimensionID);
		}
		return q.list();
	}
	

	
	@Override
	public boolean isGroupBeingUse(Group group){
		Query q = session.createQuery("SELECT COUNT(a.id) FROM Profiling AS a " +
				" WHERE a.group = :group "  )
				.setParameter("group", group);
		long size = (Long) q.uniqueResult();
		if(size==0)
			return false;
		
		return true;
	}

	@Override
	public List<LDimension> getAllLDimensions(){
		Query q =  session.createQuery("SELECT a FROM LDimension AS a "
				+ " WHERE active=true "
				+ " ORDER BY a.orderNum ASC "
				)
				;
		
		return q.list();
	}
	@Override
	public List<LDimension> getLDimensionsByStyleGroup(String styleGroup){
		Query q =  session.createQuery("SELECT a FROM LDimension AS a "
				+ " WHERE active=true "
				+ " AND a.styleGroup=:styleGroup"
				+ " ORDER BY a.orderNum ASC "
				)
				.setString("styleGroup", styleGroup);
				;
		
		return q.list();
	}
	@Override
	public void addLDimension(LDimension d) {
		session.saveOrUpdate(d);
	}
	@Override
	public void updateLDimension(LDimension d) {
		session.merge(d);
	}

	@Override
	public List<ProfileUser> getExpiredProfUsers() {
		Calendar expiredTime = Calendar.getInstance();
		expiredTime.add(Calendar.MINUTE, (-1)* ProfileUser.VALID_MINUTE_PER_QUESTION);
		
		Query q = session.createQuery("SELECT pu FROM ProfileUser pu  " 
				+ " WHERE pu.lastAssessTime < :expireTime "
				+ " AND NOT(pu.finished = true) "
				+ " AND pu.lastQuestionNum > 0"
				)
				.setTimestamp("expireTime", expiredTime.getTime());
		return q.list();
	}

	@Override
	public List<User> getNotSubmitedUsersByProfiling(Profiling prof) {
		Query q = session.createQuery("SELECT pu.user FROM ProjUser pu  " 
				+ " WHERE pu.project = :rProject "
				+ " AND pu.user NOT IN (SELECT pfu.assessor FROM ProfileUser pfu" +
						" WHERE pfu.profile = :rProfiling" +
						" AND pfu.finished = true ) "
				+ " "
				)
				.setParameter("rProject", prof.getProject())
				.setParameter("rProfiling", prof);
				
		return q.list();
	}

	@Override
	public List<User> getNotAssessedUsersByProfiling(Profiling prof) {
		Query q = session.createQuery("SELECT pu.user FROM ProjUser pu  " 
				+ " WHERE pu.project = :rProject "
				+ " AND pu.user NOT IN (SELECT pfu.assessee FROM ProfileUser pfu" +
						" WHERE pfu.profile = :rProfiling" +
						" AND pfu.finished = true ) "
				+ " "
				)
				.setParameter("rProject", prof.getProject())
				.setParameter("rProfiling", prof);
		
		
		return q.list();
	}


	@Override
	public List<Profiling> getProfsToSendReminder() {
		Date today = new Date();
		Calendar next8days = Calendar.getInstance();
		next8days.add(Calendar.DAY_OF_YEAR, 8);
		Query q = session.createQuery("SELECT p FROM Profiling AS p " +
				" WHERE p.edate >= :today AND p.edate <= :next8days "  )
				.setDate("next8days", next8days.getTime())
				.setDate("today", today);
		
		
		//HQL has no standard way to obtain Date Different
		//		"   AND (day(t.endDate) - day(current_date())) in elements(t.daysToRemind) "); 
		List<Profiling> objList = q.list();
		for(int i = objList.size()-1; i>=0; i--){
			Profiling e = (Profiling) objList.get(i);
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
