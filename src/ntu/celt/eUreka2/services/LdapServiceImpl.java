package ntu.celt.eUreka2.services;


import org.slf4j.Logger;

import netscape.ldap.LDAPConnection;
import netscape.ldap.LDAPException;
import netscape.ldap.LDAPv2;
import ntu.celt.eUreka2.internal.Config;

public class LdapServiceImpl implements LdapService{
	private final int connSize = 3;
	private final int sizeLimit ;
    private final int timeLimit;
    private final int ldapVersion;
    private final int ldapPort;
    
                
    private final LDAPConnection[] ldapConnection = new LDAPConnection[connSize];
	private final String ldapHostName[] = new String[connSize];
	private final String ldapUserPrincipalNameFormat[] = new String[connSize];
	
	private final Logger logger;
	
	
	public LdapServiceImpl(	Logger logger) {
		super();
		this.logger = logger;
		
		sizeLimit = Config.getInt("ldap.sizeLimit");
	    timeLimit = Config.getInt("ldap.timeLimit");
	    ldapVersion = Config.getInt("ldap.version");
	    ldapPort = Config.getInt("ldap.port");
	    
	    
		for(int i=0; i<connSize; i++){
			 initConnection(i);
		}
	}
	private void initConnection(int i){
		ldapHostName[i] = Config.getString("ldap.hostname"+i);
		
	    ldapConnection[i] = null; 
		if (ldapHostName[i] == null || ldapHostName[i].trim().length() < 1){
            //throw new RuntimeException("property '" + "ldap.hostname"+i + "' cant be empty!");
			logger.info("ldap.hostname"+i+" is empty, this server is ignored.");
		}
		else{
			ldapUserPrincipalNameFormat[i] = Config.getString("ldap.userPrincipalName.format"+i);
		    
			ldapConnection[i] = new LDAPConnection();
	    	try
	    	{
	    		ldapConnection[i].setOption(LDAPv2.SIZELIMIT, new Integer(sizeLimit));
	    		ldapConnection[i].setOption(LDAPv2.TIMELIMIT, new Integer(timeLimit));
			}
	    	catch (LDAPException le)
	    	{
	    		logger.error(le.getMessage(), le);
	    		
	    		throw new RuntimeException(le);
			}
		}
	}
	
	@Override
	public boolean authUser(String username, String password){
		for(int i=0; i<ldapConnection.length; i++){
			if(ldapConnection[i]!=null && username!=null){
				String userPrincipalName = String.format(ldapUserPrincipalNameFormat[i], username);
				checkAndconnect(i, userPrincipalName, password);
				
				if(ldapConnection[i].isAuthenticated()){
					checkAnddisconnect(i);
					return true; //Authentication successfull
				}
			}
		}
		
		return false;
	}
	
	
	
    private synchronized void checkAndconnect(int i, String userPrincipalName, String password) 
    {
    	try {
	    	if (!ldapConnection[i].isConnected())
	        {
	            if (logger.isDebugEnabled())
	                logger.debug("connecting server: {}", ldapHostName);
	
	            ldapConnection[i].connect(ldapVersion, ldapHostName[i], ldapPort, userPrincipalName, password);
	        }
	
	        if (!ldapConnection[i].isAuthenticated())
	        {
	            if (logger.isDebugEnabled())
	                logger.debug("authenticate at server: {}", ldapHostName[i]);
	
	            ldapConnection[i].authenticate(ldapVersion, userPrincipalName, password);
	        }
	    } catch (LDAPException e) {
			 if (e.getLDAPResultCode()!=LDAPException.INVALID_CREDENTIALS){
				logger.error("Error while trying to connect to LDAP server");
				e.printStackTrace();
			 }
		}
    }
    public void checkAnddisconnect(int i) 
    {
    	try{
	    	if (ldapConnection[i] != null && ldapConnection[i].isConnected())
	        {
	            if (logger.isDebugEnabled())
	                logger.debug("disconnecting from server {}", ldapHostName[i]);
	
	            ldapConnection[i].disconnect();
	        }
    	} catch (LDAPException e) {
				logger.error("Error while trying to connect to LDAP server");
				e.printStackTrace();
		}
	}
	
	
	
