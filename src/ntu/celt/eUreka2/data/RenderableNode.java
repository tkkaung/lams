package ntu.celt.eUreka2.data;

public class RenderableNode {

    private TreeNode innerNode;
    private String dotId;
    private int depth;

    public RenderableNode(TreeNode innerNode, int depth, String dotId) {
        super();
        this.innerNode = innerNode;
        this.dotId = dotId;
        this.depth = depth;
    }

    public TreeNode getInnerNode() {
        return innerNode;
    }

    public String getDotId() {
        return dotId;
    }

    public int getDepth() {
        return depth;
    }

}

