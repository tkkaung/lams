package ntu.celt.eUreka2.pages.admin.project;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeProject;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.data.ViewMode;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.Privilege;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjStatus;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.pages.admin.AdminIndex;
import ntu.celt.eUreka2.pages.modules.elog.Submit;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.UserDAO;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

public class BatchCreateProject  extends AbstractPageAdminProject{
	@Property
    private UploadedFile file;
    private final char seperator = ',';
	private final int numColumn = 9;
	private String LINE_SEPARATOR = System.getProperty("line.separator");
	@Property
	private School school;
	@Property
	private ProjType ptype;
	
	
	@Inject
	private Logger logger;
	@Inject
	private RequestGlobals requestGlobal;
	@Inject
	private UserDAO userDAO;
	@Inject
	private SchoolDAO schoolDAO;
	@Inject
	private ProjTypeDAO projTypeDAO;
	@Inject
	private SysRoleDAO sysRoleDAO;
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private Messages messages;
	@Inject
	private ProjStatusDAO projStatusDAO;

	@Component (id="form")
	private Form form;
	private File savedFile;
	
	@Persist(PersistenceConstants.FLASH)
    @Property
	private ViewMode mode;
	@SuppressWarnings("unused")
	@Persist(PersistenceConstants.FLASH)
    @Property
    private String message;
	@SuppressWarnings("unused")
	@Persist(PersistenceConstants.CLIENT)
	@Property
    private String processLog;
	
	void setupRender(){
		if(!canBatchCreateProject()) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		if(mode==null)
			mode = ViewMode.VIEW;
	}
	
	

	public void onValidateFormFromForm() throws IOException{
		if(! file.getFileName().toLowerCase().endsWith(".csv")){
			form.recordError(messages.format("incorrect-file-extension-x", ".csv"));  
		}
		else{
	    	String toSaveFolder = System.getProperty("java.io.tmpdir"); //get OS current temporary directory
	        String prefix = requestGlobal.getHTTPServletRequest().getSession(true).getId();
	    	savedFile = new File(toSaveFolder+ "/"+ prefix + file.getFileName());
	        file.write(savedFile);
	       
	        FileInputStream fis = new FileInputStream(savedFile);
	        BufferedInputStream bis = new BufferedInputStream(fis); //use buffer to improve reading speed
	        DataInputStream in = new DataInputStream(bis);
		    	
	    	CSVReader csvReader = new CSVReader(new InputStreamReader(in), seperator);
	        String[] nextLine;
	        nextLine = csvReader.readNext(); //read the first row, (the title row)
	        in.close();
        	
	        if(nextLine == null || nextLine.length != numColumn){
	        	savedFile.delete();
	        	form.recordError(messages.format("incorrect-input-format-require-x-found-y", numColumn, (nextLine==null? 0 : nextLine.length))); 
	        }
		}
	}
	
