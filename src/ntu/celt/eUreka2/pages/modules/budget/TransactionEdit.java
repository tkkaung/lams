package ntu.celt.eUreka2.pages.modules.budget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.budget.Budget;
import ntu.celt.eUreka2.modules.budget.BudgetDAO;
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
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;

public class TransactionEdit extends AbstractPageBudget {
	@Property
	private Project curProj;
	@SessionState
	private AppState appState;
	private Budget budget;
	@Property
	private Transaction transact;
	private Long transactId;
	@SuppressWarnings("unused")
	@Property
	private String shouldDisableInput = null; 
	@Property
	private String categorySelect; 
	@Property
	private boolean notifyMember;
	@Property
    private UploadedFile file1;
	@SuppressWarnings("unused")
	@Property
	private TransactionAttachedFile tempAttFile;
	
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
		transactId = ec.get(Long.class, 0);
		
		transact = budgetDAO.getTransactionById(transactId);
		if(transact==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "TransactionID", transactId));
		budget = transact.getBudget();
		curProj = budget.getProject();
		categorySelect = transact.getCategory();
		
	}
	Object[] onPassivate() {
		return new Object[] {transactId};
	}
	
	
	void setupRender(){
		if(!canEditTransaction(transact))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		//shouldDisableInput = "disabled"; //if equals null, means enable input
	}
	
	void onPrepareFromForm(){ 
		transact.setRemarks(Util.html2textarea(transact.getRemarks()));
	}
	
	@CommitAfter
	Object onSuccessFromForm(){
		if(!"NEW_VALUE".equals(categorySelect)){
			transact.setCategory(categorySelect);
		}
		transact.setModifyDate(new Date());
		transact.setEditor(getCurUser());
		transact.setRemarks(Util.textarea2html(transact.getRemarks()));
		
		if(file1!=null)
			saveFile(file1, null);
		budgetDAO.saveTransaction(transact);
		
		if(notifyMember){
			EmailTemplateVariables var = new EmailTemplateVariables(
					transact.getCreateDate().toString(), transact.getModifyDate().toString(),
					transact.getDescription(), 
					Util.truncateString(Util.stripTags(transact.getRemarks()), Config.getInt("max_content_lenght_in_email")), 
					transact.getEditor().getDisplayName(),
					curProj.getDisplayName(), 
					emailManager.createLinkBackURL(request, BudgetIndex.class, curProj.getId()),
					Util.nvl(transact.getCategory()), transact.getStatus().toString());
			
			try{
				emailManager.sendHTMLMail(curProj.getUsers(), EmailTemplateConstants.BUDGET_TRAN_EDITED, var);
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
	@InjectComponent
	private Zone attachedFilesZone;
	@CommitAfter
	Object onActionFromRemoveAttachment(String attachId){
		TransactionAttachedFile attFile = budgetDAO.getAttachedFileById(attachId);
		if(attFile==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AttachmentID", attachId));
		
		transact = attFile.getTransact();
		
		if(!canEditTransaction(transact)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		attFileManager.removeAttachedFile(attFile);
		
		transact.removeAttachFile(attFile);
		transact.setEditor(getCurUser());
		transact.setModifyDate(new Date());
		budgetDAO.saveTransaction(transact);
		return attachedFilesZone.getBody();
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
