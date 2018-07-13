package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.MathStatistic;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.group.GroupUser;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
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
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;

public class Home extends AbstractPageEvaluation{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<Evaluation> evals;
	@Property
	private Evaluation eval;
	@Property
	private List<User> assessees;
	@SuppressWarnings("unused")
	@Property
	private User tempUser;
	@Property
	private int rowIndex;
	
	@Property
	private List<EvaluationUser> evalUserList = new ArrayList<EvaluationUser>();
	private List<Double> totalList ;
	private float totalScore;
	private boolean noTotal = true;
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private EvaluationDAO eDAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	
	@Inject
    private Logger logger;
    @Inject
	private Request request;
    @Inject
	private Response response;


    @InjectPage
	private ViewDetailByAssessee pageEvalDetailByAssessee;
    
    
	Object onActivate(String id) {
		pid = id;
		
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", pid));
		
		ProjUser pu = project.getMember(getCurUser());
		if(pu!=null && !isLeader(pu)){ 
			return linkSource.createPageRenderLinkWithContext(AssesseeHome.class, pid);
		}
		return null;
	}
	String onPassivate() {
		return pid;
	}
	
	
	void setupRender() {
		
		
		if(!canManageEvaluation(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		evals =  eDAO.getEvaluationsByProject(project);

		assessees = getAllAssessees(project);
		
		totalList = new ArrayList<Double>();
	}
	
	
	public int getRowNum(){
		return  rowIndex + 1;
	}
	
	public int getAssmtColumnWidth(){
		if(evals.isEmpty())
			return 710;
		
		int width = 974;
		int initwidth = 265;
		int padding = 4;
		int minwidth = 50;
		
		int num = Math.round((width - initwidth)/(evals.size()) - padding);
		return Math.max(num, minwidth);
	}

	
	
	
	public String getEvalUserScoreClass(){
		if(!hasGraded())
			return null;
		return convertScoreToGradeClass(getEvalUserScore(eval, tempUser), 100);
	}
	public String getEvalUserScoreDisplay(User u){
		if(!hasGraded())
			return "-";
		return Util.formatDecimal(getEvalUserScore(eval, u));
	}
	public float getEvalUserScore(Evaluation eval, User u){
		float total=0;
		int num = 0;
		if(eval.getUseFixedPoint()){
			GroupUser groupUser = getGroupUser(eval.getGroup(), u);
			float maxStudentPoint = getMaxGroupStudentTotalFixedPoint(groupUser, eval);
			if(maxStudentPoint!=-1)
				total = getStudentTotalFixedPoint(evalUserList) * 100 / maxStudentPoint ;
			return total;
		}
		
		for(EvaluationUser eu : evalUserList){
			if(!eu.getAssessee().equals(eu.getAssessor())){
				double score = getTotalScore(eu);
				if(score!=0){
					total += score;
					num++;
				}
			}
		}
		if(num==0)
			return 0;
		return total/num;
	}
	
	
	public String loadTotalScore(User u){
		float tScore = 0;
		float tWeight = 0;
		int count = 0;
		for(Evaluation e : evals){
			loadEvalUserList(e, u);
			
			if(!"-".equals(getEvalUserScoreDisplay(u))){
				tScore += (getEvalUserScore(e, u)* e.getWeightage() );
				tWeight += e.getWeightage();
				count++;
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
	public String getTotalScoreDisplay(){
		if(noTotal)
			return "-";
		return Util.formatDecimal(totalScore); 
	}
	public String getTotalScoreClass(){
		if(noTotal)
			return null;
		return convertScoreToGradeClass(totalScore, 100);
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
	
	
	
	public String getAvg(Evaluation eval){
		int count=0;
		double total=0;
		
		for(User u : assessees){
			loadEvalUserList(eval,u);
			String s =	getEvalUserScoreDisplay(u);
			if(!"-".equals(s)){
				total += Double.parseDouble(s);
				count++;
			}
		}
				
		if(count<1)
			return "-";
		else
			return Util.formatDecimal(total/count);
	}
	public String getSTDEV(Evaluation eval){
		List<Double> values = new ArrayList<Double>();
		for(User u : assessees){
			loadEvalUserList(eval,u);
			String s =	getEvalUserScoreDisplay(u);
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
	
	
	
	
	
	public StreamResponse onExportXls(String projId) throws IOException {
		project = pDAO.getProjectById(projId);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjID", projId));
		if(!canManageEvaluation(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		evals =  eDAO.getEvaluationsByProject(project);
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
		wb.setSheetName(0, "Evaluation" );
		
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
		
		int colNum = 4;
		
		s.addMergedRegion(new CellRangeAddress(2, 4, 0, 0));
		s.addMergedRegion(new CellRangeAddress(2, 4, 1, 1));
		s.addMergedRegion(new CellRangeAddress(2, 4, 2, 2));
		s.addMergedRegion(new CellRangeAddress(2, 2, colNum, (evals.isEmpty()? colNum : colNum+2*evals.size()-1)));
		s.addMergedRegion(new CellRangeAddress(2, 3, colNum+2*evals.size(), colNum+2*evals.size()));
		s.addMergedRegion(new CellRangeAddress(2, 3, colNum+2*evals.size() + 1, colNum+2*evals.size() + 1));
		
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
		c.setCellValue("Matric");
		c = r.createCell(4);
		c.setCellStyle(cs2);
		c.setCellValue(messages.get("evaluation"));
		c = r.createCell(colNum+2*evals.size());
		c.setCellStyle(cs2);
		c.setCellValue(messages.get("total"));
		c = r.createCell(colNum+2*evals.size() + 1);
		c.setCellStyle(cs2);
		c.setCellValue(messages.get("grade"));
		
		
		r = s.createRow(3);
		r2 = s.createRow(4);
		
		for(int i=0; i < evals.size(); i++){
			s.addMergedRegion(new CellRangeAddress(3, 3, colNum+2*i, colNum+2*i+1));
			
			c = r.createCell(colNum+2*i);
			c.setCellStyle(cs2);
			c.setCellValue(evals.get(i).getName());
			c = r2.createCell(colNum+2*i);
			c.setCellStyle(cs2);
			c.setCellValue("Group");
			c = r2.createCell(colNum+2*i +1);
			c.setCellStyle(cs2);
			c.setCellValue(evals.get(i).getWeightage()+"%");
		}
		c = r2.createCell(colNum+2*evals.size());
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
			c = r.createCell(3);
			c.setCellValue(u.getExternalKey());
				
			
			
			for(int j=0; j<evals.size(); j++){
				c = r.createCell(colNum + 2*j);
				eval = evals.get(j);
				loadEvalUserList(eval,u);
				
				String grpName = getGroupTypeName(eval.getGroup(), u);
				c.setCellValue(grpName);
				
				
				c = r.createCell(colNum + 2*j + 1);
				String tscore = getEvalUserScoreDisplay(u);
				if(!"-".equals(tscore))
					c.setCellValue(Double.parseDouble(tscore));
				else
					c.setCellValue("-");
				
				
			}
			if(evals.size()> 0){
				loadTotalScore(u);
				c = r.createCell(colNum+2*evals.size());
				String tscore = getTotalScoreDisplay();
				if(!"-".equals(tscore)){
					c.setCellValue(Double.parseDouble(tscore));
				}
				else{
					c.setCellValue("-");
				}
				c = r.createCell(colNum+2*evals.size() + 1);
				if(!"-".equals(tscore)){
					c.setCellValue(convertScoreToGrade(totalScore, 100));
				}
			}
			rownum++;
		}
		r =  s.createRow(rownum);
		c = r.createCell(1);
		c.setCellStyle(cs2);
		c.setCellValue("Mean");
		for(int i=0; i < evals.size()+1; i++){
			c = r.createCell(colNum+2*i);
			c = r.createCell(colNum+2*i+1);
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
		for(int i=0; i < evals.size()+1; i++){
			c = r.createCell(colNum+2*i);
			c = r.createCell(colNum+2*i+1);
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

		
		String fileName = (project.getDisplayName()+"-PeerEvaluations").replace(" ", "_")+".xls";
		String contentType = "application/vnd.ms-excel";
		return new ExportFileStreamResponse(bis, fileName, contentType);
	}
	
	
	
	
	void onExportXlsByStudent(String projId) throws IOException{
		evals =  eDAO.getEvaluationsByProject(project);

		Project proj = project;
		
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
			
			
			for(Evaluation eval : evals){ 
				
				
				List<User> assessees = getAllAssessees(proj, eval.getGroup());
				for(User u : assessees) {
					InputStream input = null;
					
					try{
						StreamResponse xls = pageEvalDetailByAssessee.onExportXls(eval.getId(), u.getId());
						
						try{
								input = new BufferedInputStream(xls.getStream(), BUFFER_SIZE);
								String name = proj.getDisplayName() + "_" + eval.getCreator().getUsername() + "_" 
									+ eval.getName() + "_" + u.getUsername() + ".xls";
								name = name.replace(" ", "");
								
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
							}
						
					}
					finally{
						if(input!=null) try{ input.close();}catch(IOException e){logger.error(e.getMessage());}
					}
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
