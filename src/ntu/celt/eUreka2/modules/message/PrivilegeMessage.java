package ntu.celt.eUreka2.modules.message;

import ntu.celt.eUreka2.data.PrivilegeConstant;

public class PrivilegeMessage extends PrivilegeConstant{
	public static final String CREATE_MESSAGE = "msg01";
	public static final String REPLY_MESSAGE = "msg01a";
	//public static final String DELETE_MESSAGE = "msg02";
	public static final String VIEW_MESSAGE = "msg03";
	
	//EDIT and DELETE are always allow, because the module only lists Messages OWN by the logged in user
	
}
