package ntu.celt.eUreka2.pages.modules.announcement;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.EvenOdd;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.PrivilegeHelper;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.announcement.Announcement;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.announcement.PrivilegeAnnouncement;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;

public class AnnouncementManage  extends AbstractPageAnnouncement
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
	private EvenOdd evenOdd ;
	@SuppressWarnings("unused")
	@Property
	private PrivilegeAnnouncement privAnnmt = new PrivilegeAnnouncement();
	@SuppressWarnings("unused")
	@Property
	private PrivilegeHelper privHelper = new PrivilegeHelper();
	/*assume if can edit one, mean can edit all. should use to check before display actionButton only*/
	@Property
	private boolean canEdit = false;
	@Property
	private boolean canDelete = false;
	@SuppressWarnings("unused")
	@Property
	private boolean canView = false;
	
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String filterSubject;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private User filterCreator;
	
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private AnnouncementDAO anmtDAO;
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private Messages messages;
    @Inject
    private PropertyConduitSource propertyConduitSource; 
   
	void onActivate(String id) {
		projId = id;
	}
	String onPassivate() {
		return projId;
	}

	void setupRender(){
		
		curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", projId));
		}
		if(!canAccessAnnouncement(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		if(canManageAnnouncement(curProj) ){
				canEdit = true;
				canDelete = true;
				canView = true;
		}
		else{
			filterCreator = getCurUser();
			canEdit = true;
			canDelete = true;
			canView = true;
		}
		annmts = anmtDAO.searchAnnouncements(curProj, filterCreator, filterSubject);
		
		evenOdd = new EvenOdd();
	}
	
	
	@CommitAfter
	void onActionFromDelete(long id) {
		annmt = anmtDAO.getAnnouncementById(id);
		if(annmt==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AnnmtId", id));
		}
		curProj = annmt.getProject();
		if(!canDeleteAnnouncement(annmt)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		anmtDAO.delete(annmt);
		appState.recordInfoMsg(messages.format("successfully-delete-x", annmt.getSubject()));
	}
	
	@CommitAfter
	void onActionFromToggleEnabled(long id) {
		annmt = anmtDAO.getAnnouncementById(id);
		if(annmt==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AnnmtId", id));
		}
		curProj = annmt.getProject();
		if(!canDeleteAnnouncement(annmt)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		annmt.setEnabled(!annmt.isEnabled());
		anmtDAO.save(annmt);
	}
	
	public BeanModel<Announcement> getModel() {
		BeanModel<Announcement> model = beanModelSource.createEditModel(Announcement.class, messages);
        model.include("urgent","subject","content","startDate","endDate","modifyDate","enabled");
        model.get("urgent").label("");
		model.add("creator", propertyConduitSource.create(Announcement.class, "creator.displayName")); 
		if(canEdit || canDelete){
			model.add("action", null);
		}
        return model;
    }
	
	public SelectModel getMemberModel() {
		List<OptionModel> optionList = new ArrayList<OptionModel>();
		for (User u: curProj.getUsers()) {
			OptionModel option = new OptionModelImpl(u.getDisplayName(), u);
			optionList.add(option);
		}
		SelectModel selModel = new SelectModelImpl(null, optionList);
		return selModel;
	}
	
	public String stripTags(String htmlStr){
		return Util.stripTags(htmlStr);
	}
	public int getTotalSize() {
		if(annmts==null) return 0;
		return annmts.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
}
