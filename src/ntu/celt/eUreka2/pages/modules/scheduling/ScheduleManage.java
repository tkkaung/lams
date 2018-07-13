package ntu.celt.eUreka2.pages.modules.scheduling;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.DataBeingUsedException;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.EvenOdd;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.scheduling.Activity;
import ntu.celt.eUreka2.modules.scheduling.ExportFileStreamResponse;
import ntu.celt.eUreka2.modules.scheduling.Milestone;
import ntu.celt.eUreka2.modules.scheduling.Phase;
import ntu.celt.eUreka2.modules.scheduling.Schedule;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.modules.scheduling.Task;

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
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.slf4j.Logger;

public class ScheduleManage extends AbstractPageScheduling {
	@Property
	private Project curProj;
	@SessionState
	private AppState appState;
	private String projId ;
	@Property
	private Schedule activeSchedule;
	@Property
	private List<Schedule> schedules;
	@Property
	private Schedule schedule;
	@Property 
	private String remarks;
	@SuppressWarnings("unused")
	@Property
	private EvenOdd evenOdd ;
	
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private SchedulingDAO scheduleDAO;
	@SuppressWarnings("unused")
	@Inject
	private Logger logger;
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private Messages messages;
    @Inject
    private PropertyConduitSource propertyConduitSource; 
    
    
	void onActivate(String id) {
		projId = id;
	}
	String onPassivate() {
		return projId;
	}
	
