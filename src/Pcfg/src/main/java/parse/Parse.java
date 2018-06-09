package parse;


import static common.Consts.INFREQUENT_WORD_THRESH;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import bracketimport.TreebankReader;
import decode.Decode;
import decode.DecodeRunnable;
import grammar.Grammar;
import grammar.Rule;
import train.ParentEncoding;
import train.TrainCalculateProbs;
import tree.Tree;
import treebank.Treebank;
import utils.LineWriter;
import utils.ListPartitioner;

public class Parse {

	/**
	 *
	 * @author Reut Tsarfaty
	 * @date 27 April 2013
	 * 
	 * @param train-set 
	 * @param test-set 
	 * @param exp-name
	 * 
	 */
	
	private static Logger LOGGER;
	public static final String LOG_CONF = "./src/Pcfg/conf/logging.properties";
//	public static final String LOG_CONF = "./conf/logging.properties";
	
	public static int numOfThreads = 20;
	public static int h = 2;
	public static boolean parentEncoding = true;
	public static boolean multithreaded = true;
	public static boolean trainOnGold = false; // for debugging, runs much faster

	
	public static void main(String[] args) {
		
		//**************************//
		//*      NLP@IDC PA2       *//
		//*   Statistical Parsing  *//
		//*     Point-of-Entry     *//
		//**************************//
		
		if (args.length < 3)
		{
			System.out.println("Usage: Parse <goldset> <trainset> <experiment-identifier-string>");
			return;
		}
		
		// 0. initialize
		initLogging(LOG_CONF);
		LOGGER = Logger.getLogger(Parse.class.getName());
		args[1] = trainOnGold ? args[0] : args[1];
		
		// 1. read input
		LOGGER.fine("args: " + Arrays.toString(args));
		LOGGER.fine(String.format("additional run args: numOfThreads=%d, h=%d, parentEncoding=%b, multithreaded=%b", numOfThreads, h, parentEncoding, multithreaded));
		LOGGER.info("reading gold treebank");
		Treebank goldTreebank = TreebankReader.getInstance().read(true, args[0]);
		LOGGER.info("finished reading gold treebank");
		LOGGER.info("reading train treebank");
		Treebank trainTreebank = TreebankReader.getInstance().read(true, args[1]);
		LOGGER.info("finished reading train treebank");

		// 2. transform trees
		LOGGER.info("preprocessing input");
		LOGGER.fine("smoothing infrequent words");
		TrainCalculateProbs.getInstance().smoothInfrequentWords(trainTreebank, INFREQUENT_WORD_THRESH);
//		trainTreebank = TrainCalculateProbs.getInstance().updateTreebankToCNF(trainTreebank, h);
		if(parentEncoding) {
			LOGGER.fine("adding parent encoding");
			trainTreebank = ParentEncoding.getInstance().smooting(trainTreebank);
			Parse.writeParseTrees("TrainWithSmooting", trainTreebank.getAnalyses());
		}
		TrainCalculateProbs.getInstance().toCnf(trainTreebank, h);
		writeParseTrees("TrainBinarizing_h" + h + "_pe" + (parentEncoding ? 1 : 0), trainTreebank.getAnalyses());

		// 3. train
		LOGGER.info("training");
		Grammar grammar = TrainCalculateProbs.getInstance().train(trainTreebank);
		
		// 4. decode
		LOGGER.info("decoding");
		Decode.getInstance(grammar); // populate Decode collections
		numOfThreads = multithreaded ? numOfThreads : 1;
		List<List<Integer>> partitionedRanges = ListPartitioner.partition(goldTreebank.size(), numOfThreads);
		List<Tree> trees = goldTreebank.getAnalyses();
		List<List<Tree>> threadsOutputs = new ArrayList<>(partitionedRanges.size());
		List<Thread> threads = new ArrayList<>(partitionedRanges.size());
		for(List<Integer> range : partitionedRanges) {
			int start = range.get(0);
			int end = range.get(1);
			String threadRange = String.format("(%d,%d)", start, end-1);
			List<Tree> threadOutput = new ArrayList<>();
			Thread thread = new Thread(new DecodeRunnable(trees.subList(start, end), threadOutput), threadRange);
			threads.add(thread);
			threadsOutputs.add(threadOutput);
			thread.start();
			LOGGER.finer(String.format("decoding partition %s", threadRange));
		}
		for(Thread thread : threads) {
			try {
				thread.join();
				LOGGER.finer(String.format("thread of range %s ended", thread.getName()));
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		List<Tree> parsedTrees = new ArrayList<>(trees.size());
		for(List<Tree> threadOutput : threadsOutputs) {
			parsedTrees.addAll(threadOutput);
		}
		Treebank parsedTreebank = new Treebank(parsedTrees);

		//4.5. unSmooting parent
		if(parentEncoding) {
			parsedTreebank = ParentEncoding.getInstance().unSmooting(parsedTreebank);
		}

		// 5. de-transform trees
		writeParseTrees("parseBinarizing_h" + h + "_pe" + (parentEncoding ? 1 : 0), parsedTreebank.getAnalyses());
//		parsedTreebank = TrainCalculateProbs.getInstance().deTransformTreebank(parsedTreebank);
		TrainCalculateProbs.getInstance().deCnf(parsedTreebank);
		writeParseTrees("parseDeBinarizing_h" + h + "_pe" + (parentEncoding ? 1 : 0), parsedTreebank.getAnalyses());

		// 6. write output
		writeOutput(args[2]+"_h"+h + "_pe" + (parentEncoding ? 1 : 0), grammar, parsedTreebank.getAnalyses());
	}
	
	
	private static void initLogging(String logConf) {
		try {
            LogManager.getLogManager().readConfiguration(new FileInputStream(logConf));
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
	}


	/**
	 * Writes output to files:
	 * = the trees are written into a .parsed file
	 * = the grammar rules are written into a .gram file
	 * = the lexicon entries are written into a .lex file
	 */
	private static void writeOutput(
			String sExperimentName, 
			Grammar myGrammar,
			List<Tree> myTrees) {
		
		writeParseTrees(sExperimentName, myTrees);
		writeGrammarRules(sExperimentName, myGrammar);
		writeLexicalEntries(sExperimentName, myGrammar);
	}

	/**
	 * Writes the parsed trees into a file.
	 */
	public static void writeParseTrees(String sExperimentName,
			List<Tree> myTrees) {
		LineWriter writer = new LineWriter(sExperimentName+".parsed");
		for (int i = 0; i < myTrees.size(); i++) {
			LOGGER.finest(String.format("writing tree %d", i));
			writer.writeLine(myTrees.get(i).toString());
		}
		writer.close();
	}
	
	/**
	 * Writes the grammar rules into a file.
	 */
	private static void writeGrammarRules(String sExperimentName,
			Grammar myGrammar) {
		LineWriter writer;
		writer = new LineWriter(sExperimentName+".gram");
		Set<Rule> myRules = myGrammar.getSyntacticRules();
		Iterator<Rule> myItrRules = myRules.iterator();
		while (myItrRules.hasNext()) {
			Rule r = (Rule) myItrRules.next();
			writer.writeLine(r.getMinusLogProb()+"\t"+r.getLHS()+"\t"+r.getRHS()); 
		}
		writer.close();
	}
	
	/**
	 * Writes the lexical entries into a file.
	 */
	private static void writeLexicalEntries(String sExperimentName, Grammar myGrammar) {
		LineWriter writer;
		Iterator<Rule> myItrRules;
		writer = new LineWriter(sExperimentName+".lex");
		Set<String> myEntries = myGrammar.getLexicalEntries().keySet();
		Iterator<String> myItrEntries = myEntries.iterator();
		while (myItrEntries.hasNext()) {
			String myLexEntry = myItrEntries.next();
			StringBuffer sb = new StringBuffer();
			sb.append(myLexEntry);
			sb.append("\t");
			Set<Rule> myLexRules =   myGrammar.getLexicalEntries().get(myLexEntry);
			myItrRules = myLexRules.iterator();
			while (myItrRules.hasNext()) {
				Rule r = (Rule) myItrRules.next();
				sb.append(r.getLHS().toString());
				sb.append(" ");
				sb.append(r.getMinusLogProb());
				sb.append(" ");
			}
			writer.writeLine(sb.toString());
		}
	}

}