 /*   private String ldapAuthDN[] = new String[connSize];
    private String ldapPwd[] = new String[connSize];
    private int ldapPort[] = new int[connSize];
    private int ldapVersion[] = new int[connSize];
    private String baseDN[] = new String[connSize];
    private String attr_userPrincipalName[] = new String[connSize];
    private String attr_username[] = new String[connSize];
    
    private final Logger logger;
	
	public LdapServiceImpl(	Logger logger) {
		super();
		this.logger = logger;
		
		sizeLimit = Config.getInt("ldap.sizeLimit");
	    timeLimit = Config.getInt("ldap.timeLimit");
	    
		for(int i=0; i<connSize; i++){
			 initConnection(i);
		}
	}
	private void initConnection(int i){
		ldapHostName[i] = Config.getString("ldap.hostname"+i);
	    
	    ldapConnection[i] = null; 
		if (ldapHostName[i] == null || ldapHostName[i].trim().length() < 1){
            //throw new RuntimeException("property '" + "ldap.hostname"+i + "' cant be empty!");
			logger.debug("ldap.hostname"+i+" is empty, this server is ignored.");
		}
		else{
			ldapAuthDN[i] = Config.getString("ldap.authDN"+i);
		    ldapPwd[i] = Config.getString("ldap.password"+i);
		    ldapPort[i] = Config.getInt("ldap.port"+i);
		    ldapVersion[i] = Config.getInt("ldap.version"+i);
		    baseDN[i] = Config.getString("ldap.baseDN"+i);
		    attr_userPrincipalName[i] = Config.getString("ldap.attribute.userPrincipalName"+i);
		    attr_username[i] = Config.getString("ldap.attribute.username"+i);
		    
		    
			ldapConnection[i] = new LDAPConnection();
	    	try
	    	{
	    		ldapConnection[i].setOption(LDAPv2.SIZELIMIT, new Integer(sizeLimit));
	    		ldapConnection[i].setOption(LDAPv2.TIMELIMIT, new Integer(timeLimit));
			}
	    	catch (LDAPException le)
	    	{
	    		logger.error(le.getMessage(), le);
	    		
	    		throw new RuntimeException(le);
			}
		}
	}
	
	@Override
	public boolean authUserNTU(String username, String password){
		for(int i=0; i<ldapConnection.length; i++){
			if(ldapConnection[i]!=null){
				try {
					openSession(i);
					int scope = LDAPv3.SCOPE_SUB;
		            if (ldapVersion[i] == 2)
		                scope = LDAPv2.SCOPE_SUB;
					String filter = attr_username[i]+"="+username;
					String attributes[] = {attr_userPrincipalName[i]};
       
					if (logger.isDebugEnabled())
		                logger.debug("BaseDN: " + baseDN[i] + " / Filter: " + filter + " / Attributes: " + attributes);

					LDAPSearchResults results = ldapConnection[i].search(baseDN[i], scope, filter, attributes, false);
					closeSession(i);
					LDAPEntry match = null;
					while(results.hasMoreElements()){
						try{
							match = results.next();
						}
						catch (LDAPReferralException e){
							if(LDAPReferralException.SUCCESS == e.getLDAPResultCode()){
								if(match!=null){
									Enumeration values = match.getAttribute(attr_userPrincipalName[i]).getStringValues();
									while(values.hasMoreElements()){
										String userPrincipalName = (String) values.nextElement();
										LDAPConnection ldapConn = new LDAPConnection();
										try{
											ldapConn.connect(ldapVersion[i], ldapHostName[i], ldapPort[i], userPrincipalName, password);
											if(ldapConn.isConnected() && ldapConn.isAuthenticated()){
												ldapConn.disconnect(); //clean up before exit
												return true; //Authentication successfull
											}
										}
										catch (LDAPException ex) {
											if(LDAPException.INVALID_CREDENTIALS == ex.getLDAPResultCode()){
												logger.info("Invalid login, username="+username+" in LDAP server"+i);
											}
											//ex.printStackTrace();
											ldapConn = null;
										}
									}
								}
							}
						}
					}
				} catch (LDAPException e) {
					logger.error("Error while trying to connect to LDAP server");
					e.printStackTrace();
				}
			}
		}
		
		return false;
	}
    private synchronized void checkAndconnect(int i) throws LDAPException
    {
        if (!ldapConnection[i].isConnected())
        {
            if (logger.isDebugEnabled())
                logger.debug("connecting server: {}", ldapHostName);

            ldapConnection[i].connect(ldapVersion[i], ldapHostName[i], ldapPort[i], ldapAuthDN[i], ldapPwd[i]);
        }

        if (!ldapConnection[i].isAuthenticated())
        {
            if (logger.isDebugEnabled())
                logger.debug("authenticate at server: {}", ldapHostName[i]);

            ldapConnection[i].authenticate(ldapVersion[i], ldapAuthDN[i], ldapPwd[i]);
        }
    }
    public LDAPConnection openSession(int i) throws LDAPException{
		checkAndconnect(i);
		return ldapConnection[i];
	}
    public void closeSession(int i) throws LDAPException
    {
    	if (ldapConnection[i] != null && ldapConnection[i].isConnected())
        {
            if (logger.isDebugEnabled())
                logger.debug("disconnecting from server {}", ldapHostName[i]);

            ldapConnection[i].disconnect();
        }
	}
    
*/    
    
   
	
}
