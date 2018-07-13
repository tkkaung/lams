package ntu.celt.eUreka2.pages.modules.learninglog;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.EvenOdd;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.learninglog.LLogFile;
import ntu.celt.eUreka2.modules.learninglog.LearningLogDAO;
import ntu.celt.eUreka2.modules.learninglog.LogEntry;

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

public class LearningLogIndex extends AbstractPageLearningLog {

	private String projId;
	@Property
	private Project curProj;
	@SuppressWarnings("unused")
	@Property
	private LogEntry entry;
	@Property
	private List<LogEntry> entries;
	@SuppressWarnings("unused")
	@Property
	private EvenOdd evenOdd ;
	
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private Boolean showFromAllProjects;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String filterType;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String filterText;
	
	@SuppressWarnings("unused")
	@Property
	private LLogFile tempFile;
	
	@SessionState
	private AppState appState;
	
	@Inject
	private LearningLogDAO learningLogDAO;
	@Inject
	private ProjectDAO projDAO;
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private Messages messages;
    
    
    void onActivate(String id) {
		projId = id;
	}
	String onPassivate() {
		return projId;
	}
	void setupRender() {
		curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", projId));
		}
		/*//because learningLog only shows PERSONAL log, so only need to check owner is accessing, no privilege required
		ProjUser pu = curProj.getMember(getCurUser());
		if((pu ==null || !pu.hasPrivilege(PrivilegeLearningLog.VIEW_LEARNING_LOG ))){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		*/
		if(showFromAllProjects==null)
			showFromAllProjects = true;
		
		entries = learningLogDAO.searchLogEntries(getCurUser(), (showFromAllProjects? null : curProj), filterType, filterText);
		evenOdd = new EvenOdd();
	}
    
    
	
	@CommitAfter
	void onActionFromDelete(long id) {
		LogEntry entry = learningLogDAO.getLogEntryById(id);
		if(entry==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "LogEntryId", id));
		}
		curProj = entry.getProject();
		if(!entry.getCreator().equals(getCurUser())){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		learningLogDAO.deleteLogEntry(entry);
	}
	
	
	
	
	public BeanModel<LogEntry> getModel(){
		BeanModel<LogEntry> model = beanModelSource.createEditModel(LogEntry.class, messages);
        model.include("title","type","mdate","shared");
        model.getById("title").label(messages.get("logTitle-label"));
        if(showFromAllProjects)
        	model.add("project",null);
        model.add("action", null); 

        return model;
	}
	public SelectModel getTypeModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<String> catList = learningLogDAO.getLogEntryTypes(getCurUser());
		for(String cat : catList){
			optModelList.add(new OptionModelImpl(cat, cat));
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	public String stripTags(String htmlStr){
		return Util.stripTags(htmlStr);
	}
	public int getTotalSize() {
		if(entries==null)
			return 0;
		return entries.size();
	}
    
	
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
}
