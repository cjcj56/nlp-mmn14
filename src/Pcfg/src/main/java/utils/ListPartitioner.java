package utils;

import java.util.HashMap;
import java.util.Map;

public class ListPartitioner {
	
	public static Map<Integer, Integer> partition(int items, int gridSize) {
		int numOfPartitions = (int)(items / gridSize);
		Map<Integer, Integer> partitionedItems = new HashMap<>();
		for (int i = 0; i < numOfPartitions; ++i) {
			partitionedItems.put(i*numOfPartitions, i*numOfPartitions + gridSize);
		}
		if(items % gridSize > 0) {
			int lastPartitionSize = numOfPartitions * gridSize - items;
			partitionedItems.put(items - lastPartitionSize, lastPartitionSize);
		}
		return partitionedItems;
	}

}
