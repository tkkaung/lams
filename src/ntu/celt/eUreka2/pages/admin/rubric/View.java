package ntu.celt.eUreka2.pages.admin.rubric;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.assessment.RubricCriteria;
import ntu.celt.eUreka2.modules.assessment.RubricCriterion;
import ntu.celt.eUreka2.modules.scheduling.ExportFileStreamResponse;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;

public class View extends AbstractPageAdminRubric{
	private int id;
	@Property
	private Rubric rubric;
	
	
	@SuppressWarnings("unused")
	@Property
	private RubricCriteria tempRCrit; 
	@SuppressWarnings("unused")
	@Property
	private RubricCriterion tempRCriterion; 
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	@SuppressWarnings("unused")
	@Property
	private int colIndex;
	@Property
	private String oQuestion;
	
	
	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;
	@Inject
    private PropertyConduitSource propertyConduitSource; 
   
	
	void onActivate(int id) {
		this.id = id;
	}
	int onPassivate() {
		return id;
	}
	
	void setupRender(){
		rubric = aDAO.getRubricById(id);
		if(rubric==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "RubricID", id));
		}  
		if(!canViewRubric(rubric)) 
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		

	}
	
	
	
	
	public List<RubricCriterion> getFirstCriterions(){
		return rubric.getCriterias().get(0).getCriterions();
	}
	
	
	public BeanModel<Rubric> getModel() {
		BeanModel<Rubric> model = beanModelSource.createEditModel(Rubric.class, messages);
		if(rubric.getOwner().equals(getCurUser())){
			model.include("name","des","shared","cdate","mdate");
		}
		else{
			model.include("name","des","mdate");
		}
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_RUBRIC)){
			model.add("master", propertyConduitSource.create(Rubric.class, "master"));
			model.reorder("name","des","master");
		}
		model.add("owner", null);
		model.add("possibleScore",null);
		
		return model;
	}
	
	
	
	public StreamResponse onExportXls(int rId) throws IOException {
		Rubric rub = aDAO.getRubricById(rId);
		if(rub==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Rubric ID", rId));
				
		// create a new workbook
		Workbook wb = new HSSFWorkbook();
		Sheet s = wb.createSheet();
		Row r = null;
		Row r2 = null;
		Cell c = null;
		
		// create 3 cell styles
		CellStyle cs = wb.createCellStyle();
		CellStyle cs11 = wb.createCellStyle();
		CellStyle cs2 = wb.createCellStyle();
		CellStyle cs21 = wb.createCellStyle();
		CellStyle cs3 = wb.createCellStyle();
		CellStyle cs4 = wb.createCellStyle();
		CellStyle cs5 = wb.createCellStyle();
		DataFormat df = wb.createDataFormat();
		
		// create 2 fonts objects
		Font f = wb.createFont();
		Font f2 = wb.createFont();
		Font f3 = wb.createFont();
		//set font 1 to 12 point type
		f.setFontHeightInPoints((short) 12);
		f.setBoldweight(Font.BOLDWEIGHT_BOLD);

		//set font 2 to 10 point type
		f2.setFontHeightInPoints((short) 10);
		f2.setBoldweight(Font.BOLDWEIGHT_BOLD);
		
		f3.setFontHeightInPoints((short) 10);
		
		//set cell stlye
		cs.setFont(f);
		cs.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		
		cs11.setAlignment(CellStyle.ALIGN_CENTER);
		cs11.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		cs11.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
		cs11.setFont(f);
		cs11.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		cs11.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		
		//set the cell format to text see DataFormat for a full list
		cs2.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
		// set the font
		cs2.setFont(f2);
		cs2.setAlignment(CellStyle.ALIGN_LEFT);
		cs2.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		cs2.setWrapText(true);
		
		cs21.setFont(f3);
		cs21.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
		cs21.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		cs21.setWrapText(true);
		
		cs3.setDataFormat(df.getFormat("dd MMM yyyy"));
		cs4.setDataFormat(df.getFormat("0.0"));
		cs5.setWrapText(true);
		
		// set the sheet name in Unicode
		wb.setSheetName(0, "Rubric" );
		
		
		List<RubricCriteria> critList = rub.getCriterias();
		List<RubricCriterion> crionList = new ArrayList<RubricCriterion>();		
		if (!critList.isEmpty())
			crionList = critList.get(0).getCriterions();
		
		int colNum = 2;  //column number that start of Criteria (vary column)
	
		
		s.setColumnWidth(0, 50*256);  //width in unit 1/256 character
		s.setColumnWidth(1, 10*256);
		for(int i=colNum; i<(colNum+crionList.size()); i+=1){
			s.setColumnWidth(i, 35*256);	
		}
	
		//create header row
		String names[] = {"name","des","shared","master","cdate","mdate"};
		Object values[] = {rub.getName(), rub.getDes(), rub.isShared(), rub.isMaster(), rub.getCdate(), rub.getMdate()};
		int rowNum = names.length + 1;
		
		for(int i=0; i<names.length; i++) {
			r = s.createRow(i);
			c = r.createCell(0);
			c.setCellStyle(cs);
			c.setCellValue(messages.get(names[i] + "-label"));
			c = r.createCell(1);
			c.setCellStyle(cs);
			switch(i) {
			case 0:
			case 1: c.setCellValue((String) values[i]);
			break;
			case 2:
			case 3: c.setCellValue((Boolean) values[i]);
			break;
			case 4:
			case 5: c.setCellValue((Date) values[i]);
			c.setCellStyle(cs3);
			break;
			}
		}
		
	
		s.addMergedRegion(new CellRangeAddress(rowNum, rowNum+1, 0, 0)); //(firstRow,lastRow, firstCol, lastCol)
		s.addMergedRegion(new CellRangeAddress(rowNum, rowNum+1, 1, 1)); 
		if (crionList.size()>0){
			s.addMergedRegion(new CellRangeAddress(rowNum, rowNum, colNum, colNum+1*crionList.size()-1));
		}
		
		//create table headers
		r = s.createRow(rowNum);
		c = r.createCell(0);
		c.setCellStyle(cs11);
		c.setCellValue(messages.get("criteria-objective"));
		c = r.createCell(1);
		c.setCellStyle(cs11);
		c.setCellValue(messages.get("weightage-label"));
		c = r.createCell(2);
		c.setCellStyle(cs11);
		c.setCellValue(messages.get("criterion"));

		
		r = s.createRow(rowNum+1);
		for(int i=0; i <  crionList.size(); i++){
			c = r.createCell(i + colNum);
			c.setCellStyle(cs11);
			c.setCellValue(crionList.get(i).getScore());
		}
		
		
		int rownum = rowNum + 2;
		
		for(int i=0; i<critList.size(); i++){
			RubricCriteria crit = critList.get(i);
			r =  s.createRow(rownum);
			c = r.createCell(0);
			c.setCellStyle(cs2);
			c.setCellValue(crit.getName());
			c = r.createCell(1);
			c.setCellStyle(cs2);
			c.setCellValue(crit.getWeightage());
			
			for(int j=0; j<crit.getCriterions().size(); j++){
				RubricCriterion crion = crit.getCriterions().get(j);
				c = r.createCell(colNum+ 1*j);
				c.setCellStyle(cs21);
				c.setCellValue(crion.getDes());

			}
			
			rownum++;
		}
		
		rownum++; //leave one row
		
		
		r = s.createRow(rownum);
		c = r.createCell(0);
		c.setCellStyle(cs2);
		c.setCellValue(messages.get("qualitative-feedback-label") + ":");
		if(rub.getUseCmtStrength()) {
			c = r.createCell(1);
			c.setCellStyle(cs2);
			c.setCellValue(rub.getCmtStrength());
			rownum++;
		}
		if(rub.getUseCmtWeakness()) {
			r = s.createRow(rownum);
			c = r.createCell(1);
			c.setCellStyle(cs2);
			c.setCellValue(rub.getCmtWeakness());
			rownum++;
		}
		if(rub.getUseCmtOther()) {
			r = s.createRow(rownum);
			c = r.createCell(1);
			c.setCellStyle(cs2);
			c.setCellValue(rub.getCmtOther());
			rownum++;
		}

		rownum++;

		if(canUseOpenQuestion()) {
			for(int j=0; j<rub.getOpenEndedQuestions().size(); j++) {
				r = s.createRow(rownum);
				if(j==0) {
					c = r.createCell(0);
					c.setCellStyle(cs2);
					c.setCellValue(messages.get("open-question-label") + ":");
				}
				c = r.createCell(1);
				c.setCellStyle(cs2);
				c.setCellValue(rub.getOpenEndedQuestions().get(j));
				s.addMergedRegion(new CellRangeAddress(rownum, rownum, 1, colNum + crionList.size()-1)); //(firstRow,lastRow, firstCol, lastCol)

				rownum++;
			}

		}
		
		
		
		//write the workbook to outputStream, then convert it to inputStream
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		wb.write(bout);
		ByteArrayInputStream bis = new ByteArrayInputStream(bout.toByteArray());

		
		String fileName = ("Rubric-" + rub.getName()).replace(" ", "_")+".xls";
		String contentType = "application/vnd.ms-excel";
		return new ExportFileStreamResponse(bis, fileName, contentType);
	}

	
}
