package tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * 
 * @author Reut Tsarfaty
 * 
 *         CLASS: Tree
 * 
 *         Definition: a linearily ordered recursive structure Role: define
 *         ID/LP relations between nodes Responsibility: provide the entry-point
 *         for navigating inside subtrees
 * 
 * 
 */

public class Tree {

	private static final Logger LOGGER = Logger.getLogger(Tree.class.getName());

	// a designated root node
	private Node m_nodeRoot = null;

	/**
	 * C'tor
	 * 
	 * @param mNodeRoot
	 */
	public Tree() {
		super();
		m_nodeRoot = new Node();
		m_nodeRoot.setRoot(true);
	}

	public Tree(Node root) {
		super();
		m_nodeRoot = root;
		m_nodeRoot.setRoot(true);
	}

	public Node getRoot() {
		return m_nodeRoot;
	}

	public String toString() {
		return getRoot().toStringSubtree();
	}

	public List<Node> getNodes() {
		List<Node> lst = new ArrayList<Node>();
		return getRoot().getNodes(lst);
	}

	public List<Terminal> getTerminals() {
		return getRoot().getTerminals();
	}

	public List<String> getYield() {
		return getRoot().getYield();
	}

	public Object clone() {
		Tree t = new Tree();
		t.setRoot((Node) getRoot().clone());
		return t;
	}

	private void setRoot(Node n) {
		m_nodeRoot = n;
	}

	public void toCnf() {
		toCnf(getRoot(), getRoot(), new SimpleAritificialNodeCreator(0));
	}
	
	public void toCnf(ArtificialNodeCreator artificialNodeCreator) {
		toCnf(getRoot(), getRoot(), artificialNodeCreator);
	}

	private void toCnf(Node node, Node realParent, ArtificialNodeCreator artificialNodeCreator) {
		if(true) {
			throw new IllegalAccessError("NotImplmenented!");
		}
		List<Node> daughters = node.getDaughters(); // initialized at creation time, assuming never null
		if (daughters.size() <= 2) {
			for (Node daughter : daughters) {
				daughter.setParent(node);
				daughter.setRealParent(realParent);
				toCnf(daughter, node.isArtificial() ? realParent : node, artificialNodeCreator);
			}
		} else if (daughters.size() > 2) {
			List<Node> redundantDaugthers = daughters.subList(1, daughters.size());
			Node newDaughter = artificialNodeCreator.createArtificialNode(redundantDaugthers, node);
			toCnf(newDaughter, node.isArtificial() ? realParent : node, artificialNodeCreator);
		}
	}

	public void deCnf() {
		deCnf(getRoot());
	}
	
	public void deCnf(Node node) {
		throw new IllegalAccessError("NotImplmenented!");
	}
	
	public void deCnfFromPreviousState() {
		deCnfFromPreviousState(getRoot());
	}

	private void deCnfFromPreviousState(Node node) {
		if(true) {
			throw new IllegalAccessError("NotImplmenented!");
		}
		List<Node> daughters = node.getRealParent() == null ? null : node.getRealParent().getDaughters();
		if(daughters != null) {
			if (daughters.contains(node)) {
				node.setParent(node.getRealParent()); // this is probably redundant
			} else {
				node.getRealParent().addDaughter(node); // automatically sets parent
			}
			for (Node daughter : node.getDaughters()) {
				deCnfFromPreviousState(daughter);
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (this == obj) {
			return true;
		} else if (!getClass().equals(obj.getClass())) {
			return false;
		} else {
			Tree otherTree = (Tree) obj;
			Node root1 = getRoot();
			Node root2 = otherTree.getRoot();
			return compareTree(root1, root2);
		}
	}

	private boolean compareTree(Node root1, Node root2) {
		if (root1 == null) {
			return root2 == null;
		} else if (!root1.equals(root2)) {
			return false;
		} else {
			List<Node> root1Daughters = root1.getDaughters();
			List<Node> root2Daughters = root1.getDaughters();
			if (root1Daughters == null || root1Daughters.isEmpty()) {
				return root2Daughters == null || root2Daughters.isEmpty();
			} else if (root1Daughters.size() != root2Daughters.size()) {
				LOGGER.fine("different size of daughters of root1=[" + root1 + "] and root2=[ " + root2 + " ]!");
				return false;
			} else {
				for (int i = 0; i < root1Daughters.size(); ++i) {
					if (!compareTree(root1Daughters.get(i), root2Daughters.get(i))) {
						LOGGER.fine("daughter #" + i + "is different! (" + root1Daughters.get(i) + " != "
								+ root2Daughters.get(i) + ")");
						return false;
					}
				}
				return true;
			}
		}
	}
}
