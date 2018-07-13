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
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.PropertyConduitSource;

public class GroupSelfSelectSet  extends AbstractPageGroup{
	@Property
	private String projId;
	@Property
	private Project curProj;
	@Property
	private List<Group> groups;
	@Property
	private Group group;
	@Property
	private GroupUser groupUser;
	
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
    	
	
	Object onActivate(EventContext ec) {
		projId = ec.get(String.class, 0);
		
		curProj = projDAO.getProjectById(projId);
		if(curProj==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Project ID", projId));
		
		//is not leader?
		if(!canSelfEnroll(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		//get list of available group set (allow self enroll)
		groups = getAllowSelfEnrollGroups(curProj);
		
		//if no group set -> display message
		if(groups ==null || groups.size()==0){
			throw new RecordNotFoundException("There is no group that you can enroll");
		}
		//if only 1 group set -> redirect to SelectGroupNum
		if(groups.size()==1){
			return linkSource.createPageRenderLinkWithContext(GroupSelfSelectGroupNo.class, groups.get(0).getId());
		}
		return null;
	}
	Object[] onPassivate() {
			return new Object[] {projId};
	}
	
	
	void setupRender(){
		
	}

	private List<Group> getAllowSelfEnrollGroups(Project proj){
		List<Group> gList = groupDAO.getGroupsByProject(proj);
		for(int i=gList.size()-1; i>=0; i--){
			Group grp = gList.get(i);
			if(!grp.getAllowSelfEnroll()){
				gList.remove(i);
			}
		}
		return gList;
	}
	
	public String loadGroupUser(Group group, User user){
		groupUser = getGroupUser(group, user);
		return "";
	}
	
	public int getNo(){
		return (grid.getCurrentPage()-1)*grid.getRowsPerPage()+ rowIndex + 1;
	}
	public BeanModel<Group> getModel(){
		BeanModel<Group> model = beanModelSource.createEditModel(Group.class, messages);
        model.include("groupType");
        model.get("groupType").label("Group Set Name");
        model.add("No", null);
        model.add("Group", null);
        model.get("Group").label("Group No./Name");
        model.add("Action", null);
		model.get("Action").label("Status/Action");
		
        
        model.reorder("No", "groupType");
		
        return model;
	}
}
