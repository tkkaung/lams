package ntu.celt.eUreka2.pages.project;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjExtraInfoDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.ApproveItemStatus;
import ntu.celt.eUreka2.entities.ProjCAOExtraInfo;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
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
import org.slf4j.Logger;

public class ScopeApprove extends AbstractPageProject{
	private int peInfoId;
	@Property
	private ProjCAOExtraInfo peInfo;
	@Property
	private Project curProj;
	@Property
	private String remarks;
	@Property
	private ApproveDecision decision;

	public enum ApproveDecision {APPROVE, REJECT};
	
	
	@SessionState
	private AppState appState;
	
	@Inject
	private ProjExtraInfoDAO peInfoDAO;
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
	
	void onActivate(EventContext ec) {
		peInfoId = ec.get(Integer.class, 0);
	}
	Object[] onPassivate() {
		return new Object[] {peInfoId};
	}
	
	
	
	void setupRender() {
		peInfo = peInfoDAO.getProjExtraInfoById(peInfoId);
		if(peInfo==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "peInfoId", peInfoId));
		
		curProj = peInfo.getProject();
		if(!canApprovedScope(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		if(!peInfo.isPending()){
			throw new NotAuthorizedAccessException(messages.get("cannot-approve-none-pending"));
		}
		
	}	
	
	void onPrepareForSubmitFromForm(){
		peInfo = peInfoDAO.getProjExtraInfoById(peInfoId);
		curProj = peInfo.getProject();
	}
	@CommitAfter
	Object onSuccessFromForm(){
		
		EmailTemplateVariables var;
		List<User> recipients = new ArrayList<User>();
		
		switch(decision){
			case APPROVE:
				
				peInfo.setApprovedScope(peInfo.getScope());
				peInfo.setApprovedPrerequisite(peInfo.getPrerequisite());
				peInfo.setStatus(ApproveItemStatus.APPROVED);
				peInfo.setPrevStatus(ApproveItemStatus.APPROVED);
				peInfo.appendlnHtmlLogs("<span class='date'>"+Util.formatDateTime2(new Date())+"</span> - "
						+messages.format("remarks-approved-by-x", getCurUser().getDisplayName()));
				peInfo.setApprover(getCurUser());
				peInfo.setApproveTime(new Date());
				
				//prepare to email to update students and other approvers
				recipients = curProj.getUsers();
				var = new EmailTemplateVariables(
						peInfo.getCdate().toString(), (new Date()).toString(),
						messages.get("scope-n-prerequisite"),
						Util.truncateString(Util.stripTags(peInfo.getScope()+",<br/>"+peInfo.getPrerequisite()), Config.getInt("max_content_lenght_in_email")), 
						getCurUser().getDisplayName(),
						curProj.getDisplayName(), 
						emailManager.createLinkBackURL(request, Home.class, curProj.getId()), 
						peInfo.getUser().getDisplayName(), remarks);
				try{
					emailManager.sendHTMLMail(recipients, EmailTemplateConstants.PROJEXTRAINFO_APPROVED, var);
					appState.recordInfoMsg(messages.format("approval-update-sent-to-x", Util.extractDisplayNames(recipients)));
				}
				catch(SendEmailFailException e){
					logger.warn(e.getMessage(), e);
					appState.recordWarningMsg(e.getMessage());
				}
				
				break;
			case REJECT:
				peInfo.setRejectedScope(peInfo.getScope());
				peInfo.setRejectedPrerequisite(peInfo.getPrerequisite());
				peInfo.setStatus(ApproveItemStatus.REJECTED);
				peInfo.setPrevStatus(ApproveItemStatus.REJECTED);
				peInfo.appendlnHtmlLogs("<span class='date'>"+Util.formatDateTime2(new Date())+"</span> - "
						+messages.format("remarks-rejected-by-x", getCurUser().getDisplayName()));
				peInfo.setApprover(getCurUser());
				peInfo.setApproveTime(new Date());
				
				//prepare to email to update students and other approvers
				recipients.add(peInfo.getUser());
				recipients.addAll(getApprovers(curProj));
				var = new EmailTemplateVariables(
						peInfo.getCdate().toString(), (new Date()).toString(),
						messages.get("scope-n-prerequisite"),
						Util.truncateString(Util.stripTags(peInfo.getScope()+",<br/>"+peInfo.getPrerequisite()), Config.getInt("max_content_lenght_in_email")), 
						getCurUser().getDisplayName(),
						curProj.getDisplayName(), 
						emailManager.createLinkBackURL(request, Home.class, curProj.getId()), 
						peInfo.getUser().getDisplayName(), remarks);
				try{
					emailManager.sendHTMLMail(recipients, EmailTemplateConstants.PROJEXTRAINFO_REJECTED, var);
					appState.recordInfoMsg(messages.format("rejection-update-sent-to-x", Util.extractDisplayNames(recipients)));
				}
				catch(SendEmailFailException e){
					logger.warn(e.getMessage(), e);
					appState.recordWarningMsg(e.getMessage());
				}
				
				break;
		}
		
		
		if(remarks!=null)
			peInfo.appendlnHtmlLogs("<span class='subdes' style='padding-left:10px;'>"
					+messages.get("remarks-label")+"</span> : "+Util.textarea2html(remarks));
	
		
		peInfoDAO.updateProjExtraInfo(peInfo);
		
		return linkSource.createPageRenderLinkWithContext(Home.class, curProj.getId());
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
