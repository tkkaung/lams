package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
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
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ViewDetailByAssessee extends AbstractPageEvaluation{
	@Property
	private Project project;
	@Property
	private Group group;	
	@Property
	private GroupUser groupUser;	
	
	private long eId;
	private int uId;
	@Property
	private Evaluation eval;
	@Property
	private List<User> assessors ;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@Property
	private User selectedUser;
	@Property
	private EvaluationUser evalUser;
	@Property
	private List<EvaluationUser> evalUserList = new ArrayList<EvaluationUser>();
	
	
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
	private EvaluationDAO eDAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private Messages messages;
	
	
	void onActivate(EventContext ec) {
		eId = ec.get(Long.class, 0);
		if(ec.getCount()>1){
			uId = ec.get(Integer.class, 1);
		}
		
		eval = eDAO.getEvaluationById(eId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "EvaluationID", eId));
		selectedUser = uDAO.getUserById(uId);
		if(selectedUser==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "UserID", uId));
		
		
		project = eval.getProject();
		group = eval.getGroup();
		if(group!=null){
			groupUser = getGroupUser(group, selectedUser);
			if(groupUser==null){
				throw new RecordNotFoundException("No Group information for student " + selectedUser.getDisplayName());
			}
		}
	}
	Object[] onPassivate() {
		return new Object[] {eId, uId};
	}
	
	void setupRender(){
		if(!canManageEvaluation(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		if(group!=null){
			if(eval.getAllowSelfEvaluation()){
				assessors = new ArrayList<User>();
				assessors.add(selectedUser);
				assessors.addAll(getOtherStudentsInSameGroup(group, selectedUser));
			}
			else{
				assessors = getOtherStudentsInSameGroup(group, selectedUser);
			}
		}
		else{
			loadEvalUserList(eval, selectedUser);
			assessors = new ArrayList<User>();
			for(int i=0; i<evalUserList.size(); i++){
				assessors.add(evalUserList.get(i).getAssessor());
			}
			Collections.sort(assessors, new Comparator<User>(){
				@Override
				public int compare(User o1, User o2) {
					return o1.getDisplayName().compareTo(o2.getDisplayName());
				}
			});

		}
			
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
		}
		return ""; //to return empty string
	}
	public String loadEvalUser(Evaluation eval, User assessor){
		EvaluationUser eu = eval.getEvalUserByAssessor(evalUserList, assessor);
		if(eu==null || !eu.isSubmited()){
			evalUser = null;
			return "";
		}
		
		evalUser = eu;
		return ""; //to return empty string
	}
	
	
	public boolean hasGraded(){
		if(evalUser==null)
			return false;
		return true;
	}
	
	
	
	public float getTotalScore(){
		if(evalUser==null)
			return 0;
		if(evalUser.getAssessee().equals(evalUser.getAssessor()))
			return 0;
		return getTotalScore(evalUser);
	}
	public String getTotalScoreDisplay(){
		return Util.formatDecimal(getTotalScore());
	}
	public String getTotalScoreClass(){
		if(eval.getUseFixedPoint())
			return null;
		return convertScoreToGradeClass(getTotalScore(), 100);
	}
	
	public float getAverageScore(EvaluationCriteria crit){
		float total = 0;
		int i =0;
		
		for(User u : assessors){
			loadEvalUser(eval, u);
			if(!selectedUser.equals(u)){
				float score = getCritScore(crit,evalUser);
				if(score != 0){
					total += score; 
					i++;
				}
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
		int i =0;
		
		if(eval.getUseFixedPoint()){
			float maxStudentPoint = getMaxGroupStudentTotalFixedPoint(groupUser, eval);
			if(maxStudentPoint!=-1)
				total = getStudentTotalFixedPoint(evalUserList) * 100 / maxStudentPoint ;
			return total;
		}
		
		for(User u : assessors){
			loadEvalUser(eval, u);
			//if(evalUser!=null){
			if(!selectedUser.equals(u)){
				float score = getTotalScore();
				if(score!=0){
					total += getTotalScore();
					i++;
				}
			}
			//}
		}
		if(i==0)
			return 0;
		
		
		return total/i;
	}
	public String getTotalAverageScoreDisplay(){
		return Util.formatDecimal(getTotalAverageScore());
	}
	public String getTotalAverageScoreClass(){
		return convertScoreToGradeClass(getTotalAverageScore(), 100); 
	}
	
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
		int numColum = eval.getCriterias().size();
		if(numColum==0) //in case not-use-rubric
			return "100%";
		
		int width = initWidth + getCritNameWidth()*numColum + padding ;
		if(pageWidth < width)
			return width +"px";
		return "100%";
	}
	
	public String getIsSelf(User u){
		if(selectedUser.equals(u)){
			return messages.get("self");
		}
		return "";
	}
	public String getIsSelfClass(User u){
		if(selectedUser.equals(u)){
			return "self";
		}
		return "";
	}
	
	
	
	public StreamResponse onExportXls(Long eId, int uId) throws IOException {
		 eval = eDAO.getEvaluationById(eId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "EvaluationID", eId));
		selectedUser = uDAO.getUserById(uId);
		if(selectedUser==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "UserID", uId));
		
		project = eval.getProject();
		group = eval.getGroup();
		loadEvalUserList(eval, selectedUser);
		/*if(group!=null){
			groupUser = getGroupUser(group, selectedUser);
			if(groupUser==null){
				throw new RecordNotFoundException("No Group information for student " + selectedUser.getDisplayName());
			}
		}*/
		if(group!=null){
			if(eval.getAllowSelfEvaluation()){
				assessors = new ArrayList<User>();
				assessors.add(selectedUser);
				assessors.addAll(getOtherStudentsInSameGroup(group, selectedUser));
			}
			else{
				assessors = getOtherStudentsInSameGroup(group, selectedUser);
			}
		}
		else{
			
			assessors = new ArrayList<User>();
			for(int i=0; i<evalUserList.size(); i++){
				assessors.add(evalUserList.get(i).getAssessor());
			}
			Collections.sort(assessors, new Comparator<User>(){
				@Override
				public int compare(User o1, User o2) {
					return o1.getDisplayName().compareTo(o2.getDisplayName());
				}
			});

		}
				
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
		int colNum = 4;  //column number that start of Criteria (vary column)
	
		
		s.setColumnWidth(0, 3*256);  //width in unit 1/256 character
		s.setColumnWidth(1, 25*256);
		s.setColumnWidth(2, 15*256);
		for(int i=0; i<qualNum; i++){
			s.setColumnWidth(colNum+2+critList.size()+i, 20*256);
		}
		for(int i=0; i<opquesList.size(); i++){
			s.setColumnWidth(colNum+2+qualNum+critList.size()+i, 40*256); 
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
		
		r = s.createRow(2);
		c = r.createCell(1);
		c.setCellValue("Evaluatee:");
		c.setCellStyle(cs);
		c = r.createCell(2);
		c.setCellStyle(cs);
		c.setCellValue(selectedUser.getDisplayName());
		
		
	
		s.addMergedRegion(new CellRangeAddress(3, 4, 0, 3)); //(firstRow,lastRow, firstCol, lastCol)
		s.addMergedRegion(new CellRangeAddress(3, 3, colNum, colNum+critList.size()-1)); //Rubric Name
		s.addMergedRegion(new CellRangeAddress(3, 4, colNum+critList.size() + 0, colNum+critList.size()+0)); //Total
		s.addMergedRegion(new CellRangeAddress(3, 4, colNum+critList.size() + 1, colNum+critList.size()+1)); //Grade

		if(qualNum>0)
			s.addMergedRegion(new CellRangeAddress(3, 3, colNum+2+critList.size() , colNum+2+critList.size()+ qualNum-1)); //Qualitative Feedback
		for(int i=0; i<qualNum; i++){
			s.addMergedRegion(new CellRangeAddress(4, 5, colNum+2+critList.size()+i , colNum+2+critList.size()+i));   //Strength
		}
		if(opquesList.size()>0)
			s.addMergedRegion(new CellRangeAddress(3, 3, colNum+2+qualNum+critList.size() , colNum+2+qualNum+critList.size()+ opquesList.size()-1)); //Open Questions
		for(int i=0; i<opquesList.size(); i++){
			s.addMergedRegion(new CellRangeAddress(4, 5, colNum+2+qualNum+critList.size()+i , colNum+2+qualNum+critList.size()+i)); 
		}
		
		//create table headers
		r = s.createRow(3);
		c = r.createCell(0);
		c.setCellStyle(cs2);
		c.setCellValue("");
		c = r.createCell(colNum);
		c.setCellStyle(cs2);
		c.setCellValue(eval.getRubric().getName());
		c = r.createCell(colNum + critList.size() );
		c.setCellStyle(cs2);
		c.setCellValue(messages.get("total"));
		c = r.createCell(colNum + critList.size() + 1);
		c.setCellStyle(cs2);
		c.setCellValue(messages.get("grade"));
		if(qualNum>0){
			c = r.createCell(colNum+ 2 + critList.size() );
			c.setCellStyle(cs2);
			c.setCellValue(messages.get("comment"));
		}
		if(opquesList.size()>0){
			c = r.createCell(colNum + 2 + qualNum + critList.size() );
			c.setCellStyle(cs2);
			c.setCellValue(messages.get("open-question-label"));
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
		c.setCellValue(messages.get("group"));
		
		
		for(int i=0; i < critList.size(); i++){
			c = r.createCell(colNum+i);
			c.setCellStyle(cs2);
			c.setCellValue(critList.get(i).getName());
			c = r2.createCell(colNum+i);
			c.setCellStyle(cs2);
			c.setCellValue(critList.get(i).getWeightage()+"%");
		}
		c = r2.createCell(colNum+critList.size()+0);
		c.setCellStyle(cs2);
		c.setCellValue("100%");
		c = r2.createCell(colNum+critList.size()+1);
		c.setCellStyle(cs2);
		c.setCellValue("");
		
		int temp = 0;
		if(eval.getUseCmtStrength()){
			c = r.createCell(colNum+2+critList.size()+temp);
			c.setCellStyle(cs2);
			c.setCellValue(eval.getCustomNameCmtStrength());
			temp++;
		}
		if(eval.getUseCmtWeakness()){
			c = r.createCell(colNum+2+critList.size()+temp);
			c.setCellStyle(cs2);
			c.setCellValue(eval.getCustomNameCmtWeakness());
			temp++;
		}
		if(eval.getUseCmtOther()){
			c = r.createCell(colNum+2+critList.size()+temp);
			c.setCellStyle(cs2);
			c.setCellValue(eval.getCustomNameCmtOther());
			temp++;
		}
		
		
		for(int i=0; i<opquesList.size(); i++){
			c = r.createCell(colNum+2+qualNum+critList.size()+i);
			c.setCellStyle(cs2);
			c.setCellValue(opquesList.get(i)); 
		}
		
		int rownumFirst = 6;
		int rownum = rownumFirst;
		
		for(int i=0; i<assessors.size(); i++){
			User u = assessors.get(i);
			r =  s.createRow(rownum);
			c = r.createCell(0);
			c.setCellValue(i+1);
			c = r.createCell(1);
			c.setCellValue(u.getDisplayName());
			c = r.createCell(2);
			c.setCellValue(u.getUsername());
			c = r.createCell(3);
			c.setCellValue(getGroupTypeNumber(eval.getGroup(), u));
			
			loadEvalUser(eval, u);
			
			for(int j=0; j<critList.size(); j++){
				c = r.createCell(colNum+j);
				if(hasGraded()){
					//c.setCellValue(getComputedCritScoreDisplay(critList.get(j), evalUserList));
					c.setCellStyle(cs4);
					
					String score = "";
					score = getCritScoreDisplay(critList.get(j),evalUser);//getComputedCritScoreDisplay(critList.get(j), evalUserList);
					if(!"".equals(score)){
						c.setCellValue(Double.parseDouble(score));
					}else{
						c.setCellValue("-");
					}
				}
				
			}
			
			
			c = r.createCell(colNum+critList.size());
			//c.setCellValue(getTotalScoreDisplay());
			String score = getTotalScoreDisplay();
			if(!"-".equals(score)){
				c.setCellValue(Double.parseDouble(score));
			}else{
				c.setCellValue("-");
			}
			c.setCellStyle(cs2);
			c = r.createCell(colNum+critList.size() + 1);
			String grade = "-";
			if(hasGraded()){
				if(! eval.getUseFixedPoint()){
					grade = convertScoreToGrade(getTotalScore(), 100);
				}
			}
			c.setCellValue(grade);
			c.setCellStyle(cs2);
			
			temp = 0;
			if(eval.getUseCmtStrength()){
				c = r.createCell(colNum+2+critList.size()+temp);
				
				c.setCellValue(evalUser==null ? "" : evalUser.getCmtStrengthDisplay());
			//	c.setCellStyle(cs5);
			}
			if(eval.getUseCmtWeakness()){
				c = r.createCell(colNum+2+critList.size()+temp);
				c.setCellValue(evalUser==null ? "" : evalUser.getCmtWeaknessDisplay());
			//	c.setCellStyle(cs5);
			}
			if(eval.getUseCmtOther()){
				c = r.createCell(colNum+2+critList.size()+temp);
				c.setCellValue(evalUser==null ? "" : evalUser.getCmtOtherDisplay());
			//	c.setCellStyle(cs5);
			}
			for(int j=0; j<opquesList.size(); j++){
				c = r.createCell(colNum+2+qualNum+critList.size()+j);
			//	c.setCellStyle(cs5);
				c.setCellValue(evalUser==null ? "" : evalUser.getOpenEndedQuestionAnswers(j)); 
			}
			
			
			
			rownum++;
		}
		
		r = s.createRow(rownum);
		c = r.createCell(3);
		c.setCellValue(messages.get("average") + ":");
		c.setCellStyle(cs2);
		for(int j=0; j<critList.size(); j++){
			c = r.createCell(colNum + j);
			c.setCellValue(getAverageScoreDisplay(critList.get(j)));
			c.setCellStyle(cs2);
		}
		c = r.createCell(colNum+critList.size()+0);
		c.setCellValue(getTotalAverageScoreDisplay());
		c.setCellStyle(cs2);
		c = r.createCell(colNum+critList.size()+1);
		c.setCellValue(convertScoreToGrade(getTotalAverageScore(), 100));
		
		r =  s.createRow(rownum+1);
		c = r.createCell(3);
		c.setCellStyle(cs2);
		c.setCellValue("Std Dev");
		for(int i=0; i < critList.size()+1; i++){
			c = r.createCell(colNum+i);
			c.setCellStyle(cs6);
			c.setCellType(Cell.CELL_TYPE_FORMULA);
			CellReference cr1 = new CellReference(rownumFirst, colNum+i);
			CellReference cr2 = new CellReference(rownum-1, colNum+i);
			c.setCellFormula("STDEV(" + cr1.formatAsString() + ":" + cr2.formatAsString() + ")");
		}
		
		
		//write the workbook to outputStream, then convert it to inputStream
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		wb.write(bout);
		ByteArrayInputStream bis = new ByteArrayInputStream(bout.toByteArray());

		
		String fileName = (project.getDisplayName() + "-Evaluation-" + eval.getName()+"_Evaluatee-" + selectedUser.getUsername() ).replace(" ", "_")+".xls";
		String contentType = "application/vnd.ms-excel";
		return new ExportFileStreamResponse(bis, fileName, contentType);
	}
	
	
}
