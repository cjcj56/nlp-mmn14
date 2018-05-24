package tree;

public class EnhancedNode extends Node {
	
	public EnhancedNode(Node node) {
		getDaughters().addAll(node.getDaughters());
		setParent(node.getParent());
		setRoot(node.isRoot());
		setIdentifier(node.getIdentifier());
	}

	public void cleanDaughters() {
		getDaughters().clear();
	}

}
