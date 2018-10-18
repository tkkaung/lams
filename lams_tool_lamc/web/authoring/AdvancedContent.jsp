<%@ include file="/common/taglibs.jsp"%>
<c:set var="formBean" value="<%= request.getAttribute(org.apache.struts.taglib.html.Constants.BEAN_KEY) %>" />
<c:set var="sessionMap" value="${sessionScope[sessionMapId]}" />

<lams:SimplePanel titleKey="label.select.leader">
	<div class="checkbox">
		<label for="useSelectLeaderToolOuput">
			<html:checkbox property="useSelectLeaderToolOuput" value="1" styleId="useSelectLeaderToolOuput" onclick="clickSelectLeaderToolOuputHandler();"/>
			<fmt:message key="label.use.select.leader.tool.output" />
		</label>
	</div>
</lams:SimplePanel>

<lams:SimplePanel titleKey="label.question.options">
	<div class="checkbox">
		<label for="prefixAnswersWithLetters">
			<html:checkbox property="prefixAnswersWithLetters" value="1" styleId="prefixAnswersWithLetters"/>
			<fmt:message key="label.prefix.sequential.letters.for.each.answer" />
		</label>
	</div>
	
	<div class="checkbox">
		<label for="questionsSequenced">
			<html:checkbox property="questionsSequenced" value="1" styleId="questionsSequenced"/>
			<fmt:message key="radiobox.onepq" />
		</label>
	</div>
	
	<div class="checkbox">
		<label for="showMarks">
			<html:checkbox property="showMarks" value="1" styleId="showMarks"/>
			<fmt:message key="label.showMarks" />
		</label>
	</div>
	
	<div class="checkbox">
		<label for="randomize">
			<html:checkbox property="randomize" value="1" styleId="randomize"/>
			<fmt:message key="label.randomize" />
		</label>
	</div>
	
	<div class="checkbox">
		<label for="retries">
			<html:checkbox property="retries" value="1" styleId="retries"/>
			<fmt:message key="radiobox.retries" />
		</label>
	</div>
	
	<div class="form-inline loffset20">
		<div id="passmark-container" class="form-group"
				<c:if test="${formBean.retries != 1}">style="display:none;"</c:if>>
			<select id="passmark" name="passmark"  class="form-control input-sm">
				<c:if test="${formBean.passmark != 0}">
					<option value="${formBean.passmark}" SELECTED>
						${formBean.passmark}
					</option>
				</c:if>
			</select>
			<fmt:message key="radiobox.passmark" />
		</div>
	</div>

	<div class="checkbox">
		<label for="enable-confidence-levels">
			<html:checkbox property="enableConfidenceLevels" value="true" styleId="enable-confidence-levels"/>
			<fmt:message key="label.enable.confidence.levels" />
		</label>
	</div>

	<lams:SimplePanel panelBodyClass="panel-body-sm" titleKey="label.feedback.simple">
	<div class="radio">
		<label for="displayAnswers">
			<html:radio property="displayAnswersFeedback" value="answers" styleId="displayAnswers"/>
			<fmt:message key="label.displayAnswers" />		
		</label>
	</div>

	<div class="radio form-inline">
		<label for="displayFeedback">
			<html:radio property="displayAnswersFeedback" value="feedback" styleId="displayFeedback"/>
			<fmt:message key="label.displayFeedbackOnly" />
		</label>
	</div>

	<div class="radio form-inline">
		<label for="displayNoAnswersOrFeedback">
			<html:radio property="displayAnswersFeedback" value="none" styleId="displayNoAnswersOrFeedback"/>
			<fmt:message key="label.displayNoAnswersOrFeedback" />
		</label>
	</div>
	</lams:SimplePanel>
	
</lams:SimplePanel>

<lams:OutcomeAuthor toolContentId="${sessionMap.toolContentID}" />

<lams:SimplePanel titleKey="label.activity.completion">
	<div class="checkbox">
		<label for="reflect">
			<html:checkbox property="reflect" value="1" styleClass="noBorder" styleId="reflect"/>
			<fmt:message key="label.reflect" />
		</label>
	</div>
	<div class="form-group">
		<html:textarea property="reflectionSubject" styleId="reflectInstructions"  styleClass="form-control" rows="3"/>
	</div>
</lams:SimplePanel>

<script type="text/javascript">
	//automatically turn on refect option if there are text input in refect instruction area
	var ra = document.getElementById("reflectInstructions");
	var rao = document.getElementById("reflect");
	function turnOnRefect(){
		if(isEmpty(ra.value)){
		//turn off	
			rao.checked = false;
		}else{
		//turn on
			rao.checked = true;		
		}
	}

	ra.onkeyup=turnOnRefect;
</script>
