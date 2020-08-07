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

package org.lamsfoundation.lams.tool.mc.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.lamsfoundation.lams.questions.Answer;
import org.lamsfoundation.lams.questions.Question;
import org.lamsfoundation.lams.questions.QuestionExporter;
import org.lamsfoundation.lams.questions.QuestionParser;
import org.lamsfoundation.lams.tool.ToolAccessMode;
import org.lamsfoundation.lams.tool.mc.McAppConstants;
import org.lamsfoundation.lams.tool.mc.dto.McOptionDTO;
import org.lamsfoundation.lams.tool.mc.dto.McQuestionDTO;
import org.lamsfoundation.lams.tool.mc.model.McContent;
import org.lamsfoundation.lams.tool.mc.model.McQueContent;
import org.lamsfoundation.lams.tool.mc.service.IMcService;
import org.lamsfoundation.lams.tool.mc.util.AuthoringUtil;
import org.lamsfoundation.lams.tool.mc.web.form.McAuthoringForm;
import org.lamsfoundation.lams.util.CommonConstants;
import org.lamsfoundation.lams.util.MessageService;
import org.lamsfoundation.lams.util.WebUtil;
import org.lamsfoundation.lams.web.util.AttributeNames;
import org.lamsfoundation.lams.web.util.SessionMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Action class that controls the logic of tool behavior.
 *
 * @author Ozgur Demirtas
 */
@Controller
@RequestMapping("/authoring")
public class AuthoringController {

    private static Logger logger = Logger.getLogger(AuthoringController.class.getName());

    @Autowired
    private IMcService mcService;

    @Autowired
    @Qualifier("lamcMessageService")
    private MessageService messageService;

    @RequestMapping("/authoring")
    public String execute(@ModelAttribute McAuthoringForm mcAuthoringForm, HttpServletRequest request) {
	ToolAccessMode mode = WebUtil.readToolAccessModeAuthorDefaulted(request);
	return readDatabaseData(mcAuthoringForm, request, mode);
    }
    
    /**
     * Set the defineLater flag so that learners cannot use content while we are editing. This flag is released when
     * updateContent is called.
     */
    @RequestMapping(path = "/definelater", method = RequestMethod.POST)
    public String definelater(@ModelAttribute McAuthoringForm mcAuthoringForm, HttpServletRequest request) {
	String strToolContentID = request.getParameter(AttributeNames.PARAM_TOOL_CONTENT_ID);
	mcService.setDefineLater(strToolContentID, true);

	return readDatabaseData(mcAuthoringForm, request, ToolAccessMode.TEACHER);
    }
    
