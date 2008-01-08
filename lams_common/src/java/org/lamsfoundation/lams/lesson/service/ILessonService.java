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
/* $Id$ */

package org.lamsfoundation.lams.lesson.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.lamsfoundation.lams.index.IndexLessonBean;
import org.lamsfoundation.lams.learningdesign.Activity;
import org.lamsfoundation.lams.learningdesign.Group;
import org.lamsfoundation.lams.learningdesign.Grouping;
import org.lamsfoundation.lams.learningdesign.GroupingActivity;
import org.lamsfoundation.lams.lesson.Lesson;
import org.lamsfoundation.lams.lesson.dto.LessonDTO;
import org.lamsfoundation.lams.lesson.dto.LessonDetailsDTO;
import org.lamsfoundation.lams.usermanagement.User;

/**
 * Access the general lesson details and access to grouping.
 * 
 * A lesson has two different "lists" of learners.
 * <OL>
 * <LI>The learners who are in the learner group attached to the lesson. This is fixed 
 * when the lesson is started and is a list of all the learners who could ever participate in
 * to the lesson. This is available via lesson.getAllLearners()
 * <LI>The learners who have started the lesson. They may or may not be logged in currently,
 * or if they are logged in they may or may not be doing this lesson. This is available
 * via getActiveLessonLearners().
 * </OL>
 * 
 * There used to be a list of all the learners who were logged into a lesson. This has been
 * removed as we do not need the functionality at present. If this is required later it should
 * be combined with the user's shared session logic and will need to purge users who haven't
 * done anything for a while - otherwise a user whose PC has crashed and then never returns
 * to a lesson will staying in the cache forever.
 */
public interface ILessonService {

	/** Get all the learners who have started the lesson. They may not be currently online.*/
	public abstract List getActiveLessonLearners(Long lessonId);

	/** 
	 * Get the count of all the learners who have started the lesson. They may not be currently online.
	 */
	public Integer getCountActiveLessonLearners(Long lessonId);
    
	/** Get the lesson details for the LAMS client. Suitable for the monitoring client.
	 * Contains a count of the total number of learners in the lesson and the number of active learners.
	 * This is a pretty intensive call as it counts all the learners in the 
	 * lessons' learner group, and determines the number of active learners.
	 * @param lessonId
	 * @return lesson details
	 */
	public abstract LessonDetailsDTO getLessonDetails(Long lessonId);

	/** Get the lesson object.
	 * @param lessonId
	 * @return lesson details
	 */
	public abstract Lesson getLesson(Long lessonId);

	/** Get the lesson details for the LAMS client. Suitable for the learner client.
	 * Contains a reduced number of fields compared to getLessonDetails.
	 * @param lessonId
	 * @return lesson details
	 */
	public abstract LessonDTO getLessonData(Long lessonId);

	/**
     * If the supplied learner is not already in a group, then perform grouping for 
     * the learners who have started the lesson, based on the grouping activity. 
     * Currently used for random grouping.
     * This method should be used when we do have an grouping activity and learner that is 
     * already part of the Hibernate session. (e.g. from the ForceComplete)
     * 
     * @param lessonId lesson id (mandatory)
     * @param groupingActivity the activity that has create grouping. (mandatory)
     * @param learner the learner to be check before grouping. (mandatory)
     */
    public void performGrouping(Long lessonId, GroupingActivity groupingActivity, User learner) throws LessonServiceException;

	/**
     * Perform the grouping, setting the given list of learners as one group. 
     * @param groupingActivity the activity that has create grouping. (mandatory)
     * @param groupName (optional)
     * @param learners to form one group (mandatory)
     */
  	public void performGrouping(GroupingActivity groupingActivity, String groupName, List learners) throws LessonServiceException;
  	
	/**
     * Perform the grouping, setting the given list of learners as one group.  Used in suitations
     * where there is a grouping but no grouping activity (e.g. in branching).
    * @param grouping the object on which to perform the grouing. (mandatory)
     * @param groupName (optional)
     * @param learners to form one group (mandatory)
     */
  	public void performGrouping(Grouping grouping, String groupName, List learners) throws LessonServiceException;  		

   /**
    * Perform grouping for all the learners who have started the lesson, based on the grouping. 
    * Currently used for chosen grouping and branching
    * @param lessonId lesson id (mandatory)
    * @param groupId group id (mandatory)
    * @param grouping the object on which to perform the grouing. (mandatory)
    */
   public void performGrouping(Grouping grouping, Long groupId, List learners) throws LessonServiceException;

    /**
     * Remove learners from the given group. 
     * @param grouping the grouping from which to remove the learners (mandatory)
     * @param groupName if not null only remove user from this group, if null remove learner from any group.
     * @param learners the learners to be removed (mandatory)
     */
    public void removeLearnersFromGroup(Grouping grouping, Long groupId, List<User> learners) throws LessonServiceException;
    
    /** Create an empty group for the given grouping. If the group name already exists 
     * then it will force the name to be unique.
     * 
     * @param grouping the grouping. (mandatory)
     * @param groupName (mandatory)
     * @return the new group
     */
    public Group createGroup(Grouping grouping, String name) throws LessonServiceException;

