/****************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */

/* $$Id$$ */
package org.lamsfoundation.lams.tool.mc;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;



/**
 * <p> DTO that holds authoring properties for authoring jsps
 * </p>
 * 
 * @author Ozgur Demirtas
 */
public class McGeneralAuthoringDTO implements Comparable
{
    protected String toolContentID;
    protected String currentTab;
    protected String activeModule;
    protected String defineLaterInEditMode;
    protected String showAuthoringTabs;
    protected String monitoringOriginatedDefineLater;
    protected String targetMode;
    protected String defaultQuestionContent;
    protected String defaultContentIdStr;
    
    protected String activityTitle;
    protected String activityInstructions;
    protected String onlineInstructions;
    protected String offlineInstructions;
    protected String usernameVisible;
    protected String reflect;
    protected String synchInMonitor;
    protected String questionsSequenced;
    protected String randomize;
    protected String editActivityEditMode;
    protected String reflectionSubject;
    protected String showMarks;
    
    protected String monitoredContentInUse;

    protected String httpSessionID;
    protected String requestedModule;
    protected String isDefineLater;
    protected Map mapQuestionContent;

    protected List attachmentList;
    protected List deletedAttachmentList;
    
    protected String sbmtSuccess;
    protected String userExceptionQuestionsDuplicate;
    
    protected String contentFolderID;
    protected String editableQuestionText;
    protected String editableQuestionFeedback;
    protected String sln;
    protected String retries;
    
    protected Map marksMap;
    protected String markValue;
    protected Map correctMap;
    
    protected String passMarkValue;
    protected Map passMarksMap;
    
    protected String totalMarks;

	public String toString() {
        return new ToStringBuilder(this)
            .append("toolContentID: ", toolContentID)
            .append("contentFolderID: ", contentFolderID)
            .append("httpSessionID: ", httpSessionID)
            .append("passMarksMap: ", passMarksMap)
            .append("totalMarks: ", totalMarks)
            .append("marksMap: ", marksMap)
            .append("currentTab: ", currentTab)            
            .append("markValue: ", markValue)
            .append("activeModule: ", activeModule)
            .append("defineLaterInEditMode: ", defineLaterInEditMode)
            .append("showAuthoringTabs: ", showAuthoringTabs)
            .append("monitoringOriginatedDefineLater: ", monitoringOriginatedDefineLater)
            .append("targetMode: ", targetMode)
            .append("defaultQuestionContent: ", defaultQuestionContent)
            .append("defaultContentIdStr: ", defaultContentIdStr)
            .append("activityTitle: ", activityTitle)
            .append("activityInstructions: ", activityInstructions)
            .append("reflect: ", reflect)
            .append("onlineInstructions: ", onlineInstructions)
            .append("offlineInstructions: ", offlineInstructions)
            .append("usernameVisible: ", usernameVisible)
            .append("synchInMonitor: ", synchInMonitor)
            .append("questionsSequenced: ", questionsSequenced)
            .append("editActivityEditMode: ", editActivityEditMode)
            .append("reflectionSubject: ", reflectionSubject)
            .append("requestedModule: ", requestedModule)
            .append("isDefineLater: ", isDefineLater)
            .append("monitoredContentInUse: ", monitoredContentInUse)
            .append("mapQuestionContent: ", mapQuestionContent)
            .append("attachmentList: ", attachmentList)
            .append("deletedAttachmentList: ", deletedAttachmentList)
            .append("sbmtSuccess: ", sbmtSuccess)
            .append("userExceptionQuestionsDuplicate: ", userExceptionQuestionsDuplicate)
            .toString();
    }
    
	
    
