<!DOCTYPE html>
		
<%@ include file="/common/taglibs.jsp"%>

<div class="panel panel-default">
<div class="panel-heading">
	<div class="panel-title"><fmt:message key="label.authoring.conditions.add.condition" /></div>
</div>

<div class="panel-body">	

	 <%-- For some reason Spring MVC consumes this first form and only renders the second one.
	   If this redundant form is removed, the other one would be consumed, so this one needs to stay --%>
	 <form:form modelAttribute="surveyConditionForm">
	 </form:form>
	<form:form action="../authoringCondition/saveOrUpdateCondition.do" method="post" modelAttribute="surveyConditionForm" id="surveyConditionForm" focus="displayName" >
		<lams:errors/>
		<form:hidden path="orderId" />
		
		<div class="form-group">
		    <label for="displayName"><fmt:message key="label.authoring.conditions.condition.name" /></label>
       		<form:input path="displayName" size="51"  cssClass="form-control"/>
		</div>

		<%-- Text search form fields are being included --%>
		<lams:TextSearch sessionMapID="${sessionMapID}" />
		<h4><fmt:message key="textsearch.questions" /></h4>
		<c:forEach var="itemE" items="${surveyConditionForm.possibleItems}">
			<div class="checkbox">
			<label>
		  	<form:checkbox path="selectedItems" value="${itemE.value }"/>
		    <c:out value="${itemE.key}" />
		    </label>
		    </div>
		</c:forEach>
	
	</form:form>

	<div class="voffset5 pull-right">
	    <a href="#" onclick="hideConditionMessage();" class="btn btn-default btn-xs"><fmt:message key="button.cancel" /> </a>
	    <a href="#" onclick="submitCondition();" class="btn btn-default btn-xs"><fmt:message key="label.save" /> </a>
	</div>

</div>
</div>