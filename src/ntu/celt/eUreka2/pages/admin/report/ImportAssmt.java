package ntu.celt.eUreka2.pages.admin.report;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.AssessCriterion;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.Rubric;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

public class ImportAssmt extends AbstractReport {
	@Property
    private UploadedFile file;
    private final char seperator = ',';
	private final int numColumn = 14;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private School searchSchool;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String searchTerm;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String searchCourseCode;
	
	@Property
	private Project _proj;
	@Property
	private List<Project> _projs = new ArrayList<Project>();
	
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private Logger logger;
	@Inject
	private RequestGlobals requestGlobal;
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private AssessmentDAO assmtDAO;
	@Inject
	private Messages messages;
	@Inject
	private SchoolDAO schoolDAO;
	@Inject
	private Request request;

	@Component (id="form")
	private Form form;
	private File savedFile;
	@Property
	private StringValueEncoder stringEncoder = new StringValueEncoder();
	
	
	
	void setupRender() {
		
		if(!canViewAssessmentReport())
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		if(searchSchool==null)
			searchSchool = getDefaultSchool();
	}
	void onPrepare(){
		if(!canViewAssessmentReport())
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		if(searchSchool==null)
			searchSchool = getDefaultSchool();
	}

	
	

	public void onValidateFormFromForm() throws IOException{
		if(! file.getFileName().toLowerCase().endsWith(".csv")){
			form.recordError(messages.format("incorrect-file-extension-x", ".csv"));  
		}
		else{
			String errorMsg = "";
	    	String toSaveFolder = System.getProperty("java.io.tmpdir"); //get OS current temporary directory
	        String prefix = requestGlobal.getHTTPServletRequest().getSession(true).getId();
	    	savedFile = new File(toSaveFolder+ "/"+ prefix + file.getFileName());
	        file.write(savedFile);
	       
	        FileInputStream fis = new FileInputStream(savedFile);
	        BufferedInputStream bis = new BufferedInputStream(fis); //use buffer to improve reading speed
	        DataInputStream in = new DataInputStream(bis);
		    	
	    	CSVReader csvReader = new CSVReader(new InputStreamReader(in), seperator);
	        int row = 1;
	        boolean isValid = true;
	        int critCol = 0;
	        String[] nextLine;
	    	while((nextLine = csvReader.readNext()) != null){
	    		switch (row){
	    		case 1 :
	    			if(nextLine.length != numColumn){
	    				isValid = false;
	    				errorMsg = "Incorrect number of column, Row1";
	    			}
	    			break;
	    		case 2 :
	    			if(nextLine.length == numColumn){
	    				if(!"".equals(nextLine[numColumn-1].trim())){
		    				if(!Util.isInteger(nextLine[numColumn-1])){
		    					isValid = false;
			    				errorMsg = "CritCol must be integer";
		    				}
		    				critCol = Integer.parseInt(nextLine[numColumn-1]);
	    				}
	    			}
	    			else {
	    				isValid = false;
	    				errorMsg = "Incorrect number of column, Row2";
	    			}
	    			break;
	    		case 3 :
	    			if(nextLine.length != (critCol+3) && nextLine.length!=numColumn ){
	    				isValid = false;
	    				errorMsg = "Incorrect number of column, Row3 " ;
	    			}
	    			break;
	    		}
	    		
	    		row++;
	        }	        
	        in.close();
        	
	        if(! isValid){
	        	savedFile.delete();
	        	form.recordError("File is not in correct format, " + errorMsg); 
	        }
		}
	}
	
