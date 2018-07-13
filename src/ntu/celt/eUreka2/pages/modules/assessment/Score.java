package ntu.celt.eUreka2.pages.modules.assessment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.AssessCriterion;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentUser;
import ntu.celt.eUreka2.modules.scheduling.ExportFileStreamResponse;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class Score extends AbstractPageAssessment{
	@Property
	private Project project;
	private int aId;
	@Property
	private Assessment assmt;
	@Property
	private List<User> assessees;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@Property
	private AssessmentUser assmtUser;
	
	@SuppressWarnings("unused")
	@Property
	private AssessCriteria tempCrit;
	@SuppressWarnings("unused")
	@Property
	private AssessCriterion tempCriterion; 
	@Property
	private int colIndex;
	@Property
	private int rowIndex;
	
	@Inject
	private AssessmentDAO assmtDAO;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		aId = ec.get(Integer.class, 0);
		
		assmt = assmtDAO.getAssessmentById(aId);
		if(assmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentID", aId));
		
		project = assmt.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {aId};
	}
	
	void setupRender(){
		if(!canViewAssessmentGrade(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		assessees = getAllAssessees(project, assmt);
	}
	
	public int getColNum(){
		
		return colIndex+1;
	}
	public int getRowNum(){
		return rowIndex+1;
	}
	
	public AssessmentUser loadAssmtUser(Assessment assmt, User user){
		assmtUser = assmt.getAssmtUser(user);
		return null; //to return empty string
	}
	
	public boolean hasGraded(){
		if(assmtUser==null)
			return false;
		return true;
	}
	
	public String getTotalAvgGradeClass(){
		
		return convertScoreToGradeClass(getTotalAverageScore(assessees, assmt), 100); 
	}
	public String getTotalGradeClass(){
		if(assmtUser==null)
			return null;
		if("-".equals(assmtUser.getTotalScoreDisplay()))
			return null;
		return convertScoreToGradeClass(assmtUser.getTotalScore(), 100); 
	}
	public String getAverageScoreGMAT(AssessCriteria crit, List<User> assessees, Assessment assmt){
		float total = 0;
		int i =0;
		for(User u : assessees){
			loadAssmtUser(assmt, u);
			if(assmtUser!=null){
				String s = getComputedCritScoreDisplayGMAT(crit, assmtUser);
				if(!"-".equals(s)){
					total += Float.parseFloat(s);
					i++;
				}
			}
		}
		if(i==0)
			return "";
		
		return Util.formatDecimal(total/i);
	}
	public String getAverageScore(AssessCriteria crit, List<User> assessees, Assessment assmt){
		float total = 0;
		int i =0;
		for(User u : assessees){
			loadAssmtUser(assmt, u);
			if(assmtUser!=null){
				String s = getComputedCritScoreDisplay(crit, assmtUser);
				if(!"-".equals(s)){
					total += Float.parseFloat(s);
					i++;
				}
			}
		}
		if(i==0)
			return "";
		
		return Util.formatDecimal(total/i);
	}
	public String getAvgGradeClassGMAT(AssessCriteria crit){
		String avgScore = getAverageScoreGMAT(crit, assessees, assmt);
		if("".equals(avgScore)){
			return null;
		}
		return convertScoreToGradeClass(Float.parseFloat(avgScore), crit.getWeightage()); 
	}
	public String getAvgGradeClass(AssessCriteria crit){
		String avgScore = getAverageScore(crit, assessees, assmt);
		if("".equals(avgScore)){
			return null;
		}
		return convertScoreToGradeClass(Float.parseFloat(avgScore), crit.getWeightage()); 
	}
	public float getTotalAverageScore( List<User> assessees, Assessment assmt){
		float total = 0;
		int i =0;
		for(User u : assessees){
			loadAssmtUser(assmt, u);
			if(assmtUser!=null){
				if(!"-".equals(assmtUser.getTotalScoreDisplay())){
					total += assmtUser.getTotalScore();
					i++;
				}
			}
		}
		if(i==0)
			return 0;
		return total/i;
	}
	public String getTotalAvgScoreDisplay(List<User> assessees, Assessment assmt){
		return Util.formatDecimal(getTotalAverageScore(assessees, assmt));
	}
	
	private int initWidth = 330;
	private int pageWidth = 974;
	private int padding = 4;
	public int getCritNameWidth(){
		int numColum = assmt.getCriterias().size();
		int minwidth = 100;
		if(numColum==0) //in case not-use-rubric
			return 1;
		
		int num = Math.round((pageWidth - initWidth)/numColum - padding);
		return Math.max(num, minwidth);
	}
	public String getTableWidth(){
		int numColum = assmt.getCriterias().size();
		if(numColum==0) //in case not-use-rubric
			return "100%";
		
		int width = initWidth + getCritNameWidth()*numColum + padding ;
		if(pageWidth < width)
			return (width-30) +"px";
		return "100%";
	}
	
	public StreamResponse onExportXls(int aId) throws IOException {
		assmt = assmtDAO.getAssessmentById(aId);
		if(assmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentID", aId));
		
		project = assmt.getProject();
		assessees = getAllAssessees(project,assmt);
		
		// create a new workbook
		Workbook wb = new HSSFWorkbook();
		Sheet s = wb.createSheet();
		Row r = null;
		Row r2 = null;
		Cell c = null;
		
		// create 3 cell styles
		CellStyle cs = wb.createCellStyle();
		CellStyle cs2 = wb.createCellStyle();
		CellStyle cs3 = wb.createCellStyle();
		CellStyle cs4 = wb.createCellStyle();
		CellStyle cs5 = wb.createCellStyle();
		DataFormat df = wb.createDataFormat();
		
		// create 2 fonts objects
		Font f = wb.createFont();
		Font f2 = wb.createFont();
		//set font 1 to 12 point type
		f.setFontHeightInPoints((short) 12);
		f.setBoldweight(Font.BOLDWEIGHT_BOLD);

		//set font 2 to 10 point type
		f2.setFontHeightInPoints((short) 10);
		f2.setBoldweight(Font.BOLDWEIGHT_BOLD);
		
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
		
		// set the sheet name in Unicode
		wb.setSheetName(0, "Assessment" );
		
		List<AssessCriteria> critList = assmt.getCriterias();
		int colNum = 5;  //column number that start of Criteria (vary column)
	
		
		s.setColumnWidth(0, 3*256);  //width in unit 1/256 character
		s.setColumnWidth(1, 25*256);
		s.setColumnWidth(2, 15*256);
		s.setColumnWidth(3, 15*256);
		s.setColumnWidth(4, 15*256);
		for(int i=colNum; i<(colNum+2*critList.size()); i+=2){
			s.setColumnWidth(i, 5*256);
			s.setColumnWidth(i+1, 20*256);
			
		}
	//	s.setColumnWidth(colNum+critList.size(), 20*256);
	//	s.setColumnWidth(colNum+critList.size()+1, 20*256);
	
		//create header row
		r = s.createRow(0);
		c = r.createCell(1);
		c.setCellValue(messages.get("project-label")+":");
		c.setCellStyle(cs);
		c = r.createCell(2);
		c.setCellStyle(cs);
		c.setCellValue(project.getDisplayName() + " - "+ project.getName());
		r = s.createRow(1);
		c = r.createCell(1);
		c.setCellValue(messages.get("assessment-name")+":");
		c.setCellStyle(cs);
		c = r.createCell(2);
		c.setCellStyle(cs);
		c.setCellValue(assmt.getName() );
		
		
	
		s.addMergedRegion(new CellRangeAddress(3, 4, 0, 2)); //(firstRow,lastRow, firstCol, lastCol)
		if (critList.size()>0){
			s.addMergedRegion(new CellRangeAddress(3, 3, colNum, colNum+2*critList.size()-1));
		}
		s.addMergedRegion(new CellRangeAddress(3, 4, colNum+2*critList.size() , colNum+2*critList.size()));
		s.addMergedRegion(new CellRangeAddress(3, 4, colNum+2*critList.size() + 1, colNum+2*critList.size()+1));
		s.addMergedRegion(new CellRangeAddress(3, 4, colNum+2*critList.size() + 2, colNum+2*critList.size()+2));
		for(int i=0; i < critList.size(); i++){
			s.addMergedRegion(new CellRangeAddress(4, 4, colNum+2*i , colNum+2*i+1));
			s.addMergedRegion(new CellRangeAddress(5, 5, colNum+2*i , colNum+2*i+1));
			
		}
		
		//create table headers
		r = s.createRow(3);
		c = r.createCell(0);
		c.setCellStyle(cs2);
		c.setCellValue("");
		if (critList.size()>0){
			c = r.createCell(colNum);
			c.setCellStyle(cs2);
			if(assmt.getRubric() != null)
				c.setCellValue(assmt.getRubric().getName());
		}
		c = r.createCell(colNum + 2*critList.size() );
		c.setCellStyle(cs2);
		c.setCellValue(messages.get("total"));
		c = r.createCell(colNum + 2*critList.size()+1 );
		c.setCellStyle(cs2);
		c.setCellValue(messages.get("grade"));
		c = r.createCell(colNum + 2*critList.size()+2 );
		c.setCellStyle(cs2);
		c.setCellValue(messages.get("comments"));
		
		r = s.createRow(4);
		
		r2 = s.createRow(5);
		c = r2.createCell(0);
		c.setCellStyle(cs2);
		c.setCellValue("");
		c = r2.createCell(1);
		c.setCellStyle(cs2);
		c.setCellValue(messages.get("student"));
		c = r2.createCell(2);
		c.setCellStyle(cs2);
		c.setCellValue(messages.get("username-label"));
		c = r2.createCell(3);
		c.setCellStyle(cs2);
		c.setCellValue("Matric");
		c = r2.createCell(4);
		c.setCellStyle(cs2);
		c.setCellValue("Group");
		
		
		for(int i=0; i < critList.size(); i++){
			c = r.createCell(colNum+2*i);
			c.setCellStyle(cs2);
			c.setCellValue(critList.get(i).getName());
			c = r2.createCell(colNum+2*i);
			c.setCellStyle(cs2);
			c.setCellValue(critList.get(i).getWeightage()+"%");
		}
		c = r2.createCell(colNum+2*critList.size());
		c.setCellStyle(cs2);
		c.setCellValue("100%");
		
		
		
		int rownumFirst = 6;
		int rownum = rownumFirst;
		
		for(int i=0; i<assessees.size(); i++){
			User u = assessees.get(i);
			r =  s.createRow(rownum);
			c = r.createCell(0);
			c.setCellValue(i+1);
			c = r.createCell(1);
			c.setCellValue(u.getDisplayName());
			c = r.createCell(2);
			c.setCellValue(u.getUsername());
			c = r.createCell(3);
			c.setCellValue(u.getExternalKey());
			c = r.createCell(4);
			if(assmt.getGroup()!=null){
				c.setCellValue(getGroupTypeName(assmt.getGroup(), u));				
			}
			
			AssessmentUser assmtUser = assmt.getAssmtUser(u);
			for(int j=0; j<critList.size(); j++){
				c = r.createCell(colNum+ 2*j);
				Cell c2 = r.createCell(colNum+ 2*j+1);
				if(assmtUser!=null){
					String score = "";
					if(assmt.getGmat()){
						score = getComputedCritScoreDisplayGMAT(critList.get(j),assmtUser);
					}
					else{
						score = getComputedCritScoreDisplay(critList.get(j),assmtUser);
					}
					if(!"-".equals(score)){
						c.setCellValue(Double.parseDouble(score));
					}else{
						c.setCellValue("-");
					}
					c.setCellStyle(cs4);
					
					c2.setCellValue(getCritComment(critList.get(j), assmtUser));
					c2.setCellStyle(cs4);
				}
			}
			if(assmtUser!=null){
				c = r.createCell(colNum+ 2*critList.size());
				String score = assmtUser.getTotalScoreDisplay();
				if(!"-".equals(score)){
					c.setCellValue(Double.parseDouble(score));
				}else{
					c.setCellValue("-");
				}
				c.setCellStyle(cs2);
				c = r.createCell(colNum+ 2*critList.size() + 1);
				if(!"-".equals(score)){
					c.setCellValue(convertScoreToGrade(Float.parseFloat(score), 100));
				}
				c = r.createCell(colNum+ 2*critList.size() + 2);
				if(assmtUser.getComments()!=null){
					c.setCellValue(assmtUser.getComments());
				}
			}
			
			rownum++;
		}
		r = s.createRow(rownum);
		c = r.createCell(1);
		c.setCellValue(messages.get("average") + "");
		c.setCellStyle(cs2);
		for(int j=0; j<critList.size(); j++){
			c = r.createCell(colNum + 2*j);
			if(assmt.getGmat()){
				c.setCellValue(getAverageScoreGMAT(critList.get(j), assessees, assmt));
			}
			else{
				c.setCellValue(getAverageScore(critList.get(j), assessees, assmt));
			}
			c.setCellStyle(cs2);
		}
		c = r.createCell(colNum+2*critList.size());
		c.setCellValue(getTotalAvgScoreDisplay(assessees, assmt));
		c.setCellStyle(cs2);
		c = r.createCell(colNum+2*critList.size()+1);
		c.setCellValue(convertScoreToGrade(getTotalAverageScore(assessees, assmt), 100));
		
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
		
		
		//write the workbook to outputStream, then convert it to inputStream
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		wb.write(bout);
		ByteArrayInputStream bis = new ByteArrayInputStream(bout.toByteArray());

		
		String fileName = (project.getDisplayName() + "-Assessment-" + assmt.getName()).replace(" ", "_")+".xls";
		String contentType = "application/vnd.ms-excel";
		return new ExportFileStreamResponse(bis, fileName, contentType);
	}
	
	public String getSTDEV(AssessCriteria acrit, Assessment assmt){
		List<Double> values = new ArrayList<Double>();
		if(assmt.getGmat()){
			for(AssessmentUser assmtUser : assmt.getAssmtUsers()){
				String s = getComputedCritScoreDisplayGMAT(acrit,assmtUser);
				if(!"-".equals(s)){
					values.add(Double.parseDouble(s));
				}
			}
		}
		else{
			for(AssessmentUser assmtUser : assmt.getAssmtUsers()){
				String s = getComputedCritScoreDisplay(acrit,assmtUser);
				if(!"-".equals(s)){
					values.add(Double.parseDouble(s));
				}
			}
		}
		
		if(values.size()==0)
			return "-";
		else{
			return Util.formatDecimal(Util.getStdDev(values));
		}
	}
	
	public String getTotalSTDEV(Assessment assmt){
		List<Double> values = new ArrayList<Double>();
		for(AssessmentUser assmtUser : assmt.getAssmtUsers()){
			if(assmtUser==null) 
				continue;
			String s = assmtUser.getTotalScoreDisplay();
			if(!"-".equals(s)){
				values.add(Double.parseDouble(s));
			}
		}
		
		if(values.size()==0)
			return "-";
		else{
			return Util.formatDecimal(Util.getStdDev(values));
		}
	}

}
