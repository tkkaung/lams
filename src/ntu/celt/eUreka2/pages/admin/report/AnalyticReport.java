package ntu.celt.eUreka2.pages.admin.report;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.AnalyticDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.AnalyticBean;
import ntu.celt.eUreka2.entities.ProjStatus;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.scheduling.ExportFileStreamResponse;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.InjectComponent;
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
import org.slf4j.Logger;

public class AnalyticReport extends AbstractReport{
	@SuppressWarnings("unused")
	@Property
	private AnalyticBean data;
	@Property
	private List<AnalyticBean> analyticDatas = new ArrayList<AnalyticBean>();
	
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String searchProjID;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private School searchSchool;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private ProjStatus searchStatus;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private ProjType searchType;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String searchTerm;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String searchModule;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private Date searchSDate;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private Date searchEDate;
	
	
	
	@InjectComponent
	private Grid grid;

	@SessionState
	private AppState appState;
	
	@Inject
	private AnalyticDAO analyticDAO;
	@Inject
	private ProjStatusDAO projStatusDAO;
	@Inject
	private ProjTypeDAO projTypeDAO;
	@Inject
	private ProjectDAO projDAO;
	
	
	@Inject
	private Messages messages;
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private PropertyConduitSource propertyConduitSource; 
	@Inject
    private Logger log;

    
	
