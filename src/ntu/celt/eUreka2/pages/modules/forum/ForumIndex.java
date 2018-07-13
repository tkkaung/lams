package ntu.celt.eUreka2.pages.modules.forum;

import java.util.List;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.EvenOdd;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.forum.Forum;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.forum.PrivilegeForum;
import ntu.celt.eUreka2.modules.forum.Thread;
import ntu.celt.eUreka2.modules.forum.ThreadReply;

public class ForumIndex extends AbstractPageForum{
	@Property
	private Project curProj;
	@SessionState
	private AppState appState;
	private String projId ;
	@SuppressWarnings("unused")
	@Property
	private EvenOdd evenOdd ;
	@Property
	private List<Forum> forums;
	@Property
	private Forum forum;
	@SuppressWarnings("unused")
	@Property
	private Thread recentThread;
	@SuppressWarnings("unused")
	@Property
	private ThreadReply recentThreadR;
	
	
	@Inject
	private ForumDAO forumDAO;
	@Inject
	private ProjectDAO projDAO;
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private Messages messages;
    @Inject
    private PropertyConduitSource propertyConduitSource; 
    
    
    
	void onActivate(String id) {
		projId = id;
	}
	String onPassivate() {
		return projId;
	}
	void setupRender() {
		curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", projId));
		}
		if(!canViewForum(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		forums = forumDAO.getActiveForums(curProj);
		evenOdd = new EvenOdd();
	}
	
	
	
	@CommitAfter
	void onActionFromDelete(Long id){
		forum = forumDAO.getForumById(id);
		if(forum==null){
			appState.recordErrorMsg(messages.format("entity-not-exists", "ForumID", id));
		}
		else{
			curProj = forum.getProject();
			if(!canDeleteForum(forum)){
				throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", forum.getName() ));
			}
			forumDAO.deleteForum(forum);
		}
	}

	public long getTotalThreads(Forum forum){
		return forumDAO.getTotalThreads(forum);  
	}
	public long getTotalReplies(Forum forum){
		return forumDAO.getTotalThreadReplies(forum);  
	}
	
	public boolean loadRecentPost(Forum forum){
		List<Thread> recentThs = forumDAO.getLatestThreads(forum, 1); //limit number of result to 1
		List<ThreadReply> recentThRs = forumDAO.getLatestThreadReplies(forum, 1); //limit number of result to 1
		if((recentThs==null || recentThs.size()==0) 
			&& (recentThRs==null || recentThRs.size()==0))
			return false;
		
		recentThread = recentThs.get(0);
		if(recentThRs.size()>0)
			recentThreadR = recentThRs.get(0);
		
		return true;
	}
	
	public boolean after(Thread th, ThreadReply thR){
		if(thR==null)
			return true;
		if(th.getModifyDate().after(thR.getModifyDate()))
			return true;
		return false;
	}
	
	public BeanModel<Forum> getModel(){
		BeanModel<Forum> model = beanModelSource.createEditModel(Forum.class, messages);
        model.include(); //remove all default
        
        model.add("forum", propertyConduitSource.create(Forum.class, "name")); 
        model.add("threads", null);
        model.add("replies", null);
        model.add("lastPost", null); 
        
        boolean showAction = false;
        for(Forum f : forums){
        	if(canDeleteForum(f) || canEditForum(f)){
        		showAction = true;
        		break;
        	}
        }
        if(showAction){
        	model.add("action", null);
        }
        return model;
	}
}
