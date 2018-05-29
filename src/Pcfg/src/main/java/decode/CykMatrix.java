package decode;

import static common.Consts.TOP;
import static java.lang.Double.NEGATIVE_INFINITY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Triplet;
import tree.Node;
import tree.Tree;

public class CykMatrix {

	private int n;
	private List<List<Map<String, Double>>> matrix;
	private Map<Triplet<Integer, Integer, String>, Triplet<Integer, String, String>> backTrace;
	private static final Triplet<Integer, String, String> TERMINAL_TRIPLET = new Triplet<>(-1, null, null);

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
		return matrix.get(row).get(realColIdx);
	}

	public Double get(int row, int col, String symbol) {
		int realColIdx = getRealColIdx(row, col); 
		assertIndicesInMatrixBounds(row, realColIdx);
		Map<String, Double> matrixCell = matrix.get(row).get(realColIdx);
		if (matrixCell == null) {
			return NEGATIVE_INFINITY;
		} else {
			return matrixCell.getOrDefault(symbol, NEGATIVE_INFINITY);
		}
	}

	private void assertIndicesInMatrixBounds(int row, int col) {
		assert row >= 0 && row < matrix.size();
		assert col >= 0 && col < matrix.get(row).size();
	}

	public void setBackTrace(int row, int col, String lhsSymbol,
			int childIdx, String rhsLeftSymbol, String rhsRightSymbol) {
		backTrace.put(new Triplet<>(row, getRealColIdx(row, col), lhsSymbol), new Triplet<>(childIdx, rhsLeftSymbol, rhsRightSymbol));
	}

	public Tree buildTree() {
		Triplet<Integer, Integer, String> rootTriplet = new Triplet<Integer, Integer, String>(0, n-1, TOP); // TODO : n or n - 1 ??? 
		Triplet<Integer, String, String> nextTriplet = backTrace.get(rootTriplet);  
		if(nextTriplet == null) {
			return null;
		}
		
		Node rootNode = new Node(TOP);
		Tree t = new Tree(rootNode);
		buildChild(rootNode, new Triplet<>(rootTriplet.a, nextTriplet.a, nextTriplet.b));		
		buildChild(rootNode, new Triplet<>(nextTriplet.a, rootTriplet.b, nextTriplet.c));		
		
		return t;
	}

	private void buildChild(Node node, Triplet<Integer, Integer, String> triplet) {
		Triplet<Integer, String, String> nextTriplet = backTrace.get(triplet);
		if(! TERMINAL_TRIPLET.equals(nextTriplet)) {
			Node leftNode = new Node(nextTriplet.b);
			node.addDaughter(leftNode);
			buildChild(leftNode, new Triplet<Integer, Integer, String>(triplet.a, nextTriplet.a, nextTriplet.b));
			Node rightNode = new Node(nextTriplet.c);
			node.addDaughter(rightNode);
			buildChild(rightNode, new Triplet<Integer, Integer, String>(nextTriplet.a, triplet.b, nextTriplet.c));
		}
	}
	
	private int getRealColIdx(int row, int col) {
		return col == row ? 0 :col - row - 1;
	}
	
}
