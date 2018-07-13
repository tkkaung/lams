package ntu.celt.eUreka2.pages.modules.announcement;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.PrivilegeHelper;
import ntu.celt.eUreka2.modules.announcement.Announcement;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.announcement.PrivilegeAnnouncement;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class AnnouncementView extends AbstractPageAnnouncement
{
	@Property
	private Announcement annmt;
	private Long annmtId;
	@Property
	private Project curProj;
	@SuppressWarnings("unused")
	@Property
	private PrivilegeAnnouncement privAnnmt = new PrivilegeAnnouncement();
	@SuppressWarnings("unused")
	@Property
	private PrivilegeHelper privHelper = new PrivilegeHelper();
	@Inject
	private PageRenderLinkSource linkSource;
	
	@Inject
	private Messages messages;

	@Inject
	private AnnouncementDAO anmtDAO;
	
	
	void onActivate(EventContext ec) {
		annmtId = ec.get(Long.class, 0);
	}
	Object[] onPassivate() {
		return new Object[] {annmtId};
	}
	
	public String getBreadcrumb(){
		if(curProj.isReference()){
			return messages.get("view")+" "+messages.get("announcement")+"=modules/announcement/view?"+curProj.getId()+":"+annmt.getId();
		}
		else 
			return messages.get("manage")+" "+messages.get("announcement")+"=modules/announcement/manage?"+curProj.getId()
				+","+messages.get("view")+" "+messages.get("announcement")+"=modules/announcement/view?"+curProj.getId()+":"+annmt.getId();
	}
	
	void setupRender(){
		annmt = anmtDAO.getAnnouncementById(annmtId);
		if(annmt==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AnnmtId", annmtId));
		}
		curProj = annmt.getProject(); 
		if(!canViewAnnouncement(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
	}
	
	@CommitAfter
	public Object onActionFromDismissAnnmt(Long annmtId){
		annmt = anmtDAO.getAnnouncementById(annmtId);
		if(annmt==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AnnmtId", annmtId));
		}
		curProj = annmt.getProject(); 
		annmt.addHasReadUsers(getCurUser());
		return linkSource.createPageRenderLinkWithContext(AnnouncementIndex.class, curProj.getId());
	}
	@CommitAfter
	public void onActionFromUnreadAnnmt(Long annmtId){
		annmt = anmtDAO.getAnnouncementById(annmtId);
		if(annmt==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AnnmtId", annmtId));
		}
		curProj = annmt.getProject(); 
		annmt.removeHasReadUsers(getCurUser());
	}
}
