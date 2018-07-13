package ntu.celt.eUreka2.pages.modules.elog;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.EvenOdd;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.elog.Elog;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.elog.ElogFile;
import ntu.celt.eUreka2.modules.elog.ElogStatus;
import ntu.celt.eUreka2.modules.elog.PrivilegeElog;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;

public class ApproveList extends AbstractPageElog{
	private String projId;
	@Property
	private Project project;
	@Property
	private Elog elog;
	@SuppressWarnings("unused")
	@Property
	private List<Elog> pendingElogs;
	@SuppressWarnings("unused")
	@Property
	private List<Elog> rejectedElogs;
	@SuppressWarnings("unused")
	@Property
	private List<Elog> approvedElogs;
	
	
	@SuppressWarnings("unused")
	@Property
	private EvenOdd evenOdd ;
	
	@SuppressWarnings("unused")
	@Property
	private ElogFile attFile;
	
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String filterText;
	
	@Inject
	private ElogDAO elogDAO;
	@Inject
	private ProjectDAO projDAO;
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
	void setupRender() {
		project = projDAO.getProjectById(projId);
		if(project==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", projId));
		}
		if(!canViewApproveList(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		pendingElogs = elogDAO.searchElogs(filterText, null, project, null, ElogStatus.PENDING, null, null, null, null, null, getCurUser());
		approvedElogs = elogDAO.searchElogs(filterText, null, project, null, ElogStatus.APPROVED, null, null, null, null, null, getCurUser());
		rejectedElogs = elogDAO.searchElogs(filterText, null, project, null, ElogStatus.REJECTED, null, null, null, null, null, getCurUser());
		
		evenOdd = new EvenOdd();
	}
    
	@CommitAfter
	void onActionFromTogglePublished(String id) {
		elog = elogDAO.getElogById(id);
		if(elog==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "eLogID", id));
		}
		if(!canApproveElog(elog.getProject())) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		elog.setPublished(!elog.isPublished());
		elogDAO.updateElog(elog);
	}
	
	public BeanModel<Elog> getPendingModel(){
		BeanModel<Elog> model = beanModelSource.createEditModel(Elog.class, messages);
        model.include("subject", "remarks");
        model.getById("subject").label(messages.get("pending-elogs"));
        model.getById("remarks").label(messages.get("logs-label"));
        model.add("author", propertyConduitSource.create(Elog.class, "author.displayName"));
        
        model.reorder("subject","author","remarks");
        return model;
	}
	public BeanModel<Elog> getApprovedModel(){
		BeanModel<Elog> model = beanModelSource.createEditModel(Elog.class, messages);
        model.include("subject", "published", "remarks");
        model.getById("subject").label(messages.get("approved-elogs"));
        model.getById("remarks").label(messages.get("logs-label"));
        model.add("author", propertyConduitSource.create(Elog.class, "author.displayName"));
        model.reorder("subject","published","author","remarks");
        return model;
	}
	public BeanModel<Elog> getRejectedModel(){
		BeanModel<Elog> model = beanModelSource.createEditModel(Elog.class, messages);
        model.include("subject", "remarks");
        model.getById("subject").label(messages.get("rejected-elogs"));
        model.getById("remarks").label(messages.get("logs-label"));
        model.add("author", propertyConduitSource.create(Elog.class, "author.displayName"));
        model.reorder("subject","author","remarks");
        return model;
	}
}
