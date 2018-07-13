package ntu.celt.eUreka2.pages.modules.announcement;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchlTypeAnnouncementDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.SchlTypeAnnouncement;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class SchlTypeAnnmtView extends AbstractPageAnnouncement
{
	@Property
	private SchlTypeAnnouncement annmt;
	@Property
	private Project curProj;
	private int annmtId;
	private String projId;
	
	@Inject
	private Messages messages;
	@Inject
	private SchlTypeAnnouncementDAO schlTypeAnnmtDAO;
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	
	void onActivate(EventContext ec) {
		projId = ec.get(String.class, 0);
		annmtId = ec.get(Integer.class, 1);
	}
	Object[] onPassivate() {
		return new Object[] {projId, annmtId};
	}
	
	void setupRender(){
		curProj = projDAO.getProjectById(projId); 
		if(curProj==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectID", projId));
		
		annmt = schlTypeAnnmtDAO.getSchlTypeAnnouncementById(annmtId);
		if(annmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AnnouncementID", annmtId));
		
	}
	@CommitAfter
	public Object onActionFromDismissAnnmt(String projId, int annmtId){
		annmt = schlTypeAnnmtDAO.getSchlTypeAnnouncementById(annmtId);
		if(annmt==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AnnmtId", annmtId));
		}
		annmt.addHasReadUsers(getCurUser());
		return linkSource.createPageRenderLinkWithContext(AnnouncementIndex.class, projId);
	}
}
