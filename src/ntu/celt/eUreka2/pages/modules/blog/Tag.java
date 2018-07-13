package ntu.celt.eUreka2.pages.modules.blog;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.blog.Blog;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.blog.BlogFile;
import ntu.celt.eUreka2.modules.blog.PrivilegeBlog;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.exception.ConstraintViolationException;

public class Tag extends AbstractPageBlog  {
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private BlogDAO bDAO;
	@Property
	private Project project;
	@SessionState
	private AppState appState;
	@Property
	private String pid;
	@Inject
	private Messages messages;
	@Inject
	private AttachedFileManager attFileManager;
	
	
	@Property
	private Blog blog;
	@Property
	private List<Blog> blogs;
	@SuppressWarnings("unused")
	@Property
	private List<Blog> blogsDisplay;
	@Persist("flash")
	@Property
	private Integer curPageNo;
	@SuppressWarnings("unused")
	@Property
	private BlogFile attFile;
	@SuppressWarnings("unused")
	@Property
	private String tempTag;
	@SuppressWarnings("unused")
	@Property
	private int tempIndex;
	
	@Property
	private String tagName;
	
	void onActivate(EventContext ec) {
		if (ec.getCount() == 1) {
			pid = ec.get(String.class, 0);
		}
		else if (ec.getCount() == 2) {
			pid = ec.get(String.class, 0);
			tagName = ec.get(String.class, 1);
		}
	}
	Object[] onPassivate() {
		if (tagName == null) {
		   return new Object[] {pid};
		}
		else {
			return new Object[] {pid, tagName};
		}
	}
	
	public void setupRender() {
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", pid));
		
		if(!canViewBlog(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		
		blogs = bDAO.getBlogsByTags(project, tagName, getCurUser());
		
		if(curPageNo==null)
			curPageNo = 1;
		int fromIndex = (curPageNo-1)*appState.getRowsPerPage();
		int toIndex = Math.min(curPageNo*appState.getRowsPerPage(), blogs.size());
		blogsDisplay = blogs.subList(fromIndex, toIndex);
	}
	

	public int getTotalSize() {
		if(blogs==null) return 0;
		return blogs.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	
	@CommitAfter
	public Object onActionFromDeleteBlog(String bid){
		blog = bDAO.getBlogById(bid);
		if(blog==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "BlogId", bid));
		project = blog.getProject();
		
		if(!canDeleteBlog(blog)){
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", blog.getSubject() ));
		}
		if(blog.getComments().size()!=0){
			appState.recordErrorMsg(messages.get("cant-delete-blog-with-comment"));
		}
		else{
			try{
				for(BlogFile bf : blog.getAttaches()){
					attFileManager.removeAttachedFile(bf);
				}
				bDAO.deleteBlog(blog);
			}
			catch(ConstraintViolationException e){
				appState.recordErrorMsg(messages.format("cant-delete-x-used-by-other", blog.getSubject()));
			}
		}
		return null; //stay at same page
	}
	
}