    /**
     * Common method for "unspecified" and "defineLater"
     */
    private String readDatabaseData(McAuthoringForm mcAuthoringForm, HttpServletRequest request, ToolAccessMode mode) {
	SessionMap<String, Object> sessionMap = new SessionMap<>();
	request.getSession().setAttribute(sessionMap.getSessionID(), sessionMap);
	String sessionMapId = sessionMap.getSessionID();
	request.setAttribute(McAppConstants.ATTR_SESSION_MAP_ID, sessionMapId);

	String contentFolderID = WebUtil.readStrParam(request, AttributeNames.PARAM_CONTENT_FOLDER_ID);
	sessionMap.put(AttributeNames.PARAM_CONTENT_FOLDER_ID, contentFolderID);
	String strToolContentID = request.getParameter(AttributeNames.PARAM_TOOL_CONTENT_ID);
	sessionMap.put(AttributeNames.PARAM_TOOL_CONTENT_ID, strToolContentID);
	sessionMap.put(AttributeNames.ATTR_MODE, mode);

	if ((strToolContentID == null) || (strToolContentID.equals(""))) {
	    return "McErrorBox";
	}

	McContent mcContent = mcService.getMcContent(new Long(strToolContentID));

	// if mcContent does not exist, try to use default content instead.
	if (mcContent == null) {
	    long defaultContentID = mcService.getToolDefaultContentIdBySignature(McAppConstants.TOOL_SIGNATURE);
	    mcContent = mcService.getMcContent(new Long(defaultContentID));
	    mcContent = McContent.newInstance(mcContent, new Long(strToolContentID));
	}

	// prepare form
	mcAuthoringForm.setSln(mcContent.isShowReport() ? "1" : "0");
	mcAuthoringForm.setQuestionsSequenced(mcContent.isQuestionsSequenced() ? "1" : "0");
	mcAuthoringForm.setRandomize(mcContent.isRandomize() ? "1" : "0");
	mcAuthoringForm.setDisplayAnswersFeedback(
		mcContent.isDisplayAnswers() ? "answers" : mcContent.isDisplayFeedbackOnly() ? "feedback" : "none");
	mcAuthoringForm.setShowMarks(mcContent.isShowMarks() ? "1" : "0");
	mcAuthoringForm.setUseSelectLeaderToolOuput(mcContent.isUseSelectLeaderToolOuput() ? "1" : "0");
	mcAuthoringForm.setPrefixAnswersWithLetters(mcContent.isPrefixAnswersWithLetters() ? "1" : "0");
	mcAuthoringForm.setRetries(mcContent.isRetries() ? "1" : "0");
	mcAuthoringForm.setPassmark("" + mcContent.getPassMark());
	mcAuthoringForm.setReflect(mcContent.isReflect() ? "1" : "0");
	mcAuthoringForm.setReflectionSubject(mcContent.getReflectionSubject());
	mcAuthoringForm.setTitle(mcContent.getTitle());
	mcAuthoringForm.setInstructions(mcContent.getInstructions());
	mcAuthoringForm.setEnableConfidenceLevels(mcContent.isEnableConfidenceLevels());

	List<McQuestionDTO> questionDtos = AuthoringUtil.buildDefaultQuestions(mcContent);
	sessionMap.put(McAppConstants.QUESTION_DTOS, questionDtos);

	List<McQuestionDTO> listDeletedQuestionDTOs = new ArrayList<>();
	sessionMap.put(McAppConstants.LIST_DELETED_QUESTION_DTOS, listDeletedQuestionDTOs);
	
	boolean isMcContentAttempted = mcContent.getUid() == null ? false
		: mcService.isMcContentAttempted(mcContent.getUid());
	sessionMap.put(McAppConstants.ATTR_IS_AUTHORING_RESTRICTED, isMcContentAttempted && mode.isTeacher());

	return "authoring/AuthoringTabsHolder";
    }

    /**
     * submits content into the tool database
     */
    @RequestMapping(path = "/submitAllContent", method = RequestMethod.POST)
    public String submitAllContent(@ModelAttribute McAuthoringForm mcAuthoringForm, HttpServletRequest request,
	    HttpServletResponse response) throws IOException, ServletException {

	String sessionMapId = mcAuthoringForm.getHttpSessionID();
	SessionMap<String, Object> sessionMap = (SessionMap<String, Object>) request.getSession()
		.getAttribute(sessionMapId);
	request.setAttribute(McAppConstants.ATTR_SESSION_MAP_ID, sessionMapId);
	ToolAccessMode mode = (ToolAccessMode) sessionMap.get(AttributeNames.ATTR_MODE);

	Long toolContentID = Long.parseLong((String) sessionMap.get(AttributeNames.PARAM_TOOL_CONTENT_ID));
	List<McQuestionDTO> questionDTOs = (List<McQuestionDTO>) sessionMap.get(McAppConstants.QUESTION_DTOS);
	McContent mcContent = mcService.getMcContent(toolContentID);
	List<McQuestionDTO> deletedQuestionDTOs = (List<McQuestionDTO>) sessionMap
		.get(McAppConstants.LIST_DELETED_QUESTION_DTOS);

	if (questionDTOs.isEmpty()) {
	    MultiValueMap<String, String> errorMap = new LinkedMultiValueMap<>();
	    errorMap.add("GLOBAL", messageService.getMessage("questions.none.submitted"));
	    request.setAttribute("errorMap", errorMap);
	    AuthoringController.logger.debug("errors saved: " + errorMap);
	    return "authoring/AuthoringTabsHolder";
	}

	//recalculate results in case content is edited from monitoring and it's been already attempted by a student
	boolean isAuthoringRestricted = (boolean) sessionMap.get(McAppConstants.ATTR_IS_AUTHORING_RESTRICTED);
	if (isAuthoringRestricted) {
	    Set<McQueContent> oldQuestions = mcContent.getMcQueContents();
	    mcService.releaseQuestionsFromCache(mcContent);

	    // audit log the teacher has started editing activity in monitor
	    mcService.auditLogStartEditingActivityInMonitor(toolContentID);

	    // recalculate User Answers
	    mcService.recalculateUserAnswers(mcContent, oldQuestions, questionDTOs);
	}

	// remove deleted questions
	for (McQuestionDTO deletedQuestionDTO : deletedQuestionDTOs) {
	    McQueContent removeableQuestion = mcService.getQuestionByUid(deletedQuestionDTO.getUid());
	    if (removeableQuestion != null) {
		// Set<McUsrAttempt> attempts = removeableQuestion.getMcUsrAttempts();
		// Iterator<McUsrAttempt> iter = attempts.iterator();
		// while (iter.hasNext()) {
		// McUsrAttempt attempt = iter.next();
		// iter.remove();
		// }
		// mcService.updateQuestion(removeableQuestion);
		mcContent.getMcQueContents().remove(removeableQuestion);
		mcService.removeMcQueContent(removeableQuestion);
	    }
	}
	
	// store content
	mcContent = AuthoringUtil.saveOrUpdateMcContent(mcService, request, mode, mcContent, toolContentID, questionDTOs);

	// store questions
	mcContent = mcService.createQuestions(questionDTOs, mcContent);

	if (mcContent != null) {
	    // sorts the questions by the display order
	    List<McQueContent> sortedQuestions = mcService.getAllQuestionsSorted(mcContent.getUid().longValue());
	    int displayOrder = 1;
	    for (McQueContent question : sortedQuestions) {
		McQueContent existingQuestion = mcService.getQuestionByUid(question.getUid());
		existingQuestion.setDisplayOrder(new Integer(displayOrder));
		mcService.saveOrUpdateMcQueContent(existingQuestion);
		displayOrder++;
	    }
	}

	request.setAttribute(CommonConstants.LAMS_AUTHORING_SUCCESS_FLAG, Boolean.TRUE);
	return "authoring/AuthoringTabsHolder";
    }

