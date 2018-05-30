package train;

import tree.Node;
import tree.Tree;
import treebank.Treebank;

import java.util.List;

public class ParentEncoding {

	public static ParentEncoding m_singParentEncoding = null;

	public static ParentEncoding getInstance() {
		if (m_singParentEncoding == null) {
			m_singParentEncoding = new ParentEncoding();
		}
		return m_singParentEncoding;
	}

	public static void main(String[] args) {
	}

	public Treebank smooting(Treebank treebank) {
		for (Tree tree : treebank.getAnalyses()) {
			addFatherForChild(tree.getNodes());
		}
		return treebank;
	}

	private void addFatherForChild(List<Node> nodes) {
		for (int i = nodes.size() - 1; i > 0; i--) { // nodes.get(0) == root nodes
			Node node = nodes.get(i);
			Node parent = node.getParent();
			if (parent != null) {
				node.setIdentifier(node.getIdentifier() + '#' + parent.getIdentifier() + '#');
			}
		}
	}
}
