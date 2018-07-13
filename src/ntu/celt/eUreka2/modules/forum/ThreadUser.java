package ntu.celt.eUreka2.modules.forum;

import java.util.Date;

import ntu.celt.eUreka2.entities.User;

public class ThreadUser implements Cloneable{
	private Long id;
	private Thread thread;
	private User user;
	private boolean hasRead = false;
	private boolean flagged = false;
	private Integer numView = 0;
	private RateType rate;
	private Date modifyDate;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Thread getThread() {
		return thread;
	}
	public void setThread(Thread thread) {
		this.thread = thread;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public boolean isHasRead() {
		return hasRead;
	}
	public void setHasRead(boolean hasRead) {
		this.hasRead = hasRead;
	}
	public boolean isFlagged() {
		return flagged;
	}
	public void setFlagged(boolean flagged) {
		this.flagged = flagged;
	}
	public Integer getNumView() {
		return numView;
	}
	public void setNumView(Integer numView) {
		this.numView = numView;
	}
	public void setRate(RateType rate) {
		this.rate = rate;
	}
	public RateType getRate() {
		return rate;
	}
	public Date getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}
	
	public ThreadUser clone(){
		ThreadUser tu = new ThreadUser();
		//tu.setId(id);
		tu.setThread(thread);
		tu.setUser(user);
		tu.setHasRead(hasRead);
		tu.setFlagged(flagged);
		tu.setNumView(numView);
		tu.setRate(rate);
		tu.setModifyDate(modifyDate);
		
		return tu;
	}
	
}
