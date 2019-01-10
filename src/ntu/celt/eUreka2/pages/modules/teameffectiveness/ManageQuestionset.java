package ntu.celt.eUreka2.pages.modules.teameffectiveness;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.teameffectiveness.TEDAO;
import ntu.celt.eUreka2.modules.teameffectiveness.TEDimension;
import ntu.celt.eUreka2.modules.teameffectiveness.TEQuestionSet;

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
import org.slf4j.Logger;

public class ManageQuestionset extends AbstractPageTE{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<TEQuestionSet> qsets;
	@Property
	private TEQuestionSet qset;
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
	private TEDAO teDAO;
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;
	@Inject
	private PropertyConduitSource propertyConduitSource;
	@Inject
	private Request request;
	@Inject
	private Logger logger;
	
	
	
	
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
		
		qsets = teDAO.searchTEQuestionSets(searchText, getCurUser(),  null);
		
		initTEDimensionIfRequired();
	}
	
	
	@CommitAfter
	void initTEDimensionIfRequired(){
		
		int numCDim = teDAO.getAllTEDimensions().size();
		if(numCDim == 0){ //if not initialized yet
			String[] names ={"Perceived Team Effectiveness", "Team viability", "Individual Satisfaction with the team", "Satisfaction with the team process", "Team Monitoring", "Team reflexivity", "Mutual trust", "Team Efficacy", "Team Learning Goal Orientation", "Team Performance Goal Orientation", "Team Identification", "Relationship Conflict", "Task Conflict", "Task Enjoyment", "Perceived Discussion on participation INDIVIDUAL"};
			String[] colorCodes = {"8db3e2", "d9d9d9", "c2d69b",  "ebd525", "fbd5b5","8db3e2", "d9d9d9", "c2d69b",  "ebd525", "fbd5b5", "8db3e2", "d9d9d9", "c2d69b",  "ebd525", "fbd5b5"};
			String[] styleNames ={"Perceived Team Effectiveness", "Team viability", "Individual Satisfaction with the team", "Satisfaction with the team process", "Team Monitoring", "Team reflexivity", "Mutual trust", "Team Efficacy", "Team Learning Goal Orientation", "Team Performance Goal Orientation", "Team Identification", "Relationship Conflict", "Task Conflict", "Task Enjoyment", "Perceived Discussion on participation INDIVIDUAL"};
			
			for(int i=1; i<=names.length; i++){
				TEDimension dim = new TEDimension(i, names[i-1], "", colorCodes[i-1], i, true, styleNames[i-1]);
				teDAO.addTEDimension(dim);
			}
			
		}
	}
	
	void onActionFromDelete(long id) {
		TEQuestionSet qset = teDAO.getTEQuestionSetById(id);
		
		if(!canDeleteQuestionSet(qset, project)) {
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", qset.getName() ));
		}
		long counEval = teDAO.countTESurveyByTEQuestionSet(qset);
		if( counEval > 0){
			appState.recordErrorMsg("Cannot delete the question set, becausing it is being used by TE Module. ("+ counEval +" profile(s) )");
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
	void deleteQuestionset(TEQuestionSet qset){
		teDAO.deleteTEQuestionSet(qset);
	}
	
	public int getTotalSize() {
		if (qsets == null)
			return 0;
		return qsets.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	
	public BeanModel<TEQuestionSet> getModel() {
		BeanModel<TEQuestionSet> model = beanModelSource.createEditModel(TEQuestionSet.class, messages);
		model.include("name","mdate");
		model.add("numQuestion", propertyConduitSource.create(TEQuestionSet.class, "questions.size()"));
		model.add("owner", propertyConduitSource.create(TEQuestionSet.class, "owner.displayName"));
		model.add("info",null);
		model.add("action",null);
		
		return model;
	}
		
	
}
