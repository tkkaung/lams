package ntu.celt.eUreka2.services;

/**
 * based on http://code.google.com/p/t5-restful-webservices/wiki/GettingStarted
 * <p>
 * Usage: 
 * 	- create a function :
 * 	<p>the function must, 
 * 		1. Be void return type
 * 		2. Be annotated with the @RestfulWebMethod annotation 
 *    	3. Take the Tapestry Request and Response objects as its first two arguments, in that order
 *  <p> 
 *  - access URL:   If the base URL for our Tapestry 5 web application is http://myapp.example.org/  
 *    (i.e., the root context path is "/") then the URL for our web service method is
 *    http://myapp.example.org/<class-map-key-configured-in-appmodule>/<lowercase-method-name>/args...
 *    e.g., http://myapp.example.org/my-web-service/foo/42/true 
 */

/*
 * - For *DAO to be able to 'commit', @CommitAfter should be added to the methods of those DAO, 
 *   and adviseTransactions() is defined in AppModule.java
 *   refer to tapestry-hibernate user guide:
 *   http://tapestry.apache.org/tapestry5/tapestry-hibernate/userguide.html
 */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ntu.celt.eUreka2.dao.ProjExtraInfoDAO;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeProject;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjCAOExtraInfo;
import ntu.celt.eUreka2.entities.ProjFYPExtraInfo;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjStatus;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.big5.BIG5DAO;
import ntu.celt.eUreka2.modules.big5.BIG5Survey;
import ntu.celt.eUreka2.modules.teameffectiveness.TEDAO;
import ntu.celt.eUreka2.modules.teameffectiveness.TESurvey;
import ntu.celt.eUreka2.modules.care.CAREDAO;
import ntu.celt.eUreka2.modules.care.CARESurvey;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupDAO;
import ntu.celt.eUreka2.modules.group.GroupUser;
import ntu.celt.eUreka2.modules.ipsp.IPSPDAO;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurvey;
import ntu.celt.eUreka2.modules.lcdp.LCDPDAO;
import ntu.celt.eUreka2.modules.lcdp.LCDPSurvey;
import ntu.celt.eUreka2.modules.message.MessageDAO;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationUser;
import ntu.celt.eUreka2.modules.profiling.Profiling;
import ntu.celt.eUreka2.modules.profiling.ProfilingDAO;
import ntu.celt.eUreka2.modules.scheduling.Milestone;
import ntu.celt.eUreka2.modules.scheduling.ReminderType;
import ntu.celt.eUreka2.modules.scheduling.Schedule;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.modules.scheduling.Task;
import ntu.celt.eUreka2.modules.scheduling.UrgencyLevel;
import ntu.celt.eUreka2.modules.usage.UsageDAO;
import ntu.celt.eUreka2.pages.admin.project.ManageAdminMember;
import ntu.celt.eUreka2.pages.admin.project.ManageAdminProjects;
import ntu.celt.eUreka2.pages.admin.project.ProjectAdminEdit;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;

import org.apache.commons.lang.time.DateUtils;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.joda.time.LocalDate;
import org.slf4j.Logger;

import us.antera.t5restfulws.RestfulWebMethod;

public class RunTaskService {
	private UserDAO userDAO;
	private SchoolDAO schoolDAO;
	private SysRoleDAO sysRoleDAO;
	private Logger logger;
	private ProjectDAO projDAO;
	private MessageDAO msgDAO;
	private ProjRoleDAO projRoleDAO;
	private ProjTypeDAO projTypeDAO;
	private ProjStatusDAO projStatusDAO;
	private ProjExtraInfoDAO projExtraInfoDAO;
	private SchedulingDAO schdlDAO;
	private UsageDAO usageDAO;
	private EmailManager emailManager;
	private EvaluationDAO evalDAO;
	private ProfilingDAO profDAO;
	private LCDPDAO lcdpDAO;
	private CAREDAO careDAO;
	private BIG5DAO big5DAO;
	private TEDAO teDAO;
	private IPSPDAO ipspDAO;
	
	@SessionState
	private ProjectAdminEdit projadmedit;
	@Inject
	private Messages _messages;
	@Inject
	private PageRenderLinkSource linkSource;
	
	private User user;
	private ProjRole role;

	@SessionState
	private AppState appState;
	
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;

	@Inject
	private WebSessionDAO webSessionDAO;
	@Inject
	private GroupDAO groupDAO;
	