    /**
     * opens up an new screen within the current page for editing a question
     */
    @RequestMapping("/editQuestionBox")
    public String editQuestionBox(@ModelAttribute McAuthoringForm mcAuthoringForm, HttpServletRequest request) {

	String sessionMapId = request.getParameter(McAppConstants.ATTR_SESSION_MAP_ID);
	SessionMap<String, Object> sessionMap = (SessionMap<String, Object>) request.getSession()
		.getAttribute(sessionMapId);
	request.setAttribute(McAppConstants.ATTR_SESSION_MAP_ID, sessionMapId);
	List<McQuestionDTO> questionDtos = (List) sessionMap.get(McAppConstants.QUESTION_DTOS);

	Integer questionIndex = WebUtil.readIntParam(request, "questionIndex", true);

	McQuestionDTO questionDto = null;
	//editing existing question
	if (questionIndex != null) {
	    mcAuthoringForm.setQuestionIndex(questionIndex);

	    //find according questionDto
	    for (McQuestionDTO questionDtoIter : questionDtos) {
		Integer displayOrder = questionDtoIter.getDisplayOrder();

		if ((displayOrder != null) && displayOrder.equals(questionIndex)) {
		    questionDto = questionDtoIter;
		    break;
		}
	    }

	    //adding new question
	} else {
	    // prepare question for adding new question page
	    questionDto = new McQuestionDTO();
	    List<McOptionDTO> newOptions = new ArrayList<>();
	    McOptionDTO newOption1 = new McOptionDTO();
	    newOption1.setCorrect("Correct");
	    McOptionDTO newOption2 = new McOptionDTO();
	    newOptions.add(newOption1);
	    newOptions.add(newOption2);
	    questionDto.setOptionDtos(newOptions);
	}
	sessionMap.put(McAppConstants.QUESTION_DTO, questionDto);

	return "authoring/editQuestionBox";
    }

    /**
     * removes a question from the questions map
     */
    @RequestMapping("/removeQuestion")
    public String removeQuestion(@ModelAttribute McAuthoringForm mcAuthoringForm, HttpServletRequest request) {

	String sessionMapId = request.getParameter(McAppConstants.ATTR_SESSION_MAP_ID);
	SessionMap<String, Object> sessionMap = (SessionMap<String, Object>) request.getSession()
		.getAttribute(sessionMapId);
	request.setAttribute(McAppConstants.ATTR_SESSION_MAP_ID, sessionMapId);

	Integer questionIndexToDelete = WebUtil.readIntParam(request, "questionIndex");
	mcAuthoringForm.setQuestionIndex(questionIndexToDelete);

	List<McQuestionDTO> questionDTOs = (List<McQuestionDTO>) sessionMap.get(McAppConstants.QUESTION_DTOS);

	//exclude Question with questionIndex From List
	List<McQuestionDTO> tempQuestionDtos = new LinkedList<>();
	int queIndex = 0;
	for (McQuestionDTO questionDTO : questionDTOs) {

	    String questionText = questionDTO.getQuestion();
	    Integer displayOrder = questionDTO.getDisplayOrder();
	    if ((questionText != null) && !questionText.isEmpty()) {

		if (!displayOrder.equals(questionIndexToDelete)) {
		    ++queIndex;
		    questionDTO.setDisplayOrder(queIndex);
		    tempQuestionDtos.add(questionDTO);

		} else {
		    List<McQuestionDTO> deletedQuestionDTOs = (List<McQuestionDTO>) sessionMap
			    .get(McAppConstants.LIST_DELETED_QUESTION_DTOS);
		    deletedQuestionDTOs.add(questionDTO);
		    sessionMap.put(McAppConstants.LIST_DELETED_QUESTION_DTOS, deletedQuestionDTOs);
		}
	    }
	}
	questionDTOs = tempQuestionDtos;
	sessionMap.put(McAppConstants.QUESTION_DTOS, questionDTOs);

	return "authoring/itemlist";
    }

