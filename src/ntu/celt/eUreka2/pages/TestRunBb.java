package ntu.celt.eUreka2.pages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.annotation.PublicPage;
import ntu.celt.eUreka2.internal.Util;

import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;

@PublicPage
public class TestRunBb {

	private static final String BB_KEY = "CELT18BEK972535K0081FECBB";
	private static String EUREKA_URL = "http://127.0.0.1:8080/webapp"; //"http://127.0.0.1:8080/webapp"; //"https://eureka.ntu.edu.sg/webapp" //"http://172.21.253.70:8080/webapp";
	private static String EUREKA_INTEGRATE_URL = "bbsvc";
	
	@Inject 
	private Response response;
	@Persist("flash")
	@Property
	private String msg;
	@Persist("flash")
	@Property
	private String logMsg;
	
	@Inject
	private Logger logger;
	
	@Persist
	@Property
	private List<String> pIdList; 
	
	
	private String callBackPage = EUREKA_URL; //default to Index page
	private String username = "jsomsak";//"intustaff5"; //"jsomsak"; //"intustaff1"; //"intustaff1"; //"student1";
	private String projId = "ADH-CELT-15-0004";//"ADH-CITS-15-0010"; // //"ADH-CITS-16-0003";
	private String courseId = "CELT-SOMSAK-TEST-11S1"; 
	private String groupId = "groupId123";
	private String studentID = "student1";
	
	@Property
    private UploadedFile file1;
	@Property
    private String inputText;
	
	
	
	void onActionFromUserLogin() throws IOException{
		String redirectLink = CustomURLEncoder.encode(callBackPage); 
		String url = generateServiceURL("isUserExist", username);
		
		String returnText = this.sendURLRequest(url, null, null);  //send without content
		if(returnText == null){
			logger.info("No acknowgement from "+ url); 
		}
		else if(returnText.equalsIgnoreCase("true")){  //user exists
			url = generateServiceURL("userLogin", username, redirectLink);
			response.sendRedirect(url);
		}
		else if(returnText.equalsIgnoreCase("false")){  //user not exist
			url = generateServiceURL("createUser", username);
			
			JSONObject json = new JSONObject();
			json.put("fName", "TestFirstName")
				.put("lName", "testLastName")
				.put("email", "jsomsak@ntu.edu.sg")
				.put("extKey", "matricNumber")  
				.put("username", username)
				.put("school", "SCE")
				.put("sysRole", "STUDENT")
				;
			//	if possible to obtain
			//	u.setMphone(mphone);
			//	u.setOrganization(organization);
			//	u.setPhone(phone);
			//	u.setRemarks(remarks);
			//	u.setTitle("MR");
				
			String returnText2 = sendURLRequest(url, "application/json", json.toString());
			if(returnText2 == null){
				logger.info("No acknowgement from "+ url); 
			}
			else if(returnText2.equalsIgnoreCase("true")){
				url = generateServiceURL("userLogin", username, redirectLink);
				response.sendRedirect(url);
			}
		}
	}
	void onActionFromGetProject() throws IOException{
	//	String projId = "ADH-OTHERS-15-0001"; 
		String url = generateServiceURL("getProject", username, projId);
		//msg = url;
		sendWithoutJSONAndOutput(url);		
	}
	void onActionFromUpdateSysRoleOfUser() throws IOException{
		String url = generateServiceURL("updateSysRoleOfUser", "student1", "User");
		sendWithoutJSONAndOutput(url);		
	}
	
	void onActionFromAddSysRoleOfUser() throws IOException{
		String url = generateServiceURL("addSysRoleOfUser", "student1", "Student", null);
		sendWithoutJSONAndOutput(url);		
	}
	
	void onActionFromRemoveSysRoleOfUser() throws IOException{
		String url = generateServiceURL("removeSysRoleOfUser", "student1", "Student");
		sendWithoutJSONAndOutput(url);		
	}
	
	
	
	
	
	void onActionFromGetProjectsByUser() throws IOException{
		String url = generateServiceURL("getProjectsByUser", username);
		sendWithoutJSONAndOutput(url);		
	}
	
