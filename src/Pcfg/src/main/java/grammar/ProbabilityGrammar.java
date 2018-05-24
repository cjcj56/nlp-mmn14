package grammar;

import utils.CountMap;

public class ProbabilityGrammar extends Grammar {

	protected CountMap<String> m_cmNonTerminalSymbolsCounts = new CountMap<String>();

	public ProbabilityGrammar() {
		super();
	}

	public ProbabilityGrammar(Grammar grammar) {
		this.m_setStartSymbols = grammar.m_setStartSymbols;
		this.m_setTerminalSymbols = grammar.m_setTerminalSymbols;
		this.m_setNonTerminalSymbols = grammar.m_setNonTerminalSymbols;
        
		this.m_setSyntacticRules = grammar.m_setSyntacticRules;
		this.m_setLexicalRules = grammar.m_setLexicalRules;
		this.m_cmRuleCounts = grammar.m_cmRuleCounts;
		this.m_lexLexicalEntries = grammar.m_lexLexicalEntries;
		
	}

	public CountMap<String> getNonTerminalSymbolsCounts() {
		return m_cmNonTerminalSymbolsCounts;
	}

}
