package tree;

import static tree.ArtificialNodeCreator.nodeIdJoiner;
import java.util.List;

public class SimpleAritificialNodeCreator implements ArtificialNodeCreator {
	
	private int h; 
	
	public SimpleAritificialNodeCreator(int h) {
		this.h = h;
	}
	
	@Override
	public Node createArtificialNode(List<Node> nodes, Node parent) {
		Node artificialNode = new Node(nodeIdJoiner(nodes.subList(0, h), parent));
		artificialNode.setParent(parent);
		artificialNode.setRealParent(null);
		artificialNode.setDaughters(nodes);
		artificialNode.setAritificial(true);
		return artificialNode;
	}

}
