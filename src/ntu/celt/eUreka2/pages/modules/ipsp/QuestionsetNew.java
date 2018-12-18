package ntu.celt.eUreka2.pages.modules.ipsp;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.ipsp.IPSPDAO;
import ntu.celt.eUreka2.modules.ipsp.IDimension;
import ntu.celt.eUreka2.modules.ipsp.IQuestion;
import ntu.celt.eUreka2.modules.ipsp.IQuestionSet;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Select;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

public class QuestionsetNew extends AbstractPageIPSP{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private IQuestionSet qset;
	@Property
	private int temCreate ; //0 = create from scratch, 1 = load from template, 2 = load from CSV
	@Property
	private IQuestionSet qsetToLoad;
	@Property
    private UploadedFile csvfile;
    private final char seperator = ',';
	private final int CSV_NUM_COLUMN = 7;
	private final String LINE_SEPARATOR = System.getProperty("line.separator");
	private File savedFile;
	
	@SessionState
	private AppState appState;
	
	@Inject
	private RequestGlobals requestGlobal;
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private IPSPDAO ipspDAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Request request;
	@Inject
	private Logger logger;
	
	
	void onActivate(String id) {
		pid = id;
		
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", pid));
		
	}
	String onPassivate() {
		return pid;
	}
		
	void setupRender(){
		if(!canManageQuestionSet(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		
		if(qset==null){
			qset = new IQuestionSet();
			qset.setName("");
		}
		
	}
	
	
	
		
	void onPrepareForSubmitFromForm(){
		if(qset==null){
			qset = new IQuestionSet();			
		}

	}
	
	
	@Component(id="form")
	private Form form;
	@Component(id="qsetToLoad")
	private Select qsetToLoadSelect;
	
	
	void onValidateFormFromForm() throws IOException{
		if(temCreate==1 && qsetToLoad==null){
			form.recordError(qsetToLoadSelect, "Please select question set to load");
			return;
		}
		if(temCreate==2 ){
		     
			if(csvfile==null){
				form.recordError(qsetToLoadSelect, "Please select CSV file to load");
				return;
			}
			
			if(! csvfile.getFileName().toLowerCase().endsWith(".csv")){
				form.recordError(messages.format("incorrect-file-extension-x", ".csv")); 
				return;
			}
			
	    	String toSaveFolder = System.getProperty("java.io.tmpdir"); //get OS current temporary directory
	        String prefix = requestGlobal.getHTTPServletRequest().getSession(true).getId();
	    	savedFile = new File(toSaveFolder+ "/"+ prefix + csvfile.getFileName());
	        csvfile.write(savedFile);
	       
	        FileInputStream fis = new FileInputStream(savedFile);
	        BufferedInputStream bis = new BufferedInputStream(fis); //use buffer to improve reading speed
	        DataInputStream in = new DataInputStream(bis);
		    	
	    	CSVReader csvReader = new CSVReader(new InputStreamReader(in), seperator);
	        String[] nextLine;
	        nextLine = csvReader.readNext(); //read the first row, (the title row)
	        in.close();
        	
	         
	        if(nextLine == null || nextLine.length != CSV_NUM_COLUMN){
	        	savedFile.delete();
	        	form.recordError(messages.format("incorrect-input-format-require-x-found-y", CSV_NUM_COLUMN, (nextLine==null? 0 : nextLine.length))); 
	        }
			
	         
		}
		
	}
	
	
	@CommitAfter
	Object onSuccessFromForm(){
		qset.setCdate(new Date());
		qset.setMdate(new Date());
		qset.setOwner(getCurUser());
		
		
		switch (temCreate){
		case 0:  //from scratch
			int numQuestion = 5;
			for(int i=1; i<=numQuestion; i++){
				String[] dims = new String[IDimension.NUM_DIMENSIONS];
				qset.addQuestion(new IQuestion("", qset,i, dims ));
			}
			break;
		case 1: //load template
			List<IQuestion> qList = qsetToLoad.getQuestions();
			for(int i=0; i<qList.size(); i++){
				IQuestion q = qList.get(i);
				;
				qset.addQuestion(q.createCopy());
			}
			break;
		case 2: //load CSV
			int numIgnore = 0;
	        StringBuffer processLog = new StringBuffer();
	        FileInputStream fis;
	        String msg;
	        
	     
	      //load DimensionList
	        Map<Integer, IDimension> dimMap = new HashMap<Integer, IDimension>();
	        List<IDimension> dimList = ipspDAO.getAllIDimensions();
	        for(IDimension dim : dimList){
	        	int numID = dim.getId();
	        	dimMap.put(numID, dim);
	        }
	        
	        
			try {
				fis = new FileInputStream(savedFile);
			
		        BufferedInputStream bis = new BufferedInputStream(fis); //use buffer to improve reading speed
		        DataInputStream in = new DataInputStream(bis);
	
		    	CSVReader csvReader = new CSVReader(new InputStreamReader(in), seperator);
		        String[] nextLine;
		        nextLine = csvReader.readNext(); //ignore the first row, (the title row)
		        
			    
		        processLog.append("Start time: "+ new Date() + LINE_SEPARATOR);
		        
		        
		        while((nextLine = csvReader.readNext()) != null){
			//	     logger.error(".....nextLine=" + Util.arrayToString(nextLine));

		        	if(nextLine.length != CSV_NUM_COLUMN){
		        		msg = "Ignore row becuase number of column is invalid, require "
		    				+ CSV_NUM_COLUMN + ", found " + nextLine.length;
		        		processLog.append(msg+LINE_SEPARATOR);
		        		numIgnore++;
		        		continue;  //ignore if the line is invalid
		        	}
		        	try{
		        		String numStr = nextLine[0].trim();
			        	Integer num = (numStr.isEmpty())? null : Integer.parseInt(numStr);
			        	String itemDes = nextLine[1].trim();
						String[] dims = new String[IDimension.NUM_DIMENSIONS];

			        	for(int i=0; i<IDimension.NUM_DIMENSIONS; i++){
			        		String dim = (nextLine[2+i].trim()).toUpperCase();
				        		dims[i] = dim;
			        	}
			        	
			        	IQuestion q = new IQuestion(itemDes, qset, num, dims);
			        	
			        	qset.addQuestion(q);
			        	
		        	}
		        	catch (NumberFormatException e) {
		        		e.printStackTrace();
						processLog.append("NumberFormatException: "+ e.getMessage() + LINE_SEPARATOR);
		        	}
		        }
				in.close();
				savedFile.delete();
			 } catch (FileNotFoundException e) {
					e.printStackTrace();
					processLog.append("FileNotFoundException: "+ e.getMessage() + LINE_SEPARATOR);
					
			} catch (IOException e) {
				e.printStackTrace();
				processLog.append("IOException: "+ e.getMessage() + LINE_SEPARATOR);
			}
			processLog.append("Finish time: "+ new Date() + LINE_SEPARATOR);
			
			//appState.recordInfoMsg(processLog.toString());
			
			break;
		}
		
		ipspDAO.immediateAddIQuestionSet(qset);
		//return linkSource.createPageRenderLinkWithContext(ManageQuestionset.class, project.getId());
		return linkSource.createPageRenderLinkWithContext(QuestionsetEdit.class, project.getId(), qset.getId());
	}
	

	  
}