	@CommitAfter
	void onSuccessFromUploadForm() throws IOException{
		//username = "jsomsak";    //creator
//		projId = "aaa";
		
		if(file1!=null){
			String url = generateServiceURL("UploadProjectAttachment", username, projId, file1.getFileName());
			byte[] bytes = IOUtils.toByteArray(file1.getStream());
		//	String s = new String(bytes, "UTF-8");

		//	logger.error(s+"......."+ s.getBytes("UTF-8"))	;
			
			String returnText = sendURLRequestBytesContent(url, file1.getContentType(), bytes);
			if(returnText == null){
				logger.info("No acknowgement from "+ url); 
			}
			else { //returnText is projectID of the newly created
				msg = returnText;
			}
			
		}
		

		
	}
	
	void onActionFromCreateProject() throws IOException{
		//username = "instructor1";    //creator
		
		String url = generateServiceURL("createProject", username);
		
		JSONObject json = new JSONObject();
		json.put("courseId", "CELT-SOMSAK-TEST-15S1")
//			.put("groupId", "groupId123")
//			.put("seqNo", "001")  // May consider delete this, just use projectID is enough
			.put("name", "Test Project Name")
			.put("projType", "ADH")
			.put("description", "Test Project description blablabla... ")
			.put("sdate", (new Date()).getTime() )
			.put("edate", (new Date()).getTime() )
			;
		JSONArray jStudents = new JSONArray();
		for(int i=1; i<5; i++){
			jStudents.put("student"+i);  //student's username
		}
		json.put("students", jStudents);
		
		//note: creator will be enroll as 'project leader'
		
			
		String returnText = sendURLRequest(url, "application/json", json.toString());
		if(returnText == null){
			logger.info("No acknowgement from "+ url); 
		}
		else { //returnText is projectID of the newly created
			msg = returnText;
			
			//then redirect creator/instructor to 'project list page' or BuildingBlock index page
		}
	}
	
	
	void onActionFromGetProjectsByCourseIdGroupId() throws IOException {
		//username = "student1";
		
		//find groupId of the student (curUser)
		
		//assume 
		//String courseId = "CELT-SOMSAK-TEST-11S1";
		
		String url = generateServiceURL("getProjectsByCourseIdGroupId", username, courseId, groupId);
		logger.debug("url="+url);	
		String returnText = this.sendURLRequest(url, null, null);  //send without content
		if(returnText == null){
			logger.info("No acknowgement from "+ url); 
		}
		else {
			JSONArray jsonArr = new JSONArray(returnText);
			
			pIdList = new ArrayList<String>();
			
			for(int i=0; i<jsonArr.length(); i++){
				JSONObject jproj = jsonArr.getJSONObject(i);
				
			/*	jproj.getString("id");
				jproj.getString("name");
				jproj.getString("description");
				jproj.getString("courseId");
				jproj.getString("groupId");
				Util.longObjToDate(jproj.get("cdate"));
				...
			
				msg += "<a href='" 
					+"'>Launch project: "+jproj.getString("name")+"</a>";
			*/
				
				pIdList.add(jproj.getString("id"));
			}
			
		}
		
	}
	
	void onActionFromGetProjectsByCourseId() throws IOException {
		//username = "student1";
		
		//assume 
		//String courseId = "CELT-SOMSAK-TEST-11S1";
		
		String url = generateServiceURL("getProjectsByCourseId", username, courseId);
		logger.debug("url="+url);	
		String returnText = this.sendURLRequest(url, null, null);  //send without content
		if(returnText == null){
			logger.info("No acknowgement from "+ url); 
		}
		else {
			JSONArray jsonArr = new JSONArray(returnText);
			
			pIdList = new ArrayList<String>();
			
			for(int i=0; i<jsonArr.length(); i++){
				JSONObject jproj = jsonArr.getJSONObject(i);
				
			/*	jproj.getString("id");
				jproj.getString("name");
				jproj.getString("description");
				jproj.getString("courseId");
				jproj.getString("groupId");
				Util.longObjToDate(jproj.get("cdate"));
				...
			
				msg += "<a href='" 
					+"'>Launch project: "+jproj.getString("name")+"</a>";
			*/
				
				pIdList.add(jproj.getString("id"));
			}
			
		}
		
	}
	
