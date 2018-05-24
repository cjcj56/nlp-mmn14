package train;

import grammar.Event;
import grammar.Grammar;
import grammar.ProbabilityGrammar;
import grammar.Rule;

import java.util.*;

import tree.EnhancedNode;
import tree.Node;
import tree.Tree;
import treebank.Treebank;

/**
 * @author Binyamin Kisch
 * <p>
 * CLASS: Train
 * <p>
 * Definition: a learning component Role: reads off a grammar from a
 * treebank Responsibility: keeps track of rule counts
 */

public class TrainCalculateProbs extends Train {

    public static TrainCalculateProbs m_singTrainerCalc = null;

    public static TrainCalculateProbs getInstance() {
        if (m_singTrainerCalc == null) {
            m_singTrainerCalc = new TrainCalculateProbs();
        }
        return m_singTrainerCalc;
    }

    public static void main(String[] args) {

    }

    public Grammar train(Treebank myTreebank) {
        ProbabilityGrammar grammar = new ProbabilityGrammar(super.train(myTreebank));

        for (Rule rule : grammar.getLexicalRules()) {
            rule.setMinusLogProb(calculateProbs(grammar.getRuleCounts().get(rule),
                    grammar.getNonTerminalSymbolsCounts().get(rule.getLHS().getSymbols().get(0))));
        }
        for (Rule rule : grammar.getSyntacticRules()) {
            rule.setMinusLogProb(calculateProbs(grammar.getRuleCounts().get(rule),
                    grammar.getNonTerminalSymbolsCounts().get(rule.getLHS().getSymbols().get(0))));
        }
        return grammar;
    }

    public double calculateProbs(int countRules, double countTerminal) {
        return (Math.log(countRules / countTerminal));
    }

    public Treebank updateTreebankToCNF(Treebank myTreebank) {
        for (Tree tree : myTreebank.getAnalyses()) {
            for (Node node : tree.getNodes()) {
                node = updateNode(node);
            }
        }
        return myTreebank;
    }

    private Node updateNode(Node node) {
    	EnhancedNode eNode = null;
        if (node.getDaughters().size() > 2) {
            eNode = new EnhancedNode(node);
            EnhancedNode newNode = (EnhancedNode) eNode.clone();
            newNode.removeDaughter(newNode.getDaughters().get(0));
            Node secondNode = updateNode(newNode);

//            secondNode.setParent();
            Node firstNode = eNode.getDaughters().get(0);
            eNode.cleanDaughters();
            eNode.addDaughter(firstNode);
            eNode.addDaughter(secondNode);
        }
        return eNode != null ? eNode : node;
    }


}
