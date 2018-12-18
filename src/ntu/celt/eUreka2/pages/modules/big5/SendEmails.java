package ntu.celt.eUreka2.pages.modules.big5;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.big5.BIG5DAO;
import ntu.celt.eUreka2.modules.big5.BIG5Survey;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;

public class SendEmails extends AbstractPageBIG5 {
	@Persist(PersistenceConstants.CLIENT)
	private long big5Id ;
	@Persist(PersistenceConstants.CLIENT)
	private String[] userIDs;
	@Property
	private BIG5Survey big5;
	
	@SessionState
	private AppState appState;
	@Property
	private Project project;
	@Property
	private User user;
	@Property
	private List<User> users;
	@Property
	private String subject;
	@Property
	private String msgContent;
	
	
	
	@Inject
	private Logger logger;
	@Inject
	private EmailManager emailManager;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject 
	private Messages messages;
	@Inject
	private BIG5DAO big5DAO;
	@Inject
	private UserDAO userDAO;
	
	/*void onActivate(EventContext ec) {
		id = ec.get(Long.class, 0);
		
		big5 = big5DAO.getBIG5SurveyById(id);
		if(big5==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "BIG5SurveyID", id));
		}
		project = big5.getProject();
	}
	
	Object[] onPassivate() {
		return new Object[] {id};
	}
*/	
	public void setContext(Long big5Id, String[] userIDs){
		this.big5Id = big5Id;
		this.userIDs = userIDs;
	}

	
	/*oid setupRender(){
		if(!canManageBIG5Survey(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		subject = messages.format("default-email-subject-x-y",  big5.getName(), big5.getEdateDisplay() );
		msgContent = messages.get("default-email-content");
	
		users = getNotSubmitedAssesseesByBIG5Survey(big5);
	}
	void onPrepareForSubmitFromForm(){  
		users = getNotSubmitedAssesseesByBIG5Survey(big5);
	}
	
	
	Object onSelectedFromCancel(){
		return linkSource.createPageRenderLinkWithContext(Manage.class, project.getId());
	}
	
	
	Object onSuccessFromForm(){
			String fromAddr = getCurUser().getEmail();
			//String toAddr = user.getEmail();
			for(User u : users){
				List<String> emails = new ArrayList<String>();
				emails.add(u.getEmail());
			
				try{
					emailManager.sendHTMLMail(emails, fromAddr, subject, msgContent);
					appState.recordInfoMsg(messages.get("message-successfully-sent"));
				}
				catch(SendEmailFailException e){
					String errMsg =  messages.get("sendEmailFailedException-message") + e.getMessage(); 
					logger.warn(errMsg, e);
					appState.recordWarningMsg(errMsg);
				}
			}
		return linkSource.createPageRenderLinkWithContext(Manage.class, project.getId());
	}
	*/
	
	
	void setupRender(){
		big5 = big5DAO.getBIG5SurveyById(big5Id);
		if(big5==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "BIG5 ID", big5Id));
		}
		
		project = big5.getProject();
		
		if(!canManageBIG5Survey(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}

		
		subject = messages.format("default-email-subject-x-y",  big5.getName(), big5.getEdateDisplay() );
		msgContent = messages.get("default-email-content");
	
		users = new ArrayList<User>();
		if(userIDs!=null){
			for(String userId: userIDs){
				user = userDAO.getUserById(Integer.parseInt(userId));
				if(user!=null)
					users.add(user);
			}
		}
	}
	void onPrepareForSubmitFromForm(){  
		big5 = big5DAO.getBIG5SurveyById(big5Id);
			
		users = new ArrayList<User>();
		if(userIDs!=null){
			for(String userId: userIDs){
				user = userDAO.getUserById(Integer.parseInt(userId));
				if(user!=null)
					users.add(user);
			}
		}
	}
	
	Object onSelectedFromCancel(){
//		big5Id = 0;
		userIDs = null;
		return linkSource.createPageRenderLinkWithContext(CheckStatus.class, big5Id);
	}
	
	
	Object onSuccessFromForm(){
			String fromAddr = getCurUser().getEmail();
			//String toAddr = user.getEmail();
			for(User u : users){
				List<String> emails = new ArrayList<String>();
				emails.add(u.getEmail());
			
				try{
					emailManager.sendHTMLMail(emails, fromAddr, subject, msgContent);
					appState.recordInfoMsg(messages.get("message-successfully-sent"));
				}
				catch(SendEmailFailException e){
					String errMsg =  messages.get("sendEmailFailedException-message") + e.getMessage(); 
					logger.warn(errMsg, e);
					appState.recordWarningMsg(errMsg);
				}
			}
		return linkSource.createPageRenderLinkWithContext(CheckStatus.class, big5.getId());
	}
	

	
}
