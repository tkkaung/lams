package ntu.celt.eUreka2.modules.resources;

import java.util.List;


import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;

public interface ResourceDAO extends GenericModuleDAO {

	
	/*
	List<ResourceFolder> getResourcesByProject(Project proj);
	List<ResourceFolder> getResourceRoot(ResourceType t, String projId);
	List<ResourceFolder> getResource(ResourceType type, String projId, String parentId);
	List<ResourceFolder> getResources(Project proj, ResourceFolder parent);
	long getCountResourcesByType(Project proj, ResourceType type, User curUser);
	long getCountResourcesByFolder(ResourceFolder folder, User curUser);
	long getCountDescedents(ResourceFolder folder, User curUser);
	boolean hasChild(ResourceFolder folder);
	ResourceFolder getResourceById(String id);
	void addResource(ResourceFolder res);
	void updateResource(ResourceFolder res);
	void deleteResource(ResourceFolder res);
	int checkFileVersion(String fileName, String projId, String folderId);
	
	long countPastUpdated(Project project, int numOfPastDays);
	
	 
	List<ResourceFolder> searchResources(String projId, String parentId, ResourceType type,  String name, User owner);
	long countFolders(Project proj);
	long countFiles(Project proj);
	long countLinks(Project proj);
	long countNumDownloads(Project proj);
	
	*/
	Resource getResourceById(String rid);
	ResourceFolder getFolderById(String rid);
	ResourceFile getFileById(String fid);
	List<ResourceFolder> getRootFolders(Project project);
	List<ResourceFolder> getSharedRootFolders(Project project);
	List<ResourceFolder> getPrivateRootFolders(User user);
	List<Resource> getResourcesByParent(ResourceFolder parent);
	List<ResourceFolder> getFoldersByParent(ResourceFolder parent);
	void addResource(Resource res);
	void updateResource(Resource res);
	void deleteResource(Resource res);
	long getCountResourcesByFolder(ResourceFolder folder);
	long getCountDescedents(ResourceFolder folder);
	boolean hasChild(ResourceFolder folder);
	ResourceFileVersion getResourceFileVersionById(String rfvId);
	void updateResourceFileVersion(ResourceFileVersion rfv);
	boolean checkResourceNameExist(String fileName, String projId, String parentFolderId);
	
	long countPastUpdated(Project project, int numOfPastDays);
	long countFolders(Project proj);
	long countFiles(Project proj);
	long countLinks(Project proj);
	int countNumDownloads(Project proj);
	
	long countFolders(Project proj, User creator);
	long countFiles(Project proj, User creator);
	long countFileVersions(Project proj, User creator);
	long countLinks(Project proj, User creator);
	List<ResourceFolder> getFolders(Project proj, User creator);
	List<ResourceFile> getFiles(Project proj, User creator);
	List<ResourceFileVersion> getFileVersions(Project proj, User creator);
	List<ResourceLink> getLinks(Project proj, User creator);
	
}
