package ntu.celt.eUreka2.components;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;

import ntu.celt.eUreka2.data.RenderableNode;
import ntu.celt.eUreka2.data.TreeNode;

/**
 * The Class TreeGrid.
 */
@SupportsInformalParameters
@Import(library="treeGrid/TreeGrid.js",stylesheet="treeGrid/TreeGrid.css")
public class TreeGrid {

    /** The list. */
    private List<RenderableNode> list;

    /** Iterator to iterate over all tree elements. */
    private Iterator<RenderableNode> iterator;

    /** Defines the source Tree to walk over. */
    @Parameter(required = true)
    private List<TreeNode> source;

    /** The column headers. */
    @Parameter(required = true)
    private List<String> columnHeaders;
    /** The column headers. */
    @Parameter
    private List<String> leftColumnHeaders;
    @Parameter
    private boolean showHeaders = true;
    @Parameter
    private String selectedObject;
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String clazz;
    
    
    /** Current node of the tree. */
    private RenderableNode currentNode;
    
    /**
     * Builds the source list.
     * 
     * @param treeNode the tree node
     * @param depth the depth
     * @param dotId the dot id
     */
    private void buildSourceList(TreeNode treeNode, int depth, String dotId) {
        list.add(new RenderableNode(treeNode, depth, dotId));
        List<TreeNode> children = treeNode.getChildren();
        if (children != null) {
            int i = 0;
            for (TreeNode node : children) {
                if (!list.contains(node)) {
                    buildSourceList(node, depth + 1, dotId + "-" + i);
                    i++;
                }
            }
        }
    }

    /**
     * Setup render.
     * 
     * @param writer the writer
     * 
     * @return true, if successful
     */
    boolean setupRender(MarkupWriter writer) {
        if (source == null)
            return false;

        list = new ArrayList<RenderableNode>();
        int i = 0;
        for (TreeNode node : source) {
            buildSourceList(node, 0, Integer.toString(i));
            i++;
        }
        iterator = list.iterator();

        // begin table
        writer.element("table",  "class", "e-treegrid");

        // header row
        writer.element("thead");
        writer.element("tr");
        if (leftColumnHeaders != null) {
            for (String column : leftColumnHeaders) {
            	if(showHeaders){
	            	writer.element("th", "class", column+"-header"+ (clazz==null? "":" "+clazz));
	                writer.writeRaw(column);
	                writer.end();
            	}
            }
        }
        if (columnHeaders != null) {
            for (String column : columnHeaders) {
            	if(showHeaders){
                	writer.element("th", "class", column+"-header"+ (clazz==null? "":" "+clazz));
                	writer.writeRaw(column);
                	writer.end();
                }
            }
        }
        writer.end();
        writer.end();

        return (iterator.hasNext());
    }

    /**
     * Begins a node.
     * 
     * @param writer the writer
     */
    void beginRender(MarkupWriter writer) {
        currentNode = iterator.next();

        String[] columns = currentNode.getInnerNode().getColumns();
        String[] leftColumns = currentNode.getInnerNode().getLeftColumns();
    
        if(selectedObject!=null && selectedObject.equals(currentNode.getInnerNode().getObjectId())){
        	writer.element("tr", "id", currentNode.getDotId(), "class", "selected");
        }
        else{
         	writer.element("tr", "id", currentNode.getDotId());
        }
        	
        if (leftColumnHeaders != null) {
	        for (int i = 0; i < leftColumnHeaders.size(); i++) {
	            writer.element("td");
	            if (i < leftColumns.length) {
	                writer.writeRaw(leftColumns[i]);
	            } else {
	                writer.writeRaw("&nbsp;");
	            }
	            // td
	            writer.end();
	        }
        }
        writer.element("td");

        if (currentNode.getInnerNode().getType() == TreeNode.TYPE_FOLDER) {
            writer.element("div", "class", "folderDiv tier" + currentNode.getDepth(), "onclick", "toggleRowsDiv(this)");
            writer.element("a", "href", "#", "class", "folderopen");
            
        } else {
            writer.element("div", "class", "folderDiv tier" + currentNode.getDepth());
            writer.element("a", "href", "#", "class", "doc");
        }
        // a
        writer.end();
        // div
        writer.end();

        if (columns.length > 0 && columns[0] != null && !columns[0].equals("")) {
            writer.writeRaw(columns[0]);
        } else {
            writer.writeRaw("&nbsp;");
        }

 
        // td
        writer.end();

        for (int i = 1; i < columnHeaders.size(); i++) {
            writer.element("td");
            if (i < columns.length) {
                writer.writeRaw(columns[i]);
            } else {
                writer.writeRaw("&nbsp;");
            }
            // td
            writer.end();
        }

        // tr
        writer.end();

    }

    /**
     * Ends the node.
     * 
     * @param writer the writer
     * 
     * @return true, if after render
     */
    boolean afterRender(MarkupWriter writer) {
        return (!iterator.hasNext());
    }

    /**
     * Cleanup render.
     * 
     * @param writer the writer
     */
    void cleanupRender(MarkupWriter writer) {
        // end table
        writer.end();
    }

}
