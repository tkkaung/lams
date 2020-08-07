<!DOCTYPE html>
<%@ include file="/common/taglibs.jsp"%>
<c:set var="lams"><lams:LAMSURL /></c:set>
<c:set var="sessionMap" value="${sessionScope[sessionMapID]}"/>
<c:set var="scratchie" value="${sessionMap.scratchie}"/>

<lams:html>
	<lams:head>
		<%@ include file="/common/header.jsp"%>
		
		<link type="text/css" href="${lams}css/jquery-ui-bootstrap-theme.css" rel="stylesheet">
		<link href="${lams}css/free.ui.jqgrid.min.css" rel="stylesheet" type="text/css">
		<style type="text/css">
			/* remove jqGrid borders */
			.ui-jqgrid {
				border-left-style: none !important;
				border-right-style: none !important;
				border-bottom-style: none !important;
			}
			.ui-jqgrid tr {
				border-left-style: none !important;
			}
			.ui-jqgrid td {
				border-style: none !important;
			}
			
			/* remove jqGrid border radius */
			.ui-jqgrid.ui-jqgrid-bootstrap {
			    border-radius:0;
			    -moz-border-radius:0;
			    -webkit-border-radius:0;
			    -khtml-border-radius:0;
			}
		</style>
		
		<script type="text/javascript" src="${lams}includes/javascript/jquery.js"></script>
 		<script type="text/javascript" src="${lams}includes/javascript/free.jquery.jqgrid.min.js"></script>
  	    <script>
	  	  	$(document).ready(function(){
	  			<c:forEach var="summary" items="${summaryList}" varStatus="status">
		  			
	  				jQuery("#session${summary.sessionId}").jqGrid({
	  					datatype: "local",
	  					autoencode:false,
	  					rowNum: 10000,
	  					height: 'auto',
	  					width: '100%',
	  					guiStyle: "bootstrap",
	  					iconSet: 'fontAwesome',
	  				   	colNames:["<fmt:message key="label.monitoring.summary.answer" />"
		  			  	   	        <c:forEach var="answer" items="${summary.answers}" varStatus="i">
		  			  	   	 			,"<fmt:message key="label.monitoring.summary.choice" />&nbsp;${i.index + 1}"
					  		        </c:forEach>
	  							],
	  						    
	  				   	colModel:[
							{name:'answer',index:'userName', width:180}
  			  	   	        <c:forEach var="answer" items="${summary.answers}" varStatus="i">
			  	   	 			,{name:'choice${i.index}', index:'${i.index}choice', align:"center", sorttype:"int"}
		  		        	</c:forEach>		
	  				   	],
	  				   	
	  				   	multiselect: false,
	  				   	//caption: "<fmt:message key="label.monitoring.item.summary.group" /> ${summary.sessionName}"
	  				});
	  				
	  	   	        <c:forEach var="answer" items="${summary.answers}" varStatus="i">
	  	   	     		jQuery("#session${summary.sessionId}").addRowData(${i.index + 1}, {
	  	   	     			answer:"${answer.description}<c:if test='${answer.correct}'> (<fmt:message key='label.monitoring.item.summary.correct' />)</c:if>"
	  	   	     			
	  	   	     			<c:forEach var="j" begin="0" end="${fn:length(summary.answers)}">
	  	   	     				,choice${j}:"${answer.attempts[j]}"
	  	   	     			</c:forEach>
	  	   	     			
	  	   	   	   	    });
	  		        </c:forEach>			
	  				
	  			</c:forEach>
	  			
	  	        //jqgrid autowidth
	  	        $(window).bind('resize', function() {
	  	            resizeJqgrid(jQuery(".ui-jqgrid-btable:visible"));
	  	        });
	  	        function resizeJqgrid(jqgrids) {
	  	            jqgrids.each(function(index) {
	  	                var gridId = $(this).attr('id');
	  	                var gridParentWidth = jQuery('#gbox_' + gridId).parent().width();
	  	                jQuery('#' + gridId).setGridWidth(gridParentWidth, true);
	  	            });
	  	        };
	  	        setTimeout(function(){ window.dispatchEvent(new Event('resize')); }, 300);
	  	        
	  		});  	

    		function refreshSummaryPage()  {
        		self.parent.tb_remove();
    		}
  		</script>
		
	</lams:head>
	
	<body class="stripes">
	
		<c:set var="title">
			<fmt:message key="label.monitoring.summary.report.for" >
				<fmt:param>${fn:escapeXml(item.title)}</fmt:param>
			</fmt:message>
		</c:set>
		<lams:Page type="learning" title="${title}">
		
			<h3>
				<c:out value="${item.description}" escapeXml="false"/>
			</h3>	
			<lams:errors/>
			
			<c:forEach var="summary" items="${summaryList}" varStatus="status">
				<div class="panel panel-default" >
		        	<div class="panel-heading">
						<span class="panel-title">
							<fmt:message key="label.monitoring.item.summary.group" />&nbsp;${summary.sessionName}
						</span>
					</div>
				
					<table id="session${summary.sessionId}" class="scroll"></table>
				</div>
			</c:forEach>	

			<a href="#" onclick="refreshSummaryPage();" class="btn btn-default pull-right">
				<fmt:message key="button.close" /> 
			</a>
	
		<div id="footer"></div>

		</lams:Page>
				
	</body>
</lams:html>
