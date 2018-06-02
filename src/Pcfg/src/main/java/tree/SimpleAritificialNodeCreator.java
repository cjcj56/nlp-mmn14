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
		parent.addDaughter(artificialNode); // invokes artificialNode.setParent(parent) as well 
		artificialNode.setDaughters(nodes);
		return artificialNode;
	}

}
