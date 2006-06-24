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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  USA

  http://www.gnu.org/licenses/gpl.txt
--%>
<%@ taglib uri="tags-bean" prefix="bean"%> 
<%@ taglib uri="tags-html" prefix="html"%>
<%@ taglib uri="tags-logic" prefix="logic" %>
<%@ taglib uri="tags-logic-el" prefix="logic-el" %>
<%@ taglib uri="tags-core" prefix="c"%>
<%@ taglib uri="tags-fmt" prefix="fmt" %>
<%@ taglib uri="fck-editor" prefix="FCK" %>
<%@ taglib uri="tags-lams" prefix="lams" %>

<c:set var="lams"><lams:LAMSURL/></c:set>
<c:set var="tool"><lams:WebAppURL/></c:set>

			<table width="80%" cellspacing="8" align="center" class="forms">
						<tr>
					 		<td NOWRAP colspan=2 > <b> <font size=2> <bean:message key="label.groupName"/> </b>
					 		<c:out value="${currentMonitoredToolSessionName}"/>  </td>
						</tr>


			  	 		<tr>
							 <td NOWRAP valign=top align=left> <b> <font size=2> <bean:message key="label.user"/> </font> </b> </td>  
				  	 		<c:set var="queIndex" scope="request" value="0"/>
							<c:forEach var="currentDto" items="${sessionScope.listMonitoredAnswersContainerDto}">
							<c:set var="queIndex" scope="request" value="${queIndex +1}"/>
								<td NOWRAP valign=top align=left> <b> <font size=2> <bean:message key="label.question.only"/> <c:out value="${queIndex}"/></b>
									 &nbsp (<bean:message key="label.weight"/> 
									<c:out value="${currentDto.weight}"/>  <bean:message key="label.percent"/>)
								</font> 
								</td>
							</c:forEach>		  	
							 
							 <td NOWRAP valign=top align=left> <b> <font size=2> <bean:message key="label.total"/> </font> </b> </td>  
			  	 		</tr>						 
			
		  	 		
					<c:forEach var="sessionMarksDto" items="${sessionScope.listMonitoredMarksContainerDto}">
			  	 		<c:set var="currentSessionId" scope="request" value="${sessionMarksDto.sessionId}"/>
			  	 		<c:set var="mapUserMarksDto" scope="request" value="${sessionMarksDto.userMarks}"/>

									<c:forEach var="markData" items="${mapUserMarksDto}">						
						  	 		<c:set var="data" scope="request" value="${markData.value}"/>
						  	 		<c:set var="currentUserSessionId" scope="request" value="${data.sessionId}"/>

									<c:if test="${sessionScope.currentMonitoredToolSession == 'All'}"> 			
										<c:if test="${currentUserSessionId == currentSessionId}"> 	
											<tr>									  	 		
							  	 				<td NOWRAP valign=top align=left> <font size=2>
														<c:out value="${data.userName}"/> 
													</font>
												</td>	
			
												<c:forEach var="mark" items="${data.marks}">
													<td NOWRAP valign=top align=left> <font size=2>
															<c:out value="${mark}"/> 								
													</td>
												</c:forEach>		  										
			
												<td NOWRAP valign=top align=left> <font size=2>
															<c:out value="${data.totalMark}"/> 																
													</font>
												</td>							
											</tr>													
										</c:if>																
									</c:if>																		
									</c:forEach>		  	
			

									<c:forEach var="markData" items="${mapUserMarksDto}">						
						  	 		<c:set var="data" scope="request" value="${markData.value}"/>
						  	 		<c:set var="currentUserSessionId" scope="request" value="${data.sessionId}"/>							  	 		
									<c:if test="${sessionScope.currentMonitoredToolSession !='All'}"> 			
										<c:if test="${sessionScope.currentMonitoredToolSession == currentUserSessionId}"> 										
											<c:if test="${currentUserSessionId == currentSessionId}"> 									
											<tr>			
												<td NOWRAP valign=top align=left> <font size=2>
														<c:out value="${data.userName}"/> 
													</font>
												</td>	
			
												<c:forEach var="mark" items="${data.marks}">
													<td NOWRAP valign=top align=left> <font size=2>
															<c:out value="${mark}"/> 								
													</td>
												</c:forEach>		  										
			
												<td NOWRAP valign=top align=left> <font size=2>
															<c:out value="${data.totalMark}"/> 																
													</font>
												</td>							
											</tr>														
											</c:if>																
										</c:if>																			
									</c:if>																								
									</c:forEach>		
					</c:forEach>		  	
			</table>		  	 		

	