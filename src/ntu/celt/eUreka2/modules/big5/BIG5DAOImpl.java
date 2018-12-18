package ntu.celt.eUreka2.modules.big5;

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

public class BIG5DAOImpl implements BIG5DAO {
	
	@Inject
	private Session session;
	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_BIG5_PERSONALITY_SURVEY;
	}
	
	@Override
	public void addBQuestionSet(BQuestionSet r) {
		session.save(r);
	}
	@Override
	public void updateBQuestionSet(BQuestionSet r) {
		session.merge(r);
	}
	
	
	
	@Override
	public void deleteBQuestionSet(BQuestionSet r) {
		session.delete(r);
	}
	@Override
	public void immediateAddBQuestionSet(BQuestionSet r) {
		session.save(r);
	}

	@Override
	public BQuestionSet getBQuestionSetById(long id) {
		return (BQuestionSet) session.get(BQuestionSet.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BQuestionSet> getBQuestionSetsByOwner(User u) {
		Query q = session.createQuery("SELECT r FROM BQuestionSet AS r WHERE r.owner = :rUser "
				+ " ORDER BY r.name ASC ")
				.setParameter("rUser", u);
		return q.list();
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<BQuestionSet> searchBQuestionSets(String searchText, User owner, School school) {
		Criteria crit = session.createCriteria(BQuestionSet.class);
		
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
	public List<BQuestionSet> searchBQuestionSets(Project proj) {
		Set<BQuestionSet> rSet = new HashSet<BQuestionSet>();
		Query q = session.createQuery("SELECT a FROM BIG5Survey AS a " 
				+" WHERE a.project=:rProject "
				)
				.setParameter("rProject", proj);

		List<BIG5Survey> aList = q.list();
		for( BIG5Survey ipsp : aList){
			if(ipsp.getQuestionSet()!= null)
				rSet.add(ipsp.getQuestionSet());
		}
		
		return new ArrayList<BQuestionSet>(rSet);
		
	}
	
	
	
	@Override
	public void addBIG5Survey(BIG5Survey big5) {
		session.save(big5);
	}
	@Override
	public void updateBIG5Survey(BIG5Survey big5) {
		session.merge(big5);
	}
	@Override
	public void deleteBIG5Survey(BIG5Survey big5) {
		big5.getBig5Users().clear();
		big5.getQuestions().clear();
		session.delete(big5);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BIG5Survey> getBIG5SurveysByProject(Project p) {
		Criteria crit = session.createCriteria(BIG5Survey.class)
					.add(Restrictions.eq("project.id", p.getId()))
					.addOrder(Order.asc("orderNumber"))
					;
				
		return crit.list();
	}
	
	@Override
	public BQuestion getBQuestionById(long id) {
		return (BQuestion) session.get(BQuestion.class, id);
	}
	
	
	
	@Override
	public BIG5Survey getBIG5SurveyById(long id) {
		return (BIG5Survey) session.get(BIG5Survey.class, id);
	}
	@Override
	public BIG5Question getBIGQuestionById(long id) {
		return (BIG5Question) session.get(BIG5Question.class, id);
	}
	
	@Override
	public BIG5SurveyUser getBIG5SurveyUserById(long id) {
		return (BIG5SurveyUser) session.get(BIG5SurveyUser.class, id);
	}
	

	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		//Decided no need to have this
		return null;
	}

	@Override
	public List<BIG5SurveyUser> getBIG5SurveyUsersByProject(Project proj) {
		
		Query q = session.createQuery("SELECT pu FROM BIG5SurveyUser as pu WHERE pu.survey.project=:rProj" +
				" ORDER BY assessee.username ASC, assessor.username ASC ")
				.setParameter("rProj", proj)
				;
	
		return q.list();
	}


	@Override
	public long countBIG5SurveyByBQuestionSet(BQuestionSet qset) {
		Query q =  session.createQuery("SELECT COUNT(a.id) FROM BIG5Survey AS a " +
				" WHERE a.questionSet=:rQuestionSet " )
				.setParameter("rQuestionSet", qset);
		
		return  (Long) q.uniqueResult();
	}
	
	@Override
	public List getBIG5SurveysByBQuestionSet(BQuestionSet qset) {
		Query q =  session.createQuery("SELECT a FROM BIG5Survey AS a " +
				" WHERE a.questionSet=:rQuestionSet " )
				.setParameter("rQuestionSet", qset);
		
		return  q.list();
	}

	


	@Override
	public void saveBIG5SurveyUser(BIG5SurveyUser big5User) {
		session.save(big5User);
	}

	
	@Override
	public List<BIG5Survey> searchBIG5Survey(String searchName, School searchSchool, String searchTerm, String searchCourseCode) {
		if(searchName==null)
			searchName = "";
		
		Query q =  session.createQuery("SELECT a FROM BIG5Survey AS a " 
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
	public List<BIG5SurveyUser> searchBIG5SurveyUser(BIG5Survey big5, User Assessor, User Assessee,
			Boolean finished) {
		
		Query q =  session.createQuery("SELECT a FROM BIG5SurveyUser AS a " 
				+ " WHERE 1=1 "
				+ ((big5!=null)? " AND a.survey= :rBIG5Survey " : "")
				+ ((Assessor!=null) ? " AND a.assessor = :rAssessor" : "")
				+ (Assessee!=null? " AND a.assessee=:rAssessee" : "")
				+ ((finished!=null)? " AND a.finished=:rFinished " : "")
				+ "  "
				)
				;
		if (big5!=null){
			q.setParameter("rBIG5Survey", big5);
		}else{
		//	return new ArrayList<BIG5SurveyUser>();
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
	public double getAverageScoreByDimension(BIG5SurveyUser big5User, Integer BDimensionID){
		if(big5User==null)
			return -1;
		Query q =  session.createQuery("SELECT avg(s.score"+BDimensionID+") FROM BQuestionScore AS s " 
				+ " WHERE s.big5User=:rBig5User AND s.big5User.finished= true  "
				+ " AND s.score"+BDimensionID + ">0"
				)
				;
		q.setParameter("rBig5User", big5User);
		Object r = q.uniqueResult();
		if(r==null)
			return -1.0;
		return (Double) r;
		
	}
	
	@Override
	public long getCountScoreByDimension(BIG5SurveyUser big5User, Integer BDimensionID){
		if(big5User==null)
			return -1;
		Query q =  session.createQuery("SELECT count(score"+BDimensionID+") FROM BQuestionScore AS s " 
				+ " WHERE s.big5User=:rbig5User AND s.big5User.finished= true "
				+  " AND score"+BDimensionID + ">0"
				)
				;
		q.setParameter("rbig5User", big5User);
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Long) r;
		
	}
	@Override
	public double getSTDEVScoreByDimension(BIG5SurveyUser big5User, Integer BDimensionID){
		if(big5User==null)
			return -1;
		Query q =  session.createQuery("SELECT sqrt((sum(score"+BDimensionID+" * score"+BDimensionID+")/count(score"+BDimensionID+"))" +
				" - (avg(score"+BDimensionID+") * avg(score"+BDimensionID+"))) FROM BQuestionScore AS s " 
				+ " WHERE s.big5User=:rbig5User AND s.big5User.finished= true "
				+ " AND score"+BDimensionID+">0"
				
				)
				;
		q.setParameter("rbig5User", big5User);
		
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	@Override
	public double getAverageScoreByDimensionStyleGroup(BIG5SurveyUser big5User, String StyleGroup){
		if(big5User==null)
			return -1;
		Query q =  session.createQuery("SELECT avg(s.score) FROM BQuestionScore AS s " 
				+ " WHERE s.big5User=:rbig5User AND s.big5User.finished= true  "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("rbig5User", big5User);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	@Override
	public long getCountScoreByDimensionStyleGroup(BIG5SurveyUser big5User, String StyleGroup){
		if(big5User==null)
			return -1;
		Query q =  session.createQuery("SELECT count(score) FROM BQuestionScore AS s " 
				+ " WHERE s.big5User=:rbig5User AND s.big5User.finished= true  "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("rbig5User", big5User);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Long) r;
		
	}
	@Override
	public double getSTDEVScoreByDimensionStyleGroup(BIG5SurveyUser big5User, String StyleGroup){
		if(big5User==null)
			return -1;
		Query q =  session.createQuery("SELECT sqrt((sum(score * score)/count(score)) - (avg(score) * avg(score))) FROM BQuestionScore AS s " 
				+ " WHERE s.big5User=:rbig5User AND s.big5User.finished= true "
				+ (StyleGroup == null ? "": " AND s.question.dimension.styleGroup=:rStyleGroup")
				)
				;
		q.setParameter("rbig5User", big5User);
		if(StyleGroup != null){
			q.setString("rStyleGroup", StyleGroup);
		}
		Object r = q.uniqueResult();
		if(r==null)
			return -1;
		return (Double) r;
		
	}
	
	
	
	@Override
	public double getAverageNormsByDimension(Project proj, Integer BDimensionID){
		String sql = "SELECT avg(su.score"+BDimensionID+") FROM BIG5SurveyUser AS su " 
			+ " WHERE su.finished= true "
			+ (proj == null ? "": " AND su.survey.project=:rProj ")
			+ " AND  su.score"+BDimensionID+">0"
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
	public double getSTDEVNormsByDimension(Project proj, Integer BDimensionID){
		Query q =  session.createQuery("SELECT sqrt((sum(score"+BDimensionID+" * score"+BDimensionID+")/count(score"+BDimensionID+")) " +
				" - (avg(score"+BDimensionID+") * avg(score"+BDimensionID+"))) FROM BIG5SurveyUser AS su " 
				+ " WHERE su.finished= true "
				+ (proj == null ? "": " AND su.survey.project=:rProj")
				+ " AND  su.score"+BDimensionID+">0"
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
		
		Query q =  session.createQuery("SELECT avg(s.score) FROM BQuestionScore AS s " 
				+ " WHERE s.big5User.finished= true "
				+ (proj == null ? "": " AND s.big5User.survey.project=:rProj")
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
		Query q =  session.createQuery("SELECT sqrt((sum(score * score)/count(score)) - (avg(score) * avg(score))) FROM BQuestionScore AS s " 
				+ " WHERE s.big5User.finished= true "
				+ (proj == null ? "": " AND s.big5User.survey.project=:rProj")
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
	public List<BQuestionScore> getScoresByDimension(BIG5SurveyUser big5User, Integer BDimensionID){
		if(big5User==null)
			return new ArrayList<BQuestionScore>();
		Query q =  session.createQuery("SELECT s.score"+BDimensionID+" FROM BQuestionScore AS s " 
				+ " WHERE s.big5User=:rbig5User "
				+ " AND s.score"+BDimensionID + ">0"
				)
				;
		q.setParameter("rbig5User", big5User);
		return q.list();
	}
	

	
	@Override
	public boolean isGroupBeingUse(Group group){
		Query q = session.createQuery("SELECT COUNT(a.id) FROM BIG5Survey AS a " +
				" WHERE a.group = :group "  )
				.setParameter("group", group);
		long size = (Long) q.uniqueResult();
		if(size==0)
			return false;
		
		return true;
	}

	

	@Override
	public List<BIG5SurveyUser> getExpiredBIG5SurveyUsers() {
		Calendar expiredTime = Calendar.getInstance();
		expiredTime.add(Calendar.MINUTE, (-1)* BIG5SurveyUser.VALID_MINUTE_PER_QUESTION);
		
		Query q = session.createQuery("SELECT pu FROM BIG5SurveyUser pu  " 
				+ " WHERE pu.lastAssessTime < :expireTime "
				+ " AND NOT(pu.finished = true) "
				+ " AND pu.lastQuestionNum > 0"
				)
				.setTimestamp("expireTime", expiredTime.getTime());
		return q.list();
	}

	@Override
	public List<User> getNotSubmitedUsersByBIG5Survey(BIG5Survey big5) {
		Query q = session.createQuery("SELECT pu.user FROM ProjUser pu  " 
				+ " WHERE pu.project = :rProject "
				+ " AND pu.user NOT IN (SELECT pfu.assessor FROM BIG5SurveyUser pfu" +
						" WHERE pfu.survey = :rbig5" +
						" AND pfu.finished = true ) "
				+ " "
				)
				.setParameter("rProject", big5.getProject())
				.setParameter("rbig5", big5);
				
		return q.list();
	}

	@Override
	public List<User> getNotAssessedUsersByBIG5Survey(BIG5Survey big5) {
		Query q = session.createQuery("SELECT pu.user FROM ProjUser pu  " 
				+ " WHERE pu.project = :rProject "
				+ " AND pu.user NOT IN (SELECT pfu.assessee FROM BIG5SurveyUser pfu" +
						" WHERE pfu.survey = :rbig5iling" +
						" AND pfu.finished = true ) "
				+ " "
				)
				.setParameter("rProject", big5.getProject())
				.setParameter("rbig5iling", big5);
		
		
		return q.list();
	}


	@Override
	public List<BIG5Survey> getBIG5sToSendReminder() {
		Date today = new Date();
		Calendar next8days = Calendar.getInstance();
		next8days.add(Calendar.DAY_OF_YEAR, 8);
		Query q = session.createQuery("SELECT p FROM BIG5Survey AS p " +
				" WHERE p.edate >= :today AND p.edate <= :next8days "  )
				.setDate("next8days", next8days.getTime())
				.setDate("today", today);
		
		
		//HQL has no standard way to obtain Date Different
		//		"   AND (day(t.endDate) - day(current_date())) in elements(t.daysToRemind) "); 
		List<BIG5Survey> objList = q.list();
		for(int i = objList.size()-1; i>=0; i--){
			BIG5Survey e = (BIG5Survey) objList.get(i);
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
	public List<BDimension> getAllBDimensions(){
		Query q =  session.createQuery("SELECT a FROM BDimension AS a "
				+ " WHERE active=true "
				+ " ORDER BY a.orderNum ASC "
				)
				;
		
		return q.list();
	}
	
	@Override
	public void addBDimension(BDimension r) {
		session.save(r);
	}
	@Override
	public void updateBDimension(BDimension r) {
		session.merge(r);
	}

}
