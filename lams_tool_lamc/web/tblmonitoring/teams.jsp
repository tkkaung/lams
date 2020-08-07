<%@ include file="/common/taglibs.jsp"%>

<c:forEach var="userAttempt" items="${userAttempts}" varStatus="i">
	<div class="panel panel-default">
		<div class="panel-heading">
			<h4 class="panel-title">
				<span class="float-left space-right">Q${i.index+1})</span> 
				<c:out value="${userAttempt.mcQueContent.question}" escapeXml="false"/>
			</h4> 
		</div>
	              
		<div class="panel-body">
			<div class="table-responsive">
				<table class="table table-striped">
					<tbody>
						<tr>
							<td width="5px">
								${ALPHABET[userAttempt.mcOptionsContent.displayOrder-1]}.
							</td>
							<td>
								<c:out value="${userAttempt.mcOptionsContent.mcQueOptionText}" escapeXml="false"/>
												
								<c:choose>
									<c:when test="${userAttempt.mcOptionsContent.correctOption}">
										<i class="fa fa-check-square"></i>
									</c:when>
									<c:otherwise>
										<i class="fa fa-minus-square"></i>
									</c:otherwise>
								</c:choose>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</c:forEach>