    /**
     * @return Returns the userExceptionQuestionsDuplicate.
     */
    public String getUserExceptionQuestionsDuplicate() {
        return userExceptionQuestionsDuplicate;
    }
    /**
     * @param userExceptionQuestionsDuplicate The userExceptionQuestionsDuplicate to set.
     */
    public void setUserExceptionQuestionsDuplicate(
            String userExceptionQuestionsDuplicate) {
        this.userExceptionQuestionsDuplicate = userExceptionQuestionsDuplicate;
    }
    /**
     * @return Returns the httpSessionID.
     */
    public String getHttpSessionID() {
        return httpSessionID;
    }
    /**
     * @param httpSessionID The httpSessionID to set.
     */
    public void setHttpSessionID(String httpSessionID) {
        this.httpSessionID = httpSessionID;
    }
    /**
     * @return Returns the monitoredContentInUse.
     */
    public String getMonitoredContentInUse() {
        return monitoredContentInUse;
    }
    /**
     * @param monitoredContentInUse The monitoredContentInUse to set.
     */
    public void setMonitoredContentInUse(String monitoredContentInUse) {
        this.monitoredContentInUse = monitoredContentInUse;
    }
    /**
     * @return Returns the editActivityEditMode.
     */
    public String getEditActivityEditMode() {
        return editActivityEditMode;
    }
    /**
     * @param editActivityEditMode The editActivityEditMode to set.
     */
    public void setEditActivityEditMode(String editActivityEditMode) {
        this.editActivityEditMode = editActivityEditMode;
    }
    /**
     * @return Returns the deletedAttachmentList.
     */
    public List getDeletedAttachmentList() {
        return deletedAttachmentList;
    }
    /**
     * @param deletedAttachmentList The deletedAttachmentList to set.
     */
    public void setDeletedAttachmentList(List deletedAttachmentList) {
        this.deletedAttachmentList = deletedAttachmentList;
    }
    /**
     * @return Returns the mapQuestionContent.
     */
    public Map getMapQuestionContent() {
        return mapQuestionContent;
    }
    /**
     * @param mapQuestionContent The mapQuestionContent to set.
     */
    public void setMapQuestionContent(Map mapQuestionContent) {
        this.mapQuestionContent = mapQuestionContent;
    }
    /**
     * @return Returns the isDefineLater.
     */
    public String getIsDefineLater() {
        return isDefineLater;
    }
    /**
     * @param isDefineLater The isDefineLater to set.
     */
    public void setIsDefineLater(String isDefineLater) {
        this.isDefineLater = isDefineLater;
    }
    
    
    /**
     * @return Returns the attachmentList.
     */
    public List getAttachmentList() {
        return attachmentList;
    }
    /**
     * @param attachmentList The attachmentList to set.
     */
    public void setAttachmentList(List attachmentList) {
        this.attachmentList = attachmentList;
    }
    /**
     * @return Returns the toolContentID.
     */
    public String getToolContentID() {
        return toolContentID;
    }
    /**
     * @param toolContentID The toolContentID to set.
     */
    public void setToolContentID(String toolContentID) {
        this.toolContentID = toolContentID;
    }

    
    /**
     * @return Returns the targetMode.
     */
    public String getTargetMode() {
        return targetMode;
    }
    /**
     * @param targetMode The targetMode to set.
     */
    public void setTargetMode(String targetMode) {
        this.targetMode = targetMode;
    }
    
    /**
     * @return Returns the monitoringOriginatedDefineLater.
     */
    public String getMonitoringOriginatedDefineLater() {
        return monitoringOriginatedDefineLater;
    }
    /**
     * @param monitoringOriginatedDefineLater The monitoringOriginatedDefineLater to set.
     */
    public void setMonitoringOriginatedDefineLater(
            String monitoringOriginatedDefineLater) {
        this.monitoringOriginatedDefineLater = monitoringOriginatedDefineLater;
    }

    
    /**
     * @return Returns the activeModule.
     */
    public String getActiveModule() {
        return activeModule;
    }
    /**
     * @param activeModule The activeModule to set.
     */
    public void setActiveModule(String activeModule) {
        this.activeModule = activeModule;
    }
    /**
     * @return Returns the defineLaterInEditMode.
     */
    public String getDefineLaterInEditMode() {
        return defineLaterInEditMode;
    }
    /**
     * @param defineLaterInEditMode The defineLaterInEditMode to set.
     */
    public void setDefineLaterInEditMode(String defineLaterInEditMode) {
        this.defineLaterInEditMode = defineLaterInEditMode;
    }
    /**
     * @return Returns the showAuthoringTabs.
     */
    public String getShowAuthoringTabs() {
        return showAuthoringTabs;
    }
    /**
     * @param showAuthoringTabs The showAuthoringTabs to set.
     */
    public void setShowAuthoringTabs(String showAuthoringTabs) {
        this.showAuthoringTabs = showAuthoringTabs;
    }
    
