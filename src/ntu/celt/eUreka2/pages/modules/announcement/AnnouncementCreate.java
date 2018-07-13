package ntu.celt.eUreka2.pages.modules.announcement;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
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
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.chenillekit.tapestry.core.components.DateTimeField;
import org.slf4j.Logger;


public class AnnouncementCreate extends AbstractPageAnnouncement
{
	@Property
	private Announcement annmt;
	@Property
	private Project curProj;
	@SessionState
	private AppState appState;
	private String projId ;
	@Property
	private Boolean acrossProjects;
	@Property
	private boolean notifyMember;
	@Property
	private List<String> acrProjs;
	
	
	@Inject
	private ProjectDAO projDAO;
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
	@SuppressWarnings("unused")
	@Property
	private StringValueEncoder stringEncoder = new StringValueEncoder();
	
	
	void onActivate(EventContext ec) {
		projId = ec.get(String.class, 0);
	}
	Object[] onPassivate() {
		return new Object[] {projId};
	}

	void setupRender(){	
		curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", projId));
		}
		if(!canCreateAnnouncement(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		if(acrProjs==null){
			acrProjs = new ArrayList<String>();
			acrProjs.add(curProj.getId());
		}
		if(annmt==null){
			annmt = new Announcement();
			annmt.setStartDate(new Date());
		}
	}
	
	void onPrepareForSubmitFromForm(){
		curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", projId));
		}
		if(!canCreateAnnouncement(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		if(annmt==null){
			annmt = new Announcement();
		}
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
		annmt.setCreateDate(new Date());
		annmt.setModifyDate(annmt.getCreateDate());
		annmt.setCreator(getCurUser());
		annmt.setContent(Util.filterOutRestrictedHtmlTags(annmt.getContent()));
		annmt.setEnabled(true);
		List<Project> pList;
		if(acrossProjects){
			pList = new ArrayList<Project>();
			for(String projId : acrProjs){
				pList.add(projDAO.getProjectById(projId));
			}
		}
		else{
			pList = new ArrayList<Project>();
			pList.add(curProj);
		}
		for(Project p : pList){
			Announcement anmt = annmt.clone();
			anmt.setProject(p);
			anmtDAO.save(anmt);	
			
			if(notifyMember){
				EmailTemplateVariables var = new EmailTemplateVariables(
						annmt.getCreateDate().toString(), 
						annmt.getModifyDate().toString() ,
						annmt.getSubject(), 
						Util.truncateString(Util.stripTags(annmt.getContent()), Config.getInt("max_content_lenght_in_email")), 
						annmt.getCreator().getDisplayName(),
						p.getDisplayName(), 
						emailManager.createLinkBackURL(request, AnnouncementView.class, anmt.getId()),
						annmt.isUrgent()? messages.get("urgent") : "", annmt.getStartDate().toString());
				
				try{
					emailManager.sendHTMLMail(p.getUsers(), EmailTemplateConstants.ANNOUNCEMENT_ADDED, var);
					appState.recordInfoMsg(messages.format("notification-sent-to-members-in-project-x", p.getDisplayName()));
				}
				catch(SendEmailFailException e){
					logger.warn(e.getMessage(), e);
					appState.recordWarningMsg(e.getMessage());
				}
			}	
		}
		
		return linkSource.createPageRenderLinkWithContext(AnnouncementManage.class, projId);
	}
	
	public SelectModel getActiveProjsModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		if(canAdminAccess(curProj)){
			OptionModel optModel = new OptionModelImpl(curProj.getDisplayName(), curProj.getId());
			optModelList.add(optModel);
		}else{
			for (Project p : projDAO.getProjectsByMember(getCurUser())) {
				if(p.getStatus().getName().equals(PredefinedNames.PROJSTATUS_ACTIVE)
					|| p.getStatus().getName().equals(PredefinedNames.PROJSTATUS_INACTIVE) ){
					OptionModel optModel = new OptionModelImpl(p.getDisplayName(), p.getId());
					optModelList.add(optModel);
				}
			}
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
}
