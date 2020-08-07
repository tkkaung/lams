/****************************************************************
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 *
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */

package org.lamsfoundation.lams.tool.service;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.lamsfoundation.lams.confidencelevel.ConfidenceLevelDTO;
import org.lamsfoundation.lams.gradebook.service.IGradebookService;
import org.lamsfoundation.lams.learning.service.ILearnerService;
import org.lamsfoundation.lams.learningdesign.Activity;
import org.lamsfoundation.lams.learningdesign.ActivityEvaluation;
import org.lamsfoundation.lams.learningdesign.DataFlowObject;
import org.lamsfoundation.lams.learningdesign.FloatingActivity;
import org.lamsfoundation.lams.learningdesign.ToolActivity;
import org.lamsfoundation.lams.learningdesign.Transition;
import org.lamsfoundation.lams.learningdesign.dao.IActivityDAO;
import org.lamsfoundation.lams.learningdesign.dao.IDataFlowDAO;
import org.lamsfoundation.lams.learningdesign.dto.ActivityPositionDTO;
import org.lamsfoundation.lams.lesson.CompletedActivityProgress;
import org.lamsfoundation.lams.lesson.LearnerProgress;
import org.lamsfoundation.lams.lesson.service.ILessonService;
import org.lamsfoundation.lams.logevent.service.ILogEventService;
import org.lamsfoundation.lams.tool.Tool;
import org.lamsfoundation.lams.tool.ToolOutput;
import org.lamsfoundation.lams.tool.ToolSession;
import org.lamsfoundation.lams.tool.dao.IToolContentDAO;
import org.lamsfoundation.lams.tool.dao.IToolDAO;
import org.lamsfoundation.lams.tool.dao.IToolSessionDAO;
import org.lamsfoundation.lams.tool.exception.ToolException;
import org.lamsfoundation.lams.usermanagement.User;
import org.lamsfoundation.lams.usermanagement.service.IUserManagementService;
import org.lamsfoundation.lams.util.FileUtil;
import org.lamsfoundation.lams.util.FileUtilException;
import org.lamsfoundation.lams.web.util.AttributeNames;

/**
 * @author Jacky Fang
 * @since 2005-3-17
 *
 * @author Ozgur Demirtas 24/06/2005
 */
public class LamsToolService implements ILamsToolService {
    // Leader selection tool Constants
    private static final String LEADER_SELECTION_TOOL_OUTPUT_NAME_LEADER_USERID = "leader.user.id";

    private static final String TOOL_SIGNATURE_ASSESSMENT = "laasse10";
    private static final String TOOL_SIGNATURE_MCQ = "lamc11";

    private IActivityDAO activityDAO;
    private ILogEventService logEventService;
    private IToolDAO toolDAO;
    private IToolSessionDAO toolSessionDAO;
    private IToolContentDAO toolContentDAO;
    private IGradebookService gradebookService;
    private ILamsCoreToolService lamsCoreToolService;
    private ILessonService lessonService;
    private ILearnerService learnerService;
    private IUserManagementService userManagementService;
    private IDataFlowDAO dataFlowDAO;

    @Override
    public Tool getToolByID(Long toolId) {
	return toolDAO.getToolByID(toolId);
    }

    @Override
    public Tool getToolBySignature(final String toolSignature) {
	return toolDAO.getToolBySignature(toolSignature);
    }

    @Override
    public Tool getPersistToolBySignature(final String toolSignature) {
	return toolDAO.getToolBySignature(toolSignature);
    }

    @Override
    public long getToolDefaultContentIdBySignature(final String toolSignature) {
	return toolDAO.getToolDefaultContentIdBySignature(toolSignature);
    }

    @Override
    public String generateUniqueContentFolder() throws FileUtilException, IOException {

	return FileUtil.generateUniqueContentFolderID();
    }

    @Override
    public String getLearnerContentFolder(Long toolSessionId, Long userId) {

	ToolSession toolSession = this.getToolSession(toolSessionId);
	Long lessonId = toolSession.getLesson().getLessonId();
	String learnerContentFolder = FileUtil.getLearnerContentFolder(lessonId, userId);

	return learnerContentFolder;
    }

