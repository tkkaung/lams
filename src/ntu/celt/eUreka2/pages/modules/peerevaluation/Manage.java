package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.AuditTrailDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.AuditTrail;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;

public class Manage extends AbstractPageEvaluation{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<Evaluation> evals;
	@Property
	private Evaluation eval;
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	private Float prevWeight;
	
	
	@SessionState
	private AppState appState;
	
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private EvaluationDAO eDAO;
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;
	@Inject
	private PropertyConduitSource propertyConduitSource;
	@Inject
	private Request request;
	@Inject
	private AuditTrailDAO auditDAO;
	
	
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
		if(!canManageEvaluation(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		evals =  eDAO.getEvaluationsByProject(project);
	}
	
	
	@CommitAfter
	void onActionFromDelete(long eId){
		eval = eDAO.getEvaluationById(eId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "EvaluationID", eId));
		project = eval.getProject();
		if(!canManageEvaluation(project))
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", eval.getName() ));
		
		eDAO.deleteEvaluation(eval);
		eDAO.reorderEvaluationNumber(project);
		
		AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(eval.getId())
				, "Delete" , getCurUser());
		auditDAO.saveAuditTrail(ad);
		
		appState.recordInfoMsg(messages.format("successfully-delete-x", eval.getName()));
	}
	void onPrepareForSubmitFromEditWeightageForm(){
		String evalId = request.getParameter("evalId");  //obtain value from 'hidden' input
		eval = eDAO.getEvaluationById(Long.parseLong(evalId));
		prevWeight = eval.getWeightage();
	}
	@CommitAfter
	void onSuccessFromEditWeightageForm(){
		String prevAuditValue = "";
		String newAuditValue = "";
		
		
		eval.setMdate(new Date());
		//eval.setWeightage(weightage)
		eDAO.updateEvaluation(eval);
		
		
		if(!eval.getWeightage().equals(prevWeight)){
			prevAuditValue += "Weightage: " + (prevWeight==null ? "" : prevWeight)
				+ System.lineSeparator();
			newAuditValue +=  "Weightage: " + eval.getWeightage() + System.lineSeparator();
		}
		
		AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(eval.getId())
				, "Change Weightage" , getCurUser());
		if (!prevAuditValue.isEmpty() || !newAuditValue.isEmpty()){
			ad.setPrevValue(prevAuditValue);
			ad.setNewValue(newAuditValue);
			
			auditDAO.saveAuditTrail(ad);
		}
	}
	
	@CommitAfter
	void onSwapOrder(long id1, long id2){
		Evaluation eval1 = eDAO.getEvaluationById(id1);
		Evaluation eval2 = eDAO.getEvaluationById(id2);
		int tempOrder = eval1.getOrderNumber();
		eval1.setOrderNumber(eval2.getOrderNumber());
		eval2.setOrderNumber(tempOrder);
		eDAO.updateEvaluation(eval1);
		eDAO.updateEvaluation(eval2);
	}
	@InjectComponent
	private Grid grid;
	public Evaluation getNextEval(int rowIndex){
		return (Evaluation) grid.getDataSource().getRowValue(rowIndex+1);
	}
	public Evaluation getPrevEval(int rowIndex){
		return (Evaluation) grid.getDataSource().getRowValue(rowIndex-1);
	}
	
	public boolean isSetEdate(Evaluation eval){
		if(eval.getEdate()!=null)
			return true;
		return false;
	}
	public BeanModel<Evaluation> getModel() {
		BeanModel<Evaluation> model = beanModelSource.createEditModel(Evaluation.class, messages);
		model.include("name","mdate","orderNumber","weightage","released","edate");
		model.get("orderNumber").label(messages.get("eval-OrderNumber-label"));
		model.get("released").label(messages.get("released-result"));
		model.get("edate").label("Date");
        model.add("group",propertyConduitSource.create(Evaluation.class, "group.groupType"));
		model.add("rubric",propertyConduitSource.create(Evaluation.class, "rubric.name"));
		model.add("allowSelfEvaluation",null);
		model.add("action",null);
		
		model.reorder("orderNumber","name","edate","weightage","group", "rubric","allowSelfEvaluation", "released","mdate", "action");
		
		return model;
	}
	
	public int getTotalSize() {
		if (evals == null)
			return 0;
		return evals.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public boolean isTotalWeight100(){
		if(getTotalWeight()==100)
			return true;
		return false;
	}
	public int getTotalWeight(){
		int total = 0;
		for(Evaluation eval: evals){
			total += eval.getWeightage();
		}
		return total;
	}
}
