/***************************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ***********************************************************************/

/**
 * @author Ozgur Demirtas
 * 
 * Created on 8/03/2005
 * 
 * initializes the tool's authoring mode  
 */

/**
 * Tool path The URL path for the tool should be <lamsroot>/tool/$TOOL_SIG.
 * 
 * CONTENT_LOCKED ->CONTENT_IN_USE 
 * 
 * QaStarterAction loads the default content and initializes the presentation Map
 * Requests can come either from authoring envuironment or from the monitoring environment for Edit Activity screen
 * 
 * Check QaUtils.createAuthoringUser again User Management Service is ready
 * 
 * */

/**
 *
 * Tool Content:
 *
 * While tool's manage their own content, the LAMS core and the tools work together to create and use the content. 
 * The tool content id (toolContentId) is the key by which the tool and the LAMS core discuss data - 
 * it is generated by the LAMS core and supplied to the tool whenever content needs to be stored. 
 * The LAMS core will refer to the tool content id whenever the content needs to be used. 
 * Tool content will be covered in more detail in following sections.
 *
 * Each tool will have one piece of content that is the default content. 
 * The tool content id for this content is created as part of the installation process. 
 * Whenever a tool is asked for some tool content that does not exist, it should supply the default tool content. 
 * This will allow the system to render the normal screen, albeit with useless information, rather than crashing. 
*/

/**
*
* Authoring URL: 
*
* The tool must supply an authoring module, which will be called to create new content or edit existing content. It will be called by an authoring URL using the following format: ?????
* The initial data displayed on the authoring screen for a new tool content id may be the default tool content.
*
* Authoring UI data consists of general Activity data fields and the Tool specific data fields.
* The authoring interface will have three tabs. The mandatory (and suggested) fields are given. Each tool will have its own fields which it will add on any of the three tabs, as appropriate to the tabs' function.
*
* Basic: Displays the basic set of fields that are needed for the tool, and it could be expected that a new LAMS user would use. Mandatory fields: Title, Instructions.
* Advanced: Displays the extra fields that would be used by experienced LAMS users. Optional fields: Lock On Finish, Make Responses Anonymous
* Instructions: Displays the "instructions" fields for teachers. Mandatory fields: Online instructions, Offline instructions, Document upload.
* The "Define Later" and "Run Offline" options are set on the Flash authoring part, and not on the tool's authoring screens.
*
* Preview The tool must be able to show the specified content as if it was running in a lesson. It will be the learner url with tool access mode set to ToolAccessMode.AUTHOR.
* Export The tool must be able to export its tool content for part of the overall learning design export.
*
* The format of the serialization for export is XML. Tool will define extra namespace inside the <Content> element to add a new data element (type). Inside the data element, it can further define more structures and types as it seems fit.
* The data elements must be "version" aware. The data elements must be "type" aware if they are to be shared between Tools.
* 
* 
* 
*    <!--Authoring Starter  -->
    <action
		path="/authoringStarter"
		type="org.lamsfoundation.lams.tool.qa.web.QaStarterAction"
		name="QaAuthoringForm"
		scope="session"
		unknown="false"
		validate="false"
    >

		<exception
			key="error.exception.QaApplication"
			type="org.lamsfoundation.lams.tool.qa.QaApplicationException"
			handler="org.lamsfoundation.lams.tool.qa.web.CustomStrutsExceptionHandler"
			path="/SystemErrorContent.jsp"
			scope="request"
		/>
		    
		<exception
		    key="error.exception.QaApplication"
		    type="java.lang.NullPointerException"
		    handler="org.lamsfoundation.lams.tool.qa.web.CustomStrutsExceptionHandler"
		    path="/SystemErrorContent.jsp"
		    scope="request"
		/>	         			
	    
	    <forward
			name="load"
			path="/AuthoringMaincontent.jsp"
			redirect="false"
	    />
	
	    <forward
			name="loadViewOnly"
	      	path="/authoring/AuthoringTabsHolder.jsp"
	      	redirect="false"
	    />
	
	  	<forward
			name="loadMonitoring"
			path="/monitoring/MonitoringMaincontent.jsp"
			redirect="true"
	  	/>
	
	  	<forward
			name="errorList"
			path="/QaErrorBox.jsp"
			redirect="true"
	  	/>
	</action>  

* 
*/


