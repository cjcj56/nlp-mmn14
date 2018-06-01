package parse;

public class ParseRunnable extends Parse implements Runnable {

	protected String[] args;
	
	public ParseRunnable(String[] args, int h, int infrequentWordThresh) {
		this.multithreaded = true;
		this.h = h;
		this.numOfThreads = numOfThreads;
		this.args = args;
	}
	
	@Override
	public void run() {
		args[2] += "_" + h;
		main(args);	
	}

}
