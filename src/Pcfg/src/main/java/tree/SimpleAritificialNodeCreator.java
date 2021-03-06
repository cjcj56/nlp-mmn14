package tree;

import static tree.ArtificialNodeCreator.nodeIdJoiner;
import static tree.ArtificialNodeCreator.parentPointerExtractor;

import java.util.List;

public class SimpleAritificialNodeCreator implements ArtificialNodeCreator {
	
	protected int h;
	
	public SimpleAritificialNodeCreator(int h) {
		this.h = h;
	}
	
	@Override
	public Node createArtificialNode(List<Node> nodes, Node parent) {
		int size = nodes.size();
		String sistersIds = h == -1 ? nodeIdJoiner(nodes) : nodeIdJoiner(h < size ? nodes.subList(size-h, size) : nodes, h);
		Node artificialNode = new Node(parentPointerExtractor(parent) + sistersIds);
		return artificialNode;
	}

}
