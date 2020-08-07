<%@ include file="/common/taglibs.jsp"%>

<div class="question-type">
	<c:choose>
		<c:when test="${question.multipleAnswersAllowed}">
			<fmt:message key="label.learning.choose.at.least.one.answer" />
		</c:when>
		<c:otherwise>
			<fmt:message key="label.learning.choose.one.answer" />
		</c:otherwise>
	</c:choose>
</div>

<div class="table-responsive">
	<table class="table table-hover table-condensed">
		<c:forEach var="option" items="${question.optionDtos}" varStatus="answerStatus">
			<c:set var="isCorrect" 
				   value="${(assessment.allowDiscloseAnswers 
				   			 ? question.correctAnswersDisclosed : assessment.allowRightAnswersAfterQuestion)
				    		 && (option.grade > 0)}" />
			<c:set var="isWrong"
				   value="${(assessment.allowDiscloseAnswers 
				   			 ? question.correctAnswersDisclosed : assessment.allowWrongAnswersAfterQuestion)
				    		&& (option.grade <= 0)}" />
			<tr ${isCorrect ? 'class="bg-success"' : '' }>
				<td class="complete-item-gif">
				    <c:if test="${option.answerBoolean && isCorrect}">
					    <i class="fa fa-check text-success"></i>
		            </c:if>
				    <c:if test="${option.answerBoolean && isWrong}">
					    <i class="fa fa-times text-danger"></i>	
				    </c:if>			
                </td>
				<td class="${question.prefixAnswersWithLetters?'has-radio-button-prefix':'has-radio-button'}">
					<c:choose>
						<c:when test="${question.multipleAnswersAllowed}">
							<input type="checkbox" name="question${status.index}_${option.sequenceId}" value="${true}"
		 						<c:if test="${option.answerBoolean}">checked="checked"</c:if>
								disabled="disabled"
							/>
						</c:when>
						<c:otherwise>
							<input type="radio" name="question${status.index}" value="${option.sequenceId}"
		 						<c:if test="${option.answerBoolean}">checked="checked"</c:if>
		 						disabled="disabled"
							/>
						</c:otherwise>
					</c:choose>
					<c:if test="${question.prefixAnswersWithLetters}">
			 			&nbsp;${option.formatPrefixLetter(answerStatus.index)}
 	                </c:if>	
				</td>
				
				<td ${question.prefixAnswersWithLetters?'class="has-radio-button-prefix-answer"':''}">
					<c:out value="${option.optionString}" escapeXml="false" />
				</td>
				
				<c:if test="${assessment.allowQuestionFeedback}">
					<td width=30%;">
						<c:if test="${option.answerBoolean}">
							<c:out value="${option.feedback}" escapeXml="false" />
						</c:if>
					</td>		
				</c:if>
				
			</tr>
			
			<c:if test="${assessment.allowDiscloseAnswers && question.groupsAnswersDisclosed && fn:length(sessions) > 1}">
				<c:set var="teams" value="" />
				<c:forEach var="session" items="${sessions}" varStatus="sessionStatus">
					<%-- Now groups other than this one --%>
					<c:if test="${sessionMap.toolSessionID != session.sessionId}">
						<%-- Get the needed piece of information from a complicated questionSummaries structure --%>
						<c:set var="sessionResults" 
							   value="${questionSummaries[question.uid].questionResultsPerSession[sessionStatus.index]}" />
						<c:set var="sessionResults"
						 	   value="${sessionResults[fn:length(sessionResults)-1]}" />
						<c:forEach var="sessionOption" items="${sessionResults.optionAnswers}">
							<c:if test="${sessionOption.answerBoolean && option.uid == sessionOption.optionUid}">
								<c:set var="teams">
									${teams}
									<lams:Portrait userId="${session.groupLeader.userId}"/>&nbsp;
									<c:out value="${session.sessionName}" escapeXml="true"/>&nbsp;
								</c:set>
							</c:if>
						</c:forEach>
					</c:if>
				</c:forEach>
				<c:if test="${not empty teams}">
					<tr class="selected-by-groups">
						<td></td>
						<td></td>
						<td colspan="2">
							<span><fmt:message key="label.learning.summary.selected.by" /></span> ${teams}
						</td>
					</tr>
				</c:if>
			</c:if>
			
		</c:forEach>
	</table>
</div>

<c:if test="${assessment.allowQuestionFeedback}">
	<div class="feedback">
		<c:choose>
			<c:when test="${question.answerTotalGrade >= 1}">
				<c:out value="${question.feedbackOnCorrect}" escapeXml="false" />
			</c:when>
			<c:when test="${question.answerTotalGrade > 0}">
				<c:out value="${question.feedbackOnPartiallyCorrect}" escapeXml="false" />
			</c:when>
			<c:otherwise>
				<c:out value="${question.feedbackOnIncorrect}" escapeXml="false" />
			</c:otherwise>		
		</c:choose>
	</div>
</c:if>
