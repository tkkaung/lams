package ntu.celt.eUreka2.pages.modules.ipsp;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.ipsp.IPSPDAO;
import ntu.celt.eUreka2.modules.ipsp.IDimension;
import ntu.celt.eUreka2.modules.ipsp.IQuestionSet;

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

public class ManageQuestionset extends AbstractPageIPSP{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<IQuestionSet> qsets;
	@Property
	private IQuestionSet qset;
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
	private IPSPDAO ipspDAO;
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
		
		qsets = ipspDAO.searchIQuestionSets(searchText, getCurUser(),  null);
		
		initIDimensionIfRequired();
	}
	
	
	@CommitAfter
	void initIDimensionIfRequired(){
		
		int numCDim = ipspDAO.getAllIDimensions().size();
		if(numCDim == 0){ //if not initialized yet
			
			String[] names ={"DRIVER", "ENERGISER", "STABILISER", "ANALYSER"};
			String[] colorCodes = {"ff3333", "ffff99", "009966",  "0066cc"};
			String[] styleNames ={"DRIVER", "ENERGISER", "STABILISER", "ANALYSER"};
			
			for(int i=1; i<=names.length; i++){
				IDimension dim = new IDimension(i, names[i-1], "", colorCodes[i-1], i, true, styleNames[i-1]);
				ipspDAO.addIDimension(dim);
			}
			
		}
	}
	
	void onActionFromDelete(long id) {
		IQuestionSet qset = ipspDAO.getIQuestionSetById(id);
		
		if(!canDeleteQuestionSet(qset, project)) {
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", qset.getName() ));
		}
		long counEval = ipspDAO.countIPSPSurveyByIQuestionSet(qset);
		if( counEval > 0){
			appState.recordErrorMsg("Cannot delete the question set, becausing it is being used by IPSP Module. ("+ counEval +" profile(s) )");
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
	void deleteQuestionset(IQuestionSet qset){
		ipspDAO.deleteIQuestionSet(qset);
	}
	
	public int getTotalSize() {
		if (qsets == null)
			return 0;
		return qsets.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	
	public BeanModel<IQuestionSet> getModel() {
		BeanModel<IQuestionSet> model = beanModelSource.createEditModel(IQuestionSet.class, messages);
		model.include("name","mdate");
		model.add("numQuestion", propertyConduitSource.create(IQuestionSet.class, "questions.size()"));
		model.add("owner", propertyConduitSource.create(IQuestionSet.class, "owner.displayName"));
		model.add("info",null);
		model.add("action",null);
		
		return model;
	}
		
	
}
