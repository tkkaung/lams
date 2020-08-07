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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 *
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */

package org.lamsfoundation.lams.learningdesign.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.lamsfoundation.lams.learningdesign.Activity;
import org.lamsfoundation.lams.learningdesign.ComplexActivity;
import org.lamsfoundation.lams.learningdesign.LearningDesign;
import org.lamsfoundation.lams.learningdesign.LearningLibrary;
import org.lamsfoundation.lams.learningdesign.LearningLibraryGroup;
import org.lamsfoundation.lams.learningdesign.ParallelActivity;
import org.lamsfoundation.lams.learningdesign.ToolActivity;
import org.lamsfoundation.lams.learningdesign.dao.IActivityDAO;
import org.lamsfoundation.lams.learningdesign.dao.IGroupingDAO;
import org.lamsfoundation.lams.learningdesign.dao.ILearningDesignDAO;
import org.lamsfoundation.lams.learningdesign.dao.ILearningLibraryDAO;
import org.lamsfoundation.lams.learningdesign.dto.AuthoringActivityDTO;
import org.lamsfoundation.lams.learningdesign.dto.LearningDesignDTO;
import org.lamsfoundation.lams.learningdesign.dto.LearningLibraryDTO;
import org.lamsfoundation.lams.learningdesign.dto.LibraryActivityDTO;
import org.lamsfoundation.lams.learningdesign.dto.ValidationErrorDTO;
import org.lamsfoundation.lams.tool.Tool;
import org.lamsfoundation.lams.tool.dao.IToolDAO;
import org.lamsfoundation.lams.tool.dto.ToolDTO;
import org.lamsfoundation.lams.tool.dto.ToolDTONameComparator;
import org.lamsfoundation.lams.usermanagement.User;
import org.lamsfoundation.lams.util.FileUtil;
import org.lamsfoundation.lams.util.ILoadedMessageSourceService;
import org.lamsfoundation.lams.util.MessageService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * The LearningDesignService class contains methods which applies validation rules to determine the validity of a
 * learning design. For the validation rules, please see the AuthoringDesignDoc in lams_documents.
 *
 * If no errors are found, a learning design is considered valid, it will set the valid_design_flag to true. If
 * validation fails, the validation messages will be returned in the response packet. The validation messages are a list
 * of ValidationErrorDTO objects.
 *
 * @author mtruong
 *
 */
public class LearningDesignService implements ILearningDesignService {

    protected Logger log = Logger.getLogger(LearningDesignService.class);
    protected MessageService messageService;

    protected ILearningDesignDAO learningDesignDAO;
    protected IActivityDAO activityDAO;
    protected IGroupingDAO groupingDAO;
    protected IToolDAO toolDAO;

    protected ILearningLibraryDAO learningLibraryDAO;
    protected ILoadedMessageSourceService toolActMessageService;

    /*
     * Default constructor
     *
     */
    public LearningDesignService() {
    }

    /**********************************************
     * Setter/Getter Methods
     *******************************************/
    /**
     * Set i18n MessageService
     */
    public void setMessageService(MessageService messageService) {
	this.messageService = messageService;
    }

    /**
     * Get i18n MessageService
     */
    public MessageService getMessageService() {
	return this.messageService;
    }

    /**
     * Access a message service related to a programatically loaded message file. Authoring uses this to access the
     * message files for tools and activities.
     */
    public ILoadedMessageSourceService getToolActMessageService() {
	return toolActMessageService;
    }

    public void setToolActMessageService(ILoadedMessageSourceService toolActMessageService) {
	this.toolActMessageService = toolActMessageService;
    }

    public void setLearningLibraryDAO(ILearningLibraryDAO learningLibraryDAO) {
	this.learningLibraryDAO = learningLibraryDAO;
    }

    public void setActivityDAO(IActivityDAO activityDAO) {
	this.activityDAO = activityDAO;
    }

    public void setLearningDesignDAO(ILearningDesignDAO learningDesignDAO) {
	this.learningDesignDAO = learningDesignDAO;
    }

    public void setGroupingDAO(IGroupingDAO groupingDAO) {
	this.groupingDAO = groupingDAO;
    }

