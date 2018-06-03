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
		toCnf(getRoot(), new SimpleAritificialNodeCreator(0));
	}

	public void toCnf(ArtificialNodeCreator artificialNodeCreator) {
		toCnf(getRoot(), artificialNodeCreator);
	}

	private void toCnf(Node node, ArtificialNodeCreator artificialNodeCreator) {
		/*
		 * if(true) { throw new IllegalAccessError("NotImplmenented!"); }
		 */
		List<Node> daughters = node.getDaughters(); // initialized at creation time, assuming never null
		if (!daughters.isEmpty()) {
			toCnf(daughters.get(0), artificialNodeCreator);
			if (daughters.size() > 2) {
				List<Node> redundantDaugthers = new ArrayList<>(daughters.subList(1, daughters.size()));
				daughters.removeAll(redundantDaugthers);
				Node newDaughter = artificialNodeCreator.createArtificialNode(redundantDaugthers, node);
			}
			if(daughters.size() == 2) { // at this point, there are either 1 or 2 daughters
				toCnf(daughters.get(1), artificialNodeCreator);
			}
		}
	}

	public void deCnf() {
		deCnf(getRoot());
	}

	private void deCnf(Node node) {
		/*
		 * if(true) { throw new IllegalAccessError("NotImplmenented!"); }
		 */
		List<Node> daughters = node.getDaughters();
		if ((daughters != null) && (!daughters.isEmpty())) {
			if (daughters.size() > 1) {
				Node rightDughter = daughters.get(1);
				while (rightDughter.isArtificial()) {
					node.addDaughters(rightDughter.getDaughters());
					rightDughter = daughters.get(daughters.size() - 1);
				}
			}
			
			int i = 0;
			while(i < daughters.size()) {
				Node daughter = daughters.get(i);
				if (daughter.isArtificial()) {
					daughters.remove(i);
				} else {
					deCnf(daughter);
					++i;
				}
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
