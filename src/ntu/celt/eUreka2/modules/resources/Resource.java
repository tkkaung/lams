package ntu.celt.eUreka2.modules.resources;

import java.util.Date;

import org.apache.tapestry5.beaneditor.NonVisual;

import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;

public class Resource implements Cloneable{
	@NonVisual
	private String id;
	private String name;
	private String des;
	private User owner;
	private User editor;
	private Date cdate;
	private Date mdate;
	private Project proj;
	private ResourceFolder parent;  //NULL for root folder
	private ResourceType type;
	private Boolean shared = true; //default to shared
	
	public Resource clone(){
		return clone(this.getClass());
	}
	@SuppressWarnings("unchecked")
	public Resource clone(Class subclazz){
		Resource r;
		try {
			r = (Resource) subclazz.newInstance();
		//	r.setId(id);
			r.setName(name);
			r.setDes(des);
			r.setOwner(owner);
			r.setEditor(editor);
			r.setCdate(cdate);
			r.setMdate(mdate);
			r.setProj(proj);
			r.setParent(parent);
			r.setType(type);
			r.setShared(shared);
			
			return r;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Resource() {
		super();
		id = Util.generateUUID();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDes(String des) {
		this.des = des;
	}
	public String getDes() {
		return des;
	}
	public User getOwner() {
		return owner;
	}
	public void setOwner(User owner) {
		this.owner = owner;
	}
	public void setEditor(User editor) {
		this.editor = editor;
	}
	public User getEditor() {
		return editor;
	}
	public Date getCdate() {
		return cdate;
	}
	public void setCdate(Date cdate) {
		this.cdate = cdate;
	}
	public void setMdate(Date mdate) {
		this.mdate = mdate;
	}
	public Date getMdate() {
		return mdate;
	}
	
	public String getCdateDisplay(){
		return Util.formatDateTime(cdate);
	}
	public String getMdateDisplay(){
		return Util.formatDateTime(mdate);
	}
	public void setProj(Project proj) {
		this.proj = proj;
	}
	public Project getProj() {
		return proj;
	}
	public ResourceFolder getParent() {
		return parent;
	}
	public void setParent(ResourceFolder parent) {
		this.parent = parent;
	}
	public boolean isFolder(){
		if(this instanceof ResourceFolder)
			return true;
		return false;
	}
	public boolean isFile(){
		if(this instanceof ResourceFile)
			return true;
		return false;
	}
	public boolean isLink(){
		if(this instanceof ResourceLink)
			return true;
		return false;
	}
	public ResourceFolder toFolder(){
		if(isFolder())
			return (ResourceFolder) this;
		return null;
	}
	public ResourceFile toFile(){
		if(isFile())
			return (ResourceFile) this;
		return null;
	}
	public ResourceLink toLink(){
		if(isLink())
			return (ResourceLink) this;
		return null;
	}
	public void setType(ResourceType type) {
		this.type = type;
	}
	public ResourceType getType() {
		return type;
	}
	public void setShared(Boolean shared) {
		if(shared==null)
			this.shared = true;
		else
			this.shared = shared;
	}
	public boolean isShared() {
		return shared;
	}
	
	
	
}
