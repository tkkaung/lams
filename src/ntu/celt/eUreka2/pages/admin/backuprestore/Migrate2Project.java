package ntu.celt.eUreka2.pages.admin.backuprestore;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.annotations.Component;
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
import ntu.celt.eUreka2.modules.backuprestore.Migrate2DAO;
import ntu.celt.eUreka2.modules.backuprestore.ResultSummary;

public class Migrate2Project extends AbstractBackupRestore{
	@Persist
	@Property
	private String projIdToMigrateFrom;
	@Persist
	@Property
	private String projIdToMigrateTo;
	
	@Property
	private Project projFrom;
	@Property
	private Project projTo;
	
	private enum SubmitType {GO, GET_INFO};
	
	private SubmitType submitType ;
	
	@Inject
	private Migrate2DAO migrate2DAO;
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
	
	
	
	void setupRender(){
		if(!canImport()){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		projFrom = migrate2DAO.getProjectById(projIdToMigrateFrom);
		projTo = projDAO.getProjectById(projIdToMigrateTo);
	}
	
	
	void onSelectedFromGO(){
		submitType = SubmitType.GO;
	}
	void onSelectedFromGetInfo(){
		submitType = SubmitType.GET_INFO;
	}
	
	
	@Component
	private Form projForm;
	
	void onValidateFormFromProjForm(){
		if(projIdToMigrateFrom!=null)
			projIdToMigrateFrom = projIdToMigrateFrom.trim();
		
		projFrom = migrate2DAO.getProjectById(projIdToMigrateFrom);
		projTo = projDAO.getProjectById(projIdToMigrateTo);
		
		if(projFrom==null){
			projForm.recordError("Not found project from the Source database");
		}
	}
	
	@CommitAfter
	Object onSuccessFromProjForm(){
		switch(submitType){
		case GET_INFO: 
			break;
		case GO: 
			List<ResultSummary> results = new ArrayList<ResultSummary>();
			ResultSummary result1 = new ResultSummary();
			result1.setMessage("Project" );
			
			if(projTo!=null){
				migrate2DAO.importProjModules(projFrom, projTo);
				
				result1.addAddedList("importProjModules for "+projFrom.getId()+"-->"+projTo.getId());
			}
			else{
				projTo = migrate2DAO.importProjInfo(projFrom, new Project());
				migrate2DAO.importProjModules(projFrom, projTo);
			
				result1.addAddedList(""+projFrom.getId()+"-->"+projTo.getId());
				
			}
			
			results.add(result1);
			resultPage.setResults(results);
			resultPage.setCallBackPageName(messages.get("migrate-project2"));
			resultPage.setCallBackPageLink("admin/backuprestore/migrate2project"); //this page
			
			return resultPage;
			
		}
		return null;
		
	}
	
	
	
	
	
}
