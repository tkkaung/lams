package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
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

public class SendEmail extends AbstractPageEvaluation {
	@SessionState
	private AppState appState;
	@Property
	private Project project;
	private long evalId ;
	private int uId;
	@Property
	private Evaluation eval;
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
	private EvaluationDAO evalDAO;
	
	void onActivate(EventContext ec) {
		evalId = ec.get(Long.class, 0);
		uId = ec.get(Integer.class, 1);
		
		eval = evalDAO.getEvaluationById(evalId);
		if(eval==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Evluation ID", evalId));
		}
		user = userDAO.getUserById(uId);
		if(user==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "User ID", uId));
		}
		
		project = eval.getProject();
		
	}
	Object[] onPassivate() {
		return new Object[] {evalId, uId};
	}
	
	void setupRender(){
		if(!canManageEvaluation(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		subject = messages.format("default-email-subject-x-y",  eval.getName(), eval.getEdateDisplay() );
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
		return linkSource.createPageRenderLinkWithContext(CheckStatus.class, eval.getId());
	}
	
	
	
}
