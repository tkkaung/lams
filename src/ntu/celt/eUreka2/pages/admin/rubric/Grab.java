package ntu.celt.eUreka2.pages.admin.rubric;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.RedirectException;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.assessment.RubricCriteria;
import ntu.celt.eUreka2.modules.assessment.RubricCriterion;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
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

public class Grab extends AbstractPageAdminRubric{
	
	@Persist
	@Property
	private Rubric rubric;
	@Persist
	@Property
	private List<RubricCriteria> rCritList; 
	private int trId;  //template rubric ID
	@Persist
	@Property
	private Rubric templateRubric;
	private Boolean resetPersist; 
	
	@SuppressWarnings("unused")
	@Property
	private RubricCriteria tempRCrit; 
	@SuppressWarnings("unused")
	@Property
	private RubricCriterion tempRCriterion; 
	@Property
	private int rowIndex;
	@Property
	private int colIndex;
	
	@Property
	private Integer numCrit ;
	@Property
	private Integer numCriterion ;
	
	
	enum SubmitType {SUBMIT, UPDATE_NUM_FIELDS, DELETEROW, DELETECOL, INSERTROW, INSERTCOL};
	private SubmitType submitType = SubmitType.SUBMIT;
	
	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Request request;
	
	void onActivate(EventContext ec) {
		if(ec.getCount()==1)
			trId = ec.get(Integer.class, 0);
		else if(ec.getCount()==2){
			trId = ec.get(Integer.class, 0);
			resetPersist = ec.get(Boolean.class, 1);
		}
	}
	Object[] onPassivate() {
		if(resetPersist==null)
			return new Object[]{trId};
		return new Object[]{trId, resetPersist};
	}
	
	public boolean canGrabRubric(Rubric r){
		if(r.getOwner().equals(getCurUser()))
			return true;
		if(r.isShared())
			return true;
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_RUBRIC))
			return true;
		
		return false;
	}
	
	void setupRender(){
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_OWN_RUBRIC)) 
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		if(resetPersist!=null && resetPersist==true){
			resetPersistValues();
			rubric = new Rubric();
			templateRubric = aDAO.getRubricById(trId);
			if(templateRubric==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "TemplateRubricID", trId));
			if(!canGrabRubric(templateRubric))
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			
			rCritList = new ArrayList<RubricCriteria>();
			for(RubricCriteria rc : templateRubric.getCriterias()){
				rCritList.add(rc.clone());
			}
			
			rubric = templateRubric.clone();


			rubric.setName(templateRubric.getName() + messages.get("suffix-copy"));
