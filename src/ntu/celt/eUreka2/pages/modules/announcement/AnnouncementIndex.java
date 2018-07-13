package ntu.celt.eUreka2.pages.modules.announcement;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchlTypeAnnouncementDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.EvenOdd;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.SchlTypeAnnouncement;
import ntu.celt.eUreka2.internal.PrivilegeHelper;
import ntu.celt.eUreka2.modules.announcement.Announcement;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.announcement.PrivilegeAnnouncement;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class AnnouncementIndex extends AbstractPageAnnouncement
{
	@Property
	private Project curProj;
	@SessionState
	private AppState appState;
	private String projId ;
	@Property
	private Announcement annmt;
	@Property
	private List<Announcement> annmts;
	@SuppressWarnings("unused")
	@Property
	private List<Announcement> annmtsDisplay;
	@Property
	private SchlTypeAnnouncement stAnnmt;
	@Property
	private List<SchlTypeAnnouncement> stAnnmts;
	@SuppressWarnings("unused")
	@Property
	private List<SchlTypeAnnouncement> stAnnmtsDisplay;
	@SuppressWarnings("unused")
	@Property
	private EvenOdd evenOdd ;
	@SuppressWarnings("unused")
	@Property
	private String showDays;
	@Persist("flash")
	@Property
	private Integer curPageNo;
	@Persist("flash")
	@Property
	private Integer curPageNoST;
	@SuppressWarnings("unused")
	@Property
	private PrivilegeAnnouncement privAnnmt = new PrivilegeAnnouncement();
	@SuppressWarnings("unused")
	@Property
	private PrivilegeHelper privHelper = new PrivilegeHelper();
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private AnnouncementDAO annmtDAO;
	@Inject
	private SchlTypeAnnouncementDAO stAnnmtDAO;
	@Inject
	private Messages _messages;


	void onActivate(String id) {
		projId = id;
	}
	String onPassivate() {
		return projId;
	}

	void setupRender(){
		curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "ProjectId", projId));
		}
		if(!canViewAnnouncement(curProj)){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		evenOdd = new EvenOdd();
		
		annmts = annmtDAO.getActiveAnnouncements(curProj, null, getCurUser());
		
		if(curPageNo==null)
			curPageNo = 1;
		int fromIndex = (curPageNo-1)*appState.getRowsPerPage();
		int toIndex = Math.min(curPageNo*appState.getRowsPerPage(), annmts.size());
		annmtsDisplay = annmts.subList(fromIndex, toIndex);
		
		stAnnmts = stAnnmtDAO.getActiveSchlTypeAnnouncements(curProj, getCurUser());
		if(curPageNoST==null)
			curPageNoST = 1;
		int fromIndex2 = (curPageNoST-1)*appState.getRowsPerPage();
		int toIndex2 = Math.min(curPageNoST*appState.getRowsPerPage(), stAnnmts.size());
		stAnnmtsDisplay = stAnnmts.subList(fromIndex2, toIndex2);
		
	}
	
	
	public boolean hasSchlTypeAnnouncement(){
		if(getTotalSizeST()>0)
			return true;
		return false;
	}
	
	public int getTotalSizeST() {
		if(stAnnmts==null) return 0;
		return stAnnmts.size();
	}
	public int getTotalSize() {
		if(annmts==null) return 0;
		return annmts.size();
	}
	public int getTotalSizeAll(){
		return getTotalSizeST() + getTotalSize();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	@CommitAfter
	public void onActionFromDismissAnnmt(Long annmtId){
		annmt = annmtDAO.getAnnouncementById(annmtId);
		if(annmt==null){
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "AnnmtId", annmtId));
		}
		curProj = annmt.getProject(); 
		annmt.addHasReadUsers(getCurUser());
	}
	@CommitAfter
	public void onActionFromDismissSTAnnmt(String projID, int annmtId){
		stAnnmt = stAnnmtDAO.getSchlTypeAnnouncementById(annmtId);
		if(stAnnmt==null){
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "AnnmtId", annmtId));
		}
		stAnnmt.addHasReadUsers(getCurUser());
	}
	@CommitAfter
	public void onActionFromDismissAll(String projID){
		
		curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "ProjectId", projId));
		}
		annmts = annmtDAO.getActiveAnnouncements(curProj, null, getCurUser());
		for(Announcement annmt : annmts){
			annmt.addHasReadUsers(getCurUser());
		}
		stAnnmts = stAnnmtDAO.getActiveSchlTypeAnnouncements(curProj, getCurUser());
		for(SchlTypeAnnouncement stAnnmt : stAnnmts){
			stAnnmt.addHasReadUsers(getCurUser());
		}
	}
	
	
	@Cached
	public int countDismissedAnnmt(){
		List<SchlTypeAnnouncement> dmSTAnnmts = stAnnmtDAO.getDismissedSchlTypeAnnouncements(curProj, getCurUser());
		List<Announcement> dmAnnmts = annmtDAO.getDismissedAnnouncements(curProj, null, getCurUser());
		return dmSTAnnmts.size() + dmAnnmts.size();
	}
}
