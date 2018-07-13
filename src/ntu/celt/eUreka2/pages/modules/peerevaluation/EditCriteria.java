package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.AuditTrailDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.AuditTrail;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.assessment.RubricCriteria;
import ntu.celt.eUreka2.modules.assessment.RubricCriterion;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriteria;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriterion;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationUser;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

public class EditCriteria extends AbstractPageEvaluation{
	@Persist
	@Property
	private Project project;
	private long eId;
	@Persist
	@Property
	private Evaluation eval;
	@Persist
	@Property
	private Rubric rubric;
	
	private Boolean resetPersist;
	
	@SuppressWarnings("unused")
	@Property
	private EvaluationCriteria tempRCrit; 
	@SuppressWarnings("unused")
	@Property
	private EvaluationCriterion tempRCriterion; 
	@Property
	private int rowIndex;
	@Property
	private int colIndex;
	
	@Property
	private Integer numCrit ;
	@Property
	private Integer numCriterion ;
	@Property
	private boolean saveAsNewRubric = false; //set default value
	
	enum SubmitType {SUBMIT, LOAD_RUBRIC, UPDATE_NUM_FIELDS, CANCEL, DELETEROW, DELETECOL, INSERTROW, INSERTCOL};
	private SubmitType submitType = SubmitType.SUBMIT;
	
	@Inject
	private EvaluationDAO eDAO;
	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	@SuppressWarnings("unused")
	@Inject
	private Logger logger;
	@Inject
	private Request request;
	@Inject
	private AuditTrailDAO auditDAO;
	
	
	
