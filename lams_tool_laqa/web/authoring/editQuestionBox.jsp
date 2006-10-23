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
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA

  http://www.gnu.org/licenses/gpl.txt
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
        "http://www.w3.org/TR/html4/strict.dtd">

<%@ include file="/common/taglibs.jsp"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<lams:css style="tabbed" />
		
		<script language="JavaScript" type="text/JavaScript">

			function submitMethod() {
				document.QaAuthoringForm.submit();
			}
			
			function submitMethod(actionMethod) {
				document.QaAuthoringForm.dispatch.value=actionMethod; 
				document.QaAuthoringForm.submit();
			}
			
		</script>
	</head>

	<body>
		<html:form action="/authoring?validate=false"
			styleId="newQuestionForm" enctype="multipart/form-data" method="POST">

			<html:hidden property="dispatch" value="saveSingleQuestion" />
			<html:hidden property="toolContentID" />
			<html:hidden property="currentTab" styleId="currentTab" />
			<html:hidden property="activeModule" />
			<html:hidden property="httpSessionID" />
			<html:hidden property="defaultContentIdStr" />
			<html:hidden property="defineLaterInEditMode" />
			<html:hidden property="contentFolderID" />
			<html:hidden property="editableQuestionIndex" />
			<html:hidden property="editQuestionBoxRequest" value="true" />

			<div class="field-name space-top">
				<fmt:message key="label.edit.question"></fmt:message>
			</div>

			<lams:FCKEditor id="newQuestion"
				value="${qaGeneralAuthoringDTO.editableQuestionText}"
				contentFolderID="${qaGeneralAuthoringDTO.contentFolderID}"></lams:FCKEditor>

			<div class="field-name space-top">
				<fmt:message key="label.feedback"></fmt:message>
			</div>
			<html:textarea property="feedback" rows="3" cols="75"></html:textarea>

			<div class="space-bottom-top">
				<a href="#" onclick="getElementById('newQuestionForm').submit();"
					class="button-add-item"> <fmt:message key="label.save.question" />
				</a>
				<a href="#" onclick="javascript:window.parent.hideMessage()"
					class="button space-left"> <fmt:message key="label.cancel" />
				</a>
			</div>
		</html:form>

	</body>
</html>
