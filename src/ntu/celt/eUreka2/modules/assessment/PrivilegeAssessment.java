package ntu.celt.eUreka2.modules.assessment;

import ntu.celt.eUreka2.data.PrivilegeConstant;

public class PrivilegeAssessment extends PrivilegeConstant{
	
	/*MANAGE_RUBRIC is defined in PrivilegeSystem, because it is not project specific */
	
	public static final String ACCESS_MODULE = "asm01";
	public static final String CREATE_ASSESSMENT = "asm02";
	public static final String EDIT_ASSESSMENT = "asm03"; //apply to all within project
	public static final String DELETE_ASSESSMENT = "asm04"; //apply to all within project
	public static final String VIEW_ASSESSMENT = "asm05";  //apply to all within project
	public static final String VIEW_ASSESSMENT_GRADES = "asm06";  //apply to all within project
	public static final String GRADE_ASSESSMENT = "asm07";  //apply to all within project
	public static final String IS_ASSESSEE = "asm08";  
	
}
