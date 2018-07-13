package ntu.celt.eUreka2.pages.admin.backuprestore;

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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

import ntu.celt.eUreka2.dao.BackupRestoreException;
import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.announcement.Announcement;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.backuprestore.BackupAttachedFile;
import ntu.celt.eUreka2.modules.backuprestore.BackupEntry;
import ntu.celt.eUreka2.modules.backuprestore.BackupRestoreDAO;
import ntu.celt.eUreka2.modules.blog.Blog;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.blog.BlogFile;
import ntu.celt.eUreka2.modules.budget.Budget;
import ntu.celt.eUreka2.modules.budget.BudgetDAO;
import ntu.celt.eUreka2.modules.forum.Forum;
import ntu.celt.eUreka2.modules.forum.ForumAttachedFile;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.forum.Thread;
import ntu.celt.eUreka2.modules.message.Message;
import ntu.celt.eUreka2.modules.message.MessageDAO;
import ntu.celt.eUreka2.modules.message.MessageType;
import ntu.celt.eUreka2.modules.resources.ResourceDAO;
import ntu.celt.eUreka2.modules.scheduling.Schedule;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;


/*
 * IMPORTANT : it may be outdated, because bean entitries (e.g Project, User, Blog, ...) has changed,
 *    some field as been added or removed. If you want to use this, you need to trace,
 *    verify, test and make change accordingly.
 */
public class BackupProject {
	
	@Property
	private BackupEntry bEntry;
	@Property
	private List<Project> projs;
	@Property
	@Persist("flash")
	private String selProjId;
	@SuppressWarnings("unused")
	@Property
	private List<ProjModule> projModules;
	@SuppressWarnings("unused")
	@Property
	private ProjModule projModule;
	
	@SuppressWarnings("unused")
	@Property
	@Persist("flash")
	private List<String> resultSucceededBackups;
	@SuppressWarnings("unused")
	@Property
	@Persist("flash")
	private List<String> resultFailedBackups;
	@SuppressWarnings("unused")
	@Property
	@Persist("flash")
	private boolean showResult;
	
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private BackupRestoreDAO backupDAO;
	@Inject
	private ModuleDAO moduleDAO;
	@Inject
	private ForumDAO forumDAO;
	@Inject
	private AnnouncementDAO announcementDAO;
	@Inject
	private BudgetDAO budgetDAO;
	@Inject
	private SchedulingDAO schedulingDAO;
	@Inject
	private MessageDAO msgDAO;
	@Inject
	private BlogDAO blogDAO;
	@Inject
	private ResourceDAO resourceDAO;
	
	@Inject
	private AttachedFileManager attFileManager;
	
	@Inject
	private Request request;
	@Inject
	private Messages messages;
	@Inject
	private Logger logger;
	@Inject
    private WebSessionDAO webSessionDAO;
    @SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
    private String ssid;
    @SessionState
    private AppState appState ;

	@InjectComponent
	private Zone projModuleZone;

	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	void setupRender(){
		projs = projDAO.getProjectsByMember(getCurUser());;
		//filter out project that users do not have privilege to backup-restore
	/*	for(int i= projs.size()-1; i>=0; i--){
			Project proj = projs.get(i);
			if(!proj.getMember(wsData.getUser()).hasPrivilege(PrivilegeConstant.PrivilegeSystem)){
				projs.remove(i);
			}
		}
	*/	
		if(selProjId!=null)
			projModules = projDAO.getProjectById(selProjId).getProjmodules();
		else
			projModules = null;
	}
	void onPrepareForSubmitFromBackupForm(){
		if(selProjId!=null)
			projModules = projDAO.getProjectById(selProjId).getProjmodules();
		else
			projModules = null;
	}
	
