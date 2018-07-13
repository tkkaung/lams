package ntu.celt.eUreka2.pages.admin.backuprestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.MigrateDAO;
import ntu.celt.eUreka2.modules.backuprestore.MigratedProjInfo;
import ntu.celt.eUreka2.modules.backuprestore.ResultSummary;
import ntu.celt.eUreka2.modules.backuprestore.migrateEnities.MProject;

@Import(library="MigrateProject.js")
public class MigrateProject extends AbstractBackupRestore{
	@Persist
	@Property
	private String usernameToMigrate;
	@Property
	private String projIdToMigrateTo;
	
	@SuppressWarnings("unused")
	@Property
	private List<MProject> mprojs;
	@SuppressWarnings("unused")
	@Property
	private MProject mproj;
	
	@Inject
	private MigrateDAO migrateDAO;// = new MigrateDAO();
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	@Inject
	private Logger logger;
	
	@InjectPage
	private ResultPage resultPage;
	
	@Component(id = "selectProjForm")
	private Form selectProjForm;
	
	
	void setupRender(){
		if(!canImport()){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		if(usernameToMigrate==null)
			usernameToMigrate = getCurUser().getUsername();
		
		mprojs = migrateDAO.getProjectsByUser(usernameToMigrate);
		
		
	}
	
	void onSuccessFromUserForm(){
		
	}
	
	void onPrepareFromSelectProjForm(){
		
	}
	
	public boolean isProjNameExist(String projname){
		List<Project> projList = projDAO.getProjectByName(projname);
		if(projList==null || projList.size()==0)
			return false;
		return true;
	}
	public boolean isProjIdExist(String mProjId){
		MigratedProjInfo mProjInfo = migrateDAO.getMigratedProjInfoByVer1ProjId(mProjId);
		if(mProjInfo==null )
			return false;
		return true;
	}
	
	void onValidateFormFromSelectProjForm(){
		String[] selProjId = request.getParameters("projChkBox");
			
		if(selProjId==null){
			selectProjForm.recordError(messages.get("error-must-select-at-least-a-item"));
		}
		
		if(projIdToMigrateTo!=null){
			Project proj = projDAO.getProjectById(projIdToMigrateTo);
			if(proj==null)
				selectProjForm.recordError(messages.format("entity-not-exists",projIdToMigrateTo));
		}
	}
	
	@CommitAfter
	Object onSuccessFromSelectProjForm(){
		List<ResultSummary> results = new ArrayList<ResultSummary>();
		
		String[] mProjIds = request.getParameters("projChkBox");
		for(String mProjId : mProjIds){
			Project proj = null;;
			
			ResultSummary result1 = new ResultSummary();
			result1.setMessage("Project Info " );
			
			//handle override projId
			if(projIdToMigrateTo!=null){
				proj = projDAO.getProjectById(projIdToMigrateTo);
				proj = migrateDAO.importProjMembers(mProjId, proj);
				
				result1.addReplacedList(mProjId +" --> "+ proj.getId());
				results.add(result1);
			}
			else{
				MigratedProjInfo mProjInfo = migrateDAO.getMigratedProjInfoByVer1ProjId(mProjId);
				
				if(mProjInfo==null){
					proj = migrateDAO.importProjInfo(mProjId); 
				}
				else{
					proj = projDAO.getProjectById(mProjInfo.getEureka2ProjId());
				}
				result1.addAddedList(mProjId +" --> "+ proj.getId());
				results.add(result1);
			}
			
			logger.info(mProjId +" --> "+ proj.getId());
			
			
			String[] selCompIds = request.getParameters(mProjId+"_compChkBox");
			for(String compId : selCompIds){
				//available values: annmt, actsk, pfile, dsbrd, wblog, budgt, links, asemt
				if(compId.equalsIgnoreCase("annmt")){
					ResultSummary result = migrateDAO.importAnnouncements(mProjId, proj);
					result.setMessage(messages.get("annmt-label"));
					results.add(result);
				}else if(compId.equalsIgnoreCase("actsk")){
					ResultSummary result = migrateDAO.importActivityTasks(mProjId, proj);
					result.setMessage(messages.get("actsk-label"));
					results.add(result);
				}else if(compId.equalsIgnoreCase("pfile")){
					ResultSummary result = migrateDAO.importFiles(mProjId, proj);
					result.setMessage(messages.get("pfile-label"));
					results.add(result);
				}else if(compId.equalsIgnoreCase("dsbrd")){
					ResultSummary result = migrateDAO.importDiscussionBoards(mProjId, proj);
					result.setMessage(messages.get("dsbrd-label"));
					results.add(result);
				}else if(compId.equalsIgnoreCase("wblog")){
					ResultSummary result = migrateDAO.importWebBlogs(mProjId, proj);
					result.setMessage(messages.get("wblog-label"));
					results.add(result);
				}else if(compId.equalsIgnoreCase("budgt")){
					ResultSummary result = migrateDAO.importBudgets(mProjId, proj);
					result.setMessage(messages.get("budgt-label"));
					results.add(result);
				}else if(compId.equalsIgnoreCase("links")){
					ResultSummary result = migrateDAO.importLinks(mProjId, proj);
					result.setMessage(messages.get("links-label"));
					results.add(result);
				}else if(compId.equalsIgnoreCase("asemt")){
					ResultSummary result = migrateDAO.importAssesments(mProjId, proj);
					result.setMessage(messages.get("asemt-label"));
					results.add(result);
				}
			}
			
			
		
			projDAO.saveProject(proj);
			

			MigratedProjInfo mpi = migrateDAO.getMigratedProjInfo(proj.getId());
			if(mpi==null){
				mpi = new MigratedProjInfo();
				mpi.setEureka1ProjId(mProjId);
				mpi.setEureka2ProjId(proj.getId());
				mpi.setCreateDate(new Date());
				mpi.setCreatorUsername(getCurUser().getUsername());
			}
			mpi.appendlnRemark(getCurUser().getUsername()+"|"+Util.formatDateTime2(new Date()) + "|"
					+ mProjId +"-->"+ proj.getId() +"|"+Arrays.toString(selCompIds));
			
			migrateDAO.saveMigratedProjInfo(mpi);
		}
		
		resultPage.setResults(results);
		resultPage.setCallBackPageName(messages.get("page-title"));
		resultPage.setCallBackPageLink("admin/backuprestore/migrateproject"); //this page
		
		return resultPage;
	
	}
	
}
