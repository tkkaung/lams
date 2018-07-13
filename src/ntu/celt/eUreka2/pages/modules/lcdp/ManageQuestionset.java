package ntu.celt.eUreka2.pages.modules.lcdp;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.lcdp.LCDPDAO;
import ntu.celt.eUreka2.modules.lcdp.PQuestionSet;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;
import org.hibernate.exception.ConstraintViolationException;

public class ManageQuestionset extends AbstractPageLCDP{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<PQuestionSet> qsets;
	@Property
	private PQuestionSet qset;
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String searchText;
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private SchoolDAO schoolDAO;
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private LCDPDAO profDAO;
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;
	@Inject
	private PropertyConduitSource propertyConduitSource;
	@Inject
	private Request request;

	
	
	
	
	void onActivate(String id) {
		pid = id;
		
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", pid));
		
	}
	String onPassivate() {
		return pid;
	}
	
	
	void setupRender() {
		if(!canManageQuestionSet(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		qsets = profDAO.searchPQuestionSets(searchText, getCurUser(),  null);
		
	}
	
	
	
	
	
	void onActionFromDelete(long id) {
		PQuestionSet qset = profDAO.getPQuestionSetById(id);
		
		if(!canDeleteQuestionSet(qset, project)) {
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", qset.getName() ));
		}
		long counEval = profDAO.countLCDPSurveyByPQuestionSet(qset);
		if( counEval > 0){
			appState.recordErrorMsg("Cannot delete the question set, becausing it is being used by LCDP Module. ("+ counEval +" profile(s) )");
			return;
		}
		
		try{
			deleteQuestionset(qset);
			appState.recordInfoMsg(messages.format("successfully-delete-x", qset.getName()));
		}
		catch(ConstraintViolationException e){
			appState.recordErrorMsg(messages.format("cant-delete-x-used-by-other", qset.getName()));
		}
	}
	@CommitAfter
	void deleteQuestionset(PQuestionSet qset){
		profDAO.deletePQuestionSet(qset);
	}
	
	public int getTotalSize() {
		if (qsets == null)
			return 0;
		return qsets.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	
	public BeanModel<PQuestionSet> getModel() {
		BeanModel<PQuestionSet> model = beanModelSource.createEditModel(PQuestionSet.class, messages);
		model.include("name","mdate");
		model.add("numQuestion", propertyConduitSource.create(PQuestionSet.class, "questions.size()"));
		model.add("owner", propertyConduitSource.create(PQuestionSet.class, "owner.displayName"));
		model.add("info",null);
		model.add("action",null);
		
		return model;
	}
		
	
}