	void onActionFromUserLoginProj(String projId) throws IOException{
		String url = generateServiceURL("userLoginProj", username, projId);
		response.sendRedirect(url);
	}
	
	
	void onActionFromGetGradesByCourseId() throws IOException{
		//String courseId = "13S1-AB0601-SEM-17";  
		//username = "jsomsak";
		String url = generateServiceURL("getGradesByCourseId", username, courseId);
		sendWithoutJSONAndOutput(url);		
	}
	
	void onActionFromGetSystemRolesByUsername() throws IOException{
		//username = "jsomsak";
		
		String url = generateServiceURL("getSystemRolesByUsername", username);
		sendWithoutJSONAndOutput(url);		
	}

	void onActionFromGetProjectRoleByUsernameByProjectId() throws IOException{
		//String projID = "ADH-OTHERS-15-0001";  
		//username = "jsomsak";
		
		String url = generateServiceURL("getProjectRoleByUsernameByProjectId", username, projId);
		sendWithoutJSONAndOutput(url);		
	}

	
	void onActionFromGetActiveAnnouncementsByProjId() throws IOException{
		//String projId = "COU-NBS-13-0613"; 
		String url = generateServiceURL("getActiveAnnouncementsByProjId", username, projId);
		sendWithoutJSONAndOutput(url);		
	}
	
	void onActionFromGetActiveAnnouncementsByCourseId() throws IOException{
		String url = generateServiceURL("getActiveAnnouncementsByCourseId", username, courseId);
		sendWithoutJSONAndOutput(url);		
	}
	
	
	void onActionFromGetAllGroups() throws IOException{
		String url = generateServiceURL("getAllGroups", username);
		sendWithoutJSONAndOutput(url);		
	}
	
	void onActionFromGetAllSchools() throws IOException{
		String url = generateServiceURL("getAllSchools", username);
		sendWithoutJSONAndOutput(url);		
	}
	
	void onActionFromGetGroupById() throws IOException{
		//String groupId = "3429167331380736000"; 
		String url = generateServiceURL("getGroupById", username, groupId);
		sendWithoutJSONAndOutput(url);		
	}
	
	void onActionFromCreateGroup() throws IOException{
		//username = "jsomsak";    //creator
		
		String url = generateServiceURL("createGroup", username);
		
		JSONObject json = new JSONObject();
		json.put("groupName", "test BB API group3")
			.put("projID", projId)
			.put("allowSelfEnroll", "false")  
			.put("maxPerGroup", 0)
			;
		JSONArray jSubgroup = new JSONArray();
		for(int j=1; j<3; j++){
			JSONObject j1 = new JSONObject();
			j1.put("groupNum", j);
			j1.put("groupNumName", "sub name "+j);
			JSONArray jStudents = new JSONArray();
			for(int i=1; i<5; i++){
				jStudents.put("student"+i);  //student's username
			}
			j1.put("groupMembers", jStudents);
			
			jSubgroup.put(j1);
		}
		
		json.put("subgroups", jSubgroup);
		
		
			
		String returnText = sendURLRequest(url, "application/json", json.toString());
		if(returnText == null){
			logger.info("No acknowgement from "+ url); 
		}
		else { //returnText is projectID of the newly created
			msg = returnText;
			
			//then redirect creator/instructor to 'project list page' or BuildingBlock index page
		}
	}
	
