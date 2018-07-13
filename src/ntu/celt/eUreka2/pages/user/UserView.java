package ntu.celt.eUreka2.pages.user;

import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.entities.User;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class UserView extends AbstractPageUser {
	@Property
	private User user;
	private int userId;
	
	@Inject
	private Messages messages;

	@Inject
	private UserDAO userDAO;

	void onActivate(int id) {
		userId = id;
	}

	int onPassivate() {
		return userId;
	}

	void setupRender() {
		user = userDAO.getUserById(userId);
		if(user==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "UserID", userId));
		}
		// check privilege
	}

}
