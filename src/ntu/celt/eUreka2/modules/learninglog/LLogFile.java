package ntu.celt.eUreka2.modules.learninglog;

import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.services.attachFiles.AttachedFile;


public class LLogFile extends AttachedFile  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 873934986604338700L;
	private LogEntry logEntry;

	public void setLogEntry(LogEntry logEntry) {
		this.logEntry = logEntry;
	}
	public LogEntry getLogEntry() {
		return logEntry;
	}
	
	public JSONObject toJSONObject(){
		JSONObject j = super.toJSONObject();
		j.put("logEntry", logEntry.getId());
		return j;
	}
	
	public LLogFile clone(){
		LLogFile at = (LLogFile) super.clone(LLogFile.class);
		at.setLogEntry(logEntry);
		return at;
	}
	
	
}