	public SelectModel getProjModel(){
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (Project p : projs) {
			OptionModel optModel = new OptionModelImpl(p.getName(), p.getId());
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
	Object onChangeOfProj(){
		selProjId = request.getParameter("param");
		if(selProjId!=null)
			projModules = projDAO.getProjectById(selProjId).getProjmodules();
		else
			projModules = null;
		
		return projModuleZone.getBody();
	}
	
	
	@Component
	private BeanEditForm backupForm;
	void onValidateFormFromBackupForm(){
		if(selProjId == null){
			backupForm.recordError(messages.get("must-select-a-project"));
		}
		
		/*	String[] selModuleId = request.getParameters("moduleChkBox");
		
			if(selModuleId==null){
				backupForm.recordError(messages.get("error-must-select-at-least-a-item"));
			}
		 */	
	}
	
	Object onSuccessFromBackupForm(){
		Project proj = projDAO.getProjectById(selProjId);
		//String projInfo = request.getParameter("projInfoChkBox"); 
		String[] selModuleIds = request.getParameters("moduleChkBox"); 
		
		bEntry = exportProj(proj, selModuleIds, getCurUser(), bEntry);
		bEntry.setCreator(getCurUser());
		backupDAO.saveBackupEntry(bEntry);
		
		showResult = true;
		resultSucceededBackups = bEntry.getSucceededBackups();
		resultFailedBackups = bEntry.getFailedBackups();
		
		return null;
	}
	
	
   
    
    
    
    public BackupEntry exportProj(Project proj, String[] selModuleIds, User creator, BackupEntry bEntry){
		Date curTime = new Date();
		String zipFileName = Util.formatDateTime(curTime, "yyyyMMdd-HHmmss")+".zip";
		String folderPath = "/" + proj.getId() + "/"	+ "BackupRestore";
		File saveFolder = new File(Config.getString(Config.VIRTUAL_DRIVE) + folderPath);
		if (!saveFolder.exists())
			saveFolder.mkdirs();
		File saveFile = new File(saveFolder.getAbsolutePath() + "/" + zipFileName);

		FileOutputStream fout;
		
		try {
			fout = new FileOutputStream(saveFile);
			ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(fout));
		
			try{
				exportAndZipProjInfo(proj, zout);
			    bEntry.addSucceededBackups(PredefinedNames.PROJECT_INFO);
			}catch(IOException e){
				bEntry.addFailedBackups(PredefinedNames.PROJECT_INFO);
				e.printStackTrace();
				logger.error("Failed to export "+PredefinedNames.PROJECT_INFO+" id="+ proj.getId());
			}
			if(selModuleIds!=null){
				for(String moduleId : selModuleIds){
					Module mod = moduleDAO.getModuleById(Integer.parseInt(moduleId));
					try{
						exportAndZipModule(proj, mod.getName(), creator, zout);
					    bEntry.addSucceededBackups(mod.getName());
					}
					catch(BackupRestoreException e){
						bEntry.addFailedBackups(mod.getName());
						e.printStackTrace();
						logger.error(" -- Failed to export module:"+mod.getName());
					}
					catch(IOException e){
						bEntry.addFailedBackups(mod.getName());
						e.printStackTrace();
						logger.error(" -- Failed to export module:"+mod.getName());
					}
				}
	        }
	        zout.close();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		
		BackupAttachedFile bAttFile = new BackupAttachedFile();
		bAttFile.setBackupEntry(bEntry);
		bAttFile.setContentType("application/zip");
		bAttFile.setPath(folderPath + "/" + zipFileName);
		bAttFile.setFileName(zipFileName);
		bAttFile.setSize(saveFile.length());
		
		bEntry.setAttachedFile(bAttFile);
		bEntry.setProject(proj);
		bEntry.setCreateDate(curTime);
		bEntry.setCreator(creator);
		
		
		return bEntry;
	}
	
    private void exportAndZipModule(Project proj, String moduleName, User user, ZipOutputStream zout) throws IOException, BackupRestoreException{
		if(PredefinedNames.MODULE_ANNOUNCEMENT.equalsIgnoreCase(moduleName)){
			exportAndZipAnnouncements(proj, zout);
		}
		else if(PredefinedNames.MODULE_ASSESSMENT.equalsIgnoreCase(moduleName)){
			exportAndZipAssesments(proj, zout);
		}
		else if(PredefinedNames.MODULE_BLOG.equalsIgnoreCase(moduleName)){
			exportAndZipBlogs(proj, zout);
		}
		else if(PredefinedNames.MODULE_BUDGET.equalsIgnoreCase(moduleName)){
			exportAndZipBudgets(proj, zout);
		}
		else if(PredefinedNames.MODULE_FORUM.equalsIgnoreCase(moduleName)){
			exportAndZipForums(proj, zout);
		}
		else if(PredefinedNames.MODULE_MESSAGE.equalsIgnoreCase(moduleName)){
			exportAndZipMessages(proj, user, zout); 
		}
		else if(PredefinedNames.MODULE_RESOURCE.equalsIgnoreCase(moduleName)){
			exportAndZipResources(proj, zout);
		}
		else if(PredefinedNames.MODULE_SCHEDULING.equalsIgnoreCase(moduleName)){
			exportAndZipSchedulings(proj, zout);
		}
		else{
			throw new BackupRestoreException("Undefined method to backup the Module, name="+moduleName);
		}
		
	}
	
	public void exportAndZipProjInfo(Project proj, ZipOutputStream zout) throws IOException {
	/*	String fileName = "eUrekaProjInfo"+"_"+java.lang.Thread.currentThread().getId()+"_"+(new Date()).getTime()+".json";
		String folderName = System.getProperty("java.io.tmpdir");
		File f = new File(folderName + "/" + fileName);
		
		FileUtils.writeStringToFile(f, proj.toJSONstring());
		zipFile("ProjInfo." + FilenameUtils.getExtension(f.getName()), f, zout);
		f.delete();
    */
		
		zipString(BackupRestoreDAO.FileName_ProjInfo, proj.toJSONObject().toString(), zout);
    }
	private void exportAndZipAnnouncements(Project proj, ZipOutputStream zout) throws IOException{
		List<Announcement> annmts = announcementDAO.searchAnnouncements(proj, null, null);
		JSONArray jAnnmts = new JSONArray();
		for(Announcement annmt : annmts){
			jAnnmts.put(annmt.toJSONObject());
		}
		zipString(BackupRestoreDAO.FileName_Announcement, jAnnmts.toString(), zout);
	}
	private void exportAndZipBudgets(Project proj, ZipOutputStream zout) throws IOException{
		Budget budget = budgetDAO.getActiveBudget(proj);
		zipString(BackupRestoreDAO.FileName_Budget, budget.toJSONObject().toString(), zout);
	}
	
	private void exportAndZipForums(Project proj, ZipOutputStream zout) throws IOException{
		List<Forum> forums = forumDAO.getActiveForums(proj);
		
		JSONArray jforums = new JSONArray();
		for(Forum forum : forums){
			jforums.put(forum.toJSONObject());
		}
		zipString(BackupRestoreDAO.FileName_Forum, jforums.toString(), zout);
		
		String folderName = BackupRestoreDAO.FolderName_Forum_AttachedFile ;
		zout.putNextEntry(new ZipEntry(folderName));
		zout.closeEntry();
		//copy actual attached files
		for(Forum fr : forums){
			for(Thread th : fr.getThreads()){ //all threads, not just rootThreads
				for(ForumAttachedFile att : th.getAttachedFiles()){
					File f = attFileManager.getAttachedFile(att);
					String newEntryName = backupDAO.getRenamedPath(folderName, att.getId(), att.getFileName());
					zipFile(newEntryName , f, zout);
					
				}
			}
		}
		
	}
	
	
	private void exportAndZipMessages(Project proj, User user, ZipOutputStream zout) throws IOException{
		List<Message> msgs = new ArrayList<Message>();
		msgs.addAll(msgDAO.getMessages(user, proj, MessageType.DRAFT)); 
		msgs.addAll(msgDAO.getMessages(user, proj, MessageType.INBOX));
		msgs.addAll(msgDAO.getMessages(user, proj, MessageType.SENT));
		
		JSONArray jMessages = new JSONArray();
		for(Message msg : msgs){
			jMessages.put(msg.toJSONObject());
		}
		zipString(BackupRestoreDAO.FileName_Message, jMessages.toString(), zout);
	}
	private void exportAndZipSchedulings(Project proj, ZipOutputStream zout) throws IOException{
		Schedule schd = schedulingDAO.getActiveSchedule(proj);
		
		zipString(BackupRestoreDAO.FileName_Scheduling, schd.toJSONObject().toString(), zout);
	}
	
	private void exportAndZipAssesments(Project proj, ZipOutputStream zout) throws IOException{
		//TODO exportAndZipAssesments
		
	}
	private void exportAndZipBlogs(Project proj, ZipOutputStream zout) throws IOException{
		
		List<Blog> blogs = blogDAO.getBlogsByProject(proj);
		JSONArray jBlogs = new JSONArray();
		for(Blog b : blogs){
			jBlogs.put(b.toJSONObject());
		}
		zipString(BackupRestoreDAO.FileName_Blog, jBlogs.toString(), zout);
		
		
		
		//copy actual attached files
		String folderName = BackupRestoreDAO.FolderName_Blog_AttachedFile ;
		zout.putNextEntry(new ZipEntry(folderName));
		zout.closeEntry();
		for(Blog b : blogs){
			for(BlogFile att : b.getAttaches()){
				File f = attFileManager.getAttachedFile(att);
				String newEntryName = backupDAO.getRenamedPath(folderName, att.getId(), att.getFileName());
				zipFile(newEntryName , f, zout);
			}
		}
		
	}
	private void exportAndZipResources(Project proj, ZipOutputStream zout) throws IOException{
		/*		List<Resource> rList = resourceDAO.getResourcesByProject(proj);
		JSONArray jRArr = new JSONArray();
		for(Resource r : rList){
			jRArr.put(r.toJSONObject());
		}
		
		zipString(BackupRestoreDAO.FileName_Resource, jRArr.toString(), zout);
	
		//copy the actual resource files
		String folderName = BackupRestoreDAO.FolderName_Resource_File ;
		zout.putNextEntry(new ZipEntry(folderName));
		zout.closeEntry();
		for(Resource r : rList){
			if(r.getType().equals(ResourceType.File)){
				File f = new File(Config.getString(Config.VIRTUAL_DRIVE) + r.getPath());
				String newEntryName = backupDAO.getRenamedPath(folderName, r.getId(), r.getName());
				zipFile(newEntryName , f, zout);
			}
			
		}
*/	}
	
	private void zipString(String zipEntryName, String text, ZipOutputStream zout) throws IOException{
		zout.putNextEntry(new ZipEntry(zipEntryName)); 
		zout.write(text.getBytes());
        zout.closeEntry();
    }
	private void zipFile(String zipEntryName, File f, ZipOutputStream zout) throws IOException{
		InputStream input = null;
		try{
			input = new FileInputStream(f);
			zout.putNextEntry(new ZipEntry(zipEntryName)); 
			
			int bufferSize = 10240;
			byte[] bytes = new byte[bufferSize];
			int bytesCount = -1;
	        while ((bytesCount = input.read(bytes)) != -1) { 
	        	zout.write(bytes, 0, bytesCount);
	        }
	        zout.closeEntry();
		}
		catch (FileNotFoundException e){
			logger.error(e.getMessage());
			appState.recordErrorMsg(e.getMessage());
		}
		if(input!=null)
			input.close();
	}
    
    
    
    
    
}
