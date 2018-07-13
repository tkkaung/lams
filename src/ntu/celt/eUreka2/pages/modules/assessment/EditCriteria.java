package ntu.celt.eUreka2.pages.modules.assessment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.AuditTrailDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.AuditTrail;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.AssessCriterion;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentUser;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.assessment.RubricCriteria;
import ntu.celt.eUreka2.modules.assessment.RubricCriterion;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
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

public class EditCriteria extends AbstractPageAssessment{
	@Persist
	@Property
	private Project project;
	private int aId;
	@Persist
	@Property
	private Assessment assmt;
	@Persist
	@Property
	private Rubric rubric;
	
	private Boolean resetPersist;
	
	@SuppressWarnings("unused")
	@Property
	private AssessCriteria tempRCrit; 
	@SuppressWarnings("unused")
	@Property
	private AssessCriterion tempRCriterion; 
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
	
	
	@SessionState
	private AppState appState;
	
	
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
			aId = ec.get(Integer.class, 0);
		else if(ec.getCount()==2){
			aId = ec.get(Integer.class, 0);
			resetPersist = ec.get(Boolean.class, 1);
		}
	}
	Object[] onPassivate() {
		if(resetPersist==null)
			return new Object[]{aId};
		return new Object[]{aId, resetPersist};
	}
	
	
	void setupRender(){
		if((resetPersist!=null && resetPersist==true ) || assmt==null){
			resetPersistValues();
			assmt = aDAO.getAssessmentById(aId);
			if(assmt==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentID", aId));
			project = assmt.getProject();
			if(!canEditAssessment(assmt))
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));

			rubric = assmt.getRubric();
			resetPersist = null;
		}	
		
		if(!assmt.getCriterias().isEmpty()){
			numCrit = assmt.getCriterias().size();
			numCriterion = getFirstCriterions().size();
		}
	}
	void onPrepareForSubmitFromForm(){
		if(assmt==null){
			assmt = aDAO.getAssessmentById(aId);
			project = assmt.getProject();
			rubric = assmt.getRubric();
		}
	}
	
	public AssessCriteria getRCrit(int index){
		return assmt.getCriterias().get(index);
	}
	public AssessCriterion getRCriterion(int rowIndex, int colIndex){
		return assmt.getCriterias().get(rowIndex).getCriterions().get(colIndex);
	}
	
	@Component(id="form")
	private Form form;
	@Component(id="weight")
	private TextField weightField;
	void onValidateFormFromForm(){
		if(submitType.equals(SubmitType.SUBMIT)
				&& !assmt.getCriterias().isEmpty()
				&& getTotalWeight(assmt.getCriterias())!=100){
			form.recordError(weightField, messages.get("total-weight-must-equal-100"));
		}
	}
	
	
	@CommitAfter
	Object onSuccessFromForm(){
	  switch(submitType){
	  	case CANCEL:
	  		resetPersistValues();
			return linkSource.createPageRenderLinkWithContext(Edit.class, aId);
		case SUBMIT:
			String prevAuditValue = "";
			String newAuditValue = "";
			
			List<AssessCriterion> firstCriterions = getFirstCriterions();
			
			//check GMAT, 1,2,3,4,5,6
			if(assmt.getGmat()){
				if(firstCriterions.size()!=6){
					appState.recordErrorMsg("GMAT Rubric scores must be 1,2,3,4,5,6");
					return null;
				}
				else{
					int GMATScore[] = {1,2,3,4,5,6};
					for(int i=0; i<GMATScore.length; i++){
						if(firstCriterions.get(i).getScore() != GMATScore[i]){
							appState.recordErrorMsg("GMAT Rubric scores must be 1,2,3,4,5,6");
							return null;
						}
					}
				}
			}
			
			assmt.setMdate(new Date());
			assmt.setEditor(getCurUser());	
			
			//update score, to make it same as first-row's score
			for(AssessCriteria ac : assmt.getCriterias()){
				for(int i=0; i<firstCriterions.size(); i++){
					ac.getCriterions().get(i).setScore(firstCriterions.get(i).getScore());
				}
			}
			
			//if assessment has been graded, keep the grade only if (same rubric), 
			//otherwise the graded data will be remove
			Assessment assmtLastSaved = aDAO.getAssessmentById(assmt.getId());
			if(!assmtLastSaved.getAssmtUsers().isEmpty()){
				if(assmtLastSaved.getRubric()==null && assmt.getRubric()==null ){
					//remain unchanged
				}
				else if(assmt.getRubric()!=null 
						&& assmtLastSaved.getRubric()!=null
						&& assmt.getRubric().getId()==assmtLastSaved.getRubric().getId()
			//			&& (assmt.getCriterias().size()==assmtLastSaved.getCriterias().size())
			//			&& (assmt.getCriterias().get(0).getCriterions().size()==assmtLastSaved.getCriterias().get(0).getCriterions().size())
					)
				{
					assmt.getAssmtUsers().clear();
					for(AssessmentUser au : assmtLastSaved.getAssmtUsers()){
						float total = 0;
						for(int i=au.getSelectedCriterions().size()-1; i>=0; i--){
							AssessCriterion ac = au.getSelectedCriterions().get(i);
							AssessCriterion newAC = findCriterionById(assmt, ac.getId());
							if(newAC!=null) {
								AssessCriteria newACrit = newAC.getCriteria();
								total += ((float) newAC.getScore() * newACrit.getWeightage() / newACrit.getMaxCritScore());
							}
							else{
								au.getSelectedCriterions().remove(i);
							}
						}
						au.setTotalScore(total);
						assmt.addAssmtUsers(au);
					}
				}
				else{
					assmt.getAssmtUsers().clear();
				}
			}
			
			
			//track audit changes
			if(assmt.getRubric()!=null 
					&& assmtLastSaved.getRubric()!=null
					&& assmt.getRubric().getId()==assmtLastSaved.getRubric().getId()
				)
			{
				int oldRow = assmtLastSaved.getCriterias().size();
				int newRow = assmt.getCriterias().size();
				int oldCol = assmtLastSaved.getCriterias().get(0).getCriterions().size();
				int newCol = assmt.getCriterias().get(0).getCriterions().size();
				
				//if size different
				if(oldRow!=newRow || oldCol!=newCol){
					prevAuditValue += "Rubric: " + assmtLastSaved.getRubric().getName()
						+ "("+ oldRow +" row, " + oldCol +" col)" 
						+ System.lineSeparator();
					newAuditValue += "Rubric: " + assmt.getRubric().getName()
							+ "("+ newRow +" row, "	+ newCol +" col)" 
							+ System.lineSeparator();
					
				}
				else{ //if same row and same col
					for(int i=1; i<= oldRow; i++){
						AssessCriteria oldCrit = assmtLastSaved.getCriterias().get(i-1);
						AssessCriteria newCrit = assmt.getCriterias().get(i-1);
						
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
							AssessCriterion oldCrion = oldCrit.getCriterions().get(j-1);
							AssessCriterion newCrion = newCrit.getCriterions().get(j-1);
							
							if(i==0){ //check score, for first row only
								if(oldCrion.getScore() != newCrion.getScore()){
									prevAuditValue += "Crit"+i+" Col"+j+" Score: " + oldCrion.getScore() + System.lineSeparator();
									newAuditValue += "Crit"+i+" Col"+j+" Score: " + newCrion.getScore()	+ System.lineSeparator();
								}
							}
							if(!oldCrion.getDes().equals(newCrion.getDes())){
								prevAuditValue += "Crit"+i+" Col"+j+": " + oldCrion.getDes() + System.lineSeparator();
								newAuditValue += "Crit"+i+" Col"+j+": " + newCrion.getDes() + System.lineSeparator();
							}
						}
					}
				}
				
				
				if(assmtLastSaved.getAssmtUsers().size()>0){
					newAuditValue += "Graded Data adjusted" + System.lineSeparator();
				}
				
				
			}	
			else{ //if Rubric changed
				prevAuditValue += "Rubric: " + (assmtLastSaved.getRubric()==null ?  "Do not use" : 
						assmtLastSaved.getRubric().getName()+ "("+ assmtLastSaved.getCriterias().size() +" row, "
						+ assmtLastSaved.getCriterias().get(0).getCriterions().size() +" col)") + System.lineSeparator();
				newAuditValue += "Rubric: " + (assmt.getRubric()==null ?  "Do not use" : assmt.getRubric().getName()
						+ "("+ assmt.getCriterias().size() +" row, "
						+ assmt.getCriterias().get(0).getCriterions().size() +" col)") +
						 System.lineSeparator();
				
				if(assmtLastSaved.getAssmtUsers().size()>0){
					newAuditValue += "Cleared Graded Data" + System.lineSeparator();
				}
			}

			
			
			if(!assmt.getCriterias().isEmpty()){
		//		assmt.setPossibleScore(getPossibleScore(assmt.getCriterias()));
			}
			
			if(saveAsNewRubric && rubric!=null){
				Rubric r = new Rubric();
				String newRName = Util.appendSequenceNo(rubric.getName(), "_", "" );
				r.setName(newRName);
				r.setDes(rubric.getDes());
				r.setCdate(new Date());
				r.setMdate(new Date());
				r.setOwner(getCurUser());
				
				for(AssessCriteria ac : assmt.getCriterias()){
					RubricCriteria rc = new RubricCriteria();
					rc.setName(ac.getName());
					rc.setDes(ac.getDes());
					rc.setWeightage(ac.getWeightage());
					rc.setRubric(r);
					for(AssessCriterion acion : ac.getCriterions()){
						RubricCriterion rcion = new RubricCriterion();
						rcion.setCriteria(rc);
						rcion.setDes(acion.getDes());
						rcion.setScore(Math.round(acion.getScore()));
						rc.addCriterion(rcion);
					}
					r.addCriteria(rc);
				}
				
				aDAO.addRubric(r);
				rubric = r;
				assmt.setRubric(rubric);
			}
			
			aDAO.updateAssessment(assmt);
			
			AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(assmt.getId())
					, "Customize Rubric", getCurUser());
			if (!prevAuditValue.isEmpty() || !newAuditValue.isEmpty()){
				ad.setPrevValue(prevAuditValue);
				ad.setNewValue(newAuditValue);
				
				auditDAO.saveAuditTrail(ad);
			}
			
			resetPersistValues();
			
			return linkSource.createPageRenderLinkWithContext(Edit.class, aId);
		
		case LOAD_RUBRIC:
			if(rubric!=null){
				assmt.setRubric(rubric);
				assmt.getCriterias().clear();
				for(RubricCriteria rc : assmt.getRubric().getCriterias()){
					AssessCriteria ac = new AssessCriteria();
					ac.setAssessment(assmt);
					ac.setName(rc.getName());
					ac.setDes(rc.getDes());
					ac.setWeightage(rc.getWeightage());
					for(RubricCriterion rcion : rc.getCriterions()){
						AssessCriterion acion = new AssessCriterion();
						acion.setCriteria(ac);
						acion.setDes(rcion.getDes());
						acion.setScore(rcion.getScore());
						
						ac.addCriterion(acion);
					}
					assmt.addCriteria(ac);
				}
				
			}
			else{
				assmt.getCriterias().clear();
				assmt.setRubric(null);
			}
			
			
			break;
			
		case UPDATE_NUM_FIELDS:
			int	nCrit = assmt.getCriterias().size();
			int	nCriterion = getFirstCriterions().size();
			if(numCrit<nCrit){
				assmt.setCriterias(assmt.getCriterias().subList(0, numCrit));
			}
			else if(numCrit>nCrit){
				for(int i=nCrit; i<numCrit; i++){
					AssessCriteria ac = new AssessCriteria();
					for(int j=0; j<nCriterion; j++){
						AssessCriterion aCriterion = new AssessCriterion();
						aCriterion.setScore(j+1);
						ac.addCriterion(aCriterion);
					}
					assmt.getCriterias().add(ac);
				}
			}
			if(numCriterion<nCriterion){
				for(AssessCriteria ac : assmt.getCriterias()){
					ac.setCriterions(ac.getCriterions().subList(0, numCriterion));
				}
			}
			else if(numCriterion>nCriterion){
				for(AssessCriteria ac : assmt.getCriterias()){
					for(int i=nCriterion; i<numCriterion; i++){
						AssessCriterion aCriterion = new AssessCriterion();
						aCriterion.setScore(i+1);
						ac.addCriterion(aCriterion);
					}
				}
			}
				
			break;
		case DELETEROW:
			int rowIdx = Integer.parseInt(request.getParameter("rowIdx"));
			assmt.getCriterias().remove(rowIdx);
			break;
		case DELETECOL:
			int colIdx = Integer.parseInt(request.getParameter("colIdx"));
			for(AssessCriteria rc : assmt.getCriterias()){
				for (AssessCriterion rrcc : rc.getCriterions()) {
					if (rrcc.getScore() > colIdx) {
						rrcc.setScore(rrcc.getScore() - 1);
					}
				}
				List<AssessCriterion> tmp = rc.getCriterions();
				tmp.remove(colIdx);
				rc.setCriterions(tmp);
			}
			break;
		case INSERTROW:
			int nrowIdx = Integer.parseInt(request.getParameter("rowIdx"));
			int	nnCriterion = getFirstCriterions().size();
			AssessCriteria rc = new AssessCriteria();
			for(int i = 0; i < nnCriterion; i++){
				AssessCriterion rCriterion = new AssessCriterion();
				rCriterion.setScore(i+1);
				rc.addCriterion(rCriterion);
			}
			assmt.getCriterias().add(nrowIdx, rc);
			break;
		case INSERTCOL:
			int ncolIdx = Integer.parseInt(request.getParameter("colIdx"));
			for(AssessCriteria rrc : assmt.getCriterias()){
				for (AssessCriterion rrcc : rrc.getCriterions()) {
					if (rrcc.getScore() > ncolIdx) {
						rrcc.setScore(rrcc.getScore() + 1);
					}
				}
				AssessCriterion rCriterion = new AssessCriterion();
				rCriterion.setScore(ncolIdx + 1);
				rrc.getCriterions().add(ncolIdx, rCriterion);
			}
			break;
	  }
		
	  return null;
	}
	public boolean hasGradeData(){
		Assessment assmtLastSaved = aDAO.getAssessmentById(aId);
		if(assmtLastSaved.getAssmtUsers().isEmpty()){
			return false;
		}
		return true;
	}
	public String getLastSavedRubricId(){
		Assessment assmtLastSaved = aDAO.getAssessmentById(aId);
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
		assmt = null;
		project = null;
		rubric = null;
	}
	
	
	public List<AssessCriterion> getFirstCriterions(){
		if(assmt.getCriterias().size()==0)
			return new ArrayList<AssessCriterion>();
		return assmt.getCriterias().get(0).getCriterions();
	}
	private AssessCriterion findCriterionById(Assessment assmt, long criterionId){
		for(AssessCriteria ac : assmt.getCriterias()){
			for(AssessCriterion acion : ac.getCriterions()){
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
