package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.MathStatistic;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupUser;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriteria;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriterion;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationUser;
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
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

@Import(library="context:lib/js/modalbox.js", stylesheet="context:lib/css/modalbox.css")
public class ViewAverage extends AbstractPageEvaluation{
	@Property
	private Project project;
	
	private long eId;
	@Property
	private Evaluation eval;
	@Property
	private List<User> assessees ;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@SuppressWarnings("unused")
	@Property
	private User colUser;
	@Property
	private List<EvaluationUser> evalUserList = new ArrayList<EvaluationUser>();
	@Property
	private EvaluationUser evalUser;
	
	
	@SuppressWarnings("unused")
	@Property
	private EvaluationCriteria tempCrit;
	@SuppressWarnings("unused")
	@Property
	private EvaluationCriterion tempCriterion; 
	@Property
	private int colIndex;
	@Property
	private int rowIndex;
	@Property
	private int quesIndex;
	@Property
	private String oQuestion;
	
	@Inject
	private EvaluationDAO evalDAO;
	@Inject
	private Messages messages;
	@Inject
    private Logger logger;
	@Inject
	private Request request;
	
	void onActivate(EventContext ec) {
		eId = ec.get(Long.class, 0);
		
		eval = evalDAO.getEvaluationById(eId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "EvaluationID", eId));
		
