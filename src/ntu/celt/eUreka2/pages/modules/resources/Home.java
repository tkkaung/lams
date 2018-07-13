package ntu.celt.eUreka2.pages.modules.resources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ntu.celt.eUreka2.components.TreeNode;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.resources.Resource;
import ntu.celt.eUreka2.modules.resources.ResourceDAO;
import ntu.celt.eUreka2.modules.resources.ResourceFile;
import ntu.celt.eUreka2.modules.resources.ResourceFileVersion;
import ntu.celt.eUreka2.modules.resources.ResourceFolder;
import ntu.celt.eUreka2.modules.resources.ResourceLink;

import org.apache.commons.io.FilenameUtils;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;

@Import(library={"context:modules/resources/js/context-menu.js"
		,"context:modules/resources/js/drag-drop-folder-tree.js"
		,"Home.js"}
	,stylesheet={"context:modules/resources/css/drag-drop-folder-tree.css"
		,"context:modules/resources/css/context-menu.css"})
public class Home extends AbstractPageResource {
	@Inject
	private ResourceDAO rDAO;
	@Inject
	private ProjectDAO pDAO;
	@Inject
    private Request request;
	@Inject
	private Response response;
	@Inject
	private Logger logger;
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private Messages messages;
    @Inject
    private PropertyConduitSource propertyConduitSource; 

	@SessionState
	private AppState appState;
	
	@Property
	private String pid;
	@Property
	private String fid;
	@Property
	private Project curProj;
	@Property
	private ResourceFolder curFolder;
	
