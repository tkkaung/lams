package ntu.celt.eUreka2.pages.modules.resources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;

@Import(library={"context:modules/resources/js/context-menu.js"
		,"context:modules/resources/js/drag-drop-folder-tree.js"
		}
	,stylesheet={"context:modules/resources/css/drag-drop-folder-tree.css"
		,"context:modules/resources/css/context-menu.css"})
public class CopyOrMove extends AbstractPageResource {
	@Inject
	private ResourceDAO rDAO;
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private Logger logger;
	@Inject
    private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	
	@SessionState
	private AppState appState;
	
	@Property
	private String pid;
	@Property
	private String fid;
	@Persist       //use persist because do want to pass it through onPassivate()
	@Property
	private List<String> resrcToCopy;
	@Property
	private boolean doMove;
	@Property
	private Project curProj;
	@Property
	private ResourceFolder curFolder;
	@Property
	private ResourceFolder selectedFolder;
	@Property
	private String destFolderId;
	
	@SuppressWarnings("unused")
	@Property
	private TreeNode selectedNode;
	@SuppressWarnings("unused")
	@Property
	private TreeNode node;
	@Property
	private List<TreeNode> treeNodes;
	
	
    
	
	void onActivate(EventContext ec) {
		pid = ec.get(String.class, 0);
		fid = ec.get(String.class, 1);
		doMove = ec.get(Boolean.class, 2);
	
		if(resrcToCopy==null)
			throw new RecordNotFoundException(messages.format("no-item-to-x", getActionDisplay()));
			
		
		curProj = pDAO.getProjectById(pid);
		if(curProj==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectID", pid));
		}
		
		curFolder = rDAO.getFolderById(fid);
		if(curFolder==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ResourceID", fid));
		if(!canAccessResource(curFolder))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
	}
	Object[] onPassivate() {
		return new Object[] {pid, fid, doMove};
	}
	
	public void setContext(String projId, String curFolderId, List<String> resrcToCopy, boolean doMove ){
		pid = projId;
		fid = curFolderId;
		this.resrcToCopy = resrcToCopy;
		this.doMove = doMove;
	}
	
	public void setupRender() {
		initTreeNodes();
		
	}
	void onPrepareFromDestinationFolderForm(){
		
	}
	
	private void initTreeNodes(){
		List<ResourceFolder> sharedFolders = rDAO.getSharedRootFolders(curProj);
		List<ResourceFolder> privateFolders = rDAO.getPrivateRootFolders(getCurUser());
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
		
		if(selectedFolder!=null){
			selectedNode = new TreeNode(selectedFolder.getId(), selectedFolder.getName(), selectedFolder.getDes(), getDepth(selectedFolder));
		}
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
	
	
	
	
	public long countResourcesByFolder(String folderId){
		ResourceFolder folder = rDAO.getFolderById(folderId);
		return rDAO.getCountResourcesByFolder(folder);
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
	
	@Component
	private Form destinationFolderForm;
	
	void onValidateFormFromDestinationFolderForm(){
		if(destFolderId==null){
			destinationFolderForm.recordError(messages.get("must-select-destination-folder"));
		}
	}
	
	@CommitAfter
	Object onSuccessFromDestinationFolderForm(){
		ResourceFolder destFolder = rDAO.getFolderById(destFolderId);
		if(destFolder==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Destination Folder ID", destFolderId));
		if(!destFolder.isShared() && !destFolder.getOwner().equals(getCurUser())){
			throw new NotAuthorizedAccessException(messages.format("cannot-x-to-this-folder", getActionDisplay()));
		}
			
		if(doMove){ //move
			for(String resrcId : resrcToCopy){
				Resource r = rDAO.getResourceById(resrcId);
				if(!canMoveResource(r)){
					appState.recordErrorMsg(messages.format("no-privilege-to-move-x", r.getName()));
					continue; //skip this resrc
				}
				if(!destFolder.isShared()  ){ //if move to private folder
					if(!allOwnedByCurUser(r)){
						if(r.isFolder())
							appState.recordErrorMsg(messages.format("cannot-move-other-user-folder-x", r.getName()));
						else
							appState.recordErrorMsg(messages.format("cannot-move-other-user-file-x", r.getName()));
						continue; //skip this resrc
					}
				}
				if(r.isShared()!=destFolder.isShared()){
					r = saveAllSharedStatus(r, destFolder.isShared());
				}
				r.setParent(destFolder);
				rDAO.updateResource(r);
			}
		}
		else{ //copy
			for(String resrcId : resrcToCopy){
				Resource r = rDAO.getResourceById(resrcId);
				if(!canCopyResource(r)){
					appState.recordErrorMsg(messages.format("no-privilege-to-copy-x", r.getName()));
					continue; //skip this resrc
				}
				
				Resource destResrc = copyAllResource(r, destFolder);
				//rDAO.updateResource(destResrc); //no need to do this
			}
		}
		
		return linkSource.createPageRenderLinkWithContext(Home.class, pid, destFolderId);
	}
	
	private boolean allOwnedByCurUser(Resource r){
		if(!r.getOwner().equals(getCurUser()))
			return false;
		if(r.isFolder()){
			for(Resource c : r.toFolder().getChildren()){
				if(!allOwnedByCurUser(c))
					return false;
			}
		}
		return true;
	}
	private Resource saveAllSharedStatus(Resource r, boolean toValue){
		r.setShared(toValue);
		if(r.isFolder()){
			for(Resource c : r.toFolder().getChildren()){
				c = saveAllSharedStatus(c, toValue);
			}
		}
		rDAO.updateResource(r);
		return r;
	}
	private Resource copyAllResource(Resource r, ResourceFolder parent){
		Resource r2 = r.clone();
		r2.setId(Util.generateUUID()); //use new ID
		r2.setParent(parent);
		r2.setShared(parent.isShared());
		r2.setCdate(new Date());
		r2.setMdate(new Date());
		
		r2.setOwner(getCurUser());
		r2.setEditor(getCurUser());
		
		if(r.isFile()){
			ResourceFile f2 = r2.toFile();
			for(int j=f2.getFileVersions().size()-1; j>=0; j--){
				ResourceFileVersion rfv = f2.getFileVersions().get(j);
				
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
				String fileName = rfv.getName();
				String prefix = Util.formatDateTime(new Date(), "yyyyMMdd-HHmmss")+"_"+getCurUser().getId();
				String destPath = dirFile + "/" + prefix +"_"+ fileName.replace(" ", "_");
				
				String srcPath = Config.getString(Config.VIRTUAL_DRIVE) + rfv.getPath();
				
				//copy actual file
				InputStream is = null;
				BufferedOutputStream bout = null;
				try {
					is = new BufferedInputStream(new FileInputStream(srcPath));
					bout = new BufferedOutputStream(new FileOutputStream(destPath));
					
					byte[] buf = new byte[1024];
					int len;
					while((len=is.read(buf))>0){
						bout.write(buf, 0, len);
					}
					
					//update resourceFileVersion info
					
					rfv.setId(Util.generateUUID());
					rfv.setRfile(f2); //fix the clone() value
					rfv.setOwner(getCurUser());
					String destSavePath = destPath.replace(Config.getString(Config.VIRTUAL_DRIVE), ""); 
					rfv.setPath(destSavePath);
					rfv.setCdate(new Date());
					
				} catch (FileNotFoundException e) {
					appState.recordErrorMsg("FileNotFound: "+rfv.getPath());
					logger.error("FileNotFound: "+srcPath);
					f2.getFileVersions().remove(j);
				} catch (IOException e) {
					appState.recordErrorMsg("IOException: "+rfv.getPath());
					logger.error("IOException: "+e.getMessage());
					f2.getFileVersions().remove(j);
					e.printStackTrace();
				} finally{
					if(is!=null )
						try {is.close(); } catch (IOException e) {}
					if(bout!=null )
						try {bout.close(); } catch (IOException e) {}
				}
			}
			if(!f2.getFileVersions().isEmpty()){
				rDAO.addResource(f2);
			}
		}
		else if(r.isFolder()){
			for(Resource c : r.toFolder().getChildren()){
				c = copyAllResource(c, r2.toFolder());
				r2.toFolder().getChildren().add(c);
			}
			rDAO.addResource(r2);
		}
		else if(r.isLink()){
			rDAO.addResource(r2);
		}
		
		
		return r2;
	}
	
	public String getActionDisplay(){
		if(doMove)
			return messages.get("move");
		else
			return messages.get("copy");
	}
	public String getDisplayMessage(){
		return messages.format("you-about-to-x-y-resource", getActionDisplay(), resrcToCopy.size());
	}
	public String getStartActionMessage(){
		return messages.format("start-x", getActionDisplay());
	}
	
	
	
	
	
}
