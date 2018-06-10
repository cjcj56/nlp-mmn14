package train;

import static common.Consts.UNK;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import grammar.Grammar;
import grammar.Rule;
import tree.ArtificialNodeCreator;
import tree.Node;
import tree.SimpleAritificialNodeCreator;
import tree.Terminal;
import tree.Tree;
import treebank.Treebank;
import utils.CountMap;


/**
 * @author Binyamin Kisch
 * @author Aviad Pinis
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
        
        Grammar grammar = super.train(treebank);

        for (Rule rule : grammar.getLexicalRules()) {
            rule.setMinusLogProb(calculateRuleProbs(grammar, rule));
        }
        for (Rule rule : grammar.getSyntacticRules()) {
            rule.setMinusLogProb(calculateRuleProbs(grammar, rule));
        }
        
        return grammar;
    }

	public void smoothInfrequentWords(Treebank treebank, int infrequentWordThresh) {
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
        int counter = 0;
        for (Tree tree : treebank.getAnalyses()) {
            for (Terminal terminal : tree.getTerminals()) {
                if (infrequentWords.contains(terminal.getIdentifier())) {
                    terminal.setIdentifier(UNK);
                    ++counter;
                }
            }
        }
        LOGGER.finer(String.format("Number of infrequent words changed to UNK: %d", counter));
    }

    public double calculateRuleProbs(Grammar grammar, Rule rule) {
    	int ruleCount = grammar.getRuleCounts().get(rule);
    	double lhsCount = grammar.getNonTerminalSymbolsCounts().get(rule.getLHS().getSymbols().get(0));
        return -(Math.log(ruleCount / lhsCount));
    }
    
    public void toCnf(Treebank treebank) {
    	toCnf(treebank, 0);
    }
    
    public void toCnf(Treebank treebank, int h) {
    	toCnf(treebank, new SimpleAritificialNodeCreator(h));
    }
    
    public void toCnf(Treebank treebank, ArtificialNodeCreator artificialNodeCreator) {
    	treebank.toCnf(artificialNodeCreator);
    }
    
    public void deCnf(Treebank treebank) {
    	treebank.deCnf();
    }
    
    

}
