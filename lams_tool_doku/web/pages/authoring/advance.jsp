<%@ include file="/common/taglibs.jsp"%>

<!-- Advance Tab Content -->

<lams:SimplePanel titleKey="label.select.leader">
	<div class="checkbox">
		<label for="useSelectLeaderToolOuput">
			<form:checkbox path="dokumaran.useSelectLeaderToolOuput" value="1" id="useSelectLeaderToolOuput"/>
			<fmt:message key="label.use.select.leader.tool.output" />
		</label>
	</div>
	
	<div class="checkbox">
		<label for="allowMultipleLeaders">
			<form:checkbox path="dokumaran.allowMultipleLeaders" value="1" id="allowMultipleLeaders"
				disabled="${!authoringForm.dokumaran.useSelectLeaderToolOuput}"/>
			<fmt:message key="label.allow.multiple.leaders" />
		</label>
	</div>
</lams:SimplePanel>

<lams:SimplePanel titleKey="label.resource.options">

	<div class="form-inline">
		<label for="timeLimit">
			<fmt:message key="label.time.limit" />&nbsp;
			<form:input path="dokumaran.timeLimit" size="3" id="timeLimit" cssClass="form-control input-sm"/>
		</label>
	</div>
	
	<div class="checkbox">
		<label for="showChat">
			<form:checkbox path="dokumaran.showChat" id="showChat"/>
			<fmt:message key="label.show.chat" />
		</label>
	</div>
	
	<div class="checkbox">
		<label for="showLineNumbers">
			<form:checkbox path="dokumaran.showLineNumbers" id="showLineNumbers"/>
			<fmt:message key="label.show.line.numbers" />
		</label>
	</div>
	
	<div class="checkbox">
		<label for="shared-pad-id-on">
			<input type="checkbox" id="shared-pad-id-on" 
				<c:if test="${not empty authoringForm.dokumaran.sharedPadId}">checked="checked"</c:if>/>
			<fmt:message key="label.shared.pad.id" />
		</label>
	</div>
	
	<div class="form-group">
		<form:input path="dokumaran.sharedPadId" cssClass="form-control" id="shared-pad-id" />
	</div>

</lams:SimplePanel>

<lams:OutcomeAuthor toolContentId="${authoringForm.dokumaran.contentId}" />

<lams:SimplePanel titleKey="label.activity.completion">

	<div class="checkbox">
		<label for="lockWhenFinished">
			<form:checkbox path="dokumaran.lockWhenFinished" id="lockWhenFinished" />
			<fmt:message key="label.authoring.advance.lock.on.finished" />
		</label>
	</div>
	
	<div class="checkbox">
		<label for="reflect-on">
			<form:checkbox path="dokumaran.reflectOnActivity" id="reflect-on"/>
			<fmt:message key="label.authoring.advanced.reflectOnActivity" />
		</label>
	</div>
	
	<div class="form-group">
		<form:textarea path="dokumaran.reflectInstructions" cssClass="form-control" id="reflect-instructions" rows="3" />
	</div>
	
</lams:SimplePanel>

<script type="text/javascript">
	//automatically turn on refect option if there are text input in refect instruction area
	$('#reflect-instructions').keyup(function(){
		$('#reflect-on').prop('checked', !isEmpty($(this).val()));
	});
	
	//automatically turn on shared-pad-id-on option if there are text input in shared-pad-id area
	$('#shared-pad-id').keyup(function(){
		$('#shared-pad-id-on').prop('checked', !isEmpty($(this).val()));
	});
	
	//automatically turn on shared-pad-id-on option if there are text input in shared-pad-id area
	$('#useSelectLeaderToolOuput').change(function(){
		$('#allowMultipleLeaders').prop('disabled', !$('#allowMultipleLeaders').prop('disabled'));
	});
</script>