    /**
     * moves a question down in the list
     */
    @RequestMapping("/moveQuestionDown")
    public String moveQuestionDown(@ModelAttribute McAuthoringForm mcAuthoringForm, HttpServletRequest request) {

	String sessionMapId = request.getParameter(McAppConstants.ATTR_SESSION_MAP_ID);
	SessionMap<String, Object> sessionMap = (SessionMap<String, Object>) request.getSession()
		.getAttribute(sessionMapId);
	request.setAttribute(McAppConstants.ATTR_SESSION_MAP_ID, sessionMapId);

	Integer questionIndex = WebUtil.readIntParam(request, "questionIndex");
	mcAuthoringForm.setQuestionIndex(questionIndex);

	List<McQuestionDTO> questionDTOs = (List) sessionMap.get(McAppConstants.QUESTION_DTOS);
	questionDTOs = AuthoringController.swapQuestions(questionDTOs, questionIndex, "down");
	questionDTOs = AuthoringController.reorderQuestionDtos(questionDTOs);
	sessionMap.put(McAppConstants.QUESTION_DTOS, questionDTOs);

	return "authoring/itemlist";
    }

    @RequestMapping("/moveQuestionUp")
    public String moveQuestionUp(@ModelAttribute McAuthoringForm mcAuthoringForm, HttpServletRequest request) {

	String sessionMapId = request.getParameter(McAppConstants.ATTR_SESSION_MAP_ID);
	SessionMap<String, Object> sessionMap = (SessionMap<String, Object>) request.getSession()
		.getAttribute(sessionMapId);
	request.setAttribute(McAppConstants.ATTR_SESSION_MAP_ID, sessionMapId);

	Integer questionIndex = WebUtil.readIntParam(request, "questionIndex");
	mcAuthoringForm.setQuestionIndex(questionIndex);

	List<McQuestionDTO> questionDTOs = (List) sessionMap.get(McAppConstants.QUESTION_DTOS);
	questionDTOs = AuthoringController.swapQuestions(questionDTOs, questionIndex, "up");
	questionDTOs = AuthoringController.reorderQuestionDtos(questionDTOs);
	sessionMap.put(McAppConstants.QUESTION_DTOS, questionDTOs);

	return "authoring/itemlist";
    }

    /*
     * swappes McQuestionDTO questions in the list. Auxiliary method for moveQuestionDown() and moveQuestionUp()
     */
    private static List<McQuestionDTO> swapQuestions(List<McQuestionDTO> questionDTOs, Integer originalQuestionIndex,
	    String direction) {

	int replacedQuestionIndex = direction.equals("down") ? originalQuestionIndex + 1 : originalQuestionIndex - 1;

	McQuestionDTO mainQuestion = questionDTOs.get(originalQuestionIndex - 1);
	McQuestionDTO replacedQuestion = questionDTOs.get(replacedQuestionIndex - 1);
	if ((mainQuestion == null) || (replacedQuestion == null)) {
	    return questionDTOs;
	}

	List<McQuestionDTO> questionDtos = new LinkedList<>();

	Iterator<McQuestionDTO> iter = questionDTOs.iterator();
	while (iter.hasNext()) {
	    McQuestionDTO questionDto = iter.next();
	    McQuestionDTO tempQuestion = null;

	    if ((!questionDto.getDisplayOrder().equals(originalQuestionIndex))
		    && !questionDto.getDisplayOrder().equals(replacedQuestionIndex)) {
		// normal copy
		tempQuestion = questionDto;

	    } else if (questionDto.getDisplayOrder().equals(originalQuestionIndex)) {
		// move type 1
		tempQuestion = replacedQuestion;

	    } else if (questionDto.getDisplayOrder().equals(replacedQuestionIndex)) {
		// move type 2
		tempQuestion = mainQuestion;
	    }

	    questionDtos.add(tempQuestion);
	}

	return questionDtos;
    }

