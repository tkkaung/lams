<%@ include file="/common/taglibs.jsp"%>

<c:set var="sessionMap" value="${sessionScope[sessionMapID]}"/>
<c:set var="summaryList" value="${sessionMap.summaryList}"/>
<c:set var="scratchie" value="${sessionMap.scratchie}"/>

<lams:html>
<lams:head>
	<title><fmt:message key="export.title" /> </title>

		<link type="text/css" href="./includes/jquery-ui-1.8.11.redmont-theme.css" rel="stylesheet">
		<link type="text/css" href="./includes/jquery.jqGrid-4.1.2.css" rel="stylesheet" />
 
		<script type="text/javascript" src="./includes/jquery-1.7.1.min.js"></script>
		<script type="text/javascript" src="./includes/jquery-ui-1.8.11.custom.min.js"></script>
 		<script type="text/javascript" src="./includes/jquery.jqGrid.locale-en.js"></script>
 		<script type="text/javascript" src="./includes/jquery.jqGrid.min.js"></script>
 		<script type="text/javascript">
		<!--	
		$(document).ready(function(){
			<c:forEach var="summary" items="${summaryList}" varStatus="status">
			
				jQuery("#list${summary.sessionId}").jqGrid({
					datatype: "local",
					height: 'auto',
					width: 900,
					shrinkToFit: true,
					
				   	colNames:['#',
							'userId',
							'sessionId',
							"<fmt:message key="label.monitoring.summary.user.name" />",
							"<fmt:message key="label.monitoring.summary.attempts" />",
						    "<fmt:message key="label.monitoring.summary.mark" />"],
						    
				   	colModel:[
				   		{name:'id',index:'id', width:0, sorttype:"int"},
				   		{name:'userId',index:'userId', width:0},
				   		{name:'sessionId',index:'sessionId', width:0},
				   		{name:'userName',index:'userName', width:400},
				   		{name:'totalAttempts',index:'totalAttempts', width:100,align:"right",sorttype:"int"},
				   		{name:'mark',index:'mark', width:100,align:"right",sorttype:"int"}		
				   	],
				   	caption: "${summary.sessionName}"
				}).hideCol("id").hideCol("userId").hideCol("sessionId");
				
	   	        <c:forEach var="user" items="${summary.users}" varStatus="i">
	   	     		jQuery("#list${summary.sessionId}").addRowData(${i.index + 1}, {
	   	   	     		id:"${i.index + 1}",
	   	   	     		userId:"${user.userId}",
	   	   	     		sessionId:"${user.session.sessionId}",
	   	   	     		userName:"${user.lastName}, ${user.firstName}",
	   	   				totalAttempts:"${user.totalAttempts}",
	   	   				mark:"<c:choose> <c:when test='${user.mark == -1}'>-</c:when> <c:otherwise>${user.mark}</c:otherwise> </c:choose>"
	   	   	   	    });
		        </c:forEach>
		   </c:forEach>
		});
		-->		
</script>

</lams:head>
<body class="stripes">

	<div id="content">

		<h1>
			${sessionMap.scratchie.title}
		</h1>
		
		<div>
			${sessionMap.scratchie.instructions}
		</div>

		<c:choose>
			<c:when test="${empty summaryList}">
				<div align="center">
					<b> <fmt:message key="message.monitoring.summary.no.session" /> </b>
				</div>	
			</c:when>
			<c:otherwise>
			
				<c:forEach var="summary" items="${summaryList}" varStatus="status">
					<div style="padding-left: 30px; <c:if test='${! status.last}'>padding-bottom: 30px;</c:if><c:if test='${ status.last}'>padding-bottom: 15px;</c:if> ">
						<c:if test="${sessionMap.isGroupedActivity}">
							<div style="padding-bottom: 5px; font-size: small;">
								<B><fmt:message key="monitoring.label.group" /></B> ${summary.sessionName}
							</div>
						</c:if>
						
						<table id="list${summary.sessionId}" class="scroll" cellpadding="0" cellspacing="0"></table>
					</div>	
					<c:if test="${! status.last}">
					
					</c:if>
				</c:forEach>
			
			</c:otherwise>
		</c:choose>

	</div>
	<!--closes content-->

	<div id="footer"></div>
	<!--closes footer-->

</body>
</lams:html>
