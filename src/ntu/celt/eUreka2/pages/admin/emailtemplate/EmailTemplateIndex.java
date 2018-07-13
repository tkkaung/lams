package ntu.celt.eUreka2.pages.admin.emailtemplate;

import java.util.List;

import ntu.celt.eUreka2.services.email.EmailTemplate;
import ntu.celt.eUreka2.services.email.EmailTemplateDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.exception.ConstraintViolationException;

public class EmailTemplateIndex {
	@Property
	private EmailTemplate _emailTem;
	@Property
	private List<EmailTemplate> _emailTems;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String _searchText;
	
	
	@Inject
	private EmailTemplateDAO _emailTemDAO;
	@Inject
	private Messages _messages;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@SessionState
	private AppState appState;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	@InjectComponent(value="emailTemplateGrid")
	private Grid grid;

	void setupRender() {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_EMAIL_TEMPLATE)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		if(_searchText==null || _searchText.isEmpty())
			_emailTems = _emailTemDAO.getAllEmailTemplates();
		else
			_emailTems = _emailTemDAO.searchEmailTemplates(_searchText);
	}
	
	void onSuccessFromSearchForm(){
		_emailTems = _emailTemDAO.searchEmailTemplates(_searchText);
	}
	
	@CommitAfter
	void onActionFromDelete(long id) {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_EMAIL_TEMPLATE)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_emailTem = _emailTemDAO.getEmailTemplateById(id);
		try{
			_emailTemDAO.delete(_emailTem);
			appState.recordInfoMsg(_messages.format("successfully-delete-x", _emailTem.getType()));
		}
		catch(ConstraintViolationException e){
			appState.recordErrorMsg(_messages.format("cant-delete-x-used-by-other", _emailTem.getType()));
		}
	}
	
	@Property
	private int rowIndex;
	public int getNo(){
		return (grid.getCurrentPage()-1)*grid.getRowsPerPage()+ rowIndex + 1;
	}
	public int getTotalSize() {
		if (_emailTems == null)
			return 0;
		return _emailTems.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	//TODO: may be allow the users to send preview email to themselves
}
