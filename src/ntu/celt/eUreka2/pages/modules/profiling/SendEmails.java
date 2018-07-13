package ntu.celt.eUreka2.pages.modules.profiling;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.profiling.ProfilingDAO;
import ntu.celt.eUreka2.modules.profiling.Profiling;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;

public class SendEmails extends AbstractPageProfiling {
	@Persist(PersistenceConstants.CLIENT)
	private long profId ;
	@Persist(PersistenceConstants.CLIENT)
	private String[] userIDs;
	@Property
	private Profiling prof;
	@Property
	private int isAll = 0;
	
	
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
	private ProfilingDAO profDAO;
	@Inject
	private UserDAO userDAO;
	
	void onActivate(EventContext ec) {
		if(ec.getCount()==2 && ec.get(Integer.class, 1)==1) {				
			profId = ec.get(Long.class, 0);
			prof = profDAO.getProfilingById(profId);
			if(prof==null){
				throw new RecordNotFoundException(messages.format("entity-not-exists", "Profiling ID", profId));
			}
			project = prof.getProject();

			users = getNotSubmitedAssesseesByProfiling(prof);
			userIDs = new String[users.size()];
			int i = 0;
			for(User u: users) {
				userIDs[i] = (String.valueOf(u.getId()));
				i++;
			}
		}
	}
	
	Object[] onPassivate() {
		if(isAll == 0) {
			return new Object[] {profId};
		}
		else {
			return new Object[] {profId, isAll};
		}
	}
	
	public void setContext(Long profId, String[] userIDs){
		this.profId = profId;
		this.userIDs = userIDs;
	}

	
	/*oid setupRender(){
		if(!canManageProfilingSurvey(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		subject = messages.format("default-email-subject-x-y",  prof.getName(), prof.getEdateDisplay() );
		msgContent = messages.get("default-email-content");
	
		users = getNotSubmitedAssesseesByProfilingSurvey(prof);
	}
	void onPrepareForSubmitFromForm(){  
		users = getNotSubmitedAssesseesByProfilingSurvey(prof);
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
		prof = profDAO.getProfilingById(profId);
		if(prof==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Profiling ID", profId));
		}
		
		project = prof.getProject();
		
		if(!canManageProfiling(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}

		
		subject = messages.format("default-email-subject-x-y",  prof.getName(), prof.getEdateDisplay() );
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
		prof = profDAO.getProfilingById(profId);
			
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
//		profId = 0;
		userIDs = null;
		return linkSource.createPageRenderLinkWithContext(CheckStatus.class, profId);
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
		return linkSource.createPageRenderLinkWithContext(CheckStatus.class, prof.getId());
	}
	

	
}
