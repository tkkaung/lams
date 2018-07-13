package ntu.celt.eUreka2.modules.assessment;

import java.util.ArrayList;
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
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;

public class AssessmentDAOImpl implements AssessmentDAO {
	final String KEYWORD_PLACE_TOP = "NBS Accreditation Office";
	
	@Inject
	private Session session;
	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_ASSESSMENT;
	}
	
	@Override
	public void addRubric(Rubric r) {
		session.save(r);
	}
	@Override
	public void updateRubric(Rubric r) {
		session.merge(r);
	}
	@Override
	public void deleteRubricOpenQuestion(RubricOpenQuestion rq){
		session.delete(rq);
	}
	
	
	@Override
	public void deleteRubric(Rubric r) {
		r.getOpenEndedQuestions().clear();
		session.delete(r);
	}
	@Override
	public void immediateAddRubric(Rubric r) {
		session.save(r);
	}

	@Override
	public Rubric getRubricById(int id) {
		return (Rubric) session.get(Rubric.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Rubric> getRubricsByOwner(User u) {
		Query q = session.createQuery("SELECT r FROM Rubric AS r WHERE r.owner = :rUser "
				+ " ORDER BY r.name ASC ")
				.setParameter("rUser", u);
		return q.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Rubric> getSharedRubrics() {
		Query q = session.createQuery("SELECT r FROM Rubric AS r WHERE r.shared = true ");
		return q.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Rubric> getMasterRubrics() {
		Query q = session.createQuery("SELECT r FROM Rubric AS r WHERE r.master = true ORDER BY r.name ASC ");
		return q.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Rubric> getMasterRubricsAllSchool() {
		
		List<Rubric> list = new ArrayList<Rubric>();
		
		Query q1 = session.createQuery("SELECT r FROM Rubric AS r " 
				+" WHERE r.master = true "
				+" AND r.school IS NULL AND r.name LIKE '%"+ KEYWORD_PLACE_TOP +"%'"
				+ " ORDER BY r.name ASC "
				);
		List<Rubric> list1 =  q1.list();
		
		if(list1 !=null)
			list.addAll(list1);
		
		Query q2 = session.createQuery("SELECT r FROM Rubric AS r " 
				+" WHERE r.master = true "
				+" AND r.school IS NULL AND NOT( r.name LIKE '%"+ KEYWORD_PLACE_TOP +"%')"
				+ " ORDER BY r.name ASC "
				);
		List<Rubric> list2 =  q2.list();
		if(list2 !=null)
			list.addAll(list2);
		return list;
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Rubric> getMasterRubrics(School school) {
		if(school==null)
			return new ArrayList<Rubric>();
		
		
		List<Rubric> list = new ArrayList<Rubric>();
		
		Query q1 = session.createQuery("SELECT r FROM Rubric AS r " 
				+" WHERE r.master = true "
				+" AND r.school = :rSchool AND r.name LIKE '%"+ KEYWORD_PLACE_TOP +"%'"
				+ " ORDER BY r.name ASC "
				).setParameter("rSchool", school);
		List<Rubric> list1 =  q1.list();
		
		if(list1 !=null)
			list.addAll(list1);
		
		Query q2 = session.createQuery("SELECT r FROM Rubric AS r " 
				+" WHERE r.master = true "
				+" AND r.school = :rSchool AND NOT( r.name LIKE '%"+ KEYWORD_PLACE_TOP +"%')"
				+ " ORDER BY r.name ASC "
				).setParameter("rSchool", school);
		List<Rubric> list2 =  q2.list();
		if(list2 !=null)
			list.addAll(list2);
		return list;
		
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Rubric> searchRubrics(String searchText, User owner, Boolean shared, Boolean master, School school) {
		Criteria crit = session.createCriteria(Rubric.class);
		
		if(searchText != null && ! "".equals(searchText)){
			String[] words = searchText.split(" ");
			for(String word : words){
				crit = crit.add(Restrictions.or(Restrictions.like("name", "%"+word+"%"),
						Restrictions.like("des", "%"+word+"%" )));
			}
		}
		if(owner!=null){
			crit = crit.add(Restrictions.eq("owner", owner));
		}
		if(shared!=null){
			crit = crit.add(Restrictions.eq("shared", shared));
		}
		if(master!=null){
			crit = crit.add(Restrictions.eq("master", master));
		}
		if(school!=null){
			crit = crit.add(Restrictions.eq("school", school));
			
		}
		crit.addOrder(Order.desc("mdate"));
		
		
		return crit.list();
	}
	@Override
	public List<Rubric> searchRubricsViaAssmtBySchool(School school) {
		Set<Rubric> rSet = new HashSet<Rubric>();
		Query q = session.createQuery("SELECT a FROM Assessment AS a " 
				+" WHERE a.project.school=:rSchool "
				)
				.setParameter("rSchool", school);

		List<Assessment> aList = q.list();
		for( Assessment assmt : aList){
			if(assmt.getRubric()!= null)
				rSet.add(assmt.getRubric());
		}
		
		return new ArrayList<Rubric>(rSet);
	}
	@Override
	public List<Rubric> searchRubricsViaEvalBySchool(School school) {
		Set<Rubric> rSet = new HashSet<Rubric>();
		Query q = session.createQuery("SELECT a FROM Evaluation AS a " 
				+" WHERE a.project.school=:rSchool "
				)
				.setParameter("rSchool", school);

		List<Evaluation> aList = q.list();
		for( Evaluation eval : aList){
			if(eval.getRubric()!= null)
				rSet.add(eval.getRubric());
		}
		
		return new ArrayList<Rubric>(rSet);
	}
	
	
	
	@Override
	public void addAssessment(Assessment assess) {
		session.save(assess);
	}
	@Override
	public void updateAssessment(Assessment assess) {
		session.merge(assess);
	}
	@Override
	public void deleteAssessment(Assessment assess) {
		assess.getAssmtUsers().clear();
		assess.getCriterias().clear();
		session.delete(assess);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Assessment> getAssessmentsByProject(Project p) {
		Criteria crit = session.createCriteria(Assessment.class)
					.add(Restrictions.eq("project.id", p.getId()))
					.addOrder(Order.asc("orderNumber"))
					;
				
		return crit.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Assessment> getVisibleAssessments(Project p) {
		Query q = session.createQuery("SELECT a FROM Assessment AS a" 
				+ " WHERE project.id = :rPid "
				+ " AND (allowViewGradeCriteria = true OR allowViewScoredCriteria = true " +
						" OR allowViewGrade = true OR allowViewGradeDetail = true " +
						" OR allowSubmitFile = true OR allowViewComment = true)"
				+ " ORDER BY orderNumber ASC"
				)
				.setString("rPid", p.getId());
		return q.list();
	}
	
	
	@Override
	public Assessment getAssessmentById(int id) {
		return (Assessment) session.get(Assessment.class, id);
	}
	@Override
	public AssessCriterion getAssessCriterionById(long id) {
		return (AssessCriterion) session.get(AssessCriterion.class, id);
	}
	@Override
	public AssessCriteria getAssessCriteriaById(int id) {
		return (AssessCriteria) session.get(AssessCriteria.class, id);
	}
	
	@Override
	public AssessmentUser getAssessmentUserById(long id) {
		return (AssessmentUser) session.get(AssessmentUser.class, id);
	}
	

	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		//Decided no need to have this
		return null;
	}

	@Override
	public int getNextOrderNumber(Project project) {
		Query q = session.createQuery("SELECT MAX(orderNumber) FROM Assessment " +
				" WHERE project.id = :rPid ")
				.setString("rPid", project.getId());
		if(q.uniqueResult()==null)
			return 1;
		
		int num = (Integer) q.uniqueResult();
		
		return num+1;
	}

	@Override
	public void reorderAssessmentNumber(Project p) {
		List<Assessment> assmtList = this.getAssessmentsByProject(p);
		int i = 1;
		for(Assessment assmt : assmtList){
			assmt.setOrderNumber(i);
			i++;
			this.updateAssessment(assmt);
		}
	}

	@Override
	public long countAssessmentByRubric(Rubric rubric) {
		Query q =  session.createQuery("SELECT COUNT(a.id) FROM Assessment AS a " +
				" WHERE a.rubric=:rRubric " )
				.setParameter("rRubric", rubric);
		
		return  (Long) q.uniqueResult();
	}
	@Override
	public long countEvaluationByRubric(Rubric rubric) {
		Query q =  session.createQuery("SELECT COUNT(a.id) FROM Evaluation AS a " +
				" WHERE a.rubric=:rRubric " )
				.setParameter("rRubric", rubric);
		
		return  (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List getAssessmentsByRubric(Rubric rubric) {
		Query q =  session.createQuery("SELECT a FROM Assessment AS a " +
				" WHERE a.rubric=:rRubric " )
				.setParameter("rRubric", rubric);
		
		return  q.list();
	}

	@Override
	public long countAssessmentsByProject(Project proj) {
		Query q =  session.createQuery("SELECT COUNT(e.id) FROM Assessment AS e " +
			" WHERE e.project=:rProject " )
			.setParameter("rProject", proj);

		return  (Long) q.uniqueResult();		
	}

	@Override
	public void saveAssessCriterion(AssessCriterion acrion) {
		session.save(acrion);
	}

	@Override
	public void saveAssessmentUser(AssessmentUser assmtUser) {
		session.save(assmtUser);
	}

	@Override
	public List<Assessment> searchAssessment(String searchName, School searchSchool, String searchTerm, String searchCourseCode) {
		return searchAssessment(searchName, searchSchool, searchTerm, searchCourseCode, null);
	}
	@Override
	public List<Assessment> searchAssessment(String searchName, School searchSchool, String searchTerm, String searchCourseCode, Integer searchRubricID) {
		return searchAssessment(searchName, searchSchool, searchTerm, searchCourseCode, searchRubricID, null);
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Assessment> searchAssessment(String searchName, School searchSchool, String searchTerm, String searchCourseCode, Integer searchRubricID, String projName) {
		if(searchName==null)
			searchName = "";
		
		Query q =  session.createQuery("SELECT a FROM Assessment AS a " 
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
	public AssmtFile getAssmtFileById(String id) {
		return (AssmtFile) session.get(AssmtFile.class, id);
	}

	@Override
	public AssmtInstructorFile getAssmtInstructorFileById(String id) {
		return (AssmtInstructorFile) session.get(AssmtInstructorFile.class, id);
	}

	
	public boolean isGroupBeingUse(Group group){
		Query q = session.createQuery("SELECT a FROM Assessment AS a " +
				" WHERE a.group = :group "  )
				.setParameter("group", group);
		List<Assessment> aList = q.list();
		if(aList.size()==0)
			return false;
		
		return true;
	}

	@Override
	public RubricOpenQuestion getRubricOpenQuestion(long id) {
		if(id==0)
			return null;
		return (RubricOpenQuestion) session.get(RubricOpenQuestion.class, id);
	}
	
}
