package ntu.celt.eUreka2.pages.modules.care;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.care.CAREDAO;
import ntu.celt.eUreka2.modules.care.CDimension;
import ntu.celt.eUreka2.modules.care.CQuestionSet;

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

public class ManageQuestionset extends AbstractPageCARE{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<CQuestionSet> qsets;
	@Property
	private CQuestionSet qset;
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
	private CAREDAO careDAO;
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
		
		qsets = careDAO.searchCQuestionSets(searchText, getCurUser(),  null);
		
		initCDimensionIfRequired();
	}
	
	
	@CommitAfter
	void initCDimensionIfRequired(){
		
		int numCDim = careDAO.getAllCDimensions().size();
		if(numCDim == 0){ //if not initialized yet
			String[] names ={"Emotional Self-Awareness", "Emotional Self-Management","Social Competence","Relationship Management", 
					"Resilience - Stress Perception", "Resilience - Control", "Resilience - Ownership", "Resilience - Reach",
					"Resilience - Endurance", "Resilience - Coping Skills" , "Adaptability - Sense-Making"
					, "Adaptability - Sense-Giving", "Adaptability - Meaning-Making", "Creativity - Openness to experience"
					, "Creativity - Appropriate, Useful, Valued"};
			String[] colorCodes = {"8db3e2", "8db3e2", "8db3e2", "8db3e2", "d9d9d9", "d9d9d9", "d9d9d9", "d9d9d9", "d9d9d9"
					, "d9d9d9", "c2d69b", "c2d69b", "c2d69b", "fbd5b5", "fbd5b5"};
			String[] styleNames = {CDimension._S_1_EMOTIONAL_INTELLIGENCE, CDimension._S_1_EMOTIONAL_INTELLIGENCE
					, CDimension._S_1_EMOTIONAL_INTELLIGENCE, CDimension._S_1_EMOTIONAL_INTELLIGENCE
					, CDimension._S_2_RESILENT, CDimension._S_2_RESILENT, CDimension._S_2_RESILENT
					, CDimension._S_2_RESILENT, CDimension._S_2_RESILENT, CDimension._S_2_RESILENT
					, CDimension._S_3_ADAPTABILITY, CDimension._S_3_ADAPTABILITY, CDimension._S_3_ADAPTABILITY
					, CDimension._S_4_CREATIVE, CDimension._S_4_CREATIVE};
			
			for(int i=1; i<=names.length; i++){
				CDimension dim = new CDimension(i, names[i-1], "", colorCodes[i-1], i, true, styleNames[i-1]);
				careDAO.addCDimension(dim);
			}
			
		}
	}
	
	void onActionFromDelete(long id) {
		CQuestionSet qset = careDAO.getCQuestionSetById(id);
		
		if(!canDeleteQuestionSet(qset, project)) {
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", qset.getName() ));
		}
		long counEval = careDAO.countCARESurveyByCQuestionSet(qset);
		if( counEval > 0){
			appState.recordErrorMsg("Cannot delete the question set, becausing it is being used by CARE Module. ("+ counEval +" profile(s) )");
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
	void deleteQuestionset(CQuestionSet qset){
		careDAO.deleteCQuestionSet(qset);
	}
	
	public int getTotalSize() {
		if (qsets == null)
			return 0;
		return qsets.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	
	public BeanModel<CQuestionSet> getModel() {
		BeanModel<CQuestionSet> model = beanModelSource.createEditModel(CQuestionSet.class, messages);
		model.include("name","mdate");
		model.add("numQuestion", propertyConduitSource.create(CQuestionSet.class, "questions.size()"));
		model.add("owner", propertyConduitSource.create(CQuestionSet.class, "owner.displayName"));
		model.add("info",null);
		model.add("action",null);
		
		return model;
	}
		
	
}
