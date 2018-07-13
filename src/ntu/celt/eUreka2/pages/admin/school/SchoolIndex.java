package ntu.celt.eUreka2.pages.admin.school;

import java.util.List;

import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.exception.ConstraintViolationException;

public class SchoolIndex {
	@Property
	private School _school;
	@Property
	private List<School> _schools;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String _searchText;
	
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@SessionState
	private AppState appState;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	@Inject
	private SchoolDAO _schoolDAO;
	@Inject
	private UserDAO _userDAO;
	@Inject
	private ProjectDAO _projDAO;
	@Inject
	private Messages _messages;
	
	// it is recommended to use setupRender() to initialize object for display.
	// use onPrepare() to initial object for editing
	void setupRender() {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_schools = _schoolDAO.searchSchools(_searchText);
	}
	
	void onSuccessFromSearchForm(){
		// only need to reload
		//_schools = _schoolDAO.searchSchools(_searchText);
	}
	
	
	public long getCountUserBySchool(School s){
		return _userDAO.countUserBySchool(s);
	}
	public long getCountProjectBySchool(School s){
		return _projDAO.countProjectBySchool(s);
	}
	
	
	void onActionFromDelete(int id) {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_school = _schoolDAO.getSchoolById(id);
		
		try{
			deleteSchool(_school);
			appState.recordInfoMsg(_messages.format("successfully-delete-x", _school.getName()));
		}
		catch(ConstraintViolationException e){
			appState.recordErrorMsg(_messages.format("cant-delete-x-used-by-other", _school.getName()));
		}	
	}
	
	@CommitAfter
	void deleteSchool(School school){
		_schoolDAO.delete(school);
	}
	
	public int getTotalSize() {
		if (_schools == null)
			return 0;
		return _schools.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
}