    /*
     * Auxiliary method for moveQuestionDown() and moveQuestionUp()
     */
    private static List<McQuestionDTO> reorderQuestionDtos(List<McQuestionDTO> questionDTOs) {
	List<McQuestionDTO> tempQuestionDtos = new LinkedList<>();

	int queIndex = 0;
	Iterator<McQuestionDTO> iter = questionDTOs.iterator();
	while (iter.hasNext()) {
	    McQuestionDTO questionDto = iter.next();

	    String question = questionDto.getQuestion();
	    String feedback = questionDto.getFeedback();
	    String mark = questionDto.getMark();

	    List<McOptionDTO> optionDtos = questionDto.getOptionDtos();
	    if ((question != null) && (!question.equals(""))) {
		++queIndex;

		questionDto.setQuestion(question);
		questionDto.setDisplayOrder(queIndex);
		questionDto.setFeedback(feedback);
		questionDto.setOptionDtos(optionDtos);
		questionDto.setMark(mark);
		tempQuestionDtos.add(questionDto);
	    }
	}

	return tempQuestionDtos;
    }

    @RequestMapping("/saveQuestion")
    public String saveQuestion(@ModelAttribute McAuthoringForm mcAuthoringForm, HttpServletRequest request) {

	String sessionMapId = request.getParameter(McAppConstants.ATTR_SESSION_MAP_ID);
	SessionMap<String, Object> sessionMap = (SessionMap<String, Object>) request.getSession()
		.getAttribute(sessionMapId);
	request.setAttribute(McAppConstants.ATTR_SESSION_MAP_ID, sessionMapId);

	String mark = request.getParameter("mark");

	List<McOptionDTO> options = AuthoringController.repopulateOptionDTOs(request, false);

	//remove blank options
	List<McOptionDTO> optionsWithoutEmptyOnes = new LinkedList<>();
	for (McOptionDTO optionDTO : options) {
	    String optionText = optionDTO.getCandidateAnswer();
	    if ((optionText != null) && (optionText.length() > 0)) {
		optionsWithoutEmptyOnes.add(optionDTO);
	    }
	}
	options = optionsWithoutEmptyOnes;

	List<McQuestionDTO> questionDTOs = (List<McQuestionDTO>) sessionMap.get(McAppConstants.QUESTION_DTOS);

	String newQuestion = request.getParameter("newQuestion");
	String feedback = request.getParameter("feedback");
	Integer questionIndex = WebUtil.readIntParam(request, "questionIndex", true);
	mcAuthoringForm.setQuestionIndex(questionIndex);

	if ((newQuestion != null) && (newQuestion.length() > 0)) {
	    // adding new question
	    if (questionIndex == null) {

		//finding max displayOrder
		int maxDisplayOrder = 0;
		for (McQuestionDTO questionDTO : questionDTOs) {
		    int displayOrder = new Integer(questionDTO.getDisplayOrder());
		    if (displayOrder > maxDisplayOrder) {
			maxDisplayOrder = displayOrder;
		    }
		}

		McQuestionDTO questionDTO = new McQuestionDTO();
		questionDTO.setQuestion(newQuestion);
		questionDTO.setFeedback(feedback);
		questionDTO.setDisplayOrder(maxDisplayOrder + 1);
		questionDTO.setOptionDtos(options);
		questionDTO.setMark(mark);

		questionDTOs.add(questionDTO);

		// updating existing question
	    } else {
		McQuestionDTO questionDto = null;
		for (McQuestionDTO questionDtoIter : questionDTOs) {
		    Integer displayOrder = questionDtoIter.getDisplayOrder();

		    if ((displayOrder != null) && displayOrder.equals(questionIndex)) {
			questionDto = questionDtoIter;
			break;
		    }
		}

		questionDto.setQuestion(newQuestion);
		questionDto.setFeedback(feedback);
		questionDto.setDisplayOrder(questionIndex);
		questionDto.setOptionDtos(options);
		questionDto.setMark(mark);
	    }
	} else {
	    // entry blank, not adding
	}

	request.setAttribute(McAppConstants.QUESTION_DTOS, questionDTOs);
	sessionMap.put(McAppConstants.QUESTION_DTOS, questionDTOs);

	return "authoring/itemlist";
    }