    @Override
    public void saveOrUpdateTool(Tool tool) {
	toolDAO.saveOrUpdateTool(tool);
    }

    @Override
    public ToolSession getToolSession(Long toolSessionId) {
	return toolSessionDAO.getToolSession(toolSessionId);
    }

    @Override
    public String completeToolSession(Long toolSessionId, Long learnerId) {
	return learnerService.completeToolSession(toolSessionId, learnerId);
    }

    @Override
    public boolean isLastActivity(Long toolSessionId) {
	ActivityPositionDTO positionDTO = learnerService.getActivityPositionByToolSessionId(toolSessionId);
	//check whether Activity is the last
	return (positionDTO != null) && positionDTO.getLast();
    }

    @Override
    public void updateActivityMark(Double mark, String feedback, Integer userID, Long toolSessionID,
	    Boolean markedInGradebook) {
	gradebookService.updateGradebookUserActivityMark(mark, feedback, userID, toolSessionID, markedInGradebook);
    }

    @Override
    public void removeActivityMark(Integer userID, Long toolSessionID) {
	gradebookService.removeActivityMark(userID, toolSessionID);
    }

    @Override
    public Boolean isGroupedActivity(long toolContentID) {
	ToolActivity toolActivity = activityDAO.getToolActivityByToolContentId(toolContentID);
	boolean isGroupedActivity = toolActivity == null ? null : toolActivity.getApplyGrouping();

	return isGroupedActivity;
    }

    @Override
    public void auditLogStartEditingActivityInMonitor(long toolContentID) {
	logEventService.logStartEditingActivityInMonitor(toolContentID);
    }

    @Override
    public String getActivityEvaluation(Long toolContentId) {
	ToolActivity toolActivity = activityDAO.getToolActivityByToolContentId(toolContentId);
	ActivityEvaluation evaluation = toolActivity.getEvaluation();
	return evaluation == null ? null : evaluation.getToolOutputDefinition();
    }

    @Override
    public void setActivityEvaluation(Long toolContentId, String toolOutputDefinition) {
	ToolActivity toolActivity = activityDAO.getToolActivityByToolContentId(toolContentId);
	ActivityEvaluation evaluation = toolActivity.getEvaluation();

	if (StringUtils.isEmpty(toolOutputDefinition)) {
	    if (evaluation != null) {
		toolActivity.setEvaluation(null);
		activityDAO.delete(evaluation);
	    }
	    gradebookService.removeActivityMark(toolContentId);
	    return;
	}

	boolean isToolOutputDefinitionChanged = true;
	if (evaluation == null) {
	    evaluation = new ActivityEvaluation();
	    evaluation.setActivity(toolActivity);
	    toolActivity.setEvaluation(evaluation);
	} else {
	    isToolOutputDefinitionChanged = !toolOutputDefinition.equals(evaluation.getToolOutputDefinition());
	}
	evaluation.setToolOutputDefinition(toolOutputDefinition);
	activityDAO.update(toolActivity);

	//update gradebook marks if required
	if (isToolOutputDefinitionChanged) {
	    gradebookService.recalculateGradebookMarksForActivity(toolActivity);
	}

    }

    @Override
    public ToolOutput getToolInput(Long requestingToolContentId, Integer assigmentId, Integer learnerId) {
	DataFlowObject dataFlowObject = dataFlowDAO.getAssignedDataFlowObject(requestingToolContentId, assigmentId);
	User learner = (User) userManagementService.findById(User.class, learnerId);
	Activity activity = dataFlowObject.getDataTransition().getFromActivity();
	String outputName = dataFlowObject.getName();
	ToolSession session = lamsCoreToolService.getToolSessionByLearner(learner, activity);
	ToolOutput output = lamsCoreToolService.getOutputFromTool(outputName, session, learnerId);

	return output;
    }

