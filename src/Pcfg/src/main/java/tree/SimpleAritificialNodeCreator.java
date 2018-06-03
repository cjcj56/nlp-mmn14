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
		Node artificialNode = new Node(nodeIdJoiner(h <= nodes.size() ? nodes.subList(0, h) : nodes, h) + parentPointerExtractor(parent));
		parent.addDaughter(artificialNode); // invokes artificialNode.setParent(parent) as well 
		artificialNode.setDaughters(nodes);
		return artificialNode;
	}

}