    /**
     * Parses questions extracted from IMS QTI file and adds them to currently edited question.
     */
    @RequestMapping("/saveQTI")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public String saveQTI(HttpServletRequest request) throws IOException, ServletException {

	// big part of code was taken from addSingleQuestion() and saveQuestion() methods
	String sessionMapId = request.getParameter(McAppConstants.ATTR_SESSION_MAP_ID);
	SessionMap<String, Object> sessionMap = (SessionMap<String, Object>) request.getSession()
		.getAttribute(sessionMapId);
	request.setAttribute(McAppConstants.ATTR_SESSION_MAP_ID, sessionMapId);
	String contentFolderID = (String) sessionMap.get(AttributeNames.PARAM_CONTENT_FOLDER_ID);

	List<McQuestionDTO> questionDtos = (List<McQuestionDTO>) sessionMap.get(McAppConstants.QUESTION_DTOS);
	// proper parsing
	Question[] questions = QuestionParser.parseQuestionChoiceForm(request);

	for (Question question : questions) {
	    // quietly do same verification as in other question-adding methods
	    String questionText = question.getText();
	    if (StringUtils.isBlank(questionText)) {
		logger.warn("Skipping a blank question.");
		continue;
	    }

	    questionText = QuestionParser.processHTMLField(questionText, false, contentFolderID,
		    question.getResourcesFolderPath());

	    List<McOptionDTO> optionDtos = new ArrayList<>();
	    String correctAnswer = null;
	    Integer correctAnswerScore = 1;

	    if (question.getAnswers() != null) {
		for (Answer answer : question.getAnswers()) {
		    McOptionDTO optionDto = new McOptionDTO();
		    String answerText = QuestionParser.processHTMLField(answer.getText(), false, contentFolderID,
			    question.getResourcesFolderPath());
		    if (answerText == null) {
			logger.warn("Skipping a blank answer");
			continue;
		    }
		    if ((correctAnswer != null) && correctAnswer.equals(answerText)) {
			logger.warn("Skipping an answer with same text as the correct answer: " + answerText);

			continue;
		    }

		    optionDto.setCandidateAnswer(answerText);

		    if ((answer.getScore() != null) && (answer.getScore() > 0)) {
			if (correctAnswer == null) {
			    optionDto.setCorrect("Correct");
			    correctAnswer = optionDto.getCandidateAnswer();
			    // marks are integer numbers
			    correctAnswerScore = Math.min(new Double(Math.ceil(answer.getScore())).intValue(), 10);
			} else {
			    // there can be only one correct answer in a MCQ question
			    logger.warn(
				    "Choosing only first correct answer, despite another one was found: " + answerText);
			    optionDto.setCorrect("Incorrect");
			}
		    } else {
			optionDto.setCorrect("Incorrect");
		    }

		    optionDtos.add(optionDto);
		}
	    }

	    if (correctAnswer == null) {
		logger.warn("No correct answer found for question: " + questionText);
		continue;
	    }

	    McQuestionDTO questionDto = new McQuestionDTO();
	    questionDto.setDisplayOrder(questionDtos.size() + 1);
	    questionDto.setQuestion(questionText);
	    questionDto.setFeedback(QuestionParser.processHTMLField(question.getFeedback(), true, null, null));
	    questionDto.setOptionDtos(optionDtos);
	    questionDto.setMark(correctAnswerScore.toString());

	    questionDtos.add(questionDto);

	    if (logger.isDebugEnabled()) {
		logger.debug("Added question: " + questionText);
	    }
	}

	sessionMap.put(McAppConstants.QUESTION_DTOS, questionDtos);

	return "authoring/itemlist";
    }

