package ntu.celt.eUreka2.dao;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;


import ntu.celt.eUreka2.entities.SysAnnouncement;

public interface SysAnnouncementDAO {
  	List<SysAnnouncement> getAllSysAnnouncements();
  	List<SysAnnouncement> getActiveSysAnnouncements();
  	SysAnnouncement getSysAnnouncementById(int sysAnnmtId);
  	void save(SysAnnouncement sysAnnmt) ;
  	@CommitAfter
  	void delete(SysAnnouncement sysAnnmt);
  	
} 
