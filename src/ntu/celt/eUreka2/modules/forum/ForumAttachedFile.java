package ntu.celt.eUreka2.modules.forum;

import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.modules.backuprestore.JSONable;
import ntu.celt.eUreka2.services.attachFiles.AttachedFile;


public class ForumAttachedFile extends AttachedFile implements JSONable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5794142997646247727L;
	private Thread thread;
	private ThreadReply threadReply;
	

	public void setThread(Thread thread) {
		this.thread = thread;
	}
	public Thread getThread() {
		return thread;
	}
	public ThreadReply getThreadReply() {
		return threadReply;
	}
	public void setThreadReply(ThreadReply threadReply) {
		this.threadReply = threadReply;
	}
	
	public JSONObject toJSONObject(){
		JSONObject j = super.toJSONObject();
		j.put("thread", thread.getId());
		j.put("threadReply", threadReply.getId());
		
		return j;
	}
	public ForumAttachedFile clone(){
		ForumAttachedFile at = (ForumAttachedFile) super.clone(ForumAttachedFile.class);
		
		at.setThread(thread);
		at.setThreadReply(threadReply);
		
		return at;
	}
}
