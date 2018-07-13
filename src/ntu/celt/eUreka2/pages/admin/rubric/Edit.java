package ntu.celt.eUreka2.pages.admin.rubric;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.components.Layout;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.assessment.RubricCriteria;
import ntu.celt.eUreka2.modules.assessment.RubricCriterion;
import ntu.celt.eUreka2.modules.assessment.RubricOpenQuestion;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextArea;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.services.LoggingAdvice;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

public class Edit extends AbstractPageAdminRubric{
	
	@Persist
	@Property
	private Rubric rubric;
	
	private int rId;  
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
	//private String question ;

	private String openQuestion ;
	
	private String oQuestion ;
	private boolean inFormSubmission;
	private boolean inRender;
	@Property
	private List<String> questionList ;
	
	
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
	@Inject
	private Logger logger;
	
	@SessionState
	private AppState appState;
	
    @SessionAttribute(Layout.EUREKA2_CALL_BACK_IFRAME)
    private String callBackURLForIframe;

	
	void onActivate(EventContext ec) {
		if(ec.getCount()==1)
			rId = ec.get(Integer.class, 0);
		else if(ec.getCount()==2){
			rId = ec.get(Integer.class, 0);
			resetPersist = ec.get(Boolean.class, 1);
		}
		inFormSubmission = false;
		inRender = false;
	}
	Object[] onPassivate() {
		if(resetPersist==null)
			return new Object[]{rId};
		return new Object[]{rId, resetPersist};
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
	
	void onPrepareForRender(){
		inRender = true;
	}
	void setupRender(){
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_OWN_RUBRIC)) 
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		if((resetPersist!=null && resetPersist==true) || rubric==null){
			rubric = aDAO.getRubricById(rId);
			if(rubric==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "RubricID", rId));
			if(!canEditRubric(rubric))
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			
			
			resetPersist = null;
		}	
		
		numCrit = rubric.getCriterias().size();
		numCriterion = getFirstCriterions().size();//rCritList.get(0).getCriterions().size();
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
	
	
	@Component(id="form")
	private Form form;
	@Component(id="weight")
	private TextField weightField;
	void onValidateFormFromForm(){
		if(submitType.equals(SubmitType.SUBMIT) && getTotalWeight(rubric.getCriterias())!=100){
			form.recordError(weightField, messages.get("total-weight-must-equal-100"));
		}
	}
	
	void onPrepareForSubmitFromForm(){
		if(rubric==null){
			rubric = aDAO.getRubricById(rId);
		}
		inFormSubmission = true;
		questionList = new ArrayList<String>();
	}
	
	@CommitAfter
	Object onSuccessFromForm() {
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
			
			
			//rubric.setCdate(new Date());
			rubric.setMdate(new Date());
			//rubric.setOwner(getCurUser());
			
			for(RubricCriteria rc : rubric.getCriterias()){
				for(int i=0; i<firstCriterions.size(); i++){
					rc.getCriterions().get(i).setScore(firstCriterions.get(i).getScore());
				}
			}
			
			
			
			aDAO.updateRubric(rubric);
			
			if(callBackURLForIframe!=null && !"".equals(callBackURLForIframe)){
				Util.sendHttpGETAsync(callBackURLForIframe);
				callBackURLForIframe = null;
			}
			
			resetPersistValues();
			return linkSource.createPageRenderLink(Home.class);
			
		case UPDATE_NUM_FIELDS:
			
			if(numCrit<nCrit){
				rubric.setCriterias(rubric.getCriterias().subList(0, numCrit));
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
	
	

		
	@CommitAfter
	  Object onAddRowFromQuestions()
	  {
	    
	    String question = "";
	    if(rubric !=null){
		    rubric.addOpenEndedQuestions(question);
		    aDAO.updateRubric(rubric);
	    }
	    return question;
	  }

	@CommitAfter
	  void onRemoveRowFromQuestions(String question)
	  {
		 if(rubric !=null){
		    rubric.removeOpenEndedQuestions(question);
		    aDAO.updateRubric(rubric);
		 }
	  }
	  
//	  @Property
//	    private final ValueEncoder<RubricOpenQuestion> valueEncoder = new ValueEncoder<RubricOpenQuestion>()
//	    {
//	        public String toClient(RubricOpenQuestion value) { return String.valueOf(value.getId()); }
//
//	        public RubricOpenQuestion toValue(String clientValue)
//	        {
//	            long id = Long.parseLong(clientValue);
//	            
//	            
//	            return aDAO.getRubricOpenQuestion(id);
//	        }
//	    };
}