    public void setToolDAO(IToolDAO toolDAO) {
	this.toolDAO = toolDAO;
    }

    /**********************************************
     * Service Methods
     *******************************************/

    @Override
    public LearningDesign getLearningDesign(Long learningDesignID) {
	return learningDesignDAO.getLearningDesignById(learningDesignID);
    }

    @Override
    public LearningDesignDTO getLearningDesignDTO(Long learningDesignID, String languageCode) {
	LearningDesign design = learningDesignID != null ? learningDesignDAO.getLearningDesignById(learningDesignID)
		: null;
	return design != null ? new LearningDesignDTO(design, activityDAO, groupingDAO, languageCode) : null;
    }

    @Override
    public Vector<ValidationErrorDTO> validateLearningDesign(LearningDesign learningDesign) {
	LearningDesignValidator validator = new LearningDesignValidator(learningDesign, messageService);
	return validator.validate();
    }

    @Override
    public void setValid(Long learningLibraryId, boolean valid) {
	LearningLibrary library = learningLibraryDAO.getLearningLibraryById(learningLibraryId);
	library.setValidLibrary(valid);
	learningLibraryDAO.update(library);
    }

    @Override
    public ArrayList<LearningLibraryDTO> getAllLearningLibraryDetails(String languageCode) throws IOException {
	// only return valid learning library
	return getAllLearningLibraryDetails(true, languageCode);
    }

    @Override
    public LearningLibrary getLearningLibrary(Long learningLibraryId) {
	return (LearningLibrary) learningDesignDAO.find(LearningLibrary.class, learningLibraryId);
    }

    @Override
    public List<LearningLibraryGroup> getLearningLibraryGroups() {
	return learningLibraryDAO.getLearningLibraryGroups();
    }

