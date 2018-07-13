package ntu.celt.eUreka2.pages.modules.scheduling;

import java.util.ArrayList;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.scheduling.Activity;
import ntu.celt.eUreka2.modules.scheduling.DisplayStatus;
import ntu.celt.eUreka2.modules.scheduling.DisplayType;
import ntu.celt.eUreka2.modules.scheduling.Milestone;
import ntu.celt.eUreka2.modules.scheduling.Phase;
import ntu.celt.eUreka2.modules.scheduling.PrivilegeSchedule;
import ntu.celt.eUreka2.modules.scheduling.Schedule;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.modules.scheduling.Task;

import org.apache.commons.lang.time.DateUtils;
import org.apache.tapestry5.Asset;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

@Import(library="SchedulingView.js", stylesheet="SchedulingView.css")
public class ScheduleView extends AbstractPageScheduling {
	@Inject
	private SchedulingDAO scheduleDAO;
	@Inject
	private Messages messages;
	
	@Property
	private Project curProj;
	private Long scheduleId ;
	@Property
	private Schedule schedule;
	@Property
	private List<Milestone> milestones;
	@Property
	private Milestone milestone;
	private Activity activity; //getter and setter are define separately
	@Property
	private Phase phase;
	@SuppressWarnings("unused")
	@Property
	private Task task;
	@SuppressWarnings("unused")
	@Property
	private User tempUser;
	
	private Calendar startCalendar;
	private Calendar endCalendar;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private DisplayType displayType;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private DisplayStatus displayStatus;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String displayAssign;
	
	private int columnWidthByDay = Integer.parseInt(messages.get("columnWidthByDay"));   
	private int columnWidthByWeek = Integer.parseInt(messages.get("columnWidthByWeek")); 
	private int columnWidthByMonth = Integer.parseInt(messages.get("columnWidthByMonth")); 
	@Property
	private int MIN_WIDTH_VIEW = Integer.parseInt(messages.get("MIN_WIDTH_VIEW")); 
	private int BORDER_WIDTH = Integer.parseInt(messages.get("BORDER_WIDTH")); 
	private int DISPLAY_TYPE_DAY_THRESHOLD = Integer.parseInt(messages.get("DISPLAY_TYPE_DAY_THRESHOLD")); 
	private int DISPLAY_TYPE_WEEK_THRESHOLD = Integer.parseInt(messages.get("DISPLAY_TYPE_WEEK_THRESHOLD")); 
	private int HIDDEN_COL_ADJUST = Integer.parseInt(messages.get("HIDDEN_COL_ADJUST"));
	
	@Property
	private int columnWidth;
	@SuppressWarnings("unused")
	@Property
	private String emptyString = "&nbsp;";
	private Date today = DateUtils.truncate(new Date(), Calendar.DATE);
	
	@Property
	private List<Calendar> dateColumns ;
	@Property
	private Calendar dateColumn ;
	@Property
	private List<String> dateTopColumns;
	@Property
	private String dateTopColumn;
	private Map<String, Integer> dateTopColumnsCount;
	private int totalWidth ;
	private int totalDays;
	@SuppressWarnings("unused")
	@Property
	private Integer todayLeftMargin = 0;
	
	
	
	void onActivate(Long id) {
		scheduleId = id;
	}
	Long onPassivate() {
		return scheduleId;
	}
	
