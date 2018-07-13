package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.AuditTrailDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.AuditTrail;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.PropertyConduitSource;

public class ReleaseResult extends AbstractPageEvaluation{
	@Property
	private Project project;
	private long eId;
	@Property
	private Evaluation eval;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@Property
	private int rowIndex;
	private Evaluation oldEval;

	
	@Inject
	private EvaluationDAO eDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;
	@Inject
	private PropertyConduitSource propertyConduitSource;
	@Inject
	private AuditTrailDAO auditDAO;
	
	
	void onActivate(EventContext ec) {
		eId = ec.get(Long.class, 0);
		
		eval = eDAO.getEvaluationById(eId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "EvaluationID", eId));
		
		project = eval.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {eId};
	}
	
	
	
	void setupRender(){
		if(!canManageEvaluation(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
	
		
		
	}
	void onPrepareFromForm(){
		oldEval = eval.clone();
	}
	
	
	public boolean hasNotAttempt(){
		for(User u : getAllAssessees(project)){
			if(messages.get("Not-Attempted").equals(getEvalStatus(eval, u))){
				return true;
			}
		}
		return false;
	}
	public List<User> getNotAttemptAssessees(){
		List<User> uList = new ArrayList<User>();
		for(User u : getAllAssessees(project)){
			if(messages.get("Not-Attempted").equals(getEvalStatus(eval, u))){
				uList.add(u);
			}
		}
		return uList;
	}
	
	
	@CommitAfter
	Object onSuccessFromForm(){
		eval.setMdate(new Date());
		eval.setEditor(getCurUser());
		//eval.setReleased(true);
		
		eDAO.updateEvaluation(eval);
		
		AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(eval.getId())
				, "Release Result", getCurUser());
		ad = eval.getDifferent(oldEval, ad);
		if (!ad.getPrevValue().isEmpty() || !ad.getNewValue().isEmpty()){			
			auditDAO.saveAuditTrail(ad);
		}
		
		
		return linkSource.createPageRenderLinkWithContext(ViewAverage.class, eval.getId());
	}
	
	
	public BeanModel<User> getModel() {
		BeanModel<User> model = beanModelSource.createEditModel(User.class, messages);
		model.include();
		model.add("no", null);
		model.add("name", propertyConduitSource.create(User.class, "displayName"));
		model.add("group", null);
		
		return model;
	}
	
	public int getRowNum(){
		return rowIndex+1;
	}
	
}
