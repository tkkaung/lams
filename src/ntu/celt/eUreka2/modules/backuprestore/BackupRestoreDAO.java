package ntu.celt.eUreka2.modules.backuprestore;

import java.util.List;

import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.User;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

public interface BackupRestoreDAO {

	public List<BackupEntry> getBackupEntriesByUser(User u);
	@CommitAfter
	public void saveBackupEntry(BackupEntry bEntry);
	public BackupEntry getBackupEntryById(String id);
	@CommitAfter
	public void deleteBackupEntry(BackupEntry bEntry);
	/*@CommitAfter
	public BackupEntry exportProj(Project proj, String[] selModuleIds, User creator, BackupEntry bEntry);
	@CommitAfter
	public void exportAndZipProjInfo(Project proj, ZipOutputStream zout) throws IOException ;
	@CommitAfter
	public void importModule(BackupEntry bEntry , String moduleName, User user) throws IOException, BackupRestoreException;
	*/
	
	String getRenamedPath(String folderName, String attId, String fileName);
	
	 final String FileName_ProjInfo = PredefinedNames.PROJECT_INFO+".json";
	 final String FileName_Announcement = PredefinedNames.MODULE_ANNOUNCEMENT+".json";
	 final String FileName_Budget = PredefinedNames.MODULE_BUDGET+".json";
	 final String FileName_Forum = PredefinedNames.MODULE_FORUM+".json";
	 final String FolderName_Forum_AttachedFile = PredefinedNames.MODULE_FORUM+"_attachedFile/";
	 final String FileName_Message = PredefinedNames.MODULE_MESSAGE+".json";
	 final String FileName_Scheduling = PredefinedNames.MODULE_SCHEDULING+".json";
	 final String FileName_Blog = PredefinedNames.MODULE_BLOG+".json";
	 final String FileName_Blog_Config = PredefinedNames.MODULE_BLOG+"_config.json";
	 final String FolderName_Blog_AttachedFile = PredefinedNames.MODULE_BLOG+"_attachedFile/";
	 final String FileName_Resource = PredefinedNames.MODULE_RESOURCE+".json";
	 final String FolderName_Resource_File = PredefinedNames.MODULE_RESOURCE+"_File/";
	
	
}
