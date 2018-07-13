package ntu.celt.eUreka2.pages.modules.peerevaluation;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationUser;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ViewComment extends AbstractPageEvaluation{
	private long euId;
	private String cmtType; //s = strength , w = weakness
	@Property
	private EvaluationUser evalUser;
	
	
	@Inject
	private EvaluationDAO eDAO;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		euId = ec.get(Long.class, 0);
		cmtType = ec.get(String.class, 1);
	}
	Object[] onPassivate() {
		return new Object[] {euId, cmtType};
	}
	
	void setupRender(){
		evalUser = eDAO.getEvaluationUserById(euId);
		if(evalUser==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "EvaluationUserID", euId));
		if(!canManageEvaluation(evalUser.getEvaluation().getProject()))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
	}

	public String getComments(){
		if("s".equals(cmtType)){
			
			return evalUser.getCmtStrength();
		}
		else if("w".equals(cmtType)){
			return evalUser.getCmtWeakness();
		}
		return evalUser.getCmtOther();
	}
	
}
