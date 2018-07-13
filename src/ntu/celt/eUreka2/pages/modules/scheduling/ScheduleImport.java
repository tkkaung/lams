package ntu.celt.eUreka2.pages.modules.scheduling;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ntu.celt.eUreka2.dao.InvalidFormatException;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.scheduling.Milestone;
import ntu.celt.eUreka2.modules.scheduling.Phase;
import ntu.celt.eUreka2.modules.scheduling.ReminderType;
import ntu.celt.eUreka2.modules.scheduling.Schedule;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.modules.scheduling.Task;
import ntu.celt.eUreka2.modules.scheduling.UrgencyLevel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.upload.services.UploadedFile;


public class ScheduleImport extends AbstractPageScheduling {
	@Property
	private Project curProj;
	
	private String projId ;
	
	@Property
    private UploadedFile xlsfile;
	
	
	@SessionState
	private AppState appState;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private SchedulingDAO scheduleDAO;
	@Inject
	private UserDAO userDAO;
    @Inject
    private Messages messages;
    @Inject
	private RequestGlobals requestGlobal;
    @Inject
    private PageRenderLinkSource linkSource;
    
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
		
	}
	
	void onPrepareForSubmitFromForm(){
		curProj = projDAO.getProjectById(projId);
	}
	
	@Component (id="form")
	private Form form;
	private File savedFile;
	private String[] columnHeaders = new String[]{"type","id", "description", "manager"
			,"assignedTo", "urgency", "comment", "startDate", "endDate", "percentDone"};
	
	public void onValidateFormFromForm() throws IOException{
		if(! xlsfile.getFileName().toLowerCase().endsWith(".xls")){
			form.recordError(messages.format("incorrect-file-extension-x", ".xls"));  
		}
		else{
	    	String toSaveFolder = System.getProperty("java.io.tmpdir"); //get OS current temporary directory
	        String prefix = requestGlobal.getHTTPServletRequest().getSession(true).getId();
	    	savedFile = new File(toSaveFolder+ "/"+ prefix + xlsfile.getFileName());
	    	xlsfile.write(savedFile);
		}
	}
	
	@CommitAfter
	Object onSuccessFromForm(){
		FileInputStream fis;
		try {
			fis = new FileInputStream(savedFile);
			Workbook wb = new HSSFWorkbook(fis);
			Schedule schd = new Schedule();
			
			Sheet sheet = wb.getSheetAt(0);
			int firstRow = 4;
			int lastRow = sheet.getLastRowNum();
			if(firstRow>lastRow)
				throw new InvalidFormatException(messages.get("import-ex-num-row-not-matched"));
			
			Row row = sheet.getRow(firstRow-1);
			for(int i=0; i<columnHeaders.length; i++){
				String header = columnHeaders[i];
				if(!messages.get(header+"-label").equalsIgnoreCase(row.getCell(i).getStringCellValue())){
					throw new InvalidFormatException(messages.get("import-ex-column-header-not-matched"));
				}
			}
			
			int numM = 0;
			int numP = 0;
			int numT = 0;
			Milestone m = null;
			Phase p = null;
			Task t = null;
			for(int i=firstRow; i<=lastRow; i++){
				row = sheet.getRow(i);
				String type = row.getCell(0).getStringCellValue();
				
				if(messages.get("milestone-label").equalsIgnoreCase(type)){
					
					p = null;
					m = new Milestone();
					m.setCreateDate(new Date());
					m.setModifyDate(new Date());
					m.setIdentifier(row.getCell(1).getStringCellValue());
					m.setName(row.getCell(2).getStringCellValue());
					User u = userDAO.getUserByUsername(row.getCell(3).getStringCellValue());
					if(u==null)
						throw new InvalidFormatException(messages.format("import-ex-invalid-data-x"
								, row.getCell(3).getStringCellValue()));
					m.setManager(u);
					m.setComment(row.getCell(6).getStringCellValue());
					m.setDeadline(row.getCell(8).getDateCellValue());
					if(m.getDeadline().before(curProj.getSdate()))
						throw new InvalidFormatException(messages.format("import-ex-invalid-data-x-y"
								, row.getCell(8).getDateCellValue() 
								, messages.get("deadline-before-startDate-error")));
					if(curProj.getEdate()!=null && m.getDeadline().after(curProj.getEdate()))
						throw new InvalidFormatException(messages.format("import-ex-invalid-data-x-y"
								, row.getCell(8).getDateCellValue() 
								,  messages.get("deadline-after-endDate-error")));
				  	
					m.setUrgency(UrgencyLevel.MEDIUM);
					m.setSchedule(schd);
					schd.addMilestone(m);
					numM++;
				}
				else if(messages.get("phase-label").equalsIgnoreCase(type)){
					p = new Phase();
					p.setCreateDate(new Date());
					p.setModifyDate(new Date());
					p.setIdentifier(row.getCell(1).getStringCellValue());
					p.setName(row.getCell(2).getStringCellValue());
					User u = userDAO.getUserByUsername(row.getCell(3).getStringCellValue());
					if(u==null)
						throw new InvalidFormatException(messages.format("import-ex-invalid-data-x"
								, row.getCell(3).getStringCellValue()));
					p.setManager(u);
					p.setComment(row.getCell(6).getStringCellValue());
					p.setStartDate(row.getCell(7).getDateCellValue());
					p.setEndDate(row.getCell(8).getDateCellValue());
					if(p.getStartDate().after(p.getEndDate()))
						throw new InvalidFormatException(messages.format("import-ex-invalid-data-x-y"
								, row.getCell(7).getDateCellValue() 
								, messages.get("endDate-must-be-after-startDate")));
					if(p.getStartDate().before(curProj.getSdate()) || p.getStartDate().after(m.getDeadline()))
						throw new InvalidFormatException(messages.format("import-ex-invalid-data-x-y"
								, row.getCell(7).getDateCellValue() 
								,messages.get("sDate-before-projSDate-nor-after-mileDeadline-error")));
					if(p.getEndDate().before(curProj.getSdate()) || p.getEndDate().after(m.getDeadline()))
						throw new InvalidFormatException(messages.format("import-ex-invalid-data-x-y"
								, row.getCell(8).getDateCellValue() 
								, messages.get("eDate-before-projSDate-nor-after-mileDeadline-error")));
				
					p.setMilestone(m);
					m.addPhase(p);
					numP++;
				}
				else if(messages.get("task-label").equalsIgnoreCase(type)){
					t = new Task();
					t.setCreateDate(new Date());
					t.setModifyDate(new Date());
					t.setReminderType(ReminderType.ASSIGNED_MEMBERS_ONLY);
					t.setIdentifier(row.getCell(1).getStringCellValue());
					t.setName(row.getCell(2).getStringCellValue());
					User u = userDAO.getUserByUsername(row.getCell(3).getStringCellValue());
					if(u==null)
						throw new InvalidFormatException(messages.format("import-ex-invalid-data-x"
								,row.getCell(3).getStringCellValue()));
					t.setManager(u);
					if(!"-".equals(row.getCell(4).getStringCellValue())){
						Set<User> uSet = new HashSet<User>();
						String[] usernames = row.getCell(4).getStringCellValue().split(",");
						for(String username : usernames){
							u = userDAO.getUserByUsername(username.trim());
							if(u==null)
								throw new InvalidFormatException(messages.format("import-ex-invalid-data-x"
										,username.trim() ));
							uSet.add(u);
						}
						t.setAssignedPersons(uSet);
					}
					t.setUrgency(UrgencyLevel.valueOf(row.getCell(5).getStringCellValue().toUpperCase()));
					t.setComment(row.getCell(6).getStringCellValue());
					t.setStartDate(row.getCell(7).getDateCellValue());
					t.setEndDate(row.getCell(8).getDateCellValue());
					
					if(t.getStartDate().after(t.getEndDate())){
						throw new InvalidFormatException(messages.format("import-ex-invalid-data-x-y"
								, row.getCell(7).getDateCellValue() 
								, messages.get("endDate-must-be-after-startDate")));
					}
					if(p==null){
						if(t.getStartDate().before(curProj.getSdate()) || t.getStartDate().after(m.getDeadline()))
							throw new InvalidFormatException(messages.format("import-ex-invalid-data-x-y"
									, row.getCell(7).getDateCellValue() 
									, messages.get("sDate-before-projSDate-nor-after-mileDeadline-error")));
						if(t.getEndDate().before(curProj.getSdate()) || t.getEndDate().after(m.getDeadline()))
							throw new InvalidFormatException(messages.format("import-ex-invalid-data-x-y"
									, row.getCell(8).getDateCellValue() 
									, messages.get("eDate-before-projSDate-nor-after-mileDeadline-error")));
					}
					else{
						if(t.getStartDate().before(p.getStartDate()) || t.getStartDate().after(p.getEndDate()))
							throw new InvalidFormatException(messages.format("import-ex-invalid-data-x-y"
									, row.getCell(7).getDateCellValue() 
									, messages.get("sDate-outside-phase-sdate-edate-error")));
						if(t.getEndDate().before(p.getStartDate()) || t.getEndDate().after(p.getEndDate()))
							throw new InvalidFormatException(messages.format("import-ex-invalid-data-x-y"
									, row.getCell(8).getDateCellValue() 
									, messages.get("eDate-outside-phase-sdate-edate-error")));
					}
					
					t.setPercentageDone((int) row.getCell(9).getNumericCellValue());
					if(t.getPercentageDone()<0 || t.getPercentageDone()>100){
						throw new InvalidFormatException(messages.format("import-ex-invalid-data-x-y"
								, row.getCell(9).getNumericCellValue() 
								, messages.get("import-ex-percent-done-between-0-100")));
					}
					
					if(p!=null){
						t.setPhase(p);
						p.addTask(t);
					}
					t.setMilestone(m);
					m.addTask(t);
					numT++;
				}
				else{
					throw new InvalidFormatException(messages.format("import-ex-type-not-recognized-x",type));
				}
			}
			
			
			fis.close();
			savedFile.delete();
			
			
			Schedule activeSchedule = scheduleDAO.getActiveSchedule(curProj);
			if(activeSchedule!=null){
				activeSchedule.setCreateDate(new Date());
				activeSchedule.setActive(false);
				activeSchedule.setRemarks(messages.get("auto-saved"));
				activeSchedule.setUser(getCurUser());
				scheduleDAO.saveSchedule(activeSchedule);
			}
			
			schd.setActive(true);
			schd.setCreateDate(new Date());
			schd.setUser(getCurUser());
			schd.setProject(curProj);
			scheduleDAO.saveSchedule(schd);
			appState.recordInfoMsg(messages.format("added-miletone-x-phase-y-task-z", numM, numP, numT));
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
			appState.recordErrorMsg(e.getMessage());
			return null; //stay in same page
		} 
		catch (InvalidFormatException e) {
			e.printStackTrace();
			appState.recordErrorMsg(e.getMessage());
			return null; //stay in same page
		} 
		catch (IllegalStateException e) {
			e.printStackTrace();
			appState.recordErrorMsg(e.getMessage());
			return null; //stay in same page
		} 
        
        return linkSource.createPageRenderLinkWithContext(ScheduleManage.class, curProj.getId());
	}
	
	
	
	

}
