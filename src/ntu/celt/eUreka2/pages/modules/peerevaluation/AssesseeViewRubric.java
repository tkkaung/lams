package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.List;

import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriteria;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriterion;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class AssesseeViewRubric extends AbstractPageEvaluation{
	@SuppressWarnings("unused")
	@Property
	private Project project;
	@Property
	private long eId;
	
	@Property
	private Evaluation eval;
	@SuppressWarnings("unused")
	@Property
	private EvaluationCriteria tempRCrit; 
	@SuppressWarnings("unused")
	@Property
	private EvaluationCriterion tempRCriterion; 
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	@SuppressWarnings("unused")
	@Property
	private int colIndex;
	@SuppressWarnings("unused")
	@Property
	private int quesIndex;
	@SuppressWarnings("unused")
	@Property
	private String oQuestion;
	
	
	@Inject
	private EvaluationDAO eDAO;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		eId = ec.get(Long.class, 0);
	}
	Object[] onPassivate() {
		return new Object[] {eId};
	}
	
	void setupRender(){
		eval = eDAO.getEvaluationById(eId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "EvaluationID", eId));
		
		project = eval.getProject();
	}
	
	public List<EvaluationCriterion> getFirstCriterions(){
		return eval.getCriterias().get(0).getCriterions();
	}
}

