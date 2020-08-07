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

package org.lamsfoundation.lams.tool.rsrc.web.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.lamsfoundation.lams.rating.model.LearnerItemRatingCriteria;
import org.lamsfoundation.lams.tool.ToolAccessMode;
import org.lamsfoundation.lams.tool.rsrc.ResourceConstants;
import org.lamsfoundation.lams.tool.rsrc.model.Resource;
import org.lamsfoundation.lams.tool.rsrc.model.ResourceItem;
import org.lamsfoundation.lams.tool.rsrc.model.ResourceItemInstruction;
import org.lamsfoundation.lams.tool.rsrc.model.ResourceUser;
import org.lamsfoundation.lams.tool.rsrc.service.IResourceService;
import org.lamsfoundation.lams.tool.rsrc.util.ResourceItemComparator;
import org.lamsfoundation.lams.tool.rsrc.web.form.ResourceForm;
import org.lamsfoundation.lams.tool.rsrc.web.form.ResourceItemForm;
import org.lamsfoundation.lams.tool.rsrc.web.form.ResourcePedagogicalPlannerForm;
import org.lamsfoundation.lams.usermanagement.dto.UserDTO;
import org.lamsfoundation.lams.util.CommonConstants;
import org.lamsfoundation.lams.util.Configuration;
import org.lamsfoundation.lams.util.ConfigurationKeys;
import org.lamsfoundation.lams.util.FileValidatorUtil;
import org.lamsfoundation.lams.util.MessageService;
import org.lamsfoundation.lams.util.WebUtil;
import org.lamsfoundation.lams.web.session.SessionManager;
import org.lamsfoundation.lams.web.util.AttributeNames;
import org.lamsfoundation.lams.web.util.SessionMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Steve.Ni
 */
@Controller
@RequestMapping("/authoring")
public class AuthoringController {
    private static Logger log = Logger.getLogger(AuthoringController.class);
    
    private static final int INIT_INSTRUCTION_COUNT = 2;
    private static final String INSTRUCTION_ITEM_DESC_PREFIX = "instructionItemDesc";
    private static final String INSTRUCTION_ITEM_COUNT = "instructionCount";
    private static final String ITEM_TYPE = "itemType";

    @Autowired
    private IResourceService resourceService;
    @Autowired
    @Qualifier("resourceMessageService")
    private MessageService messageService;

    /**
     * Remove resource item attachment, such as single file, learning object
     * ect. It is a ajax call and just temporarily remove from page, all
     * permenant change will happen only when user sumbit this resource item
     * again.
     *
     * @param request
     * @return
     */
    @RequestMapping("/removeItemAttachment")
    private String removeItemAttachment(HttpServletRequest request) {
	request.setAttribute("resourceItemForm", null);
	return "pages/authoring/parts/itemattachment";
    }

    /**
     * Remove resource item from HttpSession list and update page display. As
     * authoring rule, all persist only happen when user submit whole page. So
     * this remove is just impact HttpSession values.
     */
    @RequestMapping(path = "/removeItem", method = RequestMethod.POST)
    private String removeItem(@ModelAttribute ResourceItemForm resourceItemForm, HttpServletRequest request) {
	SessionMap<String, Object> sessionMap = getSessionMap(request);

	@SuppressWarnings("deprecation")
	int itemIdx = NumberUtils.stringToInt(request.getParameter(ResourceConstants.PARAM_ITEM_INDEX), -1);
	if (itemIdx != -1) {
	    SortedSet<ResourceItem> resourceList = getResourceItemList(sessionMap);
	    List<ResourceItem> rList = new ArrayList<>(resourceList);
	    ResourceItem item = rList.remove(itemIdx);
	    resourceList.clear();
	    resourceList.addAll(rList);
	    // add to delList
	    List<ResourceItem> delList = getDeletedResourceItemList(sessionMap);
	    delList.add(item);
	}

	return "pages/authoring/parts/itemlist";
    }

    /**
     * Display edit page for existed resource item.
     *
     * @param resourceItemForm
     * @param request
     * @return
     */
    @RequestMapping("/editItemInit")
    private String editItemInit(@ModelAttribute ResourceItemForm resourceItemForm, HttpServletRequest request) {
	SessionMap<String, Object> sessionMap = getSessionMap(request);

	int itemIdx = NumberUtils.stringToInt(request.getParameter(ResourceConstants.PARAM_ITEM_INDEX), -1);
	ResourceItem item = null;
	if (itemIdx != -1) {
	    SortedSet<ResourceItem> resourceList = getResourceItemList(sessionMap);
	    List<ResourceItem> rList = new ArrayList<>(resourceList);
	    item = rList.get(itemIdx);
	    if (item != null) {
		populateItemToForm(itemIdx, item, resourceItemForm, request);
	    }
	}
	switch (item.getType()) {
	    case 1:
		return "pages/authoring/parts/addurl";
	    case 2:
		return "pages/authoring/parts/addfile";
	    case 3:
		return "pages/authoring/parts/addwebsite";
	    case 4:
		return "pages/authoring/parts/addlearningobject";
	    default:
		throw new IllegalArgumentException("Unknown item type" + item.getType());
	}
    }

