package ntu.celt.eUreka2.modules.forum;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;

public interface ForumDAO extends GenericModuleDAO {
  	Forum getForumById(Long id);
  	@CommitAfter
  	void saveForum(Forum forum) ;
  	void deleteForum(Forum forum) ;
  	List<Forum> getActiveForums(Project project); 
  	
  	Thread getThreadById(Long id);
  	@CommitAfter
  	void saveThread(Thread thread);
  	void deleteThread(Thread thread);
  	
  	ThreadReply getThreadReplyById(Long id);
  	@CommitAfter
  	void saveThreadReply(ThreadReply thread);
  	void deleteThreadReply(ThreadReply thread);
  	
  	ThreadUser getThreadUser(Long threadId, Integer userId);
  	void saveThreadUser(ThreadUser threadUser);
  	ThreadReplyUser getThreadReplyUser(Long threadReplyId, Integer userId);
  	void saveThreadReplyUser(ThreadReplyUser threadReplyUser);
  	
  	ForumAttachedFile getAttachedFileById(String id);
  	@CommitAfter
  	void saveAttachFile(ForumAttachedFile attachedFile);
  	
  	
  	long countPastUpdated(Project project, int numOfPastDays);
  	long getTotalForums(Project proj);
  	long getTotalThreads(Project proj);
  	long getTotalThreadReplies(Project proj);
  	long getTotalThreadReflection(Project proj, User curUser);
  	long getTotalThreads(Forum forum);
  	long getTotalThreadReplies(Forum forum);
  	List<Thread> getLatestThreads(Project project, int maxResult, int numOfPastDays) ;
  	List<Thread> getLatestThreads(Forum forum, int maxResult);
  	List<ThreadReply> getLatestThreadReplies(Project project, int maxResult, int numOfPastDays) ;
  	List<ThreadReply> getLatestThreadReplies(Forum forum, int maxResult);
  	List<ThreadReply> getLatestThreadReplies(Thread thread, int maxResult);
  	
  	long getTotalView(Thread thread);
  	long getTotalRateByType(Thread thread, RateType type);
  	long getTotalRateByType(ThreadReply threadR, RateType type);
	List<ThreadUser> getThUserByRateType(Thread thread, RateType type);
	List<ThreadReplyUser> getThUserByRateType(ThreadReply threadR, RateType type);
	
	long getTotalThreads(Project proj, User user);
	long getTotalThreadReplies(Project proj, User user);
	long getTotalThreadsRate(Project proj, User user, RateType type);
	long getTotalThreadsYouRate(Project proj, User user, RateType type,	User curUser);
	long getTotalThreadRepliesRate(Project proj, User user, RateType type);
	long getTotalThreadRepliesYouRate(Project proj, User user, RateType type, User curUser);
	List<Thread> getThreads(Project proj, User user);
	List<ThreadReply> getThreadReplies(Project proj, User user);
	List<Thread> getThreadsRate(Project proj, User user, RateType type);
	List<Thread> getThreadsYouRate(Project proj, User user, RateType type,	User curUser);
	List<ThreadReply> getThreadRepliesRate(Project proj, User user, RateType type);
	List<ThreadReply> getThreadRepliesYouRate(Project proj, User user, RateType type, User curUser);
	
} 
