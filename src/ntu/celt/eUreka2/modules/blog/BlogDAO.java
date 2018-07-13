package ntu.celt.eUreka2.modules.blog;

import java.util.Date;
import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;

public interface BlogDAO  extends GenericModuleDAO{
	List<Blog> getBlogsByProject(Project project);
	
	List<Blog> searchBlogs(String searchText, BlogSearchableField searchIn,
			Project project, User author, BlogStatus status, Date sdate, Date edate,
			Integer firstResult, Integer maxResults, User curUser);  //if the value is null, means ignore check in that field
	List<Blog> getActiveBlogs(Project proj, User u);
	
	Blog getBlogById(String id);
	@CommitAfter
	void addBlog(Blog blog);
	void updateBlog(Blog blog);
	void deleteBlog(Blog blog);
	
	
	BlogComment getBlogCommentById(int id);
	void updateComment(BlogComment comment);
	
	BlogFile getBlogFileById(String id);
	void updateBlogFile(BlogFile blogFile);
	
	long getTotalBlogComment(Project proj, User curUser);
	long countBlogs(Project proj);
	long countBlogs(Project proj, User creator, BlogStatus status, User curUser);
	List<Blog> getBlogs(Project proj, User creator, BlogStatus status, User curUser);
	long countBlogComments(Project proj);
	long countBlogComments(Project proj, User creator);
	List<BlogComment> getBlogComments(Project proj, User creator);
	
	boolean hasBlogTags(Project proj);
	List<String> getBlogTags(Project proj, int MaxResult);//sort by number used
	List<Blog> getBlogsByTags(Project proj, String tag, User curUser);
	long countBlogsByTags(Project proj, String tag, User curUser);

	
}
