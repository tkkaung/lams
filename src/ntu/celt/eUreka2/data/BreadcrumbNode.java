package ntu.celt.eUreka2.data;

import org.apache.tapestry5.Link;

public class BreadcrumbNode {
	private String title;
	private Link link;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setLink(Link link) {
		this.link = link;
	}
	public Link getLink() {
		return link;
	}
	
	
	
}
