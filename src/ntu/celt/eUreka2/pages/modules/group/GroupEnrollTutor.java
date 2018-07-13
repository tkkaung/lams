package ntu.celt.eUreka2.pages.modules.group;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupDAO;
import ntu.celt.eUreka2.modules.group.GroupUser;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

public class GroupEnrollTutor extends AbstractPageGroup {
	@Property
	private Project curProj;
	@Property
	private Group group;
	@Property
	private long groupId;
	@Property
	private long groupUserID;
	
	
	private enum SubmitType {ASSIGN, UNASSIGN, SAVE_GROUPNAME}; 
	@Property
	private List<User> resultUsers ;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@SuppressWarnings("unused")
	@Property
	private User enrlUser;
	@SuppressWarnings("unused")
	@Property
	private GroupUser groupUser ;
	
	private SubmitType submitType;
	
	
	@Inject
	private UserDAO userDAO;
	@Inject
	private GroupDAO groupDAO;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	@Inject
	private BeanModelSource source;
	@Inject
    private PropertyConduitSource propertyConduitSource; 
	
	@SessionState
	private AppState appState;
	
		
	

	void onActivate(EventContext ec) {
		groupId = ec.get(Long.class, 0);
	}

	Object[] onPassivate() {
		return new Object[] { groupId };
	}

	void setupRender() {
		group = groupDAO.getGroupById(groupId);
		if (group == null)
			throw new RecordNotFoundException(messages.format(
					"entity-not-exists", "GroupID", groupId));
		curProj = group.getProject();

		if (!canManageGroup(curProj)) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		if(resultUsers==null){
			resultUsers = getLeaderMembers(curProj);
		}
		resultUsers = filterSelected(resultUsers);
		
	}

	
	void onPrepareForSubmit(){
		group = groupDAO.getGroupById(groupId);
		if (group == null)
			throw new RecordNotFoundException(messages.format(
					"entity-not-exists", "GroupId", groupId));
		curProj = group.getProject();
	}
	

	private List<User> filterSelected(List<User> users){
		for(int i=0; i<group.getGroupUsers().size(); i++){
			GroupUser gu = group.getGroupUsers().get(i);
			if(gu.getTutor()!=null)
				users.remove(gu.getTutor());
		}
		return users;
	}
	
	void onSelectedFromAssign(long groupUserID) {
		this.groupUserID = groupUserID;
		submitType = SubmitType.ASSIGN;
	}
	void onSelectedFromUnassign(long groupUserID) {
		this.groupUserID = groupUserID;
		submitType = SubmitType.UNASSIGN;
	}
	
	
	@Component(id = "enrollForm")
	private Form enrollForm;
	void onValidateFormFromEnrollForm() {
		if(SubmitType.ASSIGN.equals(submitType)){
			String[] selUserId = request.getParameters("chkBox");
			if(selUserId==null || selUserId.length==0){
				enrollForm.recordError(messages.get("select-at-least-one-from-left"));
			}
			else if(selUserId.length>1){
				enrollForm.recordError("Please select only one user to assign");
			}
			
		}
		else if(SubmitType.UNASSIGN.equals(submitType)){
			String[] selProjUserId = request.getParameters("selChkBox" + groupUserID);
			if(selProjUserId==null || selProjUserId.length==0){
				enrollForm.recordError(messages.get("select-at-least-one-from-right"));
			}			
		}
		
	}
	@CommitAfter
	void onSuccessFromEnrollForm(){
		if(SubmitType.ASSIGN.equals(submitType)){
			String[] selUserId = request.getParameters("chkBox");
			GroupUser gu = groupDAO.getGroupUserById(groupUserID);
			Group g = gu.getGroup();
			int count = 0;
			for(String uId : selUserId){
				User u = userDAO.getUserById(Integer.parseInt(uId));
				gu.setTutor(u);
					groupDAO.saveGroupUser(gu);
					appState.recordInfoMsg(u.getDisplayName() + " have been assigned as group tutor for group "+gu.getGroupNumNameDisplay() );
			}

		}
		else if(SubmitType.UNASSIGN.equals(submitType)){
			String[] selUserId = request.getParameters("selChkBox"+groupUserID);
			GroupUser gu = groupDAO.getGroupUserById(groupUserID);
			for(String uId : selUserId){
				User u = userDAO.getUserById(Integer.parseInt(uId));
				gu.setTutor(null);
				groupDAO.saveGroupUser(gu);
				appState.recordInfoMsg(u.getDisplayName() + " have been unassigned from group tutor for group " +gu.getGroupNumNameDisplay() );
			}
			
		}
		
	}
	@Inject
	private Logger logger;
	
	public BeanModel<User> getModel() {
		BeanModel<User> model = source.createEditModel(User.class, messages);
		model.include("username","Email");
		model.add("name", propertyConduitSource.create(User.class, "getDisplayName()")); 
		//model.get("displayName").label(messages.get("name-label"));
		//model.add("chkBox", null);
		model.reorder( "username", "name");
		return model;
	}
	public BeanModel<User> getModelLeft() {
		BeanModel<User> model = source.createEditModel(User.class, messages);
		model.include("username","Email");
		model.add("name", propertyConduitSource.create(User.class, "getDisplayName()")); 
		//model.get("displayName").label(messages.get("name-label"));
		model.add("chkBox", null);
		model.reorder("chkBox", "username", "name");
		return model;
	}

	@Cached
	public String getShouldDisableInput(){	
		return null;
	}
	
}
