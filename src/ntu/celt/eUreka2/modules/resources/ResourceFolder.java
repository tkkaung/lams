package ntu.celt.eUreka2.modules.resources;

import java.util.HashSet;
import java.util.Set;



public class ResourceFolder extends Resource{
	
	private Set<Resource> children = new HashSet<Resource>();
	
	public ResourceType getType() {
		return ResourceType.Folder;
	}
	public ResourceFolder clone(){
		ResourceFolder rf = (ResourceFolder) super.clone(ResourceFolder.class);
	/*	for(Resource c : children){  
			rf.getChildren().add(c.clone()); //didn't do this because it can conflict with resource.setParent() 
		}
		*/
		return rf;
	}
	
	public void setChildren(Set<Resource> children) {
		this.children = children;
	}
	public Set<Resource> getChildren() {
		return children;
	}

	
}
