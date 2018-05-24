package decode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CykMatrix {
	
	private int n;
	private List<List<Map<String, Double>>> matrix;
	
	public CykMatrix(int n) {
		this.n = n;
		initMatrix();
	}

	private void initMatrix() {
		matrix = new ArrayList<List<Map<String, Double>>>(n);
		for(int i = 0; i < n; ++i) {
			List<Map<String, Double>> matrixSublist = new ArrayList<Map<String, Double>>(i+1);
			for(int j = 0; j < n-i; ++j) {
				matrixSublist.add(null);
			}
			matrix.add(matrixSublist);
		}
		
	}

}