    @Override
    public Long getLeaderUserId(Long toolSessionId, Integer learnerId) {
	Long leaderUserId = null;

	ToolSession toolSession = this.getToolSession(toolSessionId);
	ToolActivity specifiedActivity = toolSession.getToolActivity();
	Activity leaderSelectionActivity = getNearestLeaderSelectionActivity(specifiedActivity, learnerId,
		toolSession.getLesson().getLessonId());

	// check if there is leaderSelectionTool available
	if (leaderSelectionActivity != null) {
	    User learner = (User) toolContentDAO.find(User.class, learnerId);
	    String outputName = LamsToolService.LEADER_SELECTION_TOOL_OUTPUT_NAME_LEADER_USERID;
	    ToolSession leaderSelectionSession = toolSessionDAO.getToolSessionByLearner(learner,
		    leaderSelectionActivity);
	    if (leaderSelectionSession != null) {
		ToolOutput output = lamsCoreToolService.getOutputFromTool(outputName, leaderSelectionSession, null);

		// check if tool produced output
		if ((output != null) && (output.getValue() != null)) {
		    leaderUserId = output.getValue().getLong();
		}
	    }
	}

	return leaderUserId;
    }

    @Override
    public Set<Long> getAllLeaderUserIds(Long toolSessionId, Integer learnerId) {
	Set<Long> leaderUserIds = null;

	ToolSession toolSession = this.getToolSession(toolSessionId);
	ToolActivity specifiedActivity = toolSession.getToolActivity();
	Activity leaderSelectionActivity = getNearestLeaderSelectionActivity(specifiedActivity, learnerId,
		toolSession.getLesson().getLessonId());

	// check if there is leaderSelectionTool available
	if (leaderSelectionActivity != null) {
	    leaderUserIds = getLeaderUserId(leaderSelectionActivity.getActivityId());
	}

	return leaderUserIds;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Long> getLeaderUserId(Long leaderSelectionActivityId) {
	Activity activity = activityDAO.getActivityByActivityId(leaderSelectionActivityId);
	List<ToolSession> toolSessions = toolSessionDAO.getToolSessionByActivity(activity);
	Set<Long> result = new TreeSet<Long>();
	for (ToolSession toolSession : toolSessions) {
	    ToolOutput output = lamsCoreToolService.getOutputFromTool(LEADER_SELECTION_TOOL_OUTPUT_NAME_LEADER_USERID,
		    toolSession, null);

	    // check if tool produced output
	    if ((output != null) && (output.getValue() != null)) {
		result.add(output.getValue().getLong());
	    }
	}

	return result;
    }

    /**
     * Finds the nearest Leader Select activity. Works recursively. Tries to find Leader Select activity in the previous
     * activities set first, and then inside the parent set.
     */
    @SuppressWarnings("rawtypes")
    private Activity getNearestLeaderSelectionActivity(Activity activity, Integer userId, Long lessonId) {
	// check if current activity is Leader Select one. if so - stop searching and return it.
	Class activityClass = Hibernate.getClass(activity);
	if (activityClass.equals(ToolActivity.class)) {
	    ToolActivity toolActivity;

	    // activity is loaded as proxy due to lazy loading and in order to prevent quering DB we just re-initialize
	    // it here again
	    Hibernate.initialize(activity);
	    if (activity instanceof HibernateProxy) {
		toolActivity = (ToolActivity) ((HibernateProxy) activity).getHibernateLazyInitializer()
			.getImplementation();
	    } else {
		toolActivity = (ToolActivity) activity;
	    }

	    if (ILamsToolService.LEADER_SELECTION_TOOL_SIGNATURE.equals(toolActivity.getTool().getToolSignature())) {
		return activity;
	    }

	    //in case of a floating activity
	} else if (activityClass.equals(FloatingActivity.class)) {
	    LearnerProgress learnerProgress = lessonService.getUserProgressForLesson(userId, lessonId);
	    Map<Activity, CompletedActivityProgress> completedActivities = learnerProgress.getCompletedActivities();

	    //find the earliest finished Leader Select Activity
	    Date leaderSelectActivityFinishDate = null;
	    Activity leaderSelectionActivity = null;
	    for (Activity completedActivity : completedActivities.keySet()) {

		if (completedActivity instanceof ToolActivity) {
		    ToolActivity completedToolActivity = (ToolActivity) completedActivity;
		    if (ILamsToolService.LEADER_SELECTION_TOOL_SIGNATURE
			    .equals(completedToolActivity.getTool().getToolSignature())) {
			Date finishDate = completedActivities.get(completedActivity).getFinishDate();

			if ((leaderSelectActivityFinishDate == null)
				|| (finishDate.compareTo(leaderSelectActivityFinishDate) < 0)) {
			    leaderSelectionActivity = completedToolActivity;
			    leaderSelectActivityFinishDate = completedActivities.get(completedActivity).getFinishDate();
			}

		    }
		}

	    }

	    return leaderSelectionActivity;
	}

	// check previous activity
	Transition transitionTo = activity.getTransitionTo();
	if (transitionTo != null) {
	    Activity fromActivity = transitionTo.getFromActivity();
	    return getNearestLeaderSelectionActivity(fromActivity, userId, lessonId);
	}

	// check parent activity
	Activity parent = activity.getParentActivity();
	if (parent != null) {
	    return getNearestLeaderSelectionActivity(parent, userId, lessonId);
	}

	return null;
    }

    @Override
    public Set<ToolActivity> getPrecedingConfidenceLevelsActivities(Long toolContentId) {
	ToolActivity specifiedActivity = activityDAO.getToolActivityByToolContentId(toolContentId);

	//if specifiedActivity is null - most likely author hasn't saved the sequence yet
	if (specifiedActivity == null) {
	    return null;
	}

	Set<Long> confidenceProvidingActivityIds = new LinkedHashSet<Long>();
	findPrecedingConfidenceProvidingActivities(specifiedActivity, confidenceProvidingActivityIds);

	Set<ToolActivity> confidenceProvidingActivities = new LinkedHashSet<ToolActivity>();
	for (Long confidenceProvidingActivityId : confidenceProvidingActivityIds) {
	    ToolActivity confidenceProvidingActivity = (ToolActivity) activityDAO
		    .getActivityByActivityId(confidenceProvidingActivityId, ToolActivity.class);
	    confidenceProvidingActivities.add(confidenceProvidingActivity);
	}

	return confidenceProvidingActivities;
    }

    /**
     * Finds all preceding activities that can provide confidence levels (currently only Assessment and MCQ provide
     * them). Please note, it does not check whether enableConfidenceLevels advanced option is ON in those activities.
     */
    @SuppressWarnings("rawtypes")
    private void findPrecedingConfidenceProvidingActivities(Activity activity,
	    Set<Long> confidenceProvidingActivityIds) {
	// check if current activity is Leader Select one. if so - stop searching and return it.
	Class activityClass = Hibernate.getClass(activity);
	if (activityClass.equals(ToolActivity.class)) {
	    ToolActivity toolActivity;

	    // activity is loaded as proxy due to lazy loading and in order to prevent quering DB we just re-initialize
	    // it here again
	    Hibernate.initialize(activity);
	    if (activity instanceof HibernateProxy) {
		toolActivity = (ToolActivity) ((HibernateProxy) activity).getHibernateLazyInitializer()
			.getImplementation();
	    } else {
		toolActivity = (ToolActivity) activity;
	    }

	    String toolSignature = toolActivity.getTool().getToolSignature();
	    if (TOOL_SIGNATURE_ASSESSMENT.equals(toolSignature) || TOOL_SIGNATURE_MCQ.equals(toolSignature)) {
		confidenceProvidingActivityIds.add(toolActivity.getActivityId());
	    }

	    //in case of a floating activity - return all available confidence providing activities
	} else if (activityClass.equals(FloatingActivity.class)) {
	    Set<Activity> activities = activity.getLearningDesign().getActivities();
	    for (Activity activityIter : activities) {
		if (activityIter instanceof ToolActivity) {
		    String toolSignatureIter = ((ToolActivity) activityIter).getTool().getToolSignature();
		    if (TOOL_SIGNATURE_ASSESSMENT.equals(toolSignatureIter)
			    || TOOL_SIGNATURE_MCQ.equals(toolSignatureIter)) {
			confidenceProvidingActivityIds.add(activityIter.getActivityId());
		    }
		}
	    }
	    return;
	}

	// check previous activity
	Transition transitionTo = activity.getTransitionTo();
	if (transitionTo != null) {
	    Activity fromActivity = transitionTo.getFromActivity();
	    findPrecedingConfidenceProvidingActivities(fromActivity, confidenceProvidingActivityIds);
	    return;
	}

	// check parent activity
	Activity parent = activity.getParentActivity();
	if (parent != null) {
	    findPrecedingConfidenceProvidingActivities(parent, confidenceProvidingActivityIds);
	    return;
	}
    }

    @Override
    public List<ConfidenceLevelDTO> getConfidenceLevelsByActivity(Integer confidenceLevelActivityUiid,
	    Integer requestorUserId, Long requestorToolSessionId) {
	User user = (User) activityDAO.find(User.class, requestorUserId);
	if (user == null) {
	    throw new ToolException("No user found for userId=" + requestorUserId);
	}

	ToolSession requestorSession = toolSessionDAO.getToolSession(requestorToolSessionId);
	if (requestorSession == null) {
	    throw new ToolException("No session found for toolSessionId=" + requestorToolSessionId);
	}

	Activity confidenceLevelActivity = activityDAO.getActivityByUIID(confidenceLevelActivityUiid,
		requestorSession.getToolActivity().getLearningDesign());
	ToolSession confidenceLevelSession = toolSessionDAO.getToolSessionByLearner(user, confidenceLevelActivity);

	List<ConfidenceLevelDTO> confidenceLevelDtos = lamsCoreToolService
		.getConfidenceLevelsByToolSession(confidenceLevelSession);

	return confidenceLevelDtos;
    }

    @Override
    public Integer getCountUsersForActivity(Long toolSessionId) {
	ToolSession session = toolSessionDAO.getToolSession(toolSessionId);
	return session.getLearners().size();
    }

    // ---------------------------------------------------------------------
    // Inversion of Control Methods - Method injection
    // ---------------------------------------------------------------------

    /**
     * @param toolDAO
     *            The toolDAO to set.
     */
    public void setToolDAO(IToolDAO toolDAO) {
	this.toolDAO = toolDAO;
    }

    public void setActivityDAO(IActivityDAO activityDAO) {
	this.activityDAO = activityDAO;
    }

    public void setLogEventService(ILogEventService logEventService) {
	this.logEventService = logEventService;
    }

    public void setToolSessionDAO(IToolSessionDAO toolSessionDAO) {
	this.toolSessionDAO = toolSessionDAO;
    }

    /**
     * @param toolContentDAO
     */
    public void setToolContentDAO(IToolContentDAO toolContentDAO) {
	this.toolContentDAO = toolContentDAO;
    }

    public void setGradebookService(IGradebookService gradebookService) {
	this.gradebookService = gradebookService;
    }

    /**
     * @param toolContentDAO
     */
    public void setLamsCoreToolService(ILamsCoreToolService lamsCoreToolService) {
	this.lamsCoreToolService = lamsCoreToolService;
    }

    public void setLessonService(ILessonService lessonService) {
	this.lessonService = lessonService;
    }

    public void setLearnerService(ILearnerService learnerService) {
	this.learnerService = learnerService;
    }

    /**
     * @param userService
     *            User Management Service
     */
    public void setUserManagementService(IUserManagementService userService) {
	this.userManagementService = userService;
    }

    public void setDataFlowDAO(IDataFlowDAO dataFlowDAO) {
	this.dataFlowDAO = dataFlowDAO;
    }
}