<%@ include file="/common/taglibs.jsp"%>
<c:set var="lams"><lams:LAMSURL/></c:set>
<c:set var="tool"><lams:WebAppURL/></c:set>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
		<script type="text/javascript">
			var reqIDVar = new Date();
			window.parent.location.href  = "${tool}/pages/learning/learning.jsp?mode=${mode}&reqID="+reqIDVar.getTime();
		</script>
		<div style="align:center">
			<c:if test="${addType == 1}">
				Add URL success, <a href="<c:url value='/pages/learning/addurl.jsp'/>?mode=${mode}" target="newResourceFrame">click here to return</a>.
			</c:if>
			<c:if test="${addType == 2}">
				Add File success, <a href="<c:url value='/pages/learning/addfile.jsp'/>?mode=${mode}" target="newResourceFrame">click here to return</a>.
			</c:if>
		</div>
	</body>
</html>