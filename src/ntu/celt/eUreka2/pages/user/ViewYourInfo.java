package ntu.celt.eUreka2.pages.user;

import ntu.celt.eUreka2.entities.User;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

public class ViewYourInfo extends AbstractPageUser {
	@SuppressWarnings("unused")
	@Property
	private User user ;
    
    @SuppressWarnings("unused")
	@Inject
    private Logger logger;
	
	
    void setupRender(){
    	user = getCurUser();
    	
    }
    
}
