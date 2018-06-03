package decode;

import static common.Consts.PARENT_DEL;
import static common.Consts.TOP;
import static java.lang.Double.POSITIVE_INFINITY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import common.Consts;
import common.Triplet;
import tree.Node;
import tree.Terminal;
import tree.Tree;

public class CykMatrix {

	private static final Logger LOGGER = Logger.getLogger(CykMatrix.class.getName());
	
	private int n;
	private List<List<Map<String, Double>>> matrix;
	private Map<Triplet<Integer, Integer, String>, Triplet<Integer, String, String>> backTrace;

	public CykMatrix(int n) {
		this.n = n;
		initMatrix();
		this.backTrace = new HashMap<>();
	}

	private void initMatrix() {
		matrix = new ArrayList<List<Map<String, Double>>>(n);
		for (int i = 0; i < n; ++i) {
			List<Map<String, Double>> matrixSublist = new ArrayList<Map<String, Double>>(i + 1);
			for (int j = 0; j < n - i; ++j) {
				matrixSublist.add(null);
			}
			matrix.add(matrixSublist);
		}

	}

	public int n() {
		return this.n;
	}

	public void set(int row, int col, String symbol, Double logProb) {
		int realColIdx = getRealColIdx(row, col);
		assertIndicesInMatrixBounds(row, realColIdx);
		Map<String, Double> matrixCell = matrix.get(row).get(realColIdx);
		if (matrixCell == null) {
			matrixCell = new HashMap<>();
			matrix.get(row).set(realColIdx, matrixCell);
		}
		matrixCell.put(symbol, logProb);
	}

	public Map<String, Double> get(int row, int col) {
		int realColIdx = getRealColIdx(row, col);
		assertIndicesInMatrixBounds(row, realColIdx);
		Map<String, Double> cellProbs = matrix.get(row).get(realColIdx);
		if(cellProbs == null ) {
			cellProbs = new HashMap<>();
			matrix.get(row).add(realColIdx, cellProbs);
		}
		return cellProbs;
	}

	public Double get(int row, int col, String symbol) {
		int realColIdx = getRealColIdx(row, col); 
		assertIndicesInMatrixBounds(row, realColIdx);
		Map<String, Double> matrixCell = matrix.get(row).get(realColIdx);
		if (matrixCell == null) {
			return POSITIVE_INFINITY;
		} else {
			return matrixCell.getOrDefault(symbol, POSITIVE_INFINITY);
		}
	}

	private void assertIndicesInMatrixBounds(int row, int col) {
		assert row >= 0 && row < matrix.size();
		assert col >= 0 && col < matrix.get(row).size();
	}

	public void setBackTrace(int row, int col, String lhsSymbol,
			int childIdx, String rhsLeftSymbol, String rhsRightSymbol) {
		backTrace.put(new Triplet<>(row, col, lhsSymbol), new Triplet<>(childIdx, rhsLeftSymbol, rhsRightSymbol));
	}

	public Tree buildTree(Set<String> startSymbols) {
		Map<String, Double> startSymbolProbMap = matrix.get(0).get(n-1);
		if(startSymbolProbMap == null) {
			return null;
		}
		
		String startSymbol = null;
		Double prob = POSITIVE_INFINITY;
		for (Map.Entry<String, Double> startSymbolProb : startSymbolProbMap.entrySet()) {
			if(startSymbols.contains(startSymbolProb.getKey())) {
				if(startSymbolProb.getValue() > prob) {
					startSymbol = startSymbolProb.getKey();
					prob = startSymbolProb.getValue();
				}
			}
		}
		if(startSymbol == null) {
			return null;
		}
		
		Triplet<Integer, Integer, String> rootTriplet = new Triplet<>(0, n, startSymbol);
		
		Node rootNode = new Node(TOP);
		rootNode.setRoot(true);
		
		Tree t = new Tree(rootNode);
		
		Node childNode = new Node(startSymbol);
		rootNode.addDaughter(childNode);
		buildChild(childNode, rootTriplet);
		
		return t;
	}

	private void buildChild(Node node, Triplet<Integer, Integer, String> triplet) {
		Triplet<Integer, String, String> nextTriplet = backTrace.get(triplet);
		if(isTerminal(nextTriplet)) {
			Terminal terminal = new Terminal(nextTriplet.b);
			node.addDaughter(terminal);
		} else {
			Node leftNode = new Node(nextTriplet.b);
			node.addDaughter(leftNode);
			buildChild(leftNode, new Triplet<Integer, Integer, String>(triplet.a, nextTriplet.a, nextTriplet.b));
			if(nextTriplet.c != null) {
				Node rightNode = new Node(nextTriplet.c);
				if(rightNode.isArtificial()) { // only right nodes are artificial
					rightNode.addBrother(leftNode);
				}
				node.addDaughter(rightNode);
				buildChild(rightNode, new Triplet<Integer, Integer, String>(nextTriplet.a, triplet.b, nextTriplet.c));
			}
		}
	}
	
	private int getRealColIdx(int row, int col) {
		return col - row - 1;
	}
	
	private boolean isTerminal(Triplet<Integer, String, String> triplet) {
		return triplet.a == -1;
	}
	
}
