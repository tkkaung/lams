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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */

/* $$Id$$ */
package org.lamsfoundation.lams.monitoring.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;

import javax.sql.DataSource;

import org.lamsfoundation.lams.learningdesign.Activity;
import org.lamsfoundation.lams.learningdesign.GateActivity;
import org.lamsfoundation.lams.learningdesign.Grouping;
import org.lamsfoundation.lams.learningdesign.dao.IActivityDAO;
import org.lamsfoundation.lams.learningdesign.dao.hibernate.ActivityDAO;
import org.lamsfoundation.lams.learningdesign.exception.LearningDesignProcessorException;
import org.lamsfoundation.lams.lesson.Lesson;
import org.lamsfoundation.lams.lesson.dao.ILessonDAO;
import org.lamsfoundation.lams.lesson.dao.hibernate.LessonDAO;
import org.lamsfoundation.lams.test.AbstractLamsTestCase;
import org.lamsfoundation.lams.tool.exception.LamsToolServiceException;
import org.lamsfoundation.lams.usermanagement.Organisation;
import org.lamsfoundation.lams.usermanagement.User;
import org.lamsfoundation.lams.usermanagement.exception.UserAccessDeniedException;
import org.lamsfoundation.lams.usermanagement.service.IUserManagementService;
import org.lamsfoundation.lams.util.Configuration;
import org.lamsfoundation.lams.util.ConfigurationKeys;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * 
 * @author Jacky Fang 9/02/2005
 * @author Manpreet Minhas
 */
public class TestMonitoringService extends AbstractLamsTestCase
{
    //---------------------------------------------------------------------
    // Dependent services
    //---------------------------------------------------------------------
    private IMonitoringService monitoringService;
    private IUserManagementService usermanageService;
    private ILessonDAO lessonDao; 
    private IActivityDAO activityDao;
    //---------------------------------------------------------------------
    // Testing Data - Constants
    //---------------------------------------------------------------------
    private final Integer TEST_USER_ID = new Integer(1);
    private final Integer TEST_LEARNER_ID = new Integer(2);
    private final Integer TEST_STAFF_ID = new Integer(3);
    // values when demo'ing the progress engine
    // private final long TEST_LEARNING_DESIGN_ID = 3;
    // private final long TEST_COPIED_LEARNING_DESIGN_ID = 4;
    // values when testing the progress engine
    private final long TEST_LEARNING_DESIGN_ID = 1;
    private final long TEST_LEARNING_DESIGN_SURVEY_ONLY_ID = 2;
    private final long TEST_COPIED_LEARNING_DESIGN_ID = 3;
    private final Integer TEST_ORGANIZATION_ID = new Integer(1);
    private final Long TEST_SCHEDULE_GATE_ID = new Long(27);
    private final Long TEST_LESSION_ID = new Long(1); // "Test_Lesson" from insert_test_data script 
    private final Integer MELCOE_WORKSPACE = new Integer(3); // MELCOE workspace from insert_test_data script 

    //it might be different because it is automatically generated by database
    //TODO create a get lesson by design method in lesson dao.
    private static Long TEST_LESSON_ID = null;
    //---------------------------------------------------------------------
    // Testing Data - Instance Variables
    //---------------------------------------------------------------------
    private User testUser;
    private User testStaff;
    private User testLearner;
    private Organisation testOrganisation;

    DataSource dataSource;

    /*
     * @see AbstractLamsCommonTestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        monitoringService = (IMonitoringService)this.context.getBean("monitoringService");
        usermanageService = (IUserManagementService)this.context.getBean("userManagementService");
        lessonDao = (LessonDAO)this.context.getBean("lessonDAO");
        activityDao = (ActivityDAO)this.context.getBean("activityDAO");
        dataSource = (DataSource) this.context.getBean("dataSource");
       
        initializeTestingData();
    }
    /**
     * @see AbstractLamsCommonTestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        context = null; // try to trigger some memory to get freed - running out of memory.
    }

    /**
     * Constructor for TestMonitoringService.
     * @param name
     */
    public TestMonitoringService(String name)
    {
        super(name);
    }
    
