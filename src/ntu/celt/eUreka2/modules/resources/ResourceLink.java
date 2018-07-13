package ntu.celt.eUreka2.modules.resources;

public class ResourceLink extends Resource {
	
	private String url;
	
	public ResourceType getType() {
		return ResourceType.Link;
	}
	public ResourceLink clone(){
		ResourceLink rl = (ResourceLink) super.clone(ResourceLink.class);
		rl.setUrl(url);
		return rl;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUrl() {
		return url;
	}
	
}
