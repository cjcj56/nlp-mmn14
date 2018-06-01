package common;

import bracketimport.TreebankReader;
import treebank.Treebank;

public abstract class AbstractTest {

	public static final String dataFolder = "../data";
	public static final String trainFile = dataFolder + "/heb-ctrees.train";
	public static final String goldFile = dataFolder + "/heb-ctrees.gold";
	
	protected Treebank trainTreebank; 
	protected Treebank goldTreebank; 
	
	public void readData() {
		this.trainTreebank = read(trainFile);
		this.goldTreebank = read(goldFile);
	}
	
	protected Treebank read(String pathToDataFile) {
		return TreebankReader.getInstance().read(true, pathToDataFile);
	}
	
	
}
