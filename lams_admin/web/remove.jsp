<!DOCTYPE html>

<%@ include file="/taglibs.jsp"%>

<lams:html>
<lams:head>
	<c:set var="title"><tiles:getAsString name="title"/></c:set>
	<c:set var="title"><fmt:message key="${title}"/></c:set>
	<title>${title}</title>

	<lams:css/>
	<link rel="stylesheet" href="<lams:LAMSURL/>/admin/css/admin.css" type="text/css" media="screen">
	<link rel="stylesheet" href="<lams:LAMSURL/>css/jquery-ui-smoothness-theme.css" type="text/css" media="screen">
	<script language="JavaScript" type="text/JavaScript" src="<lams:LAMSURL/>/includes/javascript/changeStyle.js"></script>
	<link rel="shortcut icon" href="<lams:LAMSURL/>/favicon.ico" type="image/x-icon" />
</lams:head>
    
<body class="stripes">
	<c:set var="subtitle"><tiles:getAsString name="subtitle" ignore="true"/></c:set>	
	<c:if test="${not empty subtitle}">
		<c:set var="title">${title}: <fmt:message key="${subtitle}"/></c:set>
	</c:if>
	
	<c:set var="help"><tiles:getAsString name='help'  ignore="true"/></c:set>
	<c:choose>
		<c:when test="${not empty help}">
			<c:set var="help"><lams:help style="small" page="${help}" /></c:set>
			<lams:Page type="admin" title="${title}" titleHelpURL="${help}">
				<form>

				<c:if test="${empty orgId}">
					<c:url var="cancel" value="usersearch.do" />
				</c:if>
				<c:if test="${not empty orgId}">
					<c:url var="cancel" value="usermanage.do">
						<c:param name="org" value="${orgId}" />
					</c:url>
				</c:if>
				
				<c:if test="${method == disable}">
					<div class="panel panel-default" >
						<div class="panel-heading">
							<span class="panel-title">
								<fmt:message key="admin.user.disable"/>
							</span>
						</div>
						<div class="panel-body">     
							<p>
								<fmt:message key="msg.disable.user.1"/>&nbsp;&nbsp;
								<fmt:message key="msg.disable.user.2"/>&nbsp;&nbsp;
								<fmt:message key="msg.disable.user.3"/><br />
								<fmt:message key="msg.disable.user.4"/>
							</p>
							<c:url var="disableaction" value="user/disable.do">
								<c:param name="userId" value="${userId}" />
								<c:param name="orgId" value="${orgId}" />
							</c:url>
							<div class="pull-right">
								<input class="btn btn-primary" type="button" value="Disable" onClick="javascript:document.location='<c:out value="${disableaction}"/>'" />
								<input class="btn btn-default" type="button" value="Cancel" onClick="javascript:document.location='<c:out value="${cancel}"/>'" />
							</div>
						</div>
					</div>	
				</c:if>
				
				<c:if test="${method == delete}">
					<div class="panel panel-default" >
						<div class="panel-heading">
							<span class="panel-title">
								<fmt:message key="admin.user.delete"/>
							</span>
						</div>
						<div class="panel-body">     
							<p><fmt:message key="msg.delete.user.1"/>&nbsp;&nbsp;<fmt:message key="msg.delete.user.2"/></p>
							<c:url var="deleteaction" value="user/delete.do">
									<c:param name="userId" value="${userId}" />
									<c:param name="orgId" value="${orgId}" />
								</c:url>
							<div class="pull-right">
								<input class="btn btn-default" type="button" value="Delete" onClick="javascript:document.location='<c:out value="${deleteaction}"/>'" />
								<input class="btn btn-default" type="button" value="Cancel" onClick="javascript:document.location='<c:out value="${cancel}"/>'" />
							</div>
						</div>
					</div>
				</c:if>
				</form>
				
				<div id="footer"/>
			</lams:Page>
		</c:when>
		<c:otherwise>
			<lams:Page type="admin" title="${title}" >
				
			<form>

			<c:if test="${empty orgId}">
				<c:url var="cancel" value="usersearch.do" />
			</c:if>
			<c:if test="${not empty orgId}">
				<c:url var="cancel" value="usermanage.do">
					<c:param name="org" value="${orgId}" />
				</c:url>
			</c:if>
			
			<c:if test="${method == disable}">
				<div class="panel panel-default" >
					<div class="panel-heading">
						<span class="panel-title">
							<fmt:message key="admin.user.disable"/>
						</span>
					</div>
					<div class="panel-body">     
						<p>
							<fmt:message key="msg.disable.user.1"/>&nbsp;&nbsp;
							<fmt:message key="msg.disable.user.2"/>&nbsp;&nbsp;
							<fmt:message key="msg.disable.user.3"/><br />
							<fmt:message key="msg.disable.user.4"/>
						</p>
						<c:url var="disableaction" value="user/disable.do">
							<c:param name="userId" value="${userId}" />
							<c:param name="orgId" value="${orgId}" />
						</c:url>
						<div class="pull-right">
							<input class="btn btn-primary" type="button" value="Disable" onClick="javascript:document.location='<c:out value="${disableaction}"/>'" />
							<input class="btn btn-default" type="button" value="Cancel" onClick="javascript:document.location='<c:out value="${cancel}"/>'" />
						</div>
					</div>
				</div>	
			</c:if>
			
			<c:if test="${method == delete}">
				<div class="panel panel-default" >
					<div class="panel-heading">
						<span class="panel-title">
							<fmt:message key="admin.user.delete"/>
						</span>
					</div>
					<div class="panel-body">     
						<p><fmt:message key="msg.delete.user.1"/>&nbsp;&nbsp;<fmt:message key="msg.delete.user.2"/></p>
						<c:url var="deleteaction" value="user/delete.do">
								<c:param name="userId" value="${userId}" />
								<c:param name="orgId" value="${orgId}" />
							</c:url>
						<div class="pull-right">
							<input class="btn btn-default" type="button" value="Delete" onClick="javascript:document.location='<c:out value="${deleteaction}"/>'" />
							<input class="btn btn-default" type="button" value="Cancel" onClick="javascript:document.location='<c:out value="${cancel}"/>'" />
						</div>
					</div>
				</div>
			</c:if>
			</form>
			
			<div id="footer"/>
			</lams:Page>
		</c:otherwise>
	</c:choose>


</body>
</lams:html>