	void onActionFromAddUserToGroup() throws IOException{
		//username = "jsomsak";    //creator
		
		String url = generateServiceURL("addUserToGroup", username);
		
		JSONObject json = new JSONObject();
		json.put("groupID", groupId)//"1486659709891492864")
			.put("subgroupNum", "1")
			.put("usernameToAdd", username )//"jsomsak")  
			
			;
		
			
		String returnText = sendURLRequest(url, "application/json", json.toString());
		if(returnText == null){
			logger.info("No acknowgement from "+ url); 
		}
		else { //returnText is projectID of the newly created
			msg = returnText;
			
			//then redirect creator/instructor to 'project list page' or BuildingBlock index page
		}
	}
	void onActionFromRemoveUserFromGroup() throws IOException{
		//username = "jsomsak";    //creator
		
		String url = generateServiceURL("removeUserFromGroup", username);
		
		JSONObject json = new JSONObject();
		json.put("groupID", groupId)//"1486659709891492864")
			.put("subgroupNum", "1")
			.put("usernameToRemove", studentID)//"student1")  
			;
		
			
		String returnText = sendURLRequest(url, "application/json", json.toString());
		if(returnText == null){
			logger.info("No acknowgement from "+ url); 
		}
		else { //returnText is projectID of the newly created
			msg = returnText;
			
			//then redirect creator/instructor to 'project list page' or BuildingBlock index page
		}
	}
	
	
	
	void onActionFromGetSchoolRubricsByUser() throws IOException{
		String url = generateServiceURL("getSchoolRubricsByUser", username);
		sendWithoutJSONAndOutput(url);		
	}
	void onActionFromGetRubricsByUser() throws IOException{
		String url = generateServiceURL("getRubricsByUser", username);
		sendWithoutJSONAndOutput(url);		
	}
	void onActionFromSearchSharedRubrics() throws IOException{
		String url = generateServiceURL("searchSharedRubrics", username, "test");
		sendWithoutJSONAndOutput(url);		
	}
	void onActionFromGetRubricsByID() throws IOException{
		String url = generateServiceURL("getRubricsByID", username, "43909120");
		sendWithoutJSONAndOutput(url);		
	}
	void onActionFromGetAssessmentsByProjectId() throws IOException{
		String url = generateServiceURL("getAssessmentsByProjectId", username, projId);
		sendWithoutJSONAndOutput(url);		
	}
	void onActionFromGetPeerEvaluationsByProjectId() throws IOException{
		String url = generateServiceURL("getPeerEvaluationsByProjectId", username, projId);
		sendWithoutJSONAndOutput(url);		
	}
	void onActionFromGetAssessmentMarksByProjectIdByStudentUsername() throws IOException{
		String url = generateServiceURL("getAssessmentMarksByProjectIdByStudentUsername", username, projId, studentID);
		sendWithoutJSONAndOutput(url);		
	}
	void onActionFromGetPeerEvaluationMarksByProjectIdByStudentUsername() throws IOException{
		String url = generateServiceURL("getPeerEvaluationMarksByProjectIdByStudentUsername", username, projId, studentID);
		sendWithoutJSONAndOutput(url);		
	}
	
	
	