	@CommitAfter
     void onSuccessFromForm() 
    {
		//get Role name for Leader
		//get Role name for Student
		ProjRole leaderRole = new ProjRole();
		ProjRole studentRole = new ProjRole() ;
		for(ProjRole pr : ptype.getRoles()){
			if(pr.getPrivileges().contains(new Privilege(PrivilegeProject.IS_LEADER))){
				leaderRole = pr;
				break;
			}
		}
		for(ProjRole pr : ptype.getRoles()){
			if(!pr.getPrivileges().contains(new Privilege(PrivilegeProject.IS_LEADER))){
				studentRole = pr;
				break;
			}
		}
		
		
        int numIgnore = 0;
        int numReplace = 0;
        int numAdd = 0;
        StringBuffer processLog = new StringBuffer();
        String msg;
		
    	FileInputStream fis;
		try {
			fis = new FileInputStream(savedFile);
		
        BufferedInputStream bis = new BufferedInputStream(fis); //use buffer to improve reading speed
        DataInputStream in = new DataInputStream(bis);

    	CSVReader csvReader = new CSVReader(new InputStreamReader(in), seperator);
        String[] nextLine;
        nextLine = csvReader.readNext(); //ignore the first row, (the title row)
        
        
        processLog.append("Start time: "+ new Date() + LINE_SEPARATOR);

        while((nextLine = csvReader.readNext()) != null){
        	if(nextLine.length != numColumn){
        		msg = "Ignore row becuase number of column is invalid, require "
    				+ numColumn + ", found " + nextLine.length;
        		logger.info(msg);
        		processLog.append(msg+LINE_SEPARATOR);
        		numIgnore++;
        		continue;  //ignore if the line is invalid
        	}
        	
        	String num = nextLine[0].trim();
        	String studentName = MyStringTrim(nextLine[1]);
        	String studentMatric = MyStringTrim(nextLine[2]);
        	String projTitle = MyStringTrim(nextLine[3]);
        	String SupervisorName = MyStringTrim(nextLine[4]);
        	String SupervisorIC =  MyStringTrim(nextLine[5]);
        	String projStartDate =  MyStringTrim(nextLine[6]);
        	String projEndDate =  MyStringTrim(nextLine[7]);
        	String projID =  MyStringTrim(nextLine[8]);
        	
        	
        	User student = userDAO.getUserByExKey(studentMatric);
			if(student==null){ //studentMatric not found
				msg = "Ignore row because Student Matric not found: ."+studentMatric +"." + arrayToString(nextLine);
        		logger.info(msg);
        		processLog.append(msg+LINE_SEPARATOR);
        		numIgnore++;
        		continue;  //ignore this row
			}
			
			User supervisor = userDAO.getUserByExKey(SupervisorIC);
			if(supervisor==null){ //studentMatric not found
				msg = "Ignore row because Supervisor IC not found: " + arrayToString(nextLine);
        		logger.info(msg);
        		processLog.append(msg+LINE_SEPARATOR);
        		numIgnore++;
        		continue;  //ignore this row
			}
			
    		Date sdate = Util.stringToDate(projStartDate, "dd-MMM-yy");
    		if(sdate==null){ //studentMatric not found
				msg = "Ignore row because Project-Start-Date is invalid format: " + arrayToString(nextLine);
        		logger.info(msg);
        		processLog.append(msg+LINE_SEPARATOR);
        		numIgnore++;
        		continue;  //ignore this row
			}
			
    		Date edate = Util.stringToDate(projEndDate, "d-MMM-yy");
    		if(edate==null){ //studentMatric not found
				msg = "Ignore row because Project-End-Date is invalid format: " + arrayToString(nextLine);
        		logger.info(msg);
        		processLog.append(msg+LINE_SEPARATOR);
        		numIgnore++;
        		continue;  //ignore this row
			}
        	
    		if( edate.before(sdate)){
    			msg = "Ignore row because Project-End-Date is before Project-Start-Date: " + arrayToString(nextLine);
        		logger.info(msg);
        		processLog.append(msg+LINE_SEPARATOR);
        		numIgnore++;
        		continue;  //ignore this row
    		}
    		
    		
    		
    		
    		
        	if ( ! "".equals(projID) ) {
        		//attempt to REPLACE
        		
        		Project proj = projDAO.getProjectById(projID);
        		if (proj == null ){ //projID not found
        			msg = "Ignore row because ProjectID not found: " + arrayToString(nextLine);
            		logger.info(msg);
            		processLog.append(msg+LINE_SEPARATOR);
            		numIgnore++;
            		continue;  //ignore this row
        		}
    		
        		//check if curUser has privilege to change the project
    			if(! canEditProject(proj)){
    				msg = "Ignore row because No privilege to edit the project: " + arrayToString(nextLine);
            		logger.info(msg);
            		processLog.append(msg+LINE_SEPARATOR);
            		numIgnore++;
            		continue;  //ignore this row
    			}
    			
    			proj.setName(projTitle);
    			proj.setSdate(sdate);
    			proj.setEdate(edate);
    			proj.setMdate(new Date());
    			proj.setEditor(getCurUser());
    			if(!proj.getUsers().contains(student)){
    				proj.addMember(new ProjUser(proj, student, studentRole));	
    			}
    			if(!proj.getUsers().contains(supervisor)){
    				proj.addMember(new ProjUser(proj, supervisor, leaderRole));	
    			}
    			projDAO.saveProject(proj);
    			
    			msg = "replace row : " + arrayToString(nextLine);
        		logger.info(msg);
        		processLog.append(msg+LINE_SEPARATOR);
        		numReplace++;
        	}
        	else{
        		//attempt to INSERT
        		
        		//check if project already exist
        		List<Project> projList = projDAO.searchProjects(projTitle, supervisor, leaderRole, sdate);
        		Project proj = null;
        		if(projList.size()>0){
        			proj = projList.get(0);
        		}
        		if(proj==null){
        			//create project
        			proj = new Project();
        			proj.setName(projTitle);
        			String id = projDAO.generateId(ptype, school, sdate);
        			proj.setId(id);
        			proj.setCdate(new Date());
        			proj.setCreator(getCurUser());
        			proj.setEditor(getCurUser());
        			proj.setMdate(new Date());
        			proj.setSchool(school);
        			proj.setType(ptype);
        			proj.setSdate(sdate);
        			proj.setEdate(edate);
        			proj.setShared(false);
        			ProjStatus active = projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_ACTIVE);
        			proj.setStatus(active);
        			
        			List<Module> defaultModules = ptype.getDefaultModules();
        			for(Module m : defaultModules){
        				proj.addModule(new ProjModule(proj, m));
        			}
        			proj.addMember(new ProjUser(proj, student, studentRole));	
        			proj.addMember(new ProjUser(proj, supervisor, leaderRole));	
        			projDAO.saveProject(proj);
        			
        			msg = "add row (create project "+ id +"): " + arrayToString(nextLine);
            		logger.info(msg);
            		processLog.append(msg+LINE_SEPARATOR);
            		numAdd++;
        		}
        		else{
        			//just enroll student
        			if(!proj.getUsers().contains(student)){
        				proj.addMember(new ProjUser(proj, student, studentRole));	
        				
        				proj.setMdate(new Date());
            			proj.setEditor(getCurUser());
            			projDAO.saveProject(proj);
            			msg = "add row (enroll user to project "+ proj.getId()+"): " + arrayToString(nextLine);
                		logger.info(msg);
                		processLog.append(msg+LINE_SEPARATOR);
                		numAdd++;
                		continue;  //ignore this row
        			}else{
        				msg = "Ignore row because The student already in the project: " + arrayToString(nextLine);
                		logger.info(msg);
                		processLog.append(msg+LINE_SEPARATOR);
                		numIgnore++;
                		continue;  //ignore this row
        			}
        				
        		}
        		
        		
        	}
       
        }
		in.close();
		savedFile.delete();
		 } catch (FileNotFoundException e) {
				e.printStackTrace();
				processLog.append("FileNotFoundException: "+ e.getMessage() + LINE_SEPARATOR);
				
			} catch (IOException e) {
			e.printStackTrace();
			processLog.append("IOException: "+ e.getMessage() + LINE_SEPARATOR);
			
		}
		    
