package ntu.celt.eUreka2.modules.announcement;

import ntu.celt.eUreka2.data.PrivilegeConstant;

public class PrivilegeAnnouncement extends PrivilegeConstant {
	public static final String CREATE_ANNOUNCEMENT = "anm01";
	public static final String UPDATE_ANNOUNCEMENT = "anm02";
	public static final String DELETE_ANNOUNCEMENT = "anm03";
	public static final String VIEW_ANNOUNCEMENT = "anm04";
	public static final String ADMIN_MANAGE = "anm05";
	public  String getCreateAnnouncement() {
		return CREATE_ANNOUNCEMENT;
	}
	public  String getUpdateAnnouncement() {
		return UPDATE_ANNOUNCEMENT;
	}
	public  String getDeleteAnnouncement() {
		return DELETE_ANNOUNCEMENT;
	}
	public String getViewAnnouncement() {
		return VIEW_ANNOUNCEMENT;
	}
	public  String getAdminManage() {
		return ADMIN_MANAGE;
	}
	
}
