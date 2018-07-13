package ntu.celt.eUreka2.services.attachFiles;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.MappedSuperclass;

import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;


@MappedSuperclass
public class AttachedFile implements JSONable, Serializable, Cloneable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5301972411357135538L;
	protected String id ;
	protected String fileName;
	protected String aliasName;
	protected String path;  //partial
	protected String contentType;
	protected Long size;
	protected User creator;
	protected Date uploadTime;
	
	public JSONObject toJSONObject(){
		JSONObject j = new JSONObject();
		j.put("id", this.getId());
		j.put("fileName", this.getFileName());
		j.put("aliasName", this.getAliasName());
		j.put("path", this.getPath());
		j.put("contentType", this.getContentType());
		j.put("size", this.getSize());
		j.put("creator", creator==null? "": creator.getUsername());
		j.put("uploadTime", this.getUploadTime());
		
		
		return j;
	}
	public AttachedFile clone(){
		return clone(this.getClass());
	}
	@SuppressWarnings("unchecked")
	public AttachedFile clone(Class subclazz){
		AttachedFile at;
		try {
			at = (AttachedFile) subclazz.newInstance();
		
			at.setId(id);
			//at.setId(Util.generateUUID());
			at.setFileName(fileName);
			at.setAliasName(aliasName);
			at.setPath(path);
			at.setContentType(contentType);
			at.setSize(size);
			at.setCreator(creator);
			return at;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		};
		return null;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");;
	}
	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}
	public String getAliasName() {
		return aliasName;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getContentType() {
		return contentType;
	}
	public void setSize(Long size) {
		this.size = size;
	}
	public Long getSize() {
		return size;
	}
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	public Date getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(Date uploadTime) {
		this.uploadTime = uploadTime;
	}
	public String getUploadTimeDisplay(){
		return Util.formatDateTime(uploadTime, Config.getString(Config.FORMAT_TIME_PATTERN));
	}
	
	public String getDisplaySize(){
		return Util.formatFileSize(size);
	}
	public String getDisplayName(){
		if(aliasName==null || aliasName.isEmpty())
			return fileName;
		else
			return aliasName;
	}
	
	// The need for a equal() method is discussed at http://www.hibernate.org/109.html
    @Override
    public boolean equals(Object obj) {
    	return (obj == this) || (obj instanceof AttachedFile) && id != null 
    			&& id.equals(((AttachedFile) obj).getId());
    	
    }
    // The need for a hashCode() method is discussed at http://www.hibernate.org/109.html
    @Override
	public int hashCode() {
		return id == null ? super.hashCode() : id.hashCode();
	}
}