    /**
     * Prepares MC questions for QTI packing
     */
    @RequestMapping("/exportQTI")
    public String exportQTI(HttpServletRequest request, HttpServletResponse response) {
	String sessionMapId = request.getParameter(McAppConstants.ATTR_SESSION_MAP_ID);
	SessionMap<String, Object> sessionMap = (SessionMap<String, Object>) request.getSession()
		.getAttribute(sessionMapId);
	request.setAttribute(McAppConstants.ATTR_SESSION_MAP_ID, sessionMapId);

	List<McQuestionDTO> questionDtos = (List<McQuestionDTO>) sessionMap.get(McAppConstants.QUESTION_DTOS);
	List<Question> questions = new LinkedList<>();

	for (McQuestionDTO mcQuestion : questionDtos) {
	    Question question = new Question();

	    question.setType(Question.QUESTION_TYPE_MULTIPLE_CHOICE);
	    question.setTitle("Question " + mcQuestion.getDisplayOrder());
	    question.setText(mcQuestion.getQuestion());
	    question.setFeedback(mcQuestion.getFeedback());
	    List<Answer> answers = new ArrayList<>();

	    for (McOptionDTO mcAnswer : mcQuestion.getOptionDtos()) {
		Answer answer = new Answer();
		answer.setText(mcAnswer.getCandidateAnswer());
		answer.setScore(
			"Correct".equalsIgnoreCase(mcAnswer.getCorrect()) ? Float.parseFloat(mcQuestion.getMark()) : 0);

		answers.add(answer);
		question.setAnswers(answers);
	    }

	    // put the question in the right place
	    questions.add(mcQuestion.getDisplayOrder() - 1, question);
	}

	String title = request.getParameter("title");
	QuestionExporter exporter = new QuestionExporter(title, questions.toArray(Question.QUESTION_ARRAY_TYPE));
	exporter.exportQTIPackage(request, response);

	return null;
    }

    @RequestMapping("/moveCandidateUp")
    public String moveCandidateUp(HttpServletRequest request) {
	String sessionMapId = request.getParameter(McAppConstants.ATTR_SESSION_MAP_ID);
	SessionMap<String, Object> sessionMap = (SessionMap<String, Object>) request.getSession()
		.getAttribute(sessionMapId);
	request.setAttribute(McAppConstants.ATTR_SESSION_MAP_ID, sessionMapId);

	String candidateIndex = request.getParameter("candidateIndex");
	request.setAttribute("candidateIndex", candidateIndex);

	List<McOptionDTO> optionDtos = AuthoringController.repopulateOptionDTOs(request, false);

	//moveAddedCandidateUp
	McQuestionDTO questionDto = (McQuestionDTO) sessionMap.get(McAppConstants.QUESTION_DTO);
	List<McOptionDTO> listCandidates = new LinkedList<>();
	listCandidates = AuthoringController.swapOptions(optionDtos, candidateIndex, "up");
	questionDto.setOptionDtos(listCandidates);
	sessionMap.put(McAppConstants.QUESTION_DTO, questionDto);

	return "authoring/candidateAnswersList";
    }

    @RequestMapping("/moveCandidateDown")
    public String moveCandidateDown(HttpServletRequest request) {
	String sessionMapId = request.getParameter(McAppConstants.ATTR_SESSION_MAP_ID);
	SessionMap<String, Object> sessionMap = (SessionMap<String, Object>) request.getSession()
		.getAttribute(sessionMapId);
	request.setAttribute(McAppConstants.ATTR_SESSION_MAP_ID, sessionMapId);

	String candidateIndex = request.getParameter("candidateIndex");
	request.setAttribute("candidateIndex", candidateIndex);

	List<McOptionDTO> optionDtos = AuthoringController.repopulateOptionDTOs(request, false);

	//moveAddedCandidateDown
	McQuestionDTO questionDto = (McQuestionDTO) sessionMap.get(McAppConstants.QUESTION_DTO);
	List<McOptionDTO> swapedOptions = new LinkedList<>();
	swapedOptions = AuthoringController.swapOptions(optionDtos, candidateIndex, "down");
	questionDto.setOptionDtos(swapedOptions);
	sessionMap.put(McAppConstants.QUESTION_DTO, questionDto);

	return "authoring/candidateAnswersList";
    }

    /*
     * swaps options in the list
     */
    private static List<McOptionDTO> swapOptions(List<McOptionDTO> optionDtos, String optionIndex, String direction) {

	int intOptionIndex = new Integer(optionIndex).intValue();
	int intOriginalOptionIndex = intOptionIndex;

	int replacedOptionIndex = 0;
	if (direction.equals("down")) {
	    replacedOptionIndex = ++intOptionIndex;
	} else {
	    replacedOptionIndex = --intOptionIndex;
	}

	McOptionDTO mainOption = AuthoringUtil.getOptionAtDisplayOrder(optionDtos, intOriginalOptionIndex);
	McOptionDTO replacedOption = AuthoringUtil.getOptionAtDisplayOrder(optionDtos, replacedOptionIndex);
	if ((mainOption == null) || (replacedOption == null)) {
	    return optionDtos;
	}

	List<McOptionDTO> newOptionDtos = new LinkedList<>();

	int queIndex = 1;
	for (McOptionDTO option : optionDtos) {

	    McOptionDTO tempOption = new McOptionDTO();
	    if ((!new Integer(queIndex).toString().equals(new Integer(intOriginalOptionIndex).toString()))
		    && !new Integer(queIndex).toString().equals(new Integer(replacedOptionIndex).toString())) {
		// normal copy
		tempOption = option;
	    } else if (new Integer(queIndex).toString().equals(new Integer(intOriginalOptionIndex).toString())) {
		// move type 1
		tempOption = replacedOption;
	    } else if (new Integer(queIndex).toString().equals(new Integer(replacedOptionIndex).toString())) {
		// move type 2
		tempOption = mainOption;
	    }

	    newOptionDtos.add(tempOption);
	    queIndex++;
	}

	return newOptionDtos;
    }

