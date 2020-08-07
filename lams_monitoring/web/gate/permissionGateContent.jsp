<%-- 
Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
License Information: http://lamsfoundation.org/licensing/lams/2.0/

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2 as 
  published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
  USA

  http://www.gnu.org/licenses/gpl.txt
--%>
<!DOCTYPE html>

<%@ include file="/taglibs.jsp"%>

<lams:html>
    <lams:head>
    	<script type="text/javascript" src="<lams:LAMSURL/>includes/javascript/jquery.js"></script>
		<script type="text/javascript">
			function allowUsers(who){
				var allowed = '';
				$('#' + who + ' input:checked').each(function(){
					allowed += $(this).val() + ',';
				});
				if (allowed == ''){
					return false;
				}
				$('#userId').val(allowed);
				return true;
			}
		</script>
		<lams:css/>
    </lams:head>
    
    <body class="stripes">
		<c:set var="title"><fmt:message key="label.permission.gate.title"/></c:set>
		<lams:Page title="${title}" type="monitoring" formID="gateForm">
			
			<%@ include file="gateInfo.jsp" %>
			
			<c:if test="${not gateForm.gate.gateOpen}" >
				<p><fmt:message key="label.gate.you.open.message"/></p>
			</c:if>
	
			<%@ include file="gateStatus.jsp" %>
			<c:if test="${not gateForm.gate.gateOpen}" >
						<%@ include file="openGateSingleUser.jsp" %>
			</c:if>
	
		</lams:Page>

		<div id="footer">
		</div><!--closes footer-->

    </body>
</lams:html>

	
	