    /**
     * Display empty page for new resource item.
     */
    @RequestMapping("/newItemInit")
    private String newItemlInit(@ModelAttribute ResourceItemForm resourceItemForm, HttpServletRequest request) {
	String sessionMapID = WebUtil.readStrParam(request, ResourceConstants.ATTR_SESSION_MAP_ID);
	resourceItemForm.setSessionMapID(sessionMapID);

	short type = (short) WebUtil.readIntParam(request, AuthoringController.ITEM_TYPE);
	List<String> instructionList = new ArrayList<>(AuthoringController.INIT_INSTRUCTION_COUNT);
	for (int idx = 0; idx < AuthoringController.INIT_INSTRUCTION_COUNT; idx++) {
	    instructionList.add("");
	}
	request.setAttribute("instructionList", instructionList);
	request.setAttribute("resourceItemForm", resourceItemForm);
	switch (type) {
	    case 1:
		return "pages/authoring/parts/addurl";
	    case 2:
		return "pages/authoring/parts/addfile";
	    case 3:
		return "pages/authoring/parts/addwebsite";
	    case 4:
		return "pages/authoring/parts/addlearningobject";
	    default:
		throw new IllegalArgumentException("Unknown item type" + type);
	}
    }

    /**
     * This method will get necessary information from resource item form and
     * save or update into <code>HttpSession</code> ResourceItemList. Notice,
     * this save is not persist them into database, just save
     * <code>HttpSession</code> temporarily. Only they will be persist when the
     * entire authoring page is being persisted.
     */
    @RequestMapping(path = "/saveOrUpdateItem", method = RequestMethod.POST)
    private String saveOrUpdateItem(@ModelAttribute ResourceItemForm resourceItemForm, HttpServletRequest request) {
	// get instructions:
	List<String> instructionList = getInstructionsFromRequest(request);
	MultiValueMap<String, String> errorMap = new LinkedMultiValueMap<>();
	validateResourceItem(resourceItemForm, errorMap);

	if (!errorMap.isEmpty()) {
	    request.setAttribute("errorMap", errorMap);
	    request.setAttribute(ResourceConstants.ATTR_INSTRUCTION_LIST, instructionList);
	    request.setAttribute("resourceItemForm", resourceItemForm);
	    switch (resourceItemForm.getItemType()) {
		case 1:
		    return "pages/authoring/parts/addurl";
		case 2:
		    return "pages/authoring/parts/addfile";
		case 3:
		    return "pages/authoring/parts/addwebsite";
		case 4:
		    return "pages/authoring/parts/addlearningobject";
		default:
		    throw new IllegalArgumentException("Unknown item type" + resourceItemForm.getItemType());
	    }

	}

	try {
	    extractFormToResourceItem(request, instructionList, resourceItemForm);
	} catch (Exception e) {
	    // any upload exception will display as normal error message rather
	    // then throw exception directly
	    errorMap.add("GLOBAL", messageService.getMessage(ResourceConstants.ERROR_MSG_UPLOAD_FAILED,
		    new Object[] { e.getMessage() }));
	    if (!errorMap.isEmpty()) {
		request.setAttribute("errorMap", errorMap);
		request.setAttribute(ResourceConstants.ATTR_INSTRUCTION_LIST, instructionList);
		switch (resourceItemForm.getItemType()) {
		    case 1:
			return "pages/authoring/parts/addurl";
		    case 2:
			return "pages/authoring/parts/addfile";
		    case 3:
			return "pages/authoring/parts/addwebsite";
		    case 4:
			return "pages/authoring/parts/addlearningobject";
		    default:
			throw new IllegalArgumentException("Unknown item type" + resourceItemForm.getItemType());
		}
	    }
	}
	// set session map ID so that itemlist.jsp can get sessionMAP
	request.setAttribute(ResourceConstants.ATTR_SESSION_MAP_ID, resourceItemForm.getSessionMapID());
	// return null to close this window
	return "pages/authoring/parts/itemlist";
    }

