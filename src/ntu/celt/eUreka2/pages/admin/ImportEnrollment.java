package ntu.celt.eUreka2.pages.admin;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

public class ImportEnrollment{
	@Property
	private UploadedFile file;
	private final char seperator = ',';
	private final int numColumn = 2;

	@SessionState
	private AppState appState;

	@Inject
	private Logger logger;
	@Inject
	private RequestGlobals requestGlobal;
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private ProjRoleDAO projRoleDAO;
	@Inject
	private UserDAO userDAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;

	@Component(id = "form")
	private Form form;
	private File savedFile;
	@Property
	private StringValueEncoder stringEncoder = new StringValueEncoder();

	@Inject
	private WebSessionDAO webSessionDAO;

	@Cached
	public User getCurUser() {
		return webSessionDAO.getCurrentUser(ssid);
	}

	void setupRender() {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_USER))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
	}

	void onPrepareFromForm() {
	}

	public void onValidateFormFromForm() throws IOException {
		if (!file.getFileName().toLowerCase().endsWith(".csv")) {
			form.recordError(messages.format("incorrect-file-extension-x",
					".csv"));
		} else {
			String errorMsg = "";
			String toSaveFolder = System.getProperty("java.io.tmpdir"); // get
																		// OS
																		// current
																		// temporary
																		// directory
			String prefix = requestGlobal.getHTTPServletRequest().getSession(
					true).getId();
			savedFile = new File(toSaveFolder + "/" + prefix
					+ file.getFileName());
			file.write(savedFile);

			FileInputStream fis = new FileInputStream(savedFile);
			BufferedInputStream bis = new BufferedInputStream(fis); // use
																	// buffer to
																	// improve
																	// reading
																	// speed
			DataInputStream in = new DataInputStream(bis);

			CSVReader csvReader = new CSVReader(new InputStreamReader(in),
					seperator);
			int row = 1;
			boolean isValid = true;
			int critCol = 0;
			String[] nextLine;
			while ((nextLine = csvReader.readNext()) != null) {
				/*switch (row) {
				case 1:
					if (nextLine.length != numColumn) {
						isValid = false;
						errorMsg = "Incorrect number of column, Row" + row + ", expect: "+numColumn +" but found:"+ nextLine.length;
					}
					break;
				}
*/
				row++;
			}
			in.close();

			if (!isValid) {
				savedFile.delete();
				form.recordError("File is not in correct format, " + errorMsg);
			}
		}
	}

	@CommitAfter
	public Object onSuccessFromForm() throws IOException {
		FileInputStream fis = new FileInputStream(savedFile);
		BufferedInputStream bis = new BufferedInputStream(fis); // use buffer to
																// improve
																// reading speed
		DataInputStream in = new DataInputStream(bis);

		int count = 0;
		int countNotFound = 0;
		
		
		try {
			CSVReader csvReader = new CSVReader(new InputStreamReader(in),
					seperator);
			String[] nextLine;
			nextLine = csvReader.readNext(); // project ID
			Project proj = projDAO.getProjectById(nextLine[1].trim());
			
			if (proj == null) {
				throw new RuntimeException("Project ID not valid :" + nextLine[1]);
			}

			nextLine = csvReader.readNext(); // group type name
//			groupTypeName = nextLine[1];
			nextLine = csvReader.readNext(); // number of group
//			int numOfGroup = Integer.parseInt(nextLine[1]);

			
			nextLine = csvReader.readNext(); // ignore title row (Username, Role)

			while ((nextLine = csvReader.readNext()) != null) {
				String username = nextLine[0].trim();
				String roleName = nextLine[1].trim();
				
				ProjRole projRole = projRoleDAO.getRoleByName(roleName);
				User u = userDAO.getUserByUsername(username);
				if (u != null && projRole!=null){
					
					if(! proj.hasMember(username)){
						proj.addMember(new ProjUser(proj, u, projRole));	
						count++;
					}
				}
				else{
					appState.recordWarningMsg("Not found username:" + username + " , or Role: " + roleName);
					countNotFound++;
				}
			}
			projDAO.saveProject(proj);			
			
			in.close();
			savedFile.delete();

		} catch (NumberFormatException ex) {
			appState.recordErrorMsg("Invalid Data type, " + ex.getMessage());

			logger.error(ex.getMessage());

			in.close();
			savedFile.delete();
			if(count>0){
				appState.recordWarningMsg( count + " users added ");
			}
			if(countNotFound > 0){
				appState.recordWarningMsg("Total users not added :" + countNotFound  );
			}
			return null;
		} catch (RuntimeException ex) {
			appState.recordErrorMsg(ex.getMessage());

			logger.error(ex.getMessage());

			in.close();
			savedFile.delete();
			
			if(count>0){
				appState.recordWarningMsg( count + " users added ");
			}
			if(countNotFound > 0){
				appState.recordWarningMsg("Total users not added :" + countNotFound  );
			}
			return null;
		}
		appState.recordInfoMsg( count + " users added");
		if(countNotFound > 0){
			appState.recordWarningMsg("Total users not added :" + countNotFound  );
		}
		return null;

	}

	Object onUploadException(FileUploadException ex) {
		form.recordError("Upload exception: " + ex.getMessage());

		return this;
	}

}