		project = eval.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {eId};
	}
	
	void setupRender(){
		if(!canManageEvaluation(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		assessees = getAllAssessees(project, eval.getGroup());
	}
	
	public int getColNum(){
		return colIndex+1;
	}
	public int getRowNum(){
		return rowIndex+1;
	}
	
	public String loadEvalUserList(Evaluation eval, User assessee){
		evalUserList = eval.getEvalUsersByAssessee( assessee);
		for(int i=evalUserList.size()-1; i>=0; i--){
			EvaluationUser eu = evalUserList.get(i);
			if(!eu.isSubmited()){
				evalUserList.remove(i);
				continue;
			}
			if(!eval.getAllowSelfEvaluation() && assessee.equals(eu.getAssessor())){
				evalUserList.remove(i);
			}
			else{
			}
		}
		return ""; //to return empty string
	}
	
	public boolean hasGraded(){
		if(evalUserList==null || evalUserList.size()==0)
			return false;
		return true;
	}
	
	
	
	
	public String getCmtStrengthDisplay(){
		String str = "";
		for(EvaluationUser eu : evalUserList){
			if(!eu.getCmtStrengthDisplay().isEmpty())
				str += "<<"+ eu.getAssessor().getDisplayName() + ">> : " + eu.getCmtStrengthDisplay() + "\n";
		}
		return str;
	}
	public String getCmtWeaknessDisplay(){
		String str = "";
		for(EvaluationUser eu : evalUserList){
			if(!eu.getCmtWeaknessDisplay().isEmpty())
				str += "<<"+ eu.getAssessor().getDisplayName() + ">> : " + eu.getCmtWeaknessDisplay() + "\n";
		}
		return str;
	}
	public String getCmtOtherDisplay(){
		String str = "";
		for(EvaluationUser eu : evalUserList){
			if(!eu.getCmtOtherDisplay().isEmpty())
				str += "<<"+ eu.getAssessor().getDisplayName() + ">> : " + eu.getCmtOtherDisplay() + "\n";
		}
		return str;
	}
	public String getOpenEndedQuestionAnswersDisplay(int quesIndex){
		String str = "";
		for(EvaluationUser eu : evalUserList){
			String ans = eu.getOpenEndedQuestionAnswers(quesIndex);
			if(ans!=null && !ans.isEmpty())
				str +=  ans + "\n\n--------------------\n\n";
		}
		return str;
	}
	
	
	
	public float getTotalScore(){
		float total = 0;
		
		if(eval.getUseFixedPoint()){
			float maxStudentPoint = getMaxGroupStudentTotalFixedPoint(groupUser, eval);
			if(maxStudentPoint!=-1)
				total = getStudentTotalFixedPoint(evalUserList) * 100 / maxStudentPoint ;
		}
		else{
			for(EvaluationCriteria ec : eval.getCriterias()){
				total += (double) (getComputedCritScore(ec, evalUserList) * ec.getWeightage())/ec.getMaxScore();
			}
		}
		return total;
	}
	public String getTotalScoreDisplay(){
		if(!hasGraded()){
			return "-";
		}
		return Util.formatDecimal(getTotalScore());
	}
	public String getTotalScoreClass(){
		if(!hasGraded()){
			return null;
		}
		
		return convertScoreToGradeClass(getTotalScore(), 100); 
	}
	
	public float getAverageScore(EvaluationCriteria crit){
		float total = 0;
		int i = 0;
		for(User u : assessees){
			loadEvalUserList(eval, u);
			float score = getComputedCritScore(crit, evalUserList);
			if(score != 0){
				total += score;
				i++;
			}
		}
		if(i==0)
			return 0;
		
		return total/i;
	}
	
	public String getAverageScoreDisplay(EvaluationCriteria crit){
		return Util.formatDecimal(getAverageScore(crit));
	}
	public String getAverageScoreClass(EvaluationCriteria crit){
		return convertScoreToGradeClass(getAverageScore(crit), crit.getMaxScore()); 
	}
	
	public float getTotalAverageScore(){
		float total = 0;
		for(EvaluationCriteria ec : eval.getCriterias()){
			total += (getAverageScore(ec) * ec.getWeightage())/ec.getMaxScore();
		}
		return total;
	}
	public float getTotalAverageScore(Evaluation eval){
		float total = 0;
		for(EvaluationCriteria ec : eval.getCriterias()){
			total += (getAverageScore(ec) * ec.getWeightage())/ec.getMaxScore();
		}
		return total;
	}
	public String getTotalAverageScoreDisplay(){
		return Util.formatDecimal(getTotalAverageScore());
	}
	public String getTotalAverageScoreClass(){
		return convertScoreToGradeClass(getTotalAverageScore(), 100); 
	}
	
	/*public String getTotalAvgGradeClass(){
		return convertScoreToGradeClass(getTotalAverageScore(), 100); 
	}
	public String getTotalGradeClass(){
		if(evalUser==null)
			return null;
		return convertScoreToGradeClass(evalUser.getTotalScore(), 100);
	}
	public String getAverageScore(EvaluationCriteria crit){
		float total = 0;
		int i =0;
		
		for(User u : assessees){
			//loadEvalUser(eval, u);
			if(evalUser!=null){
				total += Float.parseFloat(getComputedCritScore(crit,evalUser));
				i++;
			}
		}
		if(i==0)
			return "";
		
		return Util.formatDecimal(total/i);
	}
	public String getAvgGradeClass(EvaluationCriteria crit){
		String avgScore = getAverageScore(crit);
		if("".equals(avgScore)){
			return null;
		}
		return convertScoreToGradeClass(Float.parseFloat(avgScore), crit.getWeightage()); 
	}
	public float getTotalAverageScore(){
		float total = 0;
		int i =0;
		
		
		for(User u : assessees){
			//loadEvalUser(eval, u);
			if(evalUser!=null){
				total += evalUser.getTotalScore();
				i++;
			}
		}
		if(i==0)
			return 0;
		return total/i;
	}
	public String getTotalAvgScoreDisplay(){
		return Util.formatDecimal(getTotalAverageScore());
	}*/
	
	private int initWidth = 330;
	private int pageWidth = 974;
	private int padding = 4;
	public int getCritNameWidth(){
		int numColum = eval.getCriterias().size();
		int minwidth = 100;
		if(numColum==0) //in case not-use-rubric
			return 1;
		
		int num = Math.round((pageWidth - initWidth)/numColum - padding);
		return Math.max(num, minwidth);
	}
	public String getTableWidth(){
		String defaultWidth = "100%";
		int scrollWidth = 0;
		if(assessees.size()>20){
			defaultWidth = "98%";
			scrollWidth = 30;
		}
		int numColum = eval.getCriterias().size();
		if(numColum==0) //in case not-use-rubric
			return defaultWidth;
		
		int width = initWidth + getCritNameWidth()*numColum + padding ;
		if(pageWidth < width)
			return (width-scrollWidth) +"px";
		return defaultWidth;
	}
	
	void onPrepareForSubmitFromEditStrengthForm(){
		String evalId = request.getParameter("evalId");  //obtain value from 'hidden' input
		eval = evalDAO.getEvaluationById(Long.parseLong(evalId));
	}
	@CommitAfter
	void onSuccessFromEditStrengthForm(){
		eval.setMdate(new Date());
		evalDAO.updateEvaluation(eval);
	}
	void onPrepareForSubmitFromEditWeaknessForm(){
		String evalId = request.getParameter("evalId");  //obtain value from 'hidden' input
		eval = evalDAO.getEvaluationById(Long.parseLong(evalId));
	}
	@CommitAfter
	void onSuccessFromEditWeaknessForm(){
		eval.setMdate(new Date());
		evalDAO.updateEvaluation(eval);
	}
	void onPrepareForSubmitFromEditOtherForm(){
		String evalId = request.getParameter("evalId");  //obtain value from 'hidden' input
		eval = evalDAO.getEvaluationById(Long.parseLong(evalId));
	}
	@CommitAfter
	void onSuccessFromEditOtherForm(){
		eval.setMdate(new Date());
		evalDAO.updateEvaluation(eval);
	}
	
	private enum ExportType { SCORE_ONLY, QF_ONLY, SCORE_N_QF};
	
	public StreamResponse onExportXlsScore(Long eId) throws IOException {
		return onExportXls(eId, ExportType.SCORE_ONLY);
	}
	public StreamResponse onExportXlsQF(Long eId) throws IOException {
		return onExportXls(eId, ExportType.QF_ONLY);
	}
	public StreamResponse onExportXlsScoreNQF(Long eId) throws IOException {
		return onExportXls(eId, ExportType.SCORE_N_QF);
	}
	public StreamResponse onExportXls(Long eId) throws IOException {
		return onExportXls(eId, ExportType.SCORE_N_QF);
	}
	public StreamResponse onExportXls(Long eId, ExportType exportType) throws IOException {
		 eval = evalDAO.getEvaluationById(eId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "EvaluationID", eId));
		
		project = eval.getProject();
		assessees = getAllAssessees(project, eval.getGroup());
				
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
		CellStyle cs6 = wb.createCellStyle();
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
		//cs4.setDataFormat(df.getFormat("#"));
		cs5.setWrapText(true);
		cs6.setDataFormat(df.getFormat("0.0"));
		
		// set the sheet name in Unicode
		wb.setSheetName(0, "Evaluation" );
		
		List<EvaluationCriteria> critList = eval.getCriterias();
		List<String> opquesList = eval.getOpenEndedQuestions();
		int qualNum = getNumQualitativeFeedback(eval);
		int colNum = 5;  //column number that start of Criteria (vary column)
		int numCrit = critList.size();
		
		if(exportType == ExportType.SCORE_ONLY  ){
			qualNum = 0;
		}
		if(exportType == ExportType.QF_ONLY  ){
			numCrit = 0;
			colNum -= 2; //take out "Total" and "Grade"
		}
		
		
		s.setColumnWidth(0, 3*256);  //width in unit 1/256 character
		s.setColumnWidth(1, 25*256);
		s.setColumnWidth(2, 15*256);
		s.setColumnWidth(3, 15*256);
			for(int i=0; i<qualNum; i++){
			s.setColumnWidth(colNum+2+numCrit+i, 20*256);
		}
		for(int i=0; i<opquesList.size(); i++){
			s.setColumnWidth(colNum+2+qualNum+numCrit+i, 40*256); 
		}
		
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
		r = s.createRow(1);
		c = r.createCell(1);
		c.setCellValue(messages.get("evaluation-name")+":");
		c.setCellStyle(cs);
		c = r.createCell(2);
		c.setCellStyle(cs);
		c.setCellValue(eval.getName() );
		
		s.addMergedRegion(new CellRangeAddress(3, 4, 0, 3)); //(firstRow,lastRow, firstCol, lastCol)
		
		if(exportType == ExportType.SCORE_ONLY || exportType == ExportType.SCORE_N_QF ){
			s.addMergedRegion(new CellRangeAddress(3, 3, colNum, colNum+numCrit-1)); //Rubric Name
			s.addMergedRegion(new CellRangeAddress(3, 4, colNum+numCrit + 0, colNum+numCrit+0)); //Total
			s.addMergedRegion(new CellRangeAddress(3, 4, colNum+numCrit + 1, colNum+numCrit+1)); //Grade
		}
		
		if(exportType == ExportType.QF_ONLY || exportType == ExportType.SCORE_N_QF ){
			
			if(qualNum>0)
				s.addMergedRegion(new CellRangeAddress(3, 3, colNum+2+numCrit , colNum+2+numCrit+ qualNum-1)); //Qualitative Feedback
			for(int i=0; i<qualNum; i++){
				s.addMergedRegion(new CellRangeAddress(4, 5, colNum+2+numCrit+i , colNum+2+numCrit+i));   //Strength
			}
			if(opquesList.size()>0)
				s.addMergedRegion(new CellRangeAddress(3, 3, colNum+2+qualNum+numCrit , colNum+2+qualNum+numCrit+ opquesList.size()-1)); //Open Questions
			for(int i=0; i<opquesList.size(); i++){
				s.addMergedRegion(new CellRangeAddress(4, 5, colNum+2+qualNum+numCrit+i , colNum+2+qualNum+numCrit+i)); 
			}
		}
		
		//create table headers
		r = s.createRow(3);
		c = r.createCell(0);
		c.setCellStyle(cs2);
		c.setCellValue("");
		
		if(exportType == ExportType.SCORE_ONLY || exportType == ExportType.SCORE_N_QF ){
			c = r.createCell(colNum);
			c.setCellStyle(cs2);
			c.setCellValue(eval.getRubric().getName());
			c = r.createCell(colNum + numCrit );
			c.setCellStyle(cs2);
			c.setCellValue(messages.get("total"));
			c = r.createCell(colNum + numCrit + 1);
			c.setCellStyle(cs2);
			c.setCellValue(messages.get("grade"));
		}
		if(exportType == ExportType.QF_ONLY || exportType == ExportType.SCORE_N_QF ){
			if(qualNum>0){
				c = r.createCell(colNum+ 2 + numCrit );
				c.setCellStyle(cs2);
				c.setCellValue(messages.get("comment")); //Qualitative feedback
			}
			if(opquesList.size()>0){
				c = r.createCell(colNum + 2 + qualNum + numCrit );
				c.setCellStyle(cs2);
				c.setCellValue(messages.get("open-question-label"));
			}
		}
		
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
		c.setCellValue(messages.get("group"));
		
		
		if(exportType == ExportType.SCORE_ONLY || exportType == ExportType.SCORE_N_QF ){
			for(int i=0; i < numCrit; i++){
				c = r.createCell(colNum+i);
				c.setCellStyle(cs2);
				c.setCellValue(critList.get(i).getName());
				c = r2.createCell(colNum+i);
				c.setCellStyle(cs2);
				c.setCellValue(critList.get(i).getWeightage()+"%");
			}
			c = r2.createCell(colNum+numCrit+0);
			c.setCellStyle(cs2);
			c.setCellValue("100%");
			c = r2.createCell(colNum+numCrit+1);
			c.setCellStyle(cs2);
			c.setCellValue("");
		}
		
		int temp = 0;
		if(exportType == ExportType.QF_ONLY || exportType == ExportType.SCORE_N_QF ){
			if(eval.getUseCmtStrength()){
				c = r.createCell(colNum+2+numCrit+temp);
				c.setCellStyle(cs2);
				c.setCellValue(getCmmtStrengthName(eval));
				temp++;
			}
			if(eval.getUseCmtWeakness()){
				c = r.createCell(colNum+2+numCrit+temp);
				c.setCellStyle(cs2);
				c.setCellValue(getCmmtWeaknessName(eval));
				temp++;
			}
			if(eval.getUseCmtOther()){
				c = r.createCell(colNum+2+numCrit+temp);
				c.setCellStyle(cs2);
				c.setCellValue(getCmmtOtherName(eval));
				temp++;
			}
		
		
			for(int i=0; i<opquesList.size(); i++){
				c = r.createCell(colNum+2+qualNum+numCrit+i);
				c.setCellStyle(cs2);
				c.setCellValue(opquesList.get(i)); 
			}
		}
		
		
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
			c.setCellValue(getGroupTypeName(eval.getGroup(), u));
			
			loadEvalUserList(eval, u);
			loadGroupUser(eval.getGroup(), u);

			if(exportType == ExportType.SCORE_ONLY || exportType == ExportType.SCORE_N_QF ){
				
				for(int j=0; j<numCrit; j++){
					c = r.createCell(colNum+j);
					if(hasGraded()){
						//c.setCellValue(getComputedCritScoreDisplay(critList.get(j), evalUserList));
						c.setCellStyle(cs4);
						
						String score = "";
						score = getComputedCritScoreDisplay(critList.get(j), evalUserList);
						if(!"-".equals(score)){
							c.setCellValue(Double.parseDouble(score));
						}else{
							c.setCellValue("-");
						}
					}
					
				}
				
				c = r.createCell(colNum+numCrit+0);
				//c.setCellValue(getTotalScoreDisplay());
				String score = getTotalScoreDisplay();
				if(!"-".equals(score)){
					c.setCellValue(Double.parseDouble(score));
				}else{
					c.setCellValue("-");
				}
				c.setCellStyle(cs2);
				c = r.createCell(colNum+numCrit + 1);
				String grade = "-";
				if(hasGraded()){
					grade = convertScoreToGrade(getTotalScore(), 100);
				}
				c.setCellValue(grade);
				
			}
			
			temp = 0;
			if(exportType == ExportType.QF_ONLY || exportType == ExportType.SCORE_N_QF ){
				
				if(eval.getUseCmtStrength()){
					c = r.createCell(colNum+2+numCrit+temp);
					c.setCellValue(getCmtStrengthDisplay());
					temp++;
				}
				if(eval.getUseCmtWeakness()){
					c = r.createCell(colNum+2+numCrit+temp);
					c.setCellValue(getCmtWeaknessDisplay());
					temp++;
				}
				if(eval.getUseCmtOther()){
					c = r.createCell(colNum+2+numCrit+temp);
					c.setCellValue(getCmtOtherDisplay());
					temp++;
				}
				for(int j=0; j<opquesList.size(); j++){
					c = r.createCell(colNum+2+qualNum+numCrit+j);
					c.setCellValue(getOpenEndedQuestionAnswersDisplay(j)); 
				}
			}	
			
			
			
			rownum++;
		}
		if(exportType == ExportType.SCORE_ONLY || exportType == ExportType.SCORE_N_QF ){
			r = s.createRow(rownum);
			c = r.createCell(3);
			
			c.setCellValue(messages.get("average") + ":");
			c.setCellStyle(cs2);
			for(int j=0; j<numCrit; j++){
				c = r.createCell(colNum + j);
				c.setCellValue(getAverageScoreDisplay(critList.get(j)));
				c.setCellStyle(cs2);
			}
			c = r.createCell(colNum+numCrit+0);
			c.setCellValue(getTotalAverageScoreDisplay());
			c.setCellStyle(cs2);
			c = r.createCell(colNum+numCrit+1);
			c.setCellValue(convertScoreToGrade(getTotalAverageScore(), 100));
			
			r =  s.createRow(rownum+1);
			c = r.createCell(3);
			c.setCellStyle(cs2);
			c.setCellValue("Std Dev");
			for(int i=0; i < numCrit+1; i++){
				c = r.createCell(colNum+i);
				c.setCellStyle(cs6);
				c.setCellType(Cell.CELL_TYPE_FORMULA);
				CellReference cr1 = new CellReference(rownumFirst, colNum+i);
				CellReference cr2 = new CellReference(rownum-1, colNum+i);
				c.setCellFormula("STDEV(" + cr1.formatAsString() + ":" + cr2.formatAsString() + ")");
			}
		}
		
		//write the workbook to outputStream, then convert it to inputStream
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		wb.write(bout);
		ByteArrayInputStream bis = new ByteArrayInputStream(bout.toByteArray());

		
		String fileName = (project.getDisplayName() + "-Evaluation-" + eval.getName() ).replace(" ", "_")+".xls";
		String contentType = "application/vnd.ms-excel";
		return new ExportFileStreamResponse(bis, fileName, contentType);
	}
	
	public String getSTDEV(EvaluationCriteria acrit, Evaluation eval){
		List<Double> values = new ArrayList<Double>();
		for(User u : assessees){
			loadEvalUserList(eval, u);
			String s = getComputedCritScoreDisplay(acrit,evalUserList);
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
	
	
	public String getTotalSTDEV(Evaluation eval){
		List<Double> values = new ArrayList<Double>();
		for(User u : assessees){
			loadEvalUserList(eval, u);
			String s = getTotalScoreDisplay();
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
	@Cached
	public int getGroupSize(){
		return getGroupSize(eval.getGroup());
	}
	
	public int getColspanRubric(){
		return eval.getCriterias().size() * getColspanCriteria() ;
	}
	public int getColspanCriteria(){
		return  getGroupSize() + 1;
	}
	@Property
	private GroupUser groupUser = new GroupUser();
	public String loadGroupUser(Group group, User assessee){
		groupUser = getGroupUser(group, assessee);
		if(groupUser==null)
			return "";
		//sort by name
		Collections.sort(groupUser.getUsers(), new Comparator<User>(){
			@Override
			public int compare(User o1, User o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});
		return "";
	}
	public String getAssessorCritScoreDisplay(EvaluationCriteria evalCrit, List<EvaluationUser> evalUserList, User assessor){
		float s = getAssessorCritScore(evalCrit, evalUserList, assessor);
		if(s==-1)
			return "";
		return Util.formatDecimal(s);
	}
	public float getAssessorCritScore(EvaluationCriteria evalCrit, List<EvaluationUser> evalUserList, User assessor){
		for(EvaluationUser eu : evalUserList){
			if(eu.getAssessor().equals(assessor)){
				float score = getCritScore(evalCrit, eu);
				return score;
			}	
		}
		return -1;
	}
	public String getCritScoreSumDisplay(EvaluationCriteria evalCrit, List<EvaluationUser> evalUserList){
		float s = getCritScoreSum(evalCrit, evalUserList);
		if(s==-1)
			return "";
		return Util.formatDecimal(s);
	}
	public float getCritScoreSum(EvaluationCriteria evalCrit, List<EvaluationUser> evalUserList){
		if(evalUserList==null)
			return -1;
		float total = 0;
		for(EvaluationUser eu : evalUserList){
			float score = getCritScore(evalCrit, eu);
			total+= score;
		}
		return total;
	}
	
	public String getSelfClass(User u1, User u2, Evaluation eval){
		if(eval.getAllowSelfEvaluation())
			return "";
		if(u1.equals(u2))
			return "selfHighlight";
		return "";
	}
	
	
}
