package ntu.celt.eUreka2.pages.modules.resources;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.io.FilenameUtils;
import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.ImageResizer;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.resources.PrivilegeResource;
import ntu.celt.eUreka2.modules.resources.Resource;
import ntu.celt.eUreka2.modules.resources.ResourceDAO;
import ntu.celt.eUreka2.modules.resources.ResourceFile;
import ntu.celt.eUreka2.modules.resources.ResourceFileVersion;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileNotFoundException;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileStreamResponse;

public abstract class AbstractPageResource {
	public String getModuleName(){
		return PredefinedNames.MODULE_RESOURCE;
	}
	public String getModuleDisplayName(Project curProj){
		if(PredefinedNames.PROJTYPE_SENATE.equalsIgnoreCase(curProj.getType().getName())){
			return messages.get("senate-module-resources-name");
		}
		return getModule().getDisplayName();
	}
	@Inject
	private ModuleDAO moduleDAO;
	@Inject
	private ResourceDAO rDAO;
	@Inject
	private Logger logger;
	@Inject
    private Messages messages;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Inject
	private SysRoleDAO sysRoleDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	public Module getModule(){
		return moduleDAO.getModuleByName(PredefinedNames.MODULE_RESOURCE);
	}
	public Object[] getParams(Object p, Object f) {
		return new Object[] {p, f}; 
	}
	public boolean isMoreThan(int a, int b){
		return (a > b) ;
	}
	public String truncateStr(String str){
		return Util.truncateString(str, 30);
	}
	public String textarea2html(String str){
		return Util.textarea2html(str); 
	}
	public StreamResponse onRetrieveFile(String rfvId) {
		ResourceFileVersion rfv = rDAO.getResourceFileVersionById(rfvId);
		if(rfv==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "FileVersionID", rfvId));
		if(!canAccessResource(rfv.getRfile())){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
			
		
		String path = null;
		try {
			path = Config.getString(Config.VIRTUAL_DRIVE) + rfv.getPath();
			File f = new File(path);
			InputStream is = new BufferedInputStream(new FileInputStream(f));
			//String returnName = Util.appendToFileName(r.getName(), "_"+r.getVersionDisplay()+"_"+Util.formatDateTime(r.getCdate(), "yyMMdd"));
			String returnName = rfv.getRfile().getName();
			
			updateNumDownload(rfv);
			return new AttachedFileStreamResponse(is, returnName, rfv.getContentType(), f.length());
		} catch (FileNotFoundException e) {
			logger.error("FileNotFound: path=" + path);
			throw new AttachedFileNotFoundException(messages.format("attachment-x-not-found", rfv.getRfile().getName()));
		}
	}
	
	
	final private String THUMB_SUFFIX = "_th";
	final private int THUMB_WIDTH_HEIGHT = Config.getInt("max_thumb_width_height");
	
	public StreamResponse onRetrieveFileThumb(String rfvId) {
		ResourceFileVersion rfv = rDAO.getResourceFileVersionById(rfvId);
		if(rfv==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "FileVersionID", rfvId));
		if(!canAccessResource(rfv.getRfile())){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		String path = null;
		try {
			path = Config.getString(Config.VIRTUAL_DRIVE) + Util.appendToFileName(rfv.getPath(), THUMB_SUFFIX);
			File f = new File(path);
			/*generate one, if not exist*/
			if(!f.exists()){
				File imgFile = new File(Config.getString(Config.VIRTUAL_DRIVE) + rfv.getPath() );
				if(imgFile.exists()){
					boolean success = ImageResizer.generateThumbnail(imgFile, f, THUMB_WIDTH_HEIGHT, false);
					if(success && f.exists()){
						InputStream is = new BufferedInputStream(new FileInputStream(f));
						String returnName = Util.appendToFileName(rfv.getRfile().getName(), THUMB_SUFFIX);
						
						return new AttachedFileStreamResponse(is, returnName, rfv.getContentType(), f.length());	
					}
					throw new RuntimeException("Fail to generate thumbnail file");
				}
			}
			InputStream is = new BufferedInputStream(new FileInputStream(f));
			String returnName = Util.appendToFileName(rfv.getRfile().getName(), THUMB_SUFFIX);
			
			return new AttachedFileStreamResponse(is, returnName, rfv.getContentType(), f.length());
			
		} catch (FileNotFoundException e) {
			logger.error("FileNotFound: path=" + path);
			throw new AttachedFileNotFoundException(messages.format("attachment-x-not-found", rfv.getRfile().getName()));
		}
	}
	
