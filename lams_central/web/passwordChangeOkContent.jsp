<%@ taglib uri="tags-fmt" prefix="fmt" %>

<table width="100%" height="177" border="0" cellpadding="5" cellspacing="0" bgcolor="#FFFFFF">
	<tr> 
		<td valign="top">
			<p class=body><fmt:message key="msg.password.changed"/></p>
			<p class=body>	
				<input name="Ok" type="button" class="button" id="Ok" 
					onClick="javascript:document.location='passwordChangeOk.do';" 		
					onMouseOver="changeStyle(this,'buttonover')"
					onMouseOut="changeStyle(this,'button')" 
					value="Ok" />
			</p>
		</td>
	</tr>
</table>
