<%@ include file="/common/taglibs.jsp"%>
<script>
	$(document).ready(function(){
		//handler for expand/collapse all button
		$("#toggle-burning-questions-button").click(function() {
			var isExpanded = eval($(this).data("expanded"));
				
			//fire the actual buttons so burning questions can be closed/expanded
			if (isExpanded) {
				$(".burning-question-title:not('.collapsed')").each(function() {
					this.click();
				});
								
			} else {
				$(".burning-question-title.collapsed").each(function() {
					this.click();
				});					
			}

			//change button label
			var newButtonLabel = isExpanded ? "<fmt:message key='label.expand.all' />" : "<fmt:message key='label.collapse.all' />";
			$(".hidden-xs", $(this)).text(newButtonLabel);

			//change button icon
			if (isExpanded) {
				$(".fa", $(this)).removeClass("fa-minus-circle").addClass("fa-plus-circle");
			} else {
				$(".fa", $(this)).removeClass("fa-plus-circle").addClass("fa-minus-circle");
			}

			//change button's data-expanded attribute
			$(this).data("expanded", !isExpanded);
		});
	});
	
	function showOptions(itemUid) {
		$('#options-show-' + itemUid).remove();
		$('#options-' + itemUid).show();
	}
</script>

<!-- Header -->
<div class="row no-gutter">
	<div class="col-xs-12">
        <button type="button" id="toggle-burning-questions-button" class="btn btn-sm btn-default pull-right voffset20" data-expanded="false">
           	<i class="fa fa-plus-circle"></i> 
           	<span class="hidden-xs">
           		<fmt:message key='label.expand.all' />
           	</span>
        </button>
            
		<h3>
			<fmt:message key="label.burning.questions"/>
		</h3>
	</div>
</div>
<!-- End header -->              

<!-- Tables -->
<div class="row no-gutter voffset10">
<div class="col-xs-12 col-md-12 col-lg-12">

	<c:forEach var="burningQuestionItemDto" items="${burningQuestionItemDtos}" varStatus="i">
		<c:set var="burningQsCount" value="${fn:length(burningQuestionItemDto.burningQuestionDtos)}"/>
		<c:set var="item" value="${burningQuestionItemDto.scratchieItem}"/>
		
		<div class="panel panel-default">
			<div class="panel-heading">
				<h4 class="panel-title panel-collapse">
					
					<!-- Don't display number prior to general burning question -->
					<c:set var="itemTitle">
						<c:choose>
							<c:when test="${burningQuestionItemDto.scratchieItem.uid == 0}">
								<c:out value="${item.title}" escapeXml="false"/>
							</c:when>
							<c:otherwise>
								Q${i.index+1}) <c:out value="${item.title}" escapeXml="false"/> 
							</c:otherwise>
						</c:choose>
					</c:set>
				
					<c:choose>
						<c:when test="${burningQuestionItemDto.scratchieItem.uid == 0 and burningQsCount == 0}">
							${itemTitle} 
						</c:when>
						<c:otherwise>
							<a data-toggle="collapse" data-itemuid="${item.uid}" class="collapsed burning-question-title">
								${itemTitle}
							</a>
							
							<span class="badge pull-right" style="margin-right: 4px">${burningQsCount}</span>
						</c:otherwise>
					</c:choose>
				</h4>
			</div>
		
			<c:if test="${burningQuestionItemDto.scratchieItem.uid != 0 or burningQsCount > 0}">
				<div id="collapse-${item.uid}" class="panel-collapse collapse">
				<div class="panel-body">
				
					<span class="burning-question-description">
						<c:out value="${item.description}" escapeXml="false"/><br /> 
					</span>
					
					<c:if test="${burningQuestionItemDto.scratchieItem.uid != 0}">
						<a id="options-show-${item.uid}" href="#" onClick="javascript:showOptions(${item.uid})">
							<fmt:message key='label.options.show' />
						</a>
						<div  id="options-${item.uid}" class="table-responsive" style="display: none">
							<table class="table table-striped table-hover">
								<c:forEach var="answer" items="${item.answers}" varStatus="j">
									<c:set var="cssClass"><c:if test='${answer.correct}'>bg-success</c:if></c:set>
									<tr>
										<td width="5px" class="${cssClass}">
											${ALPHABET[j.index]}.
										</td>
										<td class="${cssClass}">
											<c:out value="${answer.description}" escapeXml="false"/> 
										</td>
									</tr>
								</c:forEach>
							</table>
						</div>
					</c:if>
					
					<div class="table-responsive voffset10">
						<table class="table table-striped table-bordered table-hover">
							<tbody>
								<c:forEach var="burningQuestionDto" items="${burningQuestionItemDto.burningQuestionDtos}">
									<tr>
										<td class="text-nowrap">
											<c:out value="${burningQuestionDto.sessionName}" escapeXml="false"/>
										</td>
										<td>
											${burningQuestionDto.escapedBurningQuestion}
										</td>
										<td class="text-nowrap">
											<span class="badge">${burningQuestionDto.likeCount}</span> &nbsp; <i class="fa fa-thumbs-o-up" style="color:darkblue"></i>
										</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</div>
				</div>
			</c:if>
			
		</div>
	</c:forEach>    
  
</div>
</div>
