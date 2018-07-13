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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchlTypeAnnouncementDAO;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeProject;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.Privilege;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.ProjectAttachedFile;
import ntu.celt.eUreka2.entities.SchlTypeAnnouncement;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.announcement.Announcement;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentUser;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupDAO;
import ntu.celt.eUreka2.modules.group.GroupUser;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationUser;
import ntu.celt.eUreka2.pages.Index;
import ntu.celt.eUreka2.pages.project.Home;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;

import us.antera.t5restfulws.RestfulWebMethod;

public class BbIntegrateService {
	private UserDAO userDAO;
	private WebSessionDAO webSessionDAO;
	private PageRenderLinkSource linkSource;
	private RequestGlobals requestGlobal;
	private SchoolDAO schoolDAO;
	private SysRoleDAO sysRoleDAO;
	private Logger logger;
	private ProjectDAO projDAO;
	private ProjStatusDAO projStatusDAO;
	private ProjTypeDAO projTypeDAO;
	private ProjRoleDAO projRoleDAO;
	private AssessmentDAO assmtDAO;
	private EvaluationDAO evalDAO;
	private AnnouncementDAO annmtDAO;
	private SchlTypeAnnouncementDAO stAnnmtDAO;
	private GroupDAO groupDAO;
	private AttachedFileManager attFileManager;
	
	private String DEFAULT_EMPTY_RETURN_STRING = "false";;
	
	
	/**
	 * Receive all the services needed as constructor arguments. 
	 * When we bind this service, T5 IoC will provide all the services! (It works like using @Inject)
	 */
	public BbIntegrateService(
			UserDAO _userDAO, 
			WebSessionDAO _webSessionDAO, 
			PageRenderLinkSource _pageRenderLinkSource,
			RequestGlobals _requestGlobals,
			SchoolDAO _schoolDAO,
			SysRoleDAO _sysRoleDAO,
			ProjectDAO _projDAO,
			ProjStatusDAO _projStatusDAO,
			ProjTypeDAO _projTypeDAO,
			ProjRoleDAO _projRoleDAO,
			AssessmentDAO _assmtDAO,
			EvaluationDAO _evalDAO,
			AnnouncementDAO _annmtDAO,
			SchlTypeAnnouncementDAO _stAnnmtDAO,
			GroupDAO _groupDAO,
			Logger _logger,
			AttachedFileManager _attFileManager
			){
		userDAO = _userDAO;
		webSessionDAO = _webSessionDAO;
		linkSource = _pageRenderLinkSource;
		requestGlobal = _requestGlobals;
		schoolDAO = _schoolDAO;
		sysRoleDAO = _sysRoleDAO;
		projDAO = _projDAO;
		projStatusDAO = _projStatusDAO;
		projTypeDAO = _projTypeDAO;
		projRoleDAO = _projRoleDAO;
		assmtDAO = _assmtDAO;
		evalDAO = _evalDAO;
		annmtDAO = _annmtDAO;
		stAnnmtDAO = _stAnnmtDAO;
		groupDAO = _groupDAO;
		logger = _logger;
		attFileManager = _attFileManager;
	}
	
	
	@RestfulWebMethod 
	public void isUserExist(Request request, Response response, String username, 
			long timestamp, String hash) throws IOException{
		authenRequest(request, response, "isUserExist", username, timestamp, hash);
		
		String result = "true";
		
		User user = userDAO.getUserByUsername(username);
		if(user == null)
			result = "false";  //indicate to Bb building block to send User data (it should call createUser)
		
		flushTextResponse(response, result);
	}
	
