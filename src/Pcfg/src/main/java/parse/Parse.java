package parse;

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
import grammar.Grammar;
import grammar.Rule;
import train.TrainCalculateProbs;
import tree.Tree;
import treebank.Treebank;
import utils.LineWriter;

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
//	public static final String LOG_CONF = "D:\\Limudim\\OpenU\\2018b_22933_IntroToNLP\\hw\\hw4\\workspace\\nlp-mmn14\\src\\Pcfg\\conf\\logging.properties";
	public static final String LOG_CONF = "./conf/logging.properties";

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
		
		// 1. read input
		LOGGER.fine("args: " + Arrays.toString(args));
		LOGGER.info("reading gold treebank");
		Treebank myGoldTreebank = TreebankReader.getInstance().read(true, args[0]);
		LOGGER.info("finished reading gold treebank");
		LOGGER.info("reading train treebank");
		Treebank myTrainTreebank = TreebankReader.getInstance().read(true, args[1]);
		LOGGER.info("finished reading train treebank");
		
		// 2. transform trees
		LOGGER.info("transforming to CNF");
		myTrainTreebank = TrainCalculateProbs.getInstance().updateTreebankToCNF(myTrainTreebank, 0);
		writeParseTrees(args[2], myTrainTreebank.getAnalyses());
		
		// 3. train
		LOGGER.info("training");
		Grammar myGrammar = TrainCalculateProbs.getInstance().train(myTrainTreebank);

		// 4. decode
		LOGGER.info("decoding");
		List<Tree> myParseTrees = new ArrayList<Tree>();		
		for (int i = 0; i < myGoldTreebank.size(); i++) {
			List<String> mySentence = myGoldTreebank.getAnalyses().get(i).getYield();
			LOGGER.finer(String.format("decoding tree %d",i));
			Tree myParseTree = Decode.getInstance(myGrammar).decode(mySentence);
			myParseTrees.add(myParseTree);
		}

		// 5. de-transform trees
		writeParseTrees("parseBinarizing", myParseTrees);
		myParseTrees = TrainCalculateProbs.getInstance().deTransformTree(myParseTrees);
		writeParseTrees("parseDeBinarizing", myParseTrees);

		// 6. write output
		writeOutput(args[2], myGrammar, myParseTrees);	
	}
	
	
	private static void initLogging(String logConf) {
		try {
            LogManager.getLogManager().readConfiguration(new FileInputStream(logConf));
            LOGGER = Logger.getLogger(Parse.class.getName());
            LOGGER.info("Logging initiated");
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
	private static void writeParseTrees(String sExperimentName,
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