	void onActionFromSyncGroups() throws IOException{
		String url = generateServiceURL("syncGroups", username);
		
		String txt = "{  \"courseId\": \" _1627_1_f58bb260e80bc89a1d52b514030b9d2ae38a495a\",  \"courseName\": \"UAT-Tester\",  \"projID\": \"COU-OTHERS-17-0001\", \"allowChangeGroupWhenBeingUsed\" : true,  \"students\": [    {       \"userName\":\"intustudent1\",      \"firstName\":\"intustudent1\",      \"lastName\":\"student1\",      \"email\": \"instudent1@ntu.ac.sg\",      \"role\": \"User\",      \"school\": \"SCS\"    },    {       \"userName\":\"intustudent2\",      \"firstName\":\"intustudent2\",      \"lastName\":\"student2\",      \"email\": \"instudent2@ntu.ac.sg\",      \"role\": \"User\",      \"school\": \"SCS\"    },    {       \"userName\":\"intustudent3\",      \"firstName\":\"intustudent3\",      \"lastName\":\"student3\",      \"email\": \"instudent3@ntu.ac.sg\",      \"role\": \"User\",      \"school\": \"SCS\"    },    {       \"userName\":\"intustudent4\",      \"firstName\":\"intustudent4\",      \"lastName\":\"student4\",      \"email\": \"instudent4@ntu.ac.sg\",      \"role\": \"User\",      \"school\": \"SCS\"    },    {       \"userName\":\"intustudent5\",      \"firstName\":\"intustudent5\",      \"lastName\":\"student5\",      \"email\": \"instudent5@ntu.ac.sg\",      \"role\": \"User\",      \"school\": \"SCS\"    },    {       \"userName\":\"intustudent6\",      \"firstName\":\"intustudent6\",      \"lastName\":\"student6\",      \"email\": \"instudent6@ntu.ac.sg\",      \"role\": \"User\",      \"school\": \"SCS\"    },    {       \"userName\":\"intustudent7\",      \"firstName\":\"intustudent7\",      \"lastName\":\"student7\",      \"email\": \"instudent7@ntu.ac.sg\",      \"role\": \"User\",      \"school\": \"SCS\"    },    {       \"userName\":\"intustudent8\",      \"firstName\":\"intustudent8\",      \"lastName\":\"student8\",      \"email\": \"instudent8@ntu.ac.sg\",      \"role\": \"User\",      \"school\": \"SCS\"    },    {       \"userName\":\"intustudent9\",      \"firstName\":\"intustudent9\",      \"lastName\":\"student9\",      \"email\": \"instudent9@ntu.ac.sg\",      \"role\": \"User\",      \"school\": \"SCS\"    },    {       \"userName\":\"intustudent10\",      \"firstName\":\"intustudent10\",      \"lastName\":\"student10\",      \"email\": \"instudent10@ntu.ac.sg\",      \"role\": \"User\",      \"school\": \"SCS\"    }  ],  \"instructors\": [    {       \"userName\":\"intustaff1\",      \"firstName\":\"intustaff1\",      \"lastName\":\"staff1\",      \"email\": \"inustaff1@ntu.ac.sg\",      \"role\": \" Faculty Staff\",      \"school\": \"SCS\"    },    {       \"userName\":\"intustaff2\",      \"firstName\":\"intustaff2\",      \"lastName\":\"staff2\",      \"email\": \"inustaff2@ntu.ac.sg\",      \"role\": \" Faculty Staff\",      \"school\": \"SCS\"    },    {       \"userName\":\"intustaff3\",      \"firstName\":\"intustaff3\",      \"lastName\":\"staff3\",      \"email\": \"inustaff3@ntu.ac.sg\",      \"role\": \" Faculty Staff\",      \"school\": \"SCS\"    },    {       \"userName\":\"intustaff4\",      \"firstName\":\"intustaff4\",      \"lastName\":\"staff4\",      \"email\": \"inustaff4@ntu.ac.sg\",      \"role\": \" Faculty Staff\",      \"school\": \"SCS\"    },    {       \"userName\":\"intustaff5\",      \"firstName\":\"intustaff5\",      \"lastName\":\"staff5\",      \"email\": \"inustaff5@ntu.ac.sg\",      \"role\": \" Faculty Staff\",      \"school\": \"SCS\"    }  ],  " +
				"\"groupsets\": [    {      \"bbGroupID\": \"123456789\",	  \"groupsetName\": \"GroupA\", 	  \"groups\": [    {		  \"bbsubGroupID\": \"123120-1\",      \"groupName\": \"GroupA 1\",      \"groupMembers\": [        \"intustudent4\"      ]    }, {		  \"bbsubGroupID\": \"123120-2221\",      \"groupName\": \"GroupA 3\",      \"groupMembers\": [        \"intustudent1\", \"intustudent2\" , \"intustudent5\",           \"intustudent7\"     ]    },    {	  \"bbsubGroupID\": \"123124-2\",      \"groupName\": \"GroupA 2\",      \"groupMembers\": [               \"intustudent8\"      ]    }	]	},	{      \"bbGroupID\": \"123456999\",	  \"groupsetName\": \"GroupX\",   \"groups\": [    {	  \"bbsubGroupID\": \"123121-1\",      \"groupName\": \"GroupX 1\",      \"groupMembers\": [        \"intustudent2\"      ]    },    {	  \"bbsubGroupID\": \"123121-2\",      \"groupName\": \"GroupX 2\",      \"groupMembers\": [        \"intustudent3\",        \"intustudent4\"      ]    }  ]  }  ]} ";
		String returnText = sendURLRequest(url, "application/json", txt);
		if(returnText == null){
			logger.info("No acknowgement from "+ url); 
		}
		else { 
			//{"result", true, "resultMsg", "successfully synced"}
			//{"result", false, "resultMsg", "Project not created yet"}
			msg = returnText;
		}
	}
	