	/**
	 * service to Authenticate (by username, timestamp, hash, and secretKey) 
	 * and redirect user to specified page.
	 * 
	 * @param request
	 * @param response
	 * @param username
	 * @param timestamp
	 * @param hash
	 * @param redirectPage  it must be encoded by CustomeURLEncoder. 
	 * @throws IOException
	 */
	@RestfulWebMethod 
	public void userLogin(Request request, Response response, String username,  
			long timestamp, String hash, String redirectPage) throws IOException{
		authenRequest(request, response, "userLogin", username, timestamp, hash, redirectPage);
		
		if(redirectPage==null || "null".equals(redirectPage))
			redirectPage = linkSource.createPageRenderLink(Index.class).toRedirectURI();
		else
			redirectPage = CustomURLEncoder.decode(redirectPage);
	
		User user = userDAO.getUserByUsername(username);
		if(user == null){
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Your Username \""+username+"\" can not be found, please contact administrator.");
            return;
		}
		if(!user.isEnabled()){
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Your Username \""+username+"\" is disabled, please contact administrator.");
            return;
		}
		
		doLogin(request, response, user);
	//	logger.info("After login, redirect to="+redirectPage);
		response.sendRedirect(redirectPage);
		return;
	}

	
	@RestfulWebMethod 
	public void createUser(Request request, Response response, String username, 
			long timestamp, String hash) throws IOException{
		authenRequest(request, response, "createUser", username, timestamp, hash);
		BufferedReader bReader = requestGlobal.getHTTPServletRequest().getReader();
		String str = "";
		String s;
		while((s = bReader.readLine()) != null){
			str += s;
		}
		JSONObject json = new JSONObject(str);
		
		User user = userDAO.getUserByUsername(username);
		if(user != null){
			flushTextResponse(response, "false, username already exist");
		}
		
		
		User u = new User();
		u.setCreateDate(new Date());
		u.setModifyDate(u.getCreateDate());
		u.setEnabled(true);
		u.setEmail(json.getString("email"));
		if(json.has("extKey"))
			u.setExternalKey(json.getString("extKey"));
		u.setFirstName(json.getString("fName"));
		u.setIp(requestGlobal.getHTTPServletRequest().getRemoteAddr());
		if(json.has("lName"))
			u.setLastName(json.getString("lName"));
		u.setUsername(json.getString("username"));
		if(json.has("remarks"))
			u.setRemarks(json.getString("remarks"));
	//	u.setId(id);
	//	u.setJobTitle(jobTitle);
	//	u.setMphone(mphone);
	//	u.setOrganization(organization);
	//	u.setPassword(password);
	//	u.setPhone(phone);
	//	u.setTitle(Honorific.valueOf("MR"));
		String schl = null;
		if(json.has("school"))
			schl = json.getString("school");
		if(schl==null || schl.isEmpty())
			schl = PredefinedNames.SCHOOL_OTHERS;
		School school = schoolDAO.getFirstSchoolByDescription(schl);
    	if(school==null){
    		//if not defined, add new
    		school = new School();
    		String nextSchlName = schoolDAO.getNextDefaultName("Dept_");
    		school.setName(nextSchlName);
    		school.setDes(schl);
    		school.setSystem(false);
    		logger.warn("School/Dept not found. New School/Dept created, you should change its abbreviation. Full name="+ schl);
    		schoolDAO.save(school);
    	}
		u.setSchool(school);
		
		String sRole = json.getString("sysRole");
		SysRole sysRole;
		if(sRole!=null && (sysRole=sysRoleDAO.getSysRoleByName(sRole))!=null){
			u.setSysRole(sysRole);
		}
		else{
			u.setSysRole(sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_USER));
		}
		
		userDAO.save(u);

		flushTextResponse(response, "true");
	}
	
	private User getOrCreateUser(String username, String firstName, String lastName, String email, SysRole role, School schl ){
		User u = userDAO.getUserByUsername(username);
		if (u != null)
			return u;
		
		u = new User();
		u.setCreateDate(new Date());
		u.setModifyDate(u.getCreateDate());
		u.setEnabled(true);
		u.setEmail(email);
		u.setFirstName(firstName);
		u.setLastName(lastName);
		u.setUsername(username);
		u.setRemarks("bbsvc created");
		u.setSchool(schl);
		u.setSysRole(role);

		userDAO.save(u);
		
		return u;
	}
	
	@RestfulWebMethod 
	public void createProject(Request request, Response response, String username, 
			long timestamp, String hash) throws IOException{
		authenRequest(request, response, "createProject", username, timestamp, hash);
		BufferedReader bReader = requestGlobal.getHTTPServletRequest().getReader();
		String str = "";
		String s;
		while((s = bReader.readLine()) != null){
			str += s;
		}
		JSONObject json = new JSONObject(str);
		
		Project proj = new Project();
		proj.setCourseId(json.getString("courseId"));
		if(json.has("groupId"))
			proj.setGroupId(json.getString("groupId"));
		if(json.has("seqNo"))
			proj.setSeqNo(json.getString("seqNo"));
		if(json.has("term"))
			proj.setTerm(json.getString("term"));
		if(json.has("courseCode"))
			proj.setCourseCode(json.getString("courseCode"));
		proj.setName(json.getString("name"));
		if(json.has("description"))
			proj.setDescription(Util.filterOutRestrictedHtmlTags(json.getString("description")));
		proj.setSdate(Util.longObjToDate(json.get("sdate")));
		proj.setEdate(Util.longObjToDate(json.get("edate")));
		//assume user already exists
		User u = userDAO.getUserByUsername(username);
		proj.setCreator(u);
		
		if(json.has("school")){
			String schl = json.getString("school");
			if(schl.isEmpty())
				schl = PredefinedNames.SCHOOL_OTHERS;
			School school = schoolDAO.getFirstSchoolByDescription(schl);
        	if(school==null){
        		//if not defined, add new
        		school = new School();
        		String nextSchlName = schoolDAO.getNextDefaultName("Dept_");
        		school.setName(nextSchlName);
        		school.setDes(schl);
        		school.setSystem(false);
        		logger.warn("School/Dept not found. New School/Dept created, you should change its abbreviation. Full name="+ schl);
        		schoolDAO.save(school);
        	}
			proj.setSchool(school);
		}
		else{
			proj.setSchool(u.getSchool()); 
		}
		
		
		String projTypeStr = PredefinedNames.PROJTYPE_COURSE;
		if(json.has("projType")){
			projTypeStr = json.getString("projType");
			if(projTypeStr.isEmpty())
				projTypeStr = PredefinedNames.PROJTYPE_COURSE;
			
		}
		ProjType projType = projTypeDAO.getTypeByName(projTypeStr);
    	if(projType==null){
    		projType = projTypeDAO.getTypeByName(PredefinedNames.PROJTYPE_COURSE);
    	}
        proj.setType(projType);
		
		proj.setCdate(new Date());
		proj.setMdate(new Date());
		proj.setStatus(projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_ACTIVE));
		proj.setLastStatusChange(new Date());
		//proj.setId(projDAO.generateId(proj.getCourseId()+"-")); //generateId with prefix
		proj.setId(projDAO.generateId(proj.getType(),proj.getSchool(),proj.getSdate())); 
		
		List<Module> defaultModules = proj.getType().getDefaultModules();
		for(Module m : defaultModules){
			proj.addModule(new ProjModule(proj, m));
		}
		
		//enroll leader
		ProjRole prLeader = getLeaderRole(proj.getType());
		if(prLeader==null)
			prLeader = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_LEADER);
		proj.addMember(new ProjUser(proj, u, prLeader));
		//enroll students
		ProjRole prStudent = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_STUDENT);
		if(json.has("students")){
			JSONArray projSts = json.getJSONArray("students");
			for(int i=0; i<projSts.length(); i++){
				String username1 = projSts.getString(i);
				User u1 = userDAO.getUserByUsername(username1);
				if(!proj.hasMember(u1))
					proj.addMember(new ProjUser(proj, u1, prStudent));
			}
		}
		projDAO.immediateSaveProject(proj);

		flushTextResponse(response, proj.getId());
	}
	private ProjRole getLeaderRole(ProjType projType){
		for(ProjRole pr : projType.getRoles()){
			if(pr.getPrivileges().contains(new Privilege(PrivilegeProject.IS_LEADER))){
				return pr;
			}
		}
		return null;
	}
	
	@RestfulWebMethod 
	public void userLoginProj(Request request, Response response, String username,  
			long timestamp, String hash, String projId) throws IOException{
		authenRequest(request, response, "userLoginProj", username, timestamp, hash, projId);
		
		User u = userDAO.getUserByUsername(username);
		if(u == null){
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Your Username \""+username+"\" can not be found, please contact administrator.");
            return;
		}
		if(!u.isEnabled()){
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Your Username \""+username+"\" is disabled, please contact administrator.");
            return;
		}
		
		Project proj = projDAO.getProjectById(projId);
		ProjUser pu = proj.getMember(u);
		if(pu==null){ //not yet enrolled, then enroll as Student
			proj.addMember(new ProjUser(proj, u, projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_STUDENT)));
			projDAO.immediateSaveProject(proj);
		}
		
		doLogin(request, response, u);
		String redirectPage = linkSource.createPageRenderLinkWithContext(Home.class, projId).toRedirectURI();
		response.sendRedirect(redirectPage);
		return;
	}
	
	@RestfulWebMethod 
	public void getProject(Request request, Response response, String username, 
			long timestamp, String hash, String projId) throws IOException{
		authenRequest(request, response, "getProject", username, timestamp, hash, projId);
		
		Project p = projDAO.getProjectById(projId);
		if(p==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested projectID \""+projId+"\" can not be found, please contact administrator.");
			return;
		}
		//check if user is a member of the project (or has access right)
		User u = userDAO.getUserByUsername(username);
		if(!p.getUsers().contains(u)){
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You are not allowed to access, because you are not a member of the project.");
			return;
		}
		
		
		
		JSONObject jObj = p.toJSONObject();
		
		flushJSONResponse(response, jObj.toString());
	}
	@RestfulWebMethod 
	public void getProjectsByUser(Request request, Response response, String username, 
			long timestamp, String hash) throws IOException{
		authenRequest(request, response, "getProjectsByUser", username, timestamp, hash);
		
		User curUser = userDAO.getUserByUsername(username);
		
		JSONArray jsonArr = new JSONArray();
		
		List<Project> projects = projDAO.getProjectsByMember(curUser);;
/*		//filter to get only active projects
		for(int i=projects.size()-1; i>=0; i--){
			Project p = projects.get(i);
			if(!p.getStatus().getName().equals(PredefinedNames.PROJSTATUS_ACTIVE)
				&& !p.getStatus().getName().equals(PredefinedNames.PROJSTATUS_INACTIVE)){
				projects.remove(i);
			}
		}
*/
		for(Project p : projects){
			jsonArr.put(p.toJSONObject());
		}
		
		flushJSONResponse(response, jsonArr.toString());
	}
	
	@RestfulWebMethod 
	public void getProjectsByCourseId(Request request, Response response, String username, 
			long timestamp, String hash, String courseId) throws IOException{
		authenRequest(request, response, "getProjectsByCourseId", username, timestamp, hash, courseId);
		
		//User curUser = userDAO.getUserByUsername(username); , to check member of the project
		
		JSONArray jsonArr = new JSONArray();
		
		List<Project> projects = projDAO.getProjectsByCourseId(courseId);;
		
		for(Project p : projects){
			jsonArr.put(p.toJSONObject());
		}
		flushJSONResponse(response, jsonArr.toString());
	}
	@RestfulWebMethod 
	public void getProjectsByCourseIdUserId(Request request, Response response, String username, 
			long timestamp, String hash, String courseId) throws IOException{
		authenRequest(request, response, "getProjectsByCourseIdUserId", username, timestamp, hash, courseId);
		
		//User curUser = userDAO.getUserByUsername(username); , to check member of the project
		
		JSONArray jsonArr = new JSONArray();
		
		List<Project> projects = projDAO.getProjectsByCourseIdUserId(courseId, username);;
		
		for(Project p : projects){
			jsonArr.put(p.toJSONObject());
		}
		flushJSONResponse(response, jsonArr.toString());
	}
	@RestfulWebMethod 
	public void getProjectsByCourseIdGroupId(Request request, Response response, String username, 
			long timestamp, String hash, String courseId, String groupId) throws IOException{
		authenRequest(request, response, "getProjectsByCourseIdGroupId", username, timestamp, hash, courseId, groupId);
		
		//User curUser = userDAO.getUserByUsername(username); , to check member of the project
		
		JSONArray jsonArr = new JSONArray();
		
		List<Project> projects = projDAO.getProjectsByCourseIdGroupId(courseId,groupId);;
		
		for(Project p : projects){
			jsonArr.put(p.toJSONObject());
		}
		
		flushJSONResponse(response, jsonArr.toString());
	}
	
	@RestfulWebMethod 
	public void getGradesByCourseId(Request request, Response response, String username, 
			long timestamp, String hash, String courseId) throws IOException{
		authenRequest(request, response, "getGradesByCourseId", username, timestamp, hash, courseId);
		
		List<Project> projects = projDAO.getProjectsByCourseId(courseId);
		
		
		/*if(projects.size()==0){
			PrintWriter writer = response.getPrintWriter("text/plain");
			//response.setContentLength(DEFAULT_EMPTY_RETURN_STRING.getBytes().length);
			writer.println(DEFAULT_EMPTY_RETURN_STRING);
			writer.flush();
			writer.close();
			return;
		}
		*/
		JSONArray jsonArr = new JSONArray();
		
		for(Project p : projects){
			JSONObject jProj = new JSONObject();
			jProj.put("project", p.getId());
			
			JSONArray jAssmts = new JSONArray();
			List<Assessment> assmts = assmtDAO.getAssessmentsByProject(p);
			for(Assessment assmt : assmts){
				JSONObject jAssmt = new JSONObject();
				jAssmt.put("name", assmt.getName());
				jAssmt.put("shortname", assmt.getShortName());
				jAssmt.put("description", assmt.getDes());
				jAssmt.put("possiblescore", assmt.getPossibleScore());
				jAssmt.put("weightage", assmt.getWeightage());
				
				JSONArray jScores = new JSONArray();
				for(User u : p.getUsers()){
					AssessmentUser au = assmt.getAssmtUser(u);
					if(au!=null){
						JSONObject jScore = new JSONObject();
						jScore.put("username", u.getUsername());
						jScore.put("score", au.getTotalScoreDisplay());
						
						jScores.put(jScore);
					}
				}
				jAssmt.put("scores", jScores);
				
				jAssmts.put(jAssmt);
			}
			jProj.put("assessments", jAssmts);
			jsonArr.put(jProj);
		}
		
		flushJSONResponse(response, jsonArr.toString());
	}
	@RestfulWebMethod 
	public void getGradesByProjId(Request request, Response response, String username, 
			long timestamp, String hash, String projId) throws IOException{
		authenRequest(request, response, "getGradesByProjId", username, timestamp, hash, projId);
		
		
		JSONArray jAssmts = new JSONArray();
		Project p = projDAO.getProjectById(projId);
		
		if(p==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested projectID \""+projId+"\" can not be found, please contact administrator.");
			return;
		}
		
		
			
		List<Assessment> assmts = assmtDAO.getAssessmentsByProject(p);
		for(Assessment assmt : assmts){
			JSONObject jAssmt = new JSONObject();
			jAssmt.put("name", assmt.getName());
			jAssmt.put("shortname", assmt.getShortName());
			jAssmt.put("description", assmt.getDes());
			jAssmt.put("possiblescore", assmt.getPossibleScore());
			jAssmt.put("weightage", assmt.getWeightage());
			
			JSONArray jScores = new JSONArray();
			for(User u : p.getUsers()){
				AssessmentUser au = assmt.getAssmtUser(u);
				if(au!=null){
					JSONObject jScore = new JSONObject();
					jScore.put("username", u.getUsername());
					jScore.put("score", au.getTotalScoreDisplay());
					
					jScores.put(jScore);
				}
			}
			jAssmt.put("scores", jScores);
			
			jAssmts.put(jAssmt);
		}
		
		flushJSONResponse(response, jAssmts.toString());
	}
	
	@RestfulWebMethod 
	public void getTotalScoresByCourseId(Request request, Response response, String username, 
			long timestamp, String hash, String courseId) throws IOException{
		authenRequest(request, response, "getTotalScoresByCourseId", username, timestamp, hash, courseId);
		
		List<Project> projs = projDAO.getProjectsByCourseId(courseId);
		
		JSONArray jsonArr = new JSONArray();
		
		for(Project p : projs){
			JSONObject jProj = new JSONObject();
			jProj.put("project", p.getId());
			jProj.put("projectName", p.getName());
			
			
			JSONArray jScores = new JSONArray();
			for(User u : p.getUsers()){
				List<Assessment> assmts = assmtDAO.getAssessmentsByProject(p);
				float tScore = 0;
				int tWeight = 0;
				for(Assessment a : assmts){
					AssessmentUser au = a.getAssmtUser(u);
					if(au!=null){
						if(!"-".equals(au.getTotalScoreDisplay())){
							tScore += (au.getTotalScore()* a.getWeightage() );
							tWeight += a.getWeightage();
						}
					}
				}
				float totalScore = 0;
				if(tWeight!=0)
					totalScore = tScore/tWeight;
				
				JSONObject jScore = new JSONObject();
				jScore.put("username", u.getUsername());
				jScore.put("totalScore", Util.formatDecimal(totalScore));
				
				jScores.put(jScore);
			}
			jProj.put("scores", jScores);
			
			jsonArr.put(jProj);
		}
		
		flushJSONResponse(response, jsonArr.toString());
	}
	@RestfulWebMethod 
	public void getAllSchools(Request request, Response response, String username, 
			long timestamp, String hash) throws IOException{
		authenRequest(request, response, "getAllSchools", username, timestamp, hash);
		

		JSONArray jsonArr = new JSONArray();
		List<School> schList = schoolDAO.getAllSchools();
		for(School s : schList){
			jsonArr.put(s.toJSONObject());
		}
		
		
		flushJSONResponse(response, jsonArr.toString());
	}

	
	@RestfulWebMethod 
	public void getActiveAnnouncementsByProjId(Request request, Response response, String username, 
			long timestamp, String hash, String projId) throws IOException{
		authenRequest(request, response, "getActiveAnnouncementsByProjId", username, timestamp, hash, projId);
		
		Project p = projDAO.getProjectById(projId);
		if(p==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested projectID \""+projId+"\" can not be found, please contact administrator.");
			return;
		}
		
		User u = userDAO.getUserByUsername(username);
		if(u==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested username \""+username+"\" can not be found, please contact administrator.");
			return;
		}
		
		JSONArray jsonArr = new JSONArray();
		
		List<Announcement> annmts = annmtDAO.getActiveAnnouncements(p, u);
		for(Announcement a : annmts){
			jsonArr.put(a.toJSONObject());
		}
		List<SchlTypeAnnouncement> stAnnmts = stAnnmtDAO.getActiveSchlTypeAnnouncements(p, u);
		for(SchlTypeAnnouncement a : stAnnmts){
			JSONObject j = a.toJSONObject();
			j.put("creatorUsername", "admin"); //override the value
			j.put("project", p.getId());
			jsonArr.put(j);
		}
		
		flushJSONResponse(response, jsonArr.toString());
	}
	@RestfulWebMethod 
	public void getActiveAnnouncementsByCourseId(Request request, Response response, String username, 
			long timestamp, String hash, String courseId) throws IOException{
		authenRequest(request, response, "getActiveAnnouncementsByCourseId", username, timestamp, hash, courseId);
		
		Set<Integer> hashSet = new HashSet<Integer>();//to keep track of duplicate
		
		User curUser = userDAO.getUserByUsername(username);
		if(curUser==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested username \""+username+"\" can not be found, please contact administrator.");
			return;
		}
		JSONArray jsonArr = new JSONArray();
		
		List<Project> projs = projDAO.getProjectsByCourseId(courseId);
		
		for(Project p : projs){
			List<Announcement> annmts = annmtDAO.getActiveAnnouncements(p, curUser);
			for(Announcement a : annmts){
				jsonArr.put(a.toJSONObject());
			}
			List<SchlTypeAnnouncement> stAnnmts = stAnnmtDAO.getActiveSchlTypeAnnouncements(p, curUser);
			for(SchlTypeAnnouncement a : stAnnmts){
				JSONObject j = a.toJSONObject();
				j.put("creatorUsername", "admin"); //override the value
				if(hashSet.add(a.getId())){ //check for duplicate, to add only once
					jsonArr.put(j);
				}
			}
			
		}
		
		flushJSONResponse(response, jsonArr.toString());
	}
	
	@RestfulWebMethod 
	public void getSystemRolesByUsername(Request request, Response response, String username, 
			long timestamp, String hash) throws IOException{
		authenRequest(request, response, "getSystemRolesByUsername", username, timestamp, hash);
		
		User curUser = userDAO.getUserByUsername(username); 
		if(curUser==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested username \""+username+"\" can not be found, please contact administrator.");
			return;
		}
		JSONArray jsonArr = new JSONArray();
		
		JSONObject j = new JSONObject();
		j.put("roleID", curUser.getSysRole().getId());
		j.put("roleName", curUser.getSysRole().getName());
		j.put("roleAliasName", curUser.getSysRole().getAlias());
		jsonArr.put(j);

		for(SysroleUser eRole : curUser.getExtraRoles()){
			SysRole srole = eRole.getSysRole();

			JSONObject j1 = new JSONObject();
			j1.put("roleID", srole.getId());
			j1.put("roleName", srole.getName());
			j1.put("roleAliasName", srole.getAlias());
			if(eRole.getParam() != null)
				j1.put("param", eRole.getParam());
			jsonArr.put(j1);			
		}
				
		flushJSONResponse(response, jsonArr.toString());
	}
	
	@RestfulWebMethod 
	public void getProjectRoleByUsernameByProjectId(Request request, Response response, String username, 
			long timestamp, String hash, String projId) throws IOException{
		authenRequest(request, response, "getProjectRoleByUsernameByProjectId", username, timestamp, hash, projId);
		
		Project curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested ProjectID \""+projId+"\" can not be found, please contact administrator.");
			return;
		}
		User curUser = userDAO.getUserByUsername(username); 
		if(curUser==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested username \""+username+"\" can not be found, please contact administrator.");
			return;
		}
		JSONObject j = new JSONObject();
		
		ProjUser pu = curProj.getMember(curUser);
		if(pu !=null){
			ProjRole pr = pu.getRole();
			j.put("roleID", pr.getId());
			j.put("roleName", pr.getName());
			j.put("roleAliasName", pr.getAlias());
			if(pu.getCustomRoleName() != null)
				j.put("roleCustomName", pu.getCustomRoleName());
		}
		
		flushJSONResponse(response, j.toString());
	}


	@RestfulWebMethod 
	public void getAllGroups(Request request, Response response, String username, 
			long timestamp, String hash) throws IOException{
		authenRequest(request, response, "getAllGroups", username, timestamp, hash);
		
		JSONArray jsonArr = new JSONArray();
		List<Group> groupList = groupDAO.getAllGroups();
		for(Group g : groupList){
			jsonArr.put(g.toJSONObject(false));
		}
		
		flushJSONResponse(response, jsonArr.toString());
	}

	@RestfulWebMethod 
	public void getGroupsByProjectId(Request request, Response response, String username, 
			long timestamp, String hash, String projId) throws IOException{
		authenRequest(request, response, "getGroupsByProjectId", username, timestamp, hash, projId);
		
		Project curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested ProjectID \""+projId+"\" can not be found, please contact administrator.");
			return;
		}
		JSONArray jsonArr = new JSONArray();
		List<Group> groupList = groupDAO.getGroupsByProject(curProj);
		for(Group g : groupList){
			jsonArr.put(g.toJSONObject());
		}
		
		
		flushJSONResponse(response, jsonArr.toString());
	}
	
	@RestfulWebMethod 
	public void getGroupById(Request request, Response response, String username, 
			long timestamp, String hash, long groupId) throws IOException{
		authenRequest(request, response, "getGroupById", username, timestamp, hash, String.valueOf(groupId));
		
		
		JSONObject jsonObj = new JSONObject();
		Group g = groupDAO.getGroupById(groupId);
		if(g==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested GroupID \""+groupId+"\" can not be found, please contact administrator.");
			return;
		}
		jsonObj = g.toJSONObject();
		
		flushJSONResponse(response, jsonObj.toString());
	}
	
	
	@RestfulWebMethod 
	public void createGroup(Request request, Response response, String username, 
			long timestamp, String hash) throws IOException{
		authenRequest(request, response, "createGroup", username, timestamp, hash);
		BufferedReader bReader = requestGlobal.getHTTPServletRequest().getReader();
		String str = "";
		String s;
		while((s = bReader.readLine()) != null){
			str += s;
		}
		JSONObject json = new JSONObject(str);
			Group g = new Group();
			g.setGroupType(json.getString("groupName"));
			//assume project already exists
			Project p = projDAO.getProjectById(json.getString("projID"));
			g.setProject(p);
			if(json.has("bbGroupID")){
				g.setBbID(json.getString("bbGroupID"));
			}
			//assume user already exists
			User u = userDAO.getUserByUsername(username);
			g.setCreator(u);
			g.setCdate(new Date());
			g.setMdate(new Date());
			if(json.has("allowSelfEnroll"))
				g.setAllowSelfEnroll(Boolean.parseBoolean(json.getString("allowSelfEnroll")));
			if(json.has("maxPerGroup"))
				g.setMaxPerGroup(Integer.parseInt(json.getString("maxPerGroup")));
			
			groupDAO.saveGroup(g);
			
			//enroll subgroup students
			if(json.has("subgroups")){
				JSONArray subGroups = json.getJSONArray("subgroups");
				for(int i=0; i<subGroups.length(); i++){
					JSONObject guJson = subGroups.getJSONObject(i);
					GroupUser gu = new GroupUser();
					g.addGroupUser(gu);
					gu.setGroup(g);
					gu.setGroupNum(guJson.getInt("groupNum"));
					gu.setGroupNumName(guJson.getString("groupNumName"));
					if(guJson.has("tutorUsername")){
						gu.setTutor(userDAO.getUserByUsername(guJson.getString("tutorUsername")));
					}
					if(guJson.has("groupMembers")){
						JSONArray gMembers = guJson.getJSONArray("groupMembers");
						for(int j=0; j<gMembers.length(); j++){
							String username1 = gMembers.getString(j);
							User u1 = userDAO.getUserByUsername(username1);
							if(!gu.getUsers().contains(u1)){
								gu.addUser(u1);
							}
						}
					}
					groupDAO.immediateSaveGroupUser(gu);
				}
			}
			groupDAO.saveGroup(g);
	
			flushTextResponse(response, String.valueOf(g.getId()));
	}
	
	
	@RestfulWebMethod 
	public void addUserToGroup(Request request, Response response, String username, 
			long timestamp, String hash) throws IOException{
		authenRequest(request, response, "addUserToGroup", username, timestamp, hash);
		BufferedReader bReader = requestGlobal.getHTTPServletRequest().getReader();
		String str = "";
		String s;
		while((s = bReader.readLine()) != null){
			str += s;
		}
		JSONObject json = new JSONObject(str);
		try {
			
			Group g = groupDAO.getGroupById(json.getLong("groupID"));
			if(g==null)
				throw new RuntimeException("Invalid GroupID");
			int subGroupNum = json.getInt("subgroupNum");
			for(int i=0; i<g.getGroupUsers().size(); i++){
				GroupUser gu = g.getGroupUsers().get(i);
				if(gu.getGroupNum() == subGroupNum){
					User u1 = userDAO.getUserByUsername(json.getString("usernameToAdd"));
					if(!gu.getUsers().contains(u1)){
						gu.addUser(u1);
						groupDAO.immediateSaveGroupUser(gu);

						flushTextResponse(response, "true"); //success
						return;
						
					}
				}
			}
			
		}
		catch(Exception e){
			logger.error(e.getMessage());
		}
		finally{
			flushTextResponse(response, "false");
		}
	}
	
	
	@RestfulWebMethod 
	public void removeUserFromGroup(Request request, Response response, String username, 
			long timestamp, String hash) throws IOException{
		authenRequest(request, response, "removeUserFromGroup", username, timestamp, hash);
		BufferedReader bReader = requestGlobal.getHTTPServletRequest().getReader();
		String str = "";
		String s;
		while((s = bReader.readLine()) != null){
			str += s;
		}
		JSONObject json = new JSONObject(str);
		try {
			
			Group g = groupDAO.getGroupById(json.getLong("groupID"));
			if(g==null)
				throw new RuntimeException("Invalid GroupID");
			int subGroupNum = json.getInt("subgroupNum");
			for(int i=0; i<g.getGroupUsers().size(); i++){
				GroupUser gu = g.getGroupUsers().get(i);
				if(gu.getGroupNum() == subGroupNum){
					User u1 = userDAO.getUserByUsername(json.getString("usernameToRemove"));
					if(gu.getUsers().contains(u1)){
						gu.removeUser(u1);
						groupDAO.immediateSaveGroupUser(gu);

						flushTextResponse(response, "true");//success
						return;
					}
				}
			}
		}
		catch(Exception e){
			logger.error(e.getMessage());
		}
		finally{
			flushTextResponse(response, "false");
		}
	}

	@RestfulWebMethod 
	public void uploadProjectAttachment(Request request, Response response, String username, 
			long timestamp, String hash, String projId, String fileName) throws IOException{
		authenRequest(request, response, "uploadProjectAttachment", username, timestamp, hash, projId, fileName);
		
		try{
			Project proj = projDAO.getProjectById(projId);
			if(proj==null){
				throw new RecordNotFoundException("Not found the projID: "+ projId);
			}
			User user = userDAO.getUserByUsername(username);
			
			HttpServletRequest httpReq = requestGlobal.getHTTPServletRequest();
			String contentType = httpReq.getContentType();
			long contentLength = httpReq.getContentLength();
			InputStream inStream = httpReq.getInputStream();
		
			ProjectAttachedFile f = new ProjectAttachedFile();
			f.setId(Util.generateUUID());
			f.setFileName(fileName);
//			f.setAliasName(aliasFileName);
			f.setContentType(contentType);
			f.setSize(contentLength);
			f.setCreator(user);
			f.setPath(attFileManager.saveAttachedFile(inStream, fileName, f.getId(), PredefinedNames.PROJECT_INFO, proj.getId()));
			
			proj.addAttachFile(f);
		
			projDAO.immediateSaveProject(proj);
			
			flushTextResponse(response, f.getId());
		}
		catch(Exception e){
			logger.error(e.getMessage());
			
			flushTextResponse(response, "false");
		}
	}

	
	@RestfulWebMethod 
	public void updateSysRoleOfUser(Request request, Response response, String username, 
			long timestamp, String hash, String newSysRoleName) throws IOException{

		authenRequest(request, response, "updateSysRoleOfUser", username, timestamp, hash, newSysRoleName);
			
		SysRole newSysRole = sysRoleDAO.getSysRoleByName(newSysRoleName);
		if(newSysRole == null){
			flushTextResponse(response, "false"); //invalid sysrole name
			return;
		}
			
		
		User user = userDAO.getUserByUsername(username);
		if(user == null){
			flushTextResponse(response, "false"); //invalid username
			return;
		}
		
		
		user.setSysRole(newSysRole);
		userDAO.save(user);
		
		flushTextResponse(response, "true");
	}	
	
	@RestfulWebMethod 
	public void addSysRoleOfUser(Request request, Response response, String username, 
			long timestamp, String hash, String newSysRoleName, String newSysRole_SchlNameOrProjTypeName) throws IOException{

		authenRequest(request, response, "addSysRoleOfUser", username, timestamp, hash, newSysRoleName, newSysRole_SchlNameOrProjTypeName);
				
		SysRole newSysRole = sysRoleDAO.getSysRoleByName(newSysRoleName);
		if(newSysRole == null){
			flushTextResponse(response, "false"); //invalid sysrole name
			return;
		}
		
		User user = userDAO.getUserByUsername(username);
		if(user == null){
			flushTextResponse(response, "false"); //invalid username
			return;
		}
		
		
		SysroleUser su = new SysroleUser();
		su.setSysRole(newSysRole);
		su.setUser(user);
		if(newSysRole.getName().equals(PredefinedNames.SYSROLE_PROJTYPE_ADMIN)){
			ProjType projType = projTypeDAO.getTypeByName(newSysRole_SchlNameOrProjTypeName);
			if(projType == null){
				flushTextResponse(response, "false"); //invalid projType name
				return;
			}
			su.setParam(projType.getId());
		}
		if(newSysRole.getName().equals(PredefinedNames.SYSROLE_SCHOOL_ADMIN)){
			School schl = schoolDAO.getSchoolByName(newSysRole_SchlNameOrProjTypeName);
			if(schl == null){
				flushTextResponse(response, "false"); //invalid school name
				return;
			}
			su.setParam(schl.getId());
		}
		
		user.addExtraRole(su);
		userDAO.save(user);
		
		flushTextResponse(response, "true"); //success
		return;

	}	
	
	@RestfulWebMethod 
	public void removeSysRoleOfUser(Request request, Response response, String username, 
			long timestamp, String hash, String sysRoleName) throws IOException{

		authenRequest(request, response, "removeSysRoleOfUser", username, timestamp, hash, sysRoleName);
				
		User user = userDAO.getUserByUsername(username);
		if(user == null){
			flushTextResponse(response, "false"); //invalid username
			return;
		}
		boolean isFound = false;
		
		if(user.getSysRole()!=null){
			if(user.getSysRole().getName().equalsIgnoreCase(sysRoleName)){
				SysRole sysRoleUser = sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_USER);
				user.setSysRole(sysRoleUser); //instead of remove, reset it to "User"
				isFound = true;
			}
		}
		for(SysroleUser sysRoleUser : user.getExtraRoles()){
			if(sysRoleUser.getSysRole().getName().equalsIgnoreCase(sysRoleName)){
				user.getExtraRoles().remove(sysRoleUser);
				
				isFound = true;
				break;
			}
		}
		
		if(isFound){
			userDAO.save(user);
			flushTextResponse(response, "true");
			return;	
		}
		else{
			flushTextResponse(response, "false"); //not found
			return;	
		}
		
	}	
	
	/*****************************/
	
	@RestfulWebMethod 
	public void getSchoolRubricsByUser(Request request, Response response, String username, 
			long timestamp, String hash) throws IOException{
		authenRequest(request, response, "getSchoolRubricsByUser", username, timestamp, hash);
		
		User user = userDAO.getUserByUsername(username);
		if(user == null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid username \""+username+"\" , please contact administrator.");
			return;
		}
		
		JSONArray jsonArr = new JSONArray();
		List<Integer> accessibleSchlIDs = getAccessibleSchlIDs(user);
		for(int schlID : accessibleSchlIDs){
			School schl = schoolDAO.getSchoolById(schlID);
			if(schl==null)
				continue;
			List<Rubric> schlRList = assmtDAO.getMasterRubrics(schl);

			
			for(Rubric r : schlRList){
				JSONObject jsonObj = r.toJSONObject();
				jsonObj.put("numAssmtUsed",  assmtDAO.countAssessmentByRubric(r));
				jsonObj.put("numEvalUsed",  assmtDAO.countEvaluationByRubric(r));
				jsonArr.put(jsonObj);
			}
		}
		
		flushJSONResponse(response, jsonArr.toString());
	}
	@RestfulWebMethod 
	public void getRubricsByUser(Request request, Response response, String username, 
			long timestamp, String hash) throws IOException{
		authenRequest(request, response, "getRubricsByUser", username, timestamp, hash);
		
		User user = userDAO.getUserByUsername(username);
		if(user == null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid username \""+username+"\" , please contact administrator.");
			return;
		}
		
		JSONArray jsonArr = new JSONArray();
		List<Rubric> rList = assmtDAO.getRubricsByOwner(user);
		for(Rubric r : rList){
			JSONObject jsonObj = r.toJSONObject();
			jsonObj.put("numAssmtUsed",  assmtDAO.countAssessmentByRubric(r));
			jsonObj.put("numEvalUsed",  assmtDAO.countEvaluationByRubric(r));
			jsonArr.put(jsonObj);
		}
		
		flushJSONResponse(response, jsonArr.toString());
	}
	
	@RestfulWebMethod 
	public void searchSharedRubrics(Request request, Response response, String username, 
			long timestamp, String hash, String searchText) throws IOException{
		authenRequest(request, response, "searchSharedRubrics", username, timestamp, hash, searchText);
		
		JSONArray jsonArr = new JSONArray();
		List<Rubric> rList = assmtDAO.searchRubrics(searchText, null, true, null, null);
		for(Rubric r : rList){
			JSONObject jsonObj = r.toJSONObject();
			jsonObj.put("numAssmtUsed",  assmtDAO.countAssessmentByRubric(r));
			jsonObj.put("numEvalUsed",  assmtDAO.countEvaluationByRubric(r));
			jsonArr.put(jsonObj);
		}
		
		flushJSONResponse(response, jsonArr.toString());
	}
	
	@RestfulWebMethod 
	public void getRubricsByID(Request request, Response response, String username, 
			long timestamp, String hash, int id) throws IOException{
		authenRequest(request, response, "getRubricsByID", username, timestamp, hash, String.valueOf(id));
		
		Rubric r = assmtDAO.getRubricById(id);
		if(r == null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid ID \""+id+"\" , please contact administrator.");
			return;
		}
		
		JSONObject jsonObj = r.toJSONObject();
		jsonObj.put("numAssmtUsed",  assmtDAO.countAssessmentByRubric(r));
		jsonObj.put("numEvalUsed",  assmtDAO.countEvaluationByRubric(r));
	
		flushJSONResponse(response, jsonObj.toString());
	}
	
	@RestfulWebMethod 
	public void getAssessmentsByProjectId(Request request, Response response, String username, 
			long timestamp, String hash, String projId) throws IOException{
		authenRequest(request, response, "getAssessmentsByProjectId", username, timestamp, hash, projId);
		
		Project curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested ProjectID \""+projId+"\" can not be found, please contact administrator.");
			return;
		}
		JSONArray jsonArr = new JSONArray();
		List<Assessment> assmtList = assmtDAO.getAssessmentsByProject(curProj);
		for(Assessment a : assmtList){
			jsonArr.put(a.toJSONObject());
		}
		
		flushJSONResponse(response, jsonArr.toString());
	}
	
	@RestfulWebMethod 
	public void getPeerEvaluationsByProjectId(Request request, Response response, String username, 
			long timestamp, String hash, String projId) throws IOException{
		authenRequest(request, response, "getPeerEvaluationsByProjectId", username, timestamp, hash, projId);
		
		Project curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested ProjectID \""+projId+"\" can not be found, please contact administrator.");
			return;
		}
		JSONArray jsonArr = new JSONArray();
		List<Evaluation> evalList = evalDAO.getEvaluationsByProject(curProj);
		for(Evaluation a : evalList){
			jsonArr.put(a.toJSONObject());
		}
		
		flushJSONResponse(response, jsonArr.toString());
	}
	
	@RestfulWebMethod 
	public void getPeerEvaluationsByProjectIdByStudentUsername(Request request, Response response, String username, 
			long timestamp, String hash, String projId, String studentUsername) throws IOException{
		authenRequest(request, response, "getPeerEvaluationsByProjectIdByStudentUsername", username, timestamp, hash, projId, studentUsername);
		
		Project curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested ProjectID \""+projId+"\" can not be found, please contact administrator.");
			return;
		}
		User student = userDAO.getUserByUsername(studentUsername);
		if(student==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested StudentUsername \""+studentUsername+"\" can not be found, please contact administrator.");
			return;
		}
		
		JSONArray jsonArr = new JSONArray();
		List<Evaluation> evalList = evalDAO.getEvaluationsByProject(curProj);
		for(Evaluation e : evalList){
			JSONObject jsonE = e.toJSONObject();
			jsonE.put("status", getEvalStatus(e, student, false));
			jsonE.put("subGroupName", getGroupTypeName(e.getGroup(), student));
			
			jsonArr.put(jsonE);
		}
		
		flushJSONResponse(response, jsonArr.toString());
	}
	
	@RestfulWebMethod 
	public void getAssessmentMarksByProjectIdByStudentUsername(Request request, Response response, String username, 
			long timestamp, String hash, String projId, String studentUsername) throws IOException{
		authenRequest(request, response, "getAssessmentMarksByProjectIdByStudentUsername", username, timestamp, hash, projId, studentUsername);
		
		Project curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested ProjectID \""+projId+"\" can not be found, please contact administrator.");
			return;
		}
		User student = userDAO.getUserByUsername(studentUsername);
		if(student==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested StudentUsername \""+studentUsername+"\" can not be found, please contact administrator.");
			return;
		}
		
		JSONArray jAssmts = new JSONArray();
		List<Assessment> assmts = assmtDAO.getAssessmentsByProject(curProj);
		for(Assessment assmt : assmts){
			JSONObject jAssmt = new JSONObject();
			jAssmt.put("id", assmt.getId());
			jAssmt.put("name", assmt.getName());
			jAssmt.put("possiblescore", assmt.getPossibleScore());
			jAssmt.put("weightage", assmt.getWeightage());
			jAssmt.put("allowViewGrade", assmt.isAllowViewGrade());
			jAssmt.put("allowViewGradeDetail", assmt.getAllowViewGradeDetail());
			
			
			AssessmentUser au = assmt.getAssmtUser(student);
			if(au!=null){
				JSONObject jScore = new JSONObject();
				jScore.put("grade", au.getTotalGrade());
				jScore.put("score", au.getTotalScoreDisplay());
				jScore.put("comment", au.getComments());
				jScore.put("assessedDate", au.getAssessedDate().getTime());
				
				jAssmt.put("score", jScore);
			}
			
			jAssmts.put(jAssmt);
		}
		
		flushJSONResponse(response, jAssmts.toString());

	}
	
	@RestfulWebMethod 
	public void getPeerEvaluationMarksByProjectIdByStudentUsername(Request request, Response response, String username, 
			long timestamp, String hash, String projId, String studentUsername) throws IOException{
		authenRequest(request, response, "getPeerEvaluationMarksByProjectIdByStudentUsername", username, timestamp, hash, projId, studentUsername);
		
		Project curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested ProjectID \""+projId+"\" can not be found, please contact administrator.");
			return;
		}
		User student = userDAO.getUserByUsername(studentUsername);
		if(student==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your requested StudentUsername \""+studentUsername+"\" can not be found, please contact administrator.");
			return;
		}
		
		JSONArray jEvals = new JSONArray();
		List<Evaluation> evals = evalDAO.getEvaluationsByProject(curProj);
		for(Evaluation eval : evals){
			JSONObject jEval = new JSONObject();
			jEval.put("id", eval.getId());
			jEval.put("name", eval.getName());
			jEval.put("weightage", eval.getWeightage());
			jEval.put("allowViewGrade", eval.isAllowViewGrade());
			jEval.put("allowViewGradeDetail", eval.getAllowViewGradeDetail());
			
			List<EvaluationUser> euList = loadEvalUserList(eval, student);
			if(euList!=null && euList.size()>0){
				float score = getEvalUserScore(euList);
				int num = getEvalUserCount(euList);
				JSONObject jScore = new JSONObject();
				jScore.put("grade", convertScoreToGrade(score, 100));
				jScore.put("score", score);
				jScore.put("numEvaluator", num);
				
				jEval.put("score", jScore);
			}
			
			jEvals.put(jEval);
		}
		
		flushJSONResponse(response, jEvals.toString());
	}
	
	@RestfulWebMethod 
	public void deleteGroup(Request request, Response response, String username, 
			long timestamp, String hash, String bbGroupID) throws IOException{
		authenRequest(request, response, "deleteGroup", username, timestamp, hash, bbGroupID);

		JSONObject jResult = new JSONObject();
		String resultMsg = "";

//		Group group = groupDAO.getGroupById(Long.parseLong(groupId));
		Group g = groupDAO.getGroupByBBID(bbGroupID);
		if(g==null){
			resultMsg = "bbGroupID does not exist, " + bbGroupID;
		} else if(groupDAO.isGroupBeingUsePE(g)){
			resultMsg = String.format("Cannot delete, the group '%S' is being used by Peer Evaluation Module", g.getGroupType());
		}
		else if(groupDAO.isGroupBeingUseAS(g)){
			resultMsg = String.format("Cannot delete, the group '%S' is being used by Assessment Module", g.getGroupType());
		}
		else{
			groupDAO.deleteGroup(g);
			jResult.put("result", true);
		}
		
		if(resultMsg.isEmpty()){
			resultMsg = "Success";
			jResult.put("result", true);
		}else{
			jResult.put("result", false);
		}
		
		jResult.put("resultMsg", resultMsg);
		flushJSONResponse(response, jResult.toString());	

	}
	
	@RestfulWebMethod 
	public void syncGroups(Request request, Response response, String username, 
			long timestamp, String hash) throws IOException{
		authenRequest(request, response, "syncGroups", username, timestamp, hash);
		BufferedReader bReader = requestGlobal.getHTTPServletRequest().getReader();
		String str = "";
		String s;
		while((s = bReader.readLine()) != null){
			str += s;
		}
		JSONObject json = new JSONObject(str);
		
		JSONObject jResult = new JSONObject();
		
		User curUser = userDAO.getUserByUsername(username);
		String resultMsg = "";
		String resultMsgDebug = "";
		
		String projID = json.getString("projID");
		Project proj = projDAO.getProjectById(projID);
		if(proj == null ){
			jResult.put("result", false);
			jResult.put("resultMsg", "Project not yet created. or ID not exist.");
			flushJSONResponse(response, jResult.toString());
			return;
		}
		
		HashMap<String, School> schlMap = new HashMap<String, School>();
		
		//sync, enroll leaders (create if needed)
		if(json.has("instructors")){
			int added = 0;
			int removed = 0;
			ProjRole prLeader = getLeaderRole(proj.getType());
			if(prLeader==null)
				prLeader = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_LEADER);
			SysRole srFaculty = sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_FACULTY);
			
			JSONArray jUsers = json.getJSONArray("instructors");
			ArrayList<Integer> userIDs = new ArrayList<Integer>();
			for(int i=0; i<jUsers.length(); i++){
				JSONObject jUser = jUsers.getJSONObject(i);
				String schlName = jUser.getString("school");
				String userName = jUser.getString("userName");
				School schl = null;
				if(schlMap.containsKey(schlName))
					schl = schlMap.get(schlName);
				else{
					schl = schoolDAO.getSchoolByName(schlName);
					if(schl==null)
						schl = schoolDAO.getSchoolByName(PredefinedNames.SCHOOL_OTHERS);
					schlMap.put(schlName, schl);
				}
				
				User u1 = getOrCreateUser(userName , jUser.getString("firstName")
						,jUser.getString("lastName"),jUser.getString("email")
						,srFaculty, schl);
				userIDs.add(u1.getId());
				//add if not exist in db
				if(!proj.hasMember(u1)){
					proj.addMember(new ProjUser(proj, u1, prLeader));
					added++;
				}
			}
			//remove if db has more
			List<ProjUser> leaders = proj.getProjUsersByRole(prLeader);
			for(int i=0; i<leaders.size(); i++){
				if(! userIDs.contains(leaders.get(i).getUser().getId())){
					proj.removeMember(leaders.get(i));	
					removed++;
				}
			}
			resultMsgDebug += String.format("%d instructor(s) added, %d instructor(s) removed. \n", added, removed);
		}
		
		//sync, enroll students (create if needed)
		if(json.has("students")){
			int added = 0;
			int removed = 0;
			ProjRole prStudent = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_STUDENT);
			SysRole srStudent = sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_STUDENT);
			
			JSONArray jUsers = json.getJSONArray("students");
			ArrayList<Integer> userIDs = new ArrayList<Integer>();
			for(int i=0; i<jUsers.length(); i++){
				JSONObject jUser = jUsers.getJSONObject(i);
				String schlName = jUser.getString("school");
				String userName = jUser.getString("userName");
				School schl = null;
				if(schlMap.containsKey(schlName))
					schl = schlMap.get(schlName);
				else{
					schl = schoolDAO.getSchoolByName(schlName);
					if(schl==null)
						schl = schoolDAO.getSchoolByName(PredefinedNames.SCHOOL_OTHERS);
					schlMap.put(schlName, schl);
				}
				
				User u1 = getOrCreateUser(userName , jUser.getString("firstName")
						,jUser.getString("lastName"),jUser.getString("email")
						,srStudent, schl);
				userIDs.add(u1.getId());
				//add if not exist in db
				if(!proj.hasMember(u1)){
					proj.addMember(new ProjUser(proj, u1, prStudent));
					added++;
				}
			}
			//remove if db has more
			List<ProjUser> students = proj.getProjUsersByRole(prStudent);
			for(int i=0; i<students.size(); i++){
				if(! userIDs.contains(students.get(i).getUser().getId())){
					proj.removeMember(students.get(i));	
					removed++;
				}
			}
			resultMsgDebug += String.format("%d student(s) added, %d student(s) removed. \n", added, removed);
		}
		
		
		//sync Group
		JSONArray jGroupsets = json.getJSONArray("groupsets");
		for(int i=0; i<jGroupsets.length(); i++){
			JSONObject jGroupset = jGroupsets.getJSONObject(i);
			Group g = groupDAO.getGroupByBBID(jGroupset.getString("bbGroupID"));
			if(g==null){ //if not exist in DB, create new
				g = new Group();
				g.setGroupType(jGroupset.getString("groupsetName"));
				g.setProject(proj);
				g.setBbID(jGroupset.getString("bbGroupID"));
				g.setCreator(curUser);
				g.setCdate(new Date());
				g.setMdate(new Date());
				if(jGroupset.has("allowSelfEnroll"))
					g.setAllowSelfEnroll(Boolean.parseBoolean(jGroupset.getString("allowSelfEnroll")));
				if(jGroupset.has("maxPerGroup"))
					g.setMaxPerGroup(Integer.parseInt(jGroupset.getString("maxPerGroup")));
				
				groupDAO.saveGroup(g);
				resultMsgDebug += "Groupset (" + g.getGroupType() +") created" ;
				
				//enroll subgroup students
				if(jGroupset.has("groups")){
					JSONArray subGroups = jGroupset.getJSONArray("groups");
					int added = 0;
					for(int j=0; j<subGroups.length(); j++){
						JSONObject guJson = subGroups.getJSONObject(j);
						GroupUser gu = new GroupUser();
						g.addGroupUser(gu);
						gu.setGroup(g);
						gu.setGroupNum(j+1);
						gu.setGroupNumName(guJson.getString("groupName"));
						gu.setBbID(guJson.getString("bbsubGroupID"));
						if(guJson.has("tutorUsername")){
							gu.setTutor(userDAO.getUserByUsername(guJson.getString("tutorUsername")));
						}
						if(guJson.has("groupMembers")){
							JSONArray gMembers = guJson.getJSONArray("groupMembers");
							for(int k=0; k<gMembers.length(); k++){
								String username1 = gMembers.getString(k);
								User u1 = userDAO.getUserByUsername(username1);
								if(!gu.getUsers().contains(u1)){
									gu.addUser(u1);
								}
							}
						}
						groupDAO.immediateSaveGroupUser(gu);
						added++;
					}
					resultMsgDebug += " ,"+ added +" group(s) added.";
					
				}
				groupDAO.saveGroup(g);
				resultMsgDebug += "\n";
				
			}
			else{ //sync name and students in the groups
				if(groupDAO.isGroupBeingUsePE(g) || groupDAO.isGroupBeingUseAS(g)){
					resultMsg += "WARNING: Groupset ("+ jGroupset.getString("groupsetName") +") is being used by Peer Evaluation or Assessment. Changing Grouping after students already started evaluating might cause evaluation data lost." +
							"\n";
					if(json.has("allowChangeGroupWhenBeingUsed") && json.getBoolean("allowChangeGroupWhenBeingUsed")){
						//do nothing
					}else{
						continue; //skip
					}
				}
				
				g.setGroupType(jGroupset.getString("groupsetName"));
				groupDAO.saveGroup(g);
				resultMsgDebug += " Groupset ("+ jGroupset.getString("groupsetName") +") existed, ";
				
				if(jGroupset.has("groups")){
					List<GroupUser> existedSubGroups = g.getGroupUsers();
					
					ArrayList<String> subgroupIDs = new ArrayList<String>();
					
					JSONArray subGroups = jGroupset.getJSONArray("groups");
					for(int j=0; j<subGroups.length(); j++){
						JSONObject guJson = subGroups.getJSONObject(j);
						String bbID = guJson.getString("bbsubGroupID");
						subgroupIDs.add(bbID);
						GroupUser existedSubGroup = findExistSubGroup(bbID, existedSubGroups);
						if(existedSubGroup==null){ //subgroup not exist,=> create
							GroupUser gu = new GroupUser();
							gu.setBbID(bbID);
							gu.setGroup(g);
							g.addGroupUser(gu);
							gu.setGroupNum(g.getGroupUsers().size());
							
							gu.setGroupNumName(guJson.getString("groupName"));
							if(guJson.has("tutorUsername")){
								gu.setTutor(userDAO.getUserByUsername(guJson.getString("tutorUsername")));
							}
							if(guJson.has("groupMembers")){
								JSONArray gMembers = guJson.getJSONArray("groupMembers");
								for(int k=0; k<gMembers.length(); k++){
									String username1 = gMembers.getString(k);
									User u1 = userDAO.getUserByUsername(username1);
									if(!gu.getUsers().contains(u1)){
										gu.addUser(u1);
									}
								}
							}
							groupDAO.immediateSaveGroupUser(gu);
							resultMsgDebug += " group ("+ gu.getGroupNumName() +") added, ";
						}
						else{ //subgroup exist in DB, => sync students
							existedSubGroup.setGroupNumName(guJson.getString("groupName"));
							List<User> members = existedSubGroup.getUsers();
							
							
							resultMsgDebug += " group ("+ existedSubGroup.getGroupNumName() +") existed, ";
							
							ArrayList<String> memList = new ArrayList<String>();
							
							JSONArray gMembers = guJson.getJSONArray("groupMembers");
							for(int k=0; k<gMembers.length(); k++){
								String username1 = gMembers.getString(k);
								User u1 = userDAO.getUserByUsername(username1);
								memList.add(username1);
								if(!members.contains(u1)){
									members.add(u1);
									resultMsgDebug += " member ("+ username1 +") added, ";
								}
							}
							//delete students of subgroup
							for(int m=members.size()-1; m>=0; m--){
								if(!memList.contains(members.get(m).getUsername())){
									resultMsgDebug += " member ("+ members.get(m).getUsername() +") removed, ";
									members.remove(m);
								}
							}
						}
					}
					//delete subgroup that eureka exist, intulearn not exist
					for(int j=0; j<g.getGroupUsers().size(); j++){
						if(!subgroupIDs.contains(g.getGroupUsers().get(j).getBbID())){
							g.removeGroupUser(g.getGroupUsers().get(j));
							
							resultMsgDebug += " group ("+ g.getGroupUsers().get(j).getGroupNumName() +") removed, ";
						}
						
					}
					
				}
				groupDAO.saveGroup(g);
				resultMsgDebug += "\n";
			}
			//delete groupset that got eureka exist, intulearn not exist
			//Do not remove for now.
			
		}
		
		projDAO.saveProject(proj);

		if(resultMsg.isEmpty())
			resultMsg = "Success";
		
		jResult.put("result", true);
		jResult.put("resultMsg", resultMsg);
		jResult.put("resultMsgDebug", resultMsgDebug);
		flushJSONResponse(response, jResult.toString());	
	}
	
	private GroupUser findExistSubGroup(String bbID, List<GroupUser> existedSubgroups){
		for(int i=0; i<existedSubgroups.size(); i++){
			if(bbID!=null && bbID.equals(existedSubgroups.get(i).getBbID())){
				return existedSubgroups.get(i);
			}
		}
		return null;
	}
	
	private void flushJSONResponse(Response response, String json) throws IOException{
		PrintWriter writer = response.getPrintWriter("application/json");
		//response.setContentLength(jEvals.toString().getBytes().length);
		writer.println(json);
		writer.flush();
		writer.close();
	}
	private void flushTextResponse(Response response, String txt) throws IOException{
		PrintWriter writer = response.getPrintWriter("text/plain");
		writer.println(txt);
		writer.flush();
		writer.close();
	}
	
	
	/*******************/
	
	public List<EvaluationUser> loadEvalUserList(Evaluation eval, User assessee){
		List<EvaluationUser> evalUserList = eval.getEvalUsersByAssessee( assessee);
		for(int i=evalUserList.size()-1; i>=0; i--){
			EvaluationUser eu = evalUserList.get(i);
			if(!eu.isSubmited()){
				evalUserList.remove(i);
				continue;
			}
			if(!eval.getAllowSelfEvaluation() && assessee.equals(eu.getAssessor())){
				evalUserList.remove(i);
			}
			
		}
		return evalUserList; 
	}
	public float getEvalUserScore(List<EvaluationUser> evalUserList){
		float total=0;
		int num = 0;
		for(EvaluationUser eu : evalUserList){
			double score = eu.getTotalScoreAfterAmend();
			if(score!=0){
				total += score;
				num++;
			}
		}
		if(num==0)
			return 0;
		return total/num;
	}
	public int getEvalUserCount(List<EvaluationUser> evalUserList){
		int num = 0;
		for(EvaluationUser eu : evalUserList){
			double score = eu.getTotalScore();
			if(score!=0){
				num++;
			}
		}
		if(num==0)
			return 0;
		return num;
	}
	public String convertScoreToGrade(float score, int possibleScore){
		if(possibleScore!=0){
			float percent = 100 * score/possibleScore;
			if(percent>=85)
				return "A+";
			if(percent>=80)
				return "A";
			if(percent>=75)
				return "A-";
			if(percent>=70)
				return "B+";
			if(percent>=65)
				return "B";
			if(percent>=60)
				return "B-";
			if(percent>=55)
				return "C+";
			if(percent>=50)
				return "C";
			if(percent>=45)
				return "D+";
			if(percent>=40)
				return "D";
			if(percent>=0)
				return "F";
		}
		return "NA";
	}
	protected List<Integer> getAccessibleSchlIDs(User u){
		List<Integer> idList = new ArrayList<Integer>();
		SysRole sysRole = sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_SCHOOL_ADMIN);
		for(SysroleUser srUser : u.getExtraRoles()){
			if(srUser.getSysRole().equals(sysRole)){
				idList.add(srUser.getParam());
			}
		}
		if(u.getSchool()!=null && !idList.contains(u.getSchool().getId())){
			idList.add(u.getSchool().getId());
		}
		return idList;
	}
	
	
	protected String getEvalStatus(Evaluation eval, User assessor, boolean isInstructor){
		List<EvaluationUser> evalUserList = eval.getEvalUsersByAssessor(assessor);
		if(evalUserList.size()==0){
			String extraInfo = "";
			Date today = Util.getTodayWithoutTime();
			if(eval.getEdate()!=null && eval.getEdate().before(today)){
				extraInfo = " (Submission Closed)";
			}
			
			return "Not Attempted" + extraInfo;
			
			//return messages.get("Not-Attempted");
		}
		
		boolean hasEdited = false;
		for(EvaluationUser eu : evalUserList){
			if(eu.getSelectedCriterionsByLeader().size()>0 
					|| eu.getEditedCmtStrength()!=null || eu.getEditedCmtWeakness()!=null
					|| eu.getEditedCmtOther()!=null){
				hasEdited = true;
				break;
			}
		}
		if(hasEdited){
			if(isInstructor)
				return "Edited by Instructor";
			else
				return "Submission Closed";
		}

		
		boolean hasNotSubmitted = false;
		for(EvaluationUser eu : evalUserList){
			if(!eu.isSubmited()){
				hasNotSubmitted = true;
				break;
			}
		}
		if(hasNotSubmitted)
			return "Not Submit Yet"; 
		
		return "Submitted";
	}
	
	public String getGroupTypeName(Group group, User user){
		if(group ==null)
			return "";
		GroupUser gu = getGroupUser(group, user);
		if(gu!=null)
			return gu.getGroupNumNameDisplay();
		else
			return "";
	}
	public GroupUser getGroupUser(Group group, User user){
		if(group ==null)
			return null;
		
		for(int i=0 ; i<group.getGroupUsers().size(); i++){
			GroupUser gu = group.getGroupUsers().get(i);
			if(gu.getUsers().contains(user)){
				return gu;
			}
		}
		return null;
	}
	//******************************
	
	
	@CommitAfter
	private void doLogin(Request request, Response response, User user){
		logger.debug("doLogin for user="+ user.getUsername());
		
		String ssid = requestGlobal.getHTTPServletRequest().getSession(true).getId();
		
		WebSessionData wsData = webSessionDAO.getWebSessionById(ssid);
		if(wsData==null){
			wsData = new WebSessionData();
			wsData.setId(ssid);
		}
		wsData.setUsername(user.getUsername());
		wsData.setLoginTime(new Date());
		
		wsData.setIp(requestGlobal.getHTTPServletRequest().getRemoteAddr());
		
		wsData.setLastActiveTime(new Date());
		requestGlobal.getHTTPServletRequest().getSession(true).setAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME, ssid);
		webSessionDAO.immediateSaveWebSessionData(wsData);
		webSessionDAO.setWebSessionToCookies(wsData, requestGlobal.getHTTPServletRequest());
		logger.debug("successful login user="+ user.getUsername());
	}
	
	private void authenRequest(Request request, Response response, String functionName,
			String username, Long timestamp, String hash, String... param) throws IOException{
		
		String params = "";
		for(String p : param){
			params += p;
		}
		logger.debug(".....bbsvc authenticate request - function="+ functionName+", username="+username
				+", time="+timestamp+ ", hash="+hash+", params="+params);
		
		/*Date now = new Date();
		if((new Date(timestamp + Config.getLong(Config.BB_TIMEOUT_LIMIT))).before(now)){
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The Link has expired");
			return ;
        }
*/		
		String temp = functionName.toLowerCase() + username + timestamp;
		for(String para : param){
			temp += para;
		}
		String h = Util.generateHashValue(temp , Config.getString(Config.BB_HASH_KEY));
		if(!h.equals(hash)){
        	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
        }
	}
	
	

	
}
//we use this encoder to over come bug of RESTfulWSDispatcher, which does not allow parameter to contain "/" or "%2f"
class CustomURLEncoder {
	public static String encode(String urlStr) {
		if(urlStr==null)
			return null;
		try {
			urlStr = URLEncoder.encode(urlStr, "UTF-8");
			urlStr = urlStr.replace("%", "$");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return urlStr;
	}
	public static String decode(String urlStr) {
		if(urlStr==null)
			return null;
		try {
			urlStr = urlStr.replace("$", "%");
			urlStr = URLDecoder.decode(urlStr, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return urlStr;
	}
}