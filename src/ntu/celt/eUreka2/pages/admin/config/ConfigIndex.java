package ntu.celt.eUreka2.pages.admin.config;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;

@Deprecated  /* not in use any more*/
public class ConfigIndex {
	
	@Property
	private String _bbKey;
	@Property
	private String _bbTimeout;
	@Property
	private String _displayDate;
	@Property
	private String _displayDateLong;
	@Property
	private String _displayTime;
	@Property
	private String _sysEmail;
	@Property
	private String _sysVirtualDrive;
	
	@Inject
	private Messages _messages;
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
	
	
	void setupRender(){
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
	}
	
	void onPrepareFromConfigForm(){
		_bbKey = Config.getString(Config.BB_HASH_KEY);
		_bbTimeout = Config.getString(Config.BB_TIMEOUT_LIMIT );
		_displayDate = Config.getString(Config.FORMAT_DATE_PATTERN );
		_displayDateLong = Config.getString(Config.FORMAT_DATE_LONG_PATTERN);
		_displayTime = Config.getString(Config.FORMAT_TIME_PATTERN);
		_sysEmail = Config.getString(Config.SYSTEM_EMAIL_ADDRESS );
		_sysVirtualDrive = Config.getString(Config.VIRTUAL_DRIVE );
	}
	void onValidateFormFromConfigForm(){
		
	}
	void onSuccessFromConfigForm(){
		Config.setProperty(Config.BB_HASH_KEY, addEscapeChar(_bbKey));
		Config.setProperty(Config.BB_TIMEOUT_LIMIT, addEscapeChar(_bbTimeout));
		Config.setProperty(Config.FORMAT_DATE_PATTERN, addEscapeChar(_displayDate));
		Config.setProperty(Config.FORMAT_DATE_LONG_PATTERN, addEscapeChar(_displayDateLong));
		Config.setProperty(Config.FORMAT_TIME_PATTERN, addEscapeChar(_displayTime));
		Config.setProperty(Config.SYSTEM_EMAIL_ADDRESS, addEscapeChar(_sysEmail));
		Config.setProperty(Config.VIRTUAL_DRIVE, addEscapeChar(_sysVirtualDrive));
		
		Config.saveConfig();
		
		appState.recordInfoMsg(_messages.get("success-save-config-text"));
	}
	void onActionFromReload(){
		Config.reloadConfig();
		appState.recordInfoMsg(_messages.get("success-reload-config-text"));
	}
	
	private String addEscapeChar(String value){
		if(value!=null && value.contains(Config.getInstance().getListDelimiter()+"")){
			value =  value.replace("\\","\\\\")
				.replace(Config.getInstance().getListDelimiter()+"", 
				"\\"+Config.getInstance().getListDelimiter())
				;
		}
		return value;
	}
	
}
