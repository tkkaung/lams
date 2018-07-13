package ntu.celt.eUreka2.pages.modules.group;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupDAO;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;

public class SendEmail extends AbstractPageGroup {
	@SessionState
	private AppState appState;
	@Property
	private Project curProj;
	private long gId ;
	private int uId;
	@Property
	private Group group;
	@Property
	private User user;
	@Property
	private String subject;
	@Property
	private String msgContent;
	
	
	@Inject
	private UserDAO userDAO;
	@Inject
	private Logger logger;
	@Inject
	private EmailManager emailManager;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject 
	private Messages messages;
	@Inject
	private GroupDAO evalDAO;
	
	void onActivate(EventContext ec) {
		gId = ec.get(Long.class, 0);
		uId = ec.get(Integer.class, 1);
		
		group = evalDAO.getGroupById(gId);
		if(group==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Group ID", gId));
		}
		user = userDAO.getUserById(uId);
		if(user==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "User ID", uId));
		}
		
		curProj = group.getProject();
		
	}
	Object[] onPassivate() {
		return new Object[] {gId, uId};
	}
	
	void setupRender(){
		if(!canManageGroup(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		subject = messages.format("default-email-subject-x",  curProj.getDisplayName() );
		msgContent = messages.get("default-email-content");
	}
	
	Object onSuccessFromForm(){
			String fromAddr = getCurUser().getEmail();
			String toAddr = user.getEmail();
			List<String> emails = new ArrayList<String>();
			emails.add(toAddr);
			
			try{
				emailManager.sendHTMLMail(emails, fromAddr, subject, msgContent);
				appState.recordInfoMsg(messages.get("message-successfully-sent"));
			}
			catch(SendEmailFailException e){
				String errMsg =  messages.get("sendEmailFailedException-message") + e.getMessage(); 
				logger.warn(errMsg, e);
				appState.recordWarningMsg(errMsg);
			}
		return linkSource.createPageRenderLinkWithContext(GroupCheckStatus.class, group.getId());
	}
	
	
	
}
