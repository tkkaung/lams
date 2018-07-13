package ntu.celt.eUreka2.services.email;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.tapestry5.services.Request;

import ntu.celt.eUreka2.entities.User;

public interface EmailManager {
	/**
	 * 
	 * @param toUsers
	 * @param emailType
	 * 		type as defined in EmailTemplateConstant
	 * @param language
	 * @param objInfo
	 * 		properties required to use in emailTemplate
	 */
	void sendHTMLMail(Collection<User> toUsers, String emailType, 
			EmailTemplateVariables itemVar, File... attachedFiles );
	void sendHTMLMail(Collection<User> toUsers, Collection<String> toEmails, String emailType, 
			EmailTemplateVariables itemVar, File... attachedFiles );
	void sendHTMLMail(Collection<User> toUsers, Collection<String> toEmails, String fromEmail, String emailType, 
			EmailTemplateVariables itemVar, File... attachedFiles );
	void sendHTMLMail(Collection<String> toEmails, String fromEmail, String subject, String body, File... attachedFiles);
	void sendHTMLMail(User toUser, String emailType,
			EmailTemplateVariables itemVar, File... attachedFiles);
	
	//String replaceEmailTemplate(String template, EmailTemplateVariables objInfo);
	
	String wrapFieldName(String fieldName);
	
	List<String> getEmailTemplateVariables();
	@SuppressWarnings("unchecked")
	String createLinkBackURL(Request request, Class clazz, Object... contexts );
	
}
