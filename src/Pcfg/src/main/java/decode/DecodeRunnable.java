package decode;

import java.util.List;
import java.util.Map;
import java.util.Set;

import grammar.Grammar;
import grammar.Rule;

public class DecodeRunnable extends Decode implements Runnable {

	public Set<Rule> m_setGrammarRules;
	public Map<String, Set<Rule>> m_mapLexicalRules;
	public List<String> input;
	
	public DecodeRunnable(Grammar grammar, List<String> input, int start, int end) {
		this.m_setGrammarRules = grammar.getSyntacticRules();
		this.m_mapLexicalRules = grammar.getLexicalEntries();
		this.input = input.subList(start, end);
	}
	
	public DecodeRunnable(Grammar grammar, List<String> input) {
		this.m_setGrammarRules = grammar.getSyntacticRules();
		this.m_mapLexicalRules = grammar.getLexicalEntries();
		this.input = input;
	}
	
	@Override
	public void run() {
		decode(input);
	}
	
}
