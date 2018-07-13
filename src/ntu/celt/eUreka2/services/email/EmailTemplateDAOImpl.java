package ntu.celt.eUreka2.services.email;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class EmailTemplateDAOImpl implements EmailTemplateDAO {

	private Session session;
	
	public EmailTemplateDAOImpl(Session session) {
		super();
		this.session = session;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<EmailTemplate> getAllEmailTemplates() {
		return session.createCriteria(EmailTemplate.class).addOrder(Order.asc("type")).list();
	}
	
	@Override
	public void save(EmailTemplate emailTemplate) {
		session.save(emailTemplate);
	}
	@Override
	public void delete(EmailTemplate emailTemplate) {
		session.delete(emailTemplate);
	}
	
	
	
	@Override
	public EmailTemplate getEmailTemplateById(Long id) {
		return (EmailTemplate) session.get(EmailTemplate.class, id);
	}

	@Override
	public EmailTemplate getEmailTemplateByTypeAndLanguage(String emailType, String language) {
		if(language==null)
			language = "en";
		Criteria c = session.createCriteria(EmailTemplate.class)
				.add(Restrictions.eq("type", emailType))
				.add(Restrictions.eq("language", language))
				;
		return (EmailTemplate) c.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<EmailTemplate> searchEmailTemplates(String searchText) {
		Criteria crit = session.createCriteria(EmailTemplate.class);
		
		if(searchText != null){
			String[] words = searchText.split(" ");
			for(String word : words){
				crit = crit.add(Restrictions.or(Restrictions.like("type", "%"+word+"%"),
						Restrictions.like("description", "%"+word+"%" )));
			}
		}
		
		return crit.list();
	}

	

	

}