	void setupRender() {
		
		if(!canViewEvaluationReport()){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		if(searchSchool==null)
			searchSchool = getDefaultSchool();
		if(searchEDate==null)
			searchEDate = new Date();
		if(searchSDate==null){
			Calendar initSDate = Calendar.getInstance();
			initSDate.setTime(searchEDate);
			initSDate.add(Calendar.DAY_OF_YEAR, -30);
			searchSDate = initSDate.getTime();
		}
		
		analyticDatas = analyticDAO.statByProjID(searchSDate, searchEDate, searchProjID,
				searchSchool, searchStatus, searchType, searchTerm, searchModule) ;
		//log.debug("..........size=" + analyticDatas.size());
		
		
		//users = analyticDAO.statInstructor(searchSDate, searchEDate, searchName, searchSchool, searchStatus, searchType, searchTerm, searchModule.getName()) ;
	
		
	}
	/*void onPrepareForSubmit(){
		if(searchSchool==null)
			searchSchool = getDefaultSchool();
		
	}	*/
	
	
	@Property
	private int rowIndex;
	public int getNo(){
		return (grid.getCurrentPage()-1)*grid.getRowsPerPage()+ rowIndex + 1;
	}
	public int getTotalSize() {
		if (analyticDatas == null)
			return 0;
		return analyticDatas.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public BeanModel<AnalyticBean> getModel() {
		BeanModel<AnalyticBean> model = beanModelSource.createEditModel(AnalyticBean.class, messages);
        
        model.add("No", null);
        
        model.add("projID", null);
        model.add("projName", null);
        model.add("AccessedByRole", null);
        model.add("numLeaderInProj", null);
        model.add("numStudentInProj", null);


        model.reorder("No", "projID", "projName","moduleName", "AccessedByRole");
        return model;
    }
	
	
	
	
	void onSuccessFromFilterForm(){
		//do nothing, only need to reload

	}

	
	public SelectModel getProjStatusModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<ProjStatus> projStatusList = projStatusDAO.getAllStatus();
		for (ProjStatus ps : projStatusList) {
			OptionModel optModel = new OptionModelImpl(ps.getDisplayName(), ps);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	public SelectModel getProjTypeModel() {
		List<OptionModel> optionList = new ArrayList<OptionModel>();
		/*if("NBS".equalsIgnoreCase(getCurUser().getSchool().getName())){
			ProjType pType = projTypeDAO.getTypeByName(PredefinedNames.PROJTYPE_COURSE);
			OptionModel option = new OptionModelImpl(pType.getDisplayName(), pType);
			optionList.add(option);
		}*/
	//	else{
			List<ProjType> typeList = projTypeDAO.getAllTypes();
			for (ProjType type: typeList) {
				OptionModel option = new OptionModelImpl(type.getDisplayName(), type);
				optionList.add(option);
			}
	//	}
		SelectModel selModel = new SelectModelImpl(null, optionList);
		return selModel;
	}
	
	public SelectModel getModuleModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<String> moduleList = analyticDAO.getDistinctModules();
		if(moduleList!=null){
			for (String t : moduleList) {
				if( t != null && !t.isEmpty()){
					OptionModel optModel = new OptionModelImpl(t, t);
					optModelList.add(optModel);
				}
			}
		}
			
		
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
	public long getCountStudentByProjectID(String projID){
		return projDAO.countStudentByProjectID(projID);
	}
	public long getCountLeaderByProjectID(String projID){
		return projDAO.countLeaderByProjectID(projID);
	}
	
	public StreamResponse onExportXls() throws IOException {
		
		analyticDatas = analyticDAO.statByProjID(searchSDate, searchEDate, searchProjID,
				searchSchool, searchStatus, searchType, searchTerm, searchModule) ;
		
		
		// create a new workbook
		Workbook wb = new HSSFWorkbook();
		Sheet s = wb.createSheet();
		Row r = null;
		Row r2 = null;
		Cell c = null;
		
		// create 3 cell styles
		CellStyle cs = wb.createCellStyle();
		CellStyle cs2 = wb.createCellStyle();
		CellStyle cs3 = wb.createCellStyle();
		CellStyle cs4 = wb.createCellStyle();
		CellStyle cs5 = wb.createCellStyle();
		DataFormat df = wb.createDataFormat();
		
		// create 2 fonts objects
		Font f = wb.createFont();
		Font f2 = wb.createFont();
		//set font 1 to 12 point type
		f.setFontHeightInPoints((short) 12);
		f.setBoldweight(Font.BOLDWEIGHT_BOLD);

		//set font 2 to 10 point type
		f2.setFontHeightInPoints((short) 10);
		f2.setBoldweight(Font.BOLDWEIGHT_BOLD);
		
		//set cell stlye
		cs.setFont(f);
		//set the cell format to text see DataFormat for a full list
		cs2.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
		// set the font
		cs2.setFont(f2);
		cs2.setAlignment(CellStyle.ALIGN_CENTER);
		cs2.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		cs2.setWrapText(true);
		
		cs3.setDataFormat(df.getFormat("dd MMM yyyy"));
		cs4.setDataFormat(df.getFormat("0.0"));
		cs5.setWrapText(true);
		
		// set the sheet name in Unicode
		wb.setSheetName(0, "analytic" );
		
		s.setColumnWidth(0, 3*256);  //width in unit 1/256 character
		s.setColumnWidth(1, 25*256);
		s.setColumnWidth(2, 25*256);
		s.setColumnWidth(3, 15*256);
		s.setColumnWidth(4, 15*256);
		s.setColumnWidth(5, 15*256);
		s.setColumnWidth(6, 15*256);
		
		//create header row
		r = s.createRow(0);
		c = r.createCell(1);
		c.setCellValue("No.");
		c.setCellStyle(cs2);
		c = r.createCell(1);
		c.setCellValue("Project ID");
		c.setCellStyle(cs2);
		c = r.createCell(2);
		c.setCellStyle(cs2);
		c.setCellValue("Project Name");
		c = r.createCell(3);
		c.setCellStyle(cs2);
		c.setCellValue("Page Load");
		c = r.createCell(4);
		c.setCellStyle(cs2);
		c.setCellValue("Number of Visit");
		c = r.createCell(5);
		c.setCellStyle(cs2);
		c.setCellValue("Module Name");
		c = r.createCell(6);
		c.setCellStyle(cs2);
		c.setCellValue("Role Name");


		int rownumFirst = 1;
		int rownum = rownumFirst;
		
		for(int i=0; i<analyticDatas.size(); i++){
			AnalyticBean data = analyticDatas.get(i);
			r =  s.createRow(rownum);
			c = r.createCell(0);
			c.setCellValue(i+1);
			if(data.getProj() != null){
				c = r.createCell(1);
				c.setCellValue(data.getProj().getId());
				c = r.createCell(2);
				c.setCellValue(data.getProj().getDisplayName());
			}
			c = r.createCell(3);
			c.setCellValue(data.getPageLoad());
			c = r.createCell(4);
			c.setCellValue(data.getNumVisit());
			c = r.createCell(5);
			c.setCellValue(data.getModuleName()==null? "": data.getModuleName());
			c = r.createCell(6);
			c.setCellValue(data.getProjRole()==null? "":data.getProjRole().getName());
		
			rownum++;
		}
				
		//write the workbook to outputStream, then convert it to inputStream
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		wb.write(bout);
		ByteArrayInputStream bis = new ByteArrayInputStream(bout.toByteArray());

		
		String fileName = ("Analytic_"+ Util.formatDateTime(new Date(), "yyyyMMdd_HHmmss"))+".xls";
		String contentType = "application/vnd.ms-excel";
		return new ExportFileStreamResponse(bis, fileName, contentType);
	}
	
}
