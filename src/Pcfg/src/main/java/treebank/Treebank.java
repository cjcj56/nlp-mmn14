package treebank;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import tree.ArtificialNodeCreator;
import tree.SimpleAritificialNodeCreator;
import tree.Tree;


/**
 * 
 * @author rtsarfat
 *
 * CLASS: Treebank
 * 
 * Definition: A collection of hand-annotated corpus of NL utterances with syntactic parse trees
 * Role: Know the kind of utterance/analysis to store
 * Responsibility: Handle tree-transforms over the treebank, cross-compare treebanks size/trees
 */
public class Treebank {

	private static final Logger LOGGER = Logger.getLogger(Treebank.class.getName());
	
	protected List<Tree> m_lstAnalyses = new ArrayList<Tree>();
	
	public Treebank() {
		super();
	}
	
	public Treebank(List<Tree> trees) {
		super();
		m_lstAnalyses = trees;
	}
	
	

	public void add(Tree pt){
		getAnalyses().add(pt);		
	}
	
	public int size()
	{
		return m_lstAnalyses.size();
	}

	public List<Tree> getAnalyses() {
		return m_lstAnalyses;
	}
	
	
	public void toCnf() {
		toCnf(0);
	}
	
	public void toCnf(int h) {
		toCnf(new SimpleAritificialNodeCreator(h));
	}
	
	public void toCnf(ArtificialNodeCreator artificialNodeCreator) {
		for(Tree tree : getAnalyses()) {
			tree.toCnf(artificialNodeCreator);
		}
	}
	
	public void deCnf() {
		for(Tree tree : getAnalyses()) {
			tree.deCnf();
		}
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < getAnalyses().size(); i++) 
		{
			sb.append((getAnalyses().get(i)).toString());
			if (i+1< getAnalyses().size()) sb.append("\n");
		}
		
		return sb.toString();
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
			Treebank otherTb = (Treebank) obj;
			if(getAnalyses().size() != otherTb.getAnalyses().size()) {
				return false;
			} else {
				List<Tree> tb1 = getAnalyses();
				List<Tree> tb2 = otherTb.getAnalyses();
				for(int i = 0; i < tb1.size(); ++i) {
					Tree tree1 = tb1.get(i);
					Tree tree2 = tb2.get(i);
					if(!tree1.equals(tree2)) {
						LOGGER.fine("tree #" + i + "is diffrent!");
						return false;
					}
				}
				return true;
			}
		}
	}
	
	
}
