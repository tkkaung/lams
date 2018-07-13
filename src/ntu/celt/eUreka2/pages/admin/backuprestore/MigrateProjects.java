package ntu.celt.eUreka2.pages.admin.backuprestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.MigrateDAO;
import ntu.celt.eUreka2.modules.backuprestore.MigratedProjInfo;
import ntu.celt.eUreka2.modules.backuprestore.ResultSummary;

public class MigrateProjects extends AbstractBackupRestore {
	
	@Inject
	private MigrateDAO migrateDAO;// = new MigrateDAO();
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private Messages messages;
	@Inject
	private Logger logger;
	
	@SuppressWarnings("unused")
	@SessionState
	private AppState appState;
	
	
	@InjectPage
	private ResultPage resultPage;
	
	@Property
	private Integer firstResult = 0;
	@Property
	private Integer maxResult = 1000;
	@Property
	private String whenExistRadio = "Ignore";
	@SuppressWarnings("unused")
	@Property
	private StringValueEncoder stringEncoder = new StringValueEncoder();
	
	@Property
	private String status;
	
	void setupRender(){
		if(!canImport()){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
	}
	
	public long getCountProjs(){
		return migrateDAO.getCountProjs(status);
	}
	
	void onPrepareFromSelectProjForm(){
		
	}
	
	
	void onValidateFormFromSelectProjForm(){
		
	}
	
	@CommitAfter
	Object onSuccessFromSelectProjForm(){
		List<ResultSummary> results = new ArrayList<ResultSummary>();
		
		List<String> mProjIds = migrateDAO.getProjIDsByStatus(status, firstResult, maxResult);
		
		int i = (firstResult==null? 0:(firstResult+0));
		int count = 0;
		ResultSummary result = new ResultSummary();
		for(String mProjId : mProjIds){
			i++;
			
			long counAnnmt = migrateDAO.countAnnouncementToMigrate(mProjId);
			long counSchd = migrateDAO.countActivitiesToMigrate(mProjId);
			long counFile = migrateDAO.countFileToMigrate(mProjId);
			long counDis = migrateDAO.countDiscussionToMigrate(mProjId);
			long counWblog = migrateDAO.countWebblogToMigrate(mProjId);
			long counLink = migrateDAO.countLinkToMigrate(mProjId);
			long totalCount = counAnnmt + counSchd + counFile + counDis + counWblog + counLink;
			
			if(totalCount==0){
				continue;
			}
			
			String msg = null;
			Project proj = null;
			
			MigratedProjInfo mProjInfo = migrateDAO.getMigratedProjInfoByVer1ProjId(mProjId);
			if(mProjInfo==null){ //if not migrated yet
				proj = migrateDAO.importProjInfo(mProjId);
				if(proj==null){
					logger.error("Unexpected error, mProjId="+mProjId);
					continue;
				}
				msg = "i="+i+", AddNew -- "+mProjId +" ---> "+proj.getId();
			//	result.addAddedList(msg);
			}
			else{
				if(whenExistRadio.equalsIgnoreCase("Ignore")){
					msg = "i="+i+", Ignore -- "+mProjId +" ---eureka2.0: "+mProjInfo.getEureka2ProjId();
					result.addIgnoredList(msg);
					logger.info("...."+msg);
					continue;
				}
				else if(whenExistRadio.equalsIgnoreCase("AddModule")){
					proj = projDAO.getProjectById(mProjInfo.getEureka2ProjId());
					if(proj==null){
						logger.error("Unexpected error, mProjId="+mProjId);
						continue;
					}
					msg = "i="+i+", AddModule -- "+mProjId +" ---> "+proj.getId();
					result.addAddedList(msg);
				}
				else if(whenExistRadio.equalsIgnoreCase("Replace")){
					proj = projDAO.getProjectById(mProjInfo.getEureka2ProjId());
					projDAO.deleteProject(proj);
					migrateDAO.deleteMigratedProjInfo(mProjInfo);
					proj = migrateDAO.importProjInfo(mProjId);
					
					if(proj==null){
						logger.error("Unexpected error, mProjId="+mProjId);
						continue;
					}
					msg = "i="+i+", DeleteAndAddNew -- "+mProjId +" ---> "+proj.getId();
					result.addReplacedList(msg);
				}

			}
			
			
			if(counAnnmt>0)
				migrateDAO.importAnnouncements(mProjId, proj);
			if(counSchd>0)
				migrateDAO.importActivityTasks(mProjId, proj);
			if(counFile>0)
				migrateDAO.importFiles(mProjId, proj);
			if(counDis>0)
				migrateDAO.importDiscussionBoards(mProjId, proj);
			if(counWblog>0)
				migrateDAO.importWebBlogs(mProjId, proj);
			if(counLink>0)
				migrateDAO.importLinks(mProjId, proj);
			//migrateDAO.importBudgets(mProjId, proj); //not implemented
			//migrateDAO.importAssesments(mProjId, proj); //not implemented
			
			migrateDAO.assignModuleToProject(PredefinedNames.MODULE_LEARNING_LOG, proj);
			
			
			proj.setRemarks("Migrated from eUreka1.0 on "+Util.formatDateTime2(new Date())+" ID= "+mProjId);
			projDAO.saveProject(proj);
			
			MigratedProjInfo mpi = migrateDAO.getMigratedProjInfo(proj.getId());
			if(mpi==null)
				mpi = new MigratedProjInfo();
			mpi.setCreateDate(new Date());
			mpi.setCreatorUsername(getCurUser().getUsername());
			mpi.setEureka1ProjId(mProjId);
			mpi.setEureka2ProjId(proj.getId());
			migrateDAO.saveMigratedProjInfo(mpi);
			
			count++;
			
			logger.info("count="+count
					+".."+msg
					+"..(Announcement:"+ counAnnmt
					+",Task/Phase:"+ counSchd
					+",File:"+ counFile
					+",Forum:"+ counDis
					+",Blog/Comment:"+ counWblog
					+",Link:"+ counLink
					+")"
			);
		}
		
		
		result.setMessage("Done! "+ count+" projects migrated.");
		results.add(result);
		
		resultPage.setResults(results);
		resultPage.setCallBackPageName(messages.get("migrate-projects"));
		resultPage.setCallBackPageLink("admin/backuprestore/migrateprojects"); //this page
		
		return resultPage;
	
	}
	
			
	private String[] eureka1StatusList = {"Active","Reference","Marked As Deleted","Marked As Archived"};
	private String[] eureka1StatusValues = {"open","ref","del","arc-done"};
	
	public SelectModel getStatusModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for(int i=0; i<eureka1StatusList.length; i++){
			String s = eureka1StatusList[i];
			String sValue = eureka1StatusValues[i];
			optModelList.add(new OptionModelImpl(s, sValue));
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	@InjectComponent
	private Zone countProjZone;
	
	Object onValueChangedFromStatusSelect(String newStatus){ //handle ajax
		status = newStatus;
		return countProjZone.getBody();
	}
	
}
