/****************************************************************
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
 * ****************************************************************
 */
/* $$Id$$ */
package org.lamsfoundation.lams.tool.qa.web;

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.lamsfoundation.lams.tool.qa.QaAppConstants;
import org.lamsfoundation.lams.tool.qa.QaContent;
import org.lamsfoundation.lams.tool.qa.QaQueContent;
import org.lamsfoundation.lams.tool.qa.QaQueUsr;
import org.lamsfoundation.lams.tool.qa.QaUsrResp;
import org.lamsfoundation.lams.tool.qa.dto.GeneralLearnerFlowDTO;
import org.lamsfoundation.lams.tool.qa.dto.QaQuestionDTO;
import org.lamsfoundation.lams.tool.qa.service.IQaService;
import org.lamsfoundation.lams.tool.qa.util.QaComparator;
import org.lamsfoundation.lams.tool.qa.web.form.QaLearningForm;
import org.lamsfoundation.lams.web.util.AttributeNames;

/**
 * 
 * Keeps all operations needed for Learning mode.
 * 
 * @author Ozgur Demirtas
 * 
 */
public class LearningUtil implements QaAppConstants {

    public static void saveFormRequestData(HttpServletRequest request, QaLearningForm qaLearningForm) {
	String toolSessionID = request.getParameter(AttributeNames.PARAM_TOOL_SESSION_ID);
	qaLearningForm.setToolSessionID(toolSessionID);

	String userID = request.getParameter("userID");
	qaLearningForm.setUserID(userID);

	String httpSessionID = request.getParameter("httpSessionID");
	qaLearningForm.setHttpSessionID(httpSessionID);

	String totalQuestionCount = request.getParameter("totalQuestionCount");
	qaLearningForm.setTotalQuestionCount(totalQuestionCount);
    }

    public static GeneralLearnerFlowDTO buildGeneralLearnerFlowDTO(QaContent qaContent) {
	GeneralLearnerFlowDTO generalLearnerFlowDTO = new GeneralLearnerFlowDTO();
	generalLearnerFlowDTO.setActivityTitle(qaContent.getTitle());
	generalLearnerFlowDTO.setActivityInstructions(qaContent.getInstructions());
	generalLearnerFlowDTO.setReportTitleLearner(qaContent.getReportTitle());

	if (qaContent.isQuestionsSequenced())
	    generalLearnerFlowDTO.setQuestionListingMode(QUESTION_LISTING_MODE_SEQUENTIAL);
	else
	    generalLearnerFlowDTO.setQuestionListingMode(QUESTION_LISTING_MODE_COMBINED);

	generalLearnerFlowDTO.setUserNameVisible(new Boolean(qaContent.isUsernameVisible()).toString());
	generalLearnerFlowDTO.setShowOtherAnswers(new Boolean(qaContent.isShowOtherAnswers()).toString());
	generalLearnerFlowDTO.setAllowRichEditor(new Boolean(qaContent.isAllowRichEditor()).toString());
	generalLearnerFlowDTO.setUseSelectLeaderToolOuput(new Boolean(qaContent.isUseSelectLeaderToolOuput()).toString());
	generalLearnerFlowDTO.setAllowRateAnswers(new Boolean(qaContent.isAllowRateAnswers()).toString());
	
	generalLearnerFlowDTO.setTotalQuestionCount(new Integer(qaContent.getQaQueContents().size()));

	//create mapQuestions
	Map<Integer, QaQuestionDTO> mapQuestions = new TreeMap<Integer, QaQuestionDTO>();
	for (QaQueContent question : qaContent.getQaQueContents()) {
	    int displayOrder = question.getDisplayOrder();
	    if (displayOrder != 0) {
		//add the question to the questions Map in the displayOrder
		QaQuestionDTO questionDTO = new QaQuestionDTO(question);
		mapQuestions.put(displayOrder, questionDTO);
	    }
	}
	generalLearnerFlowDTO.setMapQuestionContentLearner(mapQuestions);
	
	return generalLearnerFlowDTO;
    }

    public static String getRemainingQuestionCount(int currentQuestionIndex, String totalQuestionCount) {
	int remainingQuestionCount = new Long(totalQuestionCount).intValue() - currentQuestionIndex + 1;
	return new Integer(remainingQuestionCount).toString();
    }

    /**
     * feedBackAnswersProgress(HttpServletRequest request, int
     * currentQuestionIndex) give user feedback on the remaining questions
     * 
     * @param qaLearningForm
     *                return void
     */
    public static String feedBackAnswersProgress(HttpServletRequest request, int currentQuestionIndex,
	    String totalQuestionCount) {
	int remainingQuestionCount = new Long(totalQuestionCount).intValue() - currentQuestionIndex + 1;
	String userFeedback = "";
	if (remainingQuestionCount != 0) {
	    userFeedback = "Remaining question count: " + remainingQuestionCount;
	} else {
	    userFeedback = "End of the questions.";
	}

	return userFeedback;
    }
    
    /**
     */
    public static void populateAnswers(Map sessionMap, QaContent qaContent, QaQueUsr qaQueUsr,
	    Map<Integer, QaQuestionDTO> mapQuestions, GeneralLearnerFlowDTO generalLearnerFlowDTO,
	    IQaService qaService) {
	
	//create mapAnswers
	Map<String, String> mapAnswers = (Map) sessionMap.get(MAP_ALL_RESULTS_KEY);
	if (mapAnswers == null) {
	    mapAnswers = new TreeMap<String, String>(new QaComparator());
	    
	    // get responses from DB
	    Map<String, String> mapAnswersFromDb = new TreeMap<String, String>();
	    for (QaQueContent question : qaContent.getQaQueContents()) {
		Long questionUid = question.getUid();
		QaUsrResp dbResponse = qaService.getResponseByUserAndQuestion(qaQueUsr.getQueUsrId(), questionUid);
		if (dbResponse != null) {
		    mapAnswersFromDb.put(String.valueOf(question.getDisplayOrder()), dbResponse.getAnswer());
		}
	    }	    
	    
	    // maybe we have come in from the review screen, if so get the answers from db.
	    if (mapAnswersFromDb.size() > 0) {
		mapAnswers.putAll(mapAnswersFromDb);
	    } else {
		for (Map.Entry pairs : mapQuestions.entrySet()) {
		    mapAnswers.put(pairs.getKey().toString(), "");
		}
	    }
	}
	String currentAnswer = (String) mapAnswers.get("1");
	generalLearnerFlowDTO.setCurrentQuestionIndex(new Integer(1));
	generalLearnerFlowDTO.setCurrentAnswer(currentAnswer);
	sessionMap.put(MAP_SEQUENTIAL_ANSWERS_KEY, mapAnswers);
	generalLearnerFlowDTO.setMapAnswers(mapAnswers);
	sessionMap.put(MAP_ALL_RESULTS_KEY, mapAnswers);
    }
}
