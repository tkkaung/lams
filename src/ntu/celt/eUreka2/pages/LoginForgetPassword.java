package ntu.celt.eUreka2.pages;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;
import ntu.celt.eUreka2.services.email.SendEmailFailException;
import ntu.celt.eUreka2.annotation.PublicPage;
import ntu.celt.eUreka2.dao.UserDAO;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;
import org.slf4j.Logger;

@PublicPage
public class LoginForgetPassword
{
	
	@Property
	private String username = "";
	
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String ctext1;
	@Property
	private String ctext2;
	
	@SuppressWarnings("unused")
	@Persist("flash")
	@Property
	private String resultMsg;
	
	
	@Inject 
	private Logger logger;
	@Inject 
	private UserDAO userDAO;
	
	@Inject
    private RequestGlobals requestGlobal;
	@Inject
    private Request request;
	@Inject
	private Messages messages;
	@Inject
	private EmailManager emailManager;
	
	@Component
	private Form form;

	void setupRender(){
		
		ctext1 = RandomStringUtils.random(5, "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789");
	}
	
	void onValidateFormFromForm()
	{
		User user = userDAO.getUserByUsername(username);
		if (user == null){
			form.recordError(messages.get("invalid-username"));
			return;
		}
		if(!user.isEnabled()){
			form.recordError(messages.format("account-x-disabled", username));
			return;
		}
		
		if(ctext1!=null && !ctext1.equalsIgnoreCase(ctext2)){
			form.recordError(messages.get("confirm-text-not-matched"));
		}
	}
	
	
	
	@CommitAfter
	void onSuccessFromForm() throws IOException 
	{
		//generate email
		Date time = new Date();
		String random = RandomStringUtils.random(6, "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789");
		String hash = ResetPwd.generateHash(username, time.getTime(), random);
		User user = userDAO.getUserByUsername(username);
		List<User> uList = new ArrayList<User>();
		uList.add(user);
		String partialEmail = Util.getPartialEmail(user.getEmail());
		
		EmailTemplateVariables var = new EmailTemplateVariables(
				time.toString(), 
				time.toString(),
				messages.get("email-forgetpassword-item-title"), messages.get("email-forgetpassword-item-content"), 
				user.getDisplayName(),
				null, 
				emailManager.createLinkBackURL(request, ResetPwd.class, username, time.getTime(), random, hash),
				username,
				requestGlobal.getHTTPServletRequest().getRemoteAddr()
				);
		
		try{
			emailManager.sendHTMLMail(uList, EmailTemplateConstants.AUTH_RESET_PASSWORD, var);
			resultMsg = messages.format("success-to-send-reset-password-email-x", partialEmail);
		}
		catch(SendEmailFailException e){
			logger.warn(e.getMessage(), e);
			resultMsg = messages.format("fail-to-send-reset-password-email-x", partialEmail)+ ".\n Error: "+e.getMessage();
			return ;
		}
	}

	
	
}
