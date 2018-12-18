package ntu.celt.eUreka2.pages.modules.big5;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.big5.BIG5DAO;
import ntu.celt.eUreka2.modules.big5.BDimension;
import ntu.celt.eUreka2.modules.big5.BQuestionSet;

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

public class ManageQuestionset extends AbstractPageBIG5{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<BQuestionSet> qsets;
	@Property
	private BQuestionSet qset;
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
	private BIG5DAO big5DAO;
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
		
		qsets = big5DAO.searchBQuestionSets(searchText, getCurUser(),  null);
		
		initBDimensionIfRequired();
	}
	
	
	@CommitAfter
	void initBDimensionIfRequired(){
		
		int numCDim = big5DAO.getAllBDimensions().size();
		if(numCDim == 0){ //if not initialized yet
			String[] names ={"Extraversion", "Agreeableness", "Conscientiousness", "Emotional Stability*", "Openness"};
			String[] colorCodes = {"8db3e2", "d9d9d9", "c2d69b",  "ebd525", "fbd5b5"};
			String[] styleNames ={"Extraversion", "Agreeableness", "Conscientiousness", "Emotional Stability*", "Openness"};
			
			for(int i=1; i<=names.length; i++){
				BDimension dim = new BDimension(i, names[i-1], "", colorCodes[i-1], i, true, styleNames[i-1]);
				big5DAO.addBDimension(dim);
			}
			
		}
	}
	
	void onActionFromDelete(long id) {
		BQuestionSet qset = big5DAO.getBQuestionSetById(id);
		
		if(!canDeleteQuestionSet(qset, project)) {
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", qset.getName() ));
		}
		long counEval = big5DAO.countBIG5SurveyByBQuestionSet(qset);
		if( counEval > 0){
			appState.recordErrorMsg("Cannot delete the question set, becausing it is being used by BIG5 Module. ("+ counEval +" profile(s) )");
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
	void deleteQuestionset(BQuestionSet qset){
		big5DAO.deleteBQuestionSet(qset);
	}
	
	public int getTotalSize() {
		if (qsets == null)
			return 0;
		return qsets.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	
	public BeanModel<BQuestionSet> getModel() {
		BeanModel<BQuestionSet> model = beanModelSource.createEditModel(BQuestionSet.class, messages);
		model.include("name","mdate");
		model.add("numQuestion", propertyConduitSource.create(BQuestionSet.class, "questions.size()"));
		model.add("owner", propertyConduitSource.create(BQuestionSet.class, "owner.displayName"));
		model.add("info",null);
		model.add("action",null);
		
		return model;
	}
		
	
}
