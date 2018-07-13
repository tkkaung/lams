package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;


public class CheckStatus extends AbstractPageEvaluation{
	@Property
	private Project project;
	@Property
	private long eid;
	@Property
	private Evaluation eval;
	@SuppressWarnings("unused")
	@Property
	private List<User> assessors;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@Property
	private int rowIndex;
	@SessionState
	private AppState appState;
	
	private enum SubmitType { EMAIL};
	private SubmitType submitType;

	
	@InjectComponent
	private Grid grid;

	@Inject
	private EvaluationDAO eDAO;
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;
	@Inject
	private PropertyConduitSource propertyConduitSource;
	@Inject
	private Request _rqust;
	
	
	void onActivate(long id) {
		eid = id;
		
		eval = eDAO.getEvaluationById(eid);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Evaluation ID", eid));
		project = eval.getProject();
		assessors = getAllAssessees(project, eval.getGroup());
	}
	long onPassivate() {
		return eid;
	}
	
	
	void setupRender() {
		if(!canManageEvaluation(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
	}
	
	public BeanModel<User> getModel() {
		BeanModel<User> model = beanModelSource.createEditModel(User.class, messages);
		model.include();
		model.add("chkBox", null);
		model.add("no", null);
		model.add("name", propertyConduitSource.create(User.class, "displayName"));
		model.get("name").label("Evaluator's Name");
		model.add("group", null);
		model.add("status", null);
		model.add("endDate", null);
	//	model.add("action",null);
		
		return model;
	}
	
	
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	public int getRowNum(){
		return (grid.getCurrentPage()-1)*grid.getRowsPerPage()+ rowIndex + 1;
	}
	
	void onSelectedFromEmail(){
		submitType = SubmitType.EMAIL;
	}
	@InjectPage
	private SendEmailGroup pageSendEmailGroup;
	public Object onSuccessFromProjForm(){
		String[] selUserId = _rqust.getParameters("gridChkBox");
		switch(submitType){
			case EMAIL:
				pageSendEmailGroup.setContext(eid, selUserId);
				return pageSendEmailGroup;
		}
		return null;
	}
}