    /**
     * Ajax call, will add one more input line for new resource item
     * instruction.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/newInstruction")
    private String newInstruction(HttpServletRequest request) {
	int numberOfInstructions = WebUtil.readIntParam(request, INSTRUCTION_ITEM_COUNT);
	List<String> instructionList = new ArrayList<>(++numberOfInstructions);
	for (int idx = 0; idx < numberOfInstructions; idx++) {
	    String item = request.getParameter(AuthoringController.INSTRUCTION_ITEM_DESC_PREFIX + idx);
	    if (item == null) {
		instructionList.add("");
	    } else {
		instructionList.add(item);
	    }
	}
	request.setAttribute(ResourceConstants.ATTR_INSTRUCTION_LIST, instructionList);
	return "pages/authoring/parts/instructions";
    }

    /**
     * Ajax call, remove the given line of instruction of resource item.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/removeInstruction")
    private String removeInstruction(HttpServletRequest request) {
	int numberOfInstructions = WebUtil.readIntParam(request, INSTRUCTION_ITEM_COUNT);
	int removeIdx = WebUtil.readIntParam(request, "removeIdx");
	List<String> instructionList = new ArrayList<>(numberOfInstructions - 1);
	for (int idx = 0; idx < numberOfInstructions; idx++) {
	    String item = request.getParameter(AuthoringController.INSTRUCTION_ITEM_DESC_PREFIX + idx);
	    if (idx == removeIdx) {
		continue;
	    }
	    if (item == null) {
		instructionList.add("");
	    } else {
		instructionList.add(item);
	    }
	}
	request.setAttribute(ResourceConstants.ATTR_INSTRUCTION_LIST, instructionList);
	return "pages/authoring/parts/instructions";
    }

    /**
     * Read resource data from database and put them into HttpSession. It will
     * redirect to init.do directly after this method run successfully.
     *
     * This method will avoid read database again and lost un-saved resouce item
     * lost when user "refresh page",
     *
     * @throws ServletException
     *
     */
    @RequestMapping(value = "/start")
    private String start(@ModelAttribute("startForm") ResourceForm startForm, HttpServletRequest request)
	    throws ServletException {

	ToolAccessMode mode = WebUtil.readToolAccessModeAuthorDefaulted(request);
	request.setAttribute(AttributeNames.ATTR_MODE, mode.toString());

	return readDatabaseData(startForm, request);
    }

    @RequestMapping(path = "/definelater", method = RequestMethod.POST)
    private String defineLater(@ModelAttribute("startForm") ResourceForm startForm, HttpServletRequest request)
	    throws ServletException {
	Long contentId = WebUtil.readLongParam(request, AttributeNames.PARAM_TOOL_CONTENT_ID);
	Resource resource = resourceService.getResourceByContentId(contentId);

	resource.setDefineLater(true);
	resourceService.saveOrUpdateResource(resource);

	//audit log the teacher has started editing activity in monitor
	resourceService.auditLogStartEditingActivityInMonitor(contentId);

	request.setAttribute(AttributeNames.ATTR_MODE, ToolAccessMode.TEACHER.toString());

	return readDatabaseData(startForm, request);
    }

    /**
     * Common method for "start" and "defineLater"
     */
    private String readDatabaseData(ResourceForm startForm, HttpServletRequest request) throws ServletException {
	// save toolContentID into HTTPSession
	Long contentId = WebUtil.readLongParam(request, ResourceConstants.PARAM_TOOL_CONTENT_ID);

	// get back the resource and item list and display them on page

	List<ResourceItem> items = null;
	Resource resource = null;

	// Get contentFolderID and save to form.
	String contentFolderID = WebUtil.readStrParam(request, AttributeNames.PARAM_CONTENT_FOLDER_ID);
	startForm.setContentFolderID(contentFolderID);

	// initial Session Map
	SessionMap<String, Object> sessionMap = new SessionMap<>();
	request.getSession().setAttribute(sessionMap.getSessionID(), sessionMap);
	startForm.setSessionMapID(sessionMap.getSessionID());

	try {
	    resource = resourceService.getResourceByContentId(contentId);
	    // if resource does not exist, try to use default content instead.
	    if (resource == null) {
		resource = resourceService.getDefaultContent(contentId);
		if (resource.getResourceItems() != null) {
		    items = new ArrayList<ResourceItem>(resource.getResourceItems());
		} else {
		    items = null;
		}
	    } else {
		items = resourceService.getAuthoredItems(resource.getUid());
	    }

	    startForm.setResource(resource);
	} catch (Exception e) {
	    AuthoringController.log.error(e);
	    throw new ServletException(e);
	}

	// init it to avoid null exception in following handling
	if (items == null) {
	    items = new ArrayList<>();
	} else {
	    ResourceUser resourceUser = null;
	    // handle system default question: createBy is null, now set it to
	    // current user
	    for (ResourceItem item : items) {
		if (item.getCreateBy() == null) {
		    if (resourceUser == null) {
			// get back login user DTO
			HttpSession ss = SessionManager.getSession();
			UserDTO user = (UserDTO) ss.getAttribute(AttributeNames.USER);
			resourceUser = new ResourceUser(user, resource);
		    }
		    item.setCreateBy(resourceUser);
		}
	    }
	}
	// init resource item list
	SortedSet<ResourceItem> resourceItemList = getResourceItemList(sessionMap);
	resourceItemList.clear();
	resourceItemList.addAll(items);

	// If there is no order id, set it up
	int i = 1;
	for (ResourceItem resourceItem : resourceItemList) {
	    if (resourceItem.getOrderId() == null || resourceItem.getOrderId() != i) {
		resourceItem.setOrderId(i);
	    }
	    i++;
	}

	sessionMap.put(ResourceConstants.ATTR_RESOURCE_FORM, startForm);
	request.getSession().setAttribute(AttributeNames.PARAM_NOTIFY_CLOSE_URL,
		request.getParameter(AttributeNames.PARAM_NOTIFY_CLOSE_URL));
	return "pages/authoring/start";
    }

