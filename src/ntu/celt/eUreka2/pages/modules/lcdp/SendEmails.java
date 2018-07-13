package ntu.celt.eUreka2.pages.modules.lcdp;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.lcdp.LCDPDAO;
import ntu.celt.eUreka2.modules.lcdp.LCDPSurvey;
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

public class SendEmails extends AbstractPageLCDP {
	@Persist(PersistenceConstants.CLIENT)
	private long lcdpId ;
	@Persist(PersistenceConstants.CLIENT)
	private String[] userIDs;
	@Property
	private LCDPSurvey lcdp;
	
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
	private LCDPDAO lcdpDAO;
	@Inject
	private UserDAO userDAO;
	
	/*void onActivate(EventContext ec) {
		id = ec.get(Long.class, 0);
		
		lcdp = lcdpDAO.getLCDPSurveyById(id);
		if(lcdp==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "LCDPSurveyID", id));
		}
		project = lcdp.getProject();
	}
	
	Object[] onPassivate() {
		return new Object[] {id};
	}
*/	
	public void setContext(Long lcdpId, String[] userIDs){
		this.lcdpId = lcdpId;
		this.userIDs = userIDs;
	}

	
	/*oid setupRender(){
		if(!canManageLCDPSurvey(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		subject = messages.format("default-email-subject-x-y",  lcdp.getName(), lcdp.getEdateDisplay() );
		msgContent = messages.get("default-email-content");
	
		users = getNotSubmitedAssesseesByLCDPSurvey(lcdp);
	}
	void onPrepareForSubmitFromForm(){  
		users = getNotSubmitedAssesseesByLCDPSurvey(lcdp);
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
		lcdp = lcdpDAO.getLCDPSurveyById(lcdpId);
		if(lcdp==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "LCDP ID", lcdpId));
		}
		
		project = lcdp.getProject();
		
		if(!canManageLCDPSurvey(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}

		
		subject = messages.format("default-email-subject-x-y",  lcdp.getName(), lcdp.getEdateDisplay() );
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
		lcdp = lcdpDAO.getLCDPSurveyById(lcdpId);
			
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
//		lcdpId = 0;
		userIDs = null;
		return linkSource.createPageRenderLinkWithContext(CheckStatus.class, lcdpId);
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
		return linkSource.createPageRenderLinkWithContext(CheckStatus.class, lcdp.getId());
	}
	

	
}
