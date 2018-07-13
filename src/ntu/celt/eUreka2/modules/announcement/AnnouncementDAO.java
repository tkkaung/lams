package ntu.celt.eUreka2.modules.announcement;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;

public interface AnnouncementDAO extends GenericModuleDAO {
  	List<Announcement> getAllAnnouncements();
  	Announcement getAnnouncementById(Long id);
  	@CommitAfter
  	void save(Announcement announcement) ;
  	@CommitAfter
  	void delete(Announcement announcement) ;
  	List<Announcement> getActiveAnnouncements(Project project, User curUser); 
  	List<Announcement> getActiveAnnouncements(Project project, Integer maxResult,User curUser); 
  	List<Announcement> getDismissedAnnouncements(Project project, Integer maxResult,User curUser); 
  	
  	List<Announcement> searchAnnouncements(Project project, User creator, String subject); 
  	

  	long countPastUpdated(Project project,  int numOfPastDays);
	long countAnnouncements(Project proj);
	long countAnnouncements(Project proj, User creator);
	List<Announcement> getAnnouncements(Project proj, User creator);
	
	
} 