    /**
     * Display same entire authoring page content from HttpSession variable.
     */
    @RequestMapping("/init")
    private String initPage(@ModelAttribute("startForm") ResourceForm startForm, HttpServletRequest request)
	    throws ServletException {
	SessionMap<String, Object> sessionMap = getSessionMap(request);
	ResourceForm existForm = (ResourceForm) sessionMap.get(ResourceConstants.ATTR_RESOURCE_FORM);

	try {
	    PropertyUtils.copyProperties(startForm, existForm);
	} catch (Exception e) {
	    throw new ServletException(e);
	}

	ToolAccessMode mode = WebUtil.readToolAccessModeAuthorDefaulted(request);
	startForm.setMode(mode.toString());
	request.setAttribute("authoringForm", startForm);
	request.setAttribute(AttributeNames.ATTR_MODE, mode.toString());

	return "pages/authoring/authoring";
    }

    /**
     * This method will persist all inforamtion in this authoring page, include
     * all resource item, information etc.
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    private String updateContent(@ModelAttribute("authoringForm") ResourceForm authoringForm, HttpServletRequest request)
	    throws Exception {
	SessionMap<String, Object> sessionMap = (SessionMap<String, Object>) request.getSession()
		.getAttribute(authoringForm.getSessionMapID());

	ToolAccessMode mode = WebUtil.readToolAccessModeAuthorDefaulted(request);
	request.setAttribute(AttributeNames.ATTR_MODE, mode.toString());

	Resource resource = authoringForm.getResource();

	// **********************************Get Resource PO*********************
	Resource resourcePO = resourceService.getResourceByContentId(resource.getContentId());
	if (resourcePO == null) {
	    // new Resource, create it
	    resourcePO = resource;
	    resourcePO.setCreated(new Timestamp(new Date().getTime()));
	    resourcePO.setUpdated(new Timestamp(new Date().getTime()));

	} else {
	    Set<LearnerItemRatingCriteria> criterias = resourcePO.getRatingCriterias();
	    Long uid = resourcePO.getUid();
	    PropertyUtils.copyProperties(resourcePO, resource);

	    // copyProperties() above may result in "collection assigned to two objects in a session" exception
	    resourceService.evict(resource);
	    authoringForm.setResource(null);
	    resource = null;
	    // set back UID && rating criteria
	    resourcePO.setUid(uid);
	    resourcePO.setRatingCriterias(criterias);

	    // if it's a Teacher (from monitor) - change define later status
	    if (mode.isTeacher()) {
		resourcePO.setDefineLater(false);
	    }

	    resourcePO.setUpdated(new Timestamp(new Date().getTime()));
	}

	// *******************************Handle user*******************
	// try to get form system session
	HttpSession ss = SessionManager.getSession();
	// get back login user DTO
	UserDTO user = (UserDTO) ss.getAttribute(AttributeNames.USER);
	ResourceUser resourceUser = resourceService.getUserByIDAndContent(user.getUserID().longValue(),
		resourcePO.getContentId());
	if (resourceUser == null) {
	    resourceUser = new ResourceUser(user, resourcePO);
	}

	resourcePO.setCreatedBy(resourceUser);

	// ************************* Handle resource items *******************
	// Handle resource items
	boolean useRatings = false;
	Set<ResourceItem> itemList = new LinkedHashSet<>();
	SortedSet<ResourceItem> topics = getResourceItemList(sessionMap);
	Iterator<ResourceItem> iter = topics.iterator();
	while (iter.hasNext()) {
	    ResourceItem item = iter.next();
	    if (item != null) {
		// This flushs user UID info to message if this user is a new
		// user.
		item.setCreateBy(resourceUser);
		itemList.add(item);
		useRatings = useRatings || item.isAllowRating();
	    }
	}
	resourcePO.setResourceItems(itemList);
	// delete instruction file from database.
	List<ResourceItem> delResourceItemList = getDeletedResourceItemList(sessionMap);
	iter = delResourceItemList.iterator();
	while (iter.hasNext()) {
	    ResourceItem item = (ResourceItem) iter.next();
	    iter.remove();
	    if (item.getUid() != null) {
		resourceService.deleteResourceItem(item.getUid());
	    }
	}
	// handle resource item attachment file:
	List<ResourceItem> delItemAttList = getDeletedItemAttachmentList(sessionMap);
	iter = delItemAttList.iterator();
	while (iter.hasNext()) {
	    ResourceItem delAtt = (ResourceItem) iter.next();
	    iter.remove();
	}

	// if miniview number is bigger than available items, then set it topics
	// size
	if (resourcePO.getMiniViewResourceNumber() > topics.size()) {
	    resourcePO.setMiniViewResourceNumber(topics.size());
	}
	// **********************************************
	// finally persist resourcePO again

	resourceService.saveOrUpdateResource(resourcePO);

	// Set up rating criteria. Do not delete existing criteria as this will destroy ratings already done
	// if the monitor edits the activity and turns of the criteria temporarily.
	if (useRatings) {
	    if (resourcePO.getRatingCriterias() == null || resourcePO.getRatingCriterias().size() == 0) {
		LearnerItemRatingCriteria newCriteria = resourceService.createRatingCriteria(resourcePO.getContentId());
		if (resourcePO.getRatingCriterias() == null) {
		    resourcePO.setRatingCriterias(new HashSet<LearnerItemRatingCriteria>());
		}
		resourcePO.getRatingCriterias().add(newCriteria);
	    }
	}
	authoringForm.setResource(resourcePO);

	request.setAttribute(CommonConstants.LAMS_AUTHORING_SUCCESS_FLAG, Boolean.TRUE);

	return "pages/authoring/authoring";
    }

    // *************************************************************************************
    // Private method
    // *************************************************************************************

    /**
     * List save current resource items.
     *
     * @param request
     * @return
     */
    @SuppressWarnings("unchecked")
    private SortedSet<ResourceItem> getResourceItemList(SessionMap<String, Object> sessionMap) {
	SortedSet<ResourceItem> list = (SortedSet<ResourceItem>) sessionMap
		.get(ResourceConstants.ATTR_RESOURCE_ITEM_LIST);
	if (list == null) {
	    list = new TreeSet<>(new ResourceItemComparator());
	    sessionMap.put(ResourceConstants.ATTR_RESOURCE_ITEM_LIST, list);
	}
	return list;
    }

