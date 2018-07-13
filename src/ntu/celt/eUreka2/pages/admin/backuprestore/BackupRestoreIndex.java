package ntu.celt.eUreka2.pages.admin.backuprestore;


import java.io.FileNotFoundException;
import java.util.List;

import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;

import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.modules.backuprestore.BackupEntry;
import ntu.celt.eUreka2.modules.backuprestore.BackupRestoreDAO;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileNotFoundException;

public class BackupRestoreIndex {
	
	@Inject
	private BackupRestoreDAO backupDAO;
	enum SubmitType {REMOVE, RESTORE};
	private SubmitType submitType ;
	
	@Property
	private List<BackupEntry> bEntries;
	@SuppressWarnings("unused")
	@Property
	private BackupEntry bEntry;
	@Inject
	private AttachedFileManager attFileManager;
	@Inject
	private Request request;
	@InjectPage
	private RestoreProject restoreProjPage;
	@Inject
	private BeanModelSource beanModelSource;
	@Inject
	private Messages messages;
	@Inject
    private PropertyConduitSource propertyConduitSource; 
   
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@SessionState
	private AppState appState;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	void setupRender(){
		bEntries = backupDAO.getBackupEntriesByUser(getCurUser());
	}
	
	public StreamResponse onActionFromRetrieveAttachment(String bEntryId) {
		BackupEntry bEnt = backupDAO.getBackupEntryById(bEntryId);
		if(bEnt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "BackupEntryID", bEntryId));
		
		try {
			return attFileManager.getAttachedFileAsStream(bEnt.getAttachedFile());
		} catch (FileNotFoundException e) {
			throw new AttachedFileNotFoundException(messages.format("entity-not-exists", messages.get("attachedFile"), bEnt.getAttachedFile().getDisplayName()));
		}
	}
	
	Object onSuccessFromBEntryForm() {
		String[] selBEntryId = request.getParameters("gridChkBox");
		if(selBEntryId!=null){
			
			switch(submitType){
			case REMOVE:
				for(String bEntryId : selBEntryId){
					BackupEntry bEntry = backupDAO.getBackupEntryById(bEntryId);
					attFileManager.removeAttachedFile(bEntry.getAttachedFile());
					backupDAO.deleteBackupEntry(bEntry);
				}
				return null; //stay on the same page
			case RESTORE:
				restoreProjPage.setBEntryId(selBEntryId[0]);
				return restoreProjPage;
			
		}}
		return null;
	}
	
	void onSelectedFromDelete() {
		submitType = SubmitType.REMOVE;
	}
	void onSelectedFromRestore() {
		submitType = SubmitType.RESTORE;
	}
	
	
	
	public BeanModel<BackupEntry> getModel() {
		BeanModel<BackupEntry> model = beanModelSource.createEditModel(BackupEntry.class, messages);
		model.add("chkBox", null);
		model.add("fileName", propertyConduitSource.create(BackupEntry.class, "attachedFile.displayName"));
		model.add("project", propertyConduitSource.create(BackupEntry.class, "project.displayname"));
		model.add("creator", propertyConduitSource.create(BackupEntry.class, "creator.displayName"));
		model.add("fileSize", propertyConduitSource.create(BackupEntry.class, "attachedFile.size"));
		model.exclude("id");
		model.reorder("chkBox","fileName","project","creator","fileSize");
		
		return model;
	}
	
	
	public int getTotalSize() {
		if (bEntries == null)
			return 0;
		return bEntries.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
}
