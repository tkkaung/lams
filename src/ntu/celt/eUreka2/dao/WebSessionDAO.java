package ntu.celt.eUreka2.dao;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.entities.Preference;
import ntu.celt.eUreka2.entities.SessionVisitStatistic;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;

public interface WebSessionDAO  {
	
	
	User doAuthenticate(String username, String password);
	@CommitAfter
	void immediateSaveWebSessionData(WebSessionData wsData);
	void deleteWebSessionData(String ssId);
	
	/**
	 * get valid, non-expired websession, return NULL if sessionId not exists or NULL
	 * @param sessionId
	 * @return
	 */
	WebSessionData getWebSessionById(String sessionId);
	
	/**
	 * call to remove expired sessions, return number of session deleted
	 */
	int cleanWebSessions();
	
	User getCurrentUser(String ssId);
	Preference getCurrentPreference(String ssId);
	
	WebSessionData getWebSessionFromCookies(HttpServletRequest request);
	void setWebSessionToCookies(WebSessionData wsData, HttpServletRequest request);
	void cleanWebSessionsCookies();
	@CommitAfter
	void addSessesionVististatistic(SessionVisitStatistic sessionVisit);
}
