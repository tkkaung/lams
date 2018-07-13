package ntu.celt.eUreka2.pages.admin.rubric;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.components.Layout;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RedirectException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.assessment.RubricCriteria;
import ntu.celt.eUreka2.modules.assessment.RubricCriterion;
import ntu.celt.eUreka2.modules.assessment.RubricOpenQuestion;
import ntu.celt.eUreka2.pages.project.ManageMember;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Select;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;

public class New extends AbstractPageAdminRubric{
	
	@Persist
	@Property
	private Rubric rubric;
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
	@SessionState
	private AppState appState;
	
	
	@Persist
	@Property
	private int temCreate ; //0 = create from scratch, 1 = load from template
	@Persist
	@Property
	private Rubric rubricToLoad;
	@Property
	private Integer numCrit ;
	@Property
	private Integer numCriterion ;
	
	private String openQuestion ;
	
	private String oQuestion ;
	private boolean inFormSubmission;
	private boolean inRender;
	@Property
	private List<String> questionList ;
	
	
	enum SubmitType {SUBMIT, LOAD_RUBRIC, UPDATE_NUM_FIELDS, DELETEROW, DELETECOL, INSERTROW, INSERTCOL};
	private SubmitType submitType = SubmitType.SUBMIT;
	
	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Request request;
	
	
	
    @SessionAttribute(Layout.EUREKA2_CALL_BACK_IFRAME)
    private String callBackURLForIframe;

	
	
	void onActivate(Boolean resetPersist) {
		this.resetPersist = resetPersist;
		inFormSubmission = false;
		inRender = false;

	}
	Boolean onPassivate() {
		return resetPersist;
	}
	
