package train;

import grammar.Event;
import grammar.Grammar;
import grammar.Rule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import tree.Node;
import tree.Tree;
import treebank.Treebank;

/**
 * 
 * @author Binyamin Kisch
 * 
 *         CLASS: Train
 * 
 *         Definition: a learning component Role: reads off a grammar from a
 *         treebank Responsibility: keeps track of rule counts
 * 
 */

public class TrainCalculateProbs extends Train {
	
	public Grammar train(Treebank myTreebank) {
		Grammar grammar = super.train(myTreebank);
		
		Set<Rule> allRules = new HashSet<>(grammar.getLexicalRules());
		allRules.addAll(grammar.getSyntacticRules());
		for(Rule rule : allRules) {
			
		}
		
		return grammar;
	}
	
}
