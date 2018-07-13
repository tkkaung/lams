package ntu.celt.eUreka2.pages;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;

import ntu.celt.eUreka2.annotation.PublicPage;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.ProjStatus;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.budget.BudgetDAO;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.learninglog.LearningLogDAO;
import ntu.celt.eUreka2.modules.resources.ResourceDAO;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;

@PublicPage
@Import(library="context:lib/js/commons1.js", stylesheet="../components/theme/main1.1.css")
public class ReportPage {
	@SessionState
	private AppState appState;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private AnnouncementDAO annucDAO;
	@Inject
	private BlogDAO blogDAO;
	@Inject
	private BudgetDAO budgetDAO;
	@Inject
	private ElogDAO elogDAO;
	@Inject
	private ForumDAO forumDAO;
	@Inject
	private ResourceDAO rsrcDAO;
	@Inject
	private SchedulingDAO schgDAO;
	@Inject
	private LearningLogDAO llogDAO;
	@Inject
	private AssessmentDAO assmtDAO;
	@Inject
	private ProjStatusDAO _projStatusDAO;
	@Inject
	private ProjTypeDAO _projTypeDAO;
	@Inject
	private SchoolDAO _schoolDAO; 
	@Inject
	private BeanModelSource source;
	@Inject
	private Messages message;
	@Inject
	private PropertyConduitSource pcSources;
	
	@Property
	@Persist
	private ProjStatus filterStatus;
	@Property
	@Persist
	private ProjType filterType;
	@Property
	@Persist
	private School filterSchool;
	@Property
	@Persist
	private String filterYear;
	@Property
	private List<Project> projects;
	@Property
	private Project project;
	@Property
	private int rowIndex;
	@InjectComponent
	private Grid grid;
	private int firstResult;
	private int maxResult ;
	
    @SuppressWarnings("unused")
	@Property
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private Integer totalShowOption;

    @SuppressWarnings("deprecation")
	void onActivate(String year) {
		filterYear = year;   	
    	if (filterYear.length() < 4) {
    		Date today = new Date();
    		filterYear = Integer.toString(today.getYear() + 1900);
    	}
    }
    
    String onPassivate() {
    	return filterYear;
    }
    
