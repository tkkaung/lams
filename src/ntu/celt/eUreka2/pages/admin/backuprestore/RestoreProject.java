package ntu.celt.eUreka2.pages.admin.backuprestore;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.Request;

import ntu.celt.eUreka2.dao.BackupRestoreException;
import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.announcement.Announcement;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.backuprestore.BackupEntry;
import ntu.celt.eUreka2.modules.backuprestore.BackupRestoreDAO;
import ntu.celt.eUreka2.modules.blog.Blog;
import ntu.celt.eUreka2.modules.blog.BlogComment;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.blog.BlogFile;
import ntu.celt.eUreka2.modules.blog.BlogStatus;
import ntu.celt.eUreka2.modules.budget.Budget;
import ntu.celt.eUreka2.modules.budget.BudgetDAO;
import ntu.celt.eUreka2.modules.budget.Transaction;
import ntu.celt.eUreka2.modules.budget.TransactionStatus;
import ntu.celt.eUreka2.modules.budget.TransactionType;
import ntu.celt.eUreka2.modules.forum.Forum;
import ntu.celt.eUreka2.modules.forum.ForumAttachedFile;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.forum.ThreadReply;
import ntu.celt.eUreka2.modules.forum.Thread;
import ntu.celt.eUreka2.modules.message.Message;
import ntu.celt.eUreka2.modules.message.MessageDAO;
import ntu.celt.eUreka2.modules.message.MessageType;
import ntu.celt.eUreka2.modules.resources.ResourceDAO;
import ntu.celt.eUreka2.modules.scheduling.Milestone;
import ntu.celt.eUreka2.modules.scheduling.Phase;
import ntu.celt.eUreka2.modules.scheduling.ReminderType;
import ntu.celt.eUreka2.modules.scheduling.Schedule;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.modules.scheduling.Task;
import ntu.celt.eUreka2.modules.scheduling.UrgencyLevel;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;


/*
 * IMPORTANT : it may be outdated, because bean entitries (e.g Project, User, Blog, ...) has changed,
 *    some field as been added or removed. If you want to use this, you need to trace,
 *    verify, test and make change accordingly.
 */
public class RestoreProject {
	
	
	private String bEntryId;
	@Property
	private BackupEntry bEntry;
	@SuppressWarnings("unused")
	@Property
	private String tempModName;
	
	@SuppressWarnings("unused")
	@Persist("flash")
	@Property
	private boolean showResult;
	@Property
	@Persist("flash")
	private List<String> resultSucceededRestores;
	@Property
	@Persist("flash")
	private List<String> resultFailedRestores;
	
	@Inject
	private Request request;
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
	private UserDAO userDAO;
	@Inject
	private ProjTypeDAO projTypeDAO;
	@Inject
	private ProjRoleDAO projRoleDAO;
	@Inject
	private ProjStatusDAO projStatusDAO;
	@Inject
	private SchoolDAO schoolDAO;
	@Inject
	private BlogDAO blogDAO;
	@Inject
	private ResourceDAO resourceDAO;
	@Inject
	private AttachedFileManager attFileManager;
	
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@SessionState
	private AppState appState;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	void onActivate(EventContext ec) {
		bEntryId = ec.get(String.class, 0);
	}
	Object onPassivate() {
		return bEntryId;
	}
	
	
	
	void setupRender(){
		bEntry = backupDAO.getBackupEntryById(bEntryId);
		if(bEntry == null){
			throw new RecordNotFoundException("BackupEntryID "+bEntryId+" does not exists.");
		}
		Project proj = bEntry.getProject();
		//check privilege
/*		if(!proj.getMember(curUser).hasPrivilege(PrivilegeConstant.PrivilegeSystem) 
			&& !curUser.hasPrivilege(PrivilegeConstant.PrivilegeSystem) ){
			throw new NotAuthorizedAccessException();
		}
*/		
	}
	
	void onPrepareForSubmitFromRestoreForm(){
		bEntry = backupDAO.getBackupEntryById(bEntryId);
	}
	
	Object onSuccessFromRestoreForm()  {
		resultSucceededRestores = new ArrayList<String>();
		resultFailedRestores = new ArrayList<String>();
		
		String[] moduleChkBox = request.getParameters("moduleChkBox");
		if(moduleChkBox!=null){
			for(String mName : moduleChkBox){
				//Module mod = moduleDAO.getModuleByName(mChkboxName);
				
				try{
					importModule(bEntry, mName, getCurUser());
					resultSucceededRestores.add(mName);
				}
				catch(IOException e){
					resultFailedRestores.add(mName);
					e.printStackTrace();
				}
			}
		}
		showResult = true;
		
		return null;
	}
	