    /**
     * List save deleted resource items, which could be persisted or
     * non-persisted items.
     *
     * @param request
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<ResourceItem> getDeletedResourceItemList(SessionMap<String, Object> sessionMap) {
	return getListFromSession(sessionMap, ResourceConstants.ATTR_DELETED_RESOURCE_ITEM_LIST);
    }

    /**
     * If a resource item has attahment file, and the user edit this item and
     * change the attachment to new file, then the old file need be deleted when
     * submitting the whole authoring page. Save the file uuid and version id
     * into ResourceItem object for temporarily use.
     */
    @SuppressWarnings("unchecked")
    private List<ResourceItem> getDeletedItemAttachmentList(SessionMap<String, Object> sessionMap) {
	return getListFromSession(sessionMap, ResourceConstants.ATTR_DELETED_RESOURCE_ITEM_ATTACHMENT_LIST);
    }

    /**
     * Get <code>java.util.List</code> from HttpSession by given name.
     */
    @SuppressWarnings("rawtypes")
    private List getListFromSession(SessionMap<String, Object> sessionMap, String name) {
	List list = (List) sessionMap.get(name);
	if (list == null) {
	    list = new ArrayList();
	    sessionMap.put(name, list);
	}
	return list;
    }

    /**
     * Get resource items instruction from <code>HttpRequest</code>
     *
     * @param request
     */
    private List<String> getInstructionsFromRequest(HttpServletRequest request) {
	String list = request.getParameter("instructionList");
	String[] params = list.split("&");
	Map<String, String> paramMap = new HashMap<>();
	String[] pair;
	for (String item : params) {
	    pair = item.split("=");
	    if (pair == null || pair.length != 2) {
		continue;
	    }
	    try {
		paramMap.put(pair[0], URLDecoder.decode(pair[1], "UTF-8"));
	    } catch (UnsupportedEncodingException e) {
		AuthoringController.log.error("Error occurs when decode instruction string:" + e.toString());
	    }
	}

	int count = paramMap.keySet().size();
	List<String> instructionList = new ArrayList<>();

	for (int idx = 0; idx < count; idx++) {
	    String item = paramMap.get(AuthoringController.INSTRUCTION_ITEM_DESC_PREFIX + idx);
	    if (item == null) {
		continue;
	    }

	    instructionList.add(item);
	}

	return instructionList;

    }

    /**
     * This method will populate resource item information to its form for edit
     * use.
     *
     * @param itemIdx
     * @param item
     * @param form
     * @param request
     */
    private void populateItemToForm(int itemIdx, ResourceItem item, ResourceItemForm form, HttpServletRequest request) {
	form.setDescription(item.getDescription());
	form.setTitle(item.getTitle());
	form.setUrl(item.getUrl());
	form.setOpenUrlNewWindow(item.isOpenUrlNewWindow());
	form.setAllowRating(item.isAllowRating());
	form.setAllowComments(item.isAllowComments());
	if (itemIdx >= 0) {
	    form.setItemIndex(String.valueOf(itemIdx));
	}

	Set<ResourceItemInstruction> instructionList = item.getItemInstructions();
	List<String> instructions = new ArrayList<>();
	for (ResourceItemInstruction in : instructionList) {
	    instructions.add(in.getDescription());
	}
	// FOR requirment from LDEV-754
	// add extra blank line for instructions
	// for(int idx=0;idx<INIT_INSTRUCTION_COUNT;idx++){
	// instructions.add("");
	// }
	if (item.getFileUuid() != null) {
	    form.setFileUuid(item.getFileUuid());
	    form.setFileVersionId(item.getFileVersionId());
	    form.setFileName(item.getFileName());
	    form.setHasFile(true);
	} else {
	    form.setHasFile(false);
	}

	request.setAttribute(ResourceConstants.ATTR_INSTRUCTION_LIST, instructions);
    }

