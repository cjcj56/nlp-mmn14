package decode;

import static common.Consts.UNK;
import static common.Consts.DEFAULT_SYM;
import static common.Consts.PARENT_ENCODING;
import static common.Consts.MAX_ITERATIONS_FOR_UNIT_RULES;
import static java.lang.Double.NEGATIVE_INFINITY;

import grammar.Grammar;
import grammar.Rule;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tree.Node;
import tree.Terminal;
import tree.Tree;

public class Decode {

	public static Set<String> m_setStartSymbols = null;
	public static Set<Rule> m_setGrammarRules = null;
	public static Map<String, Set<Rule>> m_mapLexicalRules = null;
	
	public static Set<Rule> m_setUnaryGrammarRules = null;
	public static Set<Rule> m_setBinaryGrammarRules = null;

	/**
	 * Implementation of a singleton pattern Avoids redundant instances in memory
	 */
	public static Decode m_singDecoder = null;

	public static Decode getInstance(Grammar g) {
		if (m_singDecoder == null) {
			m_singDecoder = new Decode();
			m_setStartSymbols = g.getStartSymbols();
			m_setGrammarRules = g.getSyntacticRules();
			m_mapLexicalRules = g.getLexicalEntries();
			
			m_setUnaryGrammarRules = new HashSet<>();
			m_setBinaryGrammarRules = new HashSet<>();
			for(Rule rule : m_setGrammarRules) {
				if(rule.isUnitRule()) {
					m_setUnaryGrammarRules.add(rule);
				} else {
					m_setBinaryGrammarRules.add(rule);
				}
			}
		}
		return m_singDecoder;
	}

	public Tree decode(List<String> input) {

		// Done: Baseline Decoder
		// Returns a flat tree with NN labels on all leaves

		Tree t = new Tree(new Node("TOP"));
		Iterator<String> theInput = input.iterator();
		while (theInput.hasNext()) {
			String theWord = (String) theInput.next();
			Node preTerminal = new Node("NN");
			Terminal terminal = new Terminal(theWord);
			preTerminal.addDaughter(terminal);
			t.getRoot().addDaughter(preTerminal);
		}

		// TODO: CYK decoder
		// if CYK fails,
		// use the baseline outcome

		CykMatrix cyk = new CykMatrix(input.size());
		for (int j = 1; j <= cyk.n(); ++j) {
			String word = input.get(j-1);
			Set<Rule> wordLexicalRules = m_mapLexicalRules.get(word);
			if (wordLexicalRules == null) {
				wordLexicalRules = m_mapLexicalRules.get(UNK);
				if(wordLexicalRules == null) { // ParentEncoding applied
					// assuming that there are unknown nouns (i.e. "UNK#NN#")
					wordLexicalRules = m_mapLexicalRules.get(new StringBuilder(UNK).append(PARENT_ENCODING)
							.append(DEFAULT_SYM).append(PARENT_ENCODING).toString());
				}
			}
			for (Rule rule : wordLexicalRules) {
				cyk.set(j-1, j, rule.getLHS().getSymbols().get(0), rule.getMinusLogProb());
				cyk.setBackTrace(j-1, j, rule.getLHS().getSymbols().get(0), -1, input.get(j-1), null);
			}

			for (int i = j - 2; i >= 0; --i) {
				for (int k = i+1; k <= j-1; ++k) {
					if ((cyk.get(i, k) != null) && (cyk.get(k, j) != null)) {
						boolean foundAppropriateRule = false; 
						for (Rule rule : m_setBinaryGrammarRules) {
							Double currProb = cyk.get(i, j, rule.getLHS().getSymbols().get(0));
							Double computedProb;
							Double leftRhsSymbolProb = cyk.get(i, k, rule.getRHS().getSymbols().get(0));
							Double rightRhsSymbolProb = cyk.get(k, j, rule.getRHS().getSymbols().get(1));
							computedProb = rule.getMinusLogProb() + leftRhsSymbolProb + rightRhsSymbolProb;
							if ((currProb < computedProb) && (computedProb > NEGATIVE_INFINITY)) {
								// Rule's form: a --> b c (for binary) and a --> b (for unit)
								String a = rule.getLHS().getSymbols().get(0); // lhsSymbol
								String b = rule.getRHS().getSymbols().get(0); // rhsLeftSymbol
								String c = rule.getRHS().getSymbols().get(1); // rhsRightSymbol
								cyk.set(i, j, a, computedProb);
								cyk.setBackTrace(i, j, a, k, b, c);
								foundAppropriateRule = true;
							}
						}
						
						int iterCount = 0;
						while((foundAppropriateRule) && (iterCount < MAX_ITERATIONS_FOR_UNIT_RULES)) { // if no rule found, there is no point going over unary rules again
							++iterCount; 
							foundAppropriateRule = false;
							for(Rule rule : m_setUnaryGrammarRules) {
								Double currProb = cyk.get(i, j, rule.getLHS().getSymbols().get(0));
								Double computedProb = rule.getMinusLogProb() + currProb;
								if ((currProb < computedProb) && (computedProb > NEGATIVE_INFINITY)) {
									// Rule's form: a --> b c (for binary) and a --> b (for unit)
									String a = rule.getLHS().getSymbols().get(0); // lhsSymbol
									String b = rule.getRHS().getSymbols().get(0); // rhsLeftSymbol
									String c = null; // rhsRightSymbol
									cyk.set(i, j, a, computedProb);
									cyk.setBackTrace(i, j, a, k, b, c);
									foundAppropriateRule = true;
								}
							}
						}
					}
				}
			}
		}
		
		Tree cykTree = cyk.buildTree(m_setStartSymbols);
		return cykTree != null ? cykTree : t;
	}

}
