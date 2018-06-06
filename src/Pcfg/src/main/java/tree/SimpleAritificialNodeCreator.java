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
		String sistersIds = h == -1 ? nodeIdJoiner(nodes) : nodeIdJoiner(h < nodes.size() ? nodes.subList(0, h) : nodes, h);
		Node artificialNode = new Node(parentPointerExtractor(parent) + sistersIds);
		artificialNode.setDaughters(nodes);
		return artificialNode;
	}

}