	void onPrepareForRender(){
		inRender = true;
	}
	void setupRender(){
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_OWN_RUBRIC)) 
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		
		
		if((resetPersist!=null && resetPersist==true ) ) {
			resetPersistValues();
			resetPersist = null;
		}
		
		if(rubric==null){
			rubric = new Rubric();
			rubric.setName(messages.get("untitled"));
			
			numCrit = 2;
			numCriterion = 4;
			for(int i=0; i<numCrit; i++){
				RubricCriteria rCrit = new RubricCriteria();
				for(int j=0; j<numCriterion; j++){
					RubricCriterion rCriterion = new RubricCriterion();
					rCriterion.setScore(j+1);
					rCrit.addCriterion(rCriterion);
				}
				rCrit.setWeightage(Math.round(100/numCrit));
				rubric.getCriterias().add(rCrit);
			}
		}
		
		numCrit = rubric.getCriterias().size();
		numCriterion = getFirstCriterions().size();//rubric.getCriterias().get(0).getCriterions().size();
		
		
		
		
	//	saveRubricAndRedirectToEdit();
		
		
		
	}
	
	
	private void saveRubricAndRedirectToEdit(){
		rubric.setCdate(new Date());
		rubric.setMdate(new Date());
		rubric.setOwner(getCurUser());
		
		List<RubricCriterion> firstCriterions = getFirstCriterions();
		
		for(RubricCriteria rc : rubric.getCriterias()){
			for(int i=0; i<firstCriterions.size(); i++){
				rc.getCriterions().get(i).setScore(firstCriterions.get(i).getScore());
			}
		}
		
		//-------R001 -- remove over crit, cause from onPrepareForSubmitFromForm(), because "Persist" rubric become NULL after sometime
		rubric.setCriterias( rubric.getCriterias().subList(0, numCrit));
		for(RubricCriteria rc : rubric.getCriterias()){
			rc.setCriterions(rc.getCriterions().subList(0, numCriterion));
		}
		//-------R001 -- 
	
		aDAO.immediateAddRubric(rubric);
		Link editRubricLink = linkSource.createPageRenderLinkWithContext(Edit.class, rubric.getId());
		throw new RedirectException(editRubricLink);
		
	}
	

	public String getOpenQuestion() {
        return openQuestion;
    }
	public void setOpenQuestion(String openQuestion) {
        this.openQuestion = openQuestion;

        if (inFormSubmission) {
        	questionList.add(openQuestion);
        }
    }
	public String getOQuestion() {
		
        return oQuestion;
    }
	public void setOQuestion(String oQuestion) {
        this.oQuestion = oQuestion;
		if(inRender){
			openQuestion = oQuestion;
		}
    }
		
	void onPrepareForSubmitFromForm(){
		if(rubric==null){
			rubric = new Rubric();
			//rubric.setName(messages.get("untitled"));
			
			numCrit = 20;
			numCriterion = 20;
			for(int i=0; i<numCrit; i++){
				RubricCriteria rCrit = new RubricCriteria();
				for(int j=0; j<numCriterion; j++){
					RubricCriterion rCriterion = new RubricCriterion();
					rCriterion.setScore(j+1);
					rCrit.addCriterion(rCriterion);
				}
//				rCrit.setWeightage(Math.round(100/numCrit));
				rubric.getCriterias().add(rCrit);
			}
		}
		inFormSubmission = true;
		questionList = new ArrayList<String>();

	}
	
	public RubricCriteria getRCrit(int index){
		return rubric.getCriterias().get(index);
	}
	public RubricCriterion getRCriterion(int rowIndex, int colIndex){
		return rubric.getCriterias().get(rowIndex).getCriterions().get(colIndex);
	}
	public List<RubricCriterion> getFirstCriterions(){
		return rubric.getCriterias().get(0).getCriterions();
	}
	
	@Component(id="form")
	private Form form;
	@Component(id="rubricToLoad")
	private Select rubricToLoadSelect;
	@Component(id="weight")
	private TextField weightField;
	
	
	void onValidateFormFromForm(){
		if(temCreate==1 && rubricToLoad==null){
			form.recordError(rubricToLoadSelect, messages.get("error-select-template-to-load"));
		}
		if(submitType.equals(SubmitType.SUBMIT) && getTotalWeight(rubric.getCriterias())!=100){
			form.recordError(weightField, messages.get("total-weight-must-equal-100"));
		}
		
	}
	
	
	@CommitAfter
	Object onSuccessFromForm(){
		int	nCrit = rubric.getCriterias().size();
		int	nCriterion = getFirstCriterions().size();
		int rowIdx = Integer.parseInt(request.getParameter("rowIdx"));
		int colIdx = Integer.parseInt(request.getParameter("colIdx"));

		if(canUseOpenQuestion()){
			
			rubric.getOpenEndedQuestions().clear();
			for(String q : questionList){
				//logger.info("  q="+ q);
				rubric.addOpenEndedQuestions(q);			
			}
		}
		
	  switch(submitType){
	  
		case SUBMIT:
			List<RubricCriterion> firstCriterions = getFirstCriterions();
			
			//check GMAT, 1,2,3,4,5,6
			if(rubric.getGmat()){
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
			
			
			rubric.setCdate(new Date());
			rubric.setMdate(new Date());
			rubric.setOwner(getCurUser());
			
			for(RubricCriteria rc : rubric.getCriterias()){
				for(int i=0; i<firstCriterions.size(); i++){
					rc.getCriterions().get(i).setScore(firstCriterions.get(i).getScore());
				}
			}
			
			//-------R001 -- remove over crit, cause from onPrepareForSubmitFromForm(), because "Persist" rubric become NULL after sometime
			rubric.setCriterias( rubric.getCriterias().subList(0, numCrit));
			for(RubricCriteria rc : rubric.getCriterias()){
				rc.setCriterions(rc.getCriterions().subList(0, numCriterion));
			}
			//-------R001 -- 
		
			aDAO.addRubric(rubric);
			
			resetPersistValues();
			
			if(callBackURLForIframe!=null && !"".equals(callBackURLForIframe)){
				Util.sendHttpGETAsync(callBackURLForIframe);
				callBackURLForIframe = null;
			}
			
			return linkSource.createPageRenderLink(Home.class);
			
		case UPDATE_NUM_FIELDS:
			
			if(numCrit<nCrit){
				rubric.setCriterias( rubric.getCriterias().subList(0, numCrit));
			}
			else if(numCrit>nCrit){
				for(int i=nCrit; i<numCrit; i++){
					RubricCriteria rc = new RubricCriteria();
					for(int j=0; j<nCriterion; j++){
						RubricCriterion rCriterion = new RubricCriterion();
						rCriterion.setScore(j+1);
						rc.addCriterion(rCriterion);
					}
					rubric.getCriterias().add(rc);
				}
			}
			if(numCriterion<nCriterion){
				for(RubricCriteria rc : rubric.getCriterias()){
					rc.setCriterions(rc.getCriterions().subList(0, numCriterion));
				}
			}
			else if(numCriterion>nCriterion){
				for(RubricCriteria rc : rubric.getCriterias()){
					for(int i=nCriterion; i<numCriterion; i++){
						RubricCriterion rCriterion = new RubricCriterion();
						rCriterion.setScore(i+1);
						rc.addCriterion(rCriterion);
					}
				}
			}
			
			break;
		case LOAD_RUBRIC:
			if(temCreate==1){
				rubric.getCriterias().clear();
				for(RubricCriteria rc : rubricToLoad.getCriterias()){
					rubric.getCriterias().add(rc.clone());
				}
			//	rubric.setGmat(rubricToLoad.getGmat());
			}
			
			break;
		case DELETEROW:
			rubric.getCriterias().remove(rowIdx);
			break;
		case DELETECOL:
			for(RubricCriteria rc : rubric.getCriterias()){
				for (RubricCriterion rrcc : rc.getCriterions()) {
					if (rrcc.getScore() > colIdx) {
						rrcc.setScore(rrcc.getScore() - 1);
					}
				}
				List<RubricCriterion> tmp = rc.getCriterions();
				tmp.remove(colIdx);
			//	rc.setCriterions(tmp);
			}
			break;
		case INSERTROW:
			RubricCriteria rc = new RubricCriteria();
			for(int i = 0; i < nCriterion; i++){
				RubricCriterion rCriterion = new RubricCriterion();
				rCriterion.setScore(i+1);
				rc.addCriterion(rCriterion);
			}
			rubric.getCriterias().add(rowIdx, rc);
			break;
		case INSERTCOL:
			for(RubricCriteria rrc : rubric.getCriterias()){
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
	void onSelectedFromLoadTemplate(){
		submitType = SubmitType.LOAD_RUBRIC;
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
		temCreate = 0;
		rubricToLoad = null;
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
	
	
	
	  Object onAddRowFromQuestions()
	  {
	    
	    String question = "";
	    if(rubric !=null){
		    rubric.addOpenEndedQuestions(question);
	    }
	    return question;
	  }

	  void onRemoveRowFromQuestions(String question)
	  {
		 if(rubric !=null){
		    rubric.removeOpenEndedQuestions(question);
		 }
	  }

	  
}
