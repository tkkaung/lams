package ntu.celt.eUreka2.modules.assessment;

import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.services.attachFiles.AttachedFile;


public class AssmtFile extends AttachedFile  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7291346762025505635L;
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
	public AssmtFile clone(){
		AssmtFile at = (AssmtFile) super.clone(AssmtFile.class);
		at.setAssmtUser(assmtUser);
		
		return at;
	}
	
	
}
