package ntu.celt.eUreka2.pages.modules.forum;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.EvenOdd;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.forum.Forum;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.forum.PrivilegeForum;
import ntu.celt.eUreka2.modules.forum.RateType;
import ntu.celt.eUreka2.modules.forum.ThreadReply;
import ntu.celt.eUreka2.modules.forum.Thread;
import ntu.celt.eUreka2.modules.forum.ThreadUser;
import ntu.celt.eUreka2.services.attachFiles.AttachedFile;

public class ForumView extends AbstractPageForum {
	@Property
	private Project curProj;
	@SessionState
	private AppState appState;
	
	@Property
	private Forum forum;
	private Long forumId;
	@Property
	private List<Thread> threads;
	@SuppressWarnings("unused")
	@Property
	private Thread thread;
	@SuppressWarnings("unused")
	@Property
	private EvenOdd evenOdd ;
	@SuppressWarnings("unused")
	@Property
	private ThreadReply recentThreadR;
	@SuppressWarnings("unused")
	@Property
	private AttachedFile tempAttFile;
	
	enum SubmitType {DELETE_THREAD};
	private SubmitType submitType ;
	
	
	@Inject
	private ForumDAO forumDAO;
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private Messages messages;
    @Inject
    private PropertyConduitSource propertyConduitSource; 
	@Inject
	private Request rqust;
	
    
	void onActivate(Long id) {
		forumId = id;
	}
	Long onPassivate() {
		return forumId;
	}
	void setupRender(){
		forum = forumDAO.getForumById(forumId);
		if(forum==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ForumId", forumId));
		curProj = forum.getProject();
		
		if(!canViewForum(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		threads = new ArrayList<Thread>(forum.getThreads());
		
		evenOdd = new EvenOdd();
		/*if (grid.getSortModel().getSortConstraints().isEmpty()) {
			grid.getSortModel().updateSort("modifyDate");
			grid.getSortModel().updateSort("modifyDate"); //do 2 times to get Descending order
		}
		*/
		
	}
	
	
	
	
	
	
	@Component(id = "GridForm")
	private Form _form;
	private String[] selThreadIDs;
	
	void onValidateFormFromGridForm() {
		forum = forumDAO.getForumById(forumId);
		curProj = forum.getProject();
		
		selThreadIDs = rqust.getParameters("thChkBox");
		if(selThreadIDs==null || selThreadIDs.length==0){
			_form.recordError(messages.get("select-at-least-one-item"));
		}
	}
	
	@CommitAfter
	void onSuccessFromGridForm() {
		if(selThreadIDs!=null){
		for(String rootThId : selThreadIDs){
			Thread rootTh = forumDAO.getThreadById(Long.parseLong(rootThId));

			switch(submitType){
			case DELETE_THREAD:
				forum.removeThread(rootTh);
				forumDAO.deleteThread(rootTh);
				break;
			}
			
		}}
	}
	
	void onSelectedFromDeleteThread() {
		submitType = SubmitType.DELETE_THREAD;
	}
	
	public long getTotalView(Thread thread){
		return forumDAO.getTotalView(thread);
	}
	
	public boolean loadRecentPost(Thread thread){
		List<ThreadReply> recentThRs = forumDAO.getLatestThreadReplies(thread, 1); //limit number of result to 1
		if(recentThRs==null || recentThRs.size()==0)
			return false;
		recentThreadR = recentThRs.get(0);
		
		return true;
	}
	
	public BeanModel<Thread> getModel(){
		BeanModel<Thread> model = beanModelSource.createEditModel(Thread.class, messages);
        model.include();  //remove all default
        
        if(canDeleteForumThread(curProj))
        	model.add("chkbox", null);
        
        model.add("flag", null); 
        model.add("thread", propertyConduitSource.create(Thread.class, "name"));
        model.add("author", propertyConduitSource.create(Thread.class, "authorDisplayname")).label(messages.get("started-by"));
        model.add("date", propertyConduitSource.create(Thread.class, "modifyDate"));
        model.add("replies", propertyConduitSource.create(Thread.class, "replies.size()"));
        model.add("views", null);
        model.add("rates", null);
        model.add("lastReplied", null);
        
        return model;
	}
	
	public String getRowClass(Thread th){
		ThreadUser tu = th.getThreadUser(getCurUser()); 
		if(tu!=null && tu.isHasRead())
			return "read";
		
		return "unread";
	}
	public boolean isFlagged(Thread th){
		ThreadUser tu = th.getThreadUser(getCurUser()); 
		if(tu!=null && tu.isFlagged())
			return true;
		
		return false;
	}
	public long getTotalNegativeRate(Thread th){
		return forumDAO.getTotalRateByType(th, RateType.BAD);
	}
	public long getTotalPositiveRate(Thread th){
		return forumDAO.getTotalRateByType(th, RateType.GOOD);
	}
	
	
	
	public int getTotalSize() {
		if(threads==null)
			return 0;
		return threads.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
}
