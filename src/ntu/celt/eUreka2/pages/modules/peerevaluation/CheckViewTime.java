package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.peerevaluation.EvalAssesseeView;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;


public class CheckViewTime extends AbstractPageEvaluation{
	@Property
	private Project project;
	@Property
	private long eid;
	@Property
	private Evaluation eval;
	@SuppressWarnings("unused")
	@Property
	private List<User> assessees;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@Property
	private int rowIndex;
	@SessionState
	private AppState appState;
	
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
	
	
	void onActivate(long id) {
		eid = id;
		
		eval = eDAO.getEvaluationById(eid);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Evaluation ID", eid));
		project = eval.getProject();
		assessees = getAllAssessees(project, eval.getGroup());
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
		model.add("no", null);
		model.add("name", propertyConduitSource.create(User.class, "displayName"));
		model.get("name").label("Student Name");
		model.add("group", null);
		model.add("firstViewTime", null);
		model.add("lastViewTime", null);
		
		return model;
	}
	
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	public int getRowNum(){
		return (grid.getCurrentPage()-1)*grid.getRowsPerPage()+ rowIndex + 1;
	}
	
	public String getEvalAssesseeViewFirst(Evaluation eval, User user){
		EvalAssesseeView evalAView = new EvalAssesseeView(user);
		int index = eval.getEvalAssesseeViews().indexOf(evalAView);
		if(index < 0)
			return "";
		return eval.getEvalAssesseeViews().get(index).getFirstGradeViewTimeDisplay();
	}
	public String getEvalAssesseeViewLast(Evaluation eval, User user){
		EvalAssesseeView evalAView = new EvalAssesseeView(user);
		int index = eval.getEvalAssesseeViews().indexOf(evalAView);
		if(index < 0)
			return "";
		return eval.getEvalAssesseeViews().get(index).getLastGradeViewTimeDisplay();
	}
	
	
}
