/***************************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ***********************************************************************/
/* $$Id$$ */
package org.lamsfoundation.lams.tool.mc.web;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.lamsfoundation.lams.notebook.model.NotebookEntry;
import org.lamsfoundation.lams.notebook.service.CoreNotebookConstants;
import org.lamsfoundation.lams.tool.mc.McAppConstants;
import org.lamsfoundation.lams.tool.mc.McApplicationException;
import org.lamsfoundation.lams.tool.mc.McComparator;
import org.lamsfoundation.lams.tool.mc.McGeneralLearnerFlowDTO;
import org.lamsfoundation.lams.tool.mc.McLearnerStarterDTO;
import org.lamsfoundation.lams.tool.mc.McUtils;
import org.lamsfoundation.lams.tool.mc.pojos.McContent;
import org.lamsfoundation.lams.tool.mc.pojos.McQueUsr;
import org.lamsfoundation.lams.tool.mc.pojos.McSession;
import org.lamsfoundation.lams.tool.mc.service.IMcService;
import org.lamsfoundation.lams.tool.mc.service.McServiceProxy;
import org.lamsfoundation.lams.usermanagement.dto.UserDTO;
import org.lamsfoundation.lams.web.session.SessionManager;
import org.lamsfoundation.lams.web.util.AttributeNames;
import org.lamsfoundation.lams.web.util.SessionMap;


/**
 * 
 * @author Ozgur Demirtas
 *
 * <lams base path>/<tool's learner url>&userId=<learners user id>&toolSessionId=123&mode=teacher

 * Tool Session:
 *
 * A tool session is the concept by which which the tool and the LAMS core manage a set of learners interacting with the tool. 
 * The tool session id (toolSessionId) is generated by the LAMS core and given to the tool.
 * A tool session represents the use of a tool for a particulate activity for a group of learners. 
 * So if an activity is ungrouped, then one tool session exist for for a tool activity in a learning design.
 *
 * More details on the tool session id are covered under monitoring.
 * When thinking about the tool content id and the tool session id, it might be helpful to think about the tool content id 
 * relating to the definition of an activity, whereas the tool session id relates to the runtime participation in the activity.

 *  
 * Learner URL:
 * The learner url display the screen(s) that the learner uses to participate in the activity. 
 * When the learner accessed this user, it will have a tool access mode ToolAccessMode.LEARNER.
 *
 * It is the responsibility of the tool to record the progress of the user. 
 * If the tool is a multistage tool, for example asking a series of questions, the tool must keep track of what the learner has already done. 
 * If the user logs out and comes back to the tool later, then the tool should resume from where the learner stopped.
 * When the user is completed with tool, then the tool notifies the progress engine by calling 
 * org.lamsfoundation.lams.learning.service.completeToolSession(Long toolSessionId, User learner).
 *
 * If the tool's content DefineLater flag is set to true, then the learner should see a "Please wait for the teacher to define this part...." 
 * style message.
 * If the tool's content RunOffline flag is set to true, then the learner should see a "This activity is not being done on the computer. 
 * Please see your instructor for details."
 *
 * ?? Would it be better to define a run offline message in the tool? We have instructions for the teacher but not the learner. ??
 * If the tool has a LockOnFinish flag, then the tool should lock learner's entries once they have completed the activity. 
 * If they return to the activity (e.g. via the progress bar) then the entries should be read only.
 * 
   <!--Learning Starter Action: initializes the Learning module -->
   <action 	path="/learningStarter" 
   			type="org.lamsfoundation.lams.tool.mc.web.McLearningStarterAction" 
   			name="McLearningForm" 
	      	scope="request"
	      	validate="false"
	      	unknown="false"
   			input="/learningIndex.jsp"> 

	  	<forward
		    name="loadLearner"
		    path="/learning/AnswersContent.jsp"
		    redirect="false"
	  	/>

	  	<forward
		    name="viewAnswers"
		    path="/learning/ViewAnswers.jsp"
		    redirect="false"
	  	/>

	  	<forward
		    name="redoQuestions"
		    path="/learning/RedoQuestions.jsp"
		    redirect="false"
	  	/>
	  	
	     <forward
	        name="preview"
	        path="/learning/Preview.jsp"
		    redirect="false"
	     />

	  	<forward
		    name="learningStarter"
		    path="/learningIndex.jsp"
		    redirect="false"
	  	/>
	  	
	  	<forward
		    name="defineLater"
	        path="/learning/defineLater.jsp"
		    redirect="false"
	  	/>

	  	<forward
		    name="runOffline"
	        path="/learning/RunOffline.jsp"
		    redirect="false"
	  	/>

	  	<forward
		    name="errorList"
		    path="/McErrorBox.jsp"
		    redirect="false"
	  	/>
	</action>  
 *
 */

