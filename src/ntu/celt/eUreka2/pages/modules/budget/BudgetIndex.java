package ntu.celt.eUreka2.pages.modules.budget;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.EvenOdd;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.budget.Budget;
import ntu.celt.eUreka2.modules.budget.BudgetDAO;
import ntu.celt.eUreka2.modules.budget.PrivilegeBudget;
import ntu.celt.eUreka2.modules.budget.Transaction;
import ntu.celt.eUreka2.modules.budget.TransactionAttachedFile;
import ntu.celt.eUreka2.modules.budget.TransactionStatus;
import ntu.celt.eUreka2.modules.budget.TransactionType;
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
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;

public class BudgetIndex extends AbstractPageBudget{
	@Property
	private Project curProj;
	@SessionState
	private AppState appState;
	private String projId ;
	@Property
	private Budget budget;
	@SuppressWarnings("unused")
	@Property
	private Set<Transaction> transacts;
	@SuppressWarnings("unused")
	@Property
	private Transaction transact;
	@SuppressWarnings("unused")
	@Property
	private EvenOdd evenOdd ;
	
	enum SubmitType {DELETE};
	private SubmitType submitType ;
	
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private TransactionStatus displayStatus;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String displayCategory;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String displayCreatedBy;
	@SuppressWarnings("unused")
	@Property
	private TransactionAttachedFile tempAttFile;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private BudgetDAO budgetDAO;
	@Inject
	private Logger logger;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	void onActivate(String id) {
		projId = id;
	}
	String onPassivate() {
		return projId;
	}
	
