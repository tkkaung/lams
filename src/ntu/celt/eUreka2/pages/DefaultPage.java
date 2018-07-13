package ntu.celt.eUreka2.pages;

import java.util.Date;

import ntu.celt.eUreka2.annotation.PublicPage;


@PublicPage
public class DefaultPage
{
	public Date getCurrentTime() 
	{ 
		return new Date(); 
	}
	
	
	void setupRender(){
		
	
	}
}