	@Cached
	public User getCurUser() {
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	/*
	 * Receive all the services needed as constructor arguments. 
	 * When we bind this service, T5 IoC will provide all the services! (It works like using @Inject)
	 */
	public RunTaskService(UserDAO userDAO, SchoolDAO schoolDAO,
			SysRoleDAO sysRoleDAO, Logger logger, ProjectDAO projDAO,
			MessageDAO msgDAO, ProjRoleDAO projRoleDAO,
			ProjTypeDAO projTypeDAO, ProjStatusDAO projStatusDAO,
			ProjExtraInfoDAO projExtraInfoDAO, SchedulingDAO schdlDAO,
			UsageDAO usageDAO, EmailManager emailManager, EvaluationDAO evalDAO, 
			ProfilingDAO profDAO, LCDPDAO lcdpDAO, CAREDAO careDAO, BIG5DAO big5DAO, TEDAO teDAO, IPSPDAO ipspDAO
			) {
		super();
		this.userDAO = userDAO;
		this.schoolDAO = schoolDAO;
		this.sysRoleDAO = sysRoleDAO;
		this.logger = logger;
		this.projDAO = projDAO;
		this.msgDAO = msgDAO;
		this.projRoleDAO = projRoleDAO;
		this.projTypeDAO = projTypeDAO;
		this.projStatusDAO = projStatusDAO;
		this.projExtraInfoDAO = projExtraInfoDAO;
		this.schdlDAO = schdlDAO;
		this.usageDAO = usageDAO;
		this.emailManager = emailManager;
		this.evalDAO = evalDAO;
		this.profDAO = profDAO;
		this.lcdpDAO = lcdpDAO;
		this.careDAO = careDAO;
		this.big5DAO = big5DAO;
		this.teDAO = teDAO;
		this.ipspDAO = ipspDAO;
	}
	
	private boolean parseEnabled(String str){
		if(str.equalsIgnoreCase("enabled"))
			return true;
		return false;
	}
	
	/**
	 * import users from snapshot files, the location and options in config in config.properties
	 */
	@RestfulWebMethod 
	public void runImportUser(Request request, Response response) throws IOException{
		PrintWriter writer = response.getPrintWriter("text/plain");
		
		String separator = Config.getString("QScheduler.Import.separator");
		int startFromRow = Config.getInt("QScheduler.ImportUser.startFromRow");
		boolean replaceExistRow = Config.getBoolean("QScheduler.ImportUser.replaceExistRow");
		String[] fileName = {"",""};
		fileName[0] = Config.getString("QScheduler.ImportUser.file1");
		fileName[1] = Config.getString("QScheduler.ImportUser.file2");
		School otherSchool = schoolDAO.getSchoolByName(PredefinedNames.SCHOOL_OTHERS);
		
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream in = null;
		BufferedReader br = null;
		try{
			
			for(String fName : fileName){
				try{
					int numReplace = 0;
					int numIgnore = 0;
					int numAdd = 0;
					
					 fis = new FileInputStream(fName);
					 bis = new BufferedInputStream(fis); 
			         in = new DataInputStream(bis);
			
			         br = new BufferedReader(new InputStreamReader(in));
					String strLine;
			        
			        logger.info("Start process "+ fName);
			        
			        //new_external_person_key	external_person_key	user_id	system_role	passwd	title	firstname	lastname	email	email_ind	department	job_title	m_phone	work_ind	institution_role	row_status	public_ind	available_ind

			        String[] nextLine;
			        int i = 0;
			        List<User> uList = new ArrayList<User>();
			        while((strLine = br.readLine()) != null){
			        	i++;
			        	if(i%1000==0)
			        		logger.info("importUser: numReplace="+numReplace+", numAdd="+numAdd +", processing row i="+i+".....");
			        	
			        	if(i<startFromRow)
				        	continue;
			        	
			        	nextLine = strLine.split(separator);
			        	if(nextLine.length<18){
			        		logger.warn("number of field incorrect: "+nextLine.length +" strLine="+ strLine);
			        		numIgnore++;
			        		continue;
			        	}
			        	for(int j=0; i<nextLine.length; i++){
			        		nextLine[j] = nextLine[j].trim();
			        	}
			        		
			        	User u = new User();
			        	u.setExternalKey(nextLine[0]);
			        	//ignore external_person_key
			        	u.setUsername(nextLine[2]);
			        	if(u.getUsername().isEmpty()){
			        		logger.warn("Username is empty. This row is ignored. Row="+ i +", ExternalKey="+u.getExternalKey());
			        		numIgnore++;
			        		continue;
			        	}
			        	
			        	if(userDAO.isUsernameExist(u.getUsername())){
			        		if(!replaceExistRow){
			        			numIgnore++;
				        		continue;
			        		}
			        	}
			        	
			        	//ignore system_role
			        	//ignore passwd
			        	u.setTitle(nextLine[5]);
			        	u.setFirstName(nextLine[6]);
			        	u.setLastName(nextLine[7].trim());
			        	u.setEmail(nextLine[8]); //be aware some email may be empty
			        	//ignore email_ind
			        	
			        	String deptFullName = nextLine[10];
			        	if(!deptFullName.isEmpty()){
			        		School s = schoolDAO.getFirstSchoolByDescription(deptFullName);
				        	if(s==null){
				        		//if not defined, add new
				        		s = new School();
				        		String nextSchlName = schoolDAO.getNextDefaultName("Dept_");
				        		s.setName(nextSchlName);
				        		s.setDes(deptFullName);
				        		s.setSystem(false);
				        		logger.warn("School/Dept not found. New School/Dept created, you should change its abbreviation. Full name="+ deptFullName);
				        		schoolDAO.save(s);
					        }
				        	u.setSchool(s);
				        }
			        	else{
			        		u.setSchool(otherSchool);
			        	}
			        	
			        	
			        	u.setJobTitle(nextLine[11]);
			        	//u.setMphone(nextLine[12]);
			        	//ignore work_ind
			        	
			        	String intitutionRole = nextLine[14];
			        	if("ROLE_10".equals(intitutionRole))
			        		intitutionRole = "FACULTY";
			        	
			        	SysRole r = sysRoleDAO.getSysRoleByName(intitutionRole);
			        	if(r==null){
			        		logger.warn("Institution_role not found. ("+nextLine[14]+"). This row is ignored,"+ " Row="+i+ ", username="+u.getUsername() );
					        numIgnore++;
			        		continue;  //ignore this row
			        	}
			        	u.setSysRole(r);
			        	u.setEnabled(parseEnabled(nextLine[15])); //set enabled by default
			        	//ignore public_ind
			        	//ignore available_ind
			        	//u.setPassword(Util.generateDefaultPassword(u));
			        	
			        	
			        	/*
			        	u.setOrganization(nextLine[7]);
			        	u.setPhone(nextLine[8]);
			        	u.setRemarks(nextLine[12]);
			        	*/
			        	
			        	
			        	if(userDAO.isUsernameExist(u.getUsername())){
		        		 	User user = userDAO.getUserByUsername(u.getUsername()); 
		        			user.setExternalKey(u.getExternalKey());
		        			//user.setPassword(u.getPassword()); //do not replace the password
		                	user.setTitle(u.getTitle());
		        			user.setFirstName(u.getFirstName());
		                	user.setLastName(u.getLastName());
		                	user.setJobTitle(u.getJobTitle());
		                	user.setSchool(u.getSchool());
		                	user.setOrganization(u.getOrganization());
		               // 	user.setPhone(u.getPhone());
		               // 	user.setMphone(u.getMphone());
		                 	user.setPhone("");  //do not import this
		                	user.setMphone(""); //do not import this
		                	user.setEmail(u.getEmail());
		                	//user.setSysRole(u.getSysRole());
		                	//user.setEnabled(u.isEnabled()); //do not replace the 'Enabled'
		                	//user.setRemarks(u.getRemarks());
		                	//user.setCreateDate(u.getCreateDate()); //do not replace the 'createDate'
		                	user.setModifyDate(new Date());
		        		
		        			//userDAO.save(user);
		                	uList.add(user);
		        				
		        			numReplace++;
		        		}
			        	else{
			        		u.setCreateDate(new Date());
			        		u.setModifyDate(new Date());
			        		//userDAO.save(u);
			        		uList.add(u);
			        		
			        		numAdd++;
			        	//	logger.info("numAdd="+numAdd +", i="+i+" rows processed");
			        	}
			        	
			        	if(uList.size()%30==0){
			        		userDAO.saveBatch(uList);
			        		uList.clear();
			        	}
			        	
			        }
			        userDAO.saveBatch(uList);
	        		uList.clear();
			        
			        logger.info("Finished "+ fName +" , "+numIgnore+" ignored, "+numAdd+" added, "+ numReplace+" replaced.");
			        writer.println("Finished "+ fName +" , "+numIgnore+" ignored, "+numAdd+" added, "+ numReplace+" replaced.");
					writer.flush();
					
				}
				catch (FileNotFoundException ex){
					logger.error("Cannot find Users file to import. "+fName);
					writer.println("Cannot find Users file to import. "+fName);
					writer.flush();
				}
			}
			
		}
		catch(IOException ex){
			logger.error(ex.getMessage());
			writer.println(ex.getMessage());
			writer.flush();
		}
		/*
		try{
			
		DataSync();
		}catch(IOException ex){
		
		}
		*/
		finally{
			DataSync();
			if(br !=null){
				br.close();
				br = null;
			}
			if(in !=null){
				in.close();
				in = null;
			}
			if(bis !=null){
				bis.close();
				bis = null;
			}
			if(fis!=null){
				fis.close();
				fis = null;
			}
		}
		
	}
	
	
	@SuppressWarnings("null")
	public static final List<String> findInFile(final Path file, final String pattern, 
			final int flags)
					throws IOException
	{

		final List<String> userlist = new ArrayList<String>();
		final Pattern p = Pattern.compile(pattern, flags);
		final Logger logger = null;
		String line;
		Matcher m;
		//final BufferedReader br = Files.newBufferedReader(path);
		try {

			BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8);;

			while ((line = br.readLine()) != null) {
				m = p.matcher(line);
				while (m.find()) {
					//System.out.println("m.group() = "+m.group()+ ", reading file = "+line );

					//sb.append(m.group());
					userlist.add(line);
					//System.out.println("Fn = "+line);
				}
			}


		} catch (IOException e) { 
			logger.error(e.getMessage());

			// do something 
			e.printStackTrace(); 
		}
		return userlist;
	}
	public boolean isCreateMode(String projId) {
		if(projId == null)
			return true;
		return false;
	}
	
