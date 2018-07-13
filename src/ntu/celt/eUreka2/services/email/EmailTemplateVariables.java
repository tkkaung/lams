package ntu.celt.eUreka2.services.email;


public class EmailTemplateVariables {
	public String CreatedTime;
	public String ModifiedTime;
	public String ItemTitle;
	public String ItemContent;
	public String UserDisplayName;
	public String ProjectName;
	public String LinkBackURL;
	public String Custom1;
	public String Custom2;
	
	
	public EmailTemplateVariables(String createdTime, String modifiedTime,
			String itemTitle, String itemContent, String userDisplayName,
			String projectName, String linkBackURL, String custom1, String custom2) {
		super();
		CreatedTime = createdTime;
		ModifiedTime = modifiedTime;
		ItemTitle = itemTitle;
		ItemContent = itemContent;
		UserDisplayName = userDisplayName;
		ProjectName = projectName;
		LinkBackURL = linkBackURL;
		Custom1 = custom1;
		Custom2 = custom2;
	}
	
	
	
}