	public String getBEntryId(){
		return bEntryId;
	}
	public void setBEntryId(String backupEntryId){
		bEntryId = backupEntryId;
	}
	
	
	
	
	private String getFileContentInsideZipFile(File zipFile, String entryName) throws IOException{
		String str = null;
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(
				new FileInputStream(zipFile)));
		ZipEntry entry;
		try {
			while((entry = zis.getNextEntry()) != null) {
				if(!entry.isDirectory() && entry.getName().equals(entryName)){
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					int BUFFER = 1024;
					int count;
					byte data[] = new byte[BUFFER];
					while ((count = zis.read(data, 0, BUFFER))  != -1) {
						bout.write(data, 0, count);
			        }
					str = bout.toString();
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			zis.close();
		}
		return str;
	}
	
	
	private Date longObjToDate(Object value){
		Long longValue = (long) 0;
		if(value instanceof Long)
			longValue = (Long) value;
		else 
			return null;
		if(longValue==0)
			return null;
		return new Date(longValue);
	}
	
	
	
	///////////Restore////////////////////////////
	public void importModule(BackupEntry bEntry , String moduleName, User user) throws IOException, BackupRestoreException{
		if(PredefinedNames.PROJECT_INFO.equalsIgnoreCase(moduleName)){
			importProjInfo(bEntry);
		}
		else if(PredefinedNames.MODULE_ANNOUNCEMENT.equalsIgnoreCase(moduleName)){
			importAnnouncements(bEntry);
		}
		else if(PredefinedNames.MODULE_ASSESSMENT.equalsIgnoreCase(moduleName)){
			importAssesments(bEntry);
		}
		else if(PredefinedNames.MODULE_BLOG.equalsIgnoreCase(moduleName)){
			importBlogs(bEntry);
		}
		else if(PredefinedNames.MODULE_BUDGET.equalsIgnoreCase(moduleName)){
			importBudgets(bEntry);
		}
		else if(PredefinedNames.MODULE_FORUM.equalsIgnoreCase(moduleName)){
			importForums(bEntry);
		}
		else if(PredefinedNames.MODULE_MESSAGE.equalsIgnoreCase(moduleName)){
			importMessages(bEntry, user); 
		}
		else if(PredefinedNames.MODULE_RESOURCE.equalsIgnoreCase(moduleName)){
			importResources(bEntry);
		}
		else if(PredefinedNames.MODULE_SCHEDULING.equalsIgnoreCase(moduleName)){
			importSchedulings(bEntry);
		}
		
		else{
			throw new BackupRestoreException("Undefined method to restore the Module, moduleName="+moduleName);
		}
	}
	
	
	private void importProjInfo(BackupEntry bEntry) throws IOException{
		String fileName = BackupRestoreDAO.FileName_ProjInfo;  
		String str = getFileContentInsideZipFile(attFileManager.getAttachedFile(bEntry.getAttachedFile()), fileName);
		JSONObject o = new JSONObject(str);
				
		Project p = projDAO.getProjectById(o.getString("id").trim());
	/*	if(p==null){
			p = new Project();  //note: can't create new project as it will generate new ID, other module import functions depend on this ID
		}
		p.setId(o.getString("id"));
	*/	
		p.setName(o.getString("name"));
		p.setDescription(Util.filterOutRestrictedHtmlTags(o.getString("description")));
		p.setCreator(userDAO.getUserById(o.getInt("creator")));
		p.setCdate(longObjToDate(o.get("cdate")));
		p.setSdate(longObjToDate(o.get("sdate")));
		p.setEdate(longObjToDate(o.get("edate")));
		p.setType(projTypeDAO.getTypeById(o.getInt("type")));
		p.setCourseId(o.has("courseId")? o.getString("courseId") : null); 
		p.setSchool(schoolDAO.getSchoolById(o.getInt("school")));
		p.setStatus(projStatusDAO.getStatusById(o.getInt("status")));
		p.setLastStatusChange(longObjToDate(o.get("lastStatusChange")));
		p.setMdate(longObjToDate(o.get("mdate")));
		p.setShared(o.getBoolean("shared"));
		p.setRemarks(o.has("remarks")? o.getString("remarks") : null);
		JSONArray projMems = o.getJSONArray("members");
		p.getMembers().clear();
		for(int i=0; i<projMems.length(); i++){
			JSONObject jpu = projMems.getJSONObject(i);
			ProjUser pu = new ProjUser();
			pu.setProject(p);
			pu.setUser(userDAO.getUserById(jpu.getInt("user")));
			pu.setRole(projRoleDAO.getRoleById(jpu.getInt("role")));
			p.addMember(pu);
		}
		JSONArray projMods = o.getJSONArray("projmodules");
		p.getProjmodules().clear();
		for(int i=0; i<projMods.length(); i++){
			JSONObject jpm = projMods.getJSONObject(i);
			ProjModule pm = new ProjModule();
			pm.setProject(p);
			pm.setModule(moduleDAO.getModuleById(jpm.getInt("module")));
			p.addModule(pm);
		}
		
		//if project exist, update it. if project not exist, create new
		projDAO.saveProject(p);
		
		
	}
	
	private void importAnnouncements(BackupEntry bEntry) throws IOException{
		String fileName = BackupRestoreDAO.FileName_Announcement;  
		String str = getFileContentInsideZipFile(attFileManager.getAttachedFile(bEntry.getAttachedFile()), fileName);
		
		List<Announcement> oldAnnmtList = announcementDAO.searchAnnouncements(bEntry.getProject(), null, null);
		List<Announcement> newAnnmtList = new ArrayList<Announcement>();
		
		JSONArray jArr = new JSONArray(str);
		for(int i=0; i<jArr.length(); i++){
			JSONObject o = jArr.getJSONObject(i);
			Announcement a = announcementDAO.getAnnouncementById(o.getLong("id"));
			if(a==null){
				a = new Announcement();
			}
			//a.setId(o.getLong("id"));
			a.setSubject(o.getString("subject"));
			a.setContent(o.getString("content"));
			a.setStartDate(o.has("startDate")? longObjToDate(o.get("startDate")) : null);
			a.setEndDate(o.has("endDate")? longObjToDate(o.get("endDate")) : null);
			a.setUrgent(o.getBoolean("urgent"));
			a.setCreateDate(longObjToDate(o.get("createDate")));
			a.setModifyDate(longObjToDate(o.get("modifyDate")));
			a.setCreator(userDAO.getUserById(o.getInt("creator")));
			a.setProject(projDAO.getProjectById(o.getString("project")));
			newAnnmtList.add(a);
			announcementDAO.save(a);
		}
		
		//remove old announcements 
		for(Announcement oldAnnmt : oldAnnmtList){
			if(!newAnnmtList.contains(oldAnnmt)){
				announcementDAO.delete(oldAnnmt);
			}
		}
	}
	
	
	private void importForums(BackupEntry bEntry) throws IOException{
		String fileName = BackupRestoreDAO.FileName_Forum;
		File zipFile = attFileManager.getAttachedFile(bEntry.getAttachedFile());
		String str = getFileContentInsideZipFile(zipFile, fileName);
		
		List<Forum> oldForums = forumDAO.getActiveForums(bEntry.getProject());
		List<Forum> newForums = new ArrayList<Forum>();
		
		JSONArray jArr = new JSONArray(str);
		for(int i=0; i<jArr.length(); i++){
			JSONObject o = jArr.getJSONObject(i);
			Forum f = forumDAO.getForumById(o.getLong("id"));
			
			
		//	f.setId(o.getLong("id"));  //if create new, but we set the id, it causes "detached entity passed to persist", 
			f.setName(o.getString("name"));
			f.setDescription(o.getString("description"));
			f.setProject(projDAO.getProjectById(o.getString("project")));
	
			f.setCreator(userDAO.getUserById(o.getInt("creator")));
			f.setCreateDate(longObjToDate(o.get("createDate")));
			
			f.getThreads().clear();
			JSONArray ths = o.getJSONArray("threads");
			for(int j=0; j<ths.length(); j++){
				JSONObject oTh = ths.getJSONObject(j);
				Thread th = jsonObjectToThread(oTh, null);
				th.setForum(f);
				f.addThread(th);
				List<ThreadReply> descendants = new ArrayList<ThreadReply>();
	/*			th.getDescendants(descendants);
				for(ThreadReply ch : descendants){
					ch.setForum(f);
					f.addThread(ch);
				}
	*/		}
			
			newForums.add(f);
			forumDAO.saveForum(f);
		}
		//copy the actual attached Files
		for(Forum f : newForums){
			for(Thread th : f.getThreads()){
				for(ForumAttachedFile att : th.getAttachedFiles()){
					//copy the actual attached file
					String entryName = backupDAO.getRenamedPath(BackupRestoreDAO.FolderName_Forum_AttachedFile, att.getId(),att.getFileName());
					String newPath = attFileManager.copyFileFromZipToRepo(zipFile, entryName, att.getId(), PredefinedNames.MODULE_FORUM, bEntry.getProject().getId());
					att.setPath(newPath);
					forumDAO.saveAttachFile(att);
				}
			}
		}
	
		//remove old forums 
		for(Forum oldForum : oldForums){
			if(!newForums.contains(oldForum)){
				forumDAO.deleteForum(oldForum);
				//then delete attached files
				for(Thread th : oldForum.getThreads()){
					for(ForumAttachedFile att : th.getAttachedFiles()){
						attFileManager.removeAttachedFile(att);
					}
				}
			}
		}
	}
	private Thread jsonObjectToThread(JSONObject o, Thread parent){
		Thread th = forumDAO.getThreadById(o.getLong("id"));
		if(th==null){
			th = new Thread();
		}
		//TODO jsonObjectToThread
/*		//th.setId(o.getLong("id"));
		th.setName(o.getString("name"));
		th.setMessage(o.getString("message"));
		th.setAuthor(userDAO.getUserById(o.getInt("author")));
		th.setCreateDate(longObjToDate(o.get("createDate")));
		th.setModifyDate(longObjToDate(o.get("modifyDate")));
		th.setParent(parent);
		th.setRoot((parent==null || parent.getRoot()==null)? parent : parent.getRoot());
	//	th.setParent(o.has("parent")? forumDAO.getThreadById(o.getLong("parent")) : null);
	//	th.setRoot(o.has("root")? forumDAO.getThreadById(o.getLong("root")) : null);
		
		
		
		JSONArray chs = o.getJSONArray("children");
		th.getChildren().clear();
		for(int j=0; j<chs.length(); j++){
			ThreadReply ch = jsonObjectToThread(chs.getJSONObject(j), th);
			th.addChildren(ch);
		}
		
		JSONArray tus = o.getJSONArray("threadUsers");
		for(int j=0; j<tus.length(); j++){
			ThreadUser tu = new ThreadUser();
			tu.setThread(th);
			tu.setNumView(tus.getJSONObject(j).getInt("numView"));
			tu.setRate(tus.getJSONObject(j).has("rate")?  RateType.valueOf(tus.getJSONObject(j).getString("rate")): null);
			tu.setUser(userDAO.getUserById(tus.getJSONObject(j).getInt("user")));
			tu.setFlag(tus.getJSONObject(j).getBoolean("flag"));
			tu.setRead(tus.getJSONObject(j).getBoolean("read"));
			th.addThreadUsers(tu);
		}
		
		//th.setForum(forumDAO.getForumById(o.getLong("forum"))); //no need to set, as it is done later in caller method
		th.setType(ThreadType.valueOf(o.getString("type")));
		JSONArray refs = o.getJSONArray("reflections");
		
		JSONArray atts = o.getJSONArray("attachedFiles");
		th.getAttachedFiles().clear();
		for(int j=0; j<atts.length(); j++){
			ForumAttachedFile att = jsonObjectToAttachedFile(atts.getJSONObject(j), th);
			att.setThread(th);
			th.addAttachFile(att);
		}
*/		return th;
	}
	
	private ForumAttachedFile jsonObjectToAttachedFile(JSONObject o, ThreadReply th){
		ForumAttachedFile att = forumDAO.getAttachedFileById(o.getString("id"));
		if(att==null){
			att = new ForumAttachedFile();
		}
	//	att.setThread(forumDAO.getThreadById(o.getLong("thread")));
		if(th.getId()==null) //th was newly created
			att.setId(Util.generateUUID());
		else
			att.setId(o.getString("id"));
		
		
		att.setFileName(o.getString("fileName"));
		att.setAliasName(o.has("aliasName")? o.getString("aliasName") : null);
		att.setPath(o.getString("path"));
		att.setContentType(o.getString("contentType"));
		att.setSize(o.getLong("size"));
		
		return att;
	}
	
	
	
	private void importBudgets(BackupEntry bEntry) throws IOException{
		String fileName = BackupRestoreDAO.FileName_Budget;
		File zipFile = attFileManager.getAttachedFile(bEntry.getAttachedFile());
		String str = getFileContentInsideZipFile(zipFile, fileName);
		
		JSONObject o = new JSONObject(str);
		Budget b = budgetDAO.getBudgetById(o.getLong("id"));
		if(b==null){
			b = new Budget();
		}
		//b.setId(o.getLong("id"));
		b.setProject(projDAO.getProjectById(o.getString("project")));
		b.setCreateDate(longObjToDate(o.get("createDate")));
			//deactivate the current budget
			Budget curActiveBudget = budgetDAO.getActiveBudget(b.getProject());
			curActiveBudget.setActive(false);
			budgetDAO.saveBudget(curActiveBudget);
		b.setActive(true);
		b.setRemarks(o.has("remarks")? o.getString("remarks") :"");
		b.getTransactions().clear();
		JSONArray trans = o.getJSONArray("transactions");
		for(int j=0; j<trans.length(); j++){
			Transaction t = jsonObjectToTransaction(trans.getJSONObject(j));
			b.addTransaction(t);
			t.setBudget(b);
		}
		
		budgetDAO.saveBudget(b);
	}
	private Transaction jsonObjectToTransaction(JSONObject o){
		Transaction t = budgetDAO.getTransactionById(o.getLong("id"));
		if(t==null)
			t = new Transaction();
		
		//t.setId(o.getLong("id"));
		t.setDescription(o.getString("description"));
		t.setTransactDate(longObjToDate(o.get("transactDate")));
		t.setCreateDate(longObjToDate(o.get("createDate")));
		t.setModifyDate(o.has("modifyDate")? longObjToDate(o.get("modifyDate")) : null);
		t.setCreator(userDAO.getUserById(o.getInt("creator")));
		t.setAmount(o.getDouble("amount"));
		t.setType(TransactionType.valueOf(o.getString("type")));
		t.setStatus(TransactionStatus.valueOf(o.getString("status")));
		t.setRemarks(o.has("remarks")? o.getString("remarks"):null);
		
		return t;
	}
	
	
	private void importSchedulings(BackupEntry bEntry) throws IOException{
		String fileName = BackupRestoreDAO.FileName_Scheduling;
		File zipFile = attFileManager.getAttachedFile(bEntry.getAttachedFile());
		String str = getFileContentInsideZipFile(zipFile, fileName);
		
		JSONObject o = new JSONObject(str);
		Schedule oldSchd = schedulingDAO.getActiveSchedule(bEntry.getProject());
		Schedule s = schedulingDAO.getScheduleById(o.getLong("id"));
		if(s==null)
			s = new Schedule();
		s.setProject(bEntry.getProject());
		s.setCreateDate(longObjToDate(o.get("createDate")));
		s.setUser(userDAO.getUserById(o.getInt("user")));
	/*		//deactivate current schedule
			Schedule curSchd = schedulingDAO.getActiveSchedule(bEntry.getProject());
			curSchd.setActive(false);
			schedulingDAO.saveSchedule(curSchd);
	*/	s.setActive(true);
		s.setRemarks(o.has("remarks")? o.getString("remarks"):null);
		s.getMilestones().clear();
		JSONArray jMilestones = o.getJSONArray("milestones");
		for(int i=0; i<jMilestones.length(); i++){
			Milestone m = jsonObjectToMilestone(jMilestones.getJSONObject(i));
			s.addMilestone(m);
			m.setSchedule(s);
		}
		schedulingDAO.saveSchedule(s);
		if(oldSchd!=s)
			schedulingDAO.deleteSchedule(oldSchd);
	}
	private Milestone jsonObjectToMilestone(JSONObject o){
		Milestone m = schedulingDAO.getMilestoneById(o.getLong("id")); 
		if(m==null)
			m = new Milestone();
		
		m.setName(o.getString("name"));
		m.setUrgency(UrgencyLevel.valueOf(o.getString("urgency")));
		m.setComment(o.has("comment")? o.getString("comment"):null);
		m.setDeadline(longObjToDate(o.get("deadline")));
		m.setManager(userDAO.getUserById(o.getInt("manager")));
		m.setCreateDate(longObjToDate(o.get("createDate")));
		//m.setSchedule(schedule);
		m.getPhases().clear();
		m.getTasks().clear();
		JSONArray jPhs = o.getJSONArray("phases");
		for(int i=0; i<jPhs.length(); i++){
			Phase ph = jsonObjectToPhase(jPhs.getJSONObject(i));
			m.addPhase(ph);
			ph.setMilestone(m);
			for(Task t : ph.getTasks()){
				m.addTask(t);
				t.setMilestone(m);
			}
		}
		JSONArray jTasks = o.getJSONArray("tasks");
		for(int i=0; i<jTasks.length(); i++){
			Task t = jsonObjectToTask(jTasks.getJSONObject(i));
			m.addTask(t);
			t.setMilestone(m);
		}
		
		return m;
	}
	private Phase jsonObjectToPhase(JSONObject o){
		Phase ph = schedulingDAO.getPhaseById(o.getLong("id"));
		if(ph==null)
			ph = new Phase();
		
		ph.setName(o.getString("name"));
		ph.setComment(o.has("comment")? o.getString("comment"):null);
		ph.setManager(userDAO.getUserById(o.getInt("manager")));
		ph.setStartDate(longObjToDate(o.get("startDate")));
		ph.setEndDate(longObjToDate(o.get("endDate")));
		//ph.setMilestone(milestone);
		ph.setCreateDate(longObjToDate(o.get("createDate")));
		JSONArray jTasks = o.getJSONArray("tasks");
		for(int i=0; i<jTasks.length(); i++){
			Task t = jsonObjectToTask(jTasks.getJSONObject(i));
			ph.addTask(t);
			t.setPhase(ph);
		}
		
		return ph;
	}
	private Task jsonObjectToTask(JSONObject o){
		Task t = schedulingDAO.getTaskById(o.getLong("id"));
		if(t==null)
			t = new Task();
		t.setName(o.getString("name"));
		t.setUrgency(UrgencyLevel.valueOf(o.getString("urgency")));
		t.setComment(o.has("comment")? o.getString("comment"):null);
		t.getAssignedPersons().clear();
		JSONArray jAssignedPersons = o.getJSONArray("assignedPersons");
		for(int i=0; i<jAssignedPersons.length(); i++){
			t.addAssignedPerson(userDAO.getUserById(jAssignedPersons.getInt(i)));
		}
		t.setPercentageDone(o.getInt("percentageDone"));
		t.setStartDate(longObjToDate(o.get("startDate")));
		t.setEndDate(longObjToDate(o.get("endDate")));
		t.setManager(userDAO.getUserById(o.getInt("manager")));
		t.setCreateDate(longObjToDate(o.get("createDate")));
		t.setModifyDate(o.has("modifyDate")? longObjToDate(o.get("modifyDate")):null);
		t.setCompleteDate(o.has("comleteDate")? longObjToDate(o.get("completeDate")) : null);
	//	t.setPhase(phase);
	//	t.setMilestone(milestone);
		t.setReminderType(ReminderType.valueOf(o.getString("reminderType")));
		t.getDaysToRemind().clear();
		JSONArray jDaysToRemind = o.getJSONArray("daysToRemind");
		for(int i=0; i<jDaysToRemind.length(); i++){
			t.addDaysToRemind(jDaysToRemind.getInt(i));
		}
		
		return t;
	}
	
	private void importMessages(BackupEntry bEntry, User user) throws IOException{
		String fileName = BackupRestoreDAO.FileName_Message;
		File zipFile = attFileManager.getAttachedFile(bEntry.getAttachedFile());
		String str = getFileContentInsideZipFile(zipFile, fileName);
		
		List<Message> oldMsgList = new ArrayList<Message>();
		oldMsgList.addAll(msgDAO.getMessages(user, bEntry.getProject(), MessageType.DRAFT)); 
		oldMsgList.addAll(msgDAO.getMessages(user, bEntry.getProject(), MessageType.INBOX));
		oldMsgList.addAll(msgDAO.getMessages(user, bEntry.getProject(), MessageType.SENT));
		List<Message> newMsgList = new ArrayList<Message>();
		
		JSONArray jArr = new JSONArray(str);
		for(int i=0; i<jArr.length(); i++){
			JSONObject o = jArr.getJSONObject(i);
			Message m = msgDAO.getMessageById(o.getLong("id"));
			if(m==null){
				m = new Message();
			}
			//m.setId(o.getLong("id"));
			m.setSubject(o.has("subject")? o.getString("subject") : null);
			m.setContent(o.has("content")? o.getString("content") : null);
			m.setCreateDate(longObjToDate(o.get("createDate")));
			m.setSendDate(longObjToDate(o.get("sendDate")));
			m.setSender(userDAO.getUserById(o.getInt("sender")));
			m.setOwner(userDAO.getUserById(o.getInt("owner")));
			m.getRecipients().clear();
			JSONArray jRecipts = o.getJSONArray("recipients");
			for(int j=0; j<jRecipts.length(); j++){
				m.addRecipients(userDAO.getUserById(jRecipts.getInt(j)));
			}
			m.getExternalEmails().clear();
			JSONArray jExtEmails = o.getJSONArray("externalEmails");
			for(int j=0; j<jExtEmails.length(); j++){
				m.addExternalEmail(jExtEmails.getString(j));
			}
			m.setFlag(o.getBoolean("flag"));
			m.setHasRead(o.getBoolean("hasRead"));
			m.setType(MessageType.valueOf(o.getString("type")));
			m.setDeleteDate(o.has("deleteDate")? longObjToDate(o.get("deleteDate")):null);
			m.setProj(bEntry.getProject());
			
			newMsgList.add(m);
			msgDAO.saveMessage(m);
		}
		
		//remove old messages
		for(Message oldMsg : oldMsgList){
			if(!newMsgList.contains(oldMsg)){
				msgDAO.deleteMessage(oldMsg);
			}
		}
	}
	
	private void importBlogs(BackupEntry bEntry) throws IOException{
		String fileName = BackupRestoreDAO.FileName_Blog_Config;
		File zipFile = attFileManager.getAttachedFile(bEntry.getAttachedFile());
		String str = getFileContentInsideZipFile(zipFile, fileName);
		
		fileName = BackupRestoreDAO.FileName_Blog;
		zipFile = attFileManager.getAttachedFile(bEntry.getAttachedFile());
		str = getFileContentInsideZipFile(zipFile, fileName);
		List<Blog> newBlogs = new ArrayList<Blog>();
		List<Blog> oldBlogs = blogDAO.getBlogsByProject(bEntry.getProject());
		JSONArray jBlogs = new JSONArray(str);
		for(int i=0; i<jBlogs.length(); i++){
			JSONObject o = jBlogs.getJSONObject(i);
			Blog b = blogDAO.getBlogById(o.getString("id"));
			if(b==null)
				b = new Blog();
			//b.setId(o.getString("id"));
			b.setProject(bEntry.getProject());
			b.setAuthor(userDAO.getUserById(o.getInt("author")));
			b.setSubject(o.getString("subject"));
			b.setContent(o.getString("content"));
			b.setCdate(longObjToDate(o.get("cdate")));
			b.setMdate(o.has("mdate")? longObjToDate("mdate"):null);
			b.setShared(o.getBoolean("shared"));
			b.setStatus(BlogStatus.valueOf(o.getString("status")));
			b.setIp(o.getString("ip"));
			b.setRemarks(o.has("remarks")? o.getString("remarks"):null);
			b.getComments().clear();
			JSONArray jCmmts = o.getJSONArray("comments");
			for(int j=0; j<jCmmts.length(); j++){
				JSONObject jCmmt = jCmmts.getJSONObject(j);
				BlogComment cmmt = jsonObjectToBlogComment(jCmmt);
				b.addComment(cmmt);
				cmmt.setBlog(b);
			}
			b.getAttaches().clear();
			JSONArray jAttchs = o.getJSONArray("attaches");
			for(int j=0; j<jAttchs.length(); j++){
				JSONObject jAttch = jAttchs.getJSONObject(j);
				BlogFile att = jsonObjectToBlogFile(jAttch);
				b.addFile(att);
				att.setBlog(b);
			}
			
			newBlogs.add(b);
			blogDAO.updateBlog(b);
		}
		
		//copy the actual attached Files
		for(Blog b : newBlogs){
			for(BlogFile att : b.getAttaches()){
				String entryName = backupDAO.getRenamedPath(BackupRestoreDAO.FolderName_Blog_AttachedFile, att.getId(),att.getFileName());
				String newPath = attFileManager.copyFileFromZipToRepo(zipFile, entryName, att.getId(), PredefinedNames.MODULE_BLOG, bEntry.getProject().getId());
				att.setPath(newPath);
				blogDAO.updateBlogFile(att);

			}
		}

		//delete old blogs
		for(Blog b : oldBlogs){
			if(!newBlogs.contains(b)){
				//then delete attached files
				for(BlogFile att : b.getAttaches()){
					attFileManager.removeAttachedFile(att);
				}
					
				blogDAO.deleteBlog(b);
			}
		}
	}
	private BlogComment jsonObjectToBlogComment(JSONObject o){
		BlogComment c = blogDAO.getBlogCommentById(o.getInt("id"));
		if(c==null)
			c = new BlogComment();
		//c.setId()
		//c.setBlog(blog)
		c.setAuthor(userDAO.getUserById(o.getInt("author")));
		c.setSubject(o.getString("subject"));
		c.setContent(o.getString("content"));
		c.setCdate(longObjToDate(o.get("cdate")));
		c.setMdate(o.has("mdate")? longObjToDate(o.get("mdate")):null);
		c.setDisabled(o.getBoolean("disabled"));
		c.setIp(o.getString("ip"));
		c.setRemarks(o.has("remarks")? o.getString("remarks"): null);
		
		return c;
	}
	private BlogFile jsonObjectToBlogFile(JSONObject o){
		BlogFile att = blogDAO.getBlogFileById(o.getString("id"));
		if(att==null){
			att = new BlogFile();
		}
		//f.setBlog(blog)
		att.setId(o.getString("id"));
		att.setFileName(o.getString("fileName"));
		att.setAliasName(o.has("aliasName")? o.getString("aliasName") : null);
		att.setPath(o.getString("path"));
		att.setContentType(o.getString("contentType"));
		att.setSize(o.getLong("size"));
		
		return att;
	}
	
	
	private void importAssesments(BackupEntry bEntry) throws IOException{
		//TODO importAssessments
	}
	
	private void importResources(BackupEntry bEntry) throws IOException{
/*		TODO importResources
 		String fileName = BackupRestoreDAO.FileName_Resource;
		File zipFile = attFileManager.getAttachedFile(bEntry.getAttachedFile());
		String str = getFileContentInsideZipFile(zipFile, fileName);
		List<Resource> newResrcs = new ArrayList<Resource>();
		List<Resource> oldResrcs = resourceDAO.getResourcesByProject(bEntry.getProject());
		JSONArray jResrcs = new JSONArray(str);
		for(int i=0; i<jResrcs.length(); i++){
			JSONObject o = jResrcs.getJSONObject(i);
			Resource r = resourceDAO.getResourceById(o.getString("id"));
			if(r==null)
				r = new Resource();
			//r.setId(id)
			r.setName(o.getString("name"));
			r.setType(ResourceType.valueOf(o.getString("type")));
			r.setContent(o.has("content")? o.getString("content") : null);
			r.setPath(o.has("path")? o.getString("path") : null);
			r.setDes(o.has("des")? o.getString("des"):null);
			r.setProj(bEntry.getProject());
			r.setOwner(userDAO.getUserById(o.getInt("owner")));
			r.setCdate(longObjToDate(o.get("cdate")));
			r.setParent(o.has("parent")? resourceDAO.getResourceById(o.getString("parent")):null); 
			r.setShared(o.getBoolean("shared"));
			r.setVersion(o.has("version")? o.getInt("version"):null);
			r.setIp(o.getString("ip"));
			r.setRemarks(o.has("remarks")? o.getString("remarks"):null);
			
			newResrcs.add(r);
			resourceDAO.updateResource(r);
		}
		
		//copy the actual resource files and folder
		for(Resource r : newResrcs){
			if(r.getType().equals(ResourceType.File)){
				String entryName = backupDAO.getRenamedPath(BackupRestoreDAO.FolderName_Resource_File, r.getId(), r.getName());
				String newPath = attFileManager.copyFileFromZipToRepo(zipFile, entryName, r.getId(),  
						PredefinedNames.MODULE_RESOURCE, r.getProj().getId(), r.getParent().getId());
				r.setPath(newPath);
				resourceDAO.updateResource(r);
			}
		}
		
		//delete old resources
		for(Resource r : oldResrcs){
			if(!newResrcs.contains(r)){
				if(r.getType().equals(ResourceType.File)){
					File f = new File(Config.getString(Config.VIRTUAL_DRIVE) + r.getPath());
					f.delete();
				}
				resourceDAO.deleteResource(r);
			}
		}
		
*/	}
	
	
}