/* $$Id$$ */
package org.lamsfoundation.lams.tool.qa.web;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.lamsfoundation.lams.tool.exception.ToolException;
import org.lamsfoundation.lams.tool.qa.QaAppConstants;
import org.lamsfoundation.lams.tool.qa.QaApplicationException;
import org.lamsfoundation.lams.tool.qa.QaComparator;
import org.lamsfoundation.lams.tool.qa.QaContent;
import org.lamsfoundation.lams.tool.qa.QaQueContent;
import org.lamsfoundation.lams.tool.qa.QaUtils;
import org.lamsfoundation.lams.tool.qa.service.IQaService;
import org.lamsfoundation.lams.tool.qa.service.QaServiceProxy;
import org.lamsfoundation.lams.web.util.AttributeNames;

/**
 * 
 * @author Ozgur Demirtas
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * 
 * A Map  data structure is used to present the UI.
   It is fetched by subsequent Action classes to manipulate its content and gets parsed in the presentation layer for display.
   
   NOTE: You have to keep in mind that once user can have multiple tool session ids.
 */
public class QaStarterAction extends Action implements QaAppConstants {
	static Logger logger = Logger.getLogger(QaStarterAction.class.getName());
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) 
  								throws IOException, ServletException, QaApplicationException {
	
		QaUtils.cleanUpSessionAbsolute(request);
		logger.debug("init authoring mode. removed attributes...");

		Map mapQuestionContent= new TreeMap(new QaComparator());
		logger.debug("mapQuestionContent: " + mapQuestionContent);
		
		QaAuthoringForm qaAuthoringForm = (QaAuthoringForm) form;
		logger.debug("qaAuthoringForm: " + qaAuthoringForm);
		qaAuthoringForm.resetRadioBoxes();
		
		IQaService qaService = (IQaService)request.getSession().getAttribute(TOOL_SERVICE);
		logger.debug("qaService: " + qaService);
		
		qaAuthoringForm.setCurrentTab("1");
		logger.debug("setting currrent tab to 1: ");

		
		if (qaService == null)
		{
			logger.debug("will retrieve qaService");
			qaService = QaServiceProxy.getQaService(getServlet().getServletContext());
			logger.debug("retrieving qaService from session: " + qaService);
		}
	    request.getSession().setAttribute(TOOL_SERVICE, qaService);
		
		String servletPath=request.getServletPath();
		logger.debug("getServletPath: "+ servletPath);
		String requestedModule=null;
		if (servletPath.indexOf("authoringStarter") > 0)
		{
			logger.debug("request is for authoring module");
			request.getSession().setAttribute(ACTIVE_MODULE, AUTHORING);
			request.getSession().setAttribute(DEFINE_LATER_IN_EDIT_MODE, new Boolean(true).toString());
			request.getSession().setAttribute(SHOW_AUTHORING_TABS,new Boolean(true).toString());
			qaAuthoringForm.setActiveModule(AUTHORING);
			requestedModule=AUTHORING;
		}
		else
		{
			logger.debug("request is for define later module either direcly from define later url or monitoring url");
			request.getSession().setAttribute(ACTIVE_MODULE, DEFINE_LATER);
			request.getSession().setAttribute(DEFINE_LATER_IN_EDIT_MODE, new Boolean(false).toString());
			request.getSession().setAttribute(SHOW_AUTHORING_TABS,new Boolean(false).toString());
			qaAuthoringForm.setActiveModule(DEFINE_LATER);			
			requestedModule=DEFINE_LATER;
			
			if (servletPath.indexOf("monitoring") > 0)
			{
				logger.debug("request is from monitoring  url.");
				request.getSession().setAttribute(MONITORING_ORIGINATED_DEFINELATER,new Boolean(true).toString());
			}
		}
		logger.debug("requestedModule: " + requestedModule);
		request.getSession().setAttribute(REQUESTED_MODULE,requestedModule);

	
		/* in development this needs to be called only once.  */ 
		/* QaUtils.configureContentRepository(request); */
		
		String sourceMcStarter = (String) request.getAttribute(SOURCE_MC_STARTER);
		logger.debug("sourceMcStarter: " + sourceMcStarter);
		
	    ActionForward validateSignature=readSignature(request,mapping);
		logger.debug("validateSignature:  " + validateSignature);
		if (validateSignature != null)
		{
			logger.debug("validateSignature not null : " + validateSignature);
			return validateSignature;
		}

		/*
	     * mark the http session as an authoring activity 
	     */
	    request.getSession().setAttribute(TARGET_MODE,TARGET_MODE_AUTHORING);
	    
	    /*
	     * find out whether the request is coming from monitoring module for EditActivity tab or from authoring environment url
	     */
	    logger.debug("no problems getting the default content, will render authoring screen");
	    String strToolContentId="";
	    /*the authoring url must be passed a tool content id*/
	    strToolContentId=request.getParameter(AttributeNames.PARAM_TOOL_CONTENT_ID);
	    logger.debug("strToolContentId: " + strToolContentId);
	    
	    if (strToolContentId == null)
	    {
	    	/*it is possible that the original request for authoring module is coming from monitoring url which keeps the
	    	 TOOL_CONTENT_ID in the session*/
	    	Long toolContentId =(Long) request.getSession().getAttribute(TOOL_CONTENT_ID);
		    logger.debug("toolContentId: " + toolContentId);
		    if (toolContentId != null)
		    {
		    	strToolContentId= toolContentId.toString();
			    logger.debug("cached strToolContentId from the session: " + strToolContentId);	
		    }
		    else
		    {
		    	logger.debug("we should IDEALLY not arrive here. The TOOL_CONTENT_ID is NOT available from the url or the session.");
		    	/*use default content instead of giving a warning*/
		    	String defaultContentId=(String) request.getSession().getAttribute(DEFAULT_CONTENT_ID_STR);
		    	logger.debug("using MCQ defaultContentId: " + defaultContentId);
		    	strToolContentId=defaultContentId;
		    }
	    }
    	logger.debug("final strToolContentId: " + strToolContentId);
	    
	    if ((strToolContentId == null) || (strToolContentId.equals(""))) 
	    {
	    	QaUtils.cleanUpSessionAbsolute(request);
	    	request.getSession().setAttribute(USER_EXCEPTION_CONTENTID_REQUIRED, new Boolean(true).toString());	    	
	    	persistError(request,"error.contentId.required");
			logger.debug("forwarding to: " + ERROR_LIST);
			return (mapping.findForward(ERROR_LIST));
	    }

	    
	    /* API test code for copying the content*/
    	String copyToolContent= (String) request.getParameter(COPY_TOOL_CONTENT);
    	logger.debug("copyToolContent: " + copyToolContent);
    	
    	if ((copyToolContent != null) && (copyToolContent.equals("1")))
		{
	    	logger.debug("user request to copy the content");
	    	Long fromContentId=new Long(strToolContentId);
	    	logger.debug("fromContentId: " + fromContentId);
	    	
	    	Long toContentId=new Long(9876);
	    	logger.debug("toContentId: " + toContentId);
	    	
	    	try
			{
	    		qaService.copyToolContent(fromContentId, toContentId);	
			}
	    	catch(ToolException e)
			{
	    		logger.debug("error copying the content: " + e);
			}
		}
        
        qaAuthoringForm.setToolContentId(strToolContentId);
        request.getSession().setAttribute(AttributeNames.PARAM_TOOL_CONTENT_ID, new Long(strToolContentId));
        logger.debug("using TOOL_CONTENT_ID: " + strToolContentId);
	    

		/*
		 * find out if the passed tool content id exists in the db 
		 * present user either a first timer screen with default content data or fetch the existing content.
		 * 
		 * if the toolcontentid does not exist in the db, create the default Map,
		 * there is no need to check if the content is locked in this case.
		 * It is always unlocked since it is the default content.
		*/
        
		if (!existsContent(new Long(strToolContentId).longValue(), qaService)) 
		{
			logger.debug("getting default content");
			/*fetch default content*/
			String defaultContentIdStr=(String) request.getSession().getAttribute(DEFAULT_CONTENT_ID_STR);
			logger.debug("defaultContentIdStr:" + defaultContentIdStr);
            retrieveContent(request, mapping, qaAuthoringForm, mapQuestionContent, new Long(defaultContentIdStr).longValue());
		}
        else
        {
        	logger.debug("getting existing content");
        	/* it is possible that the content is in use by learners.*/
        	QaContent qaContent=qaService.loadQa(new Long(strToolContentId).longValue());
        	logger.debug("qaContent: " + qaContent);
        	if (qaService.studentActivityOccurredGlobal(qaContent))
    		{
        		QaUtils.cleanUpSessionAbsolute(request);
    			logger.debug("student activity occurred on this content:" + qaContent);
    	    	request.getSession().setAttribute(USER_EXCEPTION_CONTENT_IN_USE, new Boolean(true).toString());    			
	    		persistError(request, "error.content.inUse");
	    		logger.debug("add error.content.inUse to ActionMessages.");
				return (mapping.findForward(ERROR_LIST));
    		}
            retrieveContent(request, mapping, qaAuthoringForm, mapQuestionContent, new Long(strToolContentId).longValue());
        }
		
		logger.debug("will return to jsp with: " + sourceMcStarter);
		String destination=QaUtils.getDestination(sourceMcStarter, requestedModule);
		logger.debug("destination: " + destination);
		return (mapping.findForward(destination));
	} 
	
	
	
	/**
	 * retrives the existing content information from the db and prepares the data for presentation purposes.
	 * ActionForward retrieveExistingContent(HttpServletRequest request, ActionMapping mapping, QaAuthoringForm qaAuthoringForm, Map mapQuestionContent, long toolContentId)
	 *  
	 * @param request
	 * @param mapping
	 * @param qaAuthoringForm
	 * @param mapQuestionContent
	 * @param toolContentId
	 * @return ActionForward
	 */
	protected void retrieveContent(HttpServletRequest request, ActionMapping mapping, QaAuthoringForm qaAuthoringForm, Map mapQuestionContent, long toolContentId)
	{
		logger.debug("starting retrieveExistingContent for toolContentId: " + toolContentId);

		IQaService qaService = (IQaService)request.getSession().getAttribute(TOOL_SERVICE);
		logger.debug("qaService: " + qaService);
		if (qaService == null)
		{
			logger.debug("will retrieve qaService");
			qaService = QaServiceProxy.getQaService(getServlet().getServletContext());
			logger.debug("retrieving qaService from session: " + qaService);
		}
	    request.getSession().setAttribute(TOOL_SERVICE, qaService);

	    logger.debug("getting existing content with id:" + toolContentId);
	    QaContent qaContent = qaService.retrieveQa(toolContentId);
		logger.debug("QaContent: " + qaContent);
		
		QaUtils.setDefaultSessionAttributes(request, qaContent, qaAuthoringForm);
		logger.debug("form title is: : " + qaAuthoringForm.getTitle());
		
        QaUtils.populateUploadedFilesData(request, qaContent, qaService);
	    request.getSession().setAttribute(IS_DEFINE_LATER, new Boolean(qaContent.isDefineLater()));
	    
	    qaAuthoringForm.setTitle(qaContent.getTitle());
		qaAuthoringForm.setInstructions(qaContent.getInstructions());
		
		if (qaContent.getTitle() == null)
		{
			request.getSession().setAttribute(ACTIVITY_TITLE, "Questions and Answers");
			request.getSession().setAttribute(ACTIVITY_INSTRUCTIONS, "Please answer the questions.");
		}
		else
		{
			request.getSession().setAttribute(ACTIVITY_TITLE, qaContent.getTitle());
			request.getSession().setAttribute(ACTIVITY_INSTRUCTIONS, qaContent.getInstructions());			
		}

		
		logger.debug("Title is: " + qaContent.getTitle());
		logger.debug("Instructions is: " + qaContent.getInstructions());
		if ((qaAuthoringForm.getTitle() == null) || (qaAuthoringForm.getTitle().equals("")))
		{
			logger.debug("resetting title");
			String activityTitle=(String)request.getSession().getAttribute(ACTIVITY_TITLE);
			logger.debug("activityTitle: " + activityTitle);
			qaAuthoringForm.setTitle(activityTitle);
		}
	    
		if ((qaAuthoringForm.getInstructions() == null) || (qaAuthoringForm.getInstructions().equals("")))
		{
			logger.debug("resetting instructions");
			String activityInstructions=(String)request.getSession().getAttribute(ACTIVITY_INSTRUCTIONS);
			logger.debug("activityInstructions: " + activityInstructions);
			qaAuthoringForm.setInstructions(activityInstructions);
		}

		
	    /*
		 * get the existing question content
		 */
		logger.debug("setting existing content data from the db");
		mapQuestionContent.clear();
		Iterator queIterator=qaContent.getQaQueContents().iterator();
		Long mapIndex=new Long(1);
		logger.debug("mapQuestionContent: " + mapQuestionContent);
		while (queIterator.hasNext())
		{
			QaQueContent qaQueContent=(QaQueContent) queIterator.next();
			if (qaQueContent != null)
			{
				logger.debug("question: " + qaQueContent.getQuestion());
	    		mapQuestionContent.put(mapIndex.toString(),qaQueContent.getQuestion());
	    		/**
	    		 * make the first entry the default(first) one for jsp
	    		 */
	    		if (mapIndex.longValue() == 1)
	    			request.getSession().setAttribute(DEFAULT_QUESTION_CONTENT, qaQueContent.getQuestion());
	    		mapIndex=new Long(mapIndex.longValue()+1);
			}
		}
		logger.debug("Map initialized with existing contentid to: " + mapQuestionContent);
		
		logger.debug("callling presentInitialUserInterface for the existing content.");
		
		request.getSession().setAttribute(MAP_QUESTION_CONTENT, mapQuestionContent);
		logger.debug("starter initialized the Comparable Map: " + request.getSession().getAttribute("mapQuestionContent") );

		logger.debug("final title: " + qaAuthoringForm.getTitle());
		logger.debug("final ins: " + qaAuthoringForm.getInstructions());
		
		/*
		 * load questions page
		 */
		qaAuthoringForm.resetUserAction();
	}

	
	/**
	 * each tool has a signature. QA tool's signature is stored in MY_SIGNATURE. The default tool content id and 
	 * other depending content ids are obtained in this method.
	 * if all the default content has been setup properly the method persists DEFAULT_CONTENT_ID in the session.
	 * 
	 * readSignature(HttpServletRequest request, ActionMapping mapping)
	 * @param request
	 * @param mapping
	 * @return ActionForward
	 */
	public ActionForward readSignature(HttpServletRequest request, ActionMapping mapping)
	{
		IQaService qaService = (IQaService)request.getSession().getAttribute(TOOL_SERVICE);
		logger.debug("qaService: " + qaService);
		if (qaService == null)
		{
			logger.debug("will retrieve qaService");
			qaService = QaServiceProxy.getQaService(getServlet().getServletContext());
			logger.debug("retrieving qaService from session: " + qaService);
		}
	    request.getSession().setAttribute(TOOL_SERVICE, qaService);
		/*
		 * retrieve the default content id based on tool signature
		 */
		long defaultContentID=0;
		try
		{
			logger.debug("attempt retrieving tool with signatute : " + MY_SIGNATURE);
            defaultContentID=qaService.getToolDefaultContentIdBySignature(MY_SIGNATURE);
			logger.debug("retrieved tool default contentId: " + defaultContentID);
			if (defaultContentID == 0)
			{
				QaUtils.cleanUpSessionAbsolute(request);
				logger.debug("default content id has not been setup");
				persistError(request,"error.defaultContent.notSetup");
		    	request.getSession().setAttribute(USER_EXCEPTION_DEFAULTCONTENT_NOTSETUP, new Boolean(true).toString());
				return (mapping.findForward(ERROR_LIST));	
			}
		}
		catch(Exception e)
		{
			QaUtils.cleanUpSessionAbsolute(request);
			logger.debug("error getting the default content id: " + e.getMessage());
			persistError(request,"error.defaultContent.notSetup");
	    	request.getSession().setAttribute(USER_EXCEPTION_DEFAULTCONTENT_NOTSETUP, new Boolean(true).toString());
			logger.debug("forwarding to: " + ERROR_LIST);
			return (mapping.findForward(ERROR_LIST));
		}

		
		/* retrieve uid of the content based on default content id determined above */
		long contentUID=0;
		try
		{
			logger.debug("retrieve uid of the content based on default content id determined above: " + defaultContentID);
			QaContent qaContent=qaService.loadQa(defaultContentID);
			if (qaContent == null)
			{
				QaUtils.cleanUpSessionAbsolute(request);
				logger.debug("Exception occured: No default content");
	    		persistError(request,"error.defaultContent.notSetup");
	    		request.getSession().setAttribute(USER_EXCEPTION_DEFAULTCONTENT_NOTSETUP, new Boolean(true).toString());
	    		return (mapping.findForward(ERROR_LIST));
			}
			logger.debug("using qaContent: " + qaContent);
			logger.debug("using mcContent uid: " + qaContent.getUid());
			contentUID=qaContent.getUid().longValue();
			logger.debug("contentUID: " + contentUID);
		}
		catch(Exception e)
		{
			QaUtils.cleanUpSessionAbsolute(request);
			logger.debug("Exception occured: No default question content");
			persistError(request,"error.defaultContent.notSetup");
    		request.getSession().setAttribute(USER_EXCEPTION_DEFAULTCONTENT_NOTSETUP, new Boolean(true).toString());
			logger.debug("forwarding to: " + ERROR_LIST);
			return (mapping.findForward(ERROR_LIST));
		}

		
		/* retrieve uid of the default question content  */
		long queContentUID=0;
		try
		{
			logger.debug("retrieve the default question content based on default content UID: " + queContentUID);
			QaQueContent qaQueContent=qaService.getToolDefaultQuestionContent(contentUID);
			logger.debug("using mcQueContent: " + qaQueContent);
			if (qaQueContent == null)
			{
				logger.debug("Exception occured: No default question content");
	    		persistError(request,"error.defaultQuestionContent.notAvailable");
	    		QaUtils.cleanUpSessionAbsolute(request);
	    		return (mapping.findForward(LOAD_QUESTIONS));
			}
			logger.debug("using qaQueContent uid: " + qaQueContent.getUid());
			request.getSession().setAttribute(DEFAULT_QUESTION_CONTENT, qaQueContent.getQuestion());
		}
		catch(Exception e)
		{
			logger.debug("Exception occured: No default question content");
    		persistError(request,"error.defaultQuestionContent.notAvailable");
    		QaUtils.cleanUpSessionAbsolute(request);    		
			logger.debug("forwarding to: " + ERROR_LIST);
			return (mapping.findForward(ERROR_LIST));
		}
		
		logger.debug("QA tool has the default content id: " + defaultContentID);
		request.getSession().setAttribute(DEFAULT_CONTENT_ID_STR, new Long(defaultContentID).toString());
		return null;
	}
	
	
	
	/**
	 * existsContent(long toolContentId)
	 * @param long toolContentId
	 * @return boolean
	 * determine whether a specific toolContentId exists in the db
	 */
	protected boolean existsContent(long toolContentId, IQaService qaService)
	{
		QaContent qaContent=qaService.loadQa(toolContentId);
	    if (qaContent == null) 
	    	return false;
	    
		return true;	
	}
	
	
	/**
	 * bridges define later url request to authoring functionality
	 * 
	 * executeDefineLater(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response, IQaService qaService) 
		throws IOException, ServletException, QaApplicationException
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param qaService
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 * @throws QaApplicationException
	 */
	public ActionForward executeDefineLater(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response, IQaService qaService) 
		throws IOException, ServletException, QaApplicationException {
		logger.debug("passed qaService: " + qaService);
		request.getSession().setAttribute(TOOL_SERVICE, qaService);
		logger.debug("calling execute...");
		return execute(mapping, form, request, response);
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

