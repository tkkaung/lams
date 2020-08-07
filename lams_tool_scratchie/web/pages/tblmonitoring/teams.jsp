<%@ include file="/common/taglibs.jsp"%>

<c:forEach var="item" items="${scratchieItems}" varStatus="i">
	<div class="panel panel-default">
			
		<div class="panel-heading">
			<h4 class="panel-title">
				<span class="float-left space-right">Q${i.index+1})</span> 
				<c:out value="${item.title}" escapeXml="false"/>
			</h4> 
		</div>
				
		<div class="panel-body">
			<div class="table-responsive">
				<table class="table table-striped">
					<tbody>
						<c:forEach var="answer" items="${item.answers}" varStatus="j">
							<c:if test="${answer.scratched}">
									
								<tr>
									<td width="5px">
										${ALPHABET[j.index]}.
									</td>
									<td>
										<c:out value="${answer.description}" escapeXml="false"/>
										
										<c:choose>
											<c:when test="${answer.correct}">
												<i class="fa fa-check-square"></i>
											</c:when>
											<c:otherwise>
												<i class="fa fa-minus-square"></i>
											</c:otherwise>
										</c:choose>
									</td>
								</tr>
										
							</c:if>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</div>
				
	</div> 
</c:forEach>
