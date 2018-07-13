package ntu.celt.eUreka2.dao;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;


import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SchoolNameMap;

public interface SchoolDAO {
  	List<School> getAllSchools();
  	School getSchoolById(int schoolId);
  	void save(School school) ;
  	@CommitAfter
  	void immediateSave(School school) ;
  	void delete(School school);
  	School getSchoolByName(String name);
  	School getFirstSchoolByDescription(String des);
  	School getFirstSchoolByDescriptionWithoutDuplicateCheck(String des);
  	
  	boolean isSchoolNameExist(String name);
	List<School> searchSchools(String searchText);
	String getNextDefaultName(String prefixText);
	
	List<SchoolNameMap> getAllSchoolMappedName();
	String getSchoolMappedName(String nameFrom);
  	void saveOrUpdateNameMap(SchoolNameMap schlNameMap);
	
} 
