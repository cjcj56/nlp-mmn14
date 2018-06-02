package train;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import common.AbstractTest;
import tree.Tree;
import treebank.Treebank;

public class TrainCaculateProbTest extends AbstractTest {

	@Before
	public void beforeTest() {
		readData();
	}

	@Test
	public void binarizationDeBinarizationBasicTestMethod1() {
		for (int h = -5; h <= 5; ++h) {
			Treebank goldTreebankCopy = read(goldFile);
			goldTreebankCopy = TrainCalculateProbs.getInstance().updateTreebankToCNF(goldTreebankCopy, h);
			goldTreebankCopy = TrainCalculateProbs.getInstance().deTransformTreebank(goldTreebankCopy);
			Assert.assertTrue("gold data changed after binarizing and de-binarizing!",
					goldTreebankCopy.equals(goldTreebank));
			Treebank trainTreebankCopy = read(trainFile);
			trainTreebankCopy = TrainCalculateProbs.getInstance().updateTreebankToCNF(trainTreebankCopy, h);
			trainTreebankCopy = TrainCalculateProbs.getInstance().deTransformTreebank(trainTreebankCopy);
			Assert.assertTrue("train data changed after binarizing and de-binarizing!",
					goldTreebankCopy.equals(goldTreebank));
		}
	}

	@Test
	public void binarizationDeBinarizationBasicTestMethod2() {
		for (int h = -5; h <= 5; ++h) {
			Treebank goldTreebankCopy = read(goldFile);
			for (Tree tree : goldTreebankCopy.getAnalyses()) {
				tree.toCnf();
				tree.deCnf();
			}
			Assert.assertTrue("gold data changed after binarizing and de-binarizing!",
					goldTreebankCopy.equals(goldTreebank));
			Treebank trainTreebankCopy = read(trainFile);
			for (Tree tree : trainTreebankCopy.getAnalyses()) {
				tree.toCnf();
				tree.deCnf();
			}
			Assert.assertTrue("train data changed after binarizing and de-binarizing!",
					trainTreebankCopy.equals(trainTreebankCopy));
		}
	}

	@Test
	public void binarizationDeBinarizationBasicTestMixedMethods() {
		for (int h = -5; h <= 5; ++h) {
			Treebank goldTreebankCopy = read(goldFile);
			goldTreebankCopy = TrainCalculateProbs.getInstance().updateTreebankToCNF(goldTreebankCopy, h);
			goldTreebankCopy.deCnf();
			Assert.assertTrue("gold data changed after binarizing and de-binarizing!",
					goldTreebankCopy.equals(goldTreebank));
			Treebank trainTreebankCopy = read(trainFile);
			trainTreebankCopy.toCnf();
			trainTreebankCopy = TrainCalculateProbs.getInstance().deTransformTreebank(trainTreebankCopy);
			Assert.assertTrue("train data changed after binarizing and de-binarizing!",
					trainTreebankCopy.equals(trainTreebankCopy));
		}
	}

}
