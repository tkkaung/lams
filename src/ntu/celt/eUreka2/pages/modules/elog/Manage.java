package ntu.celt.eUreka2.pages.modules.elog;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.EvenOdd;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.elog.Elog;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.elog.ElogFile;
import ntu.celt.eUreka2.modules.elog.ElogStatus;
import ntu.celt.eUreka2.modules.elog.PrivilegeElog;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.hibernate.exception.ConstraintViolationException;

public class Manage extends AbstractPageElog{
	private String projId;
	@Property
	private Project project;
	@SuppressWarnings("unused")
	@Property
	private Elog elog;
	@Property
	private List<Elog> elogs;
	@SuppressWarnings("unused")
	@Property
	private EvenOdd evenOdd ;
	@SuppressWarnings("unused")
	@Property
	private ElogFile tempFile;
	
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String filterText;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private ElogStatus filterStatus;
	
	@SessionState
	private AppState appState;
	
	@Inject
	private ElogDAO elogDAO;
	@Inject
	private ProjectDAO projDAO;
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private Messages messages;
    @Inject
	private AttachedFileManager attFileManager;
    
    void onActivate(String id) {
		projId = id;
	}
	String onPassivate() {
		return projId;
	}
	void setupRender() {
		project = projDAO.getProjectById(projId);
		if(project==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", projId));
		}
		if(!canViewElog(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		elogs = elogDAO.searchElogs(filterText, null, project, getCurUser(), filterStatus,null, null, null, null, null, getCurUser());

		evenOdd = new EvenOdd();
	}
    
	
	@CommitAfter
	void onActionFromDelete(String id) {
		Elog elog = elogDAO.getElogById(id);
		if(elog==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "eLogID", id));
		}
		if(!canDeleteElog(elog)){
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", elog.getSubject() ));
		}
		
		try{
			for(ElogFile ef : elog.getFiles()){
				attFileManager.removeAttachedFile(ef);;
			}
			
			elogDAO.deleteElog(elog);
		}
		catch(ConstraintViolationException e){
			appState.recordErrorMsg(messages.format("cant-delete-x-used-by-other", elog.getSubject()));
		}
	}
	
	public boolean isApprovedUnpublished(Elog elog){
		if(ElogStatus.APPROVED.equals(elog.getStatus())
			&& !elog.isPublished() )
			return true;
		return false;
	}
	
	public BeanModel<Elog> getModel(){
		BeanModel<Elog> model = beanModelSource.createEditModel(Elog.class, messages);
        model.include("subject", "mdate","status","remarks");
      //  model.getById("subject").label(messages.get("elogTitle-label"));
        model.getById("remarks").label(messages.get("logs-label"));
        model.add("action", null); 

        return model;
	}
	
	
	public int getTotalSize() {
		if(elogs==null)
			return 0;
		return elogs.size();
	}
    public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
}