	public int getNo(){
		return (grid.getCurrentPage()-1)*grid.getRowsPerPage()+ rowIndex + 1;
	}
	@Cached
	public int getTotalSize() {
		return (int) projects.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public void setRowsPerPage(int num){
		appState.setRowsPerPage(num);
	}
	
	void setupRender(){
		maxResult = appState.getRowsPerPage();
		firstResult = (grid.getCurrentPage()-1)*grid.getRowsPerPage();
//		projects = projDAO.searchProjects(filterStatus, filterType, filterSchool, firstResult, maxResult);
		projects = projDAO.searchProjects(filterStatus, filterType, filterSchool, filterYear.substring(2));
	}
	
	@SuppressWarnings("deprecation")
	public SelectModel getProjYearModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (int i = 0; i < 10; i++) {
			int intYear = (new Date()).getYear() + 1900;
			OptionModel optModel = new OptionModelImpl(Integer.toString(intYear - i), Integer.toString(intYear - i));
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	public SelectModel getProjStatusModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<ProjStatus> projStatusList = _projStatusDAO.getAllStatus();
		for (ProjStatus ps : projStatusList) {
			OptionModel optModel = new OptionModelImpl(ps.getDisplayName(), ps);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	public SelectModel getProjTypeModel() {
		List<OptionModel> optionList = new ArrayList<OptionModel>();
		List<ProjType> typeList = _projTypeDAO.getAllTypes();
		for (ProjType type: typeList) {
			OptionModel option = new OptionModelImpl(type.getDisplayName(), type);
			optionList.add(option);
		}
		SelectModel selModel = new SelectModelImpl(null, optionList);
		return selModel;
	}
	public SelectModel getSchoolModel() {
		List<OptionModel> optionList = new ArrayList<OptionModel>();
		List<School> sList = _schoolDAO.getAllSchools();
		for (School s: sList) {
			OptionModel option = new OptionModelImpl(s.getDisplayName(), s);
			optionList.add(option);
		}
		SelectModel selModel = new SelectModelImpl(null, optionList);
		return selModel;
	}
	
	private PropertyConduit createMemberPropertyConduit() {
		PropertyConduit pcList = new PropertyConduit() {
			@Override
			public Object get(Object arg0) {
				// TODO Auto-generated method stub
				Project myRow = (Project) arg0;
				return myRow.getMembers().size();
			}

			@Override
			public Class<Integer> getPropertyType() {
				// TODO Auto-generated method stub
				return Integer.class;
			}

			@Override
			public void set(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> arg0) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		return pcList;
	}
	private PropertyConduit createAssessmentPropertyConduit() {
		PropertyConduit pcList = new PropertyConduit() {

			@Override
			public Object get(Object arg0) {
				// TODO Auto-generated method stub
				Project myRow = (Project) arg0;
				long num = assmtDAO.countAssessmentsByProject(myRow);
				return num;
			}

			@Override
			public Class getPropertyType() {
				// TODO Auto-generated method stub
				return Integer.class;
			}

			@Override
			public void set(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		return pcList;
	}
	private PropertyConduit createAnnouncePropertyConduit() {
		PropertyConduit pcList = new PropertyConduit() {

			@Override
			public Object get(Object arg0) {
				// TODO Auto-generated method stub
				Project myRow = (Project) arg0;
				long num = annucDAO.countAnnouncements(myRow);
				return num;
			}

			@Override
			public Class getPropertyType() {
				// TODO Auto-generated method stub
				return Integer.class;
			}

			@Override
			public void set(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		return pcList;
	}
	private PropertyConduit createBudgetPropertyConduit() {
		PropertyConduit pcList = new PropertyConduit() {

			@Override
			public Object get(Object arg0) {
				// TODO Auto-generated method stub
				Project myRow = (Project) arg0;
				long num = budgetDAO.countTransactions(myRow); 
				return num;
			}

			@Override
			public Class getPropertyType() {
				// TODO Auto-generated method stub
				return Integer.class;
			}

			@Override
			public void set(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		return pcList;
	}
	private PropertyConduit createBlogPropertyConduit() {
		PropertyConduit pcList = new PropertyConduit() {

			@Override
			public Object get(Object arg0) {
				// TODO Auto-generated method stub
				Project myRow = (Project) arg0;
				long num = blogDAO.countBlogs(myRow) + blogDAO.countBlogComments(myRow);
				return num;
			}

			@Override
			public Class getPropertyType() {
				// TODO Auto-generated method stub
				return Integer.class;
			}

			@Override
			public void set(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		return pcList;
	}
	private PropertyConduit createElogPropertyConduit() {
		PropertyConduit pcList = new PropertyConduit() {

			@Override
			public Object get(Object arg0) {
				// TODO Auto-generated method stub
				Project myRow = (Project) arg0;
				long num = elogDAO.countElogs(myRow) + elogDAO.countElogComments(myRow);
				return num;
			}

			@Override
			public Class getPropertyType() {
				// TODO Auto-generated method stub
				return Integer.class;
			}

			@Override
			public void set(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		return pcList;
	}
	private PropertyConduit createForumPropertyConduit() {
		PropertyConduit pcList = new PropertyConduit() {

			@Override
			public Object get(Object arg0) {
				// TODO Auto-generated method stub
				Project myRow = (Project) arg0;
				long num = forumDAO.getTotalForums(myRow);
				return num;
			}

			@Override
			public Class getPropertyType() {
				// TODO Auto-generated method stub
				return Integer.class;
			}

			@Override
			public void set(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		return pcList;
	}
	private PropertyConduit createNotePropertyConduit() {
		PropertyConduit pcList = new PropertyConduit() {

			@Override
			public Object get(Object arg0) {
				// TODO Auto-generated method stub
				Project myRow = (Project) arg0;
				long num = llogDAO.countLlogs(myRow);
				return num;
			}

			@Override
			public Class getPropertyType() {
				// TODO Auto-generated method stub
				return Integer.class;
			}

			@Override
			public void set(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		return pcList;
	}
	private PropertyConduit createResourcePropertyConduit() {
		PropertyConduit pcList = new PropertyConduit() {

			@Override
			public Object get(Object arg0) {
				// TODO Auto-generated method stub
				Project myRow = (Project) arg0;
				long num = rsrcDAO.countFiles(myRow) + rsrcDAO.countFolders(myRow) + rsrcDAO.countLinks(myRow); 
				return num;
			}

			@Override
			public Class getPropertyType() {
				// TODO Auto-generated method stub
				return Integer.class;
			}

			@Override
			public void set(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		return pcList;
	}
	private PropertyConduit createTimelinePropertyConduit() {
		PropertyConduit pcList = new PropertyConduit() {

			@Override
			public Object get(Object arg0) {
				// TODO Auto-generated method stub
				Project myRow = (Project) arg0;
				long num = schgDAO.countMilestones(myRow) + schgDAO.countPhases(myRow) + schgDAO.countTasks(myRow);
				return num;
			}

			@Override
			public Class getPropertyType() {
				// TODO Auto-generated method stub
				return Integer.class;
			}

			@Override
			public void set(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		return pcList;
	}
	
	public BeanModel<Project> getModel() {
		BeanModel<Project> model = source.createDisplayModel(Project.class, message);
		model.include("ID", "Name");
		model.add("No", null);
		model.add("status", pcSources.create(Project.class, "status.displayname"));
		model.add("school", pcSources.create(Project.class, "school.displayname"));
		model.add("type", pcSources.create(Project.class, "type.displayname"));
		model.add("numVisit", pcSources.create(Project.class, "numVisit"));
		model.add("members", createMemberPropertyConduit()).sortable(true);
		model.add("announcement", createAnnouncePropertyConduit()).sortable(true);
		model.add("assessment", createAssessmentPropertyConduit());
		model.add("blog", createBlogPropertyConduit());
		model.add("budget", createBudgetPropertyConduit());
		model.add("elog", createElogPropertyConduit());
		model.add("forum", createForumPropertyConduit());
		model.add("note", createNotePropertyConduit());
		model.add("resource", createResourcePropertyConduit());
		model.add("timeline", createTimelinePropertyConduit());
		model.reorder("No", "ID");
		return model;
	}
	
	public int getMember() {
		return project.getMembers().size();
	}
	public long getAssessment() {
		return assmtDAO.countAssessmentsByProject(project);
	}
	public long getAnnouncement() {
		return annucDAO.countAnnouncements(project);
	}
	public long getBlog() {
		return blogDAO.countBlogs(project) + blogDAO.countBlogComments(project);
	}
	public long getElog() {
		return elogDAO.countElogs(project) + elogDAO.countElogComments(project);
	}
	public long getBudget() {
		return budgetDAO.countTransactions(project);
	}
	public long getResource() {
		return rsrcDAO.countFiles(project) + rsrcDAO.countFolders(project) + rsrcDAO.countLinks(project);
	}
	public long getForum() {
		return forumDAO.getTotalForums(project);
	}
	public long getTimeline() {
		return schgDAO.countMilestones(project) + schgDAO.countPhases(project) + schgDAO.countTasks(project);
	}
	public long getNote() {
		return llogDAO.countLlogs(project);
	}
}
