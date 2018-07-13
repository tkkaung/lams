package ntu.celt.eUreka2.modules.backuprestore;

import java.util.List;

import ntu.celt.eUreka2.entities.User;

import org.apache.commons.io.FilenameUtils;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Query;
import org.hibernate.Session;

public class BackupRestoreDAOImpl implements BackupRestoreDAO {
	
	@Inject
	private Session session;
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<BackupEntry> getBackupEntriesByUser(User u){
		Query q = session.createQuery("SELECT b FROM BackupEntry b " +
				" JOIN b.project.members AS pu "+
				" WHERE b.creator=:rUser " +
/*				" OR (EXISTS (SELECT priv FROM pu.role.privileges priv" +
				"	WHERE pu.user=:rUser AND priv.name=:rPriv))")
				.setParameter("rUser", u)   
				.setString("rPriv", PrivilegeConstant.PrivilegeSystem)		
				;
	*/			"");
		return q.list();
	}
	@Override
	public void saveBackupEntry(BackupEntry bEntry){
		session.save(bEntry);
	}
	@Override
	public BackupEntry getBackupEntryById(String id){
		if(id==null) return null;
		return (BackupEntry) session.get(BackupEntry.class, id);
	}
	@Override
	public void deleteBackupEntry(BackupEntry bEntry){
		session.delete(bEntry);
	}
	@Override
	public String getRenamedPath(String folderName, String attId, String fileName){
		return folderName + attId + (fileName!=null? "."+FilenameUtils.getExtension(fileName) : "");
	}
	
}
