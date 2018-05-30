package decode;

import java.util.List;
import java.util.Map;
import java.util.Set;

import grammar.Grammar;
import grammar.Rule;
import tree.Tree;

public class DecodeRunnable extends Decode implements Runnable {

	public Set<Rule> m_setGrammarRules;
	public Map<String, Set<Rule>> m_mapLexicalRules;
	public List<Tree> input;
	public List<Tree> output;
	
	
	public DecodeRunnable(Grammar grammar, List<Tree> input, List<Tree> output) {
		this.m_setGrammarRules = grammar.getSyntacticRules();
		this.m_mapLexicalRules = grammar.getLexicalEntries();
		this.input = input;
		this.output = output;
	}
	
	@Override
	public void run() {
		for(Tree tree : input) {
			List<String> sentence = tree.getYield();
			output.add(decode(sentence));
		}
	}
	
}
