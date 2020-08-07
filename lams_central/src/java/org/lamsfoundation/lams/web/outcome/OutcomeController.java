/****************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 * USA
 *
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */

package org.lamsfoundation.lams.web.outcome;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.lamsfoundation.lams.learningdesign.ToolActivity;
import org.lamsfoundation.lams.lesson.Lesson;
import org.lamsfoundation.lams.outcome.Outcome;
import org.lamsfoundation.lams.outcome.OutcomeMapping;
import org.lamsfoundation.lams.outcome.OutcomeResult;
import org.lamsfoundation.lams.outcome.OutcomeScale;
import org.lamsfoundation.lams.outcome.OutcomeScaleItem;
import org.lamsfoundation.lams.outcome.service.IOutcomeService;
import org.lamsfoundation.lams.security.ISecurityService;
import org.lamsfoundation.lams.usermanagement.Organisation;
import org.lamsfoundation.lams.usermanagement.Role;
import org.lamsfoundation.lams.usermanagement.User;
import org.lamsfoundation.lams.usermanagement.dto.UserDTO;
import org.lamsfoundation.lams.usermanagement.service.IUserManagementService;
import org.lamsfoundation.lams.util.FileUtil;
import org.lamsfoundation.lams.util.MessageService;
import org.lamsfoundation.lams.util.WebUtil;
import org.lamsfoundation.lams.util.excel.ExcelSheet;
import org.lamsfoundation.lams.util.excel.ExcelUtil;
import org.lamsfoundation.lams.web.session.SessionManager;
import org.lamsfoundation.lams.web.util.AttributeNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Controller
@RequestMapping("/outcome")
public class OutcomeController {
    private static Logger log = Logger.getLogger(OutcomeController.class);

    @Autowired
    private IUserManagementService userManagementService;
    @Autowired
    private ISecurityService securityService;
    @Autowired
    private IOutcomeService outcomeService;
    @Autowired
    @Qualifier("centralMessageService")
    private MessageService messageService;

