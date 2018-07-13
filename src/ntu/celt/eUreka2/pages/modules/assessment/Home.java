package ntu.celt.eUreka2.pages.modules.assessment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.MathStatistic;
import ntu.celt.eUreka2.internal.Util;
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
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class Home extends AbstractPageAssessment{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<Assessment> assmts;
	@SuppressWarnings("unused")
	@Property
	private Assessment assmt;
	@Property
	private List<User> assessees;
	@SuppressWarnings("unused")
	@Property
	private List<User> assesseesDisplay;
	@SuppressWarnings("unused")
	@Property
	private User tempUser;
	@Property
	private int rowIndex;
	
	//@Persist("flash")
	/*@Persist
	@Property
	private Integer curPageNo;*/
	
	@SessionState
	private AppState appState;
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	
	Object onActivate(String id) {
		pid = id;
		
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", pid));
		
		ProjUser pu = project.getMember(getCurUser());
		if((pu!=null && isAssessee(pu)) 
				|| (pu==null && project.isReference())){
			return linkSource.createPageRenderLinkWithContext(AssesseeHome.class, pid);
		}
		return null;
	}
	String onPassivate() {
		return pid;
	}
	
	@Cached
	public boolean canViewAssessmentGrade(){
		return canViewAssessmentGrade(project);
	}
	
	@Cached
	public boolean canGradeAssessment(){
		return canGradeAssessment(project);
	}
	
	void setupRender() {
		
		if(!canAccessModule(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		assmts =  aDAO.getAssessmentsByProject(project);
		assessees = getAllAssessees(project);
	/*	if(curPageNo==null)
			curPageNo = 1;
		int fromIndex = (curPageNo-1)*getRowsPerPage();
		int toIndex = Math.min(curPageNo*getRowsPerPage(), assessees.size());
		assesseesDisplay = assessees.subList(fromIndex, toIndex);	
	*/
		assesseesDisplay = assessees;
		totalList = new ArrayList<Double>();
	}
	
	
	public int getRowNum(){
	//	return (curPageNo-1)*getRowsPerPage()+ rowIndex + 1;
		return  rowIndex + 1;
		
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public int getTotalSize(){
		return assessees.size();
	}
	
	public int getAssmtColumnWidth(){
		if(assmts.isEmpty())
			return 710;
		
		int width = 974;
		int initwidth = 265;
		int padding = 4;
		int minwidth = 50;
		
		int num = Math.round((width - initwidth)/(assmts.size()) - padding);
		return Math.max(num, minwidth);
	}
	
	
	@Property
	private AssessmentUser assmtUser;
	public AssessmentUser loadAssmtUser(Assessment assmt, User user){
		assmtUser = assmt.getAssmtUser(user);
		return null;
	}
	public boolean hasGraded(){
		if(assmtUser==null)
			return false;
		return true;
	}
	public String getAssmtUserDisplayGradeClass(){
		if(assmtUser==null)
			return null;
		if("-".equals(assmtUser.getTotalScoreDisplay()))
			return null;
		return convertScoreToGradeClass(assmtUser.getTotalScore(), 100); 
	}
	
	public String getAssmtUserDisplayScore(){
		if(assmtUser==null)
			return null;
		return assmtUser.getTotalScoreDisplay();
	}
	public String getAssmtUserScoreTip(){
		if(assmtUser==null)
			return messages.get("click-to-grade");
		if(assmtUser.getTotalGrade()!=null)
			return assmtUser.getTotalGrade() ;
		return assmtUser.getTotalScoreDisplay();
	}
	private List<Double> totalList ;
	private float totalScore;
	private boolean noTotal = true;
	public String loadTotalScore(User u){
		float tScore = 0;
		float tWeight = 0;
		int count = 0;
		for(Assessment a : assmts){
			AssessmentUser au = a.getAssmtUser(u);
			if(au!=null){
				if(!"-".equals(au.getTotalScoreDisplay())){
					tScore += (au.getTotalScore()* a.getWeightage() ); //compute before rounding
					//tScore += (Float.parseFloat(au.getTotalScoreDisplay())* a.getWeightage() ); //rounding before compute
					tWeight += a.getWeightage();
					count++;
				}
			}
		}
		if(tWeight==0){
			totalScore = 0;
		}
		else{
			totalScore = tScore/tWeight;
		}
		if(count>0){
			if (totalList==null)
				totalList = new ArrayList<Double>();
			noTotal = false;
			totalList.add(Double.parseDouble(Util.formatDecimal(totalScore)));
		}
		else{
			noTotal = true;
		}
			
		return null; 
	}
	public String getDisplayTotalScore(){
		if(noTotal)
			return "-";
		return Util.formatDecimal(totalScore); 
	}
	public String getDisplayTotalScoreTip(){
		if(noTotal)
			return "-";
		return Util.formatDecimal(totalScore) ;
	}
	public String getDisplayTotalScoreClass(){
		if(noTotal)
			return null;
		return convertScoreToGradeClass(totalScore, 100);
	}
	
	public StreamResponse onExportXls(String projId) throws IOException {
		project = pDAO.getProjectById(projId);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjID", projId));
		if(!canViewAssessmentGrade())
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		assmts =  aDAO.getAssessmentsByProject(project);
		assessees = getAllAssessees(project);
				
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
		
		cs3.setDataFormat(df.getFormat("dd MMM yyyy"));

		cs4.setDataFormat(df.getFormat("0.0")); 
		
		// set the sheet name in Unicode
		wb.setSheetName(0, "Assessment" );
		
		s.setColumnWidth(0, 3*256);  //width in unit 1/256 character
		s.setColumnWidth(1, 25*256);
	//	s.setColumnWidth(3, 20*256);
	//	s.setColumnWidth(5, 20*256);
	//	s.setColumnWidth(6, 12*256);
	//	s.setColumnWidth(7, 12*256);
		
		//create header row
		r = s.createRow(0);
		c = r.createCell(1);
		c.setCellValue(messages.get("project-label")+":");
		c.setCellStyle(cs);
		c = r.createCell(2);
		c.setCellStyle(cs);
		c.setCellValue(project.getDisplayName() + " - "+ project.getName());
		
		int colNum = 3;
		
		s.addMergedRegion(new CellRangeAddress(2, 4, 0, 0));
		s.addMergedRegion(new CellRangeAddress(2, 4, 1, 1));
		s.addMergedRegion(new CellRangeAddress(2, 4, 2, 2));
		s.addMergedRegion(new CellRangeAddress(2, 2, colNum, (assmts.isEmpty()? colNum : colNum+assmts.size()-1)));
		s.addMergedRegion(new CellRangeAddress(2, 3, colNum+assmts.size(), colNum+assmts.size()));
		s.addMergedRegion(new CellRangeAddress(2, 3, colNum+assmts.size() + 1, colNum+assmts.size() + 1));
		
		//create table headers
		r = s.createRow(2);
		c = r.createCell(0);
		c.setCellStyle(cs2);
		c.setCellValue("");
		c = r.createCell(1);
		c.setCellStyle(cs2);
		c.setCellValue(messages.get("student"));
		c = r.createCell(2);
		c.setCellStyle(cs2);
		c.setCellValue(messages.get("username-label"));
		c = r.createCell(3);
		c.setCellStyle(cs2);
		c.setCellValue(messages.get("assessment"));
		c = r.createCell(colNum+assmts.size());
		c.setCellStyle(cs2);
		c.setCellValue(messages.get("total"));
		c = r.createCell(colNum+assmts.size() + 1);
		c.setCellStyle(cs2);
		c.setCellValue(messages.get("grade"));
		
		
		r = s.createRow(3);
		r2 = s.createRow(4);
		
		for(int i=0; i < assmts.size(); i++){
			c = r.createCell(colNum+i);
			c.setCellStyle(cs2);
			c.setCellValue(assmts.get(i).getShortNameDisplay());
			c = r2.createCell(colNum+i);
			c.setCellStyle(cs2);
			c.setCellValue(assmts.get(i).getWeightage()+"%");
		}
		c = r2.createCell(colNum+assmts.size());
		c.setCellStyle(cs2);
		c.setCellValue("%");
		
		int rownumFirst = 5;
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
			
			for(int j=0; j<assmts.size(); j++){
				c = r.createCell(colNum+j);
				
				if(assmts.get(j).getAssmtUser(u)!=null){
					String tscore = assmts.get(j).getAssmtUser(u).getTotalScoreDisplay();
					if(!"-".equals(tscore))
						c.setCellValue(Double.parseDouble(tscore));
					else
						c.setCellValue("-");
				}
			}
			loadTotalScore(u);
			c = r.createCell(colNum+assmts.size());
			if(!"-".equals(getDisplayTotalScore())){
				c.setCellValue(Double.parseDouble(getDisplayTotalScore()));
			}
			else{
				c.setCellValue("-");
			}
			c = r.createCell(colNum+assmts.size() + 1);
			if(!"-".equals(getDisplayTotalScore())){
				c.setCellValue(convertScoreToGrade(totalScore, 100));
			}
			rownum++;
		}
		r =  s.createRow(rownum);
		c = r.createCell(1);
		c.setCellStyle(cs2);
		c.setCellValue("Mean");
		for(int i=0; i < assmts.size()+1; i++){
			c = r.createCell(colNum+i);
			c.setCellStyle(cs4);
			c.setCellType(Cell.CELL_TYPE_FORMULA);
			CellReference cr1 = new CellReference(rownumFirst, colNum+i);
			CellReference cr2 = new CellReference(rownum-1, colNum+i);
			c.setCellFormula("AVERAGE(" + cr1.formatAsString() + ":" + cr2.formatAsString() + ")");
		}
		r =  s.createRow(rownum+1);
		c = r.createCell(1);
		c.setCellStyle(cs2);
		c.setCellValue("Std Dev");
		for(int i=0; i < assmts.size()+1; i++){
			c = r.createCell(colNum+i);
			c.setCellStyle(cs4);
			c.setCellType(Cell.CELL_TYPE_FORMULA);
			CellReference cr1 = new CellReference(rownumFirst, colNum+i);
			CellReference cr2 = new CellReference(rownum-1, colNum+i);
			c.setCellFormula("STDEV(" + cr1.formatAsString() + ":" + cr2.formatAsString() + ")");
			
		}
		
				
		//write the workbook to outputStream, then convert it to inputStream
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		wb.write(bout);
		ByteArrayInputStream bis = new ByteArrayInputStream(bout.toByteArray());

		
		String fileName = (project.getDisplayName()+"-Assessments").replace(" ", "_")+".xls";
		String contentType = "application/vnd.ms-excel";
		return new ExportFileStreamResponse(bis, fileName, contentType);
	}
	public String getAvg(Assessment assmt){
		int count=0;
		double total=0;
		for(AssessmentUser assmtUser : assmt.getAssmtUsers()){
			if(assmtUser==null) 
				continue;
			String s = assmtUser.getTotalScoreDisplay();
			if(!"-".equals(s)){
				total += Double.parseDouble(s);
				count++;
			}
		}
		if(count==0)
			return "-";
		else
			return Util.formatDecimal(total/count);
	}
	public String getSTDEV(Assessment assmt){
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
			Double[] d = new Double[values.size()];
			values.toArray(d);
			MathStatistic m = new MathStatistic(d);
			return Util.formatDecimal(m.getStdDev());
		}
	}
	public String getTotalAvg(){
		if(totalList.size()==0)
			return "-";
		else{
			Double[] d = new Double[totalList.size()];
			totalList.toArray(d);
			MathStatistic m = new MathStatistic(d);
			return  Util.formatDecimal(m.getMean());
		}
			
	}
	public String getTotalSTDEV(){
		if(totalList.size()==0)
			return "-";
		else{
			Double[] d = new Double[totalList.size()];
			totalList.toArray(d);
			MathStatistic m = new MathStatistic(d);
			return Util.formatDecimal(m.getStdDev());
		}
	}
}
