package ntu.celt.eUreka2.modules.backuprestore;

import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.modules.backuprestore.JSONable;
import ntu.celt.eUreka2.services.attachFiles.AttachedFile;


public class BackupAttachedFile extends AttachedFile implements JSONable {
	
	private static final long serialVersionUID = 6994661851322767627L;
	private BackupEntry backupEntry;
	
	public BackupEntry getBackupEntry() {
		return backupEntry;
	}
	public void setBackupEntry(BackupEntry backupEntry) {
		this.backupEntry = backupEntry;
	}

	public JSONObject toJSONObject(){
		JSONObject j = new JSONObject();
		j.put("backupEntry", backupEntry.getId());
		j.put("id", this.getId());
		j.put("fileName", this.getFileName());
		j.put("aliasName", this.getAliasName());
		j.put("path", this.getPath());
		j.put("contentType", this.getContentType());
		j.put("size", this.getSize());
		
		return j;
	}
}