//			rubric.setDes(templateRubric.getDes());
			
			resetPersist = null;
		}	
		
		numCrit = rCritList.size();
		numCriterion = getFirstCriterions().size();//rCritList.get(0).getCriterions().size();
		
		saveRubricAndRedirectToEdit();
	}
	
	private void saveRubricAndRedirectToEdit(){
		rubric.setCdate(new Date());
		rubric.setMdate(new Date());
		rubric.setOwner(getCurUser());
		
//		for(RubricCriteria rc : rCritList){
//			for(int i=0; i<getFirstCriterions().size(); i++){
//				rc.getCriterions().get(i).setScore(rCritList.get(0).getCriterions().get(i).getScore());
//			}
//		}
//		rubric.setCriterias(rCritList);

		
		
		aDAO.immediateAddRubric(rubric);
		Link editRubricLink = linkSource.createPageRenderLinkWithContext(Edit.class, rubric.getId());
		throw new RedirectException(editRubricLink);
		
	}
	
	void onPrepareForSubmitFromForm(){
		if(rubric==null){
			rubric = new Rubric();
		}
		if(rCritList==null){
			rCritList = new ArrayList<RubricCriteria>();
			templateRubric = aDAO.getRubricById(trId);
			for(RubricCriteria rc : templateRubric.getCriterias()){
				rCritList.add(rc.clone());
			}
		}
	}
	
	public RubricCriteria getRCrit(int index){
		return rCritList.get(index);
	}
	public RubricCriterion getRCriterion(int rowIndex, int colIndex){
		return rCritList.get(rowIndex).getCriterions().get(colIndex);
	}
	public List<RubricCriterion> getFirstCriterions(){
		return rCritList.get(0).getCriterions();
	}
	
	@Component(id="form")
	private Form form;
	@Component(id="weight")
	private TextField weightField;
	
	void onValidateFormFromForm(){
		if(submitType.equals(SubmitType.SUBMIT) && getTotalWeight(rCritList)!=100){
			form.recordError(weightField, messages.get("total-weight-must-equal-100"));
		}
	}
	
	@CommitAfter
	Object onSuccessFromForm(){
		int	nCrit = rCritList.size();
		int	nCriterion = getFirstCriterions().size();
		int rowIdx = Integer.parseInt(request.getParameter("rowIdx"));
		int colIdx = Integer.parseInt(request.getParameter("colIdx"));
		
	  switch(submitType){
		case SUBMIT:
			rubric.setCdate(new Date());
			rubric.setMdate(new Date());
			rubric.setOwner(getCurUser());
			
			for(RubricCriteria rc : rCritList){
				for(int i=0; i<getFirstCriterions().size(); i++){
					rc.getCriterions().get(i).setScore(rCritList.get(0).getCriterions().get(i).getScore());
				}
			}
			rubric.setCriterias(rCritList);
			
			aDAO.addRubric(rubric);
			
			resetPersistValues();
			return linkSource.createPageRenderLink(Home.class);
			
		case UPDATE_NUM_FIELDS:
			
			if(numCrit<nCrit){
				rCritList = rCritList.subList(0, numCrit);
			}
			else if(numCrit>nCrit){
				for(int i=nCrit; i<numCrit; i++){
					RubricCriteria rc = new RubricCriteria();
					for(int j=0; j<nCriterion; j++){
						RubricCriterion rCriterion = new RubricCriterion();
						rCriterion.setScore(j+1);
						rc.addCriterion(rCriterion);
					}
					rCritList.add(rc);
				}
			}
			if(numCriterion<nCriterion){
				for(RubricCriteria rc : rCritList){
					rc.setCriterions(rc.getCriterions().subList(0, numCriterion));
				}
			}
			else if(numCriterion>nCriterion){
				for(RubricCriteria rc : rCritList){
					for(int i=nCriterion; i<numCriterion; i++){
						RubricCriterion rCriterion = new RubricCriterion();
						rCriterion.setScore(i+1);
						rc.addCriterion(rCriterion);
					}
				}
			}
			
			break;
		case DELETEROW:
			rCritList.remove(rowIdx);
			break;
		case DELETECOL:
			for(RubricCriteria rc : rCritList){
				for (RubricCriterion rrcc : rc.getCriterions()) {
					if (rrcc.getScore() > colIdx) {
						rrcc.setScore(rrcc.getScore() - 1);
					}
				}
				List<RubricCriterion> tmp = rc.getCriterions();
				tmp.remove(colIdx);
				rc.setCriterions(tmp);
			}
			break;
		case INSERTROW:
			RubricCriteria rc = new RubricCriteria();
			for(int i = 0; i < nCriterion; i++){
				RubricCriterion rCriterion = new RubricCriterion();
				rCriterion.setScore(i+1);
				rc.addCriterion(rCriterion);
			}
			rCritList.add(rowIdx, rc);
			break;
		case INSERTCOL:
			for(RubricCriteria rrc : rCritList){
				for (RubricCriterion rrcc : rrc.getCriterions()) {
					if (rrcc.getScore() > colIdx) {
						rrcc.setScore(rrcc.getScore() + 1);
					}
				}
				RubricCriterion rCriterion = new RubricCriterion();
				rCriterion.setScore(colIdx + 1);
				rrc.getCriterions().add(colIdx, rCriterion);
			}
			break;
	  }
		
	  return null;
	}
	
	
	void onSelectedFromUpdateNumFields(){
		submitType = SubmitType.UPDATE_NUM_FIELDS;
	}
	void onSelectedFromBSubmit(){
		submitType = SubmitType.SUBMIT;
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
		rubric = null;
		rCritList = null;
		templateRubric = null;
	}
	public int getRowNum(){
		return rowIndex+1;
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
