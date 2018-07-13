package ntu.celt.eUreka2.components;

import ntu.celt.eUreka2.dao.PreferenceDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.ThemeColor;
import ntu.celt.eUreka2.entities.Preference;
import ntu.celt.eUreka2.entities.WebSessionData;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ThemeSelect {
	@SuppressWarnings("unused")
	@Property
	private ThemeColor[] _themeColors = ThemeColor.values();
	@SuppressWarnings("unused")
	@Property
	private ThemeColor _themeColor;
	@Property
	private String[] _colors = {"#ffffff","#0465a1","#486a1c","#e2770f","#a71382"};
	@SuppressWarnings("unused")
	@Property
	private int _index;
	
	
	
	@Inject
	private PreferenceDAO _prefDAO;
	@Inject
    private WebSessionDAO websessionDAO;
    
    @SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
    private String ssid;
    
	public String getColor(int index){
		return _colors[index];
	}
	public String getSpace(){
		return "&nbsp;";
	}
	
	@CommitAfter
	void onActionFromSelectColor(ThemeColor tc){
		Preference pref = websessionDAO.getCurrentPreference(ssid);
		if(pref==null){
			pref = new Preference();
			pref.setUser(websessionDAO.getCurrentUser(ssid));
		}
		pref.setThemeColor(tc);
		_prefDAO.immediateSave(pref);
	}
}
