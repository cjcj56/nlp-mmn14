package tree;

import static common.Consts.*;

import java.util.Iterator;
import java.util.List;

public interface ArtificialNodeCreator {

	Node createArtificialNode(List<Node> nodes, Node parent);
	
	static String nodeIdJoiner(List<Node> nodes, int len) {
		if(len == 0) {
			return NO_NODE_SYM; 
		}
		StringBuilder sb = new StringBuilder();
		if(nodes.size() < len) {
			for(int i = 0; i < len-nodes.size()-1; ++i) {
				sb.append(NO_NODE_SYM).append(SISTER_DEL);
			}
			if(nodes.isEmpty()) {
				return sb.append(NO_NODE_SYM).toString();
			} else {
				sb.append(NO_NODE_SYM).append(SISTER_DEL);
			}
		}
		// at this point, nodes is a non-empty list
		for(Iterator<Node> nodesIterator = nodes.iterator(); nodesIterator.hasNext();) {
			Node node = nodesIterator.next();
			sb.append(node.getIdentifier());
			if(nodesIterator.hasNext()) {
				sb.append(SISTER_DEL);
			}
		}
		 return sb.toString();
	}
	
	/**
	 * @param parent
	 * @return 
	 */
	static String parentPointerExtractor(Node parent) {
		if(parent.isArtificial()) { // if node is artificial, it has a parent delimiter for reconstruction purposes
			return parent.getIdentifier().substring(parent.getIdentifier().indexOf(PARENT_DEL));
		} else {
			return PARENT_DEL + parent.getIdentifier();
		}
	}
}