    /**
     * Extract web from content to resource item.
     *
     * BE CAREFUL: This method will copy nessary info from request form to a
     * old or new ResourceItem instance. It gets all info EXCEPT
     * ResourceItem.createDate and ResourceItem.createBy, which need be set
     * when persisting this resource item.
     */
    private void extractFormToResourceItem(HttpServletRequest request, List<String> instructionList,
	    ResourceItemForm itemForm) throws Exception {

	SessionMap<String, Object> sessionMap = (SessionMap<String, Object>) request.getSession()
		.getAttribute(itemForm.getSessionMapID());
	// check whether it is "edit(old item)" or "add(new item)"
	SortedSet<ResourceItem> resourceList = getResourceItemList(sessionMap);
	int itemIdx = NumberUtils.stringToInt(itemForm.getItemIndex(), -1);
	ResourceItem item = null;

	if (itemIdx == -1) { // add
	    item = new ResourceItem();
	    item.setCreateDate(new Timestamp(new Date().getTime()));
	    item.setOrderId(resourceList.size() + 1);
	    
	} else { // edit
	    List<ResourceItem> rList = new ArrayList<>(resourceList);
	    item = rList.get(itemIdx);
	}
	short type = itemForm.getItemType();
	item.setType(itemForm.getItemType());
	/*
	 * Set following fields regards to the type: item.setFileUuid();
	 * item.setFileVersionId(); item.setFileType(); item.setFileName();
	 *
	 * item.getInitialItem() item.setImsSchema() item.setOrganizationXml()
	 */
	// if the item is edit (not new add) then the getFile may return null
	// it may throw exception, so put it as first, to avoid other invlidate
	// update:
	if (itemForm.getFile() != null) {
	    if (type == ResourceConstants.RESOURCE_TYPE_WEBSITE
		    || type == ResourceConstants.RESOURCE_TYPE_LEARNING_OBJECT
		    || type == ResourceConstants.RESOURCE_TYPE_FILE) {
		// if it has old file, and upload a new, then save old to deleteList
		ResourceItem delAttItem = new ResourceItem();
		boolean hasOld = false;
		if (item.getFileUuid() != null) {
		    hasOld = true;
		    // be careful, This new ResourceItem object never be save
		    // into database
		    // just temporarily use for saving fileUuid and versionID
		    // use:
		    delAttItem.setFileUuid(item.getFileUuid());
		    delAttItem.setFileVersionId(item.getFileVersionId());
		}

		//throws UploadResourceFileException
		resourceService.uploadResourceItemFile(item, itemForm.getFile());
		
		// put it after "upload" to ensure deleted file added into list
		// only no exception happens during upload
		if (hasOld) {
		    List<ResourceItem> delAtt = getDeletedItemAttachmentList(sessionMap);
		    delAtt.add(delAttItem);
		}
	    }
	}
	item.setTitle(itemForm.getTitle());
	item.setCreateByAuthor(true);
	item.setHide(false);
	item.setAllowRating(itemForm.isAllowRating());
	item.setAllowComments(itemForm.isAllowComments());
	// set instructions
	Set<ResourceItemInstruction> instructions = new LinkedHashSet<>();
	int idx = 0;
	for (String ins : instructionList) {
	    ResourceItemInstruction instruction = new ResourceItemInstruction();
	    instruction.setDescription(ins);
	    instruction.setSequenceId(idx++);
	    instructions.add(instruction);
	}
	item.setItemInstructions(instructions);

	if (type == ResourceConstants.RESOURCE_TYPE_URL) {
	    item.setUrl(itemForm.getUrl());
	}
	if (type == ResourceConstants.RESOURCE_TYPE_URL || type == ResourceConstants.RESOURCE_TYPE_FILE) {
	    item.setOpenUrlNewWindow(itemForm.isOpenUrlNewWindow());
	}
	item.setDescription(itemForm.getDescription());

	// if it's a new item, add it to resourceList
	if (itemIdx == -1) {
	    resourceList.add(item);
	}
    }

