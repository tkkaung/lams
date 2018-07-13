package ntu.celt.eUreka2.pages.project;


import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjExtraInfoDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
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
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

public class ScopeSubmit extends AbstractPageProject{
	@Property
	private Project curProj;
	private String projId;
	@Property
	private ProjCAOExtraInfo peInfo;
	@Persist
	@Property
	private String scope;
	@Persist
	@Property
	private String prerequisite;
	
	
	@SessionState
	private AppState appState;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private ProjExtraInfoDAO projExtraInfoDAO;
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
	
	void onActivate(EventContext ec){
			projId = ec.get(String.class, 0);
	}
	Object[] onPassivate(){
		return new Object[]{projId};
	}
	
	public void setContext(String projId, String scope, String prerequisite){
		this.projId = projId;
		this.scope = scope;
		this.prerequisite = prerequisite;
	}
	
	void setupRender(){
		curProj = projDAO.getProjectById(projId);
		if(curProj==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjId", projId));
		if(!canUpdateScope(curProj))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		peInfo = projExtraInfoDAO.getProjExtraInfoByProject(curProj);
		if(peInfo==null)
			peInfo = new ProjCAOExtraInfo();
	}
	void onPrepareForSubmitFromForm(){
		curProj = projDAO.getProjectById(projId);
		peInfo = projExtraInfoDAO.getProjExtraInfoByProject(curProj);
	}
	
	@InjectPage
	private ScopeEdit scopeEditPage;
	Object onSelectedFromBack(){
		scopeEditPage.setContext(projId, scope, prerequisite);
		return scopeEditPage;
	}
	
	@CommitAfter
	Object onSuccessFromForm(){
		if(peInfo==null){
			peInfo = new ProjCAOExtraInfo();
			peInfo.setCdate(new Date());
			peInfo.setMdate(new Date());
			peInfo.setProject(curProj);
			projExtraInfoDAO.immediateSaveProjExtraInfo(peInfo); //commit to database at this point, ID is initialized
		}
		peInfo.setUser(getCurUser());
		peInfo.setScope(scope);
		peInfo.setPrerequisite(prerequisite);
		peInfo.setMdate(new Date());
		
		
		
		List<User> approvers = getApprovers(curProj);
		
		if(approvers.isEmpty()){ //if still no approvers, skip and publish the Scope automatically
			peInfo.setStatus(ApproveItemStatus.APPROVED);
			peInfo.appendlnHtmlLogs("<span class='date'>"+Util.formatDateTime2(new Date())+"</span> - "+messages.get("remarks-auto-approved"));
			projExtraInfoDAO.updateProjExtraInfo(peInfo);
			appState.recordInfoMsg(messages.get("request-auto-approved" ));
		}
		else{
			EmailTemplateVariables var = new EmailTemplateVariables(
					peInfo.getCdate().toString(), peInfo.getMdate().toString(),
					messages.get("scope-n-prerequisite"),
					Util.truncateString(Util.stripTags(peInfo.getScope()+",<br/>"+peInfo.getPrerequisite()), Config.getInt("max_content_lenght_in_email")), 
					peInfo.getUser().getDisplayName(),
					curProj.getDisplayName(), 
					emailManager.createLinkBackURL(request, ScopeApprove.class, peInfo.getId()), 
					null, null);
			
			try{
				//send email to notify approver(s)
				emailManager.sendHTMLMail(approvers, EmailTemplateConstants.PROJEXTRAINFO_REQUEST_APPROVAL, var);
				peInfo.setStatus(ApproveItemStatus.PENDING);
				peInfo.appendlnHtmlLogs("<span class='date'>"+Util.formatDateTime2(new Date())+"</span> - "
						+messages.format("remarks-request-sent-by-x", getCurUser().getDisplayName()));
				
				projExtraInfoDAO.updateProjExtraInfo(peInfo);
				appState.recordInfoMsg(messages.format("request-approval-sent-to-x", Util.extractDisplayNames(approvers)));
			}
			catch(SendEmailFailException e){
				logger.warn(e.getMessage(), e);
				appState.recordWarningMsg(e.getMessage());
			}
		}
		
		return linkSource.createPageRenderLinkWithContext(Home.class, curProj.getId());
	}
	
	
	
}
