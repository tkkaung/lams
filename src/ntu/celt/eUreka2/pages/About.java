package ntu.celt.eUreka2.pages;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;


public class About
{
	@Inject
    @Symbol(SymbolConstants.APPLICATION_VERSION)
    private String buildNumber ; 
    
	public String getBuildNumber(){
    	return buildNumber;
    }
}
