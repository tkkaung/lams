<%@ include file="/common/taglibs.jsp" %>
<c:set var="ctxPath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="listSize" value="${fn:length(instructionList)}"/>
<div id="instructionArea">
	<form id="instructionForm">
	<input type="hidden" name="instructionCount" id="instructionCount">
	<table border="0" style="width:400px">
		<tr>
			<td colspan="5">
				<fmt:message key="label.authoring.basic.resource.instructions"/>
				<a href="javascript:;" onclick="addInstruction()" class="button"><fmt:message key="label.authoring.basic.resource.add.instruction"/></a>
				<img src="${ctxPath}/includes/images/indicator.gif" style="display:none" id="instructionArea_Busy" />
			</td>
		</tr>
		<c:forEach var="item" items="${instructionList}" varStatus="status">
			<tr id="instructionItem${status.index}">
				<td width="3px">${status.index+1}</td>
				<td width="100px"><input type="text" name="instructionItemDesc${status.index}" id="instructionItemDesc${status.index}" size="60" value="${item}"></td>
				
				<td width="20px">
					<%-- Don't display down icon if last line --%>
					<c:if test="${0 != status.index}">
						<a href="javascript:;" onclick="upItem('${status.index}')">
							<img src="<html:rewrite page='/includes/images/uparrow.gif'/>" border="0">
						</a>
					</c:if>
				</td>
				<td width="20px">
					<%-- Don't display down icon if last line --%>
					<c:if test="${listSize != status.count}">
						<a href="javascript:;" onclick="downItem('${status.index}','${listSize}')">
							<img src="<html:rewrite page='/includes/images/downarrow.gif'/>" border="0"> 
						</a>
					</c:if>
				</td>
				<td width="30px" align="center">
					<a href="javascript:;" onclick="removeInstruction('${status.index}')">
						<img src="<html:rewrite page='/includes/images/cross.gif'/>" border="0">
					</a>
				</td>
			</tr>
		</c:forEach>
	</table>
	</form>
</div>

<%-- This script will adjust resource item input area height according to the new instruction item amount. --%>
<script type="text/javascript">
	$("instructionCount").value="${listSize}";
	var obj = window.top.document.getElementById('reourceInputArea');
	obj.style.height=obj.contentWindow.document.body.scrollHeight+'px';
</script>