	@CommitAfter
	public Project  AddProject(String name,String courseId,String courseCode,String desc,
			String schoolname,Date startdate,Date enddate, String groupId, String term, String keywords,String remarks) {
		Project myproj = new Project();

		try {
		String projId = null;

		myproj.setType(projTypeDAO.getTypeByName(PredefinedNames.PROJTYPE_COURSE));
		myproj.setName(name);
		myproj.setCdate(new Date());
		myproj.setCourseId(courseId);
		myproj.setCourseCode(courseCode);
		myproj.setDescription(desc);
		
		School school = schoolDAO.getSchoolByName(schoolname); //NBS
		myproj.setSchool(school);

		myproj.setRemarks(remarks);
		myproj.setTerm(term);
		
		myproj.setSdate(startdate);
		
		myproj.setEdate(enddate);
		myproj.setMdate(new Date());
		
		
		myproj.setStatus(projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_ACTIVE));
		myproj.setLastStatusChange(new Date());
		
		
		User curuser = userDAO.getUserById(1);
		
		myproj.setGroupId(groupId);
		
		if(isCreateMode(projId)){
			myproj.setCdate(new Date());
			myproj.setCreator(curuser);
			
			List<Module> defaultModules = myproj.getType().getDefaultModules();
			for(Module m : defaultModules){
				myproj.addModule(new ProjModule(myproj, m));
			}
			
		}
		myproj.setEditor(curuser);
		myproj.setMdate(new Date());
			
		if (keywords!=null) {
			String keywordsStr[] = keywords.split(",");
			for(String keyword : keywordsStr){
				keyword = keyword.trim();
				if(!keyword.isEmpty())
					myproj.addKeywords(keyword);
			}
		}
		
		System.out.println("Project creating... (save)");
		projDAO.immediateSaveProject(myproj);
		System.out.println("Project created");
		
		} catch(Exception e) {
			logger.error(e.getMessage());
		}
		return myproj;
	}
	
	@CommitAfter
	public Object DataSync() throws IOException {
	//public Object onSuccessFromForm() throws IOException 
	System.out.println("eUreaka DataSync...");
	
	try {

		ProjRole studentRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_STUDENT);

		String fileName = Config.getString("QScheduler.ImportUser.eureka_enrol");
			
		
		int year = LocalDate.now().getYear();
		int month = LocalDate.now().getMonthOfYear();
		
		int y =   year % 100;
		CharSequence term_past2 = null;
		CharSequence term_past1 =  null;
		CharSequence term_current = null;
		if (month >=1 && month <= 7) { //Jan -July
			 term_past2 = y-1+"S1";
			 term_past1 =  y-1+"S2";
			 term_current = y+"S1";
			
		} else { // Aug - Dec
			 term_past2 = y-1+"S2";
			 term_past1 =  y+"S1";
			 term_current = y+"S2";
		}
		System.out.println("term_current = "+term_current);
		//////////////////////////////////
		/*
		List<String> lines = Collections.emptyList(); 
		try { 
			lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
			
		} catch (FileNotFoundException ex){
				logger.error("Cannot find Users file to import. "+fileName);
		} catch (IOException e) { 

			// do something 
			e.printStackTrace(); 
		}
		*/
		////////////////////////////
		String  schoolToSearch = "(NBS)";
		String  past1termToSearch = term_past1.toString();
		String  currenttermToSearch = term_current.toString();

		//List<Object> enrollist = new ArrayList<Object>();
		List<String>  enrolmatchedlines = findInFile(Paths.get(fileName),schoolToSearch, 1);
		//System.out.println("ProjID="+proj_id+"  CourseId = "+proj.getCourseId() + ",  DsiplayName = "+proj.getDisplayName() + ", enrolment nos = "+matchedlines.size());
		//projadmedit.onSuccessFromForm();
		///////
		
		int count = 0;
		//////////////////////////////////
		for (String line : enrolmatchedlines) {
			if (line.contains(past1termToSearch) || line.contains(currenttermToSearch)) {
				String[]  arrOfStr = line.split("\\|",-2);

				//USERNAME|CAMPUS|FULLNAME|EMAIL|USER_ROLE|COURSE_NAME|COURSE_CODE|ACAD_YR|SEM|COURSE_ID
				//SONAM002|NTU|SONAM SAHNI|SONAM002@ntu.edu.sg|STUDENT|B6027 - OPERATIONS (MBA) 2018/2019 Trimester 2 (Group 1)|B6027|2018|2|18S2-B6027-MBA-1|CoB (NBS)
				//String USERNAME = arrOfStr[0];
				
				String desc;
				String schoolname = "NBS";
				String groupId = null;
				String keywords = null;
				String remarks = "created by datasync";
				
				//String campus = arrOfStr[1];
				//String FULLNAME = arrOfStr[2];
				//String EMAIL = arrOfStr[3];
				//String USER_ROLE = arrOfStr[4];
				String COURSE_NAME =  arrOfStr[5];
				String COURSE_CODE = arrOfStr[6];
				String ACAD_YR = arrOfStr[7];
				String SEM = arrOfStr[8];
				
				String COURSE_ID = arrOfStr[9];

				//String school = arrOfStr[10];
				desc = COURSE_NAME;
				int ACADYR =   Integer(ACAD_YR) % 100;
				String term = ACADYR+SEM;

			    //enrollist.add(line.split("\\|",-2));
			    Date startdate = new Date();
			    Date enddate  = DateUtils.addMonths(new Date(), 12);
			    
			    List<Project> p1 = projDAO.getProjectsByCourseId(COURSE_ID);
				
				if (p1.size() <= 0) {
					count++; 
					Project p = AddProject(COURSE_ID,COURSE_ID,COURSE_CODE,desc,schoolname,startdate,enddate,groupId,term,keywords, remarks);
					System.out.println("p.courseId = "+p.getCourseId());
				}
			}
		}
		System.out.println(count +" projects created");
		//Create new projects based on if it not existing in eUreka 
		
		List<Project> projList = projDAO.getAllProjects(0, 10000);

		List<Project> projList_filered = new ArrayList<Project>();
		
		//CharSequence  stringToSearch = null;
		String  stringToSearch = null;

		String   teststr = null;
		
		for(int i=0; i<projList.size(); i++) {
			
			try {
			Project proj = projList.get(i);
			String proj_id = proj.getId();
			//System.out.println("i="+i);
			
			//CharSequence stringToSearch = "18S2-AD2101-SEM-2";

			teststr = proj.getCourseId();
			
			List<Integer> exceptType = new ArrayList<Integer>();	
			
			//System.out.println(" status = "+proj.getStatus().getId());
			
			//32768 active 32768
			if (proj.getStatus().getId() != 32768) { // Non-active projects skip (32768 is the active project status)
				continue;
			}

			//exceptType.add(2260993);  //SEN Senate
			//exceptType.add(2260992);  //CAO Career Affairs Office
			//exceptType.add(98304);    //ADH
			//exceptType.add(1867776);  //FYP
			//exceptType.add(5832704);  //Course
			//exceptType.add(22380544); // LAB	eLog Book	Digital Lab Log Book

			if (exceptType.contains(proj.getType().getId())) {
				//System.out.println("Adhoc type found proj.getType().getId() = "+proj.getType().getId());
				continue;

			} else {
				//System.out.println("Project not going to use");

				//continue;
				//System.out.println("Project type going to use");

				//System.out.println("project filtered"+projList_filered.size());
			}

			if (teststr != null && (teststr.contains(term_current) || teststr.contains(term_past1) || teststr.contains(term_past2))) {
				//System.out.println("Course ID>>"+proj.getCourseId() +" Display Name>>"+proj.getDisplayName());
				stringToSearch = proj.getCourseId();
				//System.out.println("proj.getCourseId() = "+proj.getCourseId());
				
				projList_filered.add(proj);
				//System.out.println("teststr = "+teststr);
			} else
				continue;

			} catch(Exception e) {
				logger.error(e.getMessage());
			}			

			/*
			CharSequence  stringToSearch = proj.getCourseId();
			//CharSequence stringToSearch = "18S2-AD2101-SEM-2";
			CharSequence term_filer = "18S1";
			String   teststr = proj.getCourseId();
			if (teststr != null && teststr.contains(term_filer)) {
				//System.out.println("Course ID>>"+proj.getCourseId() +" Display Name>>"+proj.getDisplayName());
				stringToSearch = proj.getCourseId();
				//stringToSearch= "18S1-AC3102-SEM-6";

			} else
				continue;
				*/
			
		}
		System.out.println("project full = "+projList.size());
		System.out.println("project filtered = "+projList_filered.size());
		
		for(int i=0; i<projList_filered.size(); i++) {

			Project proj = projList_filered.get(i);
			//System.out.println("No: of Projects="+projList.size());
			String proj_id = proj.getId();
			//System.out.println("ProjID="+proj_id);
			//System.out.println("ProjID="+proj_id+"  CourseId ="+proj.getCourseId());
			stringToSearch = proj.getCourseId()+"\\|";
			//CharSequence stringToSearch = "18S2-AD2101-SEM-2";
			
			/*
			 String   teststr = proj.getCourseId();
			
			CharSequence term_filer = "18S1";
			if (teststr != null && teststr.contains(term_filer)) {
				//System.out.println("Course ID>>"+proj.getCourseId() +" Display Name>>"+proj.getDisplayName());
				stringToSearch = proj.getCourseId();
				//stringToSearch= "18S1-AC3102-SEM-6";

			} else
				continue;
			 */
			

			//System.out.println("lines = "+lines.size());

			List<Object> userlist = new ArrayList<Object>();
			List<String>  matchedlines = findInFile(Paths.get(fileName),stringToSearch, 1);
			//System.out.println("ProjID="+proj_id+"  CourseId = "+proj.getCourseId() + ",  DsiplayName = "+proj.getDisplayName() + ", enrolment nos = "+matchedlines.size());

			/*
 			Iterator<String> itr = lines.iterator();

			//Reading file lines one by one
			while (itr.hasNext()) {
				count1++;

				String line = itr.next();
				if (stringToSearch != null && line.contains(stringToSearch) ) {

					//String[] arrOfStr = line.split("\\|",-2);
					
					//System.out.println("matches : "+itr.next());
					userlist.add(line.split("\\|",-2));
					count++;
				} else {
					//System.out.println("not matches : "+itr.next());
					continue;
				}
				count1++;
			}
			*/
			
			for (String line : matchedlines) {
				userlist.add(line.split("\\|",-2));
			}
			
			//System.out.println("userlist.size(): "+userlist.size());
			if (userlist.size() > 0)
			for(int i1 = 0; i1 < userlist.size(); i1++) {
				//System.out.println(userlist.get(i1).toString());
				String[]  arrOfStr = (String[]) userlist.get(i1);

				//USERNAME|CAMPUS|FULLNAME|EMAIL|USER_ROLE|COURSE_NAME|COURSE_CODE|ACAD_YR|SEM|COURSE_ID
				//	            for(int i11 = 0; i11 < arrOfStr.length; i11++) {
				//		            System.out.println("USERNAME: "+arrOfStr[i11]);               
				//		        }

				String USERNAME = arrOfStr[0];
				/*
				String campus = arrOfStr[1];
				String FULLNAME = arrOfStr[2];
				String EMAIL = arrOfStr[3];
				String USER_ROLE = arrOfStr[4];
				String COURSE_NAME = arrOfStr[5];
				String COURSE_CODE = arrOfStr[6];
				String ACAD_YR = arrOfStr[7];
				String SEM = arrOfStr[8];
				*/
				String COURSE_ID = arrOfStr[9];
				//String school = arrOfStr[10];
		
				/*
				if (COURSE_ID != null && !COURSE_ID.equals(stringToSearch))
					System.out.println("project is not avalable in eUreka = "+COURSE_ID+" stringToSearch = "+stringToSearch);
					*/

				if (!proj.hasMember(USERNAME)) { 
					// if user is not member to this project, add it here
					
					try {
						// create a user
						User user = userDAO.getUserByUsername(USERNAME);

						System.out.println(i1+": Debug :  Adding mbr to proj "+USERNAME+" displayname = "+user.getDisplayName()+", "+proj.getCourseId());

						// create a project user
						ProjUser pu = new ProjUser(proj, user, studentRole);
						//System.out.println("Debug getName()"+pu.getUser().getDisplayName());
						//System.out.println("Debug getDisplayName()"+pu.getProject().getDisplayName());

						proj.addMember(pu);
						userDAO.save(user);
					} catch (Exception e) {
						System.out.println(USERNAME+" : addMember exception:"+e);
					}
				}
			}
		}
		
		System.out.print("All filtered projects enrolment have been synced.");
	
	} catch (NumberFormatException ex) {
		appState.recordErrorMsg("Invalid Data type, " + ex.getMessage());

		logger.error(ex.getMessage());
/*
		in.close();
		savedFile.delete();
		if(count>0){
			appState.recordWarningMsg("created Group : " + groupTypeName +", " + count + " users added to the group");
		}
		if(countNotFound > 0){
			appState.recordWarningMsg("Total users not added :" + countNotFound  );
		}
		return null;
		*/
	} catch (RuntimeException ex) {
		// Kanesh commented below
		//appState.recordErrorMsg(ex.getMessage());

		logger.error(ex.getMessage());
/*
		in.close();
		savedFile.delete();
		
		if(count>0){
			appState.recordWarningMsg("created Group : " + groupTypeName +", " + count + " users added to the group");
		}
		if(countNotFound > 0){
			appState.recordWarningMsg("Total users not added :" + countNotFound  );
		}
		return null;
		*/
	}catch(Exception e) {
		appState.recordErrorMsg(e.getMessage());

		logger.error(e.getMessage());
	}
	/*
	appState.recordInfoMsg("Successfully created Group : " + groupTypeName +", " + count + " users added to the group");
	if(countNotFound > 0){
		appState.recordWarningMsg("Total users not added :" + countNotFound  );
	}
	*/
	return null;

}
	private int Integer(String aCAD_YR) {
		// TODO Auto-generated method stub
		return 0;
	}

	private void isCreateMode() {
		// TODO Auto-generated method stub
		
	}

	private void User() {
		// TODO Auto-generated method stub
		
	}

	@RestfulWebMethod 
	public void runImportCAOProj(Request request, Response response) throws IOException{
		PrintWriter writer = response.getPrintWriter("text/plain");
		
		String separator = Config.getString("QScheduler.Import.separator");
		int startFromRow = Config.getInt("QScheduler.ImportCAOProj.startFromRow");
		String fileName = Config.getString("QScheduler.ImportCAOProj.file");
		boolean replaceExistRow = Config.getBoolean("QScheduler.ImportCAOProj.replaceExistRow");
		
		ProjRole studentRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_STUDENT);
    	ProjRole ntuTutorRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_SCHOOL_TUTOR);
		ProjType caoType = projTypeDAO.getTypeByName(PredefinedNames.PROJTYPE_CAO);
		List<Module> defaultModules = caoType.getDefaultModules();
		ProjStatus activeStatus = projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_ACTIVE);
		String defaultMilestoneNames[] = {"","",""};
		Date defaultMilestoneDates[] = {null,null,null};
		defaultMilestoneNames[0] = Config.getString("QScheduler.ImportCAOProj.defaultMilestone1.name");
		defaultMilestoneNames[1] = Config.getString("QScheduler.ImportCAOProj.defaultMilestone2.name");
		defaultMilestoneNames[2] = Config.getString("QScheduler.ImportCAOProj.defaultMilestone3.name");
		defaultMilestoneDates[0] = Util.stringToDate(Config.getString("QScheduler.ImportCAOProj.defaultMilestone1.date")
				, Config.getString("QScheduler.ImportCAOProj.dateFormat"));
		defaultMilestoneDates[1] = Util.stringToDate(Config.getString("QScheduler.ImportCAOProj.defaultMilestone2.date")
				, Config.getString("QScheduler.ImportCAOProj.dateFormat"));
		defaultMilestoneDates[2] = Util.stringToDate(Config.getString("QScheduler.ImportCAOProj.defaultMilestone3.date")
				, Config.getString("QScheduler.ImportCAOProj.dateFormat"));
		
		FileInputStream fis = null;
		BufferedInputStream bis = null; 
        DataInputStream in = null;
        BufferedReader br = null;
		
		try{
			
			int numReplace = 0;
			int numIgnore = 0;
			int numAdd = 0;
			
			 fis = new FileInputStream(fileName);
			 bis = new BufferedInputStream(fis); 
	         in = new DataInputStream(bis);
	        
	         br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			List<ProjUser> members = null;
			User u = null;
			Project proj = null;
        	ProjCAOExtraInfo projCao = null;
        	
	        logger.info("Start processing "+ fileName);
	        
	        /*Username,	Project ID,	Project Type,	Project title,	Project Objective,
	        Project Pre-requisite,	No. of attachment weeks, Record Status,	Start Date,
	        End Date,	NTU Tutor Username
	        */
	        String[] nextLine;
	        int i = 0;
	        while((strLine = br.readLine()) != null){
	        	i++;
	        	if(i%100==0)
	        		logger.info("ImportCAOProj: numReplace="+numReplace+", numAdd="+numAdd+", processing row i="+i +".....");
	        	
	        	if(i<startFromRow)
		        	continue;
	        	
	        	nextLine = strLine.split(separator);
	        	if(nextLine.length<11){
	        		logger.warn("number of field incorrect: "+nextLine.length +" strLine="+ strLine);
	        		numIgnore++;
	        		continue;
	        	}
	        	for(int j=0; i<nextLine.length; i++){
	        		nextLine[j] = nextLine[j].trim();
	        	}
	        	
	        	String username = nextLine[0];
	        	u = userDAO.getUserByUsername(username);
	        	if(u==null){
	        		logger.warn("Username not exist. This row is ignored. Row="+ i +", username="+username);
	        		numIgnore++;
	        		continue;
	        	}
	        	
	        	members = new ArrayList<ProjUser>();
	        	members.add(new ProjUser(null, u, studentRole));
	        	
	        	String caoID = nextLine[1];
	        	if(projExtraInfoDAO.getProjExtraInfoByCAOId(caoID)!=null){ //if exist project of same CaoID
	        		if(!replaceExistRow){
	        			numIgnore++;
		        		continue;
	        		}
	        	}
	        	
	        	
	        	String caoProjType = nextLine[2]; //e.g: IA, IO...
	        	String name = nextLine[3];
	        	String originalScope = nextLine[4];
	        	String originalPrerequisite = nextLine[5];
	        	//ignore No._of_attachment_weeks
	        	//ignore Record_Status
	        	Date sdate = Util.stringToDate(nextLine[8], Config.getString("QScheduler.ImportCAOProj.dateFormat"));
	        	Date edate = Util.stringToDate(nextLine[9], Config.getString("QScheduler.ImportCAOProj.dateFormat"));
	        	
	        	String ntuTutorUsername = nextLine[10];
	        	User ntuTutor = userDAO.getUserByUsername(ntuTutorUsername);
	        	if(ntuTutor==null){
	        		logger.warn("NTU tutor username not exist. NTU tutor is not assigned. Row="+ i +", NTU-tutor-username="+ntuTutorUsername);
	        	}else{
	        		members.add(new ProjUser(null, ntuTutor, ntuTutorRole));
	        	}
	        	
	        	if(projExtraInfoDAO.getProjExtraInfoByCAOId(caoID)!=null){ //if exist project of same CaoID
	        		projCao = projExtraInfoDAO.getProjExtraInfoByCAOId(caoID);
	        		
	        		projCao.setCaoType(caoProjType);
	        		projCao.setOriginalPrerequisite(originalPrerequisite);
	        		projCao.setOriginalScope(originalScope);
	        		
	        		projCao.getProject().getMembers().clear(); //delete old members list
	        		for(ProjUser pu :members){
	        			pu.setProject(projCao.getProject());
	        			projCao.getProject().addMember(pu);
	        		}
	        		projCao.getProject().setName(name);
	        		projCao.getProject().setSdate(sdate);
	        		projCao.getProject().setEdate(edate);
	        		
	        		projDAO.immediateSaveProject(projCao.getProject());
	        		projExtraInfoDAO.immediateSaveProjExtraInfo(projCao);
	        		
        			numReplace++;
        		
	        	}
	        	else{
	        		proj = new Project();
		        	projCao = new ProjCAOExtraInfo();
		        	projCao.setProject(proj);
		        	proj.addMember(new ProjUser(proj, u, studentRole));
		        	
		        	projCao.setCaoID(caoID);
		        	projCao.setCaoType(caoProjType);
	        		projCao.setOriginalPrerequisite(originalPrerequisite);
	        		projCao.setOriginalScope(originalScope);
	        		for(ProjUser pu :members){
	        			pu.setProject(proj);
	        		}
	        		proj.setMembers(members);
	        		proj.setName(name);
	        		proj.setSdate(sdate);
	        		proj.setEdate(edate);
	        		
	        		proj.setType(caoType);
	        		//TODO: get proj.School instead of user.school?
	        		proj.setId(projDAO.generateId(proj.getType(), u.getSchool(), proj.getSdate()));
	        		proj.setSchool(u.getSchool());
	    			
	    			for(Module m : defaultModules){
	    				proj.addModule(new ProjModule(proj, m));
	    			}
	    			
	    			//create default milestones
	    			Schedule sd = new Schedule();
	    			sd.setProject(proj);
	    			sd.setActive(true);
	    			sd.setCreateDate(new Date());
	    			sd.setUser(ntuTutor);
	    			schdlDAO.saveSchedule(sd);
	    			for(int j=0; j<defaultMilestoneNames.length; j++){
	    				Milestone mstone = new Milestone();
		    			mstone.setCreateDate(new Date());
		    			mstone.setModifyDate(new Date());
		    			mstone.setManager(ntuTutor);
		    			mstone.setUrgency(UrgencyLevel.MEDIUM);
	    				mstone.setName(defaultMilestoneNames[j]);
	    				mstone.setDeadline(defaultMilestoneDates[j]);
	    				mstone.setSchedule(sd);
	    				sd.addMilestone(mstone);
	    				schdlDAO.saveMilestone(mstone);
	    			}
	    			
	    			proj.setCdate(new Date());
	    			proj.setMdate(new Date());
	    			//proj.setCreator(creator);
	    			proj.setStatus(activeStatus);
	    			projCao.setCdate(new Date());
	    			projCao.setMdate(new Date());
	    			
	    			projDAO.immediateSaveProject(proj);
	    			projExtraInfoDAO.immediateSaveProjExtraInfo(projCao);
	    			
	    			numAdd++;
	        	}
	        	
	        }
	    
	        String msg = "Finished "+ fileName +" , "+numIgnore+" ignored, "+numAdd+" added, "+ numReplace+" replaced.";
			logger.info(msg);
			writer.println(msg);
			writer.flush();
		}
		catch (FileNotFoundException ex){
			logger.error("Cannot find the file to import. "+fileName);
			writer.println("Cannot find the file to import. "+fileName);
			writer.flush();
		}
		catch(IOException ex){
			logger.error(ex.getMessage());
			writer.println(ex.getMessage());
			writer.flush();
		}
		finally{
			if(br !=null){
				br.close();
				br = null;
			}
			if(in !=null){
				in.close();
				in = null;
			}
			if(bis !=null){
				bis.close();
				bis = null;
			}
			if(fis!=null){
				fis.close();
				fis = null;
			}
		}
	}
	
	@RestfulWebMethod 
	public void runImportFYPProj(Request request, Response response) throws IOException{
		PrintWriter writer = response.getPrintWriter("text/plain");
		
		
		String separator = Config.getString("QScheduler.Import.separator");
		int startFromRow = Config.getInt("QScheduler.ImportFYPProj.startFromRow");
		String fileName = Config.getString("QScheduler.ImportFYPProj.file");
		boolean replaceExistRow = Config.getBoolean("QScheduler.ImportFYPProj.replaceExistRow");
		
		ProjRole supervisorRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_SUPERVISOR);
    	ProjRole examinerRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_EXAMINER);
    	ProjRole studentRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_STUDENT);
    	
		ProjType fypType = projTypeDAO.getTypeByName(PredefinedNames.PROJTYPE_FYP);
		List<Module> defaultModules = fypType.getDefaultModules();
		ProjStatus activeStatus = projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_ACTIVE);
		School otherSchool = schoolDAO.getSchoolByName(PredefinedNames.SCHOOL_OTHERS);
		
		FileInputStream fis = null;
		DataInputStream in = null;
        BufferedReader br = null;
	
		try{
			
			int numReplace = 0;
			int numIgnore = 0;
			int numAdd = 0;
			
			fis = new FileInputStream(fileName);
			in = new DataInputStream(fis);
			br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			List<ProjUser> members = null;
			User u = null;
			Project proj = null;
        	ProjFYPExtraInfo projFyp = null;
        	
	        logger.info("Start processing "+ fileName);
	        
	        /*
	         FYP_PROJ_ID, FYP_SCHOOL, FYP_PROJ_NO, FYP_ACAD_YR,	FYP_SEM_OFF,
	         FYP_PROJ_TITLE, FYP_PROJ_OBJ, FYP_SUPERVISOR1, FYP_SUPERVISOR2,
	         FYP_EXAMINER1, FYP_EXAMINER2, 
	         FYP_STUDENT1, FYP_STUDENT2, FYP_STUDENT3, FYP_STUDENT4, FYP_STUDENT5,
	         FYP_EXAM_DAY, FYP_EXAM_TIME, FYP_EXAM_VENUE                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
	         */
	        String[] nextLine;
	        int i = 0;
	        while((strLine = br.readLine()) != null){
	        	i++;
	        	if(i%100==0)
	        		logger.info("ImportFYPProj: numReplace="+numReplace+", numAdd="+numAdd+", processing row i="+i +".....");
	        	
	        	
	        	if(i<startFromRow)
		        	continue;
	        	
	        	nextLine = strLine.split(separator);
	        	if(nextLine.length<19){
	        		logger.warn("number of field incorrect: "+nextLine.length +" strLine="+ strLine);
	        		numIgnore++;
	        		continue;
	        	}
	        	for(int j=0; i<nextLine.length; i++){
	        		nextLine[j] = nextLine[j].trim();
	        	}
	        	
	        	String fypID = nextLine[0];
	        	if(projExtraInfoDAO.getProjFYPExtraInfoByFYPId(fypID)!=null){ //if exist project of same FypID
	        		if(!replaceExistRow){
	        			numIgnore++;
		        		continue;
	        		}
	        	}
	        	
	        	String schoolName = nextLine[1];
	        	School school = schoolDAO.getSchoolByName(schoolName);
	        	if(school==null){
	        		if(schoolName.isEmpty())
	        			school = otherSchool;
	        		else{
		        		//if not defined, add new
		        		School s = new School();
		        		s.setName(schoolName);
		        		s.setSystem(false);
		        		logger.warn("School/Dept not found. New School/Dept created. School/Dept name="+ schoolName);
		        		schoolDAO.immediateSave(s);
		        		school = s;
	        		}
	        	}
		        
	        	String fypNo = nextLine[2];
	        	String acadYear = nextLine[3];
	        	String acadSem = nextLine[4];
	        	String name = nextLine[5];
	        	
	        	
	        	String description = nextLine[6];
	        	
	        	//Date Sdate = Util.stringToDate(nextLine[8], Config.getString("QScheduler.Import.dateFormat")));
	        	//Date Edate = Util.stringToDate(nextLine[9], Config.getString("QScheduler.Import.dateFormat")));
        	
	        	
	        	members = new ArrayList<ProjUser>();
	        	String supervisor1 = nextLine[7];
	        	u = userDAO.getUserByExKey(supervisor1);
	        	if(u!=null){
	        		members.add(new ProjUser(null, u, supervisorRole));
		        }
	        	else{
	        		logger.info("row="+i +", not found supervisor1="+supervisor1+" ");
	        	}
	        	u = userDAO.getUserByExKey(nextLine[8]);
	        	if(u!=null){
	        		members.add(new ProjUser(null, u, supervisorRole));
		        }
	        	u = userDAO.getUserByExKey(nextLine[9]);
	        	if(u!=null){
	        		members.add(new ProjUser(null, u, examinerRole));
		        }
	        	u = userDAO.getUserByExKey(nextLine[10]);
	        	if(u!=null){
	        		members.add(new ProjUser(null, u, examinerRole));
		        }
	        	u = userDAO.getUserByExKey(nextLine[11]);
	        	if(u!=null){
	        		members.add(new ProjUser(null, u, studentRole));
		        }
	        	else{
	        		logger.info("row="+i +", not found student1="+nextLine[11]+" ");
	        	}
	        	u = userDAO.getUserByExKey(nextLine[12]);
	        	if(u!=null){
	        		members.add(new ProjUser(null, u, studentRole));
		        }
	        	u = userDAO.getUserByExKey(nextLine[13]);
	        	if(u!=null){
	        		members.add(new ProjUser(null, u, studentRole));
		        }
	        	u = userDAO.getUserByExKey(nextLine[14]);
	        	if(u!=null){
	        		members.add(new ProjUser(null, u, studentRole));
		        }
	        	u = userDAO.getUserByExKey(nextLine[15]);
	        	if(u!=null){
	        		members.add(new ProjUser(null, u, studentRole));
		        }
	        	
	        	String dateTimeStr = nextLine[16]+" "+nextLine[17];
	        	Date examDateTime = null;
	        	if(!dateTimeStr.isEmpty() && !dateTimeStr.equals(". .")){
	        		examDateTime = Util.stringToDate(dateTimeStr, Config.getString("QScheduler.ImportFYPProj.dateTimeFormat"));
	        	}
	        	String examVenue = nextLine[18].trim();
	        	
	        	
	        	if(projExtraInfoDAO.getProjFYPExtraInfoByFYPId(fypID)!=null){ //if exist project of same FypID, replace it
        			projFyp = projExtraInfoDAO.getProjFYPExtraInfoByFYPId(fypID);
	        		
	        		projFyp.getProject().setSchool(school);
	        		projFyp.setFypNo(fypNo);
	        		projFyp.setAcadYear(acadYear);
	        		projFyp.setAcadSem(acadSem);
	        		projFyp.getProject().setName(name);
	        		projFyp.getProject().setDescription(Util.filterOutRestrictedHtmlTags(description));
	        		
	        		projFyp.getProject().getMembers().clear(); //delete old members list
	        		for(ProjUser pu :members){
	        			pu.setProject(projFyp.getProject());
	        			projFyp.getProject().addMember(pu);
	        		}
	        		
	        		
	        		getFypStartDateEndDate(projFyp, proj);
	        		projFyp.setExamDateTime(examDateTime);
	        		projFyp.setExamVenue(examVenue);
	        		
	        		projDAO.immediateSaveProject(projFyp.getProject());
	        		projExtraInfoDAO.immediateSaveProjExtraInfo(projFyp);
	        		
	        		numReplace++;
        		
	        	}
	        	else{
	        		proj = new Project();
	        		projFyp = new ProjFYPExtraInfo();
	        		projFyp.setProject(proj);
	        		
	        		proj.setSchool(school);
	        		projFyp.setFypID(fypID);
	        		projFyp.setFypNo(fypNo);
	        		projFyp.setAcadYear(acadYear);
	        		projFyp.setAcadSem(acadSem);
	        		proj.setName(name);
	        		proj.setDescription(description);
	        		
	        		proj.getMembers().clear(); //delete old members list
	        		for(ProjUser pu :members){
	        			pu.setProject(proj);
	        			proj.addMember(pu);
	        		}
	        		
	        		
	        		getFypStartDateEndDate(projFyp, proj);
	        		projFyp.setExamDateTime(examDateTime);
	        		projFyp.setExamVenue(examVenue);
	        		
	        		proj.setType(fypType);
	        		proj.setId(projDAO.generateId(proj.getType(), proj.getSchool(), (Util.stringToDate(projFyp.getAcadYear(), "yyyy"))));
	        		
	    			for(Module m : defaultModules){
	    				proj.addModule(new ProjModule(proj, m));
	    			}
	    			proj.setCdate(new Date());
	    			proj.setMdate(new Date());
	    			//proj.setCreator(creator);
	    			proj.setStatus(activeStatus);
	    		
	    			//TODO create default milestones of important dates
	    			
	    			projDAO.immediateSaveProject(proj);
	    			projExtraInfoDAO.immediateSaveProjExtraInfo(projFyp);
	    			
	    			numAdd++;
	        	}
	        	
	        }
	        
	        logger.info("Finished "+ fileName +" , "+numIgnore+" ignored, "+numAdd+" added, "+ numReplace+" replaced.");
	        writer.println("Finished "+ fileName +" , "+numIgnore+" ignored, "+numAdd+" added, "+ numReplace+" replaced.");
			writer.flush();
		}
		catch (FileNotFoundException ex){
			logger.error("Cannot find the file to import. "+fileName);
			writer.println("Cannot find the file to import. "+fileName);
			writer.flush();
		}
		catch(IOException ex){
			logger.error(ex.getMessage());
			writer.println(ex.getMessage());
			writer.flush();
		}
		finally{
			if(br !=null){
				br.close();
				br = null;
			}
			if(in !=null){
				in.close();
				in = null;
			}
			if(fis!=null){
				fis.close();
				fis = null;
			}
		}
	}
	private Project getFypStartDateEndDate(ProjFYPExtraInfo pInfo, Project p){
		Calendar d = Calendar.getInstance();
		
		int year = 0;
		try{
			year = Integer.parseInt(pInfo.getAcadYear());
		}
		catch (NumberFormatException e){
			year = d.get(Calendar.YEAR); //current year
		}
		
		int sem = 0;
		try{
			sem = Integer.parseInt(pInfo.getAcadSem());
		}
		catch (NumberFormatException e){
			sem = 1;
		}
		
		String sDate = "";
		int numDayToEnd = 0;
		if(sem==1){
			sDate = Config.getString("QScheduler.ImportFYPProj.defaultStartDateSem1");
			numDayToEnd = Config.getInt("QScheduler.ImportFYPProj.numDayToEndDateSem1");
		}
		else{
			sDate = Config.getString("QScheduler.ImportFYPProj.defaultStartDateSem1");
			numDayToEnd = Config.getInt("QScheduler.ImportFYPProj.numDayToEndDateSem1");
		}
		p.setSdate(Util.stringToDate(sDate+"-"+year, "dd-MMM-yyyy"));
		d.setTime(p.getSdate());
		d.add(Calendar.DAY_OF_YEAR, numDayToEnd);
		p.setEdate(d.getTime());
		
		return p;
	}
	
	
	/**
	 * in Message modules, check for messages that have been marked as 'deleted' for more than 30 days, then delete them  
	 */
	@RestfulWebMethod 
	public void runCleanupTrashMessage(Request request, Response response) throws IOException{
		int count = 0;
		
		count = msgDAO.deleteTrashMessages();
		
		String msg = "Run job CleanupTrashMessage, "+ count +" messages were deleted from Trash";
		logger.info(msg);
		
		PrintWriter writer = response.getPrintWriter("text/plain");
		writer.println(msg);
		writer.flush();
	}
	
	
	/**
	 * Check and Update project status, e.g 90days from "Active" -> "Inactive", 30days from "Inactive" -> "Archived",  
	 * 30days from "Deleted" -> actually delete project and all its data
	 */
	@RestfulWebMethod 
	public void runProjectHouseKeeping(Request request, Response response) throws IOException{
		PrintWriter writer = response.getPrintWriter("text/plain");
		String projEditStatusURL = Config.getString(Config.BASE_URL)+"/admin/project/editstatus";
		
		//set active to inactive, send email to notify member
		List<Project> pList = projDAO.findProjectsToInactive(Config.getInt("project.status.numDayActiveToInactive"));
		for(Project p: pList){
			p.setStatus(projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_INACTIVE));
			p.setLastStatusChange(new Date());
			
			projDAO.immediateSaveProject(p);
			logger.info("INACTIVE proj="+p.getId());
			
			if(p.getLastAccess()==null || !usageDAO.hasItemInProj(p)){
				continue; //if the Project never been accessed, then don't send the email
			}
			else{
				List<User> userList = new ArrayList<User>();
				for(ProjUser pu : p.getMembers()){
					if(pu.hasPrivilege(PrivilegeProject.CHANGE_STATUS)){
						userList.add(pu.getUser());
					}
				}
				EmailTemplateVariables var = new EmailTemplateVariables(
						p.getCdate().toString(), p.getMdate().toString(),
						p.getName(), 
						Util.truncateString(Util.stripTags(p.getDescription()), Config.getInt("max_content_lenght_in_email")), 
						p.getCreator().getDisplayName(), 
						p.getDisplayName(), 
						projEditStatusURL + "/" + p.getId(),
						p.getLastAccess().toString(),
						(new Date()).toString()
					);
				try{
					emailManager.sendHTMLMail(userList, EmailTemplateConstants.PROJECT_UPDATE_STATUS_INACTIVE,	var);
				}catch(Exception e){
					logger.error(e.getMessage());
					writer.println("<br/>"+e.getMessage());
					writer.flush();
				}
			}
			
		}
		String msg = "Run task ProjectHouseKeeping "+ pList.size() +" project status(s) was set to INACTIVE."; 
		logger.info(msg);
		writer.println(msg);
		writer.flush();
		
		pList = projDAO.findProjectsToArchive(Config.getInt("project.status.numDayInactiveToArchive"));
		for(Project p: pList){
			p.setStatus(projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_ARCHIVED));
			p.setLastStatusChange(new Date());
			
			projDAO.immediateSaveProject(p);
			logger.info("ARCHIVED proj="+p.getId());
			
			if(p.getLastAccess()==null || !usageDAO.hasItemInProj(p)){
				continue; //if the Project never been accessed, then don't send the email
			}
			else{
				List<User> userList = new ArrayList<User>();
				for(ProjUser pu : p.getMembers()){
					if(pu.hasPrivilege(PrivilegeProject.CHANGE_STATUS)){
						userList.add(pu.getUser());
					}
				}
			
				EmailTemplateVariables var = new EmailTemplateVariables(
						p.getCdate().toString(), p.getMdate().toString(),
						p.getName(), 
						Util.truncateString(Util.stripTags(p.getDescription()), Config.getInt("max_content_lenght_in_email")), 
						p.getCreator().getDisplayName(), 
						p.getDisplayName(), 
						projEditStatusURL + "/" + p.getId(),
						p.getLastStatusChange().toString(),
						(new Date()).toString()
					);
				emailManager.sendHTMLMail(userList, EmailTemplateConstants.PROJECT_UPDATE_STATUS_ARCHIVED,	var);
			}
		}
		msg = "Run task ProjectHouseKeeping "+ pList.size() +" project status(s) was set to ARCHIVED. ";
		logger.info(msg);
		writer.println(msg);
		writer.flush();
		
		pList = projDAO.findProjectsToActualDelete(Config.getInt("project.status.numDayDeleteToPermanentDelete"));
		int i = 0;
		for(Project p: pList){
	//		logger.debug("pid="+p.getId());
			if(!(p.getRemarks()!=null && p.getRemarks().startsWith("Migrated from eUreka1"))){
				//if not the migrated project from eUreka1, i.e. only want to delete those created in eUreka2 only
				projDAO.immediateDeleteProjectPermanently(p); //delete the project and casecade to all its data
				i++;
				logger.info("DELETED proj="+p.getId());
			}
		}
		msg = "Run task ProjectHouseKeeping "+ i +" project(s) status DELETED was deleted permanently.";
		
		logger.info(msg);
		writer.println(msg);
		writer.flush();
		
	}
	
	/**
	 * in Scheduling module, check and send out reminder emails for Tasks that due soon
	 */
	@RestfulWebMethod 
	public void runSendTaskReminderEmail(Request request, Response response) throws IOException{
		PrintWriter writer = response.getPrintWriter("text/plain");
		String taskEditURL = Config.getString(Config.BASE_URL)+"/modules/scheduling/taskedit";
		int count = 0;
		//send reminder 
		List<Task> tasks = schdlDAO.getTasksToSendReminder();
		for(Task t : tasks){
			Collection<User> users = new ArrayList<User>();
			Project proj = t.getMilestone().getSchedule().getProject();
			if(ReminderType.ALL_PROJECT_MEMBERS.equals(t.getReminderType())){
				users = proj.getUsers();
			}
			else if(ReminderType.ASSIGNED_MEMBERS_ONLY.equals(t.getReminderType())){
				users = t.getAssignedPersons();
			}
			
			EmailTemplateVariables var = new EmailTemplateVariables(
					t.getCreateDate().toString(), t.getModifyDate().toString(),
					t.getName(),
					Util.truncateString(Util.stripTags(t.getComment()), Config.getInt("max_content_lenght_in_email")), 
					t.getAssignedPersonsDisplay(), 
					proj.getDisplayName(), 
					taskEditURL + "/" + t.getId(),
					t.getStartDateDisplay(),
					t.getEndDateDisplay()
				);
			try{
				emailManager.sendHTMLMail(users, EmailTemplateConstants.SCHEDULING_TASK_REMINDER,	var);
				count++;
			}catch(Exception e){
				logger.error(e.getMessage());
				writer.println("<br/>"+e.getMessage());
				writer.flush();
			}
		}
		
	//	SendEvaluationReminderEmail();
		
		writer.println("Running task SendTaskReminderEmail. "+ count +" task-reminders sent.");
		writer.flush();
		
	}

	
	
	@RestfulWebMethod 
	public void runSendEvaluationReminderEmail(Request request, Response response) throws IOException{
//		PrintWriter writer = response.getPrintWriter("text/html");
		
		String returnMessage = SendEvaluationReminderEmail( );
		
		//response.setContentLength(returnMessage.getBytes().length);
	//	writer.println( returnMessage);
		
		//writer.println("Running task SendEvaluationReminderEmail. " + returnMessage);
		//writer.flush();
		
		OutputStream out = response.getOutputStream("text/html");
		
		
		out.write(returnMessage.getBytes(), 0, returnMessage.getBytes().length);
		out.close();
		
	}
	
	private String SendEvaluationReminderEmail( ){
		String returnMsg = (new Date()).toString();
		String peerEvalHomeURL = Config.getString(Config.BASE_URL)+"/modules/peerevaluation/home";
		String profilingHomeURL = Config.getString(Config.BASE_URL)+"/modules/profiling/home";
		String lcdpHomeURL = Config.getString(Config.BASE_URL)+"/modules/lcdp/home";
		String careHomeURL = Config.getString(Config.BASE_URL)+"/modules/care/home";
		String big5HomeURL = Config.getString(Config.BASE_URL)+"/modules/big5/home";
		String teHomeURL = Config.getString(Config.BASE_URL)+"/modules/te/home";
		String ipspHomeURL = Config.getString(Config.BASE_URL)+"/modules/ipsp/home";
			
		
		int count = 0;
		//send reminder 
		List<Evaluation> evals = evalDAO.getEvalsToSendReminder();
		for(Evaluation eval : evals){
			Collection<User> users = new ArrayList<User>();
			Project proj = eval.getProject();
			users.addAll(getNonSubmitted(eval));
			
			for(User u : users){
				EmailTemplateVariables var = new EmailTemplateVariables(
						eval.getCdate().toString(), eval.getMdate().toString(),
						eval.getName(),
						Util.truncateString(Util.stripTags(eval.getDes()), Config.getInt("max_content_lenght_in_email")), 
						eval.getCreator().getDisplayName(), 
						proj.getDisplayName(), 
						peerEvalHomeURL + "/" + eval.getProject().getId(),
						eval.getEdateDisplay(),
						u.getDisplayName() //(eval.getReleased()? "Yes" : "No")
					);
				try{
					emailManager.sendHTMLMail(u, EmailTemplateConstants.EVALUATION_REMINDER, var);
					count++;
					returnMsg +=  "<br/><b>" + count +"</b>, evalID=" + eval.getId() + ", evalName="+eval.getName() 
							+ " ,projID=" + eval.getProject().getId() + ", emailType=" + EmailTemplateConstants.EVALUATION_REMINDER 
							+ "<br/>,  username=" + u.getUsername()
							+ " <br/>," +" emails=" + u.getEmail()
					;
				}catch(Exception e){
					logger.error(e.getMessage());
					returnMsg += "<br/>"+e.getMessage();
				}
				
			}
			
		}
		
		//send reminder for Instructor
		evals = evalDAO.getEvalsToSendReminderInstructor();
		for(Evaluation eval : evals){
			Project proj = eval.getProject();
			Collection<User> users = new ArrayList<User>();
			users.addAll(getLeaderUsers(proj));
			
			for(User u: users){
				EmailTemplateVariables var = new EmailTemplateVariables(
						eval.getCdate().toString(), eval.getMdate().toString(),
						eval.getName(),
						Util.truncateString(Util.stripTags(eval.getDes()), Config.getInt("max_content_lenght_in_email")), 
						eval.getCreator().getDisplayName(), 
						eval.getProject().getDisplayName(), 
						peerEvalHomeURL + "/" + eval.getProject().getId(),
						eval.getEdateDisplay(),
						u.getDisplayName() //(eval.getReleased()? "Yes" : "No")
					);
				try{
					emailManager.sendHTMLMail(u, EmailTemplateConstants.EVALUATION_REMINDER_INSTRUCTOR, var);
					count++;
					returnMsg +=  "<br/><b>" + count +"</b>, evalID=" + eval.getId() + ", evalName="+eval.getName() 
							+ " ,projID=" + eval.getProject().getId() + ", emailType=" + EmailTemplateConstants.EVALUATION_REMINDER_INSTRUCTOR 
							+ "<br/>,  username=" + u.getUsername()
							+ " <br/>," +" emails=" + u.getEmail()
					;
				}catch(Exception e){
					logger.error(e.getMessage());
					returnMsg += "<br/>"+e.getMessage();
				}
			}
			
		}
		returnMsg  += "<br/><b> " + " " + count +" evaluation-reminders were sent.</b>";
		
		
		
		//reminder to launch
		count = 0;
		returnMsg += "<br/>";
		evals = evalDAO.getEvalsToSendLaunchReminder();
		for(Evaluation eval : evals){
			Collection<User> leaders = new ArrayList<User>();
			Project proj = eval.getProject();
			leaders.addAll(getLeaderUsers(proj));
			Collection<User> users = proj.getUsers();
			
			for( User u : users){
				String emailTemplateType = "";
				if (leaders.contains(u)){
					emailTemplateType = EmailTemplateConstants.EVALUATION_REMINDER_LAUNCH_INSTRUCTOR;
				}
				else{
					emailTemplateType = EmailTemplateConstants.EVALUATION_REMINDER_LAUNCH;
				}
				
				EmailTemplateVariables var = new EmailTemplateVariables(
						eval.getCdate().toString(), eval.getMdate().toString(),
						eval.getName(),
						Util.truncateString(Util.stripTags(eval.getDes()), Config.getInt("max_content_lenght_in_email")), 
						eval.getCreator().getDisplayName(), 
						proj.getDisplayName(), 
						peerEvalHomeURL + "/" + eval.getProject().getId(),
						eval.getEdateDisplay(),
						u.getDisplayName()
					);
				try{
					emailManager.sendHTMLMail(u, emailTemplateType, var);
					count++;
					returnMsg +=  "<br/><b>" + count +"</b>, evalID=" + eval.getId() + ", evalName="+eval.getName() 
							+ " ,projID=" + eval.getProject().getId() + ", emailType=" + emailTemplateType 
							+ "<br/>,  username=" + u.getUsername()
							+ " <br/>," +" emails=" + u.getEmail()
					;
				}catch(Exception e){
					logger.error(e.getMessage());
					returnMsg += "<br/>"+e.getMessage();
				}
			}
		}
		
		returnMsg += "<br/><b> " + count + " evaluation-launch-reminders were sent.</b>";
		

		
		//Profiling
		List<Profiling> profs = profDAO.getProfsToSendReminder();
		for(Profiling prof : profs){
		//	Project proj = prof.getProject();
			Collection<User> users = new ArrayList<User>();
			users.addAll(profDAO.getNotSubmitedUsersByProfiling(prof));
			
			for(User u: users){
				EmailTemplateVariables var = new EmailTemplateVariables(
						prof.getCdate().toString(), prof.getMdate().toString(),
						prof.getName(),
						Util.truncateString(Util.stripTags(prof.getQuestionSetName()), Config.getInt("max_content_lenght_in_email")), 
						prof.getCreator().getDisplayName(), 
						prof.getProject().getDisplayName(), 
						profilingHomeURL + "/" + prof.getProject().getId(),
						prof.getEdateDisplay(),
						u.getDisplayName() 
					);
				try{
					emailManager.sendHTMLMail(u, EmailTemplateConstants.PROFILING_REMINDER, var);
					count++;
					returnMsg +=  "<br/><b>" + count +"</b>, profID=" + prof.getId() + ", profName="+prof.getName() 
							+ " ,projID=" + prof.getProject().getId() + ", emailType=" + EmailTemplateConstants.PROFILING_REMINDER 
							+ "<br/>,  username=" + u.getUsername()
							+ " <br/>," +" emails=" + u.getEmail()
					;
				}catch(Exception e){
					logger.error(e.getMessage());
					returnMsg += "<br/>"+e.getMessage();
				}
			}
			
		}
		returnMsg  += "<br/><b> " + " " + count +" profiling-reminders were sent.</b>";

		
		//LCDP  (Leadership Survey)
		List<LCDPSurvey> lcdps = lcdpDAO.getLCDPsToSendReminder();
		for(LCDPSurvey lcdp : lcdps){
		//	Project proj = lcdp.getProject();
			Collection<User> users = new ArrayList<User>();
			users.addAll(lcdpDAO.getNotSubmitedUsersByLCDPSurvey(lcdp));
			
			for(User u: users){
				EmailTemplateVariables var = new EmailTemplateVariables(
						lcdp.getCdate().toString(), lcdp.getMdate().toString(),
						lcdp.getName(),
						Util.truncateString(Util.stripTags(lcdp.getQuestionSetName()), Config.getInt("max_content_lenght_in_email")), 
						lcdp.getCreator().getDisplayName(), 
						lcdp.getProject().getDisplayName(), 
						lcdpHomeURL + "/" + lcdp.getProject().getId(),
						lcdp.getEdateDisplay(),
						u.getDisplayName() 
					);
				try{
					emailManager.sendHTMLMail(u, EmailTemplateConstants.LCDP_REMINDER, var);
					count++;
					returnMsg +=  "<br/><b>" + count +"</b>, lcdpID=" + lcdp.getId() + ", lcdpName="+lcdp.getName() 
							+ " ,projID=" + lcdp.getProject().getId() + ", emailType=" + EmailTemplateConstants.LCDP_REMINDER 
							+ "<br/>,  username=" + u.getUsername()
							+ " <br/>," +" emails=" + u.getEmail()
					;
				}catch(Exception e){
					logger.error(e.getMessage());
					returnMsg += "<br/>"+e.getMessage();
				}
			}
			
		}
		returnMsg  += "<br/><b> " + " " + count +" LCDP(Leadership Survey)-reminders were sent.</b>";

		

		
		//CARE  (Leadership Survey)
		List<CARESurvey> cares = careDAO.getCAREsToSendReminder();
		for(CARESurvey care : cares){
		//	Project proj = lcdp.getProject();
			Collection<User> users = new ArrayList<User>();
			users.addAll(careDAO.getNotSubmitedUsersByCARESurvey(care));
			
			for(User u: users){
				EmailTemplateVariables var = new EmailTemplateVariables(
						care.getCdate().toString(), care.getMdate().toString(),
						care.getName(),
						Util.truncateString(Util.stripTags(care.getQuestionSetName()), Config.getInt("max_content_lenght_in_email")), 
						care.getCreator().getDisplayName(), 
						care.getProject().getDisplayName(), 
						careHomeURL + "/" + care.getProject().getId(),
						care.getEdateDisplay(),
						u.getDisplayName() 
					);
				try{
					emailManager.sendHTMLMail(u, EmailTemplateConstants.CARE_REMINDER, var);
					count++;
					returnMsg +=  "<br/><b>" + count +"</b>, careID=" + care.getId() + ", careName="+care.getName() 
							+ " ,projID=" + care.getProject().getId() + ", emailType=" + EmailTemplateConstants.CARE_REMINDER 
							+ "<br/>,  username=" + u.getUsername()
							+ " <br/>," +" emails=" + u.getEmail()
					;
				}catch(Exception e){
					logger.error(e.getMessage());
					returnMsg += "<br/>"+e.getMessage();
				}
			}
			
		}
		returnMsg  += "<br/><b> " + " " + count +" (CARE Survey)-reminders were sent.</b>";

		
		//BIG5  (Leadership Survey)
		List<BIG5Survey> big5s = big5DAO.getBIG5sToSendReminder();
		for(BIG5Survey big5 : big5s){
			Collection<User> users = new ArrayList<User>();
			users.addAll(big5DAO.getNotSubmitedUsersByBIG5Survey(big5));
			
			for(User u: users){
				EmailTemplateVariables var = new EmailTemplateVariables(
						big5.getCdate().toString(), big5.getMdate().toString(),
						big5.getName(),
						Util.truncateString(Util.stripTags(big5.getQuestionSetName()), Config.getInt("max_content_lenght_in_email")), 
						big5.getCreator().getDisplayName(), 
						big5.getProject().getDisplayName(), 
						big5HomeURL + "/" + big5.getProject().getId(),
						big5.getEdateDisplay(),
						u.getDisplayName() 
					);
				try{
					emailManager.sendHTMLMail(u, EmailTemplateConstants.BIG5_REMINDER, var);
					count++;
					returnMsg +=  "<br/><b>" + count +"</b>, big5ID=" + big5.getId() + ", big5Name="+big5.getName() 
							+ " ,projID=" + big5.getProject().getId() + ", emailType=" + EmailTemplateConstants.BIG5_REMINDER 
							+ "<br/>,  username=" + u.getUsername()
							+ " <br/>," +" emails=" + u.getEmail()
					;
				}catch(Exception e){
					logger.error(e.getMessage());
					returnMsg += "<br/>"+e.getMessage();
				}
			}
			
		}
		returnMsg  += "<br/><b> " + " " + count +" (BIG5 Survey)-reminders were sent.</b>";
		
		//TeamEffectiveness  (Leadership Survey)
				List<TESurvey> tes = teDAO.getTEsToSendReminder();
				for(TESurvey te : tes){
					Collection<User> users = new ArrayList<User>();
					users.addAll(teDAO.getNotSubmitedUsersByTESurvey(te));
					
					for(User u: users){
						EmailTemplateVariables var = new EmailTemplateVariables(
								te.getCdate().toString(), te.getMdate().toString(),
								te.getName(),
								Util.truncateString(Util.stripTags(te.getQuestionSetName()), Config.getInt("max_content_lenght_in_email")), 
								te.getCreator().getDisplayName(), 
								te.getProject().getDisplayName(), 
								teHomeURL + "/" + te.getProject().getId(),
								te.getEdateDisplay(),
								u.getDisplayName() 
							);
						try{
							emailManager.sendHTMLMail(u, EmailTemplateConstants.TE_REMINDER, var);
							count++;
							returnMsg +=  "<br/><b>" + count +"</b>, teID=" + te.getId() + ", teName="+te.getName() 
									+ " ,projID=" + te.getProject().getId() + ", emailType=" + EmailTemplateConstants.TE_REMINDER 
									+ "<br/>,  username=" + u.getUsername()
									+ " <br/>," +" emails=" + u.getEmail()
							;
						}catch(Exception e){
							logger.error(e.getMessage());
							returnMsg += "<br/>"+e.getMessage();
						}
					}
					
				}
				returnMsg  += "<br/><b> " + " " + count +" (Team Effectiveness Survey)-reminders were sent.</b>";
				
		
		//IPSP  (Infuencial and Persuading Survey)
				List<IPSPSurvey> ipsps = ipspDAO.getIPSPsToSendReminder();
				for(IPSPSurvey ipsp : ipsps){
					Collection<User> users = new ArrayList<User>();
					users.addAll(ipspDAO.getNotSubmitedUsersByIPSPSurvey(ipsp));
					
					for(User u: users){
						EmailTemplateVariables var = new EmailTemplateVariables(
								ipsp.getCdate().toString(), ipsp.getMdate().toString(),
								ipsp.getName(),
								Util.truncateString(Util.stripTags(ipsp.getQuestionSetName()), Config.getInt("max_content_lenght_in_email")), 
								ipsp.getCreator().getDisplayName(), 
								ipsp.getProject().getDisplayName(), 
								ipspHomeURL + "/" + ipsp.getProject().getId(),
								ipsp.getEdateDisplay(),
								u.getDisplayName() 
							);
						try{
							emailManager.sendHTMLMail(u, EmailTemplateConstants.BIG5_REMINDER, var);
							count++;
							returnMsg +=  "<br/><b>" + count +"</b>, ipspID=" + ipsp.getId() + ", ipspName="+ipsp.getName() 
									+ " ,projID=" + ipsp.getProject().getId() + ", emailType=" + EmailTemplateConstants.BIG5_REMINDER 
									+ "<br/>,  username=" + u.getUsername()
									+ " <br/>," +" emails=" + u.getEmail()
							;
						}catch(Exception e){
							logger.error(e.getMessage());
							returnMsg += "<br/>"+e.getMessage();
						}
					}
					
				}
				returnMsg  += "<br/><b> " + " " + count +" (IPSP Survey)-reminders were sent.</b>";
				
				
		
		
		return returnMsg;
	}
	public boolean isLeader(ProjUser pu){
		if(pu.hasPrivilege(PrivilegeProject.IS_LEADER))
			return true;
		return false;
	}
	public List<User> getLeaderUsers(Project p){
		List<User> uList = new ArrayList<User>();
		for(ProjUser pu : p.getMembers()){
			if(isLeader(pu))
				uList.add(pu.getUser());
		}
		return uList;
	}
	public List<User> getNonLeaderUsers(Project p){
		List<User> uList = new ArrayList<User>();
		for(ProjUser pu : p.getMembers()){
			if(!isLeader(pu))
				uList.add(pu.getUser());
		}
		return uList;
	}
	public List<User> getNonSubmitted(Evaluation eval){
		List<User> uList = getNonLeaderUsers(eval.getProject());
		for(int i=uList.size()-1; i>=0; i--){
			User u = uList.get(i);
			if(getEvalStatusSubmitted(eval, u)){
				uList.remove(i);
				continue;
			}
		}
		return uList;
	}
	public boolean getEvalStatusSubmitted(Evaluation eval, User assessor){
		List<EvaluationUser> evalUserList = eval.getEvalUsersByAssessor(assessor);
		if(evalUserList.size()==0){
			return false;//messages.get("Not-Attempted");
		}
		//no need to check for edited-by-instructor, because it is considered as Submitted
		
		return true; //messages.get("Submited"); 
	}
}
