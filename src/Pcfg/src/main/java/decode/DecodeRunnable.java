package decode;

import java.util.List;
import tree.Tree;

public class DecodeRunnable extends Decode implements Runnable {

	public List<Tree> input;
	public List<Tree> output;
	
	public DecodeRunnable(List<Tree> input, List<Tree> output) {
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
