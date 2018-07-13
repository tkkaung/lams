package ntu.celt.eUreka2.modules.blog;

import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.modules.backuprestore.JSONable;
import ntu.celt.eUreka2.services.attachFiles.AttachedFile;



public class BlogFile extends AttachedFile implements JSONable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1897803180249807551L;
	private Blog blog;

	public void setBlog(Blog blog) {
		this.blog = blog;
	}
	public Blog getBlog() {
		return blog;
	}
	
	
	public JSONObject toJSONObject(){
		JSONObject j = super.toJSONObject();
		j.put("blog", blog.getId());
		
		return j;
	}
	public BlogFile clone(){
		BlogFile at = (BlogFile) super.clone(BlogFile.class);
		at.setBlog(blog);
		
		return at;
	}
}