	void setupRender(){
		schedule = scheduleDAO.getScheduleById(scheduleId);
		if(schedule==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ScheduleID", scheduleId));
		
		curProj = schedule.getProject();
		if(!canViewScheduling(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		milestones = schedule.getMilestones();
		
		//---------begin initialize render variables
		Date startDate = curProj.getSdate();
		Date endDate = curProj.getEdate();
		if(endDate==null){ //work around when data is missing in database, 
			endDate = startDate;
		}
		for(Milestone m : milestones){
			if(m.getDeadline().before(startDate))
				startDate = m.getDeadline();
			else if(m.getDeadline().after(endDate))
				endDate = m.getDeadline();

			for(Phase p : m.getPhases()){
				if(p.getStartDate().before(startDate))
					startDate = p.getStartDate();
				if(p.getEndDate().after(endDate))
					endDate = p.getEndDate();
			}
			for(Task t : m.getTasks()){
				if(t.getStartDate().before(startDate))
					startDate = t.getStartDate();
				if(t.getEndDate().after(endDate))
					endDate = t.getEndDate();
			}
		}
		startCalendar = Calendar.getInstance();
		startCalendar.setTime(startDate);
		endCalendar = Calendar.getInstance();
		endCalendar.setTime(endDate);
		
		if(displayType==null){
			//if gap is less than a certain number of days, then display BY_DAY or BY_WEEK
			if(Util.getNumDaysBetween(startCalendar, endCalendar)<= DISPLAY_TYPE_DAY_THRESHOLD)
				displayType = DisplayType.BY_DAY;
			else if(Util.getNumDaysBetween(startCalendar, endCalendar)<= DISPLAY_TYPE_WEEK_THRESHOLD)
				displayType = DisplayType.BY_WEEK;
			else
				displayType = DisplayType.BY_MONTH;
		}
		
		if(displayType.equals(DisplayType.BY_DAY))
			columnWidth = columnWidthByDay;
		else if(displayType.equals(DisplayType.BY_WEEK))
			columnWidth = columnWidthByWeek;
		else if(displayType.equals(DisplayType.BY_MONTH))
			columnWidth = columnWidthByMonth;
		
		initializeDateColumns();
		
		Calendar now = Calendar.getInstance();
		Calendar startDay = dateColumns.get(0);
		Calendar nextOfEndDay = getNextDateColumn(dateColumns.get(dateColumns.size()-1));
		
		totalWidth = getColumnAndBorderWidth() * dateColumns.size();
		totalDays = Util.getNumDaysBetween(startDay, nextOfEndDay);
		
		if(now.compareTo(startDay)>=0 && now.compareTo(nextOfEndDay)<0){
			todayLeftMargin = computeWidthAdjustSingle(startDay.getTime(), now.getTime(), false, 3);
		}
		else{
			//hide todayMarker
			todayLeftMargin = -9999;
		}
		
		//---------end initialize render variables
	}
	public Date getStartDay(){
		return dateColumns.get(0).getTime();
	}
	
	public boolean filterCheck(Task t){
		if(displayStatus!=null){
			switch(displayStatus){
				case IN_PROGRESS:	if(t.getPercentageDone()<=0 || t.getPercentageDone()>=100) return false; break;
				case OUTSTANDING:	if(t.getPercentageDone()<0 || t.getPercentageDone()>=100 ) return false; break;
				case OVERDUE:	if(t.getPercentageDone()==100 || t.getEndDate().compareTo(today)>0) return false; break;
				case COMPLETED:		if(t.getPercentageDone()!=100) return false; break;
			}
		}
		if(displayAssign==null){
			//nothing
		}
		else if(displayAssign.equals("ASSIGN")){
			if(t.getAssignedPersons().size()==0) return false;
		}
		else if(displayAssign.equals("UNASSIGN")){
			if(t.getAssignedPersons().size()>0) return false;
		}
		else {
			String userId = displayAssign;
			if(! t.getAssignedPersonsAsStringIds().contains(userId) ) return false;
		}
		
		return true;
	}
	
	private void initializeDateColumns(){
		dateColumns = new ArrayList<Calendar>();
		dateTopColumns = new ArrayList<String>();
		dateTopColumnsCount = new HashMap<String, Integer>();
		Calendar tempCal = startCalendar;
		if(displayType.equals(DisplayType.BY_DAY)){
			int numDays = Util.getNumDaysBetween(startCalendar, endCalendar);
			String dateFormat = "MMMMM";
			if(numDays>365)
				dateFormat = "MMMMM yyyy";
			while(tempCal.compareTo(endCalendar)<=0 ){ 
				dateColumns.add((Calendar)tempCal.clone());   
				String m = Util.formatDateTime(tempCal.getTime(), dateFormat);
				if(!dateTopColumns.contains(m)){
					dateTopColumns.add(m);
					dateTopColumnsCount.put(m, 1);
				}
				else{
					dateTopColumnsCount.put(m, dateTopColumnsCount.remove(m) + 1);
				}

				tempCal.add(Calendar.DAY_OF_YEAR, 1);
			}
		}
		else if(displayType.equals(DisplayType.BY_WEEK)){
			//get the first day of that week
			tempCal.add(Calendar.DAY_OF_WEEK, - (tempCal.get(Calendar.DAY_OF_WEEK)- tempCal.getFirstDayOfWeek()));			
			int numDays = Util.getNumDaysBetween(startCalendar, endCalendar);
			String dateFormat = "MMMMM";
			if(numDays>365)
				dateFormat = "MMMMM yyyy";
			while(tempCal.before(endCalendar) 
					|| (tempCal.get(Calendar.WEEK_OF_YEAR) == endCalendar.get(Calendar.WEEK_OF_YEAR))){ //still in same week
				dateColumns.add((Calendar)tempCal.clone());   
				String m = Util.formatDateTime(tempCal.getTime(), dateFormat);
				if(!dateTopColumns.contains(m)){
					dateTopColumns.add(m);
					dateTopColumnsCount.put(m, 1);
				}
				else{
					dateTopColumnsCount.put(m, dateTopColumnsCount.remove(m) + 1);
				}

				tempCal.add(Calendar.WEEK_OF_YEAR, 1);
			}
		}
		else if(displayType.equals(DisplayType.BY_MONTH)){
			//get the first day of that month
			tempCal.add(Calendar.DAY_OF_MONTH, - tempCal.get(Calendar.DAY_OF_MONTH)+1);			
			
			while(tempCal.before(endCalendar)
					|| (tempCal.get(Calendar.MONTH) == endCalendar.get(Calendar.MONTH))){//still in same month
				dateColumns.add((Calendar)tempCal.clone());   
				String m = Util.formatDateTime(tempCal.getTime(), "yyyy");
				if(!dateTopColumns.contains(m)){
					dateTopColumns.add(m);
					dateTopColumnsCount.put(m, 1);
				}
				else{
					dateTopColumnsCount.put(m, dateTopColumnsCount.remove(m) + 1);
				}

				tempCal.add(Calendar.MONTH, 1);
				
			}
		}
	}
	
	
	public int getColumnAndBorderWidth(){
		return columnWidth + BORDER_WIDTH; 
	}
	public int computeWidth(Date startDate, Date endDate, boolean includeBothDate){
		return computeWidth(startDate, endDate, includeBothDate, 0);
	}
	public int computeWidthAdjust(Date startDate, Date endDate, boolean includeBothDate){
		return computeWidth(startDate, endDate, includeBothDate, HIDDEN_COL_ADJUST);
	}
	public int computeWidthAdjustSingle(Date startDate, Date endDate, boolean includeBothDate, int imageWidth){
		int adjustWidth = HIDDEN_COL_ADJUST ;
		if(displayType.equals(DisplayType.BY_DAY)){
			adjustWidth += (columnWidthByDay-imageWidth)/2;
		}
		
		return computeWidth(startDate, endDate, includeBothDate, adjustWidth);
	}
	public int computeWidth(Date startDate, Date endDate, boolean includeBothDate, int adjustValue){
		Calendar d1 = Calendar.getInstance();
		d1.setTime(startDate);
		Calendar d2 = Calendar.getInstance();
		d2.setTime(endDate);
		
		int numDays = Util.getNumDaysBetween(d1, d2);
		if(includeBothDate)
			numDays += 1;
		
		int result = Math.round(numDays * totalWidth /totalDays) + adjustValue;

		return result;
	}
	public int computeWidthDone(Date startDate, Date endDate, boolean includeBothDate, int percentageDone){
		return Math.round(percentageDone * computeWidth(startDate, endDate, includeBothDate)/100);
	}
	
	public String getDateColumnDisplay(){
		if(displayType.equals(DisplayType.BY_DAY)){
			return Util.formatDateTime(dateColumn.getTime(), "d"); 
		}
		else if(displayType.equals(DisplayType.BY_WEEK)){
			return Util.formatDateTime(dateColumn.getTime(), "dd"); 
		}
		else if(displayType.equals(DisplayType.BY_MONTH)){
			return Util.formatDateTime(dateColumn.getTime(), "MMM"); 
		}
		return null;
	}
	public String getDateTopColumnDisplay(){
		return dateTopColumn; 
	}
	public Integer getDateTopColumnCountDisplay(){
		return dateTopColumnsCount.get(dateTopColumn);
	}
	
	private Calendar getNextDateColumn(Calendar curDateColumn){
		Calendar nextDateColumn = ((Calendar) curDateColumn.clone());
		if(displayType.equals(DisplayType.BY_DAY)){
			nextDateColumn.add(Calendar.DAY_OF_YEAR, 1);
		}
		else if(displayType.equals(DisplayType.BY_WEEK)){
			nextDateColumn.add(Calendar.WEEK_OF_YEAR, 1);
		}
		else if(displayType.equals(DisplayType.BY_MONTH)){
			nextDateColumn.add(Calendar.MONTH, 1);
		}
		return nextDateColumn; 
	}
	
	public boolean isInstanceofPhase(Activity activity){
		if(activity instanceof Phase){
			return true;
		}
		return false;
	}
	public boolean isInstanceofTask(Activity activity){
		if(activity instanceof Task){
			return true;
		}
		return false;
	}
	public void setActivity(Activity activity){
		this.activity = activity;
		if(activity instanceof Phase)
			phase = (Phase) activity;
		else if(activity instanceof Task)
			task = (Task) activity;
	}
	public Activity getActivity(){
		return activity;
	}
	
	public String getCompletedOn(Task task){
		if(task.getPercentageDone()==100)
			return messages.get("completed-on") + " " + task.getCompleteDateDisplay();
		return "";
	}
	
	public String getPercentDoneClass(int percentDone, Date endDate){
		if(percentDone<100 && endDate.before(today))
			return "highLightRed";
		return "highLightGreen";
	}
	public String getPhaseIconClass(){
		if(phase.getTasks().size()>0)
			return "phaseopen";
		return null;
	}
	public String getMilestoneIconClass(){
		if(!milestone.isEmpty())
			return "milestoneopen";
		return null;
	}
	

	public int getTotalScheduleWidth(){
		return MIN_WIDTH_VIEW + totalWidth;
	}
	public SelectModel getDisplayAssignModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		
		optModelList.add(new OptionModelImpl(messages.get("assigned-only"), "ASSIGN"));
		optModelList.add(new OptionModelImpl(messages.get("unassigned-only"), "UNASSIGN"));
		
		List<User> users = curProj.getUsers();
		for(User u : users){
			optModelList.add(new OptionModelImpl(u.getDisplayName(), Integer.toString(u.getId())));
		}
		
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
	void onSuccessFromDisplayOptionForm(){
		//do nothing 
	}
	
	@SuppressWarnings("unused")
	@Property
	@Inject
	@Path("images/gantt-milestone.png")
	private Asset milestone_png;
	@SuppressWarnings("unused")
	@Property
	@Inject
	@Path("images/gantt-phase.png")
	private Asset phase_png;
	@SuppressWarnings("unused")
	@Property
	@Inject
	@Path("images/gantt-task-complete.png")
	private Asset task_complete_png;
/*	@SuppressWarnings("unused")
	@Property
	@Inject
	@Path("images/gantt-task-completion-marker.gif")
	private Asset task_completion_marker_png;
*/	@SuppressWarnings("unused")
	@Property
	@Inject
	@Path("images/gantt-task-ongoing.png")
	private Asset task_ongoing_png;
	@SuppressWarnings("unused")
	@Property
	@Inject
	@Path("images/today-marker.png")
	private Asset today_marker_png;

}
