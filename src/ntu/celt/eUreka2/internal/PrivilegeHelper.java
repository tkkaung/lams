package ntu.celt.eUreka2.internal;

import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.User;

public class PrivilegeHelper {
	public static boolean hasPrivU(User user, String privId){
		if(user==null) 
			return false;
		return user.hasPrivilege(privId);
	}
	public static boolean hasPriv(ProjUser pu, String privId){
		if(pu==null) 
			return false;
		return pu.hasPrivilege(privId);
	}
	public static boolean or(boolean b1, boolean b2){
		return (b1 || b2);
	}
}
