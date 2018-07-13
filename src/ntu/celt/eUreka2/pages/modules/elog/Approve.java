package ntu.celt.eUreka2.pages.modules.elog;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
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
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;

public class Approve extends AbstractPageElog{
	private String eid;
	@Property
	private Elog elog;
	@Property
	private Project project;
	@SuppressWarnings("unused")
	@Property
	private ElogFile attFile;
	@Property
	private String remarks;
	@Property
	private ApproveDecision decision;

	public enum ApproveDecision {APPROVE_N_PUBLISH, APPROVE_N_UNPUBLISH, REJECT};
	
	@SessionState
	private AppState appState;
	
	@Inject
	private ElogDAO eDAO;
	@Inject
	private Messages messages;
	@Inject
	private EmailManager emailManager;
	@Inject
	private Logger logger;
	@Inject
	private Request request;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Response response;
	
	void onActivate(EventContext ec) {
		eid = ec.get(String.class, 0);
	}
	Object[] onPassivate() {
		return new Object[] {eid};
	}
	
	
	
	void setupRender() {
		elog = eDAO.getElogById(eid);
		if(elog==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ElogId", eid));
		
		project = elog.getProject();
		
		if(!canApproveElog(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		response.setIntHeader("X-XSS-Protection", 0); //to let browser execute script after the script was added, 
													// e.g. youtube iframe was added
	}	
	
	void onPrepareForSubmitFromForm(){
		elog = eDAO.getElogById(eid);
		project = elog.getProject();
	}
	@CommitAfter
	Object onSuccessFromForm(){
		EmailTemplateVariables var;
		List<User> recipients = new ArrayList<User>();
		
		switch(decision){
			case APPROVE_N_PUBLISH:
				elog.setStatus(ElogStatus.APPROVED);
				elog.setPublished(true);
				elog.appendlnHtmlRemarks("<span class='date'>"+Util.formatDateTime2(new Date())+"</span> - "
						+messages.format("remarks-approved-by-x", getCurUser().getDisplayName()));
				elog.appendlnHtmlRemarks("<span class='date'>"+Util.formatDateTime2(new Date())+"</span> - "
						+messages.format("remarks-published-by-x", getCurUser().getDisplayName()));
			
				//prepare to email to update students and other approvers
				recipients = project.getUsers();
				var = new EmailTemplateVariables(
						elog.getCdate().toString(),(new Date()).toString(),
						elog.getSubject(), 
						Util.truncateString(Util.stripTags(elog.getContent()), Config.getInt("max_content_lenght_in_email")), 
						getCurUser().getDisplayName(),
						project.getDisplayName(), 
						emailManager.createLinkBackURL(request, View.class, elog.getId()), 
						elog.getAuthor().getDisplayName(), remarks);
				try{
					emailManager.sendHTMLMail(recipients, EmailTemplateConstants.ELOG_APPROVED, var);
					appState.recordInfoMsg(messages.format("approval-update-sent-to-x", Util.extractDisplayNames(recipients)));
				}
				catch(SendEmailFailException e){
					logger.warn(e.getMessage(), e);
				//	appState.recordWarningMsg(e.getMessage());
				}
				
				
				break;
			case APPROVE_N_UNPUBLISH:
				elog.setStatus(ElogStatus.APPROVED);
				elog.setPublished(false);
				elog.appendlnHtmlRemarks("<span class='date'>"+Util.formatDateTime2(new Date())+"</span> - "
						+messages.format("remarks-approved-by-x", getCurUser().getDisplayName()));
				
				
				//prepare to email to update students and other approvers
				recipients.add(elog.getAuthor());
				recipients.addAll(getApprovers(project));
				var = new EmailTemplateVariables(
						(new Date()).toString(), elog.getMdate().toString(),
						elog.getSubject(), 
						Util.truncateString(Util.stripTags(elog.getContent()), Config.getInt("max_content_lenght_in_email")), 
						getCurUser().getDisplayName(),
						project.getDisplayName(), 
						emailManager.createLinkBackURL(request, View.class, elog.getId()), 
						elog.getAuthor().getDisplayName(), remarks);
				try{
					emailManager.sendHTMLMail(recipients, EmailTemplateConstants.ELOG_APPROVED, var);
					appState.recordInfoMsg(messages.format("approval-update-sent-to-x", Util.extractDisplayNames(recipients)));
				}
				catch(SendEmailFailException e){
					logger.warn(e.getMessage(), e);
			//		appState.recordWarningMsg(e.getMessage());
				}
				
				break;
			case REJECT:
				elog.setStatus(ElogStatus.REJECTED);
				elog.appendlnHtmlRemarks("<span class='date'>"+Util.formatDateTime2(new Date())+"</span> - "
						+messages.format("remarks-rejected-by-x", getCurUser().getDisplayName()));
				
				//prepare to email to update students and other approvers
				recipients.add(elog.getAuthor());
				recipients.addAll(getApprovers(project));
				var = new EmailTemplateVariables(
						(new Date()).toString(), elog.getMdate().toString(),
						elog.getSubject(), 
						Util.truncateString(Util.stripTags(elog.getContent()), Config.getInt("max_content_lenght_in_email")), 
						getCurUser().getDisplayName(),
						project.getDisplayName(), 
						emailManager.createLinkBackURL(request, View.class, elog.getId()), 
						elog.getAuthor().getDisplayName(), remarks);
				try{
					emailManager.sendHTMLMail(recipients, EmailTemplateConstants.ELOG_REJECTED, var);
					appState.recordInfoMsg(messages.format("rejection-update-sent-to-x", Util.extractDisplayNames(recipients)));
				}
				catch(SendEmailFailException e){
					logger.warn(e.getMessage(), e);
				//	appState.recordWarningMsg(e.getMessage());
				}
				
				break;
		}
		
		
		if(remarks!=null)
			elog.appendlnHtmlRemarks("<span class='subdes' style='padding-left:10px;'>"
					+messages.get("remarks-label")+"</span> : "+Util.textarea2html(remarks));
	
		
		eDAO.updateElog(elog);
		
		return linkSource.createPageRenderLinkWithContext(ApproveOverview.class, project.getId());
	}
	
	
	public SelectModel getApproveDecisionModel(){
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (ApproveDecision t : ApproveDecision.values()) {
			OptionModel optModel = new OptionModelImpl(messages.get(t.name()+"-label"), t);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
}
