package ntu.celt.eUreka2.pages.modules.ipsp;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.ipsp.IPSPDAO;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurvey;
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

public class SendEmails extends AbstractPageIPSP {
	@Persist(PersistenceConstants.CLIENT)
	private long ipspId ;
	@Persist(PersistenceConstants.CLIENT)
	private String[] userIDs;
	@Property
	private IPSPSurvey ipsp;
	
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
	private IPSPDAO ipspDAO;
	@Inject
	private UserDAO userDAO;
	
	/*void onActivate(EventContext ec) {
		id = ec.get(Long.class, 0);
		
		ipsp = ipspDAO.getIPSPSurveyById(id);
		if(ipsp==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "IPSPSurveyID", id));
		}
		project = ipsp.getProject();
	}
	
	Object[] onPassivate() {
		return new Object[] {id};
	}
*/	
	public void setContext(Long ipspId, String[] userIDs){
		this.ipspId = ipspId;
		this.userIDs = userIDs;
	}

	
	/*oid setupRender(){
		if(!canManageIPSPSurvey(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		subject = messages.format("default-email-subject-x-y",  ipsp.getName(), ipsp.getEdateDisplay() );
		msgContent = messages.get("default-email-content");
	
		users = getNotSubmitedAssesseesByIPSPSurvey(ipsp);
	}
	void onPrepareForSubmitFromForm(){  
		users = getNotSubmitedAssesseesByIPSPSurvey(ipsp);
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
		ipsp = ipspDAO.getIPSPSurveyById(ipspId);
		if(ipsp==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "IPSP ID", ipspId));
		}
		
		project = ipsp.getProject();
		
		if(!canManageIPSPSurvey(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}

		
		subject = messages.format("default-email-subject-x-y",  ipsp.getName(), ipsp.getEdateDisplay() );
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
		ipsp = ipspDAO.getIPSPSurveyById(ipspId);
			
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
//		ipspId = 0;
		userIDs = null;
		return linkSource.createPageRenderLinkWithContext(CheckStatus.class, ipspId);
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
		return linkSource.createPageRenderLinkWithContext(CheckStatus.class, ipsp.getId());
	}
	

	
}
