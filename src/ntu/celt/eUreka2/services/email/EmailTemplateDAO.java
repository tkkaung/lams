package ntu.celt.eUreka2.services.email;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

public interface EmailTemplateDAO {
	
	List<EmailTemplate> getAllEmailTemplates();
	@CommitAfter
	void save(EmailTemplate emailTemplate);
	EmailTemplate getEmailTemplateById(Long id);
	@CommitAfter
	void delete(EmailTemplate emailTemplate);
	
	/**
	 * 
	 * @param emailType
	 * 		type as defined in EmailTemplateConstant
	 * @param language
	 * 		if null, the default 'en' is used
	 * @return
	 */
	EmailTemplate getEmailTemplateByTypeAndLanguage(String emailType, String language);
	
	List<EmailTemplate> searchEmailTemplates(String searchText);
	
}
