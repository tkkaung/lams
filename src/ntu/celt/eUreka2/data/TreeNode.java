package ntu.celt.eUreka2.data;

import java.util.List;

public class TreeNode {

    public static final int TYPE_DOCUMENT = 0;
    public static final int TYPE_FOLDER = 1;

    private List<TreeNode> children;
    private String[] columns;
    private String[] leftColumns;
    private int type;
    private String objectId;

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }
    public String[] getLeftColumns() {
        return leftColumns;
    }

    public void setLeftColumns(String[] columns) {
        this.leftColumns = columns;
    }
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getObjectId() {
		return objectId;
	}


    

}