    /**
     * @see org.lamsfoundation.lams.AbstractLamsTestCase#getContextConfigLocation()
     */
    protected String[] getContextConfigLocation()
    {
        return new String[] { 
        		"/org/lamsfoundation/lams/localApplicationContext.xml",
        		"/org/lamsfoundation/lams/lesson/lessonApplicationContext.xml",
        		"/org/lamsfoundation/lams/toolApplicationContext.xml",
          		"/org/lamsfoundation/lams/learning/learningApplicationContext.xml",        					  
          		"/org/lamsfoundation/lams/authoring/authoringApplicationContext.xml",
          		"/org/lamsfoundation/lams/monitoring/monitoringApplicationContext.xml",
        		"/org/lamsfoundation/lams/tool/survey/applicationContext.xml"};            
    }
    /**
     * @see org.lamsfoundation.lams.AbstractLamsTestCase#getHibernateSessionFactoryName()
     */
    protected String getHibernateSessionFactoryName()
    {
        return "coreSessionFactory";
    }
   public void testInitializeLesson()
    {
        
       String lamsLearningDesignTableName = "lams_learning_design";
       String idName = "learning_design_id";
       long previousLDId = getMaxId(lamsLearningDesignTableName, idName);

        Lesson testLesson = monitoringService.initializeLesson("Test_Lesson",
                                                               "Test_Description",
                                                               TEST_LEARNING_DESIGN_ID,
                                                               null,
                                                               testUser.getUserId());
        TEST_LESSON_ID=testLesson.getLessonId();
        Lesson createdLesson = lessonDao.getLesson(TEST_LESSON_ID);
        assertNotNull(createdLesson);
        
        // can't reliably predict the id of the copied learning design - depends on what
        // has happened before the test and what might be running at the same time.
        // so compare it to the last id created in this table. This will usually be the 
        // right value as it it looked up using getLastInsertId() and that works
        // on per connection basis - it is unlikely that this test case will be sharing 
        // the connection with anything else that will update this table. This may
        // change in the future if more test cases are added.
        long mostRecentLDId = getMaxId(lamsLearningDesignTableName, idName);
        long copiedLDId = testLesson.getLearningDesign().getLearningDesignId().longValue();
        assertTrue("A learning design has been created (previous insert #previousLDId, recent insert #mostRecentLDId",
                	previousLDId!=mostRecentLDId);
        assertTrue("Copied learning design is the new id", copiedLDId == mostRecentLDId);
        	
        assertEquals("verify the user", TEST_USER_ID,createdLesson.getUser().getUserId());
        assertEquals("verify the lesson state",Lesson.CREATED,createdLesson.getLessonStateId());
      }

    public void testCreateLessonClassForLesson()
    {
        LinkedList learners = new LinkedList();
        learners.add(testLearner);
        LinkedList staffs = new LinkedList();
        staffs.add(testStaff);
        
        Lesson testLesson = monitoringService.createLessonClassForLesson(TEST_LESSON_ID.longValue(),
                                                                         testOrganisation,
                                                                         "leaner group",
                                                                         learners,
                                                                         "staff group",
                                                                         staffs,
                                                                         TEST_STAFF_ID);
        Lesson createdLesson = lessonDao.getLesson(TEST_LESSON_ID);
        
        assertEquals("verify the staff group",staffs.size(),createdLesson.getLessonClass().getStaffGroup().getUsers().size());
		assertNotNull("verify lesson has organisation",createdLesson.getOrganisation());
        assertEquals("verify the organization",TEST_ORGANIZATION_ID,createdLesson.getOrganisation().getOrganisationId());
        assertEquals("verify number of the learners",1,createdLesson.getAllLearners().size());
        assertEquals("verify the lesson class",Grouping.CLASS_GROUPING_TYPE,createdLesson.getLessonClass().getGroupingTypeId());
        assertEquals("verify the learner group",1,createdLesson.getLessonClass().getGroups().size());

        
    }
    
