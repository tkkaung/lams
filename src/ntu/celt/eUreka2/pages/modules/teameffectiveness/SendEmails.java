package ntu.celt.eUreka2.pages.modules.teameffectiveness;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.teameffectiveness.TEDAO;
import ntu.celt.eUreka2.modules.teameffectiveness.TESurvey;
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

public class SendEmails extends AbstractPageTE {
	@Persist(PersistenceConstants.CLIENT)
	private long teId ;
	@Persist(PersistenceConstants.CLIENT)
	private String[] userIDs;
	@Property
	private TESurvey te;
	
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
	private TEDAO teDAO;
	@Inject
	private UserDAO userDAO;
	
	/*void onActivate(EventContext ec) {
		id = ec.get(Long.class, 0);
		
		te = teDAO.getTESurveyById(id);
		if(te==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "TESurveyID", id));
		}
		project = te.getProject();
	}
	
	Object[] onPassivate() {
		return new Object[] {id};
	}
*/	
	public void setContext(Long teId, String[] userIDs){
		this.teId = teId;
		this.userIDs = userIDs;
	}

	
	/*oid setupRender(){
		if(!canManageTESurvey(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		subject = messages.format("default-email-subject-x-y",  te.getName(), te.getEdateDisplay() );
		msgContent = messages.get("default-email-content");
	
		users = getNotSubmitedAssesseesByTESurvey(te);
	}
	void onPrepareForSubmitFromForm(){  
		users = getNotSubmitedAssesseesByTESurvey(te);
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
		te = teDAO.getTESurveyById(teId);
		if(te==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "TE ID", teId));
		}
		
		project = te.getProject();
		
		if(!canManageTESurvey(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}

		
		subject = messages.format("default-email-subject-x-y",  te.getName(), te.getEdateDisplay() );
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
		te = teDAO.getTESurveyById(teId);
			
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
//		teId = 0;
		userIDs = null;
		return linkSource.createPageRenderLinkWithContext(CheckStatus.class, teId);
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
		return linkSource.createPageRenderLinkWithContext(CheckStatus.class, te.getId());
	}
	

	
}
