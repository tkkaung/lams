package ntu.celt.eUreka2.dao;

import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import ntu.celt.eUreka2.entities.Preference;
import ntu.celt.eUreka2.entities.SessionVisitStatistic;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Cookies;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class WebSessionDAOImpl implements  WebSessionDAO {

	@Inject
    private Session session;
	@Inject
	private Cookies cookies;
	
	
	
	public WebSessionDAOImpl(Session session, Cookies cookies) {
		super();
		this.session = session;
		this.cookies = cookies;
	}
	
	@Override
	public User doAuthenticate(String username, String password) {
		User u = (User) session.createCriteria(User.class)
			.add(Restrictions.eq("username", username))
			.add(Restrictions.eq("password", password))
			.uniqueResult();
		
		return u;
	}
	@Override
	public void immediateSaveWebSessionData(WebSessionData wsData) {
		session.saveOrUpdate(wsData);
		
	}
	@Override
	public int cleanWebSessions() {
		Calendar expiredTime = Calendar.getInstance();
		int validTime = Config.getInt("session.validtimelimit");
		expiredTime.add(Calendar.SECOND, (-1)*validTime);
		
		Query q = session.createQuery("DELETE FROM WebSessionData  " +
				" WHERE lastActiveTime < :expireTime ")
				.setTimestamp("expireTime", expiredTime.getTime());
		return q.executeUpdate();
	}
	@Override
	public WebSessionData getWebSessionById(String sessionId) {
		Query q = session.createQuery("SELECT ws FROM WebSessionData AS ws " +
				" WHERE ws.id = :sessionId" )
				.setString("sessionId", sessionId);
		
		return (WebSessionData) q.uniqueResult();
	}
	
	
	@Override
	public User getCurrentUser(String sessionId) {
		Query q = session.createQuery("SELECT u FROM WebSessionData AS ws, User AS u " +
				" WHERE  ws.id = :sessionId " +
				" AND ws.username = u.username ")
				.setString("sessionId", sessionId);
		
		return (User) q.uniqueResult();
	}
	@Override
	public Preference getCurrentPreference(String sessionId) {
		Query q = session.createQuery("SELECT p FROM Preference AS p, WebSessionData AS ws  " +
				" WHERE  ws.id = :sessionId " +
				" AND ws.username = p.user.username " 
				)
				.setString("sessionId", sessionId);
		List<Preference> pList = q.list();
		if(pList.size()>0)
			return pList.get(0);
		return null;
		
//		return (Preference) q.uniqueResult();
	}
	
	@Override
	public void deleteWebSessionData(String ssId) {
		Query q = session.createQuery("DELETE FROM WebSessionData WHERE id=:id")
				.setString("id", ssId);
		q.executeUpdate();
	}
	@Override
	public WebSessionData getWebSessionFromCookies(HttpServletRequest request) {
		String ssid = cookies.readCookieValue("sess");
		String hash = cookies.readCookieValue("hash"); 
		String ip = request.getRemoteAddr();
		String browser = request.getHeader("User-Agent");
		if(Util.generateHashValue(ssid+ip+browser).equals(hash)){
			//ssid is valid
			return getWebSessionById(ssid);
		}
		return null;
	}
	@Override
	public void setWebSessionToCookies(WebSessionData wsData, HttpServletRequest request) {
		String ip = request.getRemoteAddr();
		String browser = request.getHeader("User-Agent");
		String hash = Util.generateHashValue(wsData.getId()+ip+browser);
		
		cookies.writeCookieValue("sess", wsData.getId(), 10*Config.getInt("session.validtimelimit"));
		cookies.writeCookieValue("hash", hash, 10*Config.getInt("session.validtimelimit")); 
		
	}

	@Override
	public void cleanWebSessionsCookies() {
		cookies.removeCookieValue("sess");
		cookies.removeCookieValue("hash");
	}

	
	@Override
	public void addSessesionVististatistic(SessionVisitStatistic sessionVisit) {
		session.save(sessionVisit);
	}
	
	
	

}
