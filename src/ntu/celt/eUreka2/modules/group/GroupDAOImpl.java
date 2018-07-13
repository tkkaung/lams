package ntu.celt.eUreka2.modules.group;

import java.util.List;

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import sun.awt.PeerEvent;

import ntu.celt.eUreka2.data.LastUpdateItem;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;

public class GroupDAOImpl implements GroupDAO {
	@Inject
    private Session session;
	@SuppressWarnings("unused")
	private Messages messages;
	@Inject
	private EvaluationDAO evaluationDAO;
	@Inject
	private AssessmentDAO assmtDAO;
	
	public GroupDAOImpl(Session session, Messages messages, EvaluationDAO evaluationDAO, AssessmentDAO assmtDAO) {
		super();
		this.session = session;
		this.messages = messages;
		this.evaluationDAO = evaluationDAO;
		this.assmtDAO = assmtDAO;
	}
	
	@Override
	public String getModuleName() {
		return "Group";
	}

	@Override
	public void deleteGroup(Group group) {
		Query q = session.createQuery("UPDATE Assessment a SET a.group = null " 
				+ " WHERE a.group = :rGroup "
				)
				.setParameter("rGroup", group);
		q.executeUpdate();
		
		Query q2 = session.createQuery("UPDATE Evaluation a SET a.group = null " 
				+ " WHERE a.group = :rGroup "
				)
				.setParameter("rGroup", group);
		q2.executeUpdate();
	
		session.delete(group);		
	}

	@Override
	public Group getGroupById(Long id) {
		if(id==null) return null;
		return (Group) session.get(Group.class, id);
	}

	@Override
	public Group getGroupByBBID(String bbID) {
		if(bbID==null)
			return null;
		Criteria c = session.createCriteria(Group.class)
					.add(Restrictions.eq("bbID", bbID))
					.setMaxResults(1)
					;
		
		return (Group) c.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Group> getGroupsByProject(Project project) {
		Criteria crit = session.createCriteria(Group.class)
		.add(Restrictions.eq("project.id", project.getId()));
	return crit.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Group> getAllGroups() {
		List<Group> groups = session.createQuery("FROM Group " 
				+ " ORDER BY mdate DESC ")
				.list();
		return groups;
	}

	@Override
	public void saveGroup(Group group) {
		session.persist(group);
	}

	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser,
			int numPastDay) {
		return null;
	}

	@Override
	public GroupUser getGroupUserById(Long id) {
		if(id==null) return null;
		return (GroupUser) session.get(GroupUser.class, id);
	}

	@Override
	public void saveGroupUser(GroupUser groupUser) {
		session.persist(groupUser);		
	}
	@Override
	public void immediateSaveGroupUser(GroupUser groupUser) {
		session.persist(groupUser);	
		session.flush();
	}
	
	@Override
	public boolean isGroupBeingUsePE(Group group){
		boolean inUseByPeerEvaluation = evaluationDAO.isGroupBeingUse(group);
		
		return inUseByPeerEvaluation;
	}
	@Override
	public boolean isGroupBeingUseAS(Group group){
		boolean inUseByAssmt = assmtDAO.isGroupBeingUse(group);
		
		return inUseByAssmt;
	}


}
