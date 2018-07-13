package ntu.celt.eUreka2.pages.modules.profiling;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.profiling.LQuestionType;
import ntu.celt.eUreka2.modules.profiling.ProfileUser;
import ntu.celt.eUreka2.modules.profiling.Profiling;
import ntu.celt.eUreka2.modules.profiling.ProfilingDAO;
import ntu.celt.eUreka2.modules.profiling.QuestionScore;
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
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class Home extends AbstractPageProfiling{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<Profiling> profs;
	@SuppressWarnings("unused")
	@Property
	private Profiling prof;
	@Property
	private List<User> assessees;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@Property
	private int rowIndex;
	@Property
	private int colIndex;
	@Property
	private ProfileUser profUser;
	@Property
	private int maxPeersInGroup;
	@Property
	private List<User> groupMembers;
	@Property
	private User peer;
	@Property
	private Profiling profSELFpre;
	@Property
	private Profiling profSELFpost;
	@Property
	private Profiling profPEER;
	@Property
	private Profiling profINSTRUCTOR;
	
	
	//@Persist("flash")
	/*@Persist
	@Property
	private Integer curPageNo;*/
	
	@SessionState
	private AppState appState;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private ProfilingDAO profDAO;
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
		
		
		profs =  profDAO.getProfilingsByProject(project);
		if(profs.size()==0){
			initProfilings();
			profs =  profDAO.getProfilingsByProject(project);
		}
		
		profSELFpre = profs.get(0);
		profSELFpost = profs.get(1);
		profPEER = profs.get(2);
		profINSTRUCTOR = profs.get(3); 
		
		
		if(isTutor(profPEER.getGroup(), getCurUser()) ){
			return (linkSource.createPageRenderLinkWithContext(TutorHome.class, pid));
		}
		return null;
	}
	String onPassivate() {
		return pid;
	}
	
	
	void setupRender() {
		
		
		
		if(!canManageProfiling(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		
		
		
		maxPeersInGroup = getMaxStudentPerGroup(profPEER.getGroup()) - 1;
		
		assessees = getAllAssessees(project, profPEER.getGroup());
		
		removeExpiredProfUser();
	}
	
	@CommitAfter
	private void initProfilings(){
		String[] names  = {"Self-Assessment (Pre)", "Self-Assessment (Post)", "Peer Assessment", "Instructor Assessment"};
		for(int i=0; i<LQuestionType.values().length; i++){
			LQuestionType qType = LQuestionType.values()[i];
			Profiling p1 = new Profiling(names[i], project, getCurUser(), i+1, qType, new Date() );
			
			profDAO.addProfiling(p1);
		}
	}
	
	
	
	public int getPeerColspan(){
		if(maxPeersInGroup > 0)
			return maxPeersInGroup;
		return 1;
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
	
	
	public ProfileUser loadProfUser(Profiling prof, User assessor, User assessee){
		List<ProfileUser> profUserList = profDAO.searchProfileUser(prof, assessor, assessee, null, null);
		if(profUserList.size()>0)
			profUser = profUserList.get(0);
		else
			profUser = null;
		return null;
	}
	public boolean hasGraded(){
		if(profUser==null)
			return false;
		return true;
	}
	public String loadPeerMember(Profiling prof, User user){
		groupMembers = getOtherStudentsInSameGroup(prof.getGroup(), user);
		return null;
	}
	
	public boolean hasRemainingStudents(Profiling prof){
		List<User> uList = profDAO.getNotAssessedUsersByProfiling(prof);
		if(uList.size()>0)
			return true;
		return false;
	}
	
	
	
	public StreamResponse onExportXls(String projId) throws IOException {
		project = projDAO.getProjectById(projId);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjID", projId));
		List<ProfileUser> profUsers  =  profDAO.getProfileUsersByProject(project);
		
				
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
		wb.setSheetName(0, "LSP Result" );
		
		s.setColumnWidth(0, 3*256);  //width in unit 1/256 character
		s.setColumnWidth(1, 25*256);
		s.setColumnWidth(2, 12*256);
		s.setColumnWidth(3, 12*256);
		s.setColumnWidth(4, 12*256);
	//	s.setColumnWidth(5, 20*256);
	//	s.setColumnWidth(6, 12*256);
	//	s.setColumnWidth(7, 12*256);
		
		//create header row
		r = s.createRow(0);
		c = r.createCell(1);
		c.setCellValue("LSP Result for :");
		c.setCellStyle(cs);
		c = r.createCell(2);
		c.setCellStyle(cs);
		c.setCellValue(project.getDisplayName() );
		
		int colNum = 5;
		
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
		c.setCellValue("Type_LSP_Rating");
		c = r.createCell(4);
		c.setCellStyle(cs2);
		c.setCellValue("Rater_ID");
		
		int numQ = 50;
		if (profUsers.size() > 0){
			numQ = profUsers.get(0).getQuestionScores().size();
		}
		for(int i=1; i <=numQ ; i++){
			c = r.createCell(colNum + i -1);
			c.setCellStyle(cs2);
			c.setCellValue("Q" + (i));
		}
		
		
		int rownumFirst = 2;
		int rownum = rownumFirst;
		for(int i=0; i<profUsers.size(); i++){
			ProfileUser pu = profUsers.get(i);
			
			r =  s.createRow(rownum);
			c = r.createCell(0);
			c.setCellValue(i+1);
			c = r.createCell(1);
			c.setCellValue(pu.getAssessee().getDisplayName());
			c = r.createCell(2);
			c.setCellValue(pu.getAssessee().getUsername());
			c = r.createCell(3);
			c.setCellValue(pu.getProfile().getQuestionType().name());
			c = r.createCell(4);
			c.setCellValue(pu.getAssessor().getUsername());
			
			Object[] qsArr = pu.getQuestionScores().toArray();
			for(int j=0; j<qsArr.length; j++){
				c = r.createCell(colNum+j);
				QuestionScore sq = (QuestionScore) qsArr[j];
				c.setCellValue(sq.getScore());
			}
			
			rownum++;
		}
		
		
				
		//write the workbook to outputStream, then convert it to inputStream
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		wb.write(bout);
		ByteArrayInputStream bis = new ByteArrayInputStream(bout.toByteArray());

		
		String fileName = (project.getDisplayName()+"-LSP").replace(" ", "_")+".xls";
		String contentType = "application/vnd.ms-excel";
		return new ExportFileStreamResponse(bis, fileName, contentType);
	}
	

}
