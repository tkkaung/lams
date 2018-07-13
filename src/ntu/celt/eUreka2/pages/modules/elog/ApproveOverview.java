package ntu.celt.eUreka2.pages.modules.elog;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.EvenOdd;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.elog.Elog;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.elog.ElogFile;
import ntu.celt.eUreka2.modules.elog.ElogStatus;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class ApproveOverview extends AbstractPageElog{
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
	private List<Elog> approvedPublishedElogs;
	@SuppressWarnings("unused")
	@Property
	private List<Elog> approvedUnpublishedElogs;
	
	
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
    private PageRenderLinkSource linkSource;
   
    
    Object onActivate(String id) {
		projId = id;
		project = projDAO.getProjectById(projId);
		if(project==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", projId));
		}
		if(!canViewApproveList(project)){
			
			return linkSource.createPageRenderLinkWithContext(Home.class, projId);
		}
		return null;
	}
	String onPassivate() {
		return projId;
	}
	void setupRender() {
		
		
		pendingElogs = elogDAO.searchElogs(filterText, null, project, null, ElogStatus.PENDING, null, null, null, null, null, getCurUser());
		approvedPublishedElogs = elogDAO.searchElogs(filterText, null, project, null, ElogStatus.APPROVED, true, null, null, null, null, getCurUser());
		approvedUnpublishedElogs = elogDAO.searchElogs(filterText, null, project, null, ElogStatus.APPROVED, false, null, null, null, null, getCurUser());
		rejectedElogs = elogDAO.searchElogs(filterText, null, project, null, ElogStatus.REJECTED, null, null, null, null, null, getCurUser());
		
		evenOdd = new EvenOdd();
	}
    
	@CommitAfter
	void onActionFromPublishElog(String id) {
		elog = elogDAO.getElogById(id);
		if(elog==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "eLogID", id));
		}
		if(!canApproveElog(elog.getProject())) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		elog.setPublished(true);
		elog.appendlnHtmlRemarks("<span class='date'>"+Util.formatDateTime2(new Date())+"</span> - "
				+messages.format("remarks-published-by-x", getCurUser().getDisplayName()));
		elogDAO.updateElog(elog);
	}
	@CommitAfter
	void onActionFromUnpublishElog(String id) {
		elog = elogDAO.getElogById(id);
		if(elog==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "eLogID", id));
		}
		if(!canApproveElog(elog.getProject())) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		elog.setPublished(false);
		elog.appendlnHtmlRemarks("<span class='date'>"+Util.formatDateTime2(new Date())+"</span> - "
				+messages.format("remarks-unpublished-by-x", getCurUser().getDisplayName()));
		elogDAO.updateElog(elog);
	}
	
	public BeanModel<Elog> getModel(){
		BeanModel<Elog> model = beanModelSource.createEditModel(Elog.class, messages);
        model.include("subject");
        model.add("action",null);
        return model;
	}
	
}
