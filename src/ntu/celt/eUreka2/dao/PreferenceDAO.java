package ntu.celt.eUreka2.dao;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.entities.Preference;

public interface PreferenceDAO {
  	Preference getPreferenceById(int preferrenceId);
  	@CommitAfter
  	void immediateSave(Preference preference) ;
  	Preference getPreferenceByUserId(int userId);
  	
  	
} 
