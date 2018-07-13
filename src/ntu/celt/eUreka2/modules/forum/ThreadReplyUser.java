package ntu.celt.eUreka2.modules.forum;

import ntu.celt.eUreka2.entities.User;

public class ThreadReplyUser implements Cloneable{
	private Long id;
	private ThreadReply threadReply;
	private User user;
	private RateType rate;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public ThreadReply getThreadReply() {
		return threadReply;
	}
	public void setThreadReply(ThreadReply threadReply) {
		this.threadReply = threadReply;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public void setRate(RateType rate) {
		this.rate = rate;
	}
	public RateType getRate() {
		return rate;
	}
	
	public ThreadReplyUser clone(){
		ThreadReplyUser tru = new ThreadReplyUser();
		//tru.setId(id);
		tru.setThreadReply(threadReply);
		tru.setRate(rate);
		tru.setUser(user);
		return tru;
	}
	
}
