<%@ include file="/common/taglibs.jsp"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
        "http://www.w3.org/TR/html4/strict.dtd">

<lams:html>
<head>
	<title><fmt:message key="label.learning.title" /></title>
	<%@ include file="/common/header.jsp"%>
</head>
<body class="stripes">

	<c:set var="sessionMapID" value="${param.sessionMapID}" />
	<c:set var="sessionMap" value="${sessionScope[sessionMapID]}" />

	<html:form action="/learning/submitReflection" method="post">
		<html:hidden property="userID" />
		<html:hidden property="sessionMapID" />

		<div id="content">
			<h1>
				${sessionMap.title}
			</h1>

			<p>
				<lams:out value="${sessionMap.reflectInstructions}" />
			</p>
			
			<%@ include file="/common/messages.jsp"%>

			<html:textarea cols="60" rows="8" property="entryText"
				styleClass="text-area" />

			<div class="space-bottom-top align-right">
				<html:submit styleClass="button">
					<fmt:message key="label.finish" />
				</html:submit>
			</div>

		</div>
	</html:form>

	<div id="footer">
	</div>
	<!--closes footer-->

</body>
</lams:html>
