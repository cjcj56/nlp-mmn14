package decode;

import grammar.Grammar;
import grammar.Rule;

import static common.Consts.UNK;
import static java.lang.Double.NEGATIVE_INFINITY;

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

	/**
	 * Implementation of a singleton pattern
	 * Avoids redundant instances in memory
	 */
	public static Decode m_singDecoder = null;
	
	public static Decode getInstance(Grammar g) {
		if (m_singDecoder == null) {
			m_singDecoder = new Decode();
			m_setStartSymbols = g.getStartSymbols();
			m_setGrammarRules = g.getSyntacticRules();
			m_mapLexicalRules = g.getLexicalEntries();
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
			if (!m_mapLexicalRules.containsKey(word)) {
				word = UNK;
			}
			for (Rule rule : m_mapLexicalRules.get(word)) {
				cyk.set(j-1, j, rule.getLHS().getSymbols().get(0), rule.getMinusLogProb());
				cyk.setBackTrace(j-1, j, rule.getLHS().getSymbols().get(0), -1, rule.getRHS().getSymbols().get(0), null);
			}

			for (int i = j - 2; i >= 0; --i) {
				for (int k = i+1; k <= j-1; ++k) {
//					System.out.format("j=%d, i=%d, k=%d, %s", j,i,k,System.lineSeparator());
					if ((cyk.get(i, k) != null) && (cyk.get(k, j) != null)) {
						for (Rule rule : m_setGrammarRules) {
							Double currProb = cyk.get(i, j, rule.getLHS().getSymbols().get(0));
							Double computedProb;
							boolean unitRule = rule.isUnitRule(); 
							if (unitRule) {
								computedProb = rule.getMinusLogProb() + currProb;
							} else {
								Double leftRhsSymbolProb = cyk.get(i, k, rule.getRHS().getSymbols().get(0));
								Double rightRhsSymbolProb = cyk.get(k, j, rule.getRHS().getSymbols().get(1));
								computedProb = rule.getMinusLogProb() + leftRhsSymbolProb + rightRhsSymbolProb;
							}
							if ((currProb < computedProb) && (computedProb > NEGATIVE_INFINITY)) {
								// Rule's form: a --> b c (for binary) and a --> b (for unit)
								String a = rule.getLHS().getSymbols().get(0); // lhsSymbol
								String b = rule.getRHS().getSymbols().get(0); // rhsLeftSymbol
								String c = unitRule ? null : rule.getRHS().getSymbols().get(1); // rhsRightSymbol
								cyk.set(i, j, a, computedProb);
								cyk.setBackTrace(i, j, a, k, b, c);
							}
						}
					}
				}
			}
		}
		
//		System.exit(0);

		Tree cykTree = cyk.buildTree(m_setStartSymbols);
		return cykTree != null ? cykTree : t;
	}
	
}
