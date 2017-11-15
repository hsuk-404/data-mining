/********************************************************************************
@FileName
    DTNode.java  
@Description
    Implementation of DTNode class. kcs0014DTNode is a class created to use it as a datatype
    Node. Its the alternative for struct that could be used in C++. It has just 
    get and set functions for all respective attributes/
@Updated 03/08/2016
@Author Kush Chandra Shrestha
********************************************************************************/
package classes;
import java.util.ArrayList;
import java.util.List;

public class DTNode {
	public String splitAttribute;
	public String label;
	public boolean isLeaf;
	public List<String> childrenValues = new ArrayList<String>();
	public List<DTNode> children = new ArrayList<DTNode>();
	
	public String getSplitAttribute() {
		return this.splitAttribute;
	}
	public void setSplitAttribute(String splitAttribute) {
		this.splitAttribute = splitAttribute;
	}
	public String getLabel() {
		return this.label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public boolean isLeaf() {
		return this.isLeaf;
	}
	public void setIsLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
	public List<String> getChildrenValues() {
		return childrenValues;
	}
	public void addChildrenValue(String childrenValue) {
		this.childrenValues.add(childrenValue);
	}
	public List<DTNode> getChildren() {
		return children;
	}
	public void addChildren(DTNode children) {
		this.children.add(children);
	}
}