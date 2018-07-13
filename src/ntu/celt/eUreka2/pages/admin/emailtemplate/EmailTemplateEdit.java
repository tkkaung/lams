package ntu.celt.eUreka2.pages.admin.emailtemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplate;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PersistentLocale;

public class EmailTemplateEdit {
	@Property
	private EmailTemplate _emailTem;
	private long _id; 	
	
	
	@Inject
    private EmailTemplateDAO _emailTemDAO;
	@Inject
	private EmailManager _emailManager;
	@Inject
	private Messages _messages;
	@Inject
	private PersistentLocale localeService;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	void onActivate(long id) {
    	_id = id;
    }
    long onPassivate() {
       return _id;
    }
    boolean isCreateMode(){
    	if(_id==0)
    		return true;
    	return false;
    }
    
    void setupRender(){
    	if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_EMAIL_TEMPLATE)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
    	if(isCreateMode()){   //create new
    		_emailTem = new EmailTemplate();
    		
    		/*
    		 to speed up create new process
    		 */
    		 EmailTemplate et = _emailTemDAO.getEmailTemplateByTypeAndLanguage(EmailTemplateConstants.DEFAULT, "en"); //default
    		 _emailTem.setContent(et.getContent());
    		 _emailTem.setLanguage(et.getLanguage());
    		 _emailTem.setSubject(et.getSubject());
    	
    	}
    	else {   //edit
	    	_emailTem = _emailTemDAO.getEmailTemplateById(_id);
	    	if(_emailTem==null){
				throw new RecordNotFoundException(_messages.format("entity-not-exists", "emailTemID", _id));
			}
    	}
    }
    
  /*NOTE: tapestry5.2.5 bug, when use BeanEditForm, onPrepareFromXXX() does Not called when render, 
   * but called twice when submit. so, we use onPrepareForSubmitFromXXX() and setupRender() as work around
   */
    void onPrepareForSubmitFromEmailTemForm(){
    	if(isCreateMode()){   //create new
    		_emailTem = new EmailTemplate();
    		
			 EmailTemplate et = _emailTemDAO.getEmailTemplateByTypeAndLanguage(EmailTemplateConstants.DEFAULT, "en"); //default
			 _emailTem.setContent(et.getContent());
			 _emailTem.setLanguage(et.getLanguage());
			 _emailTem.setSubject(et.getSubject());
    	
    	}
    	else {   //edit
	    	_emailTem = _emailTemDAO.getEmailTemplateById(_id);
	    	if(_emailTem==null){
				throw new RecordNotFoundException(_messages.format("entity-not-exists", "emailTemID", _id));
			}
    	}
    }
    
    @Component
    private BeanEditForm emailTemForm;
    @Component(id="type")
    private TextField typeField;
    
    void onValidateFormFromEmailTemForm(){
    	//check 'type' and 'language' not exist before creating new
    	if(isCreateMode() && _emailTemDAO.getEmailTemplateByTypeAndLanguage(_emailTem.getType(), _emailTem.getLanguage())!=null){
    		emailTemForm.recordError(typeField, _messages.format("duplicate-key-exception"
    				, "Type-Language pair", _emailTem.getType()+" - "+ _emailTem.getLanguage()));
    	}
    	
    	//TODO: LOW priority,  'subject' and 'content' only contain valid variable
    	
    }
    
    Object onSuccessFromEmailTemForm()
    {
    	_emailTem.setContent(Util.filterOutRestrictedHtmlTags(_emailTem.getContent()));
    	_emailTem.setType(_emailTem.getType().trim());
    	_emailTem.setModifyDate(new Date());
		_emailTemDAO.save(_emailTem);
		return EmailTemplateIndex.class;
    }
   
    public List<String> getEmailTemplateVariables(){
    	return _emailManager.getEmailTemplateVariables();
    }
    
    public List<String> getEmailTemplateTypes(){
//    	return emailManager.getEmailTemplateTypes();
    	List<String> l = new ArrayList<String>();
    	EmailTemplateConstants etConstant = new EmailTemplateConstants();
    	Field[] fields = EmailTemplateConstants.class.getFields();
    	for(Field f : fields){
    		try {
				l.add((String) f.get(etConstant) );
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
    	}
    	
    	return l;
    	
    }
   public List<String> getUnDefinedEmailTemplateTypes(){
	   List<String> l = new ArrayList<String>();
	   String language = localeService.isSet()? localeService.get().getLanguage() : "en";
		
	   for(String s : getEmailTemplateTypes()){
		   EmailTemplate et = _emailTemDAO.getEmailTemplateByTypeAndLanguage(s, language);
		   if(et==null){
			   l.add(s);
		   }
	   }
	   return l;
   }
	
}
