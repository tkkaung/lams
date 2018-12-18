package ntu.celt.eUreka2.pages.modules.big5;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.big5.BIG5DAO;
import ntu.celt.eUreka2.modules.big5.BIG5Survey;

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


public class CheckStatus extends AbstractPageBIG5{
	@Property
	private Project project;
	@Property
	private long eid;
	@Property
	private BIG5Survey big5;
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
	private BIG5DAO eDAO;
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
		
		big5 = eDAO.getBIG5SurveyById(eid);
		if(big5==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "BIG5 ID", eid));
		project = big5.getProject();
		assessors = getNotSubmitedAssesseesByBIG5Survey(big5); 
	}
	long onPassivate() {
		return eid;
	}
	
	
	void setupRender() {
		if(!canManageBIG5Survey(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
	}
	
	public BeanModel<User> getModel() {
		BeanModel<User> model = beanModelSource.createEditModel(User.class, messages);
		model.include();
		model.add("chkBox", null);
		model.add("no", null);
		model.add("name", propertyConduitSource.create(User.class, "displayName"));
		model.get("name").label("Student Name");
		model.add("status", null);
		model.add("endDate", null);
	//	model.add("action",null);
		
		return model;
	}
	
	public int getTotalSize() {
		if (assessors == null)
			return 0;
		return assessors.size();
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
	private SendEmails pageSendEmailGroup;
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
