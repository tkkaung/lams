package ntu.celt.eUreka2.modules.budget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ntu.celt.eUreka2.data.LastUpdateItem;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.pages.modules.budget.BudgetIndex;

public class BudgetDAOImpl implements BudgetDAO{
	@Inject
    private Session session;
	private PageRenderLinkSource linkSource;
	
	
	public BudgetDAOImpl(Session session, PageRenderLinkSource linkSource) {
		super();
		this.session = session;
		this.linkSource = linkSource;
	}

	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_BUDGET;
	}
	
	@Override
	public void deleteBudget(Budget budget) {
		budget.getTransactions().clear();
		session.delete(budget);
	}
	@Override
	public void deleteTransaction(Transaction transaction) {
		session.delete(transaction);
	}
	@Override
	public Budget getActiveBudget(Project project) {
		Criteria crit = session.createCriteria(Budget.class)
						.add(Restrictions.eq("active", true))
						.add(Restrictions.eq("project.id", project.getId()))
						;
		return (Budget) crit.uniqueResult();
	}
	@Override
	public Budget getBudgetById(Long id) {
		if(id==null) return null;
		return (Budget) session.get(Budget.class, id);
	}
	@Override
	public Transaction getTransactionById(Long id) {
		return (Transaction) session.get(Transaction.class, id);
	}
	@Override
	public void saveBudget(Budget budget) {
		session.persist(budget);
	}
	@Override
	public void saveTransaction(Transaction transaction) {
		session.persist(transaction);
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getTransactionCategories(Project project) {
		Query q = session.createQuery("SELECT DISTINCT t.category " 
				+ " FROM Transaction as t " 
				+ " WHERE t.budget.project.id = :rPid "
				+ " AND t.budget.active = true "
				+ " AND t.category IS NOT NULL"
				+ " ORDER BY t.category ASC ")
				.setString("rPid", project.getId());
		
		return q.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<User> getTransactionUsers(Project project) {
		Query q = session.createQuery(
				"FROM User a1 WHERE a1.id IN ("
					+ " SELECT DISTINCT t.creator.id " 
					+ " FROM Transaction as t "
					+ " WHERE t.budget.project.id = :rPid "
					+ " AND t.budget.active = true "
				+" )"
				)
				.setString("rPid", project.getId());
		
		return q.list();
	}
	@Override
	public long getCountTransactionsByType(Project proj, TransactionType tType) {
		Query q = session.createQuery("SELECT COUNT(t) " 
				+ " FROM Transaction AS t " 
				+ " WHERE t.budget.project.id = :rPid "
				+ " AND t.budget.active = true "
				+ " AND t.type = :rType "
				)
				.setString("rPid", proj.getId())
				.setParameter("rType", tType)
				;
		
		return (Long) q.uniqueResult();
	}
	@Override
	public double getTotalTransactionsByType(Project proj, TransactionType tType) {
		Query q = session.createQuery("SELECT SUM(t.amount) " 
				+ " FROM Transaction AS t " 
				+ " WHERE t.budget.project.id = :rPid "
				+ " AND t.budget.active = true "
				+ " AND t.type = :rType "
				)
				.setString("rPid", proj.getId())
				.setParameter("rType", tType)
				;
		Object result = q.uniqueResult();
		if(result==null)
			return 0;
		return (Double) result;
	}
	@Override
	public double getTotalTransactionsByTypeAndStatus(Project proj, TransactionType tType, TransactionStatus tStatus) {
		Query q = session.createQuery("SELECT SUM(t.amount) " 
				+ " FROM Transaction AS t " 
				+ " WHERE t.budget.project.id = :rPid "
				+ " AND t.budget.active = true "
				+ " AND t.type = :rType "
				+ " AND t.status = :rStatus "
				)
				.setString("rPid", proj.getId())
				.setParameter("rType", tType)
				.setParameter("rStatus", tStatus)
				;
		Object result = q.uniqueResult();
		if(result==null)
			return 0;
		return (Double) result;
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_YEAR, -1 * numPastDay);
		
		Query q = session.createQuery("SELECT t FROM Transaction AS t " +
				" WHERE t.budget.project.id=:rPid " +
				"   AND t.budget.active = true  " +
				"   AND t.modifyDate>=:rPastDay " +
				"   ORDER BY modifyDate DESC" +
				" 	 ")
				.setString("rPid", project.getId())
				.setDate("rPastDay", pastDay.getTime())
				;
		
		List<Transaction> tList = q.list();
		List<LastUpdateItem> lList = new ArrayList<LastUpdateItem>();
		for(Transaction t : tList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(t.getModifyDate());
			l.setTitle(t.getDescription());
			l.setUrl(linkSource.createPageRenderLinkWithContext(BudgetIndex.class
					, t.getBudget().getId())
					.toAbsoluteURI());
			
			lList.add(l);
		}
		
		return lList;
	}

	@Override
	public TransactionAttachedFile getAttachedFileById(String attachId) {
		return (TransactionAttachedFile) session.get(TransactionAttachedFile.class, attachId);
	}
	
	@Override
	public long countTransactions(Project proj) {
		Query q = session.createQuery("SELECT count(t) FROM Transaction AS t " +
				" WHERE t.budget.project.id=:rPid " +
				" AND t.budget.active = true ")
				.setString("rPid", proj.getId());
		
		return (Long) q.uniqueResult();
	}
	
	
}