    /**
     * Vaidate resource item regards to their type (url/file/learning
     * object/website zip file)
     *
     * @param resourceItemForm
     * @return
     */
    private void validateResourceItem(ResourceItemForm resourceItemForm, MultiValueMap<String, String> errorMap) {
	if (StringUtils.isBlank(resourceItemForm.getTitle())) {
	    errorMap.add("GLOBAL", messageService.getMessage(ResourceConstants.ERROR_MSG_TITLE_BLANK));
	}

	if (resourceItemForm.getItemType() == ResourceConstants.RESOURCE_TYPE_URL) {
	    if (StringUtils.isBlank(resourceItemForm.getUrl())) {
		errorMap.add("GLOBAL", messageService.getMessage(ResourceConstants.ERROR_MSG_URL_BLANK));
		// URL validation: Commom URL validate(1.3.0) work not very
		// well: it can not support http://
		// address:port format!!!
		// UrlValidator validator = new UrlValidator();
		// if(!validator.isValid(itemForm.getUrl()))
		// errors.add(ActionMessages.GLOBAL_MESSAGE,new
		// ActionMessage(ResourceConstants.ERROR_MSG_INVALID_URL));
	    }
	}
	// if(itemForm.getItemType() == ResourceConstants.RESOURCE_TYPE_WEBSITE
	// ||itemForm.getItemType() ==
	// ResourceConstants.RESOURCE_TYPE_LEARNING_OBJECT){
	// if(StringUtils.isBlank(itemForm.getDescription()))
	// errors.add(ActionMessages.GLOBAL_MESSAGE,new
	// ActionMessage(ResourceConstants.ERROR_MSG_DESC_BLANK));
	// }
	if (resourceItemForm.getItemType() == ResourceConstants.RESOURCE_TYPE_WEBSITE
		|| resourceItemForm.getItemType() == ResourceConstants.RESOURCE_TYPE_LEARNING_OBJECT
		|| resourceItemForm.getItemType() == ResourceConstants.RESOURCE_TYPE_FILE) {
	    // validate item size
	    if (!FileValidatorUtil.validateFileSize(resourceItemForm.getFile(), false)) {
		errorMap.add("GLOBAL", messageService.getMessage("errors.maxfilesize",
			new Object[] { Configuration.getAsInt(ConfigurationKeys.UPLOAD_FILE_MAX_SIZE) }));
	    }

	    // for edit validate: file already exist
	    if (!resourceItemForm.isHasFile() && (resourceItemForm.getFile() == null
		    || StringUtils.isEmpty(resourceItemForm.getFile().getOriginalFilename()))) {
		errorMap.add("GLOBAL", messageService.getMessage(ResourceConstants.ERROR_MSG_FILE_BLANK));
	    }
	}
    }

    @RequestMapping("/initPedagogicalPlannerForm")
    public String initPedagogicalPlannerForm(ResourcePedagogicalPlannerForm pedagogicalPlannerForm,
	    HttpServletRequest request) {
	Long toolContentID = WebUtil.readLongParam(request, AttributeNames.PARAM_TOOL_CONTENT_ID);
	Resource taskList = resourceService.getResourceByContentId(toolContentID);
	pedagogicalPlannerForm.fillForm(taskList);
	String contentFolderId = WebUtil.readStrParam(request, AttributeNames.PARAM_CONTENT_FOLDER_ID);
	pedagogicalPlannerForm.setContentFolderID(contentFolderId);
	return "pages/authoring/pedagogicalPlannerForm";
    }

    @RequestMapping(value = "/saveOrUpdatePedagogicalPlannerForm", method = RequestMethod.POST)
    public String saveOrUpdatePedagogicalPlannerForm(ResourcePedagogicalPlannerForm pedagogicalPlannerForm,
	    HttpServletRequest request) throws IOException {

	MultiValueMap<String, String> errorMap = new LinkedMultiValueMap<>();
	pedagogicalPlannerForm.validate(messageService);
	if (errorMap.isEmpty()) {
	    Resource taskList = resourceService.getResourceByContentId(pedagogicalPlannerForm.getToolContentID());
	    taskList.setInstructions(pedagogicalPlannerForm.getInstructions());

	    int itemIndex = 0;
	    String title = null;
	    ResourceItem resourceItem = null;
	    List<ResourceItem> newItems = new LinkedList<>();
	    // we need a copy for later Hibernate-bound processing
	    LinkedList<ResourceItem> resourceItems = new LinkedList<ResourceItem>(taskList.getResourceItems());
	    Iterator<ResourceItem> taskListItemIterator = resourceItems.iterator();
	    /*
	     * Not the case anymore (why?):
	     * We need to reverse the order, since the items are delivered newest-first
	     * LinkedList<ResourceItem> reversedResourceItems = new LinkedList<ResourceItem>();
	     * while (taskListItemIterator.hasNext()) {
	     * reversedResourceItems.addFirst(taskListItemIterator.next());
	     * }
	     * taskListItemIterator = reversedResourceItems.iterator();
	     */
	    do {
		title = pedagogicalPlannerForm.getTitle(itemIndex);
		if (StringUtils.isEmpty(title)) {
		    pedagogicalPlannerForm.removeItem(itemIndex);
		} else {
		    if (taskListItemIterator.hasNext()) {
			resourceItem = taskListItemIterator.next();
		    } else {
			resourceItem = new ResourceItem();
			resourceItem.setCreateByAuthor(true);
			Date currentDate = new Date();
			resourceItem.setCreateDate(currentDate);

			HttpSession session = SessionManager.getSession();
			UserDTO user = (UserDTO) session.getAttribute(AttributeNames.USER);
			ResourceUser taskListUser = resourceService.getUserByIDAndContent(user.getUserID().longValue(),
				pedagogicalPlannerForm.getToolContentID());
			resourceItem.setCreateBy(taskListUser);

			newItems.add(resourceItem);
		    }
		    resourceItem.setTitle(title);
		    Short type = pedagogicalPlannerForm.getType(itemIndex);
		    resourceItem.setType(type);
		    boolean hasFile = resourceItem.getFileUuid() != null;
		    if (type.equals(ResourceConstants.RESOURCE_TYPE_URL)) {
			resourceItem.setUrl(pedagogicalPlannerForm.getUrl(itemIndex));
			if (hasFile) {
			    resourceItem.setFileName(null);
			    resourceItem.setFileUuid(null);
			    resourceItem.setFileVersionId(null);
			    resourceItem.setFileType(null);
			}
		    } else if (type.equals(ResourceConstants.RESOURCE_TYPE_FILE)) {
			MultipartFile file = pedagogicalPlannerForm.getFile(itemIndex);
			resourceItem.setUrl(null);
			if (file != null && !StringUtils.isEmpty(file.getOriginalFilename())) {
			    try {
				if (hasFile) {
				    // delete the old file
				    resourceService.deleteFromRepository(resourceItem.getFileUuid(),
					    resourceItem.getFileVersionId());
				}
				resourceService.uploadResourceItemFile(resourceItem, file);
			    } catch (Exception e) {
				AuthoringController.log.error(e);
				errorMap.add("GLOBAL", messageService.getMessage("error.msg.io.exception"));
				request.setAttribute("errorMap", errorMap);
				pedagogicalPlannerForm.setValid(false);
				return "pages/authoring/pedagogicalPlannerForm";
			    }
			    pedagogicalPlannerForm.setFileName(itemIndex, resourceItem.getFileName());
			    pedagogicalPlannerForm.setFileUuid(itemIndex, resourceItem.getFileUuid());
			    pedagogicalPlannerForm.setFileVersion(itemIndex, resourceItem.getFileVersionId());
			    pedagogicalPlannerForm.setFile(itemIndex, null);
			}
		    }
		    itemIndex++;
		}

	    } while (title != null);
	    // we need to clear it now, otherwise we get Hibernate error (item
	    // re-saved by cascade)
	    taskList.getResourceItems().clear();
	    while (taskListItemIterator.hasNext()) {
		resourceItem = taskListItemIterator.next();
		taskListItemIterator.remove();
		resourceService.deleteResourceItem(resourceItem.getUid());
	    }
	    resourceItems.addAll(newItems);

	    taskList.getResourceItems().addAll(resourceItems);
	    resourceService.saveOrUpdateResource(taskList);
	} else {
	    request.setAttribute("errorMap", errorMap);
	}
	return "pages/authoring/pedagogicalPlannerForm";
    }

