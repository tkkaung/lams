package ntu.celt.eUreka2.services.email;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.PersistentLocale;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

import com.sun.mail.smtp.SMTPAddressFailedException;
import com.sun.mail.smtp.SMTPSendFailedException;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;

public class EmailManagerImpl implements EmailManager {

	private EmailTemplateDAO emailTemDao;
	private PageRenderLinkSource linkSource;
	private Logger logger;
	private PersistentLocale localeService;
	
	public EmailManagerImpl(
			EmailTemplateDAO emailTemDao, 
			PageRenderLinkSource linkSource, 
			Logger logger,
			PersistentLocale localeService
			) {
		super();
		this.emailTemDao = emailTemDao;
		this.linkSource = linkSource;
		this.logger = logger;
		this.localeService = localeService;
	}
	private EmailDelivery getInitEmailDelivery() throws MessagingException {
		EmailDelivery ed = new EmailDelivery();
		ed.setSMTPPort(Config.getInt("mail.smtp.port"));
		ed.setSMTPHost(Config.getString("mail.smtp.host"), 
					Config.getString("mail.smtp.user"), 
					Config.getString("mail.smtp.password"),
					Config.getString("mail.smtp.sendpartial")
					);
		
		return ed;
	}
	
	@Override
	public void sendHTMLMail(User toUser, String emailType, 
			EmailTemplateVariables itemVar, File... attachedFiles) {
		Collection<User> toUsers = new ArrayList<User>();
		toUsers.add(toUser);
		
		sendHTMLMail(toUsers, null, null, emailType, itemVar, attachedFiles);
	}
	
	@Override
	public void sendHTMLMail(Collection<User> toUsers, String emailType, 
			EmailTemplateVariables itemVar, File... attachedFiles) {
		sendHTMLMail(toUsers, null, null, emailType, itemVar, attachedFiles);
	}
	
	@Override
	public void sendHTMLMail(Collection<User> toUsers, Collection<String> toEmails, String emailType, 
			EmailTemplateVariables itemVar, File... attachedFiles) {
		sendHTMLMail(toUsers, toEmails, null, emailType, itemVar, attachedFiles);
	}
	
	@Override
	public void sendHTMLMail(Collection<User> toUsers, Collection<String> toEmails, String fromEmail, String emailType, 
			EmailTemplateVariables itemVar, File... attachedFiles) {
		if(fromEmail==null || fromEmail.length()==0)
			fromEmail = Config.getString(Config.SYSTEM_EMAIL_ADDRESS);
		
		String language = localeService.isSet()? localeService.get().getLanguage() : "en";
		
		EmailTemplate emailTem = emailTemDao.getEmailTemplateByTypeAndLanguage(emailType, language);
		if(emailTem==null){
			//throw new SendEmailFailException("Email template not found. emailType="+emailType+" language="+language);
			//or use Default:
			emailTem = emailTemDao.getEmailTemplateByTypeAndLanguage(EmailTemplateConstants.DEFAULT, "en");
		}
		
		List<String> toList = new ArrayList<String>();
		for(User u : toUsers){
			toList.add(u.getEmail());
		}
		if(toEmails!=null)
			toList.addAll(toEmails);
		
		sendHTMLMail(toList, fromEmail, replaceEmailTemplate(emailTem.getSubject(), itemVar), 
				replaceEmailTemplate(emailTem.getContent(), itemVar), attachedFiles);
		
	}
	public void sendHTMLMail(Collection<String> toEmails, String fromEmail, String subject, String body
			, File... attachedFiles){
		boolean sendMailEnabled = Config.getBoolean("mail.enable.sending");
		if(!sendMailEnabled){
			logger.warn("Trying to send email while Email function is disabled. From="+fromEmail
					+ " To="+Util.collectionToString(toEmails, ",")+" Subject="+subject);
			//throw new SendEmailFailException("Email function is disabled.\n" );
			return ;
		}
		String to = "";
		String seperator = ",";
		if(toEmails==null)
			return; //don't send anything
		to = Util.collectionToString(toEmails, seperator);
		
		try{
			EmailDelivery ed = getInitEmailDelivery();
			ed.setTo(to);
	        ed.setFrom(fromEmail);
	        ed.setSubject(subject);
	        ed.setBodyHTML(body);
	        for(File f : attachedFiles){
	        	ed.addFileAttachment(f.getAbsolutePath());
	        }
	        
			//System.err.println(".......email, to " + to + " ---- from:"+ fromEmail + " ----subject:"+ subject + " ----body:"+ body + " ----");
			
	        ed.sendMsgHTML();  
		}
		catch (SMTPSendFailedException e){
			Exception ex = e.getNextException();
			String invalidEmails = "";
			if(ex instanceof SMTPAddressFailedException){
				do{
					SMTPAddressFailedException exc = (SMTPAddressFailedException) ex;
					exc.printStackTrace();
					exc.getMessage();
					InternetAddress addr = exc.getAddress();
					invalidEmails += addr.getAddress() + seperator;
					
					ex = exc.getNextException();
				} while(ex instanceof SMTPAddressFailedException);
				
				invalidEmails = Util.removeLastSeparator(invalidEmails, seperator);
				
				throw new SendEmailFailException("Fail to send notification emails to some email address(es): " + invalidEmails	);
			}
			else
				throw new SendEmailFailException("Fail to send notification emails to users.", e);
		}
		catch (MessagingException e) {
			e.printStackTrace();	
			throw new SendEmailFailException("Fail to send notification emails to users. \n", e );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();	
			throw new SendEmailFailException("Fail to send notification emails to users. \n", e);
		}
	}
	
	private String replaceEmailTemplate(String template, EmailTemplateVariables itemVar) {
		for(Field f : itemVar.getClass().getFields() ){
			String name = f.getName();
			try {
				String s = wrapFieldName(name);
				String value = (String) f.get(itemVar);
				if(value==null)
					value = "NULL";
				
				template = template.replaceAll("(?i)"+ s, Matcher.quoteReplacement(value));   //replace with ignorecase
				
			} catch (IllegalArgumentException e) {
				logger.error(e.getMessage());
				e.printStackTrace() ;
			} catch (IllegalAccessException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		
		return template;
	}
	
	@Override
	public String wrapFieldName(String fieldName){
		return "%" + fieldName + "%";
	}

	private List<String> emailTemplateVariables;
	@Override
	public List<String> getEmailTemplateVariables() {
		if(emailTemplateVariables==null){
			emailTemplateVariables = new ArrayList<String>();
			for(Field f : EmailTemplateVariables.class.getFields() ){

				emailTemplateVariables.add(wrapFieldName(f.getName()));
			}
		}
		return emailTemplateVariables;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String createLinkBackURL(Request request, Class clazz,
			Object... contexts) {
		String link =  linkSource.createPageRenderLinkWithContext(clazz, contexts).toAbsoluteURI(); 
	
		return link;
	}
	
	

}
