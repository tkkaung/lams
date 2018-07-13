package ntu.celt.eUreka2.pages.admin.report;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.pages.modules.assessment.Score;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;

public class SchoolAssessments extends AbstractReport{
	@SuppressWarnings("unused")
	@Property
	private Assessment assmt;
	@Property
	private List<Assessment> assmts = new ArrayList<Assessment>();
	
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String searchProjName;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String searchName;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private School searchSchool;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String searchTerm;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String searchCourseCode;

	@InjectPage
	private Score pageAssessmentScore;
	
	
	@SessionState
	private AppState appState;
	
	@Inject
	private AssessmentDAO assmtDAO;
	@Inject
	private SchoolDAO schoolDAO;
	@Inject 
	private ProjectDAO projDAO;
	@Inject
	private Messages messages;
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private PropertyConduitSource propertyConduitSource; 
	@Inject
    private Logger logger;
	@InjectComponent
	private Grid grid;
	@Inject
	private Request request;
	@Inject
	private Response response;

    
	void setupRender() {
		
		if(!canViewAssessmentReport())
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		if(searchSchool==null)
			searchSchool = getDefaultSchool();

		assmts = assmtDAO.searchAssessment(searchName, searchSchool, searchTerm, searchCourseCode , null, searchProjName);
	}
	
	void onPrepareForSubmit(){
		if(searchSchool==null)
			searchSchool = getDefaultSchool();
		
		assmts = assmtDAO.searchAssessment(searchName, searchSchool, searchTerm, searchCourseCode, null, searchProjName );
	}	
	
	
	@Property
	private int rowIndex;
	public int getNo(){
		return (grid.getCurrentPage()-1)*grid.getRowsPerPage()+ rowIndex + 1;
	}
	public int getTotalSize() {
		if (assmts == null)
			return 0;
		return assmts.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public BeanModel<Assessment> getModel() {
		BeanModel<Assessment> model = beanModelSource.createEditModel(Assessment.class, messages);
        model.include("name","cdate");
        model.get("name").label(messages.get("assessment-name"));
        model.add("No", null);
        model.add("chkBox", null);
		model.add("projectName", propertyConduitSource.create(Assessment.class, "project.displayName"));
        model.add("creator", propertyConduitSource.create(Assessment.class, "creator.displayName"));
        model.add("rubric", propertyConduitSource.create(Assessment.class, "rubric"));
        model.reorder("No", "chkBox", "projectName", "name","rubric","cdate","creator");
        
        return model;
    }
	
	public SelectModel getCourseCodeModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<String> courseCodeList = projDAO.getCourseCodeList(searchSchool, searchTerm);
			for (String t : courseCodeList) {
				OptionModel optModel = new OptionModelImpl(t, t);
				optModelList.add(optModel);
			}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	@InjectComponent
	private Zone courseCodeZone;
	Object onSelectTerm(){
		String rId = request.getParameter("param");
		searchTerm = rId;
		return courseCodeZone.getBody();
	}
	Object onSelectSchool(){
		String rId = request.getParameter("param");
		if(rId==null){
			searchSchool = null;
		}
		else
			searchSchool = schoolDAO.getSchoolById(Integer.parseInt(rId));
		return courseCodeZone.getBody();
	}
	
	void onSuccessFromFilterForm(){
		//do nothing, only need to reload
	}

	void onSuccessFromAssmtForm(){
		String[] selectedId = request.getParameters("chkBox");
		
		
		final int BUFFER_SIZE = 10240; 
		Set<String> zEntrySet = new HashSet<String>();//to keep track duplicate zipEntry. function add() return true if not already contain the element
		
		ZipOutputStream zipOut = null;
		byte[] buffer = new byte[BUFFER_SIZE];
		response.setHeader("Content-Disposition", "attachment; filename=\"Files.zip\"");
		
		try{
			zipOut = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream("application/zip"), BUFFER_SIZE));
			
			String tempFolderName = Config.getString(Config.VIRTUAL_DRIVE)+"/temp";
			File tempFolder = new File(tempFolderName);
			if(!tempFolder.exists())
				tempFolder.mkdir();
			
			for(String id : selectedId){
				int aId = Integer.parseInt(id);
				Assessment assmt = assmtDAO.getAssessmentById(aId);
				Project proj = assmt.getProject(); 
				
				InputStream input = null;
				
				try{
					StreamResponse xls = pageAssessmentScore.onExportXls(aId);
					
					try{
							input = new BufferedInputStream(xls.getStream(), BUFFER_SIZE);
							String name = proj.getDisplayName() + "_" + assmt.getCreator().getDisplayName() 
								+ assmt.getName() + ".xls";
							name = name.replace(" ", "_").replace(":", "_").replace("\"", "_")
									.replace("*", "_").replace("/", "_").replace("\\", "_");
							
							while(!zEntrySet.add(name)){ //add & check if element exists
								name = Util.appendSequenceNo(name,"_", ".xls");
							}
							zipOut.putNextEntry(new ZipEntry(name));
							for(int length=0; (length = input.read(buffer))>0;){
								zipOut.write(buffer, 0, length);
							}
							zipOut.closeEntry();
						}
						catch(FileNotFoundException e){
							logger.error(e.getMessage());
							e.printStackTrace();
						}
					
				}
				finally{
					if(input!=null) try{ input.close();}catch(IOException e){logger.error(e.getMessage());}
				}
			}
			
			
			
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		finally{
			if(zipOut!=null) try{ zipOut.close();}catch(IOException e){logger.error(e.getMessage());}
		}
	//	return null;
	}	
	
	
	
	
	
	
	
}
