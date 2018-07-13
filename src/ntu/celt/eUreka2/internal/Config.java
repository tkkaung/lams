package ntu.celt.eUreka2.internal;

import ntu.celt.eUreka2.data.PredefinedNames;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
	private static Logger logger = LoggerFactory.getLogger(Config.class);
	
	public static final String CONFIG_FILE_NAME = "Config.properties";
	
	public static final String BB_HASH_KEY = "bbIntegrate.hashkey";
	public static final String BB_TIMEOUT_LIMIT = "bbIntegrate.timeout";
	public static final String FORMAT_DATE_PATTERN = "display.pattern.date";
	public static final String FORMAT_DATE_LONG_PATTERN = "display.pattern.date.long";
	public static final String FORMAT_TIME_PATTERN = "display.pattern.time";
	public static final String SYSTEM_EMAIL_ADDRESS = "system.email.address"; 
	public static final String VIRTUAL_DRIVE = "system.virtual.drive";
	
	public static final String LOGOUT_REDIRECT_PAGE = "authen.logout.redirectpage";
	
	public static final String BASE_URL = "base.url";
	
	public static final String AUTH_RESETPASS_TIMEOUT_LIMIT = "auth.resetpwd.timeout";
	
	public static final String[] mandatoryModules = {PredefinedNames.MODULE_ANNOUNCEMENT
													, PredefinedNames.MODULE_RESOURCE
													, PredefinedNames.MODULE_SCHEDULING
													, PredefinedNames.MODULE_LEARNING_LOG};
	
	public static final String[] paramSysRoles = {PredefinedNames.SYSROLE_PROJTYPE_ADMIN , PredefinedNames.SYSROLE_SCHOOL_ADMIN};
	
	
	
	private static PropertiesConfiguration instance ;
	public static PropertiesConfiguration getInstance(){
		if(instance==null){
			Resource r = new ClasspathResource(CONFIG_FILE_NAME);
			try {
				instance = new PropertiesConfiguration(r.getFile());
			} catch (ConfigurationException e) {
				logger.error("There is a problem loading config file "+ r.getFile());
				e.printStackTrace();
			}
		}
		return instance;
	}
	public static void saveConfig(){
		try {
			getInstance().save();
		} catch (ConfigurationException e) {
			logger.error("There is a problem saving config file "+ getInstance().getFile());
			e.printStackTrace();
		}
	}
	public static void reloadConfig(){
		Resource r = new ClasspathResource(CONFIG_FILE_NAME);
		try {
			instance = new PropertiesConfiguration(r.getFile());
		} catch (ConfigurationException e) {
			logger.error("There is a problem loading config file "+ r.getFile());
			e.printStackTrace();
		}
	}
	
	public static String getString(String key) {
		return getInstance().getString(key);
	}
	public static boolean getBoolean(String key) {
		return getInstance().getBoolean(key);
	}
	public static int getInt(String key) {
		return getInstance().getInt(key);
	}
	public static long getLong(String key) {
		return getInstance().getLong(key);
	}
	public static Object getProperty(String key) {
		return getInstance().getProperty(key);
	}
	public static void setProperty(String key, Object value){
		getInstance().setProperty(key, value);
	}
}