	@Inject
	private ComponentResources comResources;
	public String getRetrieveFileThumbLink(String rfvId){
		Link link = comResources.createEventLink("RetrieveFileThumb", rfvId); //event_name, to call onRetrieveFileThumb
		return link.toURI();
	}
	

	
	
	@CommitAfter
	private void updateNumDownload(ResourceFileVersion rfv){
		rfv.setNumDownload(rfv.getNumDownload()+1);
		rDAO.updateResourceFileVersion(rfv);
	}
	
	
	@Inject
	@Path("context:lib/img/24/folder.png")
	private Asset folder_png;
	@Inject
	@Path("context:lib/img/24/folder_shared.png")
	private Asset folder_shared_png;
	@Inject
	@Path("context:lib/img/24/folder_private.png")
	private Asset folder_private_png;
	@Inject
	@Path("context:lib/img/24/link.png")
	private Asset link_png;
	@Inject
	@Path("context:lib/img/24/file_unknown.png")
	private Asset file_png;
	@Inject
	@Path("context:lib/img/24/file_doc.png")
	private Asset file_doc_png;
	@Inject
	@Path("context:lib/img/24/file_xls.png")
	private Asset file_xls_png;
	@Inject
	@Path("context:lib/img/24/file_ppt.png")
	private Asset file_ppt_png;
	@Inject
	@Path("context:lib/img/24/file_pdf.png")
	private Asset file_pdf_png;
	@Inject
	@Path("context:lib/img/24/file_image.png")
	private Asset file_image_png;
	@Inject
	@Path("context:lib/img/24/file_sound.png")
	private Asset file_sound_png;
	@Inject
	@Path("context:lib/img/24/file_video.png")
	private Asset file_video_png;
	@Inject
	@Path("context:lib/img/24/file_zip.png")
	private Asset file_zip_png;
	@Inject
	@Path("context:lib/img/24/file_txt.png")
	private Asset file_txt_png;
	
	
	public Asset getTypeIcon(Resource r){
		if(r.isFolder()){
			if(r.getParent()==null){
				if(r.isShared())
					return folder_shared_png;
				else
					return folder_private_png;
			}
				
			return folder_png;
		}
		if(r.isLink())
			return link_png;
		if(r.isFile()){ 
			return getRFVTypeIcon( r.toFile().getLatestFileVersion());
		}
		return null;
	}
	public Asset getIcon(String contentType, String fileName){
		if(contentType == null || fileName== null)
			return file_png;
		
		if(contentType.endsWith("msword") 
				|| FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"doc","docx"}))
					return file_doc_png;
			if(contentType.endsWith("excel") 
				|| FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"xls","xlsx"}))
					return file_xls_png;
			if(contentType.endsWith("powerpoint") 
				|| FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"ppt","pptx"}))
					return file_ppt_png;
			if(contentType.endsWith("pdf")
				|| FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"pdf"}))
					return file_pdf_png;
			if(contentType.startsWith("image"))
				return file_image_png;
			if(contentType.startsWith("audio")
				|| FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"mp3","wma","wav"}))
				return file_sound_png;
			if(contentType.startsWith("video")
				|| FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"flv","wmv","avi","mov","mp4"}))
				return file_video_png;
			if(contentType.startsWith("text")
				|| FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"txt","xml","html","htm"}))
				return file_txt_png;
			if(FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"zip","rar","gz","gzip","7z"}))
				return file_zip_png;

		return file_png;
	}
	public Asset getRFVTypeIcon(ResourceFileVersion rfv){
		if(rfv!=null && rfv.getContentType()!=null){
			return getIcon(rfv.getContentType(), rfv.getName());
		}
		return file_png;
	}
	public boolean isImage(ResourceFileVersion rfv){
		if(rfv.getContentType().startsWith("image"))
			return true;
		return false;
	}
	
	
	
	protected List<Integer> getAccessibleSchlIDs(User u){
		List<Integer> idList = new ArrayList<Integer>();
		SysRole sysRole = sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_SCHOOL_ADMIN);
		for(SysroleUser srUser : u.getExtraRoles()){
			if(srUser.getSysRole().equals(sysRole)){
				idList.add(srUser.getParam());
			}
		}
		return idList;
	}
	public boolean canAdminAccess(Project proj){
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA))
			return true;
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SCHOOL_RUBRIC)){
			List<Integer> accessibleSchlIDs = getAccessibleSchlIDs(getCurUser());
			if(proj.getSchool() != null && accessibleSchlIDs.contains(proj.getSchool().getId()) )
				return true;
		}
		return false;
	}
	public boolean canViewResource(Project curProj){
		if(canAdminAccess(curProj))
			return true;
		if(curProj.isReference())
			return true;
		ProjUser pu = curProj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeResource.VIEW_RESOURCE ))
			return true;
		
		return false;
	}
	public boolean canAccessResource(Resource r){
		if(canAdminAccess(r.getProj()))
			return true;
		if(r.getOwner().equals(getCurUser()))
			return true;
		if(!r.isShared())
			return false;
		if(r.getProj().isReference())
			return true;
		ProjUser pu = r.getProj().getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeResource.VIEW_RESOURCE ))
			return true;
		
		return false;
	}
	
	public boolean canCreateFolder(Project curProj){
		if(canAdminAccess(curProj))
			return true;
		if(curProj.isReference())
			return false;
		ProjUser pu = curProj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeResource.CREATE_FOLDER )){
			return true;
		}
		return false;
	}
	
	public boolean canCreateLink(Project curProj){
		if(canAdminAccess(curProj))
			return true;
		if(curProj.isReference())
			return false;
		ProjUser pu = curProj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeResource.CREATE_LINK )){
			return true;
		}
		return false;
	}
	
	public boolean canCreateFile(Project curProj){
		if(canAdminAccess(curProj))
			return true;
		if(curProj.isReference())
			return false;
		ProjUser pu = curProj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeResource.CREATE_FILE )){
			return true;
		}
		return false;
	}
	public boolean canEditResource(Resource r){
		if(r==null)
			return false;
		if(r.getParent()==null)
			return false;
		if(canAdminAccess(r.getProj()))
			return true;
		if(r.getOwner().equals(getCurUser()))
			return true;
		if(!r.isShared())
			return false;
		ProjUser pu = r.getProj().getMember(getCurUser());
		if(pu ==null)
			return false;
		if(r.isFile() && pu.hasPrivilege(PrivilegeResource.UPDATE_FILE ))
			return true;
		if(r.isFolder() && pu.hasPrivilege(PrivilegeResource.UPDATE_FOLDER ))
			return true;
		if(r.isLink() && pu.hasPrivilege(PrivilegeResource.UPDATE_LINK ))
			return true;

		return false;
	}
	
	public boolean canDeleteResource(Resource r){
		if(r==null)
			return false;
		if(canAdminAccess(r.getProj()))
			return true;
		if(r.getOwner().equals(getCurUser()))
			return true;
		if(!r.isShared())
			return false;
		ProjUser pu = r.getProj().getMember(getCurUser());
		if(pu == null)
			return false;
		if(r.isFile() && pu.hasPrivilege(PrivilegeResource.REMOVE_FILE ))
			return true;
		if(r.isFolder() && pu.hasPrivilege(PrivilegeResource.REMOVE_FOLDER ))
			return true;
		if(r.isLink() && pu.hasPrivilege(PrivilegeResource.REMOVE_LINK ))
			return true;

		return false;
	}
	public boolean canCopyResource(Resource r){
		if(r==null)
			return false;
		if(canAdminAccess(r.getProj()))
			return true;
		if(r.getOwner().equals(getCurUser()))
			return true;
		if(!r.isShared())
			return false;
		ProjUser pu = r.getProj().getMember(getCurUser());
		if(pu == null)
			return false;
		if(r.isFile() && pu.hasPrivilege(PrivilegeResource.CREATE_FILE ))
			return true;
		if(r.isFolder() && pu.hasPrivilege(PrivilegeResource.CREATE_FOLDER ))
			return true;
		if(r.isLink() && pu.hasPrivilege(PrivilegeResource.CREATE_LINK ))
			return true;

		return false;
	}
	public boolean canMoveResource(Resource r){
		if(r==null)
			return false;
		if(canAdminAccess(r.getProj()))
			return true;
		if(r.getOwner().equals(getCurUser()))
			return true;
		if(!r.isShared())
			return false;
		ProjUser pu = r.getProj().getMember(getCurUser());
		if(pu == null)
			return false;
		if(r.isFile() && pu.hasPrivilege(PrivilegeResource.REMOVE_FILE ))
			return true;
		if(r.isFolder() && pu.hasPrivilege(PrivilegeResource.REMOVE_FOLDER ))
			return true;
		if(r.isLink() && pu.hasPrivilege(PrivilegeResource.REMOVE_LINK ))
			return true;

		return false;
	}
	
	public boolean canLock(ResourceFile rfile){
		if(!rfile.isLocked() && canEditResource(rfile))
			return true;
		
		return false;
	}
	public boolean canUnlock(ResourceFile rfile){
		if(getCurUser().equals(rfile.getLockedBy()))
			return true;
		return false;
	}
}
