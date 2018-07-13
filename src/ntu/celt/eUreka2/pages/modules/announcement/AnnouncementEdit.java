package ntu.celt.eUreka2.pages.modules.announcement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.announcement.Announcement;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.announcement.PrivilegeAnnouncement;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.chenillekit.tapestry.core.components.DateTimeField;
import org.slf4j.Logger;

public class AnnouncementEdit extends AbstractPageAnnouncement
{
	@Property
	private Announcement annmt;
	private Long annmtId;
	private Announcement prevAnnmt;
	
	@Property
	private Project curProj; /* note: curProj must be set in setupRender(), for it to be accessible in ProjModuleTabs component */
	@SessionState
	private AppState appState;
	@Property
	private boolean notifyMember;
	
	
	@Inject
	private AnnouncementDAO anmtDAO;
	@Inject
	private EmailManager emailManager;
	@Inject
	private Request request;
	@Inject
	private Logger logger;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		annmtId = ec.get(Long.class, 0);
	}
	Object[] onPassivate() {
		return new Object[] {annmtId};
	}
	
	
	void setupRender(){
		annmt = anmtDAO.getAnnouncementById(annmtId);
		if(annmt==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AnnmtId", annmtId));
		}
		curProj = annmt.getProject(); 
	
		if( !canUpdateAnnouncement(annmt)) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
	}
	void onPrepareFromForm(){
		annmt = anmtDAO.getAnnouncementById(annmtId);
		curProj = annmt.getProject();
		prevAnnmt = annmt.clone();
	}
	
	@Component(id = "Form")
	private Form form;
	@Component(id="endDate")
    private DateTimeField edateField;
	void onValidateFormFromForm(){
		if(annmt.getStartDate()!=null && annmt.getEndDate()!=null
			&&	annmt.getStartDate().after(annmt.getEndDate())){
			form.recordError(edateField, messages.get("endDate-must-be-after-startDate"));
		}
	}
	
	Object onSuccessFromForm(){
		annmt.setModifyDate(new Date());
		annmt.setContent(Util.filterOutRestrictedHtmlTags(annmt.getContent()));
		
		anmtDAO.save(annmt);
		if(notifyMember){
			EmailTemplateVariables var = new EmailTemplateVariables(
					annmt.getCreateDateDisplay(), annmt.getModifyDateDisplay(),
					annmt.getSubject(), 
					Util.truncateString(Util.stripTags(annmt.getContent()), Config.getInt("max_content_lenght_in_email")), 
					annmt.getCreator().getDisplayName(),
					curProj.getDisplayName(), 
					emailManager.createLinkBackURL(request, AnnouncementView.class, annmt.getId()),
					annmt.isUrgent()? messages.get("urgent") : "", annmt.getStartDateDisplay());
			
			try{
				emailManager.sendHTMLMail(curProj.getUsers(), EmailTemplateConstants.ANNOUNCEMENT_EDITED, var);
				appState.recordInfoMsg(messages.get("notification-sent-to-members"));
			}
			catch(SendEmailFailException e){
				logger.warn(e.getMessage(), e);
				appState.recordWarningMsg(e.getMessage());
			}
		}	
		if(!isCreator()){
			EmailTemplateVariables var = new EmailTemplateVariables(
					prevAnnmt.getCreateDateDisplay(), annmt.getModifyDateDisplay(),
					prevAnnmt.getSubject(), 
					Util.truncateString(Util.stripTags(prevAnnmt.getContent()), Config.getInt("max_content_lenght_in_email")), 
					getCurUser().getDisplayName(),
					curProj.getDisplayName(), 
					emailManager.createLinkBackURL(request, AnnouncementView.class, annmt.getId()),
					prevAnnmt.isUrgent()? messages.get("urgent") : "", prevAnnmt.getStartDateDisplay());
			
			try{
				List<User> recipients = new ArrayList<User>();
				recipients.add(annmt.getCreator());
				emailManager.sendHTMLMail(recipients, EmailTemplateConstants.ANNOUNCEMENT_EDITED_BY_OTHER, var);
				appState.recordInfoMsg(messages.format("notification-sent-to-x", Util.extractDisplayNames(recipients)));
			}
			catch(SendEmailFailException e){
				logger.warn(e.getMessage(), e);
				appState.recordWarningMsg(e.getMessage());
			}
		}	
		
		
		return linkSource.createPageRenderLinkWithContext(AnnouncementManage.class, curProj.getId());
	}
	
	public boolean isCreator(){
		return getCurUser().equals(annmt.getCreator());
	}
}