    /** 
     * Remove a group for the given grouping. If the group is already used (e.g. a tool session exists)
     * then it throws a GroupingException.
     *  
     * @param grouping the grouping that contains the group to remove. (mandatory)
     * @param groupName (mandatory)
     */
    public void removeGroup(Grouping grouping, Long groupId) throws LessonServiceException;
    
    /** 
     * Add a learner to the lesson class. Checks for duplicates.
     * @paran userId new learner id
     * @return true if added user, returns false if the user already a learner and hence not added.
     */ 
    public boolean addLearner(Long lessonId, Integer userId) throws LessonServiceException;

    /** 
     * Add a set of learners to the lesson class. 
	 * 
	 * If version of the method is designed to be called from Moodle or some other external system, 
	 * and is less efficient in that it has to look up the user from the user id.
	 * If we don't do this, then we may get a a session closed issue if this code is called from the 
	 * LoginRequestValve (as the users will be from a previous session) 
	 * 
     * @param lessonId new learner id
     * @param userIds array of new learner ids
     */ 
    public void addLearners(Long lessonId, Integer[] userIds) throws LessonServiceException;

	   /** 
     * Add a set of learners to the lesson class. To be called within LAMS - see 
     * addLearners(Long lessonId, Integer[] userIds) if calling from an external system.
	 *
     * @param lesson lesson
     * @param users the users to add as learners
     */ 
    public void addLearners(Lesson lesson, Collection<User> users) throws LessonServiceException;


    /** 
     * Add a new staff member to the lesson class. Checks for duplicates.
     * @paran userId new learner id
     * @return true if added user, returns false if the user already a staff member and hence not added.
     */ 
    public boolean addStaffMember(Long lessonId, Integer userId) throws LessonServiceException;
 	
    /** 
     * Add a set of staff to the lesson class. 
	 * 
	 * If version of the method is designed to be called from Moodle or some other external system, 
	 * and is less efficient in that it has to look up the user from the user id.
	 * If we don't do this, then we may get a a session closed issue if this code is called from the 
	 * LoginRequestValve (as the users will be from a previous session) 
	 * 
     * @param lessonId 
     * @param userIds array of new staff ids
     */ 
    public void addStaffMembers(Long lessonId, Integer[] userIds) throws LessonServiceException;
    
	   /** 
	     * Add a set of staff members to the lesson class. To be called within LAMS - see 
	     * addLearners(Long lessonId, Integer[] userIds) if calling from an external system.
		 *
	     * @param lesson lesson
	     * @param users the users to add as learners
	     */ 
	    public void addStaffMembers(Lesson lesson, Collection<User> users) throws LessonServiceException;
	    
	 /** 
	  * Remove references to an activity from all learner progress entries.
	  * Used by Live Edit, to remove any references to the system gates
	  * @param activity The activity for which learner progress references should be removed.
	  */
	 public void removeProgressReferencesToActivity(Activity activity) throws LessonServiceException;
	 
	 /** 
	  * Mark any learner progresses for this lesson as not completed. Called when Live Edit
	  * ends, to ensure that if there were any completed progress records, and the design
	  * was extended, then they are no longer marked as completed. 
	  * @param lessonId The lesson for which learner progress entries should be updated.
	  */
	 public void performMarkLessonUncompleted(Long lessonId) throws LessonServiceException;


	/**
	 * Get the list of users who have attempted an activity. This is based on the progress engine records.
	 * This will give the users in all tool sessions for an activity (if it is a tool activity) or
	 * it will give all the users who have attempted an activity that doesn't have any tool sessions, i.e. 
	 * system activities such as branching.
	 */
	public List<User> getLearnersHaveAttemptedActivity(Activity activity) throws LessonServiceException;

	/**
	 * Get the list of users who have completed an activity. This is based on the progress engine records.
	 * This will give the users in all tool sessions for an activity (if it is a tool activity) or
	 * it will give all the users who have attempted an activity that doesn't have any tool sessions, i.e. 
	 * system activities such as branching.
	 */
	public List<User> getLearnersHaveCompletedActivity(Activity activity) throws LessonServiceException;
	
	/**
	 * Gets the count of the users who have attempted an activity. This is based on the progress engine records.
	 * This will work on all activities, including ones that don't have any tool sessions, i.e. 
	 * system activities such as branching.
	 */
	public Integer getCountLearnersHaveAttemptedActivity(Activity activity) throws LessonServiceException;

	/**
	 * Gets the count of the users who have completed an activity. This is based on the progress engine records.
	 * This will work on all activities, including ones that don't have any tool sessions, i.e. 
	 * system activities such as branching.
	 */
	public Integer getCountLearnersHaveCompletedActivity(Activity activity) throws LessonServiceException;
	
	/**
     * Returns map of lessons in an organisation for a particular learner or staff user.
     * @param userId user's id
     * @param orgId org's id
     * @param isStaff return lessons where user is staff, or where user is learner
     * @return map of lesson beans used in the index page
     */
    public Map<Long, IndexLessonBean> getLessonsByOrgAndUserWithCompletedFlag(Integer userId, Integer orgId, boolean isStaff);
}