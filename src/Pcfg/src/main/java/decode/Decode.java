package decode;

import static common.Consts.DEFAULT_SYM;
import static common.Consts.UNK;
import static common.Consts.MAX_ITERATIONS_FOR_UNIT_RULES;
import static java.lang.Double.POSITIVE_INFINITY;

import grammar.Grammar;
import grammar.Rule;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.Triplet;
import tree.Node;
import tree.Terminal;
import tree.Tree;

public class Decode {

	private static final Logger LOGGER = Logger.getLogger(Decode.class.getName());

	public static boolean parentEncoding = true;
	public static Set<String> m_setStartSymbols = null;
	public static Set<Rule> m_setGrammarRules = null;
	public static Map<String, Set<Rule>> m_mapLexicalRules = null;

	public static Map<String, Set<Rule>> m_mapUnaryGrammarIndex = null; // IndexedByRhs
	public static Map<String, Set<Rule>> m_mapBinaryGrammarIndex = null; // IndexedByRhsLeftSymbol
	public static Double defaultUnkLogProb = null;

	/**
	 * Implementation of a singleton pattern Avoids redundant instances in memory
	 */
	public static Decode m_singDecoder = null;

	public static Decode getInstance(Grammar g) {
		if (m_singDecoder == null) {
			LOGGER.info("creating decoder instance");
			m_singDecoder = new Decode();
			m_setStartSymbols = g.getStartSymbols();
			m_setGrammarRules = g.getSyntacticRules();
			m_mapLexicalRules = g.getLexicalEntries();

			m_mapUnaryGrammarIndex = new HashMap<>();
			m_mapBinaryGrammarIndex = new HashMap<>();

			Map<String, Set<Rule>> index;
			for (Rule rule : m_setGrammarRules) {
				String rhsLeftSymbol = rule.getRHS().getSymbols().get(0);
				index = rule.isUnitRule() ? m_mapUnaryGrammarIndex : m_mapBinaryGrammarIndex;
				if (!index.containsKey(rhsLeftSymbol)) {
					index.put(rhsLeftSymbol, new HashSet<Rule>());
				}
				index.get(rhsLeftSymbol).add(rule);
			}

			for(Rule rule : m_mapLexicalRules.get(UNK)) { // TODO : look for default symbol or for maximal / minimal probability? 
				if(DEFAULT_SYM.equals(rule.getLHS().getSymbols().get(0))) {
					defaultUnkLogProb = rule.getMinusLogProb();
					break;
				}
			}
			assert defaultUnkLogProb != null;
			
			LOGGER.info("decoder instance created");
		}
		return m_singDecoder;
	}