	public int compareTo(Object o)
    {
	    McGeneralAuthoringDTO mcGeneralAuthoringDTO = (McGeneralAuthoringDTO) o;
     
        if (mcGeneralAuthoringDTO == null)
        	return 1;
		else
			return 0;
    }

    /**
     * @return Returns the defaultContentIdStr.
     */
    public String getDefaultContentIdStr() {
        return defaultContentIdStr;
    }
    /**
     * @param defaultContentIdStr The defaultContentIdStr to set.
     */
    public void setDefaultContentIdStr(String defaultContentIdStr) {
        this.defaultContentIdStr = defaultContentIdStr;
    }
    /**
     * @return Returns the defaultQuestionContent.
     */
    public String getDefaultQuestionContent() {
        return defaultQuestionContent;
    }
    /**
     * @param defaultQuestionContent The defaultQuestionContent to set.
     */
    public void setDefaultQuestionContent(String defaultQuestionContent) {
        this.defaultQuestionContent = defaultQuestionContent;
    }
    /**
     * @return Returns the requestedModule.
     */
    public String getRequestedModule() {
        return requestedModule;
    }
    /**
     * @param requestedModule The requestedModule to set.
     */
    public void setRequestedModule(String requestedModule) {
        this.requestedModule = requestedModule;
    }
    /**
     * @return Returns the activityInstructions.
     */
    public String getActivityInstructions() {
        return activityInstructions;
    }
    /**
     * @param activityInstructions The activityInstructions to set.
     */
    public void setActivityInstructions(String activityInstructions) {
        this.activityInstructions = activityInstructions;
    }
    /**
     * @return Returns the activityTitle.
     */
    public String getActivityTitle() {
        return activityTitle;
    }
    /**
     * @param activityTitle The activityTitle to set.
     */
    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }
    /**
     * @return Returns the offlineInstructions.
     */
    public String getOfflineInstructions() {
        return offlineInstructions;
    }
    /**
     * @param offlineInstructions The offlineInstructions to set.
     */
    public void setOfflineInstructions(String offlineInstructions) {
        this.offlineInstructions = offlineInstructions;
    }
    /**
     * @return Returns the onlineInstructions.
     */
    public String getOnlineInstructions() {
        return onlineInstructions;
    }
    /**
     * @param onlineInstructions The onlineInstructions to set.
     */
    public void setOnlineInstructions(String onlineInstructions) {
        this.onlineInstructions = onlineInstructions;
    }
    /**
     * @return Returns the questionsSequenced.
     */
    public String getQuestionsSequenced() {
        return questionsSequenced;
    }
    /**
     * @param questionsSequenced The questionsSequenced to set.
     */
    public void setQuestionsSequenced(String questionsSequenced) {
        this.questionsSequenced = questionsSequenced;
    }
 
    /**
     * @return Returns the synchInMonitor.
     */
    public String getSynchInMonitor() {
        return synchInMonitor;
    }
    /**
     * @param synchInMonitor The synchInMonitor to set.
     */
    public void setSynchInMonitor(String synchInMonitor) {
        this.synchInMonitor = synchInMonitor;
    }
    /**
     * @return Returns the usernameVisible.
     */
    public String getUsernameVisible() {
        return usernameVisible;
    }
    /**
     * @param usernameVisible The usernameVisible to set.
     */
    public void setUsernameVisible(String usernameVisible) {
        this.usernameVisible = usernameVisible;
    }
    /**
     * @return Returns the currentTab.
     */
    public String getCurrentTab() {
        return currentTab;
    }
    /**
     * @param currentTab The currentTab to set.
     */
    public void setCurrentTab(String currentTab) {
        this.currentTab = currentTab;
    }
    
    /**
     * @return Returns the sbmtSuccess.
     */
    public String getSbmtSuccess() {
        return sbmtSuccess;
    }
    /**
     * @param sbmtSuccess The sbmtSuccess to set.
     */
    public void setSbmtSuccess(String sbmtSuccess) {
        this.sbmtSuccess = sbmtSuccess;
    }
    
    /**
     * @return Returns the contentFolderID.
     */
    public String getContentFolderID() {
        return contentFolderID;
    }
    /**
     * @param contentFolderID The contentFolderID to set.
     */
    public void setContentFolderID(String contentFolderID) {
        this.contentFolderID = contentFolderID;
    }

    /**
     * @return Returns the editableQuestionText.
     */
    public String getEditableQuestionText() {
        return editableQuestionText;
    }
    /**
     * @param editableQuestionText The editableQuestionText to set.
     */
    public void setEditableQuestionText(String editableQuestionText) {
        this.editableQuestionText = editableQuestionText;
    }
    
    /**
     * @return Returns the editableQuestionFeedback.
     */
    public String getEditableQuestionFeedback() {
        return editableQuestionFeedback;
    }
    /**
     * @param editableQuestionFeedback The editableQuestionFeedback to set.
     */
    public void setEditableQuestionFeedback(String editableQuestionFeedback) {
        this.editableQuestionFeedback = editableQuestionFeedback;
    }
    
    /**
     * @return Returns the reflect.
     */
    public String getReflect() {
        return reflect;
    }
    /**
     * @param reflect The reflect to set.
     */
    public void setReflect(String reflect) {
        this.reflect = reflect;
    }
    /**
     * @return Returns the reflectionSubject.
     */
    public String getReflectionSubject() {
        return reflectionSubject;
    }
    /**
     * @param reflectionSubject The reflectionSubject to set.
     */
    public void setReflectionSubject(String reflectionSubject) {
        this.reflectionSubject = reflectionSubject;
    }
    
    /**
     * @return Returns the sln.
     */
    public String getSln() {
        return sln;
    }
    /**
     * @param sln The sln to set.
     */
    public void setSln(String sln) {
        this.sln = sln;
    }
    /**
     * @return Returns the retries.
     */
    public String getRetries() {
        return retries;
    }
    /**
     * @param retries The retries to set.
     */
    public void setRetries(String retries) {
        this.retries = retries;
    }
    /**
     * @return Returns the marksMap.
     */
    public Map getMarksMap() {
        return marksMap;
    }
    /**
     * @param marksMap The marksMap to set.
     */
    public void setMarksMap(Map marksMap) {
        this.marksMap = marksMap;
    }
    /**
     * @return Returns the markValue.
     */
    public String getMarkValue() {
        return markValue;
    }
    /**
     * @param markValue The markValue to set.
     */
    public void setMarkValue(String markValue) {
        this.markValue = markValue;
    }
    
    /**
     * @return Returns the correctMap.
     */
    public Map getCorrectMap() {
        return correctMap;
    }
    /**
     * @param correctMap The correctMap to set.
     */
    public void setCorrectMap(Map correctMap) {
        this.correctMap = correctMap;
    }
    /**
     * @return Returns the passMarkValue.
     */
    public String getPassMarkValue() {
        return passMarkValue;
    }
    /**
     * @param passMarkValue The passMarkValue to set.
     */
    public void setPassMarkValue(String passMarkValue) {
        this.passMarkValue = passMarkValue;
    }
    /**
     * @return Returns the passMarksMap.
     */
    public Map getPassMarksMap() {
        return passMarksMap;
    }
    /**
     * @param passMarksMap The passMarksMap to set.
     */
    public void setPassMarksMap(Map passMarksMap) {
        this.passMarksMap = passMarksMap;
    }
    /**
     * @return Returns the totalMarks.
     */
    public String getTotalMarks() {
        return totalMarks;
    }
    /**
     * @param totalMarks The totalMarks to set.
     */
    public void setTotalMarks(String totalMarks) {
        this.totalMarks = totalMarks;
    }
    /**
     * @return Returns the showMarks.
     */
    public String getShowMarks() {
        return showMarks;
    }
    /**
     * @param showMarks The showMarks to set.
     */
    public void setShowMarks(String showMarks) {
        this.showMarks = showMarks;
    }

    /**
     * @return Returns the randomize.
     */
    public String getRandomize() {
        return randomize;
    }
    /**
     * @param randomize The randomize to set.
     */
    public void setRandomize(String randomize) {
        this.randomize = randomize;
    }
}