		//appState.recordInfoMsg(processLog.toString());
//		batchCreateResultPage.setContext(processLog.toString());
//		return batchCreateResultPage;
	//	batchCreateResultPage.setContext("aaaaaaa");
	//	return batchCreateResultPage;
		message = "Added: " +numAdd + ", Ignored: "+numIgnore + ", Replaced: "+numReplace;
        processLog.append("Finish time: "+ new Date() + LINE_SEPARATOR);
		this.processLog = processLog.toString();
		mode = ViewMode.RESULT;
    }
	
	public String MyStringTrim(String string){
		return string.replace(String.valueOf((char) 160), " ").trim();
	}
	
	
    Object onUploadException(FileUploadException ex)
    {
        form.recordError("Upload exception: " + ex.getMessage());

        return this;
    }
    
    
    
    

	public boolean canManageProject(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA))
			return true;
		return false;
	}
	public boolean canBatchCreateProject(){
		if(canManageProject())
			return true;
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SCHOOL_RUBRIC)) //schoolAdmin
			return true;
		
		return false;
	}
	public boolean canEditProject(Project proj){
		if(canManageProject())
			return true;
		
		List<Integer> schIDList = getAccessibleSchlIDs(getCurUser());
		if (schIDList.contains(proj.getSchool().getId()))
			return true;
		
		return false;
	}
	
	protected List<Integer> getAccessibleSchlIDs(User u){
		List<Integer> idList = new ArrayList<Integer>();
		SysRole sysRole = sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_SCHOOL_ADMIN);
		for(SysroleUser srUser : u.getExtraRoles()){
			if(srUser.getSysRole().equals(sysRole)){
				idList.add(srUser.getParam());
			}
		}
		return idList;
	}
	public SelectModel getProjTypeModel() {
		List<OptionModel> optionList = new ArrayList<OptionModel>();
		List<ProjType> typeList = projTypeDAO.getAllTypes();
		for (ProjType type: typeList) {
			OptionModel option = new OptionModelImpl(type.getDisplayName(), type);
			optionList.add(option);
		}
		SelectModel selModel = new SelectModelImpl(null, optionList);
		return selModel;
	}
	public String arrayToString(Object[] objArray){
		String output = "";
		String separator = ", ";
		for(Object obj : objArray){
			output += obj.toString() + separator;
		}
		output = Util.removeLastSeparator(output, separator);
		return output;
	}
	
	@Inject
	private Block viewModeBlock;
    @Inject
	private Block resultModeBlock;
    public Block getDisplayModeBlock(){
    	if(ViewMode.VIEW.equals(mode))
    		return viewModeBlock;
    	else if(ViewMode.RESULT.equals(mode))
    		return resultModeBlock;
    	return null;
    }
    
    
    @Inject
	private Block logDetails;
    @Inject
	private Block logDetailsDefault;
    Block onShowLog(){
    	return logDetails;
    }
    Block onHideLog(){
    	return logDetailsDefault;
    }
}
