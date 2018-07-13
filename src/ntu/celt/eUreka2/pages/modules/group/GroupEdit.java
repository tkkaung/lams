package ntu.celt.eUreka2.pages.modules.group;

import java.util.Date;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupDAO;
import ntu.celt.eUreka2.modules.group.GroupUser;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class GroupEdit  extends AbstractPageGroup{
	@Property
	private String projId;
	@Property
	private Project curProj;
	@Property
	private Group group;
	@Property
	private long groupId;
	@Property
	private int numOfGroup = 3;
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private GroupDAO groupDAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	
	
	void onActivate(EventContext ec) {
		if(ec.getCount()==1){
			groupId = ec.get(Long.class, 0);
		}
		if(ec.getCount()==2){
			projId = ec.get(String.class, 0);
			groupId = ec.get(Long.class, 1);
		}
	}
	Object[] onPassivate() {
		if(projId==null)
			return new Object[] {groupId};
		else
			return new Object[] {projId, groupId};
	}
	
	public boolean isCreateMode(){
		if(groupId == 0)
			return true;
		return false;
	}
	
	void setupRender(){
		if(isCreateMode()){ //create new
			curProj = projDAO.getProjectById(projId);
			if(curProj==null){
				throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", projId));
			}
			if(!canManageGroup(curProj)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
			
		}
		else{ //edit
			group = groupDAO.getGroupById(groupId);
			if(group==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "GroupId", groupId));
			curProj = group.getProject();
			
			if(!canManageGroup(curProj)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
			numOfGroup = group.getGroupUsers().size();
			
			if(groupDAO.isGroupBeingUsePE(group)){
				appState.recordWarningMsg("This group cannot be edited as it is currently being used for Peer Evaluation.");
			}
			if(groupDAO.isGroupBeingUseAS(group)){
				appState.recordWarningMsg("WARNING: This group is being used in Assessment Module." );
			}


		
		}
	}

	void onPrepareFromForm(){
		if(isCreateMode()){
			curProj = projDAO.getProjectById(projId);
			if(group==null)
				group = new Group();
		}
		else{
			group = groupDAO.getGroupById(groupId);
			curProj = group.getProject();
		}
	}
	
	@CommitAfter
	Object onSuccessFromForm(){
		if(isCreateMode()){ //creating new
			group.setCdate(new Date());
			group.setCreator(getCurUser());
			group.setProject(curProj);
			
			for( int i=1; i<=numOfGroup; i++){
				GroupUser gu = new GroupUser();
			//	gu.setGroup(group);
				gu.setGroupNum(i);
				group.addGroupUser(gu);
			}
		}
		else{ //Edit
			if(group.getGroupUsers().size() == numOfGroup){
				//do nothing
			}
			else if(group.getGroupUsers().size()> numOfGroup){
				for( int i=group.getGroupUsers().size()-1; i>=numOfGroup; i--){
					group.getGroupUsers().get(i).getUsers().clear();
					group.getGroupUsers().remove(i);
				}
			}
			else {
				for( int i=group.getGroupUsers().size()+1; i<=numOfGroup; i++){
					GroupUser gu = new GroupUser();
					gu.setGroup(group);
					gu.setGroupNum(i);
					group.addGroupUser(gu);
				}
			}
		}
		group.setMdate(new Date());
		
		
		groupDAO.saveGroup(group);
		
		return linkSource.createPageRenderLinkWithContext(GroupEnrollUser.class, group.getId());
	}
	
	
	
	@Cached
	public String getShouldDisableInput(){
		if(groupDAO.isGroupBeingUsePE(group))
			return "disabled";
		return null;
	}
	
}
