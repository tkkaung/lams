package ntu.celt.eUreka2.modules.budget;

import java.util.List;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;

public interface BudgetDAO extends GenericModuleDAO{
  	Transaction getTransactionById(Long id);
  	void saveTransaction(Transaction transaction) ;
  	void deleteTransaction(Transaction transaction) ;
 	List<String> getTransactionCategories(Project project);
 	List<User> getTransactionUsers(Project project);
  	TransactionAttachedFile getAttachedFileById(String id);
 	
 	
  	Budget getBudgetById(Long id); 
  	Budget getActiveBudget(Project project); 
  	void saveBudget(Budget budget) ;
  	void deleteBudget(Budget budget) ;
  	
  	long getCountTransactionsByType(Project proj, TransactionType tType);
  	double getTotalTransactionsByType(Project proj, TransactionType tType);
  	double getTotalTransactionsByTypeAndStatus(Project proj, TransactionType tType, TransactionStatus tStatus);
	long countTransactions(Project proj);
  	
  	
  	
} 
