<%@ taglib uri="tags-core" prefix="c"%>
<%@ taglib uri="tags-lams" prefix="lams"%>
<%@ taglib uri="fck-editor" prefix="fck"%>

<%@ attribute name="id" required="true" rtexprvalue="true"%>
<%@ attribute name="value" required="true" rtexprvalue="true"%>
<%@ attribute name="toolbarSet" required="false" rtexprvalue="true"%>
<%@ attribute name="height" required="false" rtexprvalue="true"%>
<%@ attribute name="width" required="false" rtexprvalue="true"%>
<%@ attribute name="contentFolderID" required="false" rtexprvalue="true"%>
<%@ attribute name="displayExpanded" required="false" rtexprvalue="true"%>

<c:set var="language">
	<lams:user property="fckLanguageMapping" />
</c:set>

<c:if test="${empty toolbarSet}">
	<c:set var="toolbarSet" value="Default" />
</c:if>

<c:if test="${empty displayExpanded}">
	<c:set var="displayExpanded" value="false" />
</c:if>

<c:if test="${empty width}">
	<c:set var="width" value="100%" />
</c:if>

<c:set var="basePath"><lams:LAMSURL/>/fckeditor/</c:set>

<!-- 
<script type="text/javascript">
	var oFCKeditor=new FCKeditor('${id}');
	oFCKeditor.BasePath= "${basePath}";
	oFCKeditor.ToolbarSet= "${toolbarSet}";
	oFCKeditor.Value= '${value}';

	oFCKeditor.Config["ImageBrowserURL"]= "${basePath}editor/filemanager/browser/default/browser.html?Type=Image&amp;Connector=connectors/jsp/connector&amp;CurrentFolder=/${contentFolderID}/";
	oFCKeditor.Config["LinkBrowserURL"]=  "${basePath}editor/filemanager/browser/default/browser.html?Connector=connectors/jsp/connector&amp;CurrentFolder=/${contentFolderID}/";
	oFCKeditor.Config["FlashBrowserURL"]= "${basePath}editor/filemanager/browser/default/browser.html?Type=Flash&amp;Connector=connectors/jsp/connector&amp;CurrentFolder=/${contentFolderID}/";
	oFCKeditor.Config["ImageUploadURL"]=  "${basePath}editor/filemanager/upload/simpleuploader?Type=Image&CurrentFolder=/${contentFolderID}/";
	oFCKeditor.Config["LinkUploadURL"]=   "${basePath}editor/filemanager/upload/simpleuploader?Type=File&CurrentFolder=/${contentFolderID}/";
	oFCKeditor.Config["FlashUploadURL"]=  "${basePath}editor/filemanager/upload/simpleuploader?Type=Flash&CurrentFolder=/${contentFolderID}/";

	oFCKeditor.Config["AutoDetectLanguage"]= false ;
	oFCKeditor.Config["DefaultLanguage"]= "${language}" ;

	oFCKeditor.Config["ToolbarStartExpanded"]= "${displayExpanded}" ;

	oFCKeditor.Create();
</script>
-->

<fck:editor instanceName="${id}"
	height="${height}"
	width="${width}"
	basePath="${basePath}"
	toolbarSet="${toolbarSet}">
<jsp:attribute name="value">${value}</jsp:attribute>
<fck:config
	ImageBrowserURL="${basePath}editor/filemanager/browser/default/browser.html?Type=Image&Connector=connectors/jsp/connector&CurrentFolder=/${contentFolderID}/"
	ImageUploadURL="${basePath}editor/filemanager/upload/simpleuploader?Type=Image&CurrentFolder=/${contentFolderID}/"
	LinkBrowserURL="${basePath}editor/filemanager/browser/default/browser.html?Connector=connectors/jsp/connector&CurrentFolder=/${contentFolderID}/"
	LinkUploadURL="${basePath}editor/filemanager/upload/simpleuploader?Type=File&CurrentFolder=/${contentFolderID}/"
	FlashBrowserURL="${basePath}editor/filemanager/browser/default/browser.html?Type=Flash&Connector=connectors/jsp/connector&CurrentFolder=/${contentFolderID}/"
	FlashUploadURL="${basePath}editor/filemanager/upload/simpleuploader?Type=Flash&CurrentFolder=/${contentFolderID}/"
	AutoDetectLanguage="false"
	DefaultLanguage="${language}"
	ToolbarStartExpanded="${displayExpanded}"/>
</fck:editor>