	void onActivate(EventContext ec) {
		if(ec.getCount()==1)
			eId = ec.get(Long.class, 0);
		else if(ec.getCount()==2){
			eId = ec.get(Long.class, 0);
			resetPersist = ec.get(Boolean.class, 1);
		}
	}
	Object[] onPassivate() {
		if(resetPersist==null)
			return new Object[]{eId};
		return new Object[]{eId, resetPersist};
	}
	
	
	void setupRender(){
		if((resetPersist!=null && resetPersist==true ) || eval==null){
			resetPersistValues();
			eval = eDAO.getEvaluationById(eId);
			if(eval==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "EvaluationID", eId));
			project = eval.getProject();
			if(!canManageEvaluation(project))
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));

			rubric = eval.getRubric();
			resetPersist = null;
		}	
		
		if(!eval.getCriterias().isEmpty()){
			numCrit = eval.getCriterias().size();
			numCriterion = getFirstCriterions().size();
		}
	}
	void onPrepareForSubmitFromForm(){
		if(eval==null){
			eval = eDAO.getEvaluationById(eId);
			project = eval.getProject();
			rubric = eval.getRubric();
		}
	}
	
	public EvaluationCriteria getRCrit(int index){
		return eval.getCriterias().get(index);
	}
	public EvaluationCriterion getRCriterion(int rowIndex, int colIndex){
		return eval.getCriterias().get(rowIndex).getCriterions().get(colIndex);
	}
	
	@Component(id="form")
	private Form form;
	@Component(id="weight")
	private TextField weightField;
	void onValidateFormFromForm(){
		if(submitType.equals(SubmitType.SUBMIT)
				&& !eval.getCriterias().isEmpty()
				&& getTotalWeight(eval.getCriterias())!=100){
			form.recordError(weightField, messages.get("total-weight-must-equal-100"));
		}
	}
	
	
	@CommitAfter
	Object onSuccessFromForm(){
	  switch(submitType){
	  	case CANCEL:
	  		resetPersistValues();
			return linkSource.createPageRenderLinkWithContext(Edit.class, eId);
		case SUBMIT:
			String prevAuditValue = "";
			String newAuditValue = "";
			
			eval.setMdate(new Date());
			eval.setEditor(getCurUser());	
			
			//update score, to make it same as first-row's score
			for(EvaluationCriteria ac : eval.getCriterias()){
				for(int i=0; i<getFirstCriterions().size(); i++){
					ac.getCriterions().get(i).setScore(getFirstCriterions().get(i).getScore());
				}
			}
			
			//if Evaluation has been graded, keep the grade only if (same rubric), 
			//otherwise the graded data will be remove
			Evaluation evalLastSaved = eDAO.getEvaluationById(eval.getId());
			if(!evalLastSaved.getEvalUsers().isEmpty()){
				if(evalLastSaved.getRubric()==null && eval.getRubric()==null ){
					//remain unchanged
				}
				else if(eval.getRubric()!=null 
						&& evalLastSaved.getRubric()!=null
						&& eval.getRubric().getId()==evalLastSaved.getRubric().getId()
			//			&& (assmt.getCriterias().size()==assmtLastSaved.getCriterias().size())
			//			&& (assmt.getCriterias().get(0).getCriterions().size()==assmtLastSaved.getCriterias().get(0).getCriterions().size())
					)
				{
					eval.getEvalUsers().clear();
					for(EvaluationUser eu : evalLastSaved.getEvalUsers()){
						for(int i=eu.getSelectedCriterions().size()-1; i>=0; i--){
							EvaluationCriterion ac = eu.getSelectedCriterions().get(i);
							EvaluationCriterion newAC = findCriterionById(eval, ac.getId());
							if(newAC!=null) {
								//nothing, no need to get total
							}
							else{
								eu.getSelectedCriterions().remove(i);
							}
						}
						for(int i=eu.getSelectedCriterionsByLeader().size()-1; i>=0; i--){
							EvaluationCriterion ac = eu.getSelectedCriterionsByLeader().get(i);
							EvaluationCriterion newAC = findCriterionById(eval, ac.getId());
							if(newAC!=null) {
								//nothing, no need to get total
							}
							else{
								eu.getSelectedCriterionsByLeader().remove(i);
							}
						}
						
						
						eval.addEvalUsers(eu);
					}
				}
				else{
					eval.getEvalUsers().clear();
				}
			}
			
			
			
			//track audit changes
			if(eval.getRubric()!=null 
					&& evalLastSaved.getRubric()!=null
					&& eval.getRubric().getId()==evalLastSaved.getRubric().getId()
				)
			{
				int oldRow = evalLastSaved.getCriterias().size();
				int newRow = eval.getCriterias().size();
				int oldCol = evalLastSaved.getCriterias().get(0).getCriterions().size();
				int newCol = eval.getCriterias().get(0).getCriterions().size();
				
				//if size different
				if(oldRow!=newRow || oldCol!=newCol){
					prevAuditValue += "Rubric: " + evalLastSaved.getRubric().getName()
						+ "("+ oldRow +" row, " + oldCol +" col)" 
						+ System.lineSeparator();
					newAuditValue += "Rubric: " + eval.getRubric().getName()
							+ "("+ newRow +" row, "	+ newCol +" col)" 
							+ System.lineSeparator();
					
				}
				else{ //if same row and same col
					for(int i=1; i<= oldRow; i++){
						EvaluationCriteria oldCrit = evalLastSaved.getCriterias().get(i-1);
						EvaluationCriteria newCrit = eval.getCriterias().get(i-1);
						
						if(!oldCrit.getName().equals(newCrit.getName())){
							prevAuditValue += "Crit"+i+": " + oldCrit.getName()	+ System.lineSeparator();
							newAuditValue += "Crit"+i+": " + newCrit.getName() 
								+ System.lineSeparator();
						}
						if(!oldCrit.getWeightage().equals(newCrit.getWeightage())){
							prevAuditValue += "Crit"+i+" weight: " + oldCrit.getWeightage()	+ System.lineSeparator();
							newAuditValue += "Crit"+i+" weight: " + newCrit.getWeightage() + System.lineSeparator();
						}
						
						for(int j=1; j<=oldCol; j++){
							EvaluationCriterion oldCrion = oldCrit.getCriterions().get(j-1);
							EvaluationCriterion newCrion = newCrit.getCriterions().get(j-1);
							
							if(i==0){ //check score, for first row only
								if(oldCrion.getScore() != newCrion.getScore()){
									prevAuditValue += "Crit"+i+" Col"+j+" Score: " + oldCrion.getScore() + System.lineSeparator();
									newAuditValue += "Crit"+i+" Col"+j+" Score: " + newCrion.getScore()	+ System.lineSeparator();
								}
							}
							if(oldCrion.getDes()!=null && !oldCrion.getDes().equals(newCrion.getDes())){
								prevAuditValue += "Crit"+i+" Col"+j+": " + oldCrion.getDes() + System.lineSeparator();
								newAuditValue += "Crit"+i+" Col"+j+": " + newCrion.getDes() + System.lineSeparator();
							}
						}
					}
				}
				
				
				if(evalLastSaved.getEvalUsers().size()>0){
					newAuditValue += "Graded Data adjusted" + System.lineSeparator();
				}
				
				
			}	
			else{ //if Rubric changed
				prevAuditValue += "Rubric: " + (evalLastSaved.getRubric()==null ?  "Do not use" : 
					evalLastSaved.getRubric().getName()+ "("+ evalLastSaved.getCriterias().size() +" row, "
						+ evalLastSaved.getCriterias().get(0).getCriterions().size() +" col)") + System.lineSeparator();
				newAuditValue += "Rubric: " + (eval.getRubric()==null ?  "Do not use" : eval.getRubric().getName()
						+ "("+ eval.getCriterias().size() +" row, "
						+ eval.getCriterias().get(0).getCriterions().size() +" col)") +
						 System.lineSeparator();
				
				if(evalLastSaved.getEvalUsers().size()>0){
					newAuditValue += "Cleared Graded Data" + System.lineSeparator();
				}
			}
		
			
			if(saveAsNewRubric && rubric!=null){
				Rubric r = new Rubric();
				String newRName = Util.appendSequenceNo(rubric.getName(), "_", "" );
				r.setName(newRName);
				r.setDes(rubric.getDes());
				r.setCdate(new Date());
				r.setMdate(new Date());
				r.setOwner(getCurUser());
				
				for(EvaluationCriteria ac : eval.getCriterias()){
					RubricCriteria rc = new RubricCriteria();
					rc.setName(ac.getName());
					rc.setDes(ac.getDes());
					rc.setWeightage(ac.getWeightage());
					rc.setRubric(r);
					for(EvaluationCriterion acion : ac.getCriterions()){
						RubricCriterion rcion = new RubricCriterion();
						rcion.setCriteria(rc);
						rcion.setDes(acion.getDes());
						rcion.setScore(acion.getScore());
						rc.addCriterion(rcion);
					}
					r.addCriteria(rc);
				}
				
				aDAO.addRubric(r);
				rubric = r;
				eval.setRubric(rubric);
			}
			
			eDAO.updateEvaluation(eval);
			
			
			AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(eval.getId())
					, "Customize Rubric", getCurUser());
			if (!prevAuditValue.isEmpty() || !newAuditValue.isEmpty()){
				ad.setPrevValue(prevAuditValue);
				ad.setNewValue(newAuditValue);
				
				auditDAO.saveAuditTrail(ad);
			}
			
			
			resetPersistValues();
			
			return linkSource.createPageRenderLinkWithContext(Edit.class, eId);
		
		case LOAD_RUBRIC:
			if(rubric!=null){
				eval.setRubric(rubric);
				eval.getCriterias().clear();
				for(RubricCriteria rc : eval.getRubric().getCriterias()){
					EvaluationCriteria ac = new EvaluationCriteria();
					ac.setEvaluation(eval);
					ac.setName(rc.getName());
					ac.setDes(rc.getDes());
					ac.setWeightage(rc.getWeightage());
					for(RubricCriterion rcion : rc.getCriterions()){
						EvaluationCriterion acion = new EvaluationCriterion();
						acion.setCriteria(ac);
						acion.setDes(rcion.getDes());
						acion.setScore(rcion.getScore());
						
						ac.addCriterion(acion);
					}
					eval.addCriteria(ac);
				}
				
			}
			else{
				eval.getCriterias().clear();
				eval.setRubric(null);
			}
			
			
			break;
			
		case UPDATE_NUM_FIELDS:
			int	nCrit = eval.getCriterias().size();
			int	nCriterion = getFirstCriterions().size();
			if(numCrit<nCrit){
				eval.setCriterias(eval.getCriterias().subList(0, numCrit));
			}
			else if(numCrit>nCrit){
				for(int i=nCrit; i<numCrit; i++){
					EvaluationCriteria ac = new EvaluationCriteria();
					for(int j=0; j<nCriterion; j++){
						EvaluationCriterion aCriterion = new EvaluationCriterion();
						aCriterion.setScore(j+1);
						ac.addCriterion(aCriterion);
					}
					eval.getCriterias().add(ac);
				}
			}
			if(numCriterion<nCriterion){
				for(EvaluationCriteria ac : eval.getCriterias()){
					ac.setCriterions(ac.getCriterions().subList(0, numCriterion));
				}
			}
			else if(numCriterion>nCriterion){
				for(EvaluationCriteria ac : eval.getCriterias()){
					for(int i=nCriterion; i<numCriterion; i++){
						EvaluationCriterion aCriterion = new EvaluationCriterion();
						aCriterion.setScore(i+1);
						ac.addCriterion(aCriterion);
					}
				}
			}
				
			break;
		case DELETEROW:
			int rowIdx = Integer.parseInt(request.getParameter("rowIdx"));
			eval.getCriterias().remove(rowIdx);
			break;
		case DELETECOL:
			int colIdx = Integer.parseInt(request.getParameter("colIdx"));
			for(EvaluationCriteria rc : eval.getCriterias()){
				for (EvaluationCriterion rrcc : rc.getCriterions()) {
					if (rrcc.getScore() > colIdx) {
						rrcc.setScore(rrcc.getScore() - 1);
					}
				}
				List<EvaluationCriterion> tmp = rc.getCriterions();
				tmp.remove(colIdx);
				rc.setCriterions(tmp);
			}
			break;
		case INSERTROW:
			int nrowIdx = Integer.parseInt(request.getParameter("rowIdx"));
			int	nnCriterion = getFirstCriterions().size();
			EvaluationCriteria rc = new EvaluationCriteria();
			for(int i = 0; i < nnCriterion; i++){
				EvaluationCriterion rCriterion = new EvaluationCriterion();
				rCriterion.setScore(i+1);
				rc.addCriterion(rCriterion);
			}
			eval.getCriterias().add(nrowIdx, rc);
			break;
		case INSERTCOL:
			int ncolIdx = Integer.parseInt(request.getParameter("colIdx"));
			for(EvaluationCriteria rrc : eval.getCriterias()){
				for (EvaluationCriterion rrcc : rrc.getCriterions()) {
					if (rrcc.getScore() > ncolIdx) {
						rrcc.setScore(rrcc.getScore() + 1);
					}
				}
				EvaluationCriterion rCriterion = new EvaluationCriterion();
				rCriterion.setScore(ncolIdx + 1);
				rrc.getCriterions().add(ncolIdx, rCriterion);
			}
			break;
	  }
		
	  return null;
	}
	public boolean hasGradeData(){
		Evaluation assmtLastSaved = eDAO.getEvaluationById(eId);
		if(assmtLastSaved.getEvalUsers().isEmpty()){
			return false;
		}
		return true;
	}
	public String getLastSavedRubricId(){
		Evaluation assmtLastSaved = eDAO.getEvaluationById(eId);
		if(assmtLastSaved.getRubric()==null)
			return null;
		return ""+assmtLastSaved.getRubric().getId();
	}
	
	void onSelectedFromLoadRubric(){
		submitType = SubmitType.LOAD_RUBRIC;
	}
	void onSelectedFromUpdateNumFields(){
		submitType = SubmitType.UPDATE_NUM_FIELDS;
	}
	void onSelectedFromBSubmit(){
		submitType = SubmitType.SUBMIT;
	}
	void onSelectedFromCancel(){
		submitType = SubmitType.CANCEL;
	}
	void onSelectedFromDeleteRow() {
		submitType = SubmitType.DELETEROW;
	}
	void onSelectedFromDeleteCol() {
		submitType = SubmitType.DELETECOL;
	}
	void onSelectedFromInsertRow() {
		submitType= SubmitType.INSERTROW;
	}
	void onSelectedFromInsertCol() {
		submitType = SubmitType.INSERTCOL;
	}
	
	private void resetPersistValues(){
		eval = null;
		project = null;
		rubric = null;
	}
	
	
	public List<EvaluationCriterion> getFirstCriterions(){
		return eval.getCriterias().get(0).getCriterions();
	}
	private EvaluationCriterion findCriterionById(Evaluation eval, long criterionId){
		for(EvaluationCriteria ac : eval.getCriterias()){
			for(EvaluationCriterion acion : ac.getCriterions()){
				if(acion.getId()==criterionId)
					return acion;
			}
		}
		return null;
	}
	
	
	public boolean getCheckRow() {
		if (rowIndex == numCrit - 1) {
			return true;
		}
		return false;
	}
	public boolean getCheckCol() {
		if (colIndex == numCriterion - 1) {
			return true;
		}
		return false;
	}
	public int getNumCol() {
		return 2 * numCriterion;
	}
	public SelectModel getListModel(int max) {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (int i = 0; i <= max; i++) { 
			OptionModel optModel = new OptionModelImpl(Integer.toString(i), Integer.toString(i));
			optModelList.add(optModel);
		}
		
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
}
