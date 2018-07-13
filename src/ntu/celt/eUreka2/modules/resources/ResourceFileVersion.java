package ntu.celt.eUreka2.modules.resources;

import java.util.Date;

import org.apache.tapestry5.beaneditor.NonVisual;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;

public class ResourceFileVersion implements Cloneable{
	@NonVisual
	private String id;
	private String name; 
	private String cmmt;
	private User owner;
	private Date cdate;
	private ResourceFile rfile;
	private int version;
	private String contentType;
	private long size;
	private String path;
	private int numDownload = 0;
	
	public ResourceFileVersion clone(){
		ResourceFileVersion r = new ResourceFileVersion();
		r.setId(id);
		r.setName(name);
		r.setCmmt(cmmt);
		r.setOwner(owner);
		r.setCdate(cdate);
		r.setRfile(rfile);
		r.setVersion(version);
		r.setContentType(contentType);
		r.setSize(size);
		r.setPath(path);
		r.setNumDownload(numDownload);
		return r;
	}
	
	public ResourceFileVersion() {
		super();
		id = Util.generateUUID();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	public void setCmmt(String cmmt) {
		this.cmmt = cmmt;
	}
	public String getCmmt() {
		return cmmt;
	}
	public User getOwner() {
		return owner;
	}
	public void setOwner(User owner) {
		this.owner = owner;
	}
	public Date getCdate() {
		return cdate;
	}
	public void setCdate(Date cdate) {
		this.cdate = cdate;
	}
	public ResourceFile getRfile() {
		return rfile;
	}
	public void setRfile(ResourceFile file) {
		this.rfile = file;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getVersionDisplay() {
		return "v "+version+".0";
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getDisplaySize(){
		return Util.formatFileSize(size);
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getCdateDisplay(){
		return Util.formatDateTime(cdate);
	}
	public void setNumDownload(int numDownload) {
		this.numDownload = numDownload;
	}
	public int getNumDownload() {
		return numDownload;
	}
	
	
	
}
