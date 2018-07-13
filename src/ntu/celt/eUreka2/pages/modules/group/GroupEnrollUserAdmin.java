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

public class GroupEnrollUserAdmin extends AbstractPageGroup {
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

		if (!canManageGroup(curProj) ) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		if(resultUsers==null){
			resultUsers = getNonLeaderMembers(curProj);
		}
		resultUsers = filterSelected(resultUsers);
		
		if(groupDAO.isGroupBeingUsePE(group)){
			appState.recordWarningMsg("WARNING: This group is being used for Peer Evaluation (some student(s) " +
					"has started evaluation), it's only safe to make change if student that you want to " +
					"change has not started evaluation nor evaluated by others. " +
					"If not, you should delete the evaluated grades before proceed. Consult Administrator on how to delete.");
		}
		if(groupDAO.isGroupBeingUseAS(group)){
			appState.recordWarningMsg("WARNING: This group is being used in Assessment Module." );
		}

	}

	private List<User> filterSelected(List<User> users){
		for(int i=0; i<group.getGroupUsers().size(); i++){
			GroupUser gu = group.getGroupUsers().get(i);
			users.removeAll(gu.getUsers());
		}
		return users;
	}
	
	void onPrepareForSubmit(){
		group = groupDAO.getGroupById(groupId);
		if (group == null)
			throw new RecordNotFoundException(messages.format(
					"entity-not-exists", "GroupId", groupId));
		curProj = group.getProject();
	}
	

	
	
	void onSelectedFromAssign(long groupUserID) {
		this.groupUserID = groupUserID;
		submitType = SubmitType.ASSIGN;
	}
	void onSelectedFromUnassign(long groupUserID) {
		this.groupUserID = groupUserID;
		submitType = SubmitType.UNASSIGN;
	}
	void onSelectedFromSaveGroupName() {
		submitType = SubmitType.SAVE_GROUPNAME;
	}
	
	
	@Component(id = "enrollForm")
	private Form enrollForm;
	void onValidateFormFromEnrollForm() {
		if(SubmitType.ASSIGN.equals(submitType)){
			String[] selUserId = request.getParameters("chkBox");
			if(selUserId==null || selUserId.length==0){
				enrollForm.recordError(messages.get("select-at-least-one-from-left"));
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
				//check not exist before add
				GroupUser guCheck = getGroupUser(g, u);
				if(guCheck==null){
					gu.addUser(u);
					groupDAO.saveGroupUser(gu);
					count++;
				}
				else{
					appState.recordWarningMsg(u.getDisplayName() + " already enrolled in group "+ guCheck.getGroupNumNameDisplay());
				}
			}
			appState.recordInfoMsg(count + " student(s) have been assigned to group " + gu.getGroupNumNameDisplay());

		}
		else if(SubmitType.UNASSIGN.equals(submitType)){
			String[] selUserId = request.getParameters("selChkBox"+groupUserID);
			GroupUser gu = groupDAO.getGroupUserById(groupUserID);
			for(String uId : selUserId){
				User u = userDAO.getUserById(Integer.parseInt(uId));
				gu.removeUser(u);
				
				groupDAO.saveGroupUser(gu);
			}
			appState.recordInfoMsg(selUserId.length + " student(s) have been removed from group " + gu.getGroupNumNameDisplay());

		}
		else if(SubmitType.SAVE_GROUPNAME.equals(submitType)){
			for(GroupUser gu : group.getGroupUsers()){
				String groupName = request.getParameter("groupName" + gu.getId());
				if(groupName != null && !groupName.isEmpty()){
					gu.setGroupNumName(groupName);
				}
				else{
					gu.setGroupNumName(null);
				}
				groupDAO.saveGroupUser(gu);
			}
			appState.recordInfoMsg("Group names have been saved");
		}
	}
	@Inject
	private Logger logger;
	
	public BeanModel<User> getModel() {
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
/*		if(groupDAO.isGroupBeingUse(group))
			return "disabled";
		
		return null;
	*/
	}
	
}