    public void testStartlesson() throws LamsToolServiceException
    {
        monitoringService.startLesson(TEST_LESSON_ID.longValue(),TEST_STAFF_ID);
        assertTrue(true);
        
        Lesson startedLesson = lessonDao.getLesson(TEST_LESSON_ID);
        
        assertNotNull(startedLesson);
        assertEquals("verify the lesson status",Lesson.STARTED_STATE,startedLesson.getLessonStateId());
        
        
    }

    public void testOpenGate()
    {
        monitoringService.openGate(TEST_SCHEDULE_GATE_ID);
        Activity openedScheduleGate = activityDao.getActivityByActivityId(TEST_SCHEDULE_GATE_ID);
        assertTrue("the gate should be opened",((GateActivity)openedScheduleGate).getGateOpen().booleanValue());
    }

    public void testCloseGate()
    {
        monitoringService.closeGate(TEST_SCHEDULE_GATE_ID);
        Activity closedScheduleGate = activityDao.getActivityByActivityId(TEST_SCHEDULE_GATE_ID);
        assertTrue("the gate should be closed",!((GateActivity)closedScheduleGate).getGateOpen().booleanValue());
    }

    public void testForceCompleteLessonByUser()
    {
    }
    
    public void testGetLessonDetails() throws IOException{
    	String packet = monitoringService.getLessonDetails(TEST_LESSION_ID);    	
    	System.out.print(packet);
    }
    public void testGetLessonLearners() throws IOException{
    	String packet = monitoringService.getLessonLearners(TEST_LESSION_ID);    	
    	System.out.println(packet);
    }
    public void testGetLessonDesign()throws IOException{
    	String packet = monitoringService.getLearningDesignDetails(TEST_LESSION_ID);    	
    	System.out.println(packet);
    }
    public void testGetAllLearnersProgress() throws IOException{
    	String packet = monitoringService.getAllLearnersProgress(TEST_LESSION_ID);    	
    	System.out.println(packet);
    }
    public void testGetLearnerActivityURL() throws Exception{
    	String url = monitoringService.getLearnerActivityURL(TEST_LESSION_ID, new Long(29),TEST_LEARNER_ID);    	
    	System.out.println(url);
    }
    public void  testGellAllContributeActivities()throws IOException, LearningDesignProcessorException{
    	String packet = monitoringService.getAllContributeActivities(TEST_LESSION_ID);    	
    	System.out.println(packet);
    }
    
    public void testMoveLesson() throws IOException{
    	String packet = monitoringService.moveLesson(TEST_LESSION_ID,MELCOE_WORKSPACE,TEST_USER_ID);    
    	System.out.println(packet);
    }
    public void testRenameLesson()throws IOException{
        String newName = "New name after renaming";
    	String packet = monitoringService.renameLesson(TEST_LESSION_ID,newName,TEST_USER_ID);    	
    	Lesson updatedLesson = lessonDao.getLesson(TEST_LESSION_ID);
    	assertEquals("Name changed correctly", newName, updatedLesson.getLessonName());
    	System.out.println(packet);
    } 
    /**
     * Initialize all instance variables for testing
     */
   private void initializeTestingData()
    {
        testUser = (User)usermanageService.findById(User.class,TEST_USER_ID);
        testStaff = (User)usermanageService.findById(User.class,TEST_STAFF_ID);
        testLearner = (User)usermanageService.findById(User.class,TEST_LEARNER_ID);        
        testOrganisation = (Organisation)usermanageService.findById(Organisation.class,TEST_ORGANIZATION_ID);
    }
   
    public void testCheckGateStatus() throws IOException
    {
	     Long syncGateID = new Long(33);
	     Long scheduleGateID = new Long(34);
	     Long permissionGateID = new Long(35);
	     Long lessonID = new Long(2);
    
        
        String packet = monitoringService.checkGateStatus(scheduleGateID, lessonID); //schedule gate
        System.out.println(packet);
        
    }
    