    @RequestMapping("/removeCandidate")
    public String removeCandidate(HttpServletRequest request) {

	String sessionMapId = request.getParameter(McAppConstants.ATTR_SESSION_MAP_ID);
	SessionMap<String, Object> sessionMap = (SessionMap<String, Object>) request.getSession()
		.getAttribute(sessionMapId);
	request.setAttribute(McAppConstants.ATTR_SESSION_MAP_ID, sessionMapId);

	String candidateIndexToRemove = request.getParameter("candidateIndex");
	request.setAttribute("candidateIndex", candidateIndexToRemove);

	// removeAddedCandidate
	McQuestionDTO questionDto = (McQuestionDTO) sessionMap.get(McAppConstants.QUESTION_DTO);

	List<McOptionDTO> optionDtos = AuthoringController.repopulateOptionDTOs(request, false);
	List<McOptionDTO> listFinalCandidatesDTO = new LinkedList<>();
	int caIndex = 0;
	for (McOptionDTO mcOptionDTO : optionDtos) {
	    caIndex++;

	    if (caIndex != new Integer(candidateIndexToRemove).intValue()) {
		listFinalCandidatesDTO.add(mcOptionDTO);
	    }
	}

	questionDto.setOptionDtos(listFinalCandidatesDTO);
	sessionMap.put(McAppConstants.QUESTION_DTO, questionDto);

	return "authoring/candidateAnswersList";
    }

    @RequestMapping("/newCandidateBox")
    public String newCandidateBox(HttpServletRequest request) {

	String sessionMapId = request.getParameter(McAppConstants.ATTR_SESSION_MAP_ID);
	SessionMap<String, Object> sessionMap = (SessionMap<String, Object>) request.getSession()
		.getAttribute(sessionMapId);
	request.setAttribute(McAppConstants.ATTR_SESSION_MAP_ID, sessionMapId);

	String candidateIndex = request.getParameter("candidateIndex");
	request.setAttribute("candidateIndex", candidateIndex);

	List<McOptionDTO> optionDtos = AuthoringController.repopulateOptionDTOs(request, true);

	//newAddedCandidateBox
	McQuestionDTO questionDto = (McQuestionDTO) sessionMap.get(McAppConstants.QUESTION_DTO);
	questionDto.setOptionDtos(optionDtos);
	sessionMap.put(McAppConstants.QUESTION_DTO, questionDto);

	return "authoring/candidateAnswersList";
    }

    /**
     * repopulateOptionsBox
     */
    private static List<McOptionDTO> repopulateOptionDTOs(HttpServletRequest request, boolean isAddBlankOptions) {

	String correct = request.getParameter("correct");

	/* check this logic again */
	int intCorrect = 0;
	if (correct != null) {
	    intCorrect = new Integer(correct).intValue();
	}

	List<McOptionDTO> optionDtos = new LinkedList<>();
	for (int i = 0; i < McAppConstants.MAX_OPTION_COUNT; i++) {
	    String optionText = request.getParameter("ca" + i);
	    Long optionUid = WebUtil.readLongParam(request, "caUid" + i, true);

	    String isCorrect = "Incorrect";

	    if (i == intCorrect) {
		isCorrect = "Correct";
	    }

	    if (optionText != null) {
		McOptionDTO optionDTO = new McOptionDTO();
		optionDTO.setUid(optionUid);
		optionDTO.setCandidateAnswer(optionText);
		optionDTO.setCorrect(isCorrect);
		optionDtos.add(optionDTO);
	    }
	}

	if (isAddBlankOptions) {
	    McOptionDTO optionDTO = new McOptionDTO();
	    optionDTO.setCandidateAnswer("");
	    optionDTO.setCorrect("Incorrect");
	    optionDtos.add(optionDTO);
	}

	return optionDtos;
    }

}