	@SuppressWarnings("unused")
	@Property
	private TreeNode curNode;
	@SuppressWarnings("unused")
	@Property
	private TreeNode node;
	@Property
	private List<TreeNode> treeNodes;
	
	
    @SuppressWarnings("unused")
	@Property
	private Resource resrc;
	@Property
	private List<Resource> resrcs;
	enum SubmitType {DELETE, MULTI_DOWNLOAD, COPY, MOVE};
	private SubmitType submitType ;
	
	
	void onActivate(EventContext ec) {
		if (ec.getCount() == 1) {
			pid = ec.get(String.class, 0);
		}
		else if (ec.getCount() == 2) {
			pid = ec.get(String.class, 0);
			fid = ec.get(String.class, 1);
		}
		
		
		curProj = pDAO.getProjectById(pid);
		if(curProj==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectID", pid));
		}
		if(fid!=null){
			curFolder = rDAO.getFolderById(fid);
			if(curFolder==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "ResourceID", fid));
			if(!canAccessResource(curFolder))
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		else{
			if(!canViewResource(curProj))
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
	}
	Object[] onPassivate() {
		if (fid == null) 
			return new Object[] {pid};
		return new Object[] {pid, fid};
	}
	
	public void setupRender() {
		
		List<ResourceFolder> sharedFolders = rDAO.getSharedRootFolders(curProj);
		if(sharedFolders.isEmpty()){
			sharedFolders.add(createDefaultSharedFolder());
		}
		
		List<ResourceFolder> privateFolders = rDAO.getPrivateRootFolders(getCurUser());
		if(privateFolders.isEmpty()){
			privateFolders.add(createDefaultPrivateFolder());
		}
		
		List<ResourceFolder> rootFolders = new ArrayList<ResourceFolder>();
		rootFolders.addAll(sharedFolders);
		rootFolders.addAll(privateFolders);
		
		
		treeNodes = new ArrayList<TreeNode>();
		int depth = 1;
		for (ResourceFolder fd : rootFolders) {
			String icon = "";
			if(fd.isShared())
				icon = "folder_shared.gif";
			else
				icon = "folder_private.gif";
			
			treeNodes.add(new TreeNode(fd.getId(), fd.getName(), fd.getDes(), depth, icon));
			initChildNode(fd, depth + 1); 
		}
		
		if(curFolder!=null){
			curNode = new TreeNode(curFolder.getId(), curFolder.getName(), curFolder.getDes(), getDepth(curFolder));
		}
		
		if(fid!=null){
			resrcs = rDAO.getResourcesByParent(curFolder);
		}
		else{
			resrcs = new ArrayList<Resource>();
			resrcs.addAll(rootFolders);
		}
		
		showApplet = false;
		
	}
	void onPrepare(){
		
	}
	
	private void initChildNode(ResourceFolder fd, int d) {
		List<ResourceFolder> folders = rDAO.getFoldersByParent(fd);
		for (ResourceFolder rs : folders) {
			treeNodes.add(new TreeNode(rs.getId(), rs.getName(), rs.getDes(), d));
			initChildNode(rs, d + 1); 
		}
	}
	
	public int getDepth(Resource r){
		int count = 0;
		Resource parent = r.getParent();
		while(parent != null){
			parent = parent.getParent();
			count++;
		}
		return count;
	}
	public String isCurNodeClass(TreeNode cur, TreeNode node){
		if(cur==null)
			return null;
		if(node.getIdentifier().equals(cur.getIdentifier()))
			return "selected";
		return null;
	}
	
	@CommitAfter
	private ResourceFolder createDefaultSharedFolder(){
		ResourceFolder fd = new ResourceFolder();
		fd.setDes(messages.get("default-shared-folder-des"));
		fd.setCdate(new Date());
		fd.setMdate(new Date());
		fd.setName(messages.get("default-shared-folder-name"));
		fd.setOwner(getCurUser());
		fd.setProj(curProj);
		fd.setShared(true);
		rDAO.addResource(fd);
		
		return fd;
	}
	@CommitAfter
	private ResourceFolder createDefaultPrivateFolder(){
		ResourceFolder fd = new ResourceFolder();
		fd.setDes(messages.get("default-private-folder-des"));
		fd.setCdate(new Date());
		fd.setMdate(new Date());
		fd.setName(messages.get("default-private-folder-name"));
		fd.setOwner(getCurUser());
		fd.setProj(curProj);
		fd.setShared(false);
		rDAO.addResource(fd);
		
		return fd;
	}
	
	public boolean testLoadDefaultFolders(){
		if(!canCreateFolder(curProj))
			return false;
		if(curFolder!=null && curFolder.isShared() && curFolder.getParent()==null){ //at the 2nd-level shared folder
			return true;
		}
		return false;
	}
	
	@CommitAfter
	void onActionFromLoadDefaultFolders(String fdId){
		
		if(!canCreateFolder(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		ResourceFolder fd = rDAO.getFolderById(fdId);
		if(fd==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "FolderID", fdId));
		
		String[] folderNames = {messages.get("default-folder-name1")
				,messages.get("default-folder-name2")
				,messages.get("default-folder-name3")
				,messages.get("default-folder-name4")};
		String[] folderDes = {messages.get("default-folder-description1")
				,messages.get("default-folder-description2")
				,messages.get("default-folder-description3")
				,messages.get("default-folder-description4")};
		
		
		for(int i=0; i<folderNames.length; i++){
			String fName = folderNames[i];
			String fDes = folderDes[i];
			ResourceFolder f = new ResourceFolder();
			f.setDes(fDes);
			f.setCdate(new Date());
			f.setMdate(new Date());
			f.setName(fName);
			f.setOwner(getCurUser());
			f.setProj(curProj);
			f.setParent(fd);
			f.setShared(true);
			rDAO.addResource(f);
		}
	}
	
	public long countResourcesByFolder(String folderId){
		ResourceFolder folder = rDAO.getFolderById(folderId);
		return rDAO.getCountResourcesByFolder(folder);
	}
	//----------
	
	public String getParentId(Resource r){
		if(r==null || r.getParent()==null)
			return null;
		return r.getParent().getId();
	}
	
	public BeanModel<Resource> getModel() {
		BeanModel<Resource> model = beanModelSource.createEditModel(Resource.class, messages);
        model.include("name","cdate");
        model.add("owner", propertyConduitSource.create(Resource.class, "owner.displayName")); 
        model.add("typeicon",null); 
        model.add("size", null);
	    model.add("action", null);
	    model.add("chkbox", null); 
        model.reorder("chkbox","typeicon","name","cdate","owner","size");
     	
        return model;
    }
	
	
	public String getBreadcrumbLink(){
		String str ="";
		ResourceFolder fd = curFolder;
		while(fd!=null){
			str = ","+Util.encode(Util.truncateString(fd.getName(),50))+"=modules/resources/home?"+pid+":"+fd.getId() + str;
			fd = fd.getParent();
		}
		return str;
	}
	public int getTotalSize() {
		if(resrcs==null) return 0;
		return resrcs.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public void setRowsPerPage(int num){
		appState.setRowsPerPage(num);
	}
	
	void onSelectedFromDelete(){
		submitType = SubmitType.DELETE;
	}
	void onSelectedFromMultiDownload(){
		submitType = SubmitType.MULTI_DOWNLOAD;
	}
	void onSelectedFromCopy(){
		submitType = SubmitType.COPY;
	}
	void onSelectedFromMove(){
		submitType = SubmitType.MOVE;
	}
	
	
	
	@CommitAfter
	Object onSuccessFromGridForm(){
		if(submitType != null){
			String[] selectedId = request.getParameters("chkBox");
			List<Resource> selectedResrc = new ArrayList<Resource>();
			for(String id : selectedId){ //find and skip item that no long in this folder, i.e deleted or moved by other users
				Resource r = rDAO.getResourceById(id);
				if(r==null){
					appState.recordErrorMsg(messages.format("resource-x-not-found", id));
					continue;
				}
				if(curFolder!=null && !curFolder.equals(r.getParent())){
					appState.recordErrorMsg(messages.format("skip-resource-x-not-in-this-folder", r.getName()));
					continue;
				}
				selectedResrc.add(r);
			}
			
			switch(submitType){
			case MULTI_DOWNLOAD:
				int dl_count = 0;
				final int BUFFER_SIZE = 10240; 
				Set<String> zEntrySet = new HashSet<String>();//to keep track duplicate zipEntry. function add() return true if not already contain the element
				/***NOTE: assume NO DUPLICATE FOLDER NAME ARE SELECTED ****/
				
				ZipOutputStream zipOut = null;
				byte[] buffer = new byte[BUFFER_SIZE];
				response.setHeader("Content-Disposition", "attachment; filename=\"Files.zip\"");
				
				try{
					zipOut = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream("application/zip"), BUFFER_SIZE));
					
					String tempFolderName = Config.getString(Config.VIRTUAL_DRIVE)+"/temp";
					File tempFolder = new File(tempFolderName);
					if(!tempFolder.exists())
						tempFolder.mkdir();
					String tempLinkFileName = Util.generateUUID()+".html";
					File tempLinkFile = new File(tempFolderName+"/"+tempLinkFileName);
					BufferedWriter linkFileWriter = new BufferedWriter(new FileWriter(tempLinkFile));
					
					for(Resource r : selectedResrc){
						curProj = r.getProj();
						if(!canAccessResource(r)){
							appState.recordErrorMsg(messages.format("not-authorized-to-access-x", r.getName()));
							continue;
						}
						InputStream input = null;
						
						try{
							if(r.isFolder()){
								//zip folder
								Deque<ResourceFolder> queue = new LinkedList<ResourceFolder>();
								queue.push((ResourceFolder) r );
								String baseName = r.getName();
								baseName = baseName.endsWith("/")? baseName : baseName + "/";
								while(!zEntrySet.add(baseName)){ //add & check if element exists
									//if exist
									baseName = Util.appendSequenceNo(baseName, "_",null);
								}
								zipOut.putNextEntry(new ZipEntry(baseName));
								
								
								while(!queue.isEmpty()){
									ResourceFolder fd = queue.pop();
									resrcs = rDAO.getResourcesByParent(fd);
									for(Resource kid : resrcs){
										String name = baseName + getResourceFilePath(kid, r);
										if(kid.isFolder()){
											queue.push((ResourceFolder) kid);
											name = name.endsWith("/")? name : name + "/";
											while(!zEntrySet.add(name)){ //add & check if element exists
												name = Util.appendSequenceNo(name,"_",null);
											}
											zipOut.putNextEntry(new ZipEntry(name));
											
										}
										else if(kid.isFile()){
											try{
												ResourceFile rf = (ResourceFile) kid;
												while(!zEntrySet.add(name)){ //add & check if element exists
													name = Util.appendSequenceNo(name,"_", "."+FilenameUtils.getExtension(name));
												}
												zipOut.putNextEntry(new ZipEntry(name));
												String path = Config.getString(Config.VIRTUAL_DRIVE) + rf.getLatestFileVersion().getPath();
												input = new BufferedInputStream(new FileInputStream(path), BUFFER_SIZE);
												for(int length=0; (length = input.read(buffer))>0;){
													zipOut.write(buffer, 0, length);
												}
												zipOut.closeEntry();
												dl_count++;
											}catch(FileNotFoundException e){
												logger.error(e.getMessage());
											}
										}	
										else if(kid.isLink()){
											ResourceLink rl = kid.toLink();
											linkFileWriter.write("<div>");
											linkFileWriter.write("<div>"+rl.getName()+"</div>");
											linkFileWriter.write("<div><a href=\""+rl.getUrl()+"\">"+rl.getUrl()+"</a></div>");
											if(rl.getDes()!=null)
												linkFileWriter.write("<div>"+rl.getDes()+"</div>");
											linkFileWriter.write("</div><br/>");
											linkFileWriter.newLine();
											
										}
									}
								}
							}
							else if(r.isFile()){
								ResourceFile rf = (ResourceFile) r;
								String path = Config.getString(Config.VIRTUAL_DRIVE) + rf.getLatestFileVersion().getPath();
								try{
									input = new BufferedInputStream(new FileInputStream(path), BUFFER_SIZE);
									String name = r.getName();
									while(!zEntrySet.add(name)){ //add & check if element exists
										name = Util.appendSequenceNo(name,"_", "."+FilenameUtils.getExtension(name));
									}
									zipOut.putNextEntry(new ZipEntry(name));
									for(int length=0; (length = input.read(buffer))>0;){
										zipOut.write(buffer, 0, length);
									}
									zipOut.closeEntry();
									dl_count++;
								}
								catch(FileNotFoundException e){
									logger.error(e.getMessage());
								}
							}
							else if(r.isLink()){
								ResourceLink rl = r.toLink();
								linkFileWriter.write("<div>");
								linkFileWriter.write("<div>"+rl.getName()+"</div>");
								linkFileWriter.write("<div><a href=\""+rl.getUrl()+"\">"+rl.getUrl()+"</a></div>");
								if(rl.getDes()!=null)
									linkFileWriter.write("<div>"+rl.getDes()+"</div>");
								linkFileWriter.write("</div><br/>");
								linkFileWriter.newLine();
								
							}
						}
						finally{
							if(input!=null) try{ input.close();}catch(IOException e){logger.error(e.getMessage());}
						}
					}
					
					if(linkFileWriter!=null) try{ linkFileWriter.close();}catch(IOException e){logger.error(e.getMessage());}
					//put the Links file into the zip
					if(tempLinkFile.length()>0){ //if have written something to this file
						InputStream input = new BufferedInputStream(new FileInputStream(tempLinkFile), BUFFER_SIZE);
						String name = "Links.html";
						while(!zEntrySet.add(name)){ //add & check if element exists
							name = Util.appendSequenceNo(name,"_", "."+FilenameUtils.getExtension(name));
						}
						zipOut.putNextEntry(new ZipEntry(name));
						for(int length=0; (length = input.read(buffer))>0;){
							zipOut.write(buffer, 0, length);
						}
						zipOut.closeEntry();
						dl_count++;
						if(input!=null) try{ input.close();}catch(IOException e){logger.error(e.getMessage());}
						
					}
					tempLinkFile.deleteOnExit();
					
				} catch (IOException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
				finally{
					if(zipOut!=null) try{ zipOut.close();}catch(IOException e){logger.error(e.getMessage());}
				}
				
				break;
				
				
			case DELETE:
				int count = 0;
				for(Resource r : selectedResrc){
					curProj = r.getProj();
					if(!canDeleteResource(r)){
						appState.recordErrorMsg(messages.format("not-authorized-to-delete-x", r.getName()));
						continue;
					}
					
					if(r.isFolder()){
						ResourceFolder rf = (ResourceFolder) r;
						if(rDAO.hasChild(rf)){
							appState.recordErrorMsg(messages.format("cant-delete-folder-x-not-empty", rf.getName()));
						}
						else{
							rDAO.deleteResource(r);
							count++;
						}
					}
					else if(r.isFile()){
						// delete the actual file
						ResourceFile rf = (ResourceFile) r;
						for(ResourceFileVersion fv : rf.getFileVersions()){
							File f = new File(Config.getString(Config.VIRTUAL_DRIVE)+ fv.getPath());
							//f.renameTo(new File(f.getAbsolutePath()+"_deleted"));
							f.delete();
						}
						rDAO.deleteResource(r);
						count++;
					}
					else if(r.isLink()){
						rDAO.deleteResource(r);
						count++;
					}
				}
				if(count!=0)
					appState.recordInfoMsg(messages.format("x-resources-successfully-deleted", count));
				break;
		
			case COPY:
				List<String> selected = new ArrayList<String>();
				for(Resource r : selectedResrc){
					if(!canCopyResource(r)){
						appState.recordErrorMsg(messages.format("no-privilege-to-copy-x", r.getName()));
					}
					else{
						selected.add(r.getId());
					}
				}
				if(!selected.isEmpty()){
					copyOrMovePage.setContext(pid, fid, selected, false);
					return copyOrMovePage;
				}
				else{
					break;
				}
			case MOVE:
				selected = new ArrayList<String>();
				for(Resource r : selectedResrc){
					if(!canMoveResource(r)){
						appState.recordErrorMsg(messages.format("no-privilege-to-move-x", r.getName()));
					}
					else{
						selected.add(r.getId());
					}
				}
				if(!selected.isEmpty()){
					copyOrMovePage.setContext(pid, fid, selected, true);
					return copyOrMovePage;
				}
				else{
					break;
				}
			}
		}
		return null;
	}
	@InjectPage
	private CopyOrMove copyOrMovePage;
	
	
	
	//------------ newFolder
	@Property
	private String fName;
	@Property
	private String fDes;
	@CommitAfter
	void onSuccessFromNewFolderForm() {
		ResourceFolder rs = new ResourceFolder();
		rs.setProj(curProj);
		rs.setCdate(new Date());
		rs.setMdate(new Date());
		rs.setOwner(getCurUser());
		rs.setParent(curFolder);
		rs.setShared(curFolder.isShared());
		
		String orgFileName = fName;
		while(rDAO.checkResourceNameExist(fName, pid, fid)){ //check with database
			fName = Util.appendSequenceNo(fName,"_", null);
		}
		if(!orgFileName.equals(fName)){
			appState.recordWarningMsg(messages.format("rename-resource-x-to-y", orgFileName, fName));
		}
		
		rs.setName(fName);
		rs.setDes(Util.textarea2html(fDes));
		rDAO.addResource(rs);
	}
	
	//------------ newLink
	@Property
	private String lName;
	@Property
	private String lURL;
	@Property
	private String lDes;
	@CommitAfter
	void onSuccessFromNewLinkForm() {
		ResourceLink rs = new ResourceLink();
		rs.setProj(curProj);
		rs.setCdate(new Date());
		rs.setMdate(new Date());
		rs.setOwner(getCurUser());
		rs.setParent(curFolder);
		rs.setShared(curFolder.isShared());
		
		String orgFileName = lName;
		while(rDAO.checkResourceNameExist(lName, pid, fid)){ //check with database
			lName = Util.appendSequenceNo(lName,"_", null);
		}
		if(!orgFileName.equals(lName)){
			appState.recordWarningMsg(messages.format("rename-resource-x-to-y", orgFileName, lName));
		}
		
		rs.setName(lName);
		rs.setUrl(Util.checkAndAddHTTP(lURL));
		rs.setDes(Util.textarea2html(lDes));
		rDAO.addResource(rs);
	}
	
	@Property
	private UploadedFile upFile;
	@Property
	private String upDes;
	@CommitAfter
	void onSuccessFromNewFileForm() {
		
		//note, when change these, also see uploadFileServlet.java, MigrateDAO.java
		String rootDir = Config.getString(Config.VIRTUAL_DRIVE) + "/" + pid + "/"+PredefinedNames.MODULE_RESOURCE;
		File rootFolder = new File(rootDir);
		if (!rootFolder.exists() || !rootFolder.isDirectory()) {
			rootFolder.mkdirs();
		}
		File dirFile = new File(rootDir + "/" + fid);
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			dirFile.mkdirs();
		}
		String fileName = upFile.getFileName();
		
		String orgFileName = fileName;
		while(rDAO.checkResourceNameExist(fileName, pid, fid)){ //check with database
			fileName = Util.appendSequenceNo(fileName,"_", "."+FilenameUtils.getExtension(fileName));
		}
		if(!orgFileName.equals(fileName)){
			appState.recordWarningMsg(messages.format("rename-filename-x-to-y", orgFileName, fileName));
		}
		
		Date now = new Date();
		String prefix = Util.formatDateTime(now, "yyyyMMdd-HHmmss")+"_"+getCurUser().getId();
		String theFilePath = dirFile + "/" + prefix +"_"+ fileName.replace(" ", "_");
		File theFile = new File (theFilePath);
		
		upFile.write(theFile);
		String path = theFilePath.replace(Config.getString(Config.VIRTUAL_DRIVE), ""); 
		
		ResourceFile rs = new ResourceFile();
		rs.setProj(curProj);
		rs.setCdate(new Date());
		rs.setMdate(new Date());
		rs.setOwner(getCurUser());
		rs.setParent(curFolder);
		rs.setShared(curFolder.isShared());
		rs.setName(fileName);
		rs.setDes(Util.textarea2html(upDes));
		//rDAO.immediateAddResource(rs);
		//rs.setId(Util.generateUUID());
		
		ResourceFileVersion rfv = new ResourceFileVersion();
		rfv.setPath(path);
		rfv.setVersion(1);
		rfv.setCmmt(rs.getDes());
		rfv.setContentType(Util.additionContentTypeCheck(upFile.getContentType(), fileName));
		rfv.setSize(upFile.getSize());
		rfv.setCdate(new Date());
		rfv.setName(fileName);
		//rfv.setNumDownload(numDownload);
		rfv.setOwner(getCurUser());
		//rfv.setRfile(rs);
		
		rs.addFileVersion(rfv);
		
		rDAO.addResource(rs);
	}
	
	//-------- newMultiFile
	public String getHandlerUrl(){
		String protocol = request.isSecure()? "https" : "http";
		String port  = request.getServerPort()==80 ? "" :  ":"+request.getServerPort();
		String host = protocol + "://"+request.getServerName() + port;
		
		return 	host + request.getContextPath()+"/Handler?pid=" + this.pid 
			+ "&fid=" + this.fid + "&uid=" + getCurUser().getId();
	}
	
	@Property
	@Persist
	private boolean showApplet;
	@InjectComponent
	private Zone appletZone;
	Object onToggleApplet(){
		showApplet = !showApplet;
		return appletZone.getBody();
	}
	
	private String getResourceFilePath(Resource r, Resource baseR){
		String filePath = r.getName();
		while(r.getParent()!=null && !r.getParent().equals(baseR)){
			String name = r.getParent().getName();
			filePath =  (name.endsWith("/")? name : name + "/") + filePath;
			r = r.getParent();
		}
		return filePath;
	}
	
	
}
