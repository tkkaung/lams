<%@ include file="/common/taglibs.jsp"%>

<table class="alternative-color" id="reflections">
						
	<tr>			
		<th>
			<fmt:message key="label.reflection"/>
		</th>
	</tr>	
						
	<c:forEach var="reflectDTO" items="${reflectionsContainerDTO}">
		<tr>			
			<td valign=top class="align-left">
				<c:out value="${reflectDTO.userName}" escapeXml="true"/> <lams:Date value="${reflectDTO.date}"/>
				<br>
				<c:out value="${reflectDTO.entry}" escapeXml="true"/>

			</td>
		</tr>	
	</c:forEach>

</table>