/**
 *
 * Note:  Because of MCQ's learning reporting structure, Show Learner Report is always ON even if in authoring it is set to false.
 */

public class McLearningStarterAction extends Action implements McAppConstants {
	static Logger logger = Logger.getLogger(McLearningStarterAction.class.getName());

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) 
  								throws IOException, ServletException, McApplicationException {
		
	    /*
	     * By now, the passed tool session id MUST exist in the db through the calling of:
	     * public void createToolSession(Long toolSessionId, Long toolContentId) by the container.
	     * 
	     * make sure this session exists in tool's session table by now.
	     */
		
		McUtils.cleanUpSessionAbsolute(request);
		
		Map mapQuestionsContent= new TreeMap(new McComparator());
		Map mapAnswers= new TreeMap(new McComparator());

		IMcService mcService = McServiceProxy.getMcService(getServlet().getServletContext());
	    logger.debug("retrieving mcService from proxy: " + mcService);

		McLearningForm mcLearningForm = (McLearningForm) form;
		mcLearningForm.setMcService(mcService);
    	mcLearningForm.setPassMarkApplicable(new Boolean(false).toString());
		mcLearningForm.setUserOverPassMark(new Boolean(false).toString());
		

		ActionForward validateParameters=validateParameters(request, mcLearningForm, mapping);
	    logger.debug("validateParameters: " + validateParameters);
	    if (validateParameters != null)
	    {
	    	return validateParameters;
	    }
	    
	    SessionMap sessionMap = new SessionMap();
	    List sequentialCheckedCa= new LinkedList();
	    sessionMap.put(QUESTION_AND_CANDIDATE_ANSWERS_KEY, sequentialCheckedCa);
	    request.getSession().setAttribute(sessionMap.getSessionID(), sessionMap);
	    mcLearningForm.setHttpSessionID(sessionMap.getSessionID());
	        
	    String toolSessionID=request.getParameter(AttributeNames.PARAM_TOOL_SESSION_ID);
	    logger.debug("retrieved toolSessionID: " + toolSessionID);
	    mcLearningForm.setToolSessionID(new Long(toolSessionID).toString());
	    
	    
		/*
		 * by now, we made sure that the passed tool session id exists in the db as a new record
		 * Make sure we can retrieve it and the relavent content
		 */
		
		McSession mcSession=mcService.retrieveMcSession(new Long(toolSessionID));
	    logger.debug("retrieving mcSession: " + mcSession);
	    
	    if (mcSession == null)
	    {
	    	McUtils.cleanUpSessionAbsolute(request);
	    	logger.debug("error: The tool expects mcSession.");
			return (mapping.findForward(ERROR_LIST));
	    }

	    /*
	     * find out what content this tool session is referring to
	     * get the content for this tool session 
	     * Each passed tool session id points to a particular content. Many to one mapping.
	     */
		McContent mcContent=mcSession.getMcContent();
	    logger.debug("using mcContent: " + mcContent);
	    
	    if (mcContent == null)
	    {
	    	McUtils.cleanUpSessionAbsolute(request);
	    	logger.debug("error: The tool expects mcContent.");
	    	persistError(request,"error.content.doesNotExist");
	    	return (mapping.findForward(ERROR_LIST));
	    }

	    
	    /*
	     * The content we retrieved above must have been created before in Authoring time. 
	     * And the passed tool session id already refers to it.
	     */
	    
	    McLearnerStarterDTO mcLearnerStarterDTO= new McLearnerStarterDTO();
	    logger.debug("IS_QUESTIONS_SEQUENCED: " + mcContent.isQuestionsSequenced());
	    if (mcContent.isQuestionsSequenced())
		{
			mcLearnerStarterDTO.setQuestionListingMode(QUESTION_LISTING_MODE_SEQUENTIAL);
			mcLearningForm.setQuestionListingMode(QUESTION_LISTING_MODE_SEQUENTIAL);
		}
	    else
	    {
	    	mcLearnerStarterDTO.setQuestionListingMode(QUESTION_LISTING_MODE_COMBINED);
	    	mcLearningForm.setQuestionListingMode(QUESTION_LISTING_MODE_COMBINED);
	    }
	    
	    
	    /*
	     * Is the tool activity been checked as Run Offline in the property inspector?
	     */
	    logger.debug("IS_TOOL_ACTIVITY_OFFLINE: " + mcContent.isRunOffline());
	    mcLearnerStarterDTO.setToolActivityOffline(new Boolean(mcContent.isRunOffline()).toString());
	    mcLearnerStarterDTO.setActivityTitle(mcContent.getTitle());
	    request.setAttribute(MC_LEARNER_STARTER_DTO, mcLearnerStarterDTO);
	    
	    mcLearningForm.setToolContentID(mcContent.getMcContentId().toString());
	    commonContentSetup(request, mcContent, mcService,mcLearningForm, toolSessionID);
	    
	    
    	/* Is the request for a preview by the author?
    	Preview The tool must be able to show the specified content as if it was running in a lesson. 
		It will be the learner url with tool access mode set to ToolAccessMode.AUTHOR 
		3 modes are:
			author
			teacher
			learner
		*/
       
	    /*handle PREVIEW mode*/
	    //String mode=mcLearningForm.getLearningMode();
	    String mode=request.getParameter(MODE);
	    logger.debug("mode: " + mode);
	    
    	if ((mode != null) && (mode.equals("author")))
    	{
    		logger.debug("Author requests for a preview of the content.");
    	}
	    
    	/* by now, we know that the mode is either teacher or learner
    	 * check if the mode is teacher and request is for Learner Progress
    	 */
		String userId=request.getParameter(USER_ID);
		logger.debug("userId: " + userId);
		
		
		if ((userId != null) && (mode.equals("teacher")))
		{
			logger.debug("request is for learner progress");
	    	
			/* LEARNER_PROGRESS for jsp*/
			mcLearningForm.setLearnerProgress(new Boolean(true).toString());
			mcLearningForm.setLearnerProgressUserId(userId);
			
			McLearningAction mcLearningAction= new McLearningAction();
			/* pay attention that this userId is the learner's userId passed by the request parameter.
			 * It is differerent than USER_ID kept in the session of the current system user*/
		    
		    McQueUsr mcQueUsr=mcService.getMcUserBySession(new Long(userId), mcSession.getUid());
		    logger.debug("mcQueUsr: " + mcQueUsr);
		    if (mcQueUsr == null)
		    {
				logger.error("error.learner.required");
		    }
		    
		    /* check whether the user's session really referrs to the session id passed to the url*/
		    Long sessionUid=mcQueUsr.getMcSessionId();
		    logger.debug("sessionUid: " + sessionUid);
		    McSession mcSessionLocal=mcService.getMcSessionByUID(sessionUid);
		    logger.debug("checking mcSessionLocal" + mcSessionLocal);

		    toolSessionID=(String)mcLearningForm.getToolSessionID();
		    logger.debug("toolSessionID: " + toolSessionID + " versus" + mcSessionLocal);
		    
		    if  ((mcSessionLocal ==  null) ||
				 (mcSessionLocal.getMcSessionId().longValue() != new Long(toolSessionID).longValue()))
		    {
		        logger.error("error.learner.sessionId.inconsistent");
		    }
		    LearningUtil.saveFormRequestData(request, mcLearningForm, true);
		    logger.debug("learnerProgress before presenting learner Progress screen: " + mcLearningForm.getLearnerProgress());
		    
		    
		    request.setAttribute(REQUEST_BY_STARTER, new Boolean (true).toString());
		    return mcLearningAction.viewAnswers(mapping, mcLearningForm, request, response);
		}
    	
		/* by now, we know that the mode is learner*/
	    
	    /* find out if the content is set to run offline or online. If it is set to run offline , the learners are informed about that. */
	    boolean isRunOffline=McUtils.isRunOffline(mcContent);
	    logger.debug("isRunOffline: " + isRunOffline);
	    if (isRunOffline == true)
	    {
	        logger.debug("the activity is offline.");
			logger.debug("MC_GENERAL_LEARNER_FLOW_DTO: " +  request.getAttribute(MC_GENERAL_LEARNER_FLOW_DTO));
			
	    	logger.debug("fwding to :" + RUN_OFFLINE);
			return (mapping.findForward(RUN_OFFLINE));
	    }

	    /* find out if the content is being modified at the moment. */
	    boolean isDefineLater=McUtils.isDefineLater(mcContent);
	    logger.debug("isDefineLater: " + isDefineLater);
	    if (isDefineLater == true)
	    {
	    	logger.debug("fwding to :" + DEFINE_LATER);
	    	return (mapping.findForward(DEFINE_LATER));
	    }

    	/*
	     * verify that userId does not already exist in the db.
	     * If it does exist, that means, that user already responded to the content and 
	     * his answers must be displayed  read-only
	     * 
	     */

		String userID = "";
	    HttpSession ss = SessionManager.getSession();
	    logger.debug("ss: " + ss);
	    
	    if (ss != null)
	    {
		    UserDTO user = (UserDTO) ss.getAttribute(AttributeNames.USER);
		    if ((user != null) && (user.getUserID() != null))
		    {
		    	userID = user.getUserID().toString();
			    logger.debug("retrieved userId: " + userID);
		    }
	    }

	    
    	McQueUsr mcQueUsr=mcService.getMcUserBySession(new Long(userID), mcSession.getUid());
	    logger.debug("mcQueUsr:" + mcQueUsr);
	    
	    if (mcQueUsr != null)
	    {
	    	logger.debug("mcQueUsr is available in the db:" + mcQueUsr);
	    	Long queUsrId=mcQueUsr.getUid();
			logger.debug("queUsrId: " + queUsrId);
			
			int highestAttemptOrder=LearningUtil.getHighestAttemptOrder(request, queUsrId, mcService);
			logger.debug("highestAttemptOrder: " + highestAttemptOrder);
			if (highestAttemptOrder == 0)
				highestAttemptOrder=1;
			logger.debug("highestAttemptOrder: " + highestAttemptOrder);
			
			int learnerBestMark=LearningUtil.getHighestMark(request, queUsrId, mcService);
			logger.debug("learnerBestMark: " + learnerBestMark);
	    }
	    else
	    {
	    	logger.debug("mcQueUsr is not available in the db:" + mcQueUsr);
	    }
	    
	    logger.debug("users learning mode is: " + mode);
	    request.setAttribute(MC_LEARNER_STARTER_DTO, mcLearnerStarterDTO);

	    boolean viewSummaryRequested=false;
	    /*if the user's session id AND user id exists in the tool tables go to redo questions.*/
	    if (mcQueUsr != null)
	    {
	        viewSummaryRequested=mcQueUsr.isViewSummaryRequested();
	        logger.debug("viewSummaryRequested: " + viewSummaryRequested);
	        
	        if (viewSummaryRequested)
	        {
		    	Long sessionUid=mcQueUsr.getMcSessionId();
		    	logger.debug("users sessionUid: " + sessionUid);
		    	McSession mcUserSession= mcService.getMcSessionByUID(sessionUid);
		    	logger.debug("mcUserSession: " + mcUserSession);
		    	String userSessionId=mcUserSession.getMcSessionId().toString();
		    	logger.debug("userSessionId: " + userSessionId);
		    	
		    	logger.debug("current toolSessionID: " + toolSessionID);
		    	
		    	if (toolSessionID.equals(userSessionId))
		    	{
		    		logger.debug("the user's session id AND user id exists in the tool tables go to redo questions. " + toolSessionID + " mcQueUsr: " + 
		    				mcQueUsr + " user id: " + mcQueUsr.getQueUsrId());
		    		logger.debug("the learner has already responsed to this content, just generate a read-only report. Use redo questions for this.");
		    		
		    		boolean isRetries=mcContent.isRetries();
		    		logger.debug("isRetries: " + isRetries);
		    		McLearningAction mcLearningAction= new McLearningAction();
			    	logger.debug("present to learner with previous attempts data");
			    	
			    	String sessionStatus=mcUserSession.getSessionStatus(); 
			    	logger.debug("sessionStatus: " +sessionStatus);
			    	/*one limitation by design here is that once a user finishes the activity, subsequent users in the same group are also assumed finished
			    	 * since they belong to the same ungrouped activity and these users have the same tool session id*/
			    	
			    	boolean isResponseFinalised=mcQueUsr.isResponseFinalised();
			    	logger.debug("isResponseFinalised: " +isResponseFinalised);
			    	
			    	if (isResponseFinalised)
			    	{
				    	mcLearningForm.setReportViewOnly(new Boolean(true).toString());		    	    
			    	}
			    	else
			    	{
			    	    mcLearningForm.setReportViewOnly(new Boolean(false).toString());
			    	}

			    	request.setAttribute(REQUEST_BY_STARTER, new Boolean (true).toString());
			    	return mcLearningAction.viewAnswers(mapping, mcLearningForm, request, response);
		    	}
	        }
	    }
	    else if (mode.equals("teacher"))
	    {
	    	McLearningAction mcLearningAction= new McLearningAction();
	    	logger.debug("present to teacher learners progress...");
			mcLearningForm.setLearnerProgress(new Boolean(true).toString());
			mcLearningForm.setLearnerProgressUserId(userId);
	    	return mcLearningAction.viewAnswers(mapping, mcLearningForm, request, response);	
	    }
	    logger.debug("just presenting standard learner screen");
	    request.setAttribute(MC_LEARNER_STARTER_DTO, mcLearnerStarterDTO);
	    return (mapping.findForward(LOAD_LEARNER));	
	}
	
	
	/**
	 * sets up question and candidate answers maps
	 * commonContentSetup(HttpServletRequest request, McContent mcContent)
	 * 
	 * @param request
	 * @param mcContent
	 */
	protected void commonContentSetup(HttpServletRequest request, McContent mcContent, IMcService mcService, 
	        McLearningForm mcLearningForm, String toolSessionID)
	{
	    logger.debug("dettingcommon content: ");
		Map mapQuestionsContent= new TreeMap(new McComparator());
		
		boolean randomize=mcContent.isRandomize();
		logger.debug("randomize: " + randomize);
		
		List listQuestionAndCandidateAnswersDTO=LearningUtil.buildQuestionAndCandidateAnswersDTO(request, mcContent, randomize, mcService);
		
		logger.debug("listQuestionAndCandidateAnswersDTO: " + listQuestionAndCandidateAnswersDTO);
		request.setAttribute(LIST_QUESTION_CANDIDATEANSWERS_DTO, listQuestionAndCandidateAnswersDTO);
		logger.debug("LIST_QUESTION_CANDIDATEANSWERS_DTO: " +  request.getAttribute(LIST_QUESTION_CANDIDATEANSWERS_DTO));
		
		McGeneralLearnerFlowDTO mcGeneralLearnerFlowDTO=LearningUtil.buildMcGeneralLearnerFlowDTO(mcContent);
		mcGeneralLearnerFlowDTO.setTotalCountReached(new Boolean(false).toString());
		mcGeneralLearnerFlowDTO.setQuestionIndex(new Integer(1).toString());
		
	    logger.debug("is tool reflective: " + mcContent.isReflect());
	    mcGeneralLearnerFlowDTO.setReflection(new Boolean(mcContent.isReflect()).toString());
		logger.debug("reflection subject: " + mcContent.getReflectionSubject());
		mcGeneralLearnerFlowDTO.setReflectionSubject(mcContent.getReflectionSubject());
		
		
		String userID=mcLearningForm.getUserID();
		logger.debug("userID: " +  userID);
		
		
		logger.debug("attempt getting notebookEntry: ");
		NotebookEntry notebookEntry = mcService.getEntry(new Long(toolSessionID),
				CoreNotebookConstants.NOTEBOOK_TOOL,
				MY_SIGNATURE, new Integer(userID));
		
        logger.debug("notebookEntry: " + notebookEntry);
		
		if (notebookEntry != null) {
		    
		    String notebookEntryPresentable=McUtils.replaceNewLines(notebookEntry.getEntry());
		    mcGeneralLearnerFlowDTO.setNotebookEntry(notebookEntryPresentable);
		}


		
		request.setAttribute(MC_GENERAL_LEARNER_FLOW_DTO, mcGeneralLearnerFlowDTO);
		logger.debug("MC_GENERAL_LEARNER_FLOW_DTO: " +  request.getAttribute(MC_GENERAL_LEARNER_FLOW_DTO));
	}
	
	
	
	protected ActionForward validateParameters(HttpServletRequest request, McLearningForm mcLearningForm, ActionMapping mapping)
	{
		/*
	     * obtain and setup the current user's data 
	     */
		
	    String userID = "";
	    HttpSession ss = SessionManager.getSession();
	    logger.debug("ss: " + ss);
	    
	    if (ss != null)
	    {
		    UserDTO user = (UserDTO) ss.getAttribute(AttributeNames.USER);
		    if ((user != null) && (user.getUserID() != null))
		    {
		    	userID = user.getUserID().toString();
			    logger.debug("retrieved userId: " + userID);
		    }
	    }
		
	    mcLearningForm.setUserID(userID);
	    
	    /*
	     * process incoming tool session id and later derive toolContentId from it. 
	     */
    	String strToolSessionId=request.getParameter(AttributeNames.PARAM_TOOL_SESSION_ID);
	    long toolSessionId=0;
	    if ((strToolSessionId == null) || (strToolSessionId.length() == 0)) 
	    {
			logger.error("error.toolSessionId.required");
	    }
	    else
	    {
	    	try
			{
	    		toolSessionId=new Long(strToolSessionId).longValue();
		    	logger.debug("passed TOOL_SESSION_ID : " + new Long(toolSessionId));
			}
	    	catch(NumberFormatException e)
			{
				logger.error("error.sessionId.numberFormatException");
			}
	    }
	    
	    /*mode can be learner, teacher or author */
	    String mode=request.getParameter(MODE);
	    logger.debug("mode: " + mode);
	    
	    if ((mode == null) || (mode.length() == 0)) 
	    {
			logger.error("error.mode.required");
	    }
	    
	    if ((!mode.equals("learner")) && (!mode.equals("teacher")) && (!mode.equals("author")))
	    {
			logger.error("error.mode.invalid");
	    }
		logger.debug("session LEARNING_MODE set to:" + mode);
	    
	    return null;
	}

	
	/**
     * persists error messages to request scope
     * @param request
     * @param message
     */
	public void persistError(HttpServletRequest request, String message)
	{
		ActionMessages errors= new ActionMessages();
		errors.add(Globals.ERROR_KEY, new ActionMessage(message));
		logger.debug("add " + message +"  to ActionMessages:");
		saveErrors(request,errors);	    	    
	}
}  
