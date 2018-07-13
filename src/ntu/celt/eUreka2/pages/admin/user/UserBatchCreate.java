package ntu.celt.eUreka2.pages.admin.user;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.data.ViewMode;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.UserDAO;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

public class UserBatchCreate  extends AbstractPageAdminUser{
	@Property
    private UploadedFile file;
    private final char seperator = ',';
	private final int numColumn = 13;
	@Property
	private String whenExistRadio = "Ignore";
	private String LINE_SEPARATOR = System.getProperty("line.separator");

	@Inject
	private Logger logger;
	@Inject
	private RequestGlobals requestGlobal;
	@Inject
	private UserDAO userDAO;
	@Inject
	private SchoolDAO schoolDAO;
	@Inject
	private SysRoleDAO sysRoleDAO;
	@Inject
	private Messages messages;
	
	@Component (id="form")
	private Form form;
	private File savedFile;
	@SuppressWarnings("unused")
	@Property
	private StringValueEncoder stringEncoder = new StringValueEncoder();
	
	
	@Persist(PersistenceConstants.FLASH)
    @Property
	private ViewMode mode;
	@SuppressWarnings("unused")
	@Persist(PersistenceConstants.FLASH)
    @Property
    private String message;
	@SuppressWarnings("unused")
	@Persist
	@Property
    private String processLog;

	void setupRender(){
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_USER)) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		if(mode==null)
			mode = ViewMode.VIEW;
	}
	
	void onPrepareFromForm(){
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
    public void onSuccessFromForm() throws IOException
    {
    	FileInputStream fis = new FileInputStream(savedFile);
        BufferedInputStream bis = new BufferedInputStream(fis); //use buffer to improve reading speed
        DataInputStream in = new DataInputStream(bis);

    	CSVReader csvReader = new CSVReader(new InputStreamReader(in), seperator);
        String[] nextLine;
        nextLine = csvReader.readNext(); //ignore the first row, (the title row)
        
        Date date = new Date();
        int numIgnore = 0;
        int numReplace = 0;
        int numAdd = 0;
        StringBuffer processLog = new StringBuffer();
        String msg;
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
        	
        	User u = new User();
        	u.setExternalKey(nextLine[0]);
        	u.setUsername(nextLine[1]);
        	if("".equals(u.getUsername())){
        		msg = "Ignore row because Username is empty";
        		logger.info(msg);
        		processLog.append(msg+LINE_SEPARATOR);
        		numIgnore++;
        		continue;  //ignore this row
        	}
        	u.setFirstName(nextLine[2]);
        	u.setLastName(nextLine[3]);
        	u.setJobTitle(nextLine[4]);
        	School s;
        	if(!nextLine[5].isEmpty()){
	    		s = schoolDAO.getSchoolByName(nextLine[5]);
	        	if(s==null){
	        		msg = "Ignore row for Username="+u.getUsername() 
						+ ", because School Name is invalid, School Name="+nextLine[5];
	        		logger.info(msg);
	        		processLog.append(msg+LINE_SEPARATOR);
	        		numIgnore++;
	        		continue;  //ignore this row
	        	}
	        	u.setSchool(s);
        	}
        	//u.setExtUser(Boolean.parseBoolean(nextLine[6]));
        	boolean isExtUser = Boolean.parseBoolean(nextLine[6]);
        	u.setOrganization(nextLine[7]);
        	u.setPhone(nextLine[8]);
        	u.setMphone(nextLine[9]);
        	u.setEmail(nextLine[10]);
        	if(!nextLine[11].isEmpty()){
    	    	SysRole r = sysRoleDAO.getSysRoleByName(nextLine[11]);
	        	if(r==null){
	        		msg = "Ignore row for Username="+u.getUsername()
						+ ", because Role is invalid, Role="+nextLine[11];
		    		logger.info(msg);
		    		processLog.append(msg+LINE_SEPARATOR);
	        		numIgnore++;
	        		continue;  //ignore this row
	        	}
	        	u.setSysRole(r);
        	}
        	u.setEnabled(true); //set enabled by default
        	u.setRemarks(nextLine[12]);
        	if(isExtUser){
        		u.setPassword(Util.generateDefaultPassword(u));
        	}
        	if(userDAO.isUsernameExist(u.getUsername())){
        		if(whenExistRadio.equalsIgnoreCase("Ignore")){
        			msg = "Ignore row for Username="+u.getUsername() + ", because User already exist";
        			logger.info(msg);
		    		processLog.append(msg+LINE_SEPARATOR);
        			numIgnore++;
        		}
        		else if(whenExistRadio.equalsIgnoreCase("Replace")){
        			User user = userDAO.getUserByUsername(u.getUsername()); 
        			if(! u.getExternalKey().isEmpty())
        				user.setExternalKey(u.getExternalKey());
        			//user.setPassword(u.getPassword()); //do not replace the password
        			if(! u.getFirstName().isEmpty())
            			user.setFirstName(u.getFirstName());
        			if(! u.getLastName().isEmpty())
            			user.setLastName(u.getLastName());
        			if(! u.getJobTitle().isEmpty())
            			user.setJobTitle(u.getJobTitle());
        			if(u.getSchool() != null)
            			user.setSchool(u.getSchool());
        			if(! u.getOrganization().isEmpty())
            			user.setOrganization(u.getOrganization());
        			if(! u.getPhone().isEmpty())
            			user.setPhone(u.getPhone());
        			if(! u.getMphone().isEmpty())
            			user.setMphone(u.getMphone());
        			if(! u.getEmail().isEmpty())
            			user.setEmail(u.getEmail());
        			if(u.getSysRole() != null)
            			user.setSysRole(u.getSysRole());
                	//user.setEnabled(u.isEnabled()); //do not replace the 'Enabled'
        			if(! u.getRemarks().isEmpty())
            			user.setRemarks(u.getRemarks());
                	//user.setCreateDate(u.getCreateDate()); //do not replace the 'createDate'
                	user.setModifyDate(date);
        			userDAO.save(user);
        			msg = "Replace user with Username="+u.getUsername() + ", because User already exist";
        			logger.info(msg);
		    		processLog.append(msg+LINE_SEPARATOR);
        			numReplace++;
        		}
        	}
        	else{
        		u.setCreateDate(date);
        		u.setModifyDate(date);
        		userDAO.save(u);
        		msg = "Added user with Username="+u.getUsername();
    			logger.info(msg);
	    		processLog.append(msg+LINE_SEPARATOR);
        		numAdd++;
        	}
        }
		in.close();
		savedFile.delete();
		message = "Added: " +numAdd + ", Ignored: "+numIgnore + ", Replaced: "+numReplace;
        processLog.append("Finish time: "+ new Date() + LINE_SEPARATOR);
		this.processLog = processLog.toString();
		mode = ViewMode.RESULT;
    }
    

    Object onUploadException(FileUploadException ex)
    {
        form.recordError("Upload exception: " + ex.getMessage());

        return this;
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
