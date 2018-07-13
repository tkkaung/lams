package ntu.celt.eUreka2.pages.admin.backuprestore;


import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.backuprestore.MigrateDAO;
import ntu.celt.eUreka2.modules.backuprestore.ResultSummary;

/*
 * Deprecated, use MigrateProject Instead
 */
@Deprecated
public class MigrateForum extends AbstractBackupRestore{
	@Inject
	private MigrateDAO migrateDAO;// = new MigrateDAO();
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private Messages messages;
	
	@Property
	private String projIdFrom = "ADH-CED-12-0002"; //project id in eureka1 
	@Property
	private String projIdTo = "SEN-SAO-12-0001"; //project id in eureka2
	
	@InjectPage
	private ResultPage resultPage;
	
	void setupRender(){
		if(!canImport()){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
	}
	
	void onPrepareFromImportForm(){
		
	}
	
	
	
	@CommitAfter
	Object onSuccessFromImportForm(){
		List<ResultSummary> results = new ArrayList<ResultSummary>();
		
		Project proj = projDAO.getProjectById(projIdTo);
		if(proj==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjId", projIdTo));
		
		ResultSummary result = migrateDAO.importDiscussionBoards(projIdFrom, proj);
		results.add(result);
		
		resultPage.setResults(results);
		resultPage.setCallBackPageName(messages.get("migrate-forum"));
		resultPage.setCallBackPageLink("admin/backuprestore/migrateforum"); //this page
		
		return resultPage;
	}
	
}
