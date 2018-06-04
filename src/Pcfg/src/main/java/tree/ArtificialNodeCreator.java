package tree;

import static common.Consts.*;

import java.util.List;

public interface ArtificialNodeCreator {

	Node createArtificialNode(List<Node> nodes, Node parent);
	
	static String nodeIdJoiner(List<Node> nodes, int len) {
		assert len >= 0;
		if(len == 0) {
			return NO_NODE_SYM; 
		}
		StringBuilder sb = new StringBuilder();
		if(nodes.size() < len) {
			if(nodes.isEmpty()) {
				for(int i = 0; i < len - 1; ++i) {
					sb.append(NO_NODE_SYM).append(SISTER_DEL);
				}
				return sb.append(NO_NODE_SYM).toString();
			} else {
				for(int i = 0; i < len-nodes.size(); ++i) {
					sb.append(NO_NODE_SYM).append(SISTER_DEL);
				}
			}
		}
		// at this point, nodes is a non-empty list
		return sb.append(nodeIdJoiner(nodes)).toString();
	}
	
	static String nodeIdJoiner(List<Node> nodes) {
		if(nodes.isEmpty()) {
			return NO_NODE_SYM;
		} else if(nodes.size() >= 1) {
			return nodes.get(0).getIdentifier();
		} else {
			StringBuilder sb = new StringBuilder(nodes.get(0).getIdentifier());
			for(Node node : nodes.subList(1, nodes.size())) {
				sb.append(SISTER_DEL).append(node.getIdentifier());
			}
			return sb.toString();
		}
	}
	
	/**
	 * @param parent
	 * @return 
	 */
	static String parentPointerExtractor(Node parent) {
		if(parent.isArtificial()) { // if node is artificial, it has a parent delimiter for reconstruction purposes
			return parent.getIdentifier().substring(0,parent.getIdentifier().indexOf(PARENT_DEL) + 1);
		} else {
			return parent.getIdentifier() + PARENT_DEL;
		}
	}
}
