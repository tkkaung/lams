package ntu.celt.eUreka2.dao;

import java.util.List;

import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.grid.SortConstraint;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.hibernate.exception.ConstraintViolationException;

import ntu.celt.eUreka2.data.FilterType;
import ntu.celt.eUreka2.data.UserSearchableField;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.User;

public interface UserDAO {
  	List<User> getAllUsers();
  	User getUserById(int id);
  	@CommitAfter
  	void save(User user);
  	@CommitAfter
  	void saveBatch(List<User> user);
  	
  	void delete(User user) throws ConstraintViolationException ;
  	User getUserByUsername(String username);
  	boolean isUsernameExist(String username);
  	User getUserByExKey(String exKey);
  	List<User> getUserByUsernameOrEmail(String usernameEmail);

  	GridDataSource searchUsersAsDataSource(FilterType filterType, String searchText
  			, UserSearchableField searchIn, Boolean enabled, School school, SysRole sysRole
  			, Integer firstResult, Integer maxResult, List<SortConstraint> sortconstraints);
  	
  	List<User> searchUsers(FilterType filterType, String searchText, UserSearchableField searchIn, 
  			Boolean enabled, School school, Integer firstResult, Integer maxResult);
  	int countSearchUsers(FilterType filterType, String searchText, UserSearchableField searchIn, 
  			Boolean enabled, School school);
  	
  	long countUserBySysRole(SysRole role);
  	long countUserBySchool(School school);
  	long countUserByProjRole(ProjRole projRole);
	
} 