    @Override
    public void saveLearningLibraryGroups(Collection<LearningLibraryGroup> groups) {
	// find out which groups do not exist anymore and get rid of them
	Collection<LearningLibraryGroup> existingGroups = learningLibraryDAO.getLearningLibraryGroups();
	Collection<LearningLibraryGroup> removeGroups = new HashSet<LearningLibraryGroup>(existingGroups);
	removeGroups.removeAll(groups);
	existingGroups.removeAll(removeGroups);
	learningLibraryDAO.deleteAll(removeGroups);

	// find out which groups are new and which ones need to be updated
	for (LearningLibraryGroup group : groups) {
	    boolean found = false;
	    for (LearningLibraryGroup existingGroup : existingGroups) {
		if (existingGroup.equals(group)) {
		    existingGroup.setName(group.getName());
		    existingGroup.setLearningLibraries(group.getLearningLibraries());
		    learningLibraryDAO.update(existingGroup);

		    found = true;
		    break;
		}
	    }

	    if (!found) {
		learningDesignDAO.insert(group);
	    }
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<LearningLibraryDTO> getAllLearningLibraryDetails(boolean valid, String languageCode)
	    throws IOException {
	Iterator<LearningLibrary> iterator = learningLibraryDAO.getAllLearningLibraries(valid).iterator();
	ArrayList<LearningLibraryDTO> libraries = new ArrayList<LearningLibraryDTO>();
	while (iterator.hasNext()) {
	    LearningLibrary learningLibrary = iterator.next();
	    List<Activity> templateActivities = activityDAO
		    .getActivitiesByLibraryID(learningLibrary.getLearningLibraryId());

	    if ((templateActivities != null) & (templateActivities.size() == 0)) {
		log.error("Learning Library with ID " + learningLibrary.getLearningLibraryId() + " \""
			+ learningLibrary.getTitle() + "\" does not have a template activity");
	    }
	    // convert library to DTO format

	    LearningLibraryDTO libraryDTO = new LearningLibraryDTO(learningLibrary);
	    libraryDTO.setTemplateActivities(populateActivities(templateActivities.iterator(), languageCode));

	    internationaliseActivities(libraryDTO.getTemplateActivities());
	    libraries.add(libraryDTO);
	}
	return libraries;
    }

    private Vector populateActivities(Iterator iterator, String languageCode) {
	Vector activities = new Vector();
	Vector childActivities = null;
	while (iterator.hasNext()) {
	    Activity object = (Activity) iterator.next();

	    if (object.isComplexActivity()) { //parallel, sequence or options activity
		object = activityDAO.getActivityByActivityId(object.getActivityId());
		ComplexActivity complexActivity = (ComplexActivity) object;
		Iterator childIterator = complexActivity.getActivities().iterator();
		childActivities = new Vector();
		while (childIterator.hasNext()) {
		    Activity activity = (Activity) childIterator.next();
		    childActivities.add(activity.getLibraryActivityDTO(languageCode));
		}
		activities.add(complexActivity.getLibraryActivityDTO(languageCode));
		activities.addAll(childActivities);
	    } else {
		activities.add(object.getLibraryActivityDTO(languageCode));
	    }
	}
	return activities;
    }

    /**
     * Gets basic information on available tools.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<ToolDTO> getToolDTOs(boolean includeParallel, boolean includeInvalid, String userName)
	    throws IOException {
	User user = (User) learningLibraryDAO.findByProperty(User.class, "login", userName).get(0);
	String languageCode = user.getLocale().getLanguageIsoCode();
	ArrayList<LearningLibraryDTO> learningLibraries = getAllLearningLibraryDetails(false, languageCode);
	List<ToolDTO> tools = new ArrayList<ToolDTO>();
	for (LearningLibraryDTO learningLibrary : learningLibraries) {
	    // skip invalid tools
	    List<LibraryActivityDTO> libraryActivityDTOs = learningLibrary.getTemplateActivities();
	    if (libraryActivityDTOs.isEmpty()) {
		log.error("Learning Library with ID " + learningLibrary.getLearningLibraryID() + " \""
			+ learningLibrary.getTitle() + "\" does not have a template activity");
		continue;
	    }
	    boolean isParallel = libraryActivityDTOs.size() > 1;
	    if ((includeInvalid || learningLibrary.getValidFlag()) && (includeParallel || !isParallel)) {
		LibraryActivityDTO libraryActivityDTO = libraryActivityDTOs.get(0);
		ToolDTO toolDTO = new ToolDTO();
		if (isParallel) {
		    List<Long> childLibraryIDs = new ArrayList<Long>();
		    for (LibraryActivityDTO childActivityDTO : libraryActivityDTOs) {
			Long childToolID = childActivityDTO.getToolID();
			if (childToolID != null) {
			    childLibraryIDs.add(childToolID);
			}
		    }
		    toolDTO.setChildToolIds(childLibraryIDs.toArray(new Long[] {}));
		} else {
		    toolDTO.setToolId(libraryActivityDTO.getToolID());
		}
		toolDTO.setLearningLibraryId(learningLibrary.getLearningLibraryID());
		toolDTO.setLearningLibraryTitle(learningLibrary.getTitle());
		toolDTO.setToolDisplayName(libraryActivityDTO.getActivityTitle());
		toolDTO.setActivityCategoryID(
			isParallel ? Activity.CATEGORY_SPLIT : libraryActivityDTO.getActivityCategoryID());
		toolDTO.setValid(learningLibrary.getValidFlag());

		if (libraryActivityDTO.getToolID() == null) {
		    toolDTO.setIconPath(libraryActivityDTO.getLibraryActivityUIImage());
		} else {
		    Tool tool = (Tool) learningLibraryDAO.find(Tool.class, libraryActivityDTO.getToolID());
		    String iconPath = "tool/" + tool.getToolSignature() + "/images/icon_" + tool.getToolIdentifier()
			    + ".svg";
		    toolDTO.setIconPath(iconPath);
		    toolDTO.setSupportsOutputs(tool.getSupportsOutputs());
		    toolDTO.setDefaultToolContentId(tool.getDefaultToolContentId());
		}

		tools.add(toolDTO);
	    }
	}

	Collections.sort(tools, new ToolDTONameComparator());
	return tools;
    }

    /**
     * Guess missing Learning Library ID based on other activity details. Old LDs may not contain this value.
     */
    @Override
    public void fillLearningLibraryID(AuthoringActivityDTO activity, Collection<AuthoringActivityDTO> activities)
	    throws ImportToolContentException {
	if (activity.getLearningLibraryID() == null) {
	    switch (activity.getActivityTypeID()) {
		case Activity.PARALLEL_ACTIVITY_TYPE:
		    // find child activities referring to the imported parallel activity
		    List<AuthoringActivityDTO> components = new LinkedList<>();
		    for (AuthoringActivityDTO componentCandidate : activities) {
			if (componentCandidate.getParentUIID() != null
				&& componentCandidate.getParentUIID().equals(activity.getActivityUIID())) {
			    components.add(componentCandidate);
			    if (components.size() == 2) {
				break;
			    }
			}
		    }
		    // find matching parallel activity in existing DB
		    for (LearningLibrary library : learningLibraryDAO.getAllLearningLibraries()) {
			Activity libraryActivity = (Activity) activityDAO
				.getActivitiesByLibraryID(library.getLearningLibraryId()).get(0);
			if (!libraryActivity.isParallelActivity()) {
			    continue;
			}
			short componentsFound = 0;
			for (Activity libraryComponent : (Set<Activity>) ((ParallelActivity) libraryActivity)
				.getActivities()) {
			    ToolActivity toolLibraryCompoment = (ToolActivity) libraryComponent;
			    for (AuthoringActivityDTO component : components) {
				// match with tool signature of children
				if (toolLibraryCompoment.getTool().getToolSignature()
					.equals(component.getToolSignature())) {
				    componentsFound++;
				}
			    }
			}
			if (componentsFound == 2) {
			    activity.setLearningLibraryID(library.getLearningLibraryId());
			    break;
			}
		    }
		    if (activity.getLearningLibraryID() == null) {
			throw new ImportToolContentException(
				"Could not recognise learning library ID for parallel activity with UIID "
					+ activity.getActivityUIID());
		    }
		    break;
		case Activity.TOOL_ACTIVITY_TYPE:
		    // when importing a LD, it will overwritten by the import process
		    // when opening an old DB, this should work correctly
		    Tool tool = toolDAO.getToolByID(activity.getToolID());
		    if (tool != null) {
			activity.setLearningLibraryID(tool.getLearningLibraryId());
		    }
		    break;
	    }
	}
    }

    @Override
    public String internationaliseActivityTitle(Long learningLibraryID) {

	ToolActivity templateActivity = (ToolActivity) activityDAO.getTemplateActivityByLibraryID(learningLibraryID);

	if (templateActivity != null) {
	    Locale locale = LocaleContextHolder.getLocale();
	    String languageFilename = templateActivity.getLanguageFile();
	    if (languageFilename != null) {
		MessageSource toolMessageSource = toolActMessageService.getMessageService(languageFilename);
		if (toolMessageSource != null) {
		    String title = toolMessageSource.getMessage(Activity.I18N_TITLE, null, templateActivity.getTitle(),
			    locale);
		    if (title != null && title.trim().length() > 0) {
			return title;
		    }
		} else {
		    log.warn("Unable to internationalise the library activity " + templateActivity.getTitle()
			    + " message file " + templateActivity.getLanguageFile()
			    + ". Activity Message source not available");
		}
	    }
	}

	if (templateActivity.getTitle() != null && templateActivity.getTitle().trim().length() > 0) {
	    return templateActivity.getTitle();
	} else {
	    return "Untitled"; // should never get here - just return something in case there is a bug. A blank title affect the layout of the main page
	}
    }

    private void internationaliseActivities(Collection<LibraryActivityDTO> activities) {
	Iterator<LibraryActivityDTO> iter = activities.iterator();
	Locale locale = LocaleContextHolder.getLocale();

	if (log.isDebugEnabled()) {
	    if (locale == null) {
		log.debug("internationaliseActivities: Locale missing.");
	    }
	}

	while (iter.hasNext()) {
	    LibraryActivityDTO activity = iter.next();
	    // update the activity fields
	    String languageFilename = activity.getLanguageFile();
	    if (languageFilename != null) {
		MessageSource toolMessageSource = toolActMessageService.getMessageService(languageFilename);
		if (toolMessageSource != null) {
		    activity.setActivityTitle(toolMessageSource.getMessage(Activity.I18N_TITLE, null,
			    activity.getActivityTitle(), locale));
		    activity.setDescription(toolMessageSource.getMessage(Activity.I18N_DESCRIPTION, null,
			    activity.getDescription(), locale));
		} else {
		    log.warn("Unable to internationalise the library activity " + activity.getActivityID() + " "
			    + activity.getActivityTitle() + " message file " + activity.getLanguageFile()
			    + ". Activity Message source not available");
		}

		// update the tool field - note only tool activities have a tool entry.
		if ((activity.getActivityTypeID() != null)
			&& (Activity.TOOL_ACTIVITY_TYPE == activity.getActivityTypeID().intValue())) {
		    languageFilename = activity.getToolLanguageFile();
		    toolMessageSource = toolActMessageService.getMessageService(languageFilename);
		    if (toolMessageSource != null) {
			activity.setToolDisplayName(toolMessageSource.getMessage(Tool.I18N_DISPLAY_NAME, null,
				activity.getToolDisplayName(), locale));
		    } else {
			log.warn("Unable to internationalise the library activity " + activity.getActivityID() + " "
				+ activity.getActivityTitle() + " message file " + activity.getLanguageFile()
				+ ". Tool Message source not available");
		    }
		}
	    } else {
		log.warn("Unable to internationalise the library activity " + activity.getActivityID() + " "
			+ activity.getActivityTitle() + ". No message file supplied.");
	    }
	}
    }

    /**
     * Gets LD SVG file path.
     */
    public static String getLearningDesignSVGPath(long learningDesignId) {
	File thumbnailDir = new File(ILearningDesignService.LD_SVG_TOP_DIR);
	String thumbnailFileName = learningDesignId + ".svg";
	File thumbnailFile = new File(thumbnailDir, thumbnailFileName);
	if (!thumbnailFile.canRead()) {
	    // the file is missing, try the new folder structure
	    String thumbnailSubdir = String.valueOf(learningDesignId);
	    if (thumbnailSubdir.length() % 2 == 1) {
		thumbnailSubdir = "0" + thumbnailSubdir;
	    }
	    for (int charIndex = 0; charIndex < thumbnailSubdir.length(); charIndex += 2) {
		thumbnailDir = new File(thumbnailDir,
			"" + thumbnailSubdir.charAt(charIndex) + thumbnailSubdir.charAt(charIndex + 1));
	    }
	}
	return FileUtil.getFullPath(thumbnailDir.getAbsolutePath(), thumbnailFileName);
    }

    /**
     * Get a unique name for a learning design, based on the names of the learning designs in the folder. If the
     * learning design has duplicated name in same folder, then the new name will have a timestamp. The new name format
     * will be oldname_ddMMYYYY_idx. The idx will be auto incremental index number, start from 1. Warning - this may be
     * quite intensive as it gets all the learning designs in a folder. Moved from AuthoringService to here so that the
     * Import code can use it.
     *
     * @param originalLearningDesign
     * @param workspaceFolder
     * @param copyType
     * @return
     */
    @Override
    public String getUniqueNameForLearningDesign(String originalTitle, Integer workspaceFolderId) {

	String newName = originalTitle;
	if (workspaceFolderId != null) {
	    List<String> ldTitleList = learningDesignDAO.getLearningDesignTitlesByWorkspaceFolder(workspaceFolderId,
		    originalTitle);

	    if (ldTitleList.size() == 0) {
		return originalTitle;
	    }

	    Calendar calendar = Calendar.getInstance();
	    int mth = calendar.get(Calendar.MONTH) + 1;
	    String mthStr = new Integer(mth).toString();
	    if (mth < 10) {
		mthStr = "0" + mthStr;
	    }
	    int day = calendar.get(Calendar.DAY_OF_MONTH);
	    String dayStr = new Integer(day).toString();
	    if (day < 10) {
		dayStr = "0" + dayStr;
	    }
	    String nameMid = dayStr + mthStr + calendar.get(Calendar.YEAR);

	    int idx = 1;
	    while (ldTitleList.contains(newName)) {
		newName = originalTitle + "_" + nameMid + "_" + idx;
		idx++;
	    }
	}
	return newName;
    }

}