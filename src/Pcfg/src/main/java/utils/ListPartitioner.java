package utils;

import java.util.HashMap;
import java.util.Map;

public class ListPartitioner {
	
	public static Map<Integer, Integer> partition(int items, int gridSize) {
		int numOfPartitions = (int)(items / gridSize) + 1;
		int lastPartitionSize = numOfPartitions * gridSize - items;
		Map<Integer, Integer> partitionedItems = new HashMap<>();
		for (int i = 0; i < numOfPartitions; ++i) {
			partitionedItems.put(i*numOfPartitions, i*numOfPartitions + gridSize - 1);
		}
		partitionedItems.put(numOfPartitions - 1, lastPartitionSize - 1);
		return partitionedItems;
	}

}