    @RequestMapping("/outcomeManage")
    public String outcomeManage(HttpServletRequest request, HttpServletResponse response) throws Exception {
	Integer userId = getUserDTO().getUserID();
	Integer organisationId = WebUtil.readIntParam(request, AttributeNames.PARAM_ORGANISATION_ID, true);

	if (organisationId == null) {
	    // check if user is allowed to view and edit global outcomes
	    if (!securityService.isSysadmin(userId, "manage global outcomes", false)) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not a sysadmin");
		return null;
	    }
	} else {
	    // check if user is allowed to view and edit course outcomes
	    if (!securityService.hasOrgRole(organisationId, userId, new String[] { Role.AUTHOR },
		    "manage course outcomes", false)) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not an author in the organisation");
		return null;
	    }
	}
	List<Outcome> outcomes = outcomeService.getOutcomes(organisationId);
	request.setAttribute("outcomes", outcomes);

	request.setAttribute("canManageGlobal", userManagementService.isUserSysAdmin());
	return "outcome/outcomeManage";
    }

    @RequestMapping("/outcomeEdit")
    public String outcomeEdit(@ModelAttribute OutcomeForm outcomeForm, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	Integer userId = getUserDTO().getUserID();
	Long outcomeId = WebUtil.readLongParam(request, "outcomeId", true);
	Outcome outcome = null;
	Integer organisationId = null;
	if (outcomeId == null) {
	    organisationId = WebUtil.readIntParam(request, AttributeNames.PARAM_ORGANISATION_ID, true);
	} else {
	    outcome = (Outcome) userManagementService.findById(Outcome.class, outcomeId);
	    if (outcome.getOrganisation() != null) {
		// get organisation ID from the outcome - the safest way
		organisationId = outcome.getOrganisation().getOrganisationId();
	    }
	}

	if (organisationId != null && !securityService.hasOrgRole(organisationId, userId, new String[] { Role.AUTHOR },
		"add/edit course outcome", false)) {
	    response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not an author in the organisation");
	    return null;
	}

	outcomeForm.setOrganisationId(organisationId);
	outcomeForm.setContentFolderId(outcomeService.getContentFolderId(organisationId));
	if (outcome == null) {
	    outcomeForm.setScaleId(IOutcomeService.DEFAULT_SCALE_ID);
	} else {
	    outcomeForm.setOutcomeId(outcome.getOutcomeId());
	    outcomeForm.setName(outcome.getName());
	    outcomeForm.setCode(outcome.getCode());
	    outcomeForm.setDescription(outcome.getDescription());
	    outcomeForm.setScaleId(outcome.getScale().getScaleId());
	}

	List<OutcomeScale> scales = outcomeService.getScales(organisationId);
	request.setAttribute("scales", scales);

	request.setAttribute("canManageGlobal", userManagementService.isUserSysAdmin());
	return "outcome/outcomeEdit";
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(path = "/outcomeSave", method = RequestMethod.POST)
    public String outcomeSave(@ModelAttribute OutcomeForm outcomeForm, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	Integer userId = getUserDTO().getUserID();
	Long outcomeId = outcomeForm.getOutcomeId();
	Outcome outcome = null;
	Integer organisationId = null;
	if (outcomeId == null) {
	    organisationId = outcomeForm.getOrganisationId();
	} else {
	    outcome = (Outcome) userManagementService.findById(Outcome.class, outcomeId);
	    if (outcome.getOrganisation() != null) {
		// get organisation ID from the outcome - the safest way
		organisationId = outcome.getOrganisation().getOrganisationId();
	    }
	}

	if (organisationId == null) {
	    if (!securityService.isSysadmin(userId, "persist global outcome", false)) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not a sysadmin");
		return null;
	    }
	} else {
	    if (!securityService.hasOrgRole(organisationId, userId, new String[] { Role.AUTHOR },
		    "persist course outcome", false)) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not an author in the organisation");
		return null;
	    }
	}

	MultiValueMap<String, String> errorMap = new LinkedMultiValueMap<>();
	validateOutcomeForm(outcomeForm, errorMap);
	if (!errorMap.isEmpty()) {
	    request.setAttribute("errorMap", errorMap);
	} else {
	    try {
		Organisation organisation = (Organisation) (organisationId == null ? null
			: userManagementService.findById(Organisation.class, organisationId));
		if (outcome == null) {
		    outcome = new Outcome();
		    outcome.setOrganisation(organisation);
		    User user = (User) userManagementService.findById(User.class, userId);
		    outcome.setCreateBy(user);
		    outcome.setCreateDateTime(new Date());
		}

		outcome.setName(outcomeForm.getName());
		outcome.setCode(outcomeForm.getCode());
		outcome.setDescription(outcomeForm.getDescription());
		outcome.setContentFolderId(outcomeForm.getContentFolderId());
		if (outcomeForm.getScaleId() != null) {
		    OutcomeScale scale = (OutcomeScale) userManagementService.findById(OutcomeScale.class,
			    outcomeForm.getScaleId());
		    outcome.setScale(scale);
		}
		userManagementService.save(outcome);

		if (log.isDebugEnabled()) {
		    log.debug("Saved outcome " + outcome.getOutcomeId());
		}
		request.setAttribute("saved", true);
	    } catch (Exception e) {
		log.error("Exception while saving an outcome", e);
		errorMap.add("GLOBAL", messageService.getMessage("outcome.manage.add.error"));
		request.setAttribute("errorMap", errorMap);
	    }
	}

	List<OutcomeScale> scales = userManagementService.findAll(OutcomeScale.class);
	request.setAttribute("scales", scales);
	return "outcome/outcomeEdit";
    }

    @RequestMapping(path = "/outcomeRemove", method = RequestMethod.POST)
    public String outcomeRemove(HttpServletRequest request, HttpServletResponse response) throws Exception {
	Long outcomeId = WebUtil.readLongParam(request, "outcomeId", false);
	Outcome outcome = (Outcome) userManagementService.findById(Outcome.class, outcomeId);
	if (outcome == null) {
	    throw new IllegalArgumentException("Can not find an outcome with ID " + outcomeId);
	}
	Integer organisationId = outcome.getOrganisation() == null ? null
		: outcome.getOrganisation().getOrganisationId();
	Integer userId = getUserDTO().getUserID();

	if (organisationId == null) {
	    if (!securityService.isSysadmin(userId, "remove global outcome", false)) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not a sysadmin");
		return null;
	    }
	} else {
	    if (!securityService.hasOrgRole(organisationId, userId, new String[] { Role.AUTHOR },
		    "remove course outcome", false)) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not an author in the organisation");
		return null;
	    }
	}
	userManagementService.delete(outcome);
	if (log.isDebugEnabled()) {
	    log.debug("Deleted outcome " + outcomeId);
	}
	return outcomeManage(request, response);
    }

    @RequestMapping("/outcomeSearch")
    @ResponseBody
    public String outcomeSearch(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String search = WebUtil.readStrParam(request, "term", true);
	String organisationIdString = WebUtil.readStrParam(request, "organisationIds", true);
	Set<Integer> organisationIds = null;
	Integer userId = getUserDTO().getUserID();
	if (StringUtils.isNotBlank(organisationIdString)) {
	    String[] split = organisationIdString.split(",");
	    organisationIds = new HashSet<Integer>(split.length);
	    for (String organisationId : split) {
		organisationIds.add(Integer.valueOf(organisationId));
	    }
	}
	if (organisationIds == null) {
	    if (!request.isUserInRole(Role.SYSADMIN) && !request.isUserInRole(Role.AUTHOR)) {
		String error = "User " + userId + " is not sysadmin nor an author and can not search outcome";
		log.error(error);
		throw new SecurityException(error);
	    }
	} else {
	    for (Integer organisationId : organisationIds) {
		securityService.hasOrgRole(organisationId, userId, new String[] { Role.AUTHOR }, "search outcome",
			true);
	    }
	}

	List<Outcome> outcomes = outcomeService.getOutcomes(search, organisationIds);
	ArrayNode responseJSON = JsonNodeFactory.instance.arrayNode();
	for (Outcome outcome : outcomes) {
	    ObjectNode outcomeJSON = JsonNodeFactory.instance.objectNode();
	    outcomeJSON.put("value", outcome.getOutcomeId());
	    outcomeJSON.put("label", outcome.getName() + " (" + outcome.getCode() + ")");
	    responseJSON.add(outcomeJSON);
	}
	response.setContentType("application/json;charset=utf-8");
	return responseJSON.toString();
    }

    @RequestMapping(path = "/outcomeMap", method = RequestMethod.POST)
    @ResponseBody
    public String outcomeMap(HttpServletRequest request, HttpServletResponse response) throws Exception {
	Long outcomeId = WebUtil.readLongParam(request, "outcomeId");
	Long lessonId = WebUtil.readLongParam(request, "lessonId", true);
	Long toolContentId = WebUtil.readLongParam(request, "toolContentId", true);
	if (lessonId == null && toolContentId == null) {
	    throw new IllegalArgumentException(
		    "Either lesson ID or tool content ID must not be null when creating an outcome mapping");
	}
	Long itemId = WebUtil.readLongParam(request, "itemId", true);

	Outcome outcome = (Outcome) userManagementService.findById(Outcome.class, outcomeId);
	Integer organisationId = outcome.getOrganisation() == null ? null
		: outcome.getOrganisation().getOrganisationId();
	Integer userId = getUserDTO().getUserID();

	if (organisationId == null) {
	    if (!request.isUserInRole(Role.SYSADMIN) && !request.isUserInRole(Role.AUTHOR)) {
		String error = "User " + userId + " is not sysadmin nor an author and can not map outcome";
		log.error(error);
		throw new SecurityException(error);
	    }
	} else {
	    securityService.hasOrgRole(organisationId, userId, new String[] { Role.AUTHOR }, "map outcome", true);
	}

	List<OutcomeMapping> outcomeMappings = outcomeService.getOutcomeMappings(lessonId, toolContentId, itemId);
	for (OutcomeMapping existingMapping : outcomeMappings) {
	    if (existingMapping.getOutcome().getOutcomeId().equals(outcome.getOutcomeId())) {
		throw new IllegalArgumentException(
			"Trying to map an already mapped outcome with ID " + outcome.getOutcomeId());
	    }
	}

	OutcomeMapping outcomeMapping = new OutcomeMapping();
	outcomeMapping.setOutcome(outcome);
	outcomeMapping.setLessonId(lessonId);
	outcomeMapping.setToolContentId(toolContentId);
	outcomeMapping.setItemId(itemId);
	userManagementService.save(outcomeMapping);

	if (log.isDebugEnabled()) {
	    log.debug("Mapped outcome " + outcome.getOutcomeId() + " to lesson ID " + lessonId + " and tool content ID "
		    + toolContentId + " and item ID " + itemId);
	}

	response.setContentType("text/plain;charset=utf-8");
	return String.valueOf(outcomeMapping.getMappingId());
    }

    @RequestMapping("/outcomeGetMappings")
    @ResponseBody
    public String outcomeGetMappings(HttpServletRequest request, HttpServletResponse response) throws Exception {
	Long lessonId = WebUtil.readLongParam(request, "lessonId", true);
	Long toolContentId = WebUtil.readLongParam(request, "toolContentId", true);
	if (lessonId == null && toolContentId == null) {
	    throw new IllegalArgumentException(
		    "Either lesson ID or tool content ID must not be null when fetching outcome mappings");
	}
	Long itemId = WebUtil.readLongParam(request, "itemId", true);
	Integer userId = getUserDTO().getUserID();
	if (!request.isUserInRole(Role.SYSADMIN) && !request.isUserInRole(Role.AUTHOR)) {
	    String error = "User " + userId + " is not sysadmin nor an author and can not map outcome";
	    log.error(error);
	    throw new SecurityException(error);
	}

	List<OutcomeMapping> outcomeMappings = outcomeService.getOutcomeMappings(lessonId, toolContentId, itemId);
	ArrayNode responseJSON = JsonNodeFactory.instance.arrayNode();
	for (OutcomeMapping outcomeMapping : outcomeMappings) {
	    ObjectNode outcomeJSON = JsonNodeFactory.instance.objectNode();
	    outcomeJSON.put("mappingId", outcomeMapping.getMappingId());
	    outcomeJSON.put("outcomeId", outcomeMapping.getOutcome().getOutcomeId());
	    outcomeJSON.put("label",
		    outcomeMapping.getOutcome().getName() + " (" + outcomeMapping.getOutcome().getCode() + ")");
	    responseJSON.add(outcomeJSON);
	}
	response.setContentType("application/json;charset=utf-8");
	return responseJSON.toString();
    }

    @RequestMapping(path = "/outcomeRemoveMapping", method = RequestMethod.POST)
    public void outcomeRemoveMapping(HttpServletRequest request, HttpServletResponse response) throws Exception {
	Long mappingId = WebUtil.readLongParam(request, "mappingId");
	OutcomeMapping outcomeMapping = (OutcomeMapping) userManagementService.findById(OutcomeMapping.class,
		mappingId);
	Organisation organisation = outcomeMapping.getOutcome().getOrganisation();
	Integer organisationId = organisation == null ? null : organisation.getOrganisationId();
	Integer userId = getUserDTO().getUserID();
	if (organisationId == null) {
	    if (!request.isUserInRole(Role.SYSADMIN) && !request.isUserInRole(Role.AUTHOR)) {
		String error = "User " + userId
			+ " is not sysadmin nor an author and can not remove an outcome mapping";
		log.error(error);
		throw new SecurityException(error);
	    }
	} else {
	    securityService.hasOrgRole(organisationId, userId, new String[] { Role.AUTHOR }, "remove outcome mapping",
		    true);
	}
	userManagementService.delete(outcomeMapping);
	if (log.isDebugEnabled()) {
	    log.debug("Deleted outcome mapping " + outcomeMapping);
	}
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/outcomeSetResult")
    @ResponseBody
    public String outcomeSetResult(HttpServletRequest request, HttpServletResponse response) throws Exception {
	Long mappingId = WebUtil.readLongParam(request, "pk");
	Integer value = WebUtil.readIntParam(request, "value");
	Integer targetUserId = WebUtil.readIntParam(request, "name");
	OutcomeMapping outcomeMapping = (OutcomeMapping) userManagementService.findById(OutcomeMapping.class,
		mappingId);
	Long lessonId = outcomeMapping.getLessonId();
	if (lessonId == null) {
	    ToolActivity toolActivity = ((List<ToolActivity>) userManagementService.findByProperty(ToolActivity.class,
		    "toolContentId", outcomeMapping.getToolContentId())).get(0);
	    lessonId = ((Lesson) toolActivity.getLearningDesign().getLessons().iterator().next()).getLessonId();
	}
	Integer userId = getUserDTO().getUserID();
	securityService.isLessonMonitor(lessonId, userId, "set outcome result", true);

	OutcomeResult result = outcomeService.getOutcomeResult(userId, mappingId);
	if (result == null) {
	    // result does not exist; if value == -1, it means it is not meant to exist, otherwise create
	    if (value > -1) {
		result = new OutcomeResult();
		User user = (User) userManagementService.findById(User.class, userId);
		result.setCreateBy(user);
		result.setCreateDateTime(new Date());
		result.setMapping(outcomeMapping);
		User targetUser = (User) userManagementService.findById(User.class, targetUserId);
		result.setUser(targetUser);
		result.setValue(value);
		userManagementService.save(result);
		if (log.isDebugEnabled()) {
		    log.debug("Added outcome result " + result.getResultId());
		}
	    }
	    // modify only if value is different
	} else if (!result.getValue().equals(value)) {
	    // if value is -1, remove the result
	    if (value == -1) {
		Long resultId = result.getResultId();
		userManagementService.delete(result);
		if (log.isDebugEnabled()) {
		    log.debug("Deleted outcome result " + resultId);
		}
	    } else {
		// update existing result
		result.setValue(value);
		User user = (User) userManagementService.findById(User.class, userId);
		result.setCreateBy(user);
		result.setCreateBy(user);
		result.setValue(value);
		userManagementService.save(result);
		if (log.isDebugEnabled()) {
		    log.debug("Edited outcome result " + result.getResultId());
		}
	    }
	}

	// if something else than OK is sent, x-editable will print ERROR!
	response.setContentType("text/plain;charset=utf-8");
	return "OK";
    }

    @RequestMapping("/outcomeExport")
    public void outcomeExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
	UserDTO user = getUserDTO();
	securityService.isSysadmin(user.getUserID(), "export outcomes", true);

	List<ExcelSheet> sheets = outcomeService.exportOutcomes();

	String fileName = "lams_outcomes.xls";
	fileName = FileUtil.encodeFilenameForDownload(request, fileName);

	response.setContentType("application/x-download");
	response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

	// set cookie that will tell JS script that export has been finished
	String downloadTokenValue = WebUtil.readStrParam(request, "downloadTokenValue");
	Cookie fileDownloadTokenCookie = new Cookie("fileDownloadToken", downloadTokenValue);
	fileDownloadTokenCookie.setPath("/");
	response.addCookie(fileDownloadTokenCookie);

	// Code to generate file and write file contents to response
	ServletOutputStream out = response.getOutputStream();
	ExcelUtil.createExcel(out, sheets, messageService.getMessage("outcome.export.date"), true, false);
    }

    @RequestMapping("/outcomeImport")
    public String outcomeImport(@RequestParam("file") MultipartFile file, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	UserDTO user = getUserDTO();
	securityService.isSysadmin(user.getUserID(), "import outcomes", true);

	try {
	    int importCount = outcomeService.importOutcomes(file);
	    log.info("Imported " + importCount + " outcomes");
	} catch (Exception e) {
	    log.error("Error while importing outcomes", e);
	    MultiValueMap<String, String> errorMap = new LinkedMultiValueMap<>(1);
	    errorMap.add("GLOBAL", messageService.getMessage("outcome.import.error"));
	    request.setAttribute("errorMap", errorMap);
	}

	return outcomeManage(request, response);
    }

    @RequestMapping("/scaleManage")
    public String scaleManage(HttpServletRequest request, HttpServletResponse response) throws Exception {
	Integer userId = getUserDTO().getUserID();
	Integer organisationId = WebUtil.readIntParam(request, AttributeNames.PARAM_ORGANISATION_ID, true);

	if (organisationId == null) {
	    // check if user is allowed to view and edit global outcomes
	    if (!securityService.isSysadmin(userId, "manage global scales", false)) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not a sysadmin");
		return null;
	    }
	} else {
	    // check if user is allowed to view and edit course outcomes
	    if (!securityService.hasOrgRole(organisationId, userId, new String[] { Role.AUTHOR },
		    "manage course scales", false)) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not an author in the organisation");
		return null;
	    }
	}
	List<OutcomeScale> scales = outcomeService.getScales(organisationId);
	request.setAttribute("scales", scales);

	request.setAttribute("canManageGlobal", userManagementService.isUserSysAdmin());
	return "outcome/scaleManage";
    }

    @RequestMapping("/scaleRemove")
    public String scaleRemove(HttpServletRequest request, HttpServletResponse response) throws Exception {
	Long scaleId = WebUtil.readLongParam(request, "scaleId", false);
	OutcomeScale scale = (OutcomeScale) userManagementService.findById(OutcomeScale.class, scaleId);
	if (scale == null) {
	    throw new IllegalArgumentException("Can not find an outcome scale with ID " + scaleId);
	}
	Integer organisationId = scale.getOrganisation() == null ? null : scale.getOrganisation().getOrganisationId();
	Integer userId = getUserDTO().getUserID();

	if (organisationId == null) {
	    if (!securityService.isSysadmin(userId, "remove global scale", false)) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not a sysadmin");
		return null;
	    }
	} else {
	    if (!securityService.hasOrgRole(organisationId, userId, new String[] { Role.AUTHOR }, "remove course scale",
		    false)) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not an author in the organisation");
		return null;
	    }
	}
	try {
	    userManagementService.delete(scale);
	    if (log.isDebugEnabled()) {
		log.debug("Deleted outcome scale " + scaleId);
	    }
	} catch (Exception e) {
	    log.error("Error while removing an outcome scale", e);
	    MultiValueMap<String, String> errorMap = new LinkedMultiValueMap<>(1);
	    errorMap.add("GLOBAL", messageService.getMessage("scale.manage.remove.scale"));
	    request.setAttribute("errorMap", errorMap);
	}

	return scaleManage(request, response);
    }

    @RequestMapping("/scaleEdit")
    public String scaleEdit(@ModelAttribute("scaleForm") OutcomeScaleForm scaleForm, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	Integer userId = getUserDTO().getUserID();
	Long scaleId = WebUtil.readLongParam(request, "scaleId", true);
	OutcomeScale scale = null;
	Integer organisationId = null;
	if (scaleId == null) {
	    organisationId = WebUtil.readIntParam(request, AttributeNames.PARAM_ORGANISATION_ID, true);
	} else {
	    scale = (OutcomeScale) userManagementService.findById(OutcomeScale.class, scaleId);
	    if (scale.getOrganisation() != null) {
		// get organisation ID from the outcome - the safest way
		organisationId = scale.getOrganisation().getOrganisationId();
	    }
	}

	if (organisationId != null && !securityService.hasOrgRole(organisationId, userId, new String[] { Role.AUTHOR },
		"add/edit course outcome", false)) {
	    response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not an author in the organisation");
	    return null;
	}

	scaleForm.setOrganisationId(organisationId);
	scaleForm.setContentFolderId(outcomeService.getContentFolderId(organisationId));
	if (scale != null) {
	    scaleForm.setScaleId(scale.getScaleId());
	    scaleForm.setName(scale.getName());
	    scaleForm.setCode(scale.getCode());
	    scaleForm.setDescription(scale.getDescription());
	    scaleForm.setItems(scale.getItemString());
	}

	request.setAttribute("canManageGlobal", userManagementService.isUserSysAdmin());
	request.setAttribute("isDefaultScale", outcomeService.isDefaultScale(scaleForm.getScaleId()));
	return "outcome/scaleEdit";
    }

    @RequestMapping("/scaleSave")
    public String scaleSave(@ModelAttribute("scaleForm") OutcomeScaleForm scaleForm, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	Integer userId = getUserDTO().getUserID();
	Long scaleId = scaleForm.getScaleId();
	OutcomeScale scale = null;
	Integer organisationId = null;
	if (scaleId == null) {
	    organisationId = scaleForm.getOrganisationId();
	} else {
	    scale = (OutcomeScale) userManagementService.findById(OutcomeScale.class, scaleId);
	    if (outcomeService.isDefaultScale(scale.getScaleId())) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "The default scale can not be altered");
		return null;
	    }
	    if (scale.getOrganisation() != null) {
		// get organisation ID from the outcome - the safest way
		organisationId = scale.getOrganisation().getOrganisationId();
	    }
	}

	if (organisationId == null) {
	    if (!securityService.isSysadmin(userId, "persist global scale", false)) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not a sysadmin");
		return null;
	    }
	} else {
	    if (!securityService.hasOrgRole(organisationId, userId, new String[] { Role.AUTHOR },
		    "persist course scale", false)) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not an author in the organisation");
		return null;
	    }
	}

	MultiValueMap<String, String> errorMap = new LinkedMultiValueMap<>(1);
	validateScaleForm(scaleForm, errorMap);
	List<String> items = OutcomeScale.parseItems(scaleForm.getItems());
	if (items == null) {
	    errorMap.add("GLOBAL", messageService.getMessage("scale.manage.add.value.error.blank"));
	}
	if (errorMap.isEmpty()) {
	    try {
		Organisation organisation = (Organisation) (organisationId == null ? null
			: userManagementService.findById(Organisation.class, organisationId));
		if (scale == null) {
		    scale = new OutcomeScale();
		    scale.setOrganisation(organisation);
		    User user = (User) userManagementService.findById(User.class, userId);
		    scale.setCreateBy(user);
		    scale.setCreateDateTime(new Date());
		}

		scale.setName(scaleForm.getName());
		scale.setCode(scaleForm.getCode());
		scale.setDescription(scaleForm.getDescription());
		scale.setContentFolderId(scaleForm.getContentFolderId());
		userManagementService.save(scale);

		// find existing scales and add new ones
		Set<OutcomeScaleItem> newItems = new LinkedHashSet<>();
		int value = 0;
		for (String itemString : items) {
		    itemString = itemString.trim();
		    if (StringUtils.isBlank(itemString)) {
			errorMap.add("GLOBAL", messageService.getMessage("scale.manage.add.value.error.blank"));
			break;
		    }
		    OutcomeScaleItem item = null;
		    for (OutcomeScaleItem exisitngItem : scale.getItems()) {
			if (itemString.equals(exisitngItem.getName())) {
			    item = exisitngItem;
			    break;
			}
		    }
		    if (item == null) {
			item = new OutcomeScaleItem();
			item.setScale(scale);
			item.setName(itemString);
		    }
		    item.setValue(value++);
		    newItems.add(item);
		}
		if (errorMap.isEmpty()) {
		    scale.getItems().clear();
		    scale.getItems().addAll(newItems);
		    userManagementService.save(scale);

		    if (log.isDebugEnabled()) {
			log.debug("Saved outcome scale " + scale.getScaleId());
		    }

		    request.setAttribute("saved", true);
		}
	    } catch (Exception e) {
		log.error("Exception while saving an outcome", e);
		errorMap.add("GLOBAL", messageService.getMessage("scale.manage.add.error"));
	    }
	}
	if (!errorMap.isEmpty()) {
	    request.setAttribute("errorMap", errorMap);
	}

	return "outcome/scaleEdit";
    }

    @RequestMapping("/scaleExport")
    public void scaleExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
	UserDTO user = getUserDTO();
	securityService.isSysadmin(user.getUserID(), "export outcome scales", true);

	List<ExcelSheet> sheets = outcomeService.exportScales();

	String fileName = "lams_outcome_scales.xls";
	fileName = FileUtil.encodeFilenameForDownload(request, fileName);

	response.setContentType("application/x-download");
	response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

	// set cookie that will tell JS script that export has been finished
	String downloadTokenValue = WebUtil.readStrParam(request, "downloadTokenValue");
	Cookie fileDownloadTokenCookie = new Cookie("fileDownloadToken", downloadTokenValue);
	fileDownloadTokenCookie.setPath("/");
	response.addCookie(fileDownloadTokenCookie);

	// Code to generate file and write file contents to response
	ServletOutputStream out = response.getOutputStream();
	ExcelUtil.createExcel(out, sheets, messageService.getMessage("outcome.export.date"), true, false);
    }

    @RequestMapping("/scaleImport")
    public String scaleImport(@RequestParam("file") MultipartFile file, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	UserDTO user = getUserDTO();
	securityService.isSysadmin(user.getUserID(), "import outcome scales", true);

	try {
	    int importCount = outcomeService.importScales(file);
	    log.info("Imported " + importCount + " outcome scales");
	} catch (Exception e) {
	    log.error("Error while importing outcome scales", e);
	    MultiValueMap<String, String> errorMap = new LinkedMultiValueMap<>(1);
	    errorMap.add("GLOBAL", messageService.getMessage("outcome.import.error"));
	    request.setAttribute("errorMap", errorMap);
	}

	return scaleManage(request, response);
    }

    private UserDTO getUserDTO() {
	HttpSession ss = SessionManager.getSession();
	return (UserDTO) ss.getAttribute(AttributeNames.USER);
    }

    private void validateOutcomeForm(OutcomeForm outcomeForm, MultiValueMap<String, String> errorMap) {
	if (StringUtils.isBlank(outcomeForm.getName())) {
	    errorMap.add("GLOBAL", messageService.getMessage("outcome.manage.add.error.name.blank"));
	}
	if (StringUtils.isBlank(outcomeForm.getCode())) {
	    errorMap.add("GLOBAL", messageService.getMessage("outcome.manage.add.error.code.blank"));
	}
	if (outcomeForm.getScaleId() == null || outcomeForm.getScaleId() == 0) {
	    errorMap.add("GLOBAL", messageService.getMessage("outcome.manage.add.error.scale.choose"));
	}
    }

    private void validateScaleForm(OutcomeScaleForm scaleForm, MultiValueMap<String, String> errorMap) {
	if (StringUtils.isBlank(scaleForm.getName())) {
	    errorMap.add("GLOBAL", messageService.getMessage("outcome.manage.add.error.name.blank"));
	}
	if (StringUtils.isBlank(scaleForm.getCode())) {
	    errorMap.add("GLOBAL", messageService.getMessage("outcome.manage.add.error.code.blank"));
	}
    }

}