	void setupRender(){
		curProj = projDAO.getProjectById(projId);
		if(curProj==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectID", projId));
		if(!canManageVersion(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		activeSchedule = scheduleDAO.getActiveSchedule(curProj);
		if(activeSchedule==null)
			throw new RecordNotFoundException(messages.get("no-active-schedule"));
		
		schedules = scheduleDAO.getInactiveSchedules(curProj);
		
		evenOdd = new EvenOdd();
	}
	
	void onPrepareForSubmitFromSaveForm(){
		curProj = projDAO.getProjectById(projId);
		activeSchedule = scheduleDAO.getActiveSchedule(curProj);
	}
	@CommitAfter
	void onSuccessFromSaveForm(){
		Schedule schd = activeSchedule.clone();
		schd.setActive(false);
		schd.setCreateDate(new Date());
		schd.setUser(getCurUser());
		schd.setRemarks(remarks);
		
		scheduleDAO.saveSchedule(schd);
	}
	
	@CommitAfter
	void onActionFromRestore(Long schdId){
		Schedule schd = scheduleDAO.getScheduleById(schdId);
		if(schd==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ScheduleID", schdId));
		if(!canManageVersion(schd.getProject())){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		activeSchedule = scheduleDAO.getActiveSchedule(schd.getProject());
		activeSchedule.setCreateDate(new Date());
		activeSchedule.setActive(false);
		activeSchedule.setRemarks(messages.get("auto-saved"));
		activeSchedule.setUser(getCurUser());
		
		Schedule schd1 = schd.clone();
		schd1.setActive(true);
		schd1.setCreateDate(new Date());
		schd1.setUser(getCurUser());
		scheduleDAO.saveSchedule(schd1);
	}
	
	@CommitAfter
	void onActionFromRemove(Long schdId){
		Schedule schd = scheduleDAO.getScheduleById(schdId);
		if(schd==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ScheduleID", schdId));
		if(!canManageVersion(schd.getProject())){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		if(schd.isActive())
			throw new DataBeingUsedException(messages.get("cant-remove-active-schedule"));
		
		scheduleDAO.deleteSchedule(schd);
	}
	
	public BeanModel<Schedule> getModel(){
		BeanModel<Schedule> model = beanModelSource.createEditModel(Schedule.class, messages);
        model.include("createDate","remarks");
        
        model.add("createdBy", propertyConduitSource.create(Schedule.class, "user.displayname")); 
        model.add("contain", null);
     	model.add("action", null);
       
        	
        return model;
	}
	
	public String getSummaryCount(Schedule schd){
		int numPhase = 0; 
		int numTask = 0;
		for(Milestone m : schd.getMilestones()){
			numPhase += m.getPhases().size();
			numTask += m.getTasks().size();
		}
		
		return messages.get("milestone")+": "+schd.getMilestones().size()
				+", "+ messages.get("phase")+": "+numPhase
				+", "+ messages.get("task")+": "+numTask;
	}
	
	public Object[] getActiveScheduleIdAndScheduleId(){
		return new Object[] {activeSchedule.getId(), schedule.getId()};
	}
	
	public int getTotalSize() {
		if(schedules==null) return 0;
		return schedules.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	
	public StreamResponse onExportXls(Long schdId) throws IOException {
		Schedule schd = scheduleDAO.getScheduleById(schdId);
		if(schd==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ScheduleID", schdId));
		Project proj = schd.getProject();
		if(!canManageVersion(proj))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		String[] columnHeaders = new String[]{"type", "id", "description", "manager"
				,"assignedTo", "urgency", "comment", "startDate", "endDate", "percentDone"};
		
		// create a new workbook
		Workbook wb = new HSSFWorkbook();
		Sheet s = wb.createSheet();
		Row r = null;
		Cell c = null;
		
		// create 3 cell styles
		CellStyle cs = wb.createCellStyle();
		CellStyle cs2 = wb.createCellStyle();
		CellStyle cs3 = wb.createCellStyle();
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
		cs3.setDataFormat(df.getFormat("dd MMM yyyy"));

		// set the sheet name in Unicode
		wb.setSheetName(0, "Timeline" );
		
		s.setColumnWidth(1, 4*256);  //width in unit 1/256 character
		s.setColumnWidth(2, 25*256);  
		s.setColumnWidth(3, 20*256);
		s.setColumnWidth(4, 20*256);
		s.setColumnWidth(6, 20*256);
		s.setColumnWidth(7, 12*256);
		s.setColumnWidth(8, 12*256);
		
		//create header row
		r = s.createRow(0);
		c = r.createCell(0);
		c.setCellValue(messages.get("project-label")+":");
		c = r.createCell(1);
		c.setCellStyle(cs);
		c.setCellValue(proj.getDisplayName() + " - "+ proj.getName());
		
		r = s.createRow(1);
		c = r.createCell(0);
		c.setCellValue(messages.get("date-label")+":");
		c = r.createCell(1);
		c.setCellValue(proj.getSdateDisplay()+ " - "+ proj.getEdateDisplay());
		
		
		//create table headers
		r = s.createRow(3);
		
		for(int colnum = 0; colnum < columnHeaders.length; colnum++){
			String header = columnHeaders[colnum];
			c = r.createCell(colnum);
			c.setCellStyle(cs2);
			c.setCellValue(messages.get(header+"-label"));
		}
		
		int rownum = 4;
		for(Milestone milestone : schd.getMilestones()){
			r =  s.createRow(rownum);
			c = r.createCell(0);
			c.setCellValue(messages.get("milestone-label"));
			c = r.createCell(1);
			c.setCellValue(milestone.getIdentifier());
			c = r.createCell(2);
			c.setCellValue(milestone.getName());
			c = r.createCell(3);
			c.setCellValue(milestone.getManager().getUsername());
			c = r.createCell(4);
			c.setCellValue("-");
			c = r.createCell(5);
			//c.setCellValue(Util.capitalize(milestone.getUrgency().toString()));
			c.setCellValue("-");
			c = r.createCell(6);
			c.setCellValue(Util.nvl(milestone.getComment()));
			c = r.createCell(7);
			c.setCellValue("-");
			c = r.createCell(8);
			c.setCellStyle(cs3);
			c.setCellValue(milestone.getDeadline());
			c = r.createCell(9);
			c.setCellValue(milestone.getAveragePercentDone());
			rownum++;
			
			for(Activity act : milestone.getSortedTasksAndPhases()){
				 if(act instanceof Task){
					Task task = (Task) act;
					r =  s.createRow(rownum);
					c = r.createCell(0);
					c.setCellValue(messages.get("task-label"));
					c = r.createCell(1);
					c.setCellValue(task.getIdentifier());
					c = r.createCell(2);
					c.setCellValue(task.getName());
					c = r.createCell(3);
					c.setCellValue(task.getManager().getUsername());
					c = r.createCell(4);
					c.setCellValue(task.getAssignedPersonUsernames());
					c = r.createCell(5);
					c.setCellValue(Util.capitalize(task.getUrgency().toString()));
					c = r.createCell(6);
					c.setCellValue(Util.nvl(task.getComment()));
					c = r.createCell(7);
					c.setCellStyle(cs3);
					c.setCellValue(task.getStartDate());
					c = r.createCell(8);
					c.setCellStyle(cs3);
					c.setCellValue(task.getEndDate());
					c = r.createCell(9);
					c.setCellValue(task.getPercentageDone());
					rownum++;
				}
				else if(act instanceof Phase){
					Phase phase = (Phase) act;
					r =  s.createRow(rownum);
					c = r.createCell(0);
					c.setCellValue(messages.get("phase-label"));
					c = r.createCell(1);
					c.setCellValue(phase.getIdentifier());
					c = r.createCell(2);
					c.setCellValue(phase.getName());
					c = r.createCell(3);
					c.setCellValue(phase.getManager().getUsername());
					c = r.createCell(4);
					c.setCellValue("-");
					c = r.createCell(5);
					c.setCellValue("-");
					c = r.createCell(6);
					c.setCellValue(Util.nvl(phase.getComment()));
					c = r.createCell(7);
					c.setCellStyle(cs3);
					c.setCellValue(phase.getStartDate());
					c = r.createCell(8);
					c.setCellStyle(cs3);
					c.setCellValue(phase.getEndDate());
					c = r.createCell(9);
					c.setCellValue(phase.getAveragePercentDone());
					rownum++;
					
					for(Task task : phase.getTasks()){
						r =  s.createRow(rownum);
						c = r.createCell(0);
						c.setCellValue(messages.get("task-label"));
						c = r.createCell(1);
						c.setCellValue(task.getIdentifier());
						c = r.createCell(2);
						c.setCellValue(task.getName());
						c = r.createCell(3);
						c.setCellValue(task.getManager().getUsername());
						c = r.createCell(4);
						c.setCellValue(task.getAssignedPersonUsernames());
						c = r.createCell(5);
						c.setCellValue(Util.capitalize(task.getUrgency().toString()));
						c = r.createCell(6);
						c.setCellValue(Util.nvl(task.getComment()));
						c = r.createCell(7);
						c.setCellStyle(cs3);
						c.setCellValue(task.getStartDate());
						c = r.createCell(8);
						c.setCellStyle(cs3);
						c.setCellValue(task.getEndDate());
						c = r.createCell(9);
						c.setCellValue(task.getPercentageDone());
						rownum++;
					}
				}
			}
		}
		
		
		/*	
		String tempFileLoc = System.getProperty("java.io.tmpdir") +"/" + Util.generateUUID()+".xls" ;
		// create a new file
		FileOutputStream out = new FileOutputStream(tempFileLoc);
		// write the workbook to the output stream
		// close our file (don't blow out our file handles
		wb.write(out);
		out.close();
		InputStream is = new FileInputStream(tempFileLoc);
	*/
		
		
		//write the workbook to outputStream, then convert it to inputStream
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		wb.write(bout);
		ByteArrayInputStream bis = new ByteArrayInputStream(bout.toByteArray());

		
		String fileName = (proj.getDisplayName()+"-"+getModule().getDisplayName()+'-'+schd.getCreateDateDisplay() ).replace(" ", "_")+".xls";
		String contentType = "application/vnd.ms-excel";
		return new ExportFileStreamResponse(bis, fileName, contentType);
	}
}
