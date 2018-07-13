package ntu.celt.eUreka2.pages.admin.schltypeannouncement;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchlTypeAnnouncementDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.SchlTypeAnnouncement;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;

public class ManageTypeAnnmt extends AbstractSchlTypeAnnouncement{
	@Property
	private SchlTypeAnnouncement _annmt;
	@Property
	private List<SchlTypeAnnouncement> _annmts;
	private List<Integer> accessibleTypeIDs ;
	
	
	@SessionState
	private AppState appState;
	
	
	
	@Inject
	private SchlTypeAnnouncementDAO _schlTypeAnnmtDAO;
	@Inject
	private Messages _messages;
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private PropertyConduitSource propertyConduitSource; 
	
    
    public String getTitle2(){
    	return _messages.get("manage")+" "+_messages.get("typeannouncement");
    }
    
    
	void setupRender() {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.PROJ_TYPE_ANNOUNCEMENT)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		accessibleTypeIDs = getAccessibleProjTypeIDs(getCurUser());
		_annmts = _schlTypeAnnmtDAO.getAllSchlTypeAnnouncements();
		
		_annmts = filterAnnmts(_annmts);
	}
	
	private List<SchlTypeAnnouncement> filterAnnmts(List<SchlTypeAnnouncement> annmts){
		for(int i=annmts.size()-1; i>=0; i--){
			SchlTypeAnnouncement annmt = annmts.get(i);
			if(annmt.getProjType()==null 
				|| !accessibleTypeIDs.contains(annmt.getProjType().getId())){
				annmts.remove(i);
			}
		}
		return annmts;
	}
	
	
	
	@CommitAfter
	void onActionFromDelete(int id) {
		_annmt = _schlTypeAnnmtDAO.getSchlTypeAnnouncementById(id);
		if(_annmt==null){
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "AnnouncementID", id));
		}
		if(!canManageTypeAnnmts(_annmt)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_schlTypeAnnmtDAO.delete(_annmt);
		appState.recordInfoMsg(_messages.format("successfully-delete-x", _annmt.getSubject()));
	}
	
	@CommitAfter
	void onActionFromToggleEnabled(int id) {
		_annmt = _schlTypeAnnmtDAO.getSchlTypeAnnouncementById(id);
		if(_annmt==null){
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "AnnouncementID", id));
		}
		if(!canManageTypeAnnmts(_annmt)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_annmt.setEnabled(!_annmt.isEnabled());
		_annmt.getHasReadUsers().clear();
		_annmt.setMdate(new Date());
		_schlTypeAnnmtDAO.save(_annmt);
	}
	

	public int getTotalSize() {
		if (_annmts == null)
			return 0;
		return _annmts.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public BeanModel<SchlTypeAnnouncement> getModel() {
		BeanModel<SchlTypeAnnouncement> model = beanModelSource.createEditModel(SchlTypeAnnouncement.class, _messages);
        model.include("urgent","subject","content","sdate","edate","enabled","mdate");
        model.get("urgent").label(_messages.get("empty-label"));
        model.add("creator", propertyConduitSource.create(SchlTypeAnnouncement.class, "creator.displayName"));
        model.add("projType", null);
        model.add("school", null);
        model.add("action", null);
        return model;
    }
}
