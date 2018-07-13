package ntu.celt.eUreka2.pages;

import java.sql.BatchUpdateException;

import ntu.celt.eUreka2.annotation.PublicPage;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ExceptionReporter;
import org.slf4j.Logger;

/**
 * This page is used to customize display Exception, 
 * see also: ntu.celt.eUreka2.service.AppModule decorateRequestExceptionHandler()
 *  
 * @author Somsak
 *
 */
@PublicPage
public class Exception implements ExceptionReporter
{
	@Property
    private Throwable exception;
    @SuppressWarnings("unused")
	@Property
    private String pageTitle ;
    @Property
    private String exceptionMessage ;
    
    @Inject
    private Messages messages;
    @Inject
    private Logger logger;
    
    @Override
    public void reportException(Throwable ex)
    {
        this.exception = ex;
        pageTitle = messages.get("exception");
        
        int i=0;
        while (ex!=null && i<10){
        	if(ex instanceof RecordNotFoundException){
	        	pageTitle = messages.get("record-not-found");
	        	exceptionMessage = ex.getMessage();//messages.get("record-not-found-text");
	        	return;
	        	
	        }
	        else if(ex instanceof NotAuthorizedAccessException){
	        	pageTitle = messages.get("not-authorized-access");
	        	exceptionMessage = ex.getMessage();
	        	return;
	        }
	        else if(ex instanceof BatchUpdateException){
	        	if(ex.getMessage().contains("Data too long")){
	        		exceptionMessage = messages.get("not-save-to-db-bcos-data-too-long");
	        		return;
	        	}
	        }
	        else if(ex instanceof NullPointerException){
	        	if(ex.getMessage()!=null){
	        		exceptionMessage = ex.getMessage();
	        	}
	        	else{
        		exceptionMessage = "Unexpected NullPointerException in ... "
        			+ex.getStackTrace()[0].getClassName()+ " ... Line " + ex.getStackTrace()[0].getLineNumber();
	        	}
        		logger.error("...."+exceptionMessage);
        		return;
	        	
	        }
        	ex = ex.getCause();
        	i++;
        }
        exceptionMessage = exception.getMessage();
        logger.error("...."+exceptionMessage);
    }
    
   


}