	void setupRender(){
		curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", projId));
		}
		if(!canViewBudget(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		budget = budgetDAO.getActiveBudget(curProj);
		if(budget==null){
			budget = initNewBudget();
		}
		transacts  = budget.getTransactions();
		evenOdd = new EvenOdd();
		
	}
	/**
	 * create and initialize a new Budget object, it is used when user access 'butged module' the first time
	 * @return
	 */
	@CommitAfter
	private Budget initNewBudget(){
		Budget bg = new Budget();
		bg.setProject(curProj);
		bg.setActive(true);
		bg.setCreateDate(new Date());
		budgetDAO.saveBudget(bg);
		
		return bg;
	}
	
	
	
	private Double transactBalance = 0.0;
	private Date prevMonth;
	private Date curMonth;
	
	/**
	 * assumption here is, transacts must be sorted by Date ascending
	 * @return
	 */
	public boolean runAndCheckEndOfMonth(Transaction t){
		prevMonth = curMonth;
		curMonth = t.getTransactDate();
		
		if(prevMonth!=null ){
			Calendar prev = Calendar.getInstance();
			prev.setTime(prevMonth);
			Calendar cur = Calendar.getInstance();
			cur.setTime(curMonth);
			if(prev.get(Calendar.YEAR)==cur.get(Calendar.YEAR)
				&&	prev.get(Calendar.MONTH)==cur.get(Calendar.MONTH)){
				return false;
			}
			else
				return true;
		}
		
		return false;
	}
	public boolean runAndFilter(Transaction t){
		transactBalance += t.getFlowAmount();
		
		//check and filter
		if(displayStatus!=null){
			if(! displayStatus.equals(t.getStatus()))
				return false;
		}
		if(displayCategory!=null ){
			if(! displayCategory.equals(t.getCategory()))
				return false;
		}
		if(displayCreatedBy!=null ){
			if(! displayCreatedBy.equals(Integer.toString(t.getCreator().getId())))
				return false;
		}
		
		return true;
	}

	public String getTransactBalanceDisplay(){
		return Util.formatCurrency(transactBalance);
	}
	public String getPrevMonthDisplay(){
		return Util.formatDateTime(prevMonth, "MMM yyyy");
	}
	
	
	
	
	void onSelectedFromDeleteBtn() {
		submitType = SubmitType.DELETE;
	}
	
	void onSuccessFromBudgetListForm(){
		String[] bgtIds = request.getParameters("bgtChkbox");
		if(bgtIds !=null){
			switch(submitType){
			case DELETE:
				for(String bgtId : bgtIds){
					Transaction t = budgetDAO.getTransactionById(Long.parseLong(bgtId));
					if(!canDeleteTransaction(t)){
						appState.recordErrorMsg(messages.format("not-allow-to-delete-x", t.getDescription()));
					}
					else{
						try{
							deleteEntry(t);
						}
						catch(ConstraintViolationException ex ){
							appState.recordErrorMsg(messages.format("cant-delete-x-used-by-other", t.getDescription()));
							logger.info(ex.getMessage());
						}
					}
				}
				break;
			}
		}
	}
	@CommitAfter
	private void deleteEntry(Transaction t){
		budgetDAO.deleteTransaction(t);
	}
	public SelectModel getDisplayCategoryModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		//optModelList.add(new OptionModelImpl("All categories", "ALL_CATEGORY"));
		List<String> catList = budgetDAO.getTransactionCategories(curProj);
		for(String cat : catList){
			optModelList.add(new OptionModelImpl(cat, cat));
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	public SelectModel getDisplayCreatedByModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		//optModelList.add(new OptionModelImpl("Created by Any Users", "ALL_USER"));
		List<User> userList = budgetDAO.getTransactionUsers(curProj);
		for(User u : userList){
			optModelList.add(new OptionModelImpl(u.getDisplayName(), Integer.toString(u.getId())));
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
	public StreamResponse onActionFromExportXls(Long bgtId) throws IOException {
		
		Budget bgt = budgetDAO.getBudgetById(bgtId);
		if(bgt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "BudgetID", bgtId));
		
		Project proj = bgt.getProject();
		String[] columnHeaders = new String[]{"transactDate", "description", "remarks"
				,"category", "status", "creator", "type-inflow", "type-outflow", "balance"};
		
		// create a new workbook
		Workbook wb = new HSSFWorkbook();
		Sheet s = wb.createSheet();
		Row r = null;
		Cell c = null;
		
		// create 3 cell styles
		CellStyle cs = wb.createCellStyle();
		CellStyle cs2 = wb.createCellStyle();
		CellStyle cs3 = wb.createCellStyle();
		CellStyle cs4 = wb.createCellStyle();
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
		cs3.setDataFormat(df.getFormat("dd MMM yyyy"));
		cs4.setDataFormat(df.getFormat("$#,##0.00;-$#,##0.00"));
				
		
		// set the sheet name in Unicode
		wb.setSheetName(0, "Budget" );
		
		s.setColumnWidth(0, 12*256); //width in unit 1/256 character
		s.setColumnWidth(1, 25*256);  
		s.setColumnWidth(2, 20*256);
		s.setColumnWidth(3, 20*256);
		s.setColumnWidth(4, 12*256);
		s.setColumnWidth(5, 20*256);
		s.setColumnWidth(6, 12*256);
		s.setColumnWidth(7, 12*256);
		s.setColumnWidth(8, 12*256);
		
		
		//create header row
		r = s.createRow(0);
		c = r.createCell(0);
		c.setCellValue(messages.get("project-label")+":");
		c = r.createCell(1);
		c.setCellStyle(cs);
		c.setCellValue(proj.getDisplayName() + " - "+ proj.getName());
		
		r = s.createRow(1);
		c = r.createCell(0);
		c.setCellValue(messages.get("date-label")+":");
		c = r.createCell(1);
		c.setCellValue(proj.getSdateDisplay()+ " - "+ proj.getEdateDisplay());
		
		//create table headers
		r = s.createRow(3);
		
		for(int colnum = 0; colnum < columnHeaders.length; colnum++){
			String header = columnHeaders[colnum];
			c = r.createCell(colnum);
			c.setCellStyle(cs2);
			c.setCellValue(messages.get(header+"-label"));
		}
		
		int rownum = 4;
		double transactBalance = 0.0;
		for(Transaction t : bgt.getTransactions()){
			transactBalance += t.getFlowAmount();
			r =  s.createRow(rownum);
			c = r.createCell(0);
			c.setCellStyle(cs3);
			c.setCellValue(t.getTransactDate());
			c = r.createCell(1);
			c.setCellValue(t.getDescription());
			c = r.createCell(2);
			c.setCellValue(Util.nvl(t.getRemarks()));
			c = r.createCell(3);
			c.setCellValue(Util.nvl(t.getCategory()));
			c = r.createCell(4);
			c.setCellValue(t.getStatus().toString());
			c = r.createCell(5);
			c.setCellValue(t.getCreator().getDisplayName());
			if(TransactionType.IN_FLOW.equals(t.getType())){
				c = r.createCell(6);
				c.setCellStyle(cs4);
				c.setCellValue(t.getAmount());
			}
			if(TransactionType.OUT_FLOW.equals(t.getType())){
				c = r.createCell(7);
				c.setCellStyle(cs4);
				c.setCellValue(t.getAmount());
			}
			c = r.createCell(8);
			c.setCellStyle(cs4);
		/*	if(rownum==3)
				c.setCellFormula("G"+(rownum+1)+"-H"+(rownum+1));
			else	
				c.setCellFormula("I"+rownum+"+G"+(rownum+1)+"-H"+(rownum+1));
		*/
			c.setCellValue(transactBalance);
			rownum++;
			
		}
		
		//write the workbook to outputStream, then convert it to inputStream
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		wb.write(bout);
		ByteArrayInputStream bis = new ByteArrayInputStream(bout.toByteArray());

		
		String fileName = "Budget-"+proj.getDisplayName().replace(" ", "_")+".xls";
		String contentType = "application/vnd.ms-excel";
		return new ExportFileStreamResponse(bis, fileName, contentType);
	}
}