	public Tree decode(List<String> input) {

		// Done: Baseline Decoder
		// Returns a flat tree with NN labels on all leaves

		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("dummy decoder for sentence: " + input);
		}
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

		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("CYK decoder for sentence: " + input);
		}
		CykMatrix cyk = new CykMatrix(input.size());
		for (int j = 1; j <= cyk.n(); ++j) {
			String word = input.get(j - 1);
			Set<Rule> wordLexicalRules = m_mapLexicalRules.get(word);
			if (wordLexicalRules == null) {
				cyk.set(j-1, j, DEFAULT_SYM, defaultUnkLogProb);
				cyk.setBackTrace(j-1, j, DEFAULT_SYM, -1, word, null);
			} else {
				// run through lexical rules for each word
				for (Rule rule : wordLexicalRules) {
					cyk.set(j - 1, j, rule.getLHS().getSymbols().get(0), rule.getMinusLogProb());
					cyk.setBackTrace(j - 1, j, rule.getLHS().getSymbols().get(0), -1, word, null);
				}
			}
			boolean foundAppropriateRule = true;
			int iterCount = 0;
			Map<String, Double> matrixCellProbs = cyk.get(j - 1, j);
			// after lexical rules, run through unary rules till convergence or max
			// iteration achieved
			while (foundAppropriateRule && iterCount < MAX_ITERATIONS_FOR_UNIT_RULES) {
				iterCount++;
				foundAppropriateRule = false;
				Map<String, Double> probChangesHolder = new HashMap<>();
				Map<Triplet<Integer, Integer, String>, Triplet<Integer, String, String>> backtraceChangesHolder = new HashMap<>();
				for (Map.Entry<String, Double> cellSymbolProb : matrixCellProbs.entrySet()) {
					for (Rule rule : m_mapUnaryGrammarIndex.getOrDefault(cellSymbolProb.getKey(),
							Collections.emptySet())) {
						Double currProb = matrixCellProbs.getOrDefault(rule.getLHS().getSymbols().get(0),
								POSITIVE_INFINITY);
						Double computedProb = rule.getMinusLogProb() + cellSymbolProb.getValue();
						if (currProb > computedProb) {
							// Rule's form: a --> b c (for binary) and a --> b (for unit)
							String a = rule.getLHS().getSymbols().get(0); // lhsSymbol
							String b = rule.getRHS().getSymbols().get(0); // rhsLeftSymbol
							String c = null; // rhsRightSymbol
							probChangesHolder.put(a, computedProb); // TODO BUG ALERT!!! if a appears in lexical and grammar rule, grammar overrides lexical and deletes words from backtrace!
							backtraceChangesHolder.put(new Triplet<>(j - 1, j, a), new Triplet<>(-2, b, c));
//							cyk.set(j - 1, j, a, computedProb);
							// cyk.setBackTrace(j - 1, j, a, -1, b, c);
							foundAppropriateRule = true;
						}
					}
				}
				matrixCellProbs.putAll(matrixCellProbs);
				cyk.putAllProbs(j-1, j, probChangesHolder);
				cyk.putAllBacktraces(backtraceChangesHolder);
			}

			for (int i = j - 2; i >= 0; --i) {
				matrixCellProbs = cyk.get(i, j);
				for (int k = i + 1; k <= j - 1; ++k) {
					Map<String, Double> matrixLeftChildCellProbs = cyk.get(i, k);
					Map<String, Double> matrixRightChildCellProbs = cyk.get(k, j);
					if ((!matrixLeftChildCellProbs.isEmpty()) && (!matrixRightChildCellProbs.isEmpty())) {

						// run through binary rules
						foundAppropriateRule = false;
						for (Map.Entry<String, Double> rhsLeftSymbolProb : matrixLeftChildCellProbs.entrySet()) {
							for (Rule rule : m_mapBinaryGrammarIndex.getOrDefault(rhsLeftSymbolProb.getKey(),
									Collections.emptySet())) {
								String rhsRightSymbol = rule.getRHS().getSymbols().get(1); 
								if(matrixRightChildCellProbs.containsKey(rhsRightSymbol)) {
									Double currProb = matrixCellProbs.getOrDefault(rule.getLHS().getSymbols().get(0), POSITIVE_INFINITY);
									Double leftRhsSymbolProb = rhsLeftSymbolProb.getValue();
									Double rightRhsSymbolProb = matrixRightChildCellProbs.getOrDefault(rule.getRHS().getSymbols().get(1), POSITIVE_INFINITY);
									Double computedProb = rule.getMinusLogProb() + leftRhsSymbolProb + rightRhsSymbolProb;
									if (currProb > computedProb) {
										// Rule's form: a --> b c (for binary) and a --> b (for unit)
										String a = rule.getLHS().getSymbols().get(0); // lhsSymbol
										String b = rule.getRHS().getSymbols().get(0); // rhsLeftSymbol
										String c = rule.getRHS().getSymbols().get(1); // rhsRightSymbol
										cyk.set(i, j, a, computedProb);
										cyk.setBackTrace(i, j, a, k, b, c);
										foundAppropriateRule = true;
									}
								}
							}
						}
					}
				}

				iterCount = 0;
				while ((foundAppropriateRule) && (iterCount < MAX_ITERATIONS_FOR_UNIT_RULES)) {
					// if no rule found, there is no point going over unary rules again
					++iterCount;
					foundAppropriateRule = false;
					Map<String, Double> probChangesHolder = new HashMap<>();
					Map<Triplet<Integer, Integer, String>, Triplet<Integer, String, String>> backtraceChangesHolder = new HashMap<>();
					for (Map.Entry<String, Double> cellSymbolProb : matrixCellProbs.entrySet()) {
						for (Rule rule : m_mapUnaryGrammarIndex.getOrDefault(cellSymbolProb.getKey(),
								Collections.emptySet())) {
							Double currProb = matrixCellProbs.getOrDefault(rule.getLHS().getSymbols().get(0),
									POSITIVE_INFINITY);
							Double computedProb = rule.getMinusLogProb() + cellSymbolProb.getValue();
							if (currProb > computedProb) {
								// Rule's form: a --> b c (for binary) and a --> b (for unit)
								String a = rule.getLHS().getSymbols().get(0); // lhsSymbol
								String b = rule.getRHS().getSymbols().get(0); // rhsLeftSymbol
								probChangesHolder.put(a, computedProb);
								backtraceChangesHolder.put(new Triplet<>(i, j, a), new Triplet<>(-2, b, null));
								// cyk.set(i, j, a, computedProb);
								// cyk.setBackTrace(i, j, a, k, b, c);
								foundAppropriateRule = true;
							}
						}
					}
					cyk.putAllProbs(i, j, probChangesHolder);
					cyk.putAllBacktraces(backtraceChangesHolder);
				}
			}
		}

		Tree cykTree = cyk.buildTree(m_setStartSymbols);
		return cykTree != null ? cykTree : t;
	}

}
