package ntu.celt.eUreka2.modules.assessment;

import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.services.attachFiles.AttachedFile;


public class AssmtInstructorFile extends AttachedFile  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2291346762025505635L;
	private AssessmentUser assmtUser;

	
	public AssessmentUser getAssmtUser() {
		return assmtUser;
	}
	public void setAssmtUser(AssessmentUser assmtUser) {
		this.assmtUser = assmtUser;
	}
	public JSONObject toJSONObject(){
		JSONObject j = super.toJSONObject();
		j.put("assmtUser", assmtUser.getId());
		
		return j;
	}
	public AssmtInstructorFile clone(){
		AssmtInstructorFile at = (AssmtInstructorFile) super.clone(AssmtInstructorFile.class);
		at.setAssmtUser(assmtUser);
		
		return at;
	}
	
	
}