    @RequestMapping("/createPedagogicalPlannerItem")
    public String createPedagogicalPlannerItem(ResourcePedagogicalPlannerForm pedagogicalPlannerForm,
	    HttpServletRequest request) throws IOException, ServletException {
	int insertIndex = pedagogicalPlannerForm.getItemCount();
	pedagogicalPlannerForm.setTitle(insertIndex, "");
	pedagogicalPlannerForm.setType(insertIndex,
		(short) WebUtil.readIntParam(request, ResourceConstants.ATTR_ADD_RESOURCE_TYPE));
	pedagogicalPlannerForm.setUrl(insertIndex, null);
	pedagogicalPlannerForm.setFileName(insertIndex, null);
	pedagogicalPlannerForm.setFile(insertIndex, null);
	pedagogicalPlannerForm.setFileUuid(insertIndex, null);
	pedagogicalPlannerForm.setFileVersion(insertIndex, null);
	return "pages/authoring/pedagogicalPlannerForm";
    }

    @RequestMapping("/switchResourceItemPosition")
    private String switchResourceItemPosition(HttpServletRequest request) {
	SessionMap<String, Object> sessionMap = getSessionMap(request);
	int resourceItemOrderID1 = WebUtil.readIntParam(request, "resourceItemOrderID1");
	int resourceItemOrderID2 = WebUtil.readIntParam(request, "resourceItemOrderID2");

	// check whether it is "edit(old item)" or "add(new item)"
	SortedSet<ResourceItem> resourceList = getResourceItemList(sessionMap);

	for (ResourceItem item : resourceList) {
	    if (item.getOrderId() == resourceItemOrderID1) {
		item.setOrderId(resourceItemOrderID2);
		continue;
	    }
	    if (item.getOrderId() == resourceItemOrderID2) {
		item.setOrderId(resourceItemOrderID1);
		continue;
	    }
	}

	SortedSet<ResourceItem> newItems = new TreeSet<>(new ResourceItemComparator());
	newItems.addAll(resourceList);
	sessionMap.put(ResourceConstants.ATTR_RESOURCE_ITEM_LIST, newItems);

	// return null to close this window
	return "pages/authoring/parts/itemlist";
    }
    
    @SuppressWarnings("unchecked")
    private SessionMap<String, Object> getSessionMap(HttpServletRequest request) {
	String sessionMapID = WebUtil.readStrParam(request, ResourceConstants.ATTR_SESSION_MAP_ID);
	request.setAttribute(ResourceConstants.ATTR_SESSION_MAP_ID, sessionMapID);
	return (SessionMap<String, Object>) request.getSession().getAttribute(sessionMapID);
    } 

}
