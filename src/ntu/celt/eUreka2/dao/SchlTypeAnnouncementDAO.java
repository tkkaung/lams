package ntu.celt.eUreka2.dao;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;


import ntu.celt.eUreka2.data.LastUpdateItem;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.SchlTypeAnnouncement;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;

public interface SchlTypeAnnouncementDAO {
  	List<SchlTypeAnnouncement> getAllSchlTypeAnnouncements();
  	List<SchlTypeAnnouncement> getSchoolAnnouncements(School school);
  	List<SchlTypeAnnouncement> getTypeAnnouncements(ProjType type);
  	
  	List<SchlTypeAnnouncement> getActiveSchlTypeAnnouncements(Project proj, User curUser);
  	long countSchlTypeAnnouncements(Project proj);
  	
  	List<SchlTypeAnnouncement> getDismissedSchlTypeAnnouncements(Project proj, User curUser);
  	
  	
  	SchlTypeAnnouncement getSchlTypeAnnouncementById(int stAnnmtId);
  	void save(SchlTypeAnnouncement stAnnmt) ;
  	@CommitAfter
  	void delete(SchlTypeAnnouncement stAnnmt);
  	List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay);
} 