	void onActionFromDeleteGroup() throws IOException{
		
		String url = generateServiceURL("deleteGroup", username, "222");
		sendWithoutJSONAndOutput(url);	
	}
		void onActionFromGetPeerEvaluationsByProjectIdByStudentUsername() throws IOException{
		
		String url = generateServiceURL("getPeerEvaluationsByProjectIdByStudentUsername", username, projId, "student4");
		sendWithoutJSONAndOutput(url);	
	}
	
	
	
	
	
	
	
	private void sendWithoutJSONAndOutput(String url) throws IOException{
		String returnText = this.sendURLRequest(url, null, null);  //send without content
		
		if(returnText == null){
			logger.info("No acknowgement from "+ url); 
		}
		msg = returnText;
		
	}
	
	
	
	
	
	//note: below methods will be put in Util class:
	
	private String generateServiceURL(String functionToCall, String userId, String... param) {
		Date time = new Date();
		String temp = functionToCall.toLowerCase() + userId + time.getTime();
		for(String para : param){
			temp += para;
		}
		String hash = Util.generateHashValue(temp, BB_KEY);
		String url = EUREKA_URL + "/" + EUREKA_INTEGRATE_URL+"/"+functionToCall.toLowerCase()+"/"+userId+"/"+time.getTime()+"/"+hash;	
		for(String p : param){
			url += "/"+ CustomURLEncoder.encode(p);
		}
		return url;
	}
	private String sendURLRequest(String url, String contentType, String content) throws IOException{
		logMsg = "sendURLRequest="+url;
		logger.debug("sendURLRequest="+url);
		
		HttpURLConnection httpConn = (HttpURLConnection) (new URL(url)).openConnection();
		if(content!=null){
			byte[] b = content.getBytes("UTF-8");
			httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
			httpConn.setRequestProperty("Content-Type", contentType);
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			//start Sending the the request
		    OutputStream os = httpConn.getOutputStream();
		    os.write(b);
		    os.flush();
		    
		}
		
		InputStream is = httpConn.getInputStream();

		BufferedReader bReader = new BufferedReader(new InputStreamReader(is));
		String returnText = "";
		String str;
		while((str = bReader.readLine()) !=null){
			returnText += str;
		}
		
		httpConn.disconnect();
		bReader.close();
		return returnText;
	}
	
	private String sendURLRequestBytesContent(String url, String contentType, byte[] bytes) throws IOException{
		logMsg = "sendURLRequest="+url;
		logger.debug("sendURLRequest="+url);
		
		HttpURLConnection httpConn = (HttpURLConnection) (new URL(url)).openConnection();
		if(bytes!=null){
//			httpConn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
//			httpConn.setRequestProperty("Content-Type", contentType);
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			//start Sending the the request
		    OutputStream os = httpConn.getOutputStream();
		    os.write(bytes);
		    os.flush();
		    
		}
		
		InputStream is = httpConn.getInputStream();

		BufferedReader bReader = new BufferedReader(new InputStreamReader(is));
		String returnText = "";
		String str;
		while((str = bReader.readLine()) !=null){
			returnText += str;
		}
		
		httpConn.disconnect();
		bReader.close();
		return returnText;
	}
	
}










//we use this encoder to over come bug of RESTfulWSDispatcher, which does not allow parameter to contain "/" or "%2f"
class CustomURLEncoder {
	public static String encode(String urlStr) {
		if(urlStr==null)
			return null;
		try {
			urlStr = URLEncoder.encode(urlStr, "UTF-8");
		//	urlStr = urlStr.replace("%", "$");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return urlStr;
	}
	public static String decode(String urlStr) {
		if(urlStr==null)
			return null;
		try {
	//		urlStr = urlStr.replace("$", "%");
			urlStr = URLDecoder.decode(urlStr, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return urlStr;
	}
}
