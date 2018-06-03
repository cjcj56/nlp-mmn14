package train;

import static common.Consts.PARENT_ENCODING;

import tree.Node;
import tree.Terminal;
import tree.Tree;
import treebank.Treebank;

import java.util.List;
import java.util.logging.Logger;

public class ParentEncoding {

	private static final Logger LOGGER = Logger.getLogger(ParentEncoding.class.getName());
	
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
    	LOGGER.finer("applying parent enconding");
        for (Tree tree : treebank.getAnalyses()) {
            addFatherForChild(tree.getNodes());
        }
        return treebank;
    }

    private void addFatherForChild(List<Node> nodes) {
        for (int i = nodes.size() - 1; i > 0; i--) { // nodes.get(0) == root nodes
            Node node = nodes.get(i);
            /*if(node instanceof Terminal) {
            	continue;
            }*/
            Node parent = node.getParent();
            if (parent != null) {
                node.setIdentifier(new StringBuilder(node.getIdentifier()).append(PARENT_ENCODING)
                		.append(parent.getIdentifier()).append(PARENT_ENCODING).toString());
            }
        }
    }

    public Treebank unSmooting(Treebank treebank) {
        for (Tree tree : treebank.getAnalyses()) {
            subFatherForChild(tree.getNodes());
        }
        return treebank;
    }

    private void subFatherForChild(List<Node> nodes) {
        for (Node nd : nodes) { // nodes.get(0) == root nodes
            if(nd.getIdentifier().contains(PARENT_ENCODING)) {
                nd.setIdentifier(nd.getIdentifier().substring(0, nd.getIdentifier().indexOf(PARENT_ENCODING)));
            }
        }
    }
}
