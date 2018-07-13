package ntu.celt.eUreka2.components;

public class TreeNode implements ITree {
	private String identifier;
	private String name;
	private String des;
	private int depth;
	private String iconName;
	
	public TreeNode(String identifier, String name,String des, int depth) {
		this.depth = depth;
		this.identifier = identifier;
		this.name = name;
		this.des = des;
	}
	public TreeNode(String identifier, String name, String des, int depth, String iconName) {
		this.depth = depth;
		this.identifier = identifier;
		this.name = name;
		this.des = des;
		this.iconName = iconName;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public void setIconName(String iconName) {
		this.iconName = iconName;
	}
	public String getIconName() {
		return iconName;
	}
	public void setDes(String des) {
		this.des = des;
	}
	public String getDes() {
		return des;
	}
	
}