	@CommitAfter
    public void saveAssessment(DataInputStream in, Project proj) throws IOException{
		
		CSVReader csvReader = new CSVReader(new InputStreamReader(in), seperator);
	    String[] nextLine;
	    nextLine = csvReader.readNext(); //ignore the first row, (the title row)
	    
	    nextLine = csvReader.readNext(); //ignore the second row, (the data row)
	    Assessment assmt = new Assessment();
	    
	    int critRow = 0;
	    int critCol = 0;
	    assmt.
	    setName(
	    		nextLine[0]);
	    assmt.setShortName(nextLine[1]);
	    assmt.setDes(nextLine[2]);
	    if (! "".equals(nextLine[3].trim())){
	    	Date rdate = Util.stringToDate(nextLine[3], "dd.MMM.yyyy");
		    if (rdate == null){
		    	throw new RuntimeException("Invalid date format: rdate ");
		    }
		    assmt.setRdate(rdate);
	    }
	    assmt.setAllowViewGradeCriteria(Boolean.parseBoolean(nextLine[4]));
	    assmt.setAllowViewScoredCriteria(Boolean.parseBoolean(nextLine[5]));
	    assmt.setAllowViewGrade(Boolean.parseBoolean(nextLine[6]));
	    assmt.setAllowViewGradeDetail(Boolean.parseBoolean(nextLine[7]));
	    assmt.setAllowSubmitFile(Boolean.parseBoolean(nextLine[8]));
	    assmt.setPossibleScore(Integer.parseInt(nextLine[9]));
	    if(! "".equals(nextLine[10].trim()) ){
	    	int rubricID = Integer.parseInt(nextLine[10]);
	    	Rubric r = assmtDAO.getRubricById(rubricID);
	    	if (r==null){
	    	//	throw new RuntimeException("Invalid rubric ID : " + rubricID);
	    	}
	    	assmt.setRubric(r);
	    }
	    assmt.setWeightage(Float.parseFloat(nextLine[11]));
	    if(! "".equals(nextLine[12].trim()) ){
	    	critRow = Integer.parseInt(nextLine[12]);
	    }
	    if(! "".equals(nextLine[13].trim()) ){
	    	critCol = Integer.parseInt(nextLine[13]);
	    }


	    //a.setId(id);
	    assmt.setCreator(getCurUser());
	    assmt.setEditor(getCurUser());
	    assmt.setCdate(new Date());
	    assmt.setMdate(new Date());
		
	    
	    nextLine = csvReader.readNext(); //ignore the third row, (the criterion score row)
	    if(nextLine != null){
	    	int[] crionScores = new int[critCol];
	    	//ignore first 3 columns
	    	for(int i=0 ; i < critCol  ; i++){
	    		crionScores[i] = Integer.parseInt(nextLine[i+3]);
	    	}
	    	
	    	while((nextLine = csvReader.readNext()) != null){
	        	AssessCriteria acrit = new AssessCriteria();
	        	acrit.setName(nextLine[0]);
	        	acrit.setDes(nextLine[1]);
	        	acrit.setWeightage(Integer.parseInt(nextLine[2]));
	        	acrit.setAssessment(assmt);
	        	
	        	
	        	for(int i=0 ; i < critCol  ; i++){
		    		AssessCriterion acrion = new AssessCriterion();
		    		acrion.setCriteria(acrit);
	        		acrion.setDes(nextLine[i+3]);
	        		acrion.setScore(crionScores[i]);
	        		acrit.addCriterion(acrion);
	        	}
	        	assmt.addCriteria(acrit);	
	        }
	    }
		    assmt.setProject(proj);
		    assmt.setOrderNumber(assmtDAO.getNextOrderNumber(proj));
		    assmtDAO.addAssessment(assmt);

	}
	
	
	@CommitAfter
    public Object onSuccessFromForm() throws IOException
    {
		DataInputStream in = null;
        int count = 0;
		try{
			_projs = projDAO.searchProjects(searchSchool, searchTerm, searchCourseCode);
			
			for (Project proj : _projs){

				FileInputStream fis = new FileInputStream(savedFile);
		        BufferedInputStream bis = new BufferedInputStream(fis); //use buffer to improve reading speed
		        in = new DataInputStream(bis);;

				
				saveAssessment(in, proj);
				count++;
				
			    in.close();
			}

			savedFile.delete();
			
    	}
		catch (NumberFormatException ex){
    		appState.recordErrorMsg("Invalid Data type, " + ex.getMessage());
    		
    		logger.error(ex.getMessage());
    		
    		in.close();
    		savedFile.delete();
    		return null;
    	}catch (RuntimeException ex){
    		appState.recordErrorMsg("Exception: " + ex.getMessage());
    		
    		logger.error(ex.getMessage());
    		
    		in.close();
    		savedFile.delete();
    		return null;
    	}
    	appState.recordInfoMsg("Successfully import to create "+ count +" Assessments");
		return null;

    }
    

    Object onUploadException(FileUploadException ex)
    {
        form.recordError("Upload exception: " + ex.getMessage());

        return this;
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


	
}
