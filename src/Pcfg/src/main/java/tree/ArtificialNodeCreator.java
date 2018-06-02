package tree;

import static common.Consts.*;

import java.util.Iterator;
import java.util.List;

public interface ArtificialNodeCreator {

	Node createArtificialNode(List<Node> nodes, Node parent);
	
	static String nodeIdJoiner(List<Node> nodes, Node parent) {
		StringBuilder sb = new StringBuilder();
		if(nodes.isEmpty()) {
			return addParentDel ? sb.append(NO_NODE_SYM).append(PARENT_DEL).append(parent).toString();
		}
		Iterator<Node> nodesIterator = nodes.iterator();
		for(Node node = nodesIterator.next(); nodesIterator.hasNext();) {
			sb.append(node.getIdentifier());
			if(nodesIterator.hasNext()) {
				sb.append(SISTER_DEL);
			}
		}
		return sb.append(PARENT_DEL).append(parent).toString();
	}
}
