package ntu.celt.eUreka2.modules.scheduling;

import java.util.Date;

import ntu.celt.eUreka2.entities.User;

public interface Activity extends Comparable<Activity>{
	String getName();
	void setName(String name) ;
	String getComment() ;
	void setComment(String comment);
	User getManager() ;
	void setManager(User manager) ;
	Date getStartDate() ;
	
	void setStartDate(Date startDate) ;
	Date getEndDate() ;
	void setEndDate(Date endDate) ;
	
	
}
