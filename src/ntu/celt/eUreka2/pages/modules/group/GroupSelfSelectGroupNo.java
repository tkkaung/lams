package ntu.celt.eUreka2.pages.modules.group;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupDAO;
import ntu.celt.eUreka2.modules.group.GroupUser;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.PropertyConduitSource;

public class GroupSelfSelectGroupNo  extends AbstractPageGroup{
	@Property
	private long gId;
	@Property
	private Project curProj;
	@Property
	private Group group;
	@Property
	private List<GroupUser> groupUsers;
	@Property
	private GroupUser groupUser;
	@Property
	private User user;
	@Property
	private boolean alreadyEnrolled = false;
	
	@SessionState
	private AppState appState;
	
	@InjectComponent
	private Grid grid;
	@Property
	private int rowIndex;

	@Inject
	private ProjectDAO projDAO;
	@Inject
	private GroupDAO groupDAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private PropertyConduitSource propertyConduitSource; 
    
	
	void onActivate(EventContext ec) {
		if(ec.getCount()==1){
			gId = ec.get(Long.class, 0);
		}		
		
	}
	Object[] onPassivate() {
			return new Object[] {gId};
	}
	
	
	void setupRender(){
		group = groupDAO.getGroupById(gId);
		if(group==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Group ID", gId));
		}
		curProj = group.getProject();
		
		//is not leader?
		if(!canSelfEnroll(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		//get list of groupUser
		groupUsers = group.getGroupUsers();
		
		//check already enrolled
		GroupUser gu = getGroupUser(group, getCurUser());
		if(gu!=null)
			alreadyEnrolled = true;
		
	}
	
	public int getNo(){
		return (grid.getCurrentPage()-1)*grid.getRowsPerPage()+ rowIndex + 1;
	}
	public BeanModel<GroupUser> getModel(){
		BeanModel<GroupUser> model = beanModelSource.createEditModel(GroupUser.class, messages);
        model.include();
        model.add("No", null);
        model.add("Group", null);
        model.get("Group").label("Group No./Name");
        model.add("Members", null);
        model.add("Action", null);
        model.get("Action").label("Status/Action");
		
        
        model.reorder("No");
		
        return model;
	}
	public boolean hasReachedMaxPerGroup(GroupUser gu){
		if(group.getMaxPerGroup()==0)
			return false;
		if(group.getMaxPerGroup()>gu.getUsers().size())
			return false;
		
		return true;
	}
	@CommitAfter
	public void onActionFromJoin(long guId){
		//if already join,
		if(alreadyEnrolled){
			appState.recordErrorMsg("You already enrolled in a group. If you want to change group, please ask your intructor.");
			return;
		}
		GroupUser gu = groupDAO.getGroupUserById(guId);
		if(gu==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Group User ID", guId));
		group = gu.getGroup();
		curProj = group.getProject();
		
		//if the group is full
		if(group.getMaxPerGroup()!=0 &&  gu.getUsers().size() >= group.getMaxPerGroup()){
			appState.recordErrorMsg("Sorry, the group has reached maximum of "+ group.getMaxPerGroup()+" students, please choose another group.");
			return;
		}
		
		gu.addUser(getCurUser());
		groupDAO.saveGroupUser(gu);
	}
}
