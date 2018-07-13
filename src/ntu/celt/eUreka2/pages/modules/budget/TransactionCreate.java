package ntu.celt.eUreka2.pages.modules.budget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.budget.Budget;
import ntu.celt.eUreka2.modules.budget.BudgetDAO;
import ntu.celt.eUreka2.modules.budget.PrivilegeBudget;
import ntu.celt.eUreka2.modules.budget.Transaction;
import ntu.celt.eUreka2.modules.budget.TransactionAttachedFile;
import ntu.celt.eUreka2.modules.budget.TransactionStatus;
import ntu.celt.eUreka2.modules.budget.TransactionType;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;

public class TransactionCreate extends AbstractPageBudget {
	@Property
	private Project curProj;
	@SessionState
	private AppState appState;
	@Property
	private Budget budget;
	private Long budgetId;
	@Property
	private Transaction transact;
	@SuppressWarnings("unused")
	@Property
	private String shouldDisableInput = null; 
	@Property
	private String categorySelect; 
	@Property
	private boolean notifyMember;
	@Property
    private UploadedFile file1;
	
	@Inject
	private BudgetDAO budgetDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	@Inject
	private EmailManager emailManager;
	@Inject
	private Logger logger;
	@Inject
	private AttachedFileManager attFileManager;
	
	
	void onActivate(EventContext ec) {
		budgetId = ec.get(Long.class, 0);
	}
	Long onPassivate() {
		return budgetId;
	}
	
	
	void setupRender(){
		budget  = budgetDAO.getBudgetById(budgetId);
		if(budget==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "BudgetID", budgetId));
		}
		curProj = budget.getProject();
		
		if(!canCreateTransaction(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
	}
	
	void onPrepareFromForm(){ 
		budget  = budgetDAO.getBudgetById(budgetId);
		curProj = budget.getProject();
		if(transact==null){
			transact = new Transaction();
			transact.setBudget(budget);
			transact.setCreator(getCurUser());
			transact.setType(TransactionType.IN_FLOW);
			transact.setStatus(TransactionStatus.PENDING);
			transact.setTransactDate(new Date());
		}
	}
	
	@CommitAfter
	Object onSuccessFromForm(){
		if(!"NEW_VALUE".equals(categorySelect)){
			transact.setCategory(categorySelect);
		}
		transact.setCreateDate(new Date());
		transact.setModifyDate(new Date());
		budget.addTransaction(transact);
		transact.setBudget(budget);
		transact.setRemarks(Util.textarea2html(transact.getRemarks()));
		
		
		if(file1!=null)
			saveFile(file1, null);
		
		budgetDAO.saveTransaction(transact);
		
		if(notifyMember){
			EmailTemplateVariables var = new EmailTemplateVariables(
					transact.getCreateDate().toString(),
					transact.getModifyDate().toString(),
					transact.getDescription(), 
					Util.truncateString(Util.stripTags(transact.getRemarks()), Config.getInt("max_content_lenght_in_email")), 
					transact.getCreator().getDisplayName(),
					curProj.getDisplayName(), 
					emailManager.createLinkBackURL(request, BudgetIndex.class, curProj.getId()),
					Util.nvl(transact.getCategory()), transact.getStatus().toString());
			
			try{
				emailManager.sendHTMLMail(curProj.getUsers(), EmailTemplateConstants.BUDGET_TRAN_ADDED, var);
				appState.recordInfoMsg(messages.get("notification-sent-to-members"));
			}
			catch(SendEmailFailException e){
				logger.warn(e.getMessage(), e);
				appState.recordWarningMsg(e.getMessage());
			}
		}
		
		return linkSource.createPageRenderLinkWithContext(BudgetIndex.class, curProj.getId());
	}
	
	
	private void saveFile(UploadedFile upFile, String aliasFileName){
		TransactionAttachedFile f = new TransactionAttachedFile();
		f.setId(Util.generateUUID());
		f.setFileName(upFile.getFileName());
		f.setAliasName(aliasFileName);
		f.setContentType(upFile.getContentType());
		f.setSize(upFile.getSize());
		f.setCreator(getCurUser());
		f.setPath(attFileManager.saveAttachedFile(upFile, f.getId(), getModuleName(), curProj.getId()));
		
		transact.addAttachFile(f);
	}
	
	
	public TransactionType getInFlow(){return TransactionType.IN_FLOW; }
	public TransactionType getOutFlow(){return TransactionType.OUT_FLOW; }
	
	public TransactionStatus getPending(){return TransactionStatus.PENDING; }
	public TransactionStatus getConfirm(){return TransactionStatus.CONFIRM; }
	
	private String[] defaultCats = {messages.get("default-category1"), messages.get("default-category2")
									,messages.get("default-category3"), messages.get("default-category4")
									,messages.get("default-category5")};
	public SelectModel getCategoryModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		optModelList.add(new OptionModelImpl(messages.get("other"), "NEW_VALUE"));
		List<String> catList = budgetDAO.getTransactionCategories(curProj);
		for(String cat : catList){
			optModelList.add(new OptionModelImpl(cat, cat));
		}
		for(String cat : defaultCats){
			if(!catList.contains(cat))
				optModelList.add(new OptionModelImpl(cat, cat));
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
}
