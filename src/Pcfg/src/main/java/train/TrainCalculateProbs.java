package train;

import static common.Consts.INFREQUENT_WORD_THRESH;
import static common.Consts.UNK;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import grammar.Grammar;
import grammar.Rule;

import tree.Node;
import tree.Terminal;
import tree.Tree;
import treebank.Treebank;
import utils.CountMap;

import java.util.ArrayList;
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
            rule.setMinusLogProb(calculateProbs(grammar.getRuleCounts().get(rule),
                    grammar.getNonTerminalSymbolsCounts().get(rule.getLHS().getSymbols().get(0))));
        }
        for (Rule rule : grammar.getSyntacticRules()) {
            rule.setMinusLogProb(calculateProbs(grammar.getRuleCounts().get(rule),
                    grammar.getNonTerminalSymbolsCounts().get(rule.getLHS().getSymbols().get(0))));
        }
        return grammar;
    }

    private void preProcess(Treebank treebank) {
		smoothInfrequentWords(treebank);
		
	}

	private void smoothInfrequentWords(Treebank treebank) {
		CountMap<String> wordsCount = new CountMap<>();
		for(Tree tree : treebank.getAnalyses()) {
			for(Terminal terminal : tree.getTerminals()) {
				wordsCount.increment(terminal.getIdentifier());
			}
		}
		
		Set<String> infrequentWords = new HashSet<>();
		for(Map.Entry<String, Integer> wordCount : wordsCount.entrySet()) {
			if(wordCount.getValue() <= INFREQUENT_WORD_THRESH) {
				infrequentWords.add(wordCount.getKey());
			}
		}
		
		for(Tree tree : treebank.getAnalyses()) {
			for(Terminal terminal : tree.getTerminals()) {
				if(infrequentWords.contains(terminal.getIdentifier())) {
					terminal.setIdentifier(UNK);
				}
			}
		}
	}

	public double calculateProbs(int countRules, double countTerminal) {
        return (Math.log(countRules / countTerminal));
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

    private Node updateNode(Node node) {
        if (node.getDaughters().size() > 2) {
            Node newNode = (Node) node.clone();
            newNode.cloneBrothers(node);

            newNode.removeDaughter(newNode.getDaughters().get(0));
            Node firstNode = node.getDaughters().get(0);
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

}
