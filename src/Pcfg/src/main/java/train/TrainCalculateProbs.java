package train;

import static common.Consts.INFREQUENT_WORD_THRESH;
import static common.Consts.UNK;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import grammar.Grammar;
import grammar.Rule;
import parse.Parse;
import tree.Node;
import tree.Terminal;
import tree.Tree;
import treebank.Treebank;
import utils.CountMap;

import java.util.List;

/**
 * @author Binyamin Kisch
 * <p>
 * CLASS: Train
 * <p>
 * Definition: a learning component Role: reads off a grammar from a
 * treebank Responsibility: keeps track of rule counts
 */

public class TrainCalculateProbs extends Train {

	private static final Logger LOGGER = Logger.getLogger(TrainCalculateProbs.class.getName());
	
    public static TrainCalculateProbs m_singTrainerCalc = null;

    public static TrainCalculateProbs getInstance() {
        if (m_singTrainerCalc == null) {
            m_singTrainerCalc = new TrainCalculateProbs();
        }
        return m_singTrainerCalc;
    }

    public static void main(String[] args) {

    }

    public Grammar train(Treebank treebank) {
        preProcess(treebank);
        
        Grammar grammar = super.train(treebank);

        for (Rule rule : grammar.getLexicalRules()) {
            rule.setMinusLogProb(calculateRuleProbs(grammar, rule));
        }
        for (Rule rule : grammar.getSyntacticRules()) {
            rule.setMinusLogProb(calculateRuleProbs(grammar, rule));
        }
        
        return grammar;
    }

    private void preProcess(Treebank treebank) {
    	LOGGER.fine("preprocessing input");
        smoothInfrequentWords(treebank);
//        treebank = ParentEncoding.getInstance().smooting(treebank);
		Parse.writeParseTrees("TrainBinarizingWithSmooting", treebank.getAnalyses());

    }
    
    private void smoothInfrequentWords(Treebank treebank) {
    	smoothInfrequentWords(treebank, INFREQUENT_WORD_THRESH);
    }
    
	private void smoothInfrequentWords(Treebank treebank, int infrequentWordThresh) {
		LOGGER.fine("smoothing infrequent words");
		
		LOGGER.finer("counting words in input");
        CountMap<String> wordsCount = new CountMap<>();
        for (Tree tree : treebank.getAnalyses()) {
            for (Terminal terminal : tree.getTerminals()) {
                wordsCount.increment(terminal.getIdentifier());
            }
        }

        LOGGER.finer("collecting infrequent words from input");
        Set<String> infrequentWords = new HashSet<>();
        for (Map.Entry<String, Integer> wordCount : wordsCount.entrySet()) {
            if (wordCount.getValue() <= infrequentWordThresh) {
                infrequentWords.add(wordCount.getKey());
            }
        }

        LOGGER.finer("transforming infrequent words of input to " + UNK);
        for (Tree tree : treebank.getAnalyses()) {
            for (Terminal terminal : tree.getTerminals()) {
                if (infrequentWords.contains(terminal.getIdentifier())) {
                    terminal.setIdentifier(UNK);
                }
            }
        }
    }

    public double calculateRuleProbs(Grammar grammar, Rule rule) {
    	int ruleCount = grammar.getRuleCounts().get(rule);
    	double lhsCount = grammar.getNonTerminalSymbolsCounts().get(rule.getLHS().getSymbols().get(0));
        return -(Math.log(ruleCount / lhsCount));
    }

    public Treebank updateTreebankToCNF(Treebank myTreebank, int h) {
        for (Tree tree : myTreebank.getAnalyses()) {
            updateNode(tree.getRoot());
            for (Node node : tree.getNodes()) {
                editIdentifier(node, h);
            }
        }
        return myTreebank;
    }

    private Node updateNode(Node node) {
        if (node.getDaughters().size() > 2) {
        	// CNF transformation of the node with more than two daughters, 
        	// includes transferring redundant daughters (except the left node)-
        	//  to the next level of the tree
            Node newNode = (Node) node.clone();
            newNode.cloneBrothers(node);

            Node firstNode = updateNode(node.getDaughters().get(0));
            newNode.removeDaughter(newNode.getDaughters().get(0));
            newNode.addBrother(firstNode);
            Node secondNode = updateNode(newNode);

            node.cleanDaughters();
            node.addDaughter(firstNode);
            node.addDaughter(secondNode);

        } else {
            for (Node childNode : node.getDaughters()) {
                updateNode(childNode);
            }
        }
        return node;
    }

    private void editIdentifier(Node node, int h) {
        if (node.isBrother()) {
            String newIdentifiers = "";
            if (h == -1) {
                for (Node borther : node.getBrothers()) {
                    newIdentifiers += "//" + borther.getIdentifier();
                }
            } else {
                int i = node.getBrothers().size() - h;
                if (i < 0) {
                    i = 0;
                }
                for (; h > 0 && i < node.getBrothers().size(); i++, h--) {
                    newIdentifiers += "//" + node.getBrothers().get(i).getIdentifier();
                }
            }
            node.setIdentifier(node.getIdentifier() + "@" + newIdentifiers);
        }
    }

    public Treebank deTransformTreebank(Treebank treebank) {
        for (Tree tree : treebank.getAnalyses()) {
            deTransform(tree.getRoot());
        }
        return treebank;
    }

    private boolean deTransform(Node node) {
        if (node.isBrother()) {
            Node parentNode = node.getParent();
            parentNode.removeDaughter(parentNode.getDaughters().get(parentNode.getDaughters().size() - 1));
            for (int i = 0; i < node.getDaughters().size(); i++) {
                parentNode.addDaughter(node.getDaughters().get(i));
            }
            return true;
        }

        if (!node.isLeaf()) {
            for (Node nd : node.getDaughters()) {
                boolean check = deTransform(nd);
                if (check) {
                    deTransform(node);
                    break;
                }
            }
        }
        return false;
    }

}
