package ntu.celt.eUreka2.components;

import java.util.Iterator;
import java.util.List;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.services.Heartbeat;

@SupportsInformalParameters
public class Tree<T extends ITree> {
	private int currentDepth;
	private Iterator<T> iterator;
	@Parameter(required = true)
	private List<T> source;
	@Parameter(required = true)
	private String treeId;
	@Parameter
	private T currentNode;
	@Environmental
	private Heartbeat heartbeat;
	
	boolean setupRender(MarkupWriter writer) {
		if (source == null) {
			return false;
		}
		currentDepth = 0;
		this.iterator = source.iterator();
		writer.element("ul", "class", "dhtmlgoodies_tree", "id", treeId);
		return iterator.hasNext();
	}
	
	void beginRender() {
		currentNode = iterator.next();
		heartbeat.begin();
	}
	
	void beforeRenderBody(MarkupWriter writer) {
		writer.writeRaw(getIndentation(currentNode.getDepth()) 
				+ "<li id='node" + currentNode.getIdentifier() + "' " +
					(currentNode.getIconName()!=null? "class='"+currentNode.getIconName()+"'" : "" )+">");
		
	}
	
	boolean afterRender() {
		heartbeat.end();
		return (!iterator.hasNext());
	}
	
	void cleanupRender(MarkupWriter writer) {
		writer.writeRaw(getIndentation(0));
		writer.end();
	}
	
	String getIndentation(int depth) {
		String s;
		if (currentDepth == 0) {
			currentDepth = 1;
			return "";
		}
		if (currentDepth > depth) {
			s = "</li>";
			while (currentDepth > depth) {
				currentDepth--;
				s += "</ul></li>";
			}
		}
		else if (currentDepth < depth) {
			currentDepth++;
			s = "<ul>";
		}
		else {
			s = "</li>";
		}
		return s;
	}
}
