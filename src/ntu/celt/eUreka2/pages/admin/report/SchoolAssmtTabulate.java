package ntu.celt.eUreka2.pages.admin.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentUser;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.scheduling.ExportFileStreamResponse;
import ntu.celt.eUreka2.pages.modules.assessment.Score;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;


public class SchoolAssmtTabulate extends AbstractReport{
	@Property
	private Assessment _assmt;
	@Property
	private List<Assessment> assmts = new ArrayList<Assessment>();
	
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String searchProjName;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String searchName;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private School searchSchool;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String searchTerm;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String searchCourseCode;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private Integer searchRubricID;
	
	
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

    
	void setupRender() {
		
		if(!canViewAssessmentReport())
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		if(searchSchool==null)
			searchSchool = getDefaultSchool();
		
		assmts = assmtDAO.searchAssessment(searchName, searchSchool, searchTerm, searchCourseCode, searchRubricID, searchProjName );
	}
	
	void onPrepareForSubmit(){
		if(searchSchool==null)
			searchSchool = getDefaultSchool();
		
		assmts = assmtDAO.searchAssessment(searchName, searchSchool, searchTerm, searchCourseCode, searchRubricID, searchProjName );
		
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
        model.add("numCrit", propertyConduitSource.create(Assessment.class, "criterias.size()"));
        
        model.reorder("No", "chkBox", "projectName", "name","rubric","numCrit","cdate","creator");
        
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
	public SelectModel getRubricModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<Rubric> rubricList = assmtDAO.searchRubrics(null, null, null, null, searchSchool);
		List<Rubric> rubricList2 = assmtDAO.searchRubricsViaAssmtBySchool(searchSchool);
		for (Rubric r : rubricList){
			if(!rubricList2.contains(r)){
				rubricList2.add(r);
			}
		}
		Collections.sort(rubricList2, new Comparator<Rubric>(){
			@Override
			public int compare(Rubric r1, Rubric r2){
				return r1.getName().compareTo(r2.getName());
			}
		});
		for (Rubric r : rubricList2) {
			if(r != null){
				//OptionModel optModel = new OptionModelImpl(r.getName(), r);
				OptionModel optModel = new OptionModelImpl(r.getName(), r.getId() );
				optModelList.add(optModel);
			}
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
	
	
	@Component(id = "assmtForm")
	private Form assmtForm;
	
	public void onValidateFormFromAssmtForm(){
		String[] selectedId = request.getParameters("chkBox");
		if(selectedId == null)
			return;
			
		int numCrit = -1;
		boolean hasDiff = false;
		for(String id : selectedId){
			int aId = Integer.parseInt(id);
			Assessment assmt = assmtDAO.getAssessmentById(aId);
			if(assmt.getCriterias()!=null){
				if(numCrit == -1 ){
					numCrit = assmt.getCriterias().size();
				}
				else{
					if(numCrit != assmt.getCriterias().size()){
						hasDiff = true;
						break;
					}
				}
			}
		}
		if (hasDiff) {
			assmtForm.recordError("Please select assessments having same number of Criteria," +
					" Ideally should be same rubric.");
		}
		else{
			assmtForm.clearErrors();
		}
	}
	void onSuccessFromFilterForm(){
		//do nothing, only need to reload
	}

	
	Object onSuccessFromAssmtForm(){
		String[] selectedId = request.getParameters("chkBox");
		

		// create a new workbook
		Workbook wb = new HSSFWorkbook();
		Sheet s = wb.createSheet();
		wb.setSheetName(0, "Assessment" );
		s.setZoom(4, 5); // 4/5 = 80%
		Row r = null;
		Row r2 = null;
		Cell c = null;
		
		
		// create 3 cell styles
		CellStyle cs = wb.createCellStyle();
		CellStyle cs2 = wb.createCellStyle();
		CellStyle cs3 = wb.createCellStyle();
		CellStyle cs4 = wb.createCellStyle();
		CellStyle cs5 = wb.createCellStyle();
		CellStyle cs6 = wb.createCellStyle();
		CellStyle cs7 = wb.createCellStyle();
		CellStyle cs8 = wb.createCellStyle();
		DataFormat df = wb.createDataFormat();
		
		// create 2 fonts objects
		Font f = wb.createFont();
		Font f2 = wb.createFont();
		Font f3 = wb.createFont();
		
		//set font 1 to 12 point type
		f.setFontHeightInPoints((short) 12);
		f.setBoldweight(Font.BOLDWEIGHT_BOLD);

		//set font 2 to 10 point type
		f2.setFontHeightInPoints((short) 10);
		f2.setBoldweight(Font.BOLDWEIGHT_BOLD);

		f3.setFontHeightInPoints((short) 10);
		f3.setColor(HSSFColor.WHITE.index);
		
		//set cell stlye
		cs.setFont(f);
		//set the cell format to text see DataFormat for a full list
		cs2.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
		// set the font
		cs2.setFont(f2);
		cs2.setAlignment(CellStyle.ALIGN_CENTER);
		cs2.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		cs2.setWrapText(true);
		
		cs3.setDataFormat(df.getFormat("dd MMM yyyy"));
		cs4.setDataFormat(df.getFormat("0.0"));
		cs5.setWrapText(true);
		
		cs6.setAlignment(CellStyle.ALIGN_CENTER);
		cs6.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		cs6.setFillForegroundColor(HSSFColor.DARK_BLUE.index);
		cs6.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cs6.setFont(f3);
		
		cs7.setBorderBottom(CellStyle.BORDER_THIN);
		cs7.setBorderTop(CellStyle.BORDER_THIN);
		cs7.setBorderRight(CellStyle.BORDER_THIN);
		cs7.setBorderLeft(CellStyle.BORDER_THIN);
		
		cs8.setAlignment(CellStyle.ALIGN_CENTER);
		cs8.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		cs8.setFillForegroundColor(HSSFColor.DARK_BLUE.index);
		cs8.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cs8.setFont(f3);
		cs8.setBorderBottom(CellStyle.BORDER_THIN);
		cs8.setBorderTop(CellStyle.BORDER_THIN);
		cs8.setBorderRight(CellStyle.BORDER_THIN);
		cs8.setBorderLeft(CellStyle.BORDER_THIN);

		int rowPerChar = 24;
		float heightPointPerRow = (float) 12.75;
		
		
		int assmtStartRow = 0;
		ArrayList<Integer> tabulateRowNumList = new ArrayList<Integer>();
		int colNumOverall = 0;
		int colNumEndOverall = 0;
		int numStudentAssessedTotal = 0;
		Rubric rubricLast = null;
		Project projLast = null;
		
		for(String id : selectedId){
			int aId = Integer.parseInt(id);
			Assessment assmt = assmtDAO.getAssessmentById(aId);
			Project proj = assmt.getProject(); 
			List<User> assessees = pageAssessmentScore.getAllAssessees(proj,assmt);
			rubricLast = assmt.getRubric();
			projLast = proj;

			List<AssessCriteria> critList = assmt.getCriterias();
			int colNum = 3;  //column number that start of Criteria (vary column)
			int colNumEnd = colNum+2*critList.size();
			int colNum2 = colNumEnd + 4;
			int colNumEnd2 = colNum2+critList.size();
			colNumOverall = colNum2;
			colNumEndOverall = colNumEnd2;
			

			s.setColumnWidth(0, 3*256);  //width in unit 1/256 character
			s.setColumnWidth(1, 25*256);
			s.setColumnWidth(2, 15*256);
			for(int i=colNum; i<colNumEnd; i+=2){
				s.setColumnWidth(i, 5*256);
				s.setColumnWidth(i+1, 20*256);
			}
			s.setColumnWidth(colNumEnd, 7*256); //Total
			s.setColumnWidth(colNumEnd+1, 8*256); //Grade
			s.setColumnWidth(colNumEnd+2, 25*256); //Comment
			
			s.setColumnWidth(colNumEnd+3, 18*256);
			for(int i=colNum2; i<colNumEnd2; i+=1){
				s.setColumnWidth(i, 25*256);
			}
		
			//create header row
			r = s.getRow(assmtStartRow);
			if(r==null)
				r = s.createRow(assmtStartRow);
			c = r.createCell(1);
			c.setCellValue(messages.get("project-label")+":");
			c.setCellStyle(cs);
			c = r.createCell(2);
			c.setCellStyle(cs);
			c.setCellValue(proj.getDisplayName() + " - "+ proj.getName());
			r = s.getRow(assmtStartRow+1);
			if(r==null)
				r = s.createRow(assmtStartRow+1);
			c = r.createCell(1);
			c.setCellValue(messages.get("assessment-name")+":");
			c.setCellStyle(cs);
			c = r.createCell(2);
			c.setCellStyle(cs);
			c.setCellValue(assmt.getName() );
			
		
			s.addMergedRegion(new CellRangeAddress(assmtStartRow+3, assmtStartRow+4, 0, 2)); //(firstRow,lastRow, firstCol, lastCol)
			if (critList.size()>0){
				s.addMergedRegion(new CellRangeAddress(assmtStartRow+3, assmtStartRow+3, colNum, colNumEnd-1));
			}
			s.addMergedRegion(new CellRangeAddress(assmtStartRow+3, assmtStartRow+4, colNumEnd , colNumEnd));
			s.addMergedRegion(new CellRangeAddress(assmtStartRow+3, assmtStartRow+4, colNumEnd + 1, colNumEnd+1));
			s.addMergedRegion(new CellRangeAddress(assmtStartRow+3, assmtStartRow+4, colNumEnd + 2, colNumEnd+2));
			for(int i=0; i < critList.size(); i++){
				s.addMergedRegion(new CellRangeAddress(assmtStartRow+4, assmtStartRow+4, colNum+2*i , colNum+2*i+1));
				s.addMergedRegion(new CellRangeAddress(assmtStartRow+5, assmtStartRow+5, colNum+2*i , colNum+2*i+1));
			}
			
			
			s.addMergedRegion(new CellRangeAddress(assmtStartRow+3, assmtStartRow+4, colNumEnd + 3, colNumEnd+3));
			if (critList.size()>0){
				s.addMergedRegion(new CellRangeAddress(assmtStartRow+3, assmtStartRow+3, colNum2, colNumEnd2-1));
				s.addMergedRegion(new CellRangeAddress(assmtStartRow+5, assmtStartRow+5, colNum2, colNumEnd2-1));
			}
			
			
			//create table headers
			r = s.createRow(assmtStartRow+3);
			c = r.createCell(0);
			c.setCellStyle(cs2);
			c.setCellValue("");
			if (critList.size()>0){
				c = r.createCell(colNum);
				c.setCellStyle(cs2);
				if(assmt.getRubric() != null)
					c.setCellValue(assmt.getRubric().getName());
			}
			c = r.createCell(colNumEnd );
			c.setCellStyle(cs2);
			c.setCellValue(messages.get("total"));
			c = r.createCell(colNumEnd+1 );
			c.setCellStyle(cs2);
			c.setCellValue(messages.get("grade"));
			c = r.createCell(colNumEnd+2 );
			c.setCellStyle(cs2);
			c.setCellValue(messages.get("comments"));
			
			c = r.createCell(colNumEnd+3 );
			c.setCellStyle(cs6);
			c.setCellValue("AO TABULATION");
			
			if (critList.size()>0){
				c = r.createCell(colNum2);
				c.setCellStyle(cs2);
				if(assmt.getRubric() != null)
					c.setCellValue(assmt.getRubric().getName());
			}
			
			
			
			r = s.createRow(assmtStartRow+4);
			
			r2 = s.createRow(assmtStartRow+5);
			c = r2.createCell(0);
			c.setCellStyle(cs2);
			c.setCellValue("");
			c = r2.createCell(1);
			c.setCellStyle(cs2);
			c.setCellValue(messages.get("student"));
			c = r2.createCell(2);
			c.setCellStyle(cs2);
			c.setCellValue(messages.get("username-label"));
			
			
			int maxHeightCritName = 1;
			for(int i=0; i < critList.size(); i++){
				c = r.createCell(colNum+2*i);
				c.setCellStyle(cs2);
				c.setCellValue(critList.get(i).getName());
				int len = critList.get(i).getName().length();
				maxHeightCritName =  Math.max(maxHeightCritName, (int) Math.ceil((double)len/rowPerChar));
				c = r2.createCell(colNum+2*i);
				c.setCellStyle(cs2);
				c.setCellValue(critList.get(i).getWeightage()+"%");
			}
			r.setHeightInPoints(maxHeightCritName * heightPointPerRow);
			c = r2.createCell(colNumEnd);
			c.setCellStyle(cs2);
			c.setCellValue("100%");
			
			
			for(int i=0; i < critList.size(); i++){
				c = r.createCell(colNum2+i);
				c.setCellStyle(cs2);
				c.setCellValue(critList.get(i).getName());
			}
			c = r2.createCell(colNum2);
			c.setCellStyle(cs2);
			c.setCellValue("SCALE OF 3");
		
			
			int rownumFirst = assmtStartRow+6;
			int rownum = rownumFirst;
			
			int numStudentAssessed = 0;
			
			for(int i=0; i<assessees.size(); i++){
				User u = assessees.get(i);
				r =  s.createRow(rownum);
				c = r.createCell(0);
				c.setCellValue(i+1);
				c = r.createCell(1);
				c.setCellValue(u.getDisplayName());
				c = r.createCell(2);
				c.setCellValue(u.getUsername());
				
				AssessmentUser assmtUser = assmt.getAssmtUser(u);
				for(int j=0; j<critList.size(); j++){
					c = r.createCell(colNum+ 2*j);
					Cell c2 = r.createCell(colNum+ 2*j+1);
					if(assmtUser!=null){
						String score = "";
						if(assmt.getGmat()){
							score = pageAssessmentScore.getComputedCritScoreDisplayGMAT(critList.get(j),assmtUser);
						}
						else{
							score = pageAssessmentScore.getComputedCritScoreDisplay(critList.get(j),assmtUser);
						}
						if(!"-".equals(score)){
							c.setCellValue(Double.parseDouble(score));
						}else{
							c.setCellValue("-");
						}
						c.setCellStyle(cs4);
						
						c2.setCellValue(pageAssessmentScore.getCritComment(critList.get(j), assmtUser));
						c2.setCellStyle(cs4);
					}
				}
				if(assmtUser!=null){
					c = r.createCell(colNum+ 2*critList.size());
					String score = assmtUser.getTotalScoreDisplay();
					if(!"-".equals(score)){
						c.setCellValue(Double.parseDouble(score));
						numStudentAssessed++;
					}else{
						c.setCellValue("-");
					}
					c.setCellStyle(cs2);
					c = r.createCell(colNum+ 2*critList.size() + 1);
					if(!"-".equals(score)){
						c.setCellValue(pageAssessmentScore.convertScoreToGrade(Float.parseFloat(score), 100));
					}
					c = r.createCell(colNum+ 2*critList.size() + 2);
					if(assmtUser.getComments()!=null){
						c.setCellValue(assmtUser.getComments());
					}
				}
				
				
				for(int j=0; j<critList.size(); j++){
					CellReference cr1 = new CellReference(rownum, colNum+j*2);
					
					if(r.getCell(cr1.getCol()).getCellType() == Cell.CELL_TYPE_NUMERIC){
						c = r.createCell(colNum2+ j);
						c.setCellStyle(cs4);
						c.setCellType(Cell.CELL_TYPE_FORMULA);
						if(critList.get(j).getWeightage() == 0){
							c.setCellFormula("(" + cr1.formatAsString() + "*0)*3");
						}
						else{
							c.setCellFormula("(" + cr1.formatAsString() + "/" + critList.get(j).getWeightage() + ")*3");
						}
					}
				}
				
				rownum++;
			}
			r = s.createRow(rownum);
			c = r.createCell(1);
			c.setCellValue(messages.get("average") + ":");
			c.setCellStyle(cs2);
			for(int j=0; j<critList.size(); j++){
				c = r.createCell(colNum + 2*j);
				if(assmt.getGmat()){
					c.setCellValue(pageAssessmentScore.getAverageScoreGMAT(critList.get(j), assessees, assmt));
				}
				else{
					c.setCellValue(pageAssessmentScore.getAverageScore(critList.get(j),assessees, assmt));
				}
				c.setCellStyle(cs2);
			}
			c = r.createCell(colNumEnd);
			c.setCellValue(pageAssessmentScore.getTotalAvgScoreDisplay(assessees, assmt));
			c.setCellStyle(cs2);
			c = r.createCell(colNumEnd+1);
			c.setCellValue(pageAssessmentScore.convertScoreToGrade(pageAssessmentScore.getTotalAverageScore(assessees, assmt), 100));
			
			r =  s.createRow(rownum+1);
			c = r.createCell(1);
			c.setCellStyle(cs2);
			c.setCellValue("Std Dev");
			for(int i=0; i < critList.size()+1; i++){
				c = r.createCell(colNum+i*2);
				c.setCellStyle(cs4);
				c.setCellType(Cell.CELL_TYPE_FORMULA);
				CellReference cr1 = new CellReference(rownumFirst, colNum+i*2);
				CellReference cr2 = new CellReference(rownum-1, colNum+i*2);
				c.setCellFormula("STDEV(" + cr1.formatAsString() + ":" + cr2.formatAsString() + ")");
			}
				
			
			//-------------tabulate footers
			r =  s.getRow(rownum);
			c = r.createCell(colNumEnd2);
			c.setCellStyle(cs6);
			c.setCellValue("AVERAGE");
			for(int i=0; i < critList.size(); i++){
				c = r.createCell(colNum2+i);
				c.setCellStyle(cs6);
				c.setCellValue("");
			}
			for(int i=rownumFirst-1; i <= rownum; i++){
				r =  s.getRow(i);
				c = r.createCell(colNum2-1);
				c.setCellStyle(cs6);
				c.setCellValue("");
			}
			
			r =  s.getRow(rownum+1);
			c = r.createCell(colNum2-1);
			c.setCellStyle(cs8);
			c.setCellValue("BELOW");
			for(int i=0; i < critList.size(); i++){
				c = r.createCell(colNum2+i);
				c.setCellStyle(cs7);
				c.setCellType(Cell.CELL_TYPE_FORMULA);
				CellReference cr1 = new CellReference(rownumFirst, colNum2+i);
				CellReference cr2 = new CellReference(rownum-1, colNum2+i);
				c.setCellFormula("COUNTIF(" + cr1.formatAsString() +":" +cr2.formatAsString() + ",\"<1.5\")");
			}
			r =  s.createRow(rownum+2);
			c = r.createCell(colNum2-1);
			c.setCellStyle(cs8);
			c.setCellValue("MEET");
			for(int i=0; i < critList.size(); i++){
				c = r.createCell(colNum2+i);
				c.setCellStyle(cs7);
				c.setCellType(Cell.CELL_TYPE_FORMULA);
				CellReference cr1 = new CellReference(rownumFirst, colNum2+i);
				CellReference cr2 = new CellReference(rownum-1, colNum2+i);
				c.setCellFormula("SUMPRODUCT((" + cr1.formatAsString() +":" +cr2.formatAsString() + ">=1.5)*(" 
						+ cr1.formatAsString() +":" +cr2.formatAsString() + "<2.25))");
			}
			r =  s.createRow(rownum+3);
			c = r.createCell(colNum2-1);
			c.setCellStyle(cs8);
			c.setCellValue("ABOVE");
			for(int i=0; i < critList.size(); i++){
				c = r.createCell(colNum2+i);
				c.setCellStyle(cs7);
				c.setCellType(Cell.CELL_TYPE_FORMULA);
				CellReference cr1 = new CellReference(rownumFirst, colNum2+i);
				CellReference cr2 = new CellReference(rownum-1, colNum2+i);
				c.setCellFormula("COUNTIF(" + cr1.formatAsString() +":" +cr2.formatAsString() + ",\">=2.25\")");
			}
			r =  s.createRow(rownum+4);
			c = r.createCell(colNum2-1);
			c.setCellStyle(cs8);
			c.setCellValue("MEAN");
			for(int i=0; i < critList.size(); i++){
				c = r.createCell(colNum2+i);
				c.setCellStyle(cs7);
				c.setCellType(Cell.CELL_TYPE_FORMULA);
				CellReference cr1 = new CellReference(rownum+1, colNum2+i);
				CellReference cr2 = new CellReference(rownum+2, colNum2+i);
				CellReference cr3 = new CellReference(rownum+3, colNum2+i);
				
				c.setCellFormula("(" + cr1.formatAsString() +"+(" +cr2.formatAsString()+ "*2)+("
						+ cr3.formatAsString() + "*3))/SUM("+cr1.formatAsString() 
						+ ":" + cr3.formatAsString() + ")");
			}
			
			for(int j=1; j<=4; j++){
				r =  s.getRow(rownum+j);
				c = r.createCell(colNumEnd2);
				c.setCellStyle(cs7);
				c.setCellType(Cell.CELL_TYPE_FORMULA);
				CellReference cr1 = new CellReference(rownum+j, colNum2);
				CellReference cr2 = new CellReference(rownum+j, colNumEnd2-1);
				c.setCellFormula("AVERAGE("+cr1.formatAsString()+ ":" + cr2.formatAsString() + ")");
			}
			
			r =  s.createRow(rownum+5);
			c = r.createCell(colNum2);
			//c.setCellStyle(cs2);
			c.setCellValue(numStudentAssessed + " STUDENTS ASSESSED");
			
			numStudentAssessedTotal += numStudentAssessed;
			tabulateRowNumList.add(rownum);
			
			assmtStartRow = rownum + 4 ;
		}
		
		if(!tabulateRowNumList.isEmpty()){
			int lastRownum = tabulateRowNumList.get(tabulateRowNumList.size()-1);
			
			r =  s.createRow(lastRownum +7);
			s.addMergedRegion(new CellRangeAddress(lastRownum+7, lastRownum+7, colNumOverall , colNumEndOverall));
			c = r.createCell(colNumEndOverall);
			c.setCellStyle(cs2);
			c.setCellValue("OVERALL");
			
			r =  s.createRow(lastRownum +8);
			c = r.createCell(colNumEndOverall);
			c.setCellStyle(cs6);
			c.setCellValue("AVERAGE");
			for(int i=colNumOverall; i < colNumEndOverall; i++){
				c = r.createCell(i);
				c.setCellStyle(cs6);
				c.setCellValue("");
			}
			
			String metLevel[] = {"BELOW", "MET", "ABOVE"};
			for(int m=0; m < metLevel.length; m++){
				r =  s.createRow(lastRownum+9 + m);
				c = r.createCell(colNumOverall-1);
				c.setCellStyle(cs8);
				c.setCellValue(metLevel[m]);
				for(int i=colNumOverall; i < colNumEndOverall; i++){
					c = r.createCell(i);
					c.setCellStyle(cs7);
					c.setCellType(Cell.CELL_TYPE_FORMULA);
					
					String cellFormula = "";
					int numMaxPer = 30; //max that Excel can support is 30
					int count = 0;
					String crList = "";
					for(Integer rnum: tabulateRowNumList){
						CellReference cr1 = new CellReference(rnum+1 + m, i);
						crList += "," + cr1.formatAsString();
						count++;
						if( count % numMaxPer == 0 ){
							if(crList.length()>0){
								crList = crList.substring(1); //remove the first ","
								cellFormula += "+SUM(" + crList + ")";
								crList = "";
							}
						}
					}
					if(crList.length()>0){
						crList = crList.substring(1); //remove the first ","
						cellFormula += "+SUM(" + crList + ")";
					}
					
					if(cellFormula.length()>0)
						cellFormula = cellFormula.substring(1); //remove the first "+"
					c.setCellFormula(cellFormula);
				}
			}
			
			
			
			r =  s.createRow(lastRownum+12);
			c = r.createCell(colNumOverall-1);
			c.setCellStyle(cs8);
			c.setCellValue("MEAN");
			for(int i=colNumOverall; i < colNumEndOverall; i++){
				c = r.createCell(i);
				c.setCellStyle(cs7);
				c.setCellType(Cell.CELL_TYPE_FORMULA);
				CellReference cr1 = new CellReference(lastRownum+9, i);
				CellReference cr2 = new CellReference(lastRownum+10, i);
				CellReference cr3 = new CellReference(lastRownum+11, i);
				
				c.setCellFormula("(" + cr1.formatAsString() +"+(" +cr2.formatAsString()+ "*2)+("
						+ cr3.formatAsString() + "*3))/SUM("+cr1.formatAsString() 
						+ ":" + cr3.formatAsString() + ")");
			}
			
			for(int j=1; j<=4; j++){
				r =  s.getRow(lastRownum+8+j);
				c = r.createCell(colNumEndOverall);
				c.setCellStyle(cs7);
				c.setCellType(Cell.CELL_TYPE_FORMULA);
				CellReference cr1 = new CellReference(lastRownum+8+j, colNumOverall);
				CellReference cr2 = new CellReference(lastRownum+8+j, colNumEndOverall-1);
				c.setCellFormula("AVERAGE("+cr1.formatAsString()+ ":" + cr2.formatAsString() + ")");
			}
			
			r =  s.createRow(lastRownum+13);
			c = r.createCell(colNumOverall);
			c.setCellValue(numStudentAssessedTotal + " STUDENTS ASSESSED");
			
			
		}
		
		
		
		//write the workbook to outputStream, then convert it to inputStream
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			wb.write(bout);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ByteArrayInputStream bis = new ByteArrayInputStream(bout.toByteArray());
		
				
		String fileName = "AssmtTabulation-" + "PROJ_" + projLast.getName() + 
			(rubricLast==null ? "-ASSMT_" + _assmt.getName() : "-RUB_"+rubricLast.getName()) + ".xls";
		fileName = fileName.replace(" ", "").replace("/", "").replace("\\", "").replace("'", "").replace("\"", "");
		String contentType = "application/vnd.ms-excel";
		return new ExportFileStreamResponse(bis, fileName, contentType);

	}	
	
	
		
	
	
	
	
}
