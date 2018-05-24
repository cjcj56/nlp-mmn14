package train;

import grammar.Grammar;
import grammar.Rule;

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
        Grammar grammar = super.train(myTreebank);

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
        if (node.getDaughters().size() > 2) {
            Node newNode = (Node) node.clone();
            newNode.removeDaughter(newNode.getDaughters().get(0));
            Node secondNode = updateNode(newNode);

            secondNode.setIdentifier(secondNode.getIdentifier()+"@//");
            Node firstNode = node.getDaughters().get(0);
            node.cleanDaughters();
            node.addDaughter(firstNode);
            node.addDaughter(secondNode);
        }
        else{
            for (Node childNode : node.getDaughters()){
                childNode = updateNode(childNode);
            }
        }
        return node;
    }

}
