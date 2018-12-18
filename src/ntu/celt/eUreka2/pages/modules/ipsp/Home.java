package ntu.celt.eUreka2.pages.modules.ipsp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.ipsp.IPSPDAO;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurvey;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurveyUser;
import ntu.celt.eUreka2.modules.ipsp.IDimension;
import ntu.celt.eUreka2.modules.ipsp.IQuestionScore;
import ntu.celt.eUreka2.modules.ipsp.IQuestionType;
import ntu.celt.eUreka2.modules.scheduling.ExportFileStreamResponse;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class Home extends AbstractPageIPSP{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<IPSPSurvey> ipsps;
	
	@Property
	private IPSPSurvey ipsp;
	@Property
	private List<User> assessees;
	@Property
	private User user;
	@Property
	private int rowIndex;
	@Property
	private int colIndex;
	@Property
	private IPSPSurveyUser ipspUser;
	
	
	//@Persist("flash")
	/*@Persist
	@Property
	private Integer curPageNo;*/
	
	@SessionState
	private AppState appState;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private IPSPDAO ipspDAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	
	Object onActivate(String id) {
		pid = id;
		
		project = projDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", pid));
		
		ProjUser pu = project.getMember(getCurUser());
		if((pu!=null && !isLeader(pu)) ){
			return (linkSource.createPageRenderLinkWithContext(AssesseeHome.class, pid));
		}
		
		
		ipsps =  ipspDAO.getIPSPSurveysByProject(project);
		if(ipsps.size()==0){
			initIPSPSurveys();
			ipsps =  ipspDAO.getIPSPSurveysByProject(project);
		}
		if(ipsps.size()>0)
			ipsp = ipsps.get(0);
	
		return null;
	}
	String onPassivate() {
		return pid;
	}
	
	
	void setupRender() {
		
		if(!canManageIPSPSurvey(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		
		assessees = getAllAssessees(project);
		
		
		removeExpiredIPSPSurveyUser();
		
		checkAndUpdateAvgScore(ipsp);
	}
	
	@CommitAfter
	private void initIPSPSurveys(){
		String[] names  = {"SELF-Rating", "PEER-Rating"};
		for(int i=0; i<names.length; i++){
			IQuestionType qType = IQuestionType.values()[i];
			IPSPSurvey p1 = new IPSPSurvey(names[i], project, getCurUser(), i+1, qType, new Date() );
			
			ipspDAO.addIPSPSurvey(p1);
		}
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
	
	
	public IPSPSurveyUser loadIPSPSurveyUser(IPSPSurvey ipsp,  User assessee){
		List<IPSPSurveyUser> ipspUserList ;
		if(ipsp.getQuestionType() == IQuestionType.SELF)
			ipspUserList = ipspDAO.searchIPSPSurveyUser(ipsp, assessee, assessee, null,  null);
		else //if (ipsp.getQuestionType() == IQuestionType.PEER)
			ipspUserList = ipspDAO.searchIPSPSurveyUser(ipsp, null, assessee, ipsp.getQuestionType(),  null);
			
		if(ipspUserList.size()>0)
			ipspUser = ipspUserList.get(0);
		else
			ipspUser = null;
		return null;
	}
	public boolean hasGraded(){
		if(ipspUser==null)
			return false;
		return true;
	}
	
	
	public boolean hasRemainingStudents(IPSPSurvey ipsp){
		List<User> uList = ipspDAO.getNotAssessedUsersByIPSPSurvey(ipsp);
		if(uList.size()>0)
			return true;
		return false;
	}
	
	
	
	public StreamResponse onExportXls(String projId) throws IOException {
		project = projDAO.getProjectById(projId);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjID", projId));
		ipsps  =  ipspDAO.getIPSPSurveysByProject(project);
		if(ipsps.size()>0)
			ipsp = ipsps.get(0);
		List<IPSPSurveyUser> ipspUsers  =  ipspDAO.searchIPSPSurveyUser(ipsp, null, null, null, true);
		List<IDimension> cdimensions = ipspDAO.getAllIDimensions();
				
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
		cs2.setWrapText(true);
		
		cs3.setDataFormat(df.getFormat("dd MMM yyyy"));

		cs4.setDataFormat(df.getFormat("0.0")); 
		
		// set the sheet name in Unicode
		wb.setSheetName(0, "IPSP Result" );
		
		s.setColumnWidth(0, 3*256);  //width in unit 1/256 character
		s.setColumnWidth(1, 25*256);
		s.setColumnWidth(2, 12*256);
		s.setColumnWidth(3, 15*256);
		s.setColumnWidth(4, 8*256);
		s.setColumnWidth(6, 15*256);
		s.setColumnWidth(8, 15*256);
		s.setColumnWidth(11, 25*256);
	//	s.setColumnWidth(5, 20*256);
	//	s.setColumnWidth(6, 12*256);
	//	s.setColumnWidth(7, 12*256);
		
		//create header row
		r = s.createRow(0);
		c = r.createCell(1);
		c.setCellValue("IPSP Result for :");
		c.setCellStyle(cs);
		c = r.createCell(2);
		c.setCellStyle(cs);
		c.setCellValue(project.getDisplayName() );
		
		int colNum = 4;
		
		/*s.addMergedRegion(new CellRangeAddress(2, 4, 0, 0));
		s.addMergedRegion(new CellRangeAddress(2, 4, 1, 1));
		s.addMergedRegion(new CellRangeAddress(2, 4, 2, 2));
		s.addMergedRegion(new CellRangeAddress(2, 2, colNum, (assmts.isEmpty()? colNum : colNum+assmts.size()-1)));
		s.addMergedRegion(new CellRangeAddress(2, 3, colNum+assmts.size(), colNum+assmts.size()));
		s.addMergedRegion(new CellRangeAddress(2, 3, colNum+assmts.size() + 1, colNum+assmts.size() + 1));*/
		
		//create table headers
		r = s.createRow(1);
		c = r.createCell(0);
		c.setCellStyle(cs2);
		c.setCellValue("");
		c = r.createCell(1);
		c.setCellStyle(cs2);
		c.setCellValue("Student Name");
		c = r.createCell(2);
		c.setCellStyle(cs2);
		c.setCellValue("Student_ID");
		
		
		c = r.createCell(3);
		c.setCellStyle(cs2);
		c.setCellValue("Matric Number");
		/*c = r.createCell(4);
		c.setCellStyle(cs2);
		c.setCellValue("Age");
		c = r.createCell(5);
		c.setCellStyle(cs2);
		c.setCellValue("Gender");
		c = r.createCell(6);
		c.setCellStyle(cs2);
		c.setCellValue("Highest Education");
		c = r.createCell(7);
		c.setCellStyle(cs2);
		c.setCellValue("Marital Status");
		c = r.createCell(8);
		c.setCellStyle(cs2);
		c.setCellValue("Last Leadership Appointment");
		c = r.createCell(9);
		c.setCellStyle(cs2);
		c.setCellValue("Years in Leadership Appointment");
		c = r.createCell(10);
		c.setCellStyle(cs2);
		c.setCellValue("Experience in Crisis Leadership");
		c = r.createCell(11);
		c.setCellStyle(cs2);
		c.setCellValue("Brief description of the crisis");
		*/
		
		
		
		int numQ = 52;
		if (ipspUsers.size() > 0){
			numQ = ipsp.getQuestions().size();
		}
		for(int i=1; i <=numQ ; i++){
			c = r.createCell(colNum + i -1);
			c.setCellStyle(cs2);
			c.setCellValue("Q" + (i));
		}
		
		
		for(int i=0; i<cdimensions.size(); i++){
			IDimension cdimension = cdimensions.get(i);
			c = r.createCell(colNum + numQ + i );
			c.setCellStyle(cs2);
			c.setCellValue(cdimension.getName());
		}
		
		
		int rownumFirst = 2;
		int rownum = rownumFirst;
		for(int i=0; i<ipspUsers.size(); i++){
			IPSPSurveyUser pu = ipspUsers.get(i);
			
			
			r =  s.createRow(rownum);
			c = r.createCell(0);
			c.setCellValue(i+1);
			c = r.createCell(1);
			c.setCellValue(pu.getAssessee().getDisplayName());
			c = r.createCell(2);
			c.setCellValue(pu.getAssessee().getUsername());
			/*c = r.createCell(3);
			c.setCellValue("");
			c = r.createCell(4);
			c.setCellValue(pu.getAssessor().getUsername());*/
			
			
			
			Object[] qsArr = pu.getQuestionScores().toArray();
			for(int j=0; j<qsArr.length; j++){
				c = r.createCell(colNum+j);
				IQuestionScore sq = (IQuestionScore) qsArr[j];
				c.setCellValue(sq.getScore());
			}
			
			for(int j=0; j<cdimensions.size(); j++){
				IDimension cdimension = cdimensions.get(j);
				c = r.createCell(colNum + numQ +j);
			//	Double score = ipspDAO.getAverageScoreByDimension(pu, cdimension.getId()); 
				Double score =  pu.getScoreDim(cdimension.getId());
				if(score != null)
					c.setCellValue(score);
			}
			
			rownum++;
		}
		
		
				
		//write the workbook to outputStream, then convert it to inputStream
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		wb.write(bout);
		ByteArrayInputStream bis = new ByteArrayInputStream(bout.toByteArray());

		
		String fileName = (project.getDisplayName()+"-IPSP").replace(" ", "_")+".xls";
		String contentType = "application/vnd.ms-excel";
		return new ExportFileStreamResponse(bis, fileName, contentType);
	}
	

}
