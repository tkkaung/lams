package ntu.celt.eUreka2.pages.modules.group;


import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupDAO;
import ntu.celt.eUreka2.modules.group.GroupUser;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class GroupUserView  extends AbstractPageGroup{
	@Property
	private Project curProj;
	@Property
	private GroupUser groupUser;
	@Property
	private Group group;
	@Property
	private long guId;
	@SuppressWarnings("unused")
	@Property
	private User user;
	
	
	@Inject
	private GroupDAO groupDAO;
	@Inject
	private Messages messages;
	
	
	void onActivate(EventContext ec) {
		if(ec.getCount()==1){
			guId = ec.get(Long.class, 0);
		}
		
	}
	Object[] onPassivate() {
			return new Object[] {guId};
	}
	
	
	void setupRender(){
		groupUser = groupDAO.getGroupUserById(guId);
		if(groupUser==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "GroupUserId", guId));
		group = groupUser.getGroup();
		curProj = group.getProject();
		
		if(!canManageGroup(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
	}

	
}
