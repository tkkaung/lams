package ntu.celt.eUreka2.modules.resources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ntu.celt.eUreka2.data.LastUpdateItem;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.pages.modules.resources.Home;

import org.apache.commons.lang.time.DateUtils;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.hibernate.Query;
import org.hibernate.Session;

public class ResourceDAOImpl implements ResourceDAO {
	
	private Session session;
private PageRenderLinkSource linkSource;
	
	public ResourceDAOImpl(Session session, PageRenderLinkSource linkSource) {
		super();
		this.session = session;
		this.linkSource = linkSource;
	}
	

	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_RESOURCE;
	}

	@Override
	public long countPastUpdated(Project project, int numOfPastDays) {
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_YEAR, -1 * numOfPastDays);
		
		Query q = session.createQuery(" SELECT COUNT(r) FROM Resource AS r"
					+ " WHERE r.proj.id = :rProj "
					+ " AND r.mdate>=:rPastDay " 
					)
					.setString("rProj", project.getId())
					.setDate("rPastDay", DateUtils.truncate(pastDay.getTime(), Calendar.DATE))
					;
		
		return (Long) q.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_YEAR, -1 * numPastDay);
		
		Query q = session.createQuery("SELECT r FROM Resource AS r " +
				" WHERE r.proj.id=:rPid " +
				"   AND r.mdate>=:rPastDay " +
				"   ORDER BY mdate DESC" +
				" 	 ")
				.setString("rPid", project.getId())
				.setDate("rPastDay", pastDay.getTime())
				;
		
		List<Resource> rList = q.list();
		List<LastUpdateItem> lList = new ArrayList<LastUpdateItem>();
		for(Resource r : rList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(r.getMdate());
			l.setTitle(r.getName());
			l.setType(r.getType().name());
			l.setUrl(linkSource.createPageRenderLinkWithContext(Home.class
					, r.getProj().getId()
					, r.getParent()==null? null:r.getParent().getId())
					.toAbsoluteURI());
			
			lList.add(l);
		}
		
		return lList;
	}

	@Override
	public Resource getResourceById(String rid) {
		if (rid == null) return null;
		Resource res = (Resource) session.get(Resource.class, new String(rid));
		return res;
	}
	@Override
	public ResourceFolder getFolderById(String fid) {
		if (fid == null) return null;
		ResourceFolder res = (ResourceFolder) session.get(ResourceFolder.class, new String(fid));
		return res;
	}
	@Override
	public ResourceFile getFileById(String fid) {
		if (fid == null) return null;
		ResourceFile res = (ResourceFile) session.get(ResourceFile.class, new String(fid));
		return res;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ResourceFolder> getRootFolders(Project project) {
		Query q = session.createQuery("SELECT f FROM ResourceFolder AS f"
				+ " WHERE f.proj.id = :pId"
				+ " AND f.parent IS NULL "
				+ " ORDER BY f.cdate DESC "
				)
				.setString("pId", project.getId())
				;
		
		return q.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<ResourceFolder> getSharedRootFolders(Project project) {
		Query q = session.createQuery("SELECT f FROM ResourceFolder AS f"
				+ " WHERE f.proj.id = :pId"
				+ " AND f.parent IS NULL "
				+ " AND (shared = true )"
				+ " ORDER BY f.cdate DESC "
				)
				.setString("pId", project.getId())
				;
		
		return q.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<ResourceFolder> getPrivateRootFolders(User user) {
		Query q = session.createQuery("SELECT f FROM ResourceFolder AS f"
				+ " WHERE f.owner = :rUser"
				+ " AND f.parent IS NULL "
				+ " AND f.shared = false "
				+ " ORDER BY f.cdate DESC "
				)
				.setParameter("rUser", user)
				;
		
		return q.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Resource> getResourcesByParent(ResourceFolder parent) {
		Query q = session.createQuery("SELECT r FROM Resource AS r"
				+ " WHERE "
				+ " r.parent = :rParent"
				+ " ORDER BY r.type, r.cdate DESC "
				)
				.setParameter("rParent", parent)
				;
		
		return q.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ResourceFolder> getFoldersByParent(ResourceFolder parent) {
		Query q = session.createQuery("SELECT f FROM ResourceFolder AS f"
				+ " WHERE f.parent = :rParent"
				+ " ORDER BY f.cdate DESC "
				)
				.setParameter("rParent", parent)
				;
		
		return q.list();
	}

	@Override
	public void addResource(Resource res) {
		session.save(res);
	//	session.flush();
	}
	@Override
	public void updateResource(Resource res) {
		session.update(res);
	//	session.flush();
	}
	@Override
	public void deleteResource(Resource res) {
		session.delete(res);
	}
	@Override
	public long getCountResourcesByFolder(ResourceFolder folder) {
		Query q = session.createQuery("SELECT COUNT(*) FROM Resource AS r"
				+ " WHERE r.parent = :rFolder"
				+ " AND NOT(r.type = :rType) "
				)
				.setParameter("rFolder", folder)
				.setParameter("rType", ResourceType.Folder)
				;
		
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public long getCountDescedents(ResourceFolder folder) {
		long curCount = 0;
		Query q = session.createQuery("SELECT COUNT(*) FROM Resource AS r"
				+ " WHERE r.parent = :rFolder"
				+ " AND r.type <> :rType "
				)
				.setParameter("rFolder", folder)
				.setParameter("rType", ResourceType.Folder)
				;
		curCount += (Long) q.uniqueResult();
		Query q2 = session.createQuery("SELECT r FROM ResourceFolder AS r"
				+ " WHERE r.parent = :rFolder"
				)
				.setParameter("rFolder", folder)
				;
		List<ResourceFolder> subList = q2.list();
		for(ResourceFolder fd : subList){
			curCount += getCountDescedents(fd);
		}
		
		return curCount;
	}
	
	@Override
	public boolean hasChild(ResourceFolder folder) {
		Query q = session.createQuery("SELECT COUNT(r) FROM Resource AS r"
				+ " WHERE r.parent = :rParent"
				)
				.setParameter("rParent", folder)
				;
		Long count = (Long) q.uniqueResult();
		if(count==0)
			return false;
		return true;
	}

	

	@Override
	public ResourceFileVersion getResourceFileVersionById(String rfvId) {
		if (rfvId == null) return null;
		ResourceFileVersion rfv = (ResourceFileVersion) session.get(ResourceFileVersion.class, new String(rfvId));
		return rfv;
	}

	@Override
	public void updateResourceFileVersion(ResourceFileVersion rfv) {
		session.update(rfv);
	}

	@Override
	public boolean checkResourceNameExist(String fileName, String projId, String parentFolderId) {
		Query q = session.createQuery("SELECT f FROM Resource AS f " 
					+ " WHERE  " 
					+ "  f.name = :rName "
					+ " AND f.proj.id = :rPID "
					+ (parentFolderId==null? "AND f.parent IS NULL " : " AND f.parent.id = :rFolderID ")
					)
					.setString("rPID", projId)
					.setString("rName", fileName)
					.setMaxResults(1)
					;
		if(parentFolderId!=null)
			q = q.setString("rFolderID", parentFolderId);
		
		Resource f = (Resource) q.uniqueResult();  //get only the latest version
		if(f!=null ){ //if File of the same name exist
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public long countFiles(Project proj) {
		Query q = session.createQuery("SELECT count(f) FROM ResourceFile AS f " 
				+ " WHERE f.proj.id = :rPID " )
				.setString("rPID", proj.getId());
		return (Long) q.uniqueResult();
	}

	@Override
	public long countFolders(Project proj) {
		Query q = session.createQuery("SELECT count(f) FROM ResourceFolder AS f " 
				+ " WHERE f.proj.id = :rPID " )
				.setString("rPID", proj.getId());
		return (Long) q.uniqueResult();
	}

	@Override
	public long countLinks(Project proj) {
		Query q = session.createQuery("SELECT count(f) FROM ResourceLink AS f " 
				+ " WHERE f.proj.id = :rPID " )
				.setString("rPID", proj.getId());
		return (Long) q.uniqueResult();
	}

	@Override
	public int countNumDownloads(Project proj) {
		Query q = session.createQuery("SELECT sum(rfv.numDownload) FROM ResourceFileVersion AS rfv " 
				+ " WHERE rfv.rfile.proj.id = :rPID " )
				.setString("rPID", proj.getId());
		Long num = (Long) q.uniqueResult();
		if(num==null)
			return 0;
		return (int) ((long) num);
	}


	@Override
	public long countFiles(Project proj, User creator) {
		Query q = session.createQuery("SELECT count(f) FROM ResourceFile AS f " 
				+ " WHERE f.proj.id = :rPID "
				+ "  AND f.owner=:rCreator")
				.setString("rPID", proj.getId())
				.setParameter("rCreator", creator)
				;
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<ResourceFile> getFiles(Project proj, User creator) {
		Query q = session.createQuery("SELECT f FROM ResourceFile AS f " 
				+ " WHERE f.proj.id = :rPID "
				+ "  AND f.owner=:rCreator")
				.setString("rPID", proj.getId())
				.setParameter("rCreator", creator)
				;
		return q.list();
	}

	@Override
	public long countFileVersions(Project proj, User creator) {
		Query q = session.createQuery("SELECT count(fv) FROM ResourceFileVersion AS fv " 
				+ " WHERE fv.rfile.proj.id = :rPID " 
				+ "  AND fv.owner=:rCreator "
			//	+ "  AND NOT(fv.version=1) "
				)
				.setString("rPID", proj.getId())
				.setParameter("rCreator", creator);
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<ResourceFileVersion> getFileVersions(Project proj, User creator) {
		Query q = session.createQuery("SELECT fv FROM ResourceFileVersion AS fv " 
				+ " WHERE fv.rfile.proj.id = :rPID " 
				+ "  AND fv.owner=:rCreator "
			//	+ "  AND NOT(fv.version=1) "
				+ "  ORDER BY fv.rfile , fv.version "
				)
				.setString("rPID", proj.getId())
				.setParameter("rCreator", creator);
		return q.list();
	}



	@Override
	public long countFolders(Project proj, User creator) {
		Query q = session.createQuery("SELECT count(f) FROM ResourceFolder AS f " 
				+ " WHERE f.proj.id = :rPID " 
				+ "  AND f.owner=:rCreator"
				)
				.setString("rPID", proj.getId())
				.setParameter("rCreator", creator);
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<ResourceFolder> getFolders(Project proj, User creator) {
		Query q = session.createQuery("SELECT f FROM ResourceFolder AS f " 
				+ " WHERE f.proj.id = :rPID " 
				+ "  AND f.owner=:rCreator"
				)
				.setString("rPID", proj.getId())
				.setParameter("rCreator", creator);
		return q.list();
	}

	@Override
	public long countLinks(Project proj, User creator) {
		Query q = session.createQuery("SELECT count(f) FROM ResourceLink AS f " 
				+ " WHERE f.proj.id = :rPID " 
				+ "  AND f.owner=:rCreator"
				)
				.setString("rPID", proj.getId())
				.setParameter("rCreator", creator);
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<ResourceLink> getLinks(Project proj, User creator) {
		Query q = session.createQuery("SELECT f FROM ResourceLink AS f " 
				+ " WHERE f.proj.id = :rPID " 
				+ "  AND f.owner=:rCreator"
				)
				.setString("rPID", proj.getId())
				.setParameter("rCreator", creator);
		return q.list();
	}


	
	

}
