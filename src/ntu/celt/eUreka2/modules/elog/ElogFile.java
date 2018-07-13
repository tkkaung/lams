package ntu.celt.eUreka2.modules.elog;

import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.services.attachFiles.AttachedFile;


public class ElogFile extends AttachedFile  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1813683239617619293L;
	private Elog elog;

	public void setElog(Elog elog) {
		this.elog = elog;
	}
	public Elog getElog() {
		return elog;
	}
	
	public JSONObject toJSONObject(){
		JSONObject j = super.toJSONObject();
		j.put("elog", elog.getId());
		
		return j;
	}
	public ElogFile clone(){
		ElogFile at = (ElogFile) super.clone(ElogFile.class);
		at.setElog(elog);
		
		return at;
	}
	
	
}
