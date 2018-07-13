package ntu.celt.eUreka2.pages.modules.elog;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.elog.Elog;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.elog.ElogFile;
import ntu.celt.eUreka2.modules.elog.ElogStatus;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;

public class Submit extends AbstractPageElog {
	@Property
	private Project project;
	@Property
	private Elog elog;
	@Property
	private String eid;
	
//	@Persist
	@Property
	private String subject;
//	@Persist
	@Property
	private String content;
	
	
	@SuppressWarnings("unused")
	@Property
	private ElogFile attFile;
	private boolean submitForApproval;
	
	
	@SessionState
	private AppState appState;
	@Inject
	private ElogDAO eDAO;
	@Inject
	private Request request;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private EmailManager emailManager;
	@Inject
	private Logger logger;
	@Inject
	private Response response;
	
	void onActivate(EventContext ec) {
		eid = ec.get(String.class, 0);
		subject = ec.get(String.class, 1);
		content = ec.get(String.class, 2);
	}
	Object[] onPassivate() {
	   return new Object[] {eid, subject, content};
	}
	
	public void setContext(String eid, String subject, String content){
		this.eid = eid;
		this.subject = subject;
		this.content = content;
	}
	
	
	void setupRender() {
		//elog must have been created by now, as draft, in order to save the uploaded files in previous step 'Edit'
		elog = eDAO.getElogById(eid);
		if(elog==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ElogId", eid));
		project = elog.getProject();
		if(!canEditElog(elog)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		response.setIntHeader("X-XSS-Protection", 0); //to let browser execute script after the script was added, 
										// e.g. youtube iframe was added
		
	}
	void onPrepareForSubmitFromElogForm(){
		elog = eDAO.getElogById(eid);
		project = elog.getProject();
	}
	
	@InjectPage
	private Edit editPage;
	Object onSelectedFromBack(){
		editPage.setContext(eid, subject, content);
		return editPage;
	}
	void onSelectedFromSubmitApproval(){
		submitForApproval = true;
	}
	
	void onValidateFormFromElogForm(){

	}
	@CommitAfter
	Object onSuccessFromElogForm() {
		elog.setSubject(Util.filterOutRestrictedHtmlTags(Util.nvl(subject)));
		elog.setContent(Util.filterOutRestrictedHtmlTags(Util.nvl(content)));
		elog.setAuthor(getCurUser());
		elog.setEditor(getCurUser());
		elog.setMdate(new Date());
		elog.setIp((String) request.getSession(true).getAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME));
		elog.setProject(project);
		eDAO.updateElog(elog);
		
		if(submitForApproval){
			List<User> approvers = getApprovers(project);
			
			if(approvers.isEmpty()){ //if still no approvers, skip and publish the eLog automatically
				elog.setStatus(ElogStatus.APPROVED);
				elog.setPublished(true);
				elog.appendlnHtmlRemarks("<span class='date'>"+Util.formatDateTime2(new Date())+"</span> - "+messages.get("remarks-auto-approved"));
				eDAO.updateElog(elog);
				appState.recordInfoMsg(messages.get("request-auto-approved" ));
			}
			else{
				
				try{
					elog.setStatus(ElogStatus.PENDING);
					elog.appendlnHtmlRemarks("<span class='date'>"+Util.formatDateTime2(new Date())
							+"</span> - "+messages.format("remarks-request-sent-by-x", getCurUser().getDisplayName()));
					eDAO.updateElog(elog);
					
					EmailTemplateVariables var = new EmailTemplateVariables(
							elog.getCdate().toString(), elog.getMdate().toString(),
							elog.getSubject(),
							Util.truncateString(Util.stripTags(elog.getContent()), Config.getInt("max_content_lenght_in_email")), 
							elog.getAuthor().getDisplayName(),
							project.getDisplayName(), 
							emailManager.createLinkBackURL(request, Approve.class, elog.getId()), 
							null, null);
					
					
					//send email to notify approver(s)
					emailManager.sendHTMLMail(approvers, EmailTemplateConstants.ELOG_REQUEST_APPROVAL, var);
					appState.recordInfoMsg(messages.format("request-approval-sent-to-x", Util.extractDisplayNames(approvers)));
				}
				catch(SendEmailFailException e){
					logger.warn(e.getMessage(), e);
		//			appState.recordWarningMsg(e.getMessage());
				}
			}
			return linkSource.createPageRenderLinkWithContext(Manage.class, project.getId());
		}
		else{
			//else, save-as-draft
			appState.recordInfoMsg(messages.format("save-draft-on-x", Util.formatDateTime2(new Date())));
		}
		return null;
	}
	
}
