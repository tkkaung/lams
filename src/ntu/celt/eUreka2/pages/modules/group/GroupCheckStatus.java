package ntu.celt.eUreka2.pages.modules.group;

import java.lang.annotation.Annotation;
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
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;

public class GroupCheckStatus  extends AbstractPageGroup{
	@Property
	private Project curProj;
	@Property
	private long gid;
	@Property
	private Group group;
	@SuppressWarnings("unused")
	@Property
	private List<User> members;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@Property
	private int rowIndex;
	@Property
	private GroupUser groupUser;
	
	@SessionState
	private AppState appState;
	
	@Inject
	private GroupDAO gDAO;
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;
	@Inject
	private PropertyConduitSource propertyConduitSource;
	
	@InjectComponent
	private Grid grid;
	
	void onActivate(long id) {
		gid = id;
		
		group = gDAO.getGroupById(gid);
		if(group==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Group ID", gid));
		curProj = group.getProject();
		members = getAllAssessees(curProj, group);
	}
	long onPassivate() {
		return gid;
	}
	
	
	void setupRender() {
		if (!canManageGroup(curProj)) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
	}
	
	public BeanModel<User> getModel() {
		BeanModel<User> model = beanModelSource.createEditModel(User.class, messages);
		model.include();
		model.add("no", null);
		model.add("name", propertyConduitSource.create(User.class, "displayName"));
		model.add("group", createGroupPropertyConduit()).sortable(true);
		model.get("group").label("Group No./Name");
		model.add("action",null);
		
		return model;
	}
	
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public int getRowNum(){
		return (grid.getCurrentPage()-1)*grid.getRowsPerPage()+ rowIndex + 1;
	}
	public String loadGroupUser(Group group, User user){
		groupUser = getGroupUser(group, user);
		return "";
	}
	
	private PropertyConduit createGroupPropertyConduit() {
		PropertyConduit pcList = new PropertyConduit() {

			@Override
			public Object get(Object arg0) {
				User myRow = (User) arg0;
				GroupUser groupUser = getGroupUser(group, myRow);
				if(groupUser==null)
					return "";
				String groupName = groupUser.getGroupNumNameDisplay();
				return groupName;
			}

			@Override
			public Class getPropertyType() {
				return String.class;
			}

			@Override
			public void set(Object arg0, Object arg1) {
				
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> arg0) {
				return null;
			}
			
		};
		return pcList;
	}

	
}
