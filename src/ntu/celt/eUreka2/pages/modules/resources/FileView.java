package ntu.celt.eUreka2.pages.modules.resources;

import java.io.File;
import java.util.Date;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.resources.ResourceDAO;
import ntu.celt.eUreka2.modules.resources.ResourceFile;
import ntu.celt.eUreka2.modules.resources.ResourceFileVersion;
import ntu.celt.eUreka2.modules.resources.ResourceFolder;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.ajax.MultiZoneUpdate;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.upload.services.UploadedFile;

public class FileView extends AbstractPageResource {
	@Property
	private ResourceFile rfile;
	@Property
	private Project curProj;
	@SuppressWarnings("unused")
	@Property
	private ResourceFileVersion rfversion;
	
	private String fid;
	@Property
	private UploadedFile upFile;
	@Property
	private String upCmmt;
	
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private ResourceDAO rDAO;
	@Inject
	private Messages messages;
	@Inject
    private BeanModelSource beanModelSource;
	
	
	void onActivate(String id) {
		fid = id;
	}
	String onPassivate() {
		return fid;
	}
	void setupRender(){
		rfile = rDAO.getFileById(fid);
		if(rfile==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ResourceFileID", fid));
		curProj = rfile.getProj();
		
		if(!canAccessResource(rfile))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		
	}
	void onPrepareForSubmitFromForm(){
		rfile = rDAO.getFileById(fid);
		curProj = rfile.getProj();
	}
	
	@SuppressWarnings("unused")
	@Property
	private String errMsg;
	
	@InjectComponent
	private Zone fileLockedZone;
	//@InjectComponent
	//private Zone uploadFileZone;
	@CommitAfter
	Object onLockFile(){
		rfile = rDAO.getFileById(fid);
		curProj = rfile.getProj();
		
		if(!rfile.isLocked()){
			rfile.setLockedBy(getCurUser());
		}else if(!getCurUser().equals(rfile.getLockedBy())){
			errMsg = messages.format("file-locked-by-x", rfile.getLockedBy().getUsername());
		}
		return new MultiZoneUpdate(fileLockedZone);  
					//.add(uploadFileZone) //if do this, the FORM id will change, so we can't javascript $('form').submit 
	}
	
	@CommitAfter
	Object onUnLockFile(){
		rfile = rDAO.getFileById(fid);
		curProj = rfile.getProj();
		if(canUnlock(rfile))
			rfile.setLockedBy(null);
		return fileLockedZone.getBody();
	}
	
	@Inject
	private ComponentResources componentResources;
	public String getAjaxLockFileURL(){
		Link link = componentResources.createEventLink("ajaxLockFile");
		return link.toURI();
	}
	
	Object onAjaxLockFile(){
		//try to lock the file
		rfile = rDAO.getFileById(fid);
		curProj = rfile.getProj();
		if(!rfile.isLocked())
			rfile.setLockedBy(getCurUser());
		else{
			if(!getCurUser().equals(rfile.getLockedBy())){
				errMsg = messages.format("file-locked-by-x", rfile.getLockedBy().getUsername());
				//throw new NotAuthorizedAccessException(messages.format("file-locked-by-x", rfile.getLockedBy().getUsername()));
			}
		}
		return fileLockedZone.getBody();
	}
	@Component
	private Form form;
	void onValidateFormFromForm(){
		if(!canEditResource(rfile)){
			form.recordError(messages.format("not-authorized-to-edit-x", rfile.getName()));
		}
		if(rfile.isLocked() && !rfile.getLockedBy().equals(getCurUser())){
			form.recordError(messages.format("file-locked-by-x", rfile.getLockedBy().getUsername()));
		}
		
	}
	@CommitAfter
	void onSuccessFromForm(){
		rfile.setMdate(new Date());
		rfile.setEditor(getCurUser());
		
		//note, when change these, also see uploadFileServlet.java
		String rootDir = Config.getString(Config.VIRTUAL_DRIVE) + "/" + curProj.getId() + "/"+PredefinedNames.MODULE_RESOURCE;
		File rootFolder = new File(rootDir);
		if (!rootFolder.exists() || !rootFolder.isDirectory()) {
			rootFolder.mkdirs();
		}
		File dirFile = new File(rootFolder + "/" + fid);
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			dirFile.mkdirs();
		}
		String fileName = upFile.getFileName();
		Date now = new Date();
		String prefix = Util.formatDateTime(now, "yyyyMMdd-HHmmss")+"_"+getCurUser().getId();
		File theFile = new File (dirFile + "/" + prefix +"_"+ fileName.replace(" ", "_"));
		
		upFile.write(theFile);
		String path = theFile.getAbsolutePath().replace(Config.getString(Config.VIRTUAL_DRIVE), ""); 
		
		
		ResourceFileVersion rfv = new ResourceFileVersion();
		rfv.setPath(path);
		rfv.setVersion(rfile.getLatestVersion()+1);
		rfv.setCmmt(Util.textarea2html(upCmmt));
		rfv.setContentType(Util.additionContentTypeCheck(upFile.getContentType(), fileName));
		rfv.setSize(upFile.getSize());
		rfv.setCdate(new Date());
		rfv.setName(fileName);
		//rfv.setNumDownload(numDownload);
		rfv.setOwner(getCurUser());
		//rfv.setRfile(rs);
		
		rfile.addFileVersion(rfv);
		
		rfile.setLockedBy(null); //unlock the file
		
		rDAO.updateResource(rfile);
		
	}
	
	
	
	
	
	
	
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public void setRowsPerPage(int num){
		appState.setRowsPerPage(num);
	}
	
	@Cached
	public String getShouldDisableInput(){
		if(!canEditResource(rfile))
			return "disabled";
		if(rfile.isLocked() && !getCurUser().equals(rfile.getLockedBy()))
			return "disabled";
		return null;
	}
	
	public String getBreadcrumbLink(){
		String str ="";
		ResourceFolder folder = rfile.getParent();
		while(folder!=null){
			str = ","+Util.encode(Util.truncateString(folder.getName(),50))+"=modules/resources/home?"+folder.getProj().getId()+":"+folder.getId() + str;
			folder = folder.getParent();
		}
		return str;
	}
	public BeanModel<ResourceFileVersion> getModel() {
		BeanModel<ResourceFileVersion> model = beanModelSource.createEditModel(ResourceFileVersion.class, messages);
        model.include("contentType","version","name","cmmt","size","numDownload");
        model.get("contentType").label(messages.get("empty-label"));
        model.add("action", null);
     	
        return model;
    }
	
}
