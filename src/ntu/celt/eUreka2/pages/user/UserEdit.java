package ntu.celt.eUreka2.pages.user;


import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class UserEdit extends AbstractPageUser {
	@Property
	private User user ;
    
    @Inject
    private UserDAO userDAO;
    @Inject
	private BeanModelSource beanModelSource;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	
    @SessionState
	private AppState appState;
    
    void setupRender(){
    	user = getCurUser();
    }
    void onPrepareForSubmitFromUserForm(){
    	user = getCurUser();
    }
    
    
    @CommitAfter
    Object onSuccessFromUserForm(){
    	userDAO.save(user);
    	
    	
    	appState.recordInfoMsg(messages.get("successful-saved"));
    	
    	return linkSource.createPageRenderLinkWithContext(ViewYourInfo.class);
    }
    
    public BeanModel<User> getModel(){
    	BeanModel<User> model = beanModelSource.createEditModel(User.class, messages);
    	
    	model.include("username","firstName","lastName","organization","jobTitle","phone","mphone","email");
    	
    	
    	return model;
    }
    
}