    public void testReleaseGate() throws IOException
    {
        Long syncGateID = new Long(33);
	    Long scheduleGateID = new Long(34);
	    Long permissionGateID = new Long(35);
	    
	    String packet = monitoringService.releaseGate(syncGateID); //schedule gate
        System.out.println(packet);
        
    }

    /** Get the largest id that was last inserted into a table. This will normally be the last
     * id inserted. Why not use LAST_INSERT_ID() - that works per connection and there is 
     * no guarantee that this test case is running using the same connection as the 
     * service. 
     */
    private long getMaxId(String tablename, String idname)
    {
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        return jt.queryForLong("SELECT max("+idname+") FROM "+tablename);
    }

	/*
	 * Test method for 'org.lamsfoundation.lams.preview.service.PreviewService.startPreviewLesson(int, long, String, String)'
	 */
	public void testStartPreviewLesson() {
		String testName = "LESSON";
		String testDesc = "DESC";
		Lesson lesson = startLesson(testName, testDesc);
		assertEquals("Lesson has correct title",testName, lesson.getLessonName());
		assertEquals("Lesson has correct description",testDesc, lesson.getLessonDescription());

		Lesson newLesson = lessonDao.getLesson(lesson.getLessonId());
		assertNotNull("Lesson can be found in database",newLesson);
	}

	private Lesson startLesson(String testName, String testDesc) {
		try {
	        Lesson previewLesson = monitoringService.initializeLessonForPreview(testName,testDesc,TEST_LEARNING_DESIGN_SURVEY_ONLY_ID,TEST_USER_ID);
	        assertNotNull("Lesson created",previewLesson);
			assertNotNull("Lesson has been saved - an id exists", previewLesson.getLessonId());

	        Lesson newLesson = monitoringService.createPreviewClassForLesson(TEST_USER_ID.intValue(), previewLesson.getLessonId().longValue());
	        assertNotNull("Lesson returned from create class",newLesson);
	        assertSame("Lesson updated from create class", newLesson.getLessonId(),previewLesson.getLessonId());

	        monitoringService.startLesson(previewLesson.getLessonId().longValue(),TEST_STAFF_ID);

			return previewLesson;
		} catch (UserAccessDeniedException e) {
			fail("Unable to start lesson as due to a user exception");
		}
		return null;
		
	}
	
	/*
	 * Test method for 'org.lamsfoundation.lams.preview.service.PreviewService.deletePreviewLesson(long)'
	 */
	public void testDeletePreviewLesson() {
		String testName = "LESSON TO DELETE";
		String testDesc = "TO BE DELETED";
		Lesson lesson = startLesson(testName, testDesc);
		Long lessonId = lesson.getLessonId();
		
		monitoringService.deletePreviewLesson(lessonId.longValue());
		Lesson deletedLesson = lessonDao.getLesson(lessonId);
		assertNull("Deleted lesson cannot be found",deletedLesson);
	}

	/*
	 * Test method for 'org.lamsfoundation.lams.preview.service.PreviewService.deleteAllOldPreviewLessons(int)'
	 */
	public void testDeleteAllOldPreviewLessons() {
		// need to dummy up something to delete. Create a lesson and force it to be over 7 days old.
		String testName = "LESSON TO DELETE By BATCH";
		String testDesc = "TO BE DELETED BY THE BATCH CALL";
		Lesson lesson = startLesson(testName, testDesc);
		Long lessonId = lesson.getLessonId();
		
		int numDays = Configuration.getAsInt(ConfigurationKeys.PREVIEW_CLEANUP_NUM_DAYS);
		assertTrue("Number of days till the cleanup should be deleted is greater than zero (value is "+numDays+")", numDays > 0);

		//Configuration.
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_MONTH, -8);
		lesson.setStartDateTime(calendar.getTime());
		int lessonsDeleted = monitoringService.deleteAllOldPreviewLessons();
		assertTrue("deleteAllOldPreviewLessons returns at least 1 lesson", lessonsDeleted>=1);
		
		Lesson deletedLesson = lessonDao.getLesson(lessonId);
		assertNull("Batch deleted lesson cannot be found",deletedLesson);
	}

